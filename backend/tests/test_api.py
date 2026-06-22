#!/usr/bin/env python3
"""
FinRag4j API接口测试脚本

测试范围:
- 认证管理 API (finrag4j-auth) - /api/auth/**
- 文档管理 API (finrag4j-document) - /api/document/**, /api/knowledge-base/**
- 搜索检索 API (finrag4j-search) - /api/rag/**, /api/vector/**
- 智能代理 API (finrag4j-agent) - /api/chat/**, /api/agent/**

运行方式:
python test_api.py
"""

import requests
import json
import sys
import traceback

# 配置
BASE_URL = "http://localhost:8080/api"
PYTHON_URL = "http://localhost:8002"
TIMEOUT = 30

# 全局变量
token = None
user_id = None
role_id = None
permission_id = None
kb_id = None
document_id = None
session_id = None

class APITester:
    def __init__(self, base_url):
        self.base_url = base_url
        self.session = requests.Session()
        self.session.timeout = TIMEOUT
        self.results = {
            "passed": 0,
            "failed": 0,
            "skipped": 0,
            "tests": []
        }
    
    def log(self, msg):
        print(f"[TEST] {msg}")
    
    def success(self, test_name, msg=""):
        self.results["passed"] += 1
        self.results["tests"].append({"name": test_name, "status": "PASS", "message": msg})
        self.log(f"✓ PASS: {test_name} {msg}")
    
    def fail(self, test_name, error):
        self.results["failed"] += 1
        self.results["tests"].append({"name": test_name, "status": "FAIL", "message": str(error)})
        self.log(f"✗ FAIL: {test_name} - {error}")
    
    def skip(self, test_name, reason):
        self.results["skipped"] += 1
        self.results["tests"].append({"name": test_name, "status": "SKIP", "message": reason})
        self.log(f"~ SKIP: {test_name} - {reason}")
    
    def print_summary(self):
        """打印测试总结"""
        print("\n" + "="*60)
        print(" API接口测试报告 ")
        print("="*60)
        print(f"总测试数: {len(self.results['tests'])}")
        print(f"通过: {self.results['passed']}")
        print(f"失败: {self.results['failed']}")
        print(f"跳过: {self.results['skipped']}")
        print("-"*60)
        
        # 打印失败的测试
        failed_tests = [t for t in self.results["tests"] if t["status"] == "FAIL"]
        if failed_tests:
            print("失败的测试:")
            for t in failed_tests:
                print(f"  - {t['name']}: {t['message']}")
        
        print("="*60)
        
        return self.results["failed"] == 0
    
    def test_auth_module(self):
        """测试认证管理模块"""
        global token, user_id, role_id, permission_id
        
        self.log("\n--- 测试认证管理模块 ---")
        
        # 1. 测试用户注册
        try:
            resp = self.session.post(
                f"{self.base_url}/auth/register",
                json={
                    "username": "testuser",
                    "password": "password123",
                    "email": "test@example.com",
                    "phone": "13800138000"
                }
            )
            if resp.status_code in [200, 400]:  # 400可能是用户名已存在
                self.success("用户注册")
            else:
                self.fail("用户注册", f"状态码: {resp.status_code}, 响应: {resp.text}")
        except Exception as e:
            self.fail("用户注册", str(e))
        
        # 2. 测试用户登录
        try:
            resp = self.session.post(
                f"{self.base_url}/auth/login",
                json={"username": "admin", "password": "admin123"}
            )
            if resp.status_code == 200:
                data = resp.json()
                token = data.get("data", {}).get("token", data.get("token"))
                if token:
                    self.session.headers["Authorization"] = f"Bearer {token}"
                    self.success("用户登录")
                else:
                    self.fail("用户登录", "未获取到token")
            else:
                self.fail("用户登录", f"状态码: {resp.status_code}, 响应: {resp.text}")
        except Exception as e:
            self.fail("用户登录", str(e))
        
        if not token:
            self.log("未获取到token，跳过后续需要认证的测试")
            return
        
        # 3. 测试获取当前用户信息
        try:
            resp = self.session.get(f"{self.base_url}/auth/me")
            if resp.status_code == 200:
                data = resp.json()
                user_id = data.get("data", {}).get("id")
                self.success("获取当前用户信息")
            else:
                self.fail("获取当前用户信息", f"状态码: {resp.status_code}")
        except Exception as e:
            self.fail("获取当前用户信息", str(e))
        
        # 4. 测试用户管理 - 创建用户
        try:
            resp = self.session.post(
                f"{self.base_url}/users",
                json={
                    "username": "api_test_user",
                    "password": "password123",
                    "email": "api_test@example.com",
                    "status": "active"
                }
            )
            if resp.status_code == 200:
                self.success("创建用户")
            else:
                self.fail("创建用户", f"状态码: {resp.status_code}, 响应: {resp.text}")
        except Exception as e:
            self.fail("创建用户", str(e))
        
        # 5. 测试用户管理 - 分页查询
        try:
            resp = self.session.get(f"{self.base_url}/users?pageNum=1&pageSize=10")
            if resp.status_code == 200:
                self.success("分页查询用户")
            else:
                self.fail("分页查询用户", f"状态码: {resp.status_code}")
        except Exception as e:
            self.fail("分页查询用户", str(e))
        
        # 6. 测试角色管理 - 创建角色
        try:
            resp = self.session.post(
                f"{self.base_url}/roles",
                json={
                    "roleName": "API测试角色",
                    "roleCode": "api_test_role",
                    "description": "用于API测试的角色"
                }
            )
            if resp.status_code == 200:
                self.success("创建角色")
            elif resp.status_code == 400:
                # 角色编码已存在
                self.success("创建角色(已存在)")
            else:
                self.fail("创建角色", f"状态码: {resp.status_code}")
        except Exception as e:
            self.fail("创建角色", str(e))
        
        # 7. 测试角色管理 - 查询所有角色
        try:
            resp = self.session.get(f"{self.base_url}/roles")
            if resp.status_code == 200:
                data = resp.json()
                if data.get("data"):
                    role_id = data["data"][0]["id"]
                self.success("查询所有角色")
            else:
                self.fail("查询所有角色", f"状态码: {resp.status_code}")
        except Exception as e:
            self.fail("查询所有角色", str(e))
        
        # 8. 测试权限管理 - 获取权限树
        try:
            resp = self.session.get(f"{self.base_url}/permissions/tree")
            if resp.status_code == 200:
                self.success("获取权限树")
            else:
                self.fail("获取权限树", f"状态码: {resp.status_code}")
        except Exception as e:
            self.fail("获取权限树", str(e))
    
    def test_document_module(self):
        """测试文档管理模块"""
        global kb_id, document_id
        
        if not token:
            self.skip("文档管理模块", "未登录")
            return
        
        self.log("\n--- 测试文档管理模块 ---")
        
        # 1. 测试知识库管理 - 创建知识库
        try:
            resp = self.session.post(
                f"{self.base_url}/knowledge-base",
                json={
                    "kbName": "API测试知识库",
                    "kbCode": "api_test_kb",
                    "description": "用于API测试的知识库"
                }
            )
            if resp.status_code == 200:
                self.success("创建知识库")
            elif resp.status_code == 400:
                # 知识库编码已存在
                self.success("创建知识库(已存在)")
            else:
                self.fail("创建知识库", f"状态码: {resp.status_code}, 响应: {resp.text}")
        except Exception as e:
            self.fail("创建知识库", str(e))
        
        # 2. 测试知识库管理 - 查询所有知识库
        try:
            resp = self.session.get(f"{self.base_url}/knowledge-base")
            if resp.status_code == 200:
                data = resp.json()
                if data.get("data"):
                    kb_id = data["data"][0]["id"]
                self.success("查询所有知识库")
            else:
                self.fail("查询所有知识库", f"状态码: {resp.status_code}")
        except Exception as e:
            self.fail("查询所有知识库", str(e))
        
        # 3. 测试文档管理 - 分页查询文档
        try:
            resp = self.session.get(f"{self.base_url}/document?pageNum=1&pageSize=10")
            if resp.status_code == 200:
                self.success("分页查询文档")
            else:
                self.fail("分页查询文档", f"状态码: {resp.status_code}")
        except Exception as e:
            self.fail("分页查询文档", str(e))
    
    def test_search_module(self):
        """测试搜索检索模块"""
        if not token:
            self.skip("搜索检索模块", "未登录")
            return
        
        self.log("\n--- 测试搜索检索模块 ---")
        
        # 1. 测试向量检索 - 语义搜索
        try:
            resp = self.session.post(
                f"{self.base_url}/rag/search",
                json={"query": "测试搜索", "kbId": kb_id, "topK": 5}
            )
            if resp.status_code == 200:
                self.success("语义搜索")
            else:
                self.fail("语义搜索", f"状态码: {resp.status_code}")
        except Exception as e:
            self.fail("语义搜索", str(e))
        
        # 2. 测试向量检索 - 关键词搜索
        try:
            resp = self.session.post(
                f"{self.base_url}/rag/keyword-search",
                json={"query": "测试", "kbId": kb_id, "topK": 5}
            )
            if resp.status_code == 200:
                self.success("关键词搜索")
            else:
                self.fail("关键词搜索", f"状态码: {resp.status_code}")
        except Exception as e:
            self.fail("关键词搜索", str(e))
        
        # 3. 测试向量检索 - 混合检索
        try:
            resp = self.session.post(
                f"{self.base_url}/rag/retrieve",
                json={
                    "query": "测试检索",
                    "kbId": kb_id,
                    "topK": 10,
                    "similarityThreshold": 0.7,
                    "enableRerank": True
                }
            )
            if resp.status_code == 200:
                self.success("混合检索")
            else:
                self.fail("混合检索", f"状态码: {resp.status_code}")
        except Exception as e:
            self.fail("混合检索", str(e))
    
    def test_agent_module(self):
        """测试智能代理模块"""
        global session_id
        
        if not token:
            self.skip("智能代理模块", "未登录")
            return
        
        self.log("\n--- 测试智能代理模块 ---")
        
        # 1. 测试创建会话
        try:
            resp = self.session.post(
                f"{self.base_url}/chat/session/create",
                json={"title": "API测试会话", "kbId": kb_id, "agentType": "rag"}
            )
            if resp.status_code == 200:
                data = resp.json()
                session_id = data.get("data", {}).get("sessionId", data.get("sessionId"))
                self.success("创建会话")
            else:
                self.fail("创建会话", f"状态码: {resp.status_code}, 响应: {resp.text}")
        except Exception as e:
            self.fail("创建会话", str(e))
        
        # 2. 测试发送消息
        if session_id:
            try:
                resp = self.session.post(
                    f"{self.base_url}/chat/send",
                    json={
                        "sessionId": session_id,
                        "message": "你好",
                        "kbId": kb_id,
                        "agentType": "rag",
                        "useRerank": True
                    }
                )
                if resp.status_code == 200:
                    self.success("发送消息")
                else:
                    self.fail("发送消息", f"状态码: {resp.status_code}, 响应: {resp.text}")
            except Exception as e:
                self.fail("发送消息", str(e))
        else:
            self.skip("发送消息", "未创建会话")
        
        # 3. 测试获取聊天历史
        if session_id:
            try:
                resp = self.session.get(f"{self.base_url}/chat/history/{session_id}?pageNum=1&pageSize=20")
                if resp.status_code == 200:
                    self.success("获取聊天历史")
                else:
                    self.fail("获取聊天历史", f"状态码: {resp.status_code}")
            except Exception as e:
                self.fail("获取聊天历史", str(e))
        else:
            self.skip("获取聊天历史", "未创建会话")
    
    def test_python_module(self):
        """测试Python预处理模块"""
        self.log("\n--- 测试Python预处理模块 ---")
        
        # 1. 测试健康检查
        try:
            resp = requests.get(f"http://localhost:8002/health", timeout=TIMEOUT)
            if resp.status_code == 200:
                data = resp.json()
                if data.get("status") == "healthy":
                    self.success("Python服务健康检查")
                else:
                    self.fail("Python服务健康检查", f"状态: {data.get('status')}")
            else:
                self.fail("Python服务健康检查", f"状态码: {resp.status_code}")
        except requests.exceptions.ConnectionError:
            self.skip("Python服务健康检查", "Python服务未启动")
        except Exception as e:
            self.fail("Python服务健康检查", str(e))
    
    def run_all_tests(self):
        """运行所有测试"""
        self.log("="*60)
        self.log(" FinRag4j API接口测试开始 ")
        self.log("="*60)
        
        try:
            self.test_auth_module()
            self.test_document_module()
            self.test_search_module()
            self.test_agent_module()
            self.test_python_module()
        except Exception as e:
            self.log(f"测试执行异常: {e}")
            traceback.print_exc()
        
        return self.print_summary()

if __name__ == "__main__":
    tester = APITester(BASE_URL)
    success = tester.run_all_tests()
    sys.exit(0 if success else 1)