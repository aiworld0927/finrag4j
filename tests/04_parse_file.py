"""
FinRag4j - 文档解析接口测试

测试目标：验证 /api/parse/file 接口的文档解析功能
"""
import requests
import sys


def test_parse_file():
    """测试文档解析接口"""
    base_url = "http://localhost:8001"
    endpoint = "/api/parse/file"

    print("=" * 60)
    print("测试：文档解析接口 /api/parse/file")
    print("=" * 60)

    # 测试文件内容
    test_files = [
        {
            "name": "TXT文件测试",
            "content": "这是一个测试文档。\n包含金融相关内容。\n\n第一章 总则\n\n第一条 为了规范金融市场秩序，制定本条例。",
            "file_type": "txt",
            "expected_type": "txt"
        },
        {
            "name": "纯文本无格式",
            "content": "简单文本测试",
            "file_type": "txt",
            "expected_type": "txt"
        },
        {
            "name": "多行文本",
            "content": "第一行\n第二行\n第三行\n\n空行后继续\n\n最后一行",
            "file_type": "txt",
            "expected_type": "txt"
        }
    ]

    all_passed = True

    for i, test_file in enumerate(test_files, 1):
        print(f"\n[测试用例 {i}: {test_file['name']}]")
        print(f"  文件类型: {test_file['file_type']}")
        print(f"  内容长度: {len(test_file['content'])} 字符")

        try:
            # 创建文件对象
            files = {
                'file': (f'test_{i}.{test_file["file_type"]}',
                        test_file['content'].encode('utf-8'),
                        'text/plain')
            }

            response = requests.post(
                f"{base_url}{endpoint}",
                files=files,
                timeout=10
            )
            data = response.json()

            print(f"  状态码: {response.status_code}")
            print(f"  成功: {data.get('success')}")
            print(f"  文件类型: {data.get('file_type')}")
            print(f"  页面数量: {data.get('page_count')}")
            print(f"  提取文本长度: {len(data.get('text', ''))} 字符")
            print(f"  消息: {data.get('message')}")

            # 显示提取的文本预览
            extracted_text = data.get('text', '')
            if extracted_text:
                print(f"  文本预览: {extracted_text[:80]}...")

            # 验证
            if response.status_code == 200 and data.get('success'):
                if data.get('file_type') == test_file['expected_type']:
                    print(f"  [PASS] 用例通过")
                else:
                    print(f"  [WARN] 文件类型不匹配")
            else:
                print(f"  [FAIL] 用例失败: {data.get('message')}")
                all_passed = False

        except requests.exceptions.ConnectionError:
            print(f"  [FAIL] 连接失败：服务未启动")
            all_passed = False
        except Exception as e:
            print(f"  [FAIL] 测试异常: {e}")
            all_passed = False

    return all_passed


def test_parse_file_with_content_type():
    """测试文档解析接口 - 使用正确的内容类型"""
    base_url = "http://localhost:8001"
    endpoint = "/api/parse/file"

    print("\n" + "-" * 60)
    print("[额外测试：表单字段方式]")

    try:
        content = "测试内容：这是一个金融合同文本。甲方：银行。乙方：客户。"
        files = {'file': ('contract.txt', content, 'text/plain')}

        response = requests.post(f"{base_url}{endpoint}", files=files)
        data = response.json()

        print(f"  状态码: {response.status_code}")
        print(f"  成功: {data.get('success')}")

        if data.get('success'):
            print("  [PASS] 表单字段方式验证通过")
            return True
        else:
            print(f"  [FAIL] 失败: {data.get('message')}")
            return False

    except Exception as e:
        print(f"  [FAIL] 测试异常: {e}")
        return False


if __name__ == "__main__":
    print("\nFinRag4j Python 服务 - 文档解析测试")
    print("-" * 60)

    success1 = test_parse_file()
    success2 = test_parse_file_with_content_type()

    print("\n" + "=" * 60)
    if success1 and success2:
        print("测试结果: [PASS] 全部通过")
        sys.exit(0)
    else:
        print("测试结果: [FAIL] 部分失败")
        sys.exit(1)
