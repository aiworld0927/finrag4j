"""
中间件服务健康检查脚本
用于检测所有中间件服务是否正常运行
"""
import socket
import sys
from typing import Dict, List, Tuple


class MiddlewareChecker:
    """中间件检查器"""

    # 中间件服务配置
    SERVICES = {
        "Nacos": {"host": "11.0.1.190", "port": 8848},
        "PostgreSQL": {"host": "11.0.1.190", "port": 5432},
        "Redis": {"host": "11.0.1.190", "port": 6379},
        "MinIO": {"host": "11.0.1.190", "port": 9000},
        "RocketMQ-NameServer": {"host": "11.0.1.190", "port": 9876},
        "RocketMQ-Broker": {"host": "11.0.1.190", "port": 10911},
        "RocketMQ-Dashboard": {"host": "11.0.1.190", "port": 8088},
    }

    def __init__(self, config_path: str = None):
        """初始化检查器

        Args:
            config_path: 可选的配置文件路径，用于覆盖默认配置
        """
        if config_path:
            self._load_config(config_path)

    def _load_config(self, config_path: str):
        """从环境变量文件加载配置"""
        try:
            with open(config_path, 'r', encoding='utf-8') as f:
                for line in f:
                    line = line.strip()
                    if not line or line.startswith('#'):
                        continue
                    if '=' in line:
                        key, value = line.split('=', 1)
                        key = key.strip()
                        value = value.strip()

                        # 映射环境变量到服务配置
                        host_map = {
                            'NACOS_HOST': 'Nacos',
                            'POSTGRES_HOST': 'PostgreSQL',
                            'REDIS_HOST': 'Redis',
                            'MINIO_HOST': 'MinIO',
                            'ROCKETMQ_NAMESRV_HOST': 'RocketMQ-NameServer',
                        }
                        port_map = {
                            'NACOS_PORT': 'Nacos',
                            'POSTGRES_PORT': 'PostgreSQL',
                            'REDIS_PORT': 'Redis',
                            'MINIO_PORT': 'MinIO',
                            'ROCKETMQ_NAMESRV_PORT': 'RocketMQ-NameServer',
                            'ROCKETMQ_BROKER_PORT': 'RocketMQ-Broker',
                            'ROCKETMQ_DASHBOARD_PORT': 'RocketMQ-Dashboard',
                        }

                        if key in host_map:
                            service = host_map[key]
                            if service in self.SERVICES:
                                self.SERVICES[service]['host'] = value

                        if key in port_map:
                            service = port_map[key]
                            if service in self.SERVICES:
                                self.SERVICES[service]['port'] = int(value)
        except FileNotFoundError:
            print(f"配置文件不存在: {config_path}, 使用默认配置")
        except Exception as e:
            print(f"加载配置文件失败: {e}, 使用默认配置")

    def check_port(self, host: str, port: int, timeout: float = 3.0) -> Tuple[bool, str]:
        """检测端口是否开放

        Args:
            host: 主机地址
            port: 端口号
            timeout: 超时时间(秒)

        Returns:
            (是否成功, 错误信息)
        """
        try:
            sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            sock.settimeout(timeout)
            result = sock.connect_ex((host, port))
            sock.close()

            if result == 0:
                return True, ""
            else:
                return False, f"端口 {port} 未开放"
        except socket.timeout:
            return False, "连接超时"
        except socket.gaierror:
            return False, f"无法解析主机名: {host}"
        except Exception as e:
            return False, str(e)

    def check_service(self, name: str, host: str, port: int) -> Dict:
        """检查单个服务

        Returns:
            包含检查结果的字典
        """
        success, error = self.check_port(host, port)

        return {
            "name": name,
            "host": host,
            "port": port,
            "status": "UP" if success else "DOWN",
            "error": error if error else None
        }

    def check_all(self) -> List[Dict]:
        """检查所有中间件服务"""
        results = []
        for name, config in self.SERVICES.items():
            result = self.check_service(
                name,
                config["host"],
                config["port"]
            )
            results.append(result)
        return results

    def print_report(self, results: List[Dict] = None):
        """打印检查报告

        Args:
            results: 检查结果列表，如果为None则重新检查
        """
        if results is None:
            results = self.check_all()

        print("\n" + "=" * 60)
        print("中间件服务健康检查报告")
        print("=" * 60)

        up_count = 0
        down_count = 0

        for r in results:
            status_icon = "[OK]" if r["status"] == "UP" else "[FAIL]"
            status_color = "\033[92m" if r["status"] == "UP" else "\033[91m"
            reset_color = "\033[0m"

            print(f"\n{status_color}{status_icon}{reset_color} {r['name']}")
            print(f"    地址: {r['host']}:{r['port']}")

            if r["status"] == "DOWN":
                print(f"    错误: {r['error']}")
                down_count += 1
            else:
                up_count += 1

        print("\n" + "-" * 60)
        print(f"服务状态: {up_count} UP / {down_count} DOWN")
        print("=" * 60)

        # 返回退出码
        return 0 if down_count == 0 else 1


def main():
    """主函数"""
    import argparse

    parser = argparse.ArgumentParser(description="中间件服务健康检查")
    parser.add_argument(
        "-c", "--config",
        help="环境配置文件路径 (.env)",
        default=None
    )
    parser.add_argument(
        "--host",
        help="覆盖所有服务的HOST (主要用于快速测试)",
        default=None
    )
    args = parser.parse_args()

    checker = MiddlewareChecker(args.config)

    # 如果指定了host，覆盖所有配置
    if args.host:
        for service in checker.SERVICES.values():
            service["host"] = args.host

    # 执行检查并打印报告
    results = checker.check_all()
    exit_code = checker.print_report(results)

    sys.exit(exit_code)


if __name__ == "__main__":
    main()
