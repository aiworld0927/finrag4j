"""
FinRag4j - 文本清洗接口测试

测试目标：验证 /api/text/clean 接口的文本清洗功能
"""
import requests
import sys


def test_text_clean():
    """测试文本清洗接口"""
    base_url = "http://localhost:8001"
    endpoint = "/api/text/clean"

    print("=" * 60)
    print("测试：文本清洗接口 /api/text/clean")
    print("=" * 60)

    # 测试数据
    test_cases = [
        {
            "name": "基本文本清洗",
            "data": {
                "text": "这是一个测试文档。包含一些特殊字符！@#$%",
                "remove_header_footer": True,
                "remove_watermark": True,
                "remove_empty_lines": True,
                "remove_junk_text": True
            }
        },
        {
            "name": "纯文本清洗",
            "data": {
                "text": "金融文本：本公司2024年年度报告显示，营业收入同比增长15.6%。",
                "remove_header_footer": True,
                "remove_watermark": True,
                "remove_empty_lines": True,
                "remove_junk_text": True
            }
        },
        {
            "name": "长文本清洗",
            "data": {
                "text": "第一章 总则\n\n第一条 为规范公司治理，保护投资者权益，制定本章程。\n\n第二条 公司应当依法合规经营。\n\n" * 5,
                "remove_header_footer": False,
                "remove_watermark": True,
                "remove_empty_lines": True,
                "remove_junk_text": True
            }
        }
    ]

    all_passed = True

    for i, test_case in enumerate(test_cases, 1):
        print(f"\n[测试用例 {i}: {test_case['name']}]")
        print(f"  输入文本: {test_case['data']['text'][:50]}...")

        try:
            response = requests.post(
                f"{base_url}{endpoint}",
                json=test_case['data'],
                timeout=10
            )
            data = response.json()

            print(f"  状态码: {response.status_code}")
            print(f"  成功: {data.get('success')}")
            print(f"  原始长度: {data.get('original_length')}")
            print(f"  清洗后长度: {data.get('cleaned_length')}")
            print(f"  消息: {data.get('message')}")

            # 验证
            if response.status_code == 200 and data.get('success'):
                print(f"  [PASS] 用例通过")
            else:
                print(f"  [FAIL] 用例失败")
                all_passed = False

        except requests.exceptions.ConnectionError:
            print(f"  [FAIL] 连接失败：服务未启动")
            all_passed = False
        except Exception as e:
            print(f"  [FAIL] 测试异常: {e}")
            all_passed = False

    return all_passed


if __name__ == "__main__":
    print("\nFinRag4j Python 服务 - 文本清洗测试")
    print("-" * 60)

    success = test_text_clean()

    print("\n" + "=" * 60)
    if success:
        print("测试结果: [PASS] 全部通过")
        sys.exit(0)
    else:
        print("测试结果: [FAIL] 部分失败")
        sys.exit(1)
