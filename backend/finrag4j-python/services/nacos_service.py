"""
Nacos 服务注册与发现模块

功能：
1. 服务启动时自动注册到 Nacos
2. 服务停止时自动注销
3. 心跳保活（SDK 自动处理）
4. 服务发现
"""

import asyncio
import socket
from typing import Optional, List, Dict, Any
from loguru import logger

from config import (
    NACOS_ENABLED,
    NACOS_HOST,
    NACOS_PORT,
    NACOS_NAMESPACE,
    NACOS_USERNAME,
    NACOS_PASSWORD,
    NACOS_TIMEOUT,
    NACOS_SERVICE_NAME,
    NACOS_SERVICE_GROUP,
    NACOS_SERVICE_WEIGHT,
    NACOS_SERVICE_CLUSTER,
    NACOS_SERVICE_EPHEMERAL,
    SERVER_PORT
)


class NacosServiceRegistry:
    """Nacos 服务注册管理器"""

    def __init__(self):
        self.naming_client = None
        self._registered = False
        self._service_ip = None
        self._service_port = SERVER_PORT

    def _get_local_ip(self) -> str:
        """获取本机 IP 地址"""
        try:
            # 创建一个 UDP socket 连接来获取本机 IP
            s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
            # 连接到 Nacos 服务器地址（不需要真正连接）
            s.connect((NACOS_HOST, NACOS_PORT))
            local_ip = s.getsockname()[0]
            s.close()
            return local_ip
        except Exception:
            # 如果获取失败，返回 localhost
            return "127.0.0.1"

    async def init_client(self) -> bool:
        """
        初始化 Nacos Naming 客户端
        :return: 是否初始化成功
        """
        if not NACOS_ENABLED:
            logger.info("Nacos 服务注册已禁用")
            return False

        try:
            from v2.nacos import NacosNamingService, ClientConfigBuilder, GRPCConfig
            import os

            logger.info(f"正在初始化 Nacos Naming 客户端: {NACOS_HOST}:{NACOS_PORT}")

            # 设置日志和缓存目录到当前项目目录
            project_dir = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
            log_dir = os.path.join(project_dir, "logs", "nacos")
            cache_dir = os.path.join(project_dir, "cache", "nacos")
            
            # 创建目录
            os.makedirs(log_dir, exist_ok=True)
            os.makedirs(cache_dir, exist_ok=True)

            # 构建客户端配置
            client_config = (ClientConfigBuilder()
                .server_address(f"{NACOS_HOST}:{NACOS_PORT}")
                .username(NACOS_USERNAME)
                .password(NACOS_PASSWORD)
                .namespace_id(NACOS_NAMESPACE)
                .log_level('WARNING')
                .log_dir(log_dir)
                .cache_dir(cache_dir)
                .grpc_config(GRPCConfig(grpc_timeout=NACOS_TIMEOUT * 1000))
                .build())

            # 创建 Naming 客户端
            self.naming_client = await NacosNamingService.create_naming_service(client_config)
            logger.info(f"Nacos Naming 客户端初始化成功: {NACOS_HOST}:{NACOS_PORT}")
            return True

        except ImportError:
            logger.warning("未安装 nacos-sdk-python，跳过服务注册。请运行: pip install nacos-sdk-python")
            return False
        except Exception as e:
            logger.error(f"Nacos Naming 客户端初始化失败: {e}")
            return False

    async def register(self, ip: Optional[str] = None, port: Optional[int] = None) -> bool:
        """
        注册服务到 Nacos
        :param ip: 服务 IP，默认自动获取
        :param port: 服务端口，默认使用配置中的端口
        :return: 是否注册成功
        """
        if not NACOS_ENABLED:
            logger.info("Nacos 服务注册已禁用，跳过注册")
            return False

        if self.naming_client is None:
            logger.warning("Nacos Naming 客户端未初始化，跳过注册")
            return False

        try:
            from v2.nacos import RegisterInstanceParam

            # 获取服务 IP 和端口
            self._service_ip = ip or self._get_local_ip()
            self._service_port = port or SERVER_PORT

            # 构建注册参数
            register_param = RegisterInstanceParam(
                service_name=NACOS_SERVICE_NAME,
                group_name=NACOS_SERVICE_GROUP,
                ip=self._service_ip,
                port=self._service_port,
                weight=NACOS_SERVICE_WEIGHT,
                cluster_name=NACOS_SERVICE_CLUSTER,
                metadata={
                    "version": "1.0.0",
                    "preserved.register.source": "PYTHON"
                },
                enabled=True,
                healthy=True,
                ephemeral=NACOS_SERVICE_EPHEMERAL
            )

            # 注册服务
            await self.naming_client.register_instance(request=register_param)
            self._registered = True

            logger.info(f"服务注册成功: {NACOS_SERVICE_NAME} -> {self._service_ip}:{self._service_port}")
            return True

        except Exception as e:
            logger.error(f"服务注册失败: {e}")
            return False

    async def deregister(self) -> bool:
        """
        从 Nacos 注销服务
        :return: 是否注销成功
        """
        if not self._registered or self.naming_client is None:
            return False

        try:
            from v2.nacos import DeregisterInstanceParam

            # 构建注销参数
            deregister_param = DeregisterInstanceParam(
                service_name=NACOS_SERVICE_NAME,
                group_name=NACOS_SERVICE_GROUP,
                ip=self._service_ip,
                port=self._service_port,
                cluster_name=NACOS_SERVICE_CLUSTER,
                ephemeral=NACOS_SERVICE_EPHEMERAL
            )

            # 注销服务
            await self.naming_client.deregister_instance(request=deregister_param)
            self._registered = False

            logger.info(f"服务注销成功: {NACOS_SERVICE_NAME}")
            return True

        except Exception as e:
            logger.error(f"服务注销失败: {e}")
            return False

    async def discover(self, service_name: str, group_name: str = "DEFAULT_GROUP") -> List[Dict[str, Any]]:
        """
        发现服务实例
        :param service_name: 服务名称
        :param group_name: 服务分组
        :return: 服务实例列表
        """
        if self.naming_client is None:
            logger.warning("Nacos Naming 客户端未初始化")
            return []

        try:
            from v2.nacos import ListInstanceParam

            # 构建查询参数
            list_param = ListInstanceParam(
                service_name=service_name,
                group_name=group_name,
                healthy_only=True
            )

            # 查询服务实例
            instances = await self.naming_client.list_instances(list_param)

            result = []
            for instance in instances:
                result.append({
                    "ip": instance.ip,
                    "port": instance.port,
                    "weight": instance.weight,
                    "healthy": instance.healthy,
                    "enabled": instance.enabled,
                    "metadata": instance.metadata
                })

            return result

        except Exception as e:
            logger.error(f"服务发现失败: {e}")
            return []

    async def shutdown(self):
        """关闭客户端"""
        if self.naming_client is not None:
            try:
                await self.naming_client.shutdown()
                logger.info("Nacos Naming 客户端已关闭")
            except Exception as e:
                logger.error(f"关闭 Nacos Naming 客户端失败: {e}")
            finally:
                self.naming_client = None
                self._registered = False

    @property
    def is_registered(self) -> bool:
        """是否已注册"""
        return self._registered

    @property
    def service_info(self) -> Dict[str, Any]:
        """获取服务信息"""
        return {
            "service_name": NACOS_SERVICE_NAME,
            "group": NACOS_SERVICE_GROUP,
            "ip": self._service_ip,
            "port": self._service_port,
            "weight": NACOS_SERVICE_WEIGHT,
            "cluster": NACOS_SERVICE_CLUSTER,
            "ephemeral": NACOS_SERVICE_EPHEMERAL,
            "registered": self._registered
        }


# 全局服务注册管理器实例
nacos_registry = NacosServiceRegistry()
