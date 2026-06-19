"""
FinRag4j - 健康检查接口测试

测试目标：验证 /health 接口返回正确的服务状态
"""
import requests
import sys


def test_health_check():
    """测试健康检查接口"""
    base_url = "http://localhost:8001"
    endpoint = "/health"

    print("=" * 60)
    print("测试：健康检查接口 /health")
    print("=" * 60)

    try:
        response = requests.get(f"{base_url}{endpoint}", timeout=5)
        data = response.json()

        print(f"\n[请求信息]")
        print(f"  URL: {base_url}{endpoint}")
        print(f"  Method: GET")
        print(f"  Status Code: {response.status_code}")

        print(f"\n[响应数据]")
        print(f"  status: {data.get('status')}")
        print(f"  service: {data.get('service')}")
        print(f"  version: {data.get('version')}")
        print(f"  nacos_enabled: {data.get('nacos_enabled')}")
        print(f"  message: {data.get('message')}")

        # 验证响应
        print(f"\n[验证结果]")
        assert response.status_code == 200, f"状态码错误: {response.status_code}"
        assert data.get('status') == 'healthy', f"服务状态错误: {data.get('status')}"
        assert data.get('service') == 'finrag4j-python', f"服务名称错误: {data.get('service')}"
        assert 'version' in data, "缺少 version 字段"

        print("  [PASS] 所有验证通过")
        return True

    except requests.exceptions.ConnectionError:
        print(f"\n  [FAIL] 连接失败：服务可能未启动")
        print(f"  请先启动服务: uv run python main.py")
        return False
    except AssertionError as e:
        print(f"\n  [FAIL] 验证失败: {e}")
        return False
    except Exception as e:
        print(f"\n  [FAIL] 测试异常: {e}")
        return False


if __name__ == "__main__":
    print("\nFinRag4j Python 服务 - 健康检查测试")
    print("-" * 60)

    success = test_health_check()

    print("\n" + "=" * 60)
    if success:
        print("测试结果: [PASS] 通过")
        sys.exit(0)
    else:
        print("测试结果: [FAIL] 失败")
        sys.exit(1)
