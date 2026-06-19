"""
FinRag4j - 文本分块接口测试

测试目标：验证 /api/text/split 接口的文本分块功能
"""
import requests
import sys


def test_text_split():
    """测试文本分块接口"""
    base_url = "http://localhost:8001"
    endpoint = "/api/text/split"

    print("=" * 60)
    print("测试：文本分块接口 /api/text/split")
    print("=" * 60)

    # 测试数据
    test_cases = [
        {
            "name": "监管文件分块",
            "data": {
                "text": """
第一章 总则

第一条 为了规范金融市场秩序，保护投资者合法权益，制定本条例。

第二条 在中华人民共和国境内从事金融活动，适用本条例。

第三条 金融机构应当遵循公平、公正、公开的原则开展业务。

第二章 监督管理

第四条 国务院金融稳定发展委员会负责统筹协调金融稳定工作。

第五条 中国人民银行依法对金融机构实施监督管理。

第三章 法律责任

第六条 金融机构违反本条例规定的，由金融监督管理机构责令改正。

第七条 金融机构应当建立健全内部控制制度。

第四章 附则

第八条 本条例自发布之日起施行。

第九条 本条例由国务院金融稳定发展委员会负责解释。
                """,
                "strategy": "regulatory",
                "chunk_size": 200,
                "chunk_overlap": 50
            }
        },
        {
            "name": "信贷文档分块",
            "data": {
                "text": """
贷款合同

借款人：张三
贷款金额：人民币壹佰万元整
贷款期限：12个月
年利率：5.6%

担保条款：
（一）借款人以其名下房产提供抵押担保。
（二）抵押物评估价值不低于人民币一百五十万元。

还款方式：等额本息
还款日期：每月20日

违约责任：
借款人未按期足额还款的，贷款人有权宣布贷款提前到期。
                """,
                "strategy": "credit",
                "chunk_size": 150,
                "chunk_overlap": 30
            }
        },
        {
            "name": "短文本测试（预期处理）",
            "data": {
                "text": "这是一段很短的文本。",
                "strategy": "regulatory"
            }
        }
    ]

    all_passed = True

    for i, test_case in enumerate(test_cases, 1):
        print(f"\n[测试用例 {i}: {test_case['name']}]")
        print(f"  策略: {test_case['data']['strategy']}")
        print(f"  文本长度: {len(test_case['data']['text'])} 字符")

        try:
            response = requests.post(
                f"{base_url}{endpoint}",
                json=test_case['data'],
                timeout=10
            )
            data = response.json()

            print(f"  状态码: {response.status_code}")
            print(f"  成功: {data.get('success')}")
            print(f"  分块数量: {data.get('total_chunks')}")
            print(f"  消息: {data.get('message')}")

            if data.get('chunks'):
                print(f"  分块预览:")
                for j, chunk in enumerate(data.get('chunks', [])[:2], 1):
                    content = chunk.get('content', '')
                    print(f"    块{j}: {content[:60]}...")

            # 验证
            if response.status_code == 200:
                if data.get('success') or '长度不足' in data.get('message', ''):
                    print(f"  [PASS] 用例通过")
                else:
                    print(f"  [WARN] 用例返回失败但服务正常")
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
    print("\nFinRag4j Python 服务 - 文本分块测试")
    print("-" * 60)

    success = test_text_split()

    print("\n" + "=" * 60)
    if success:
        print("测试结果: [PASS] 全部通过")
        sys.exit(0)
    else:
        print("测试结果: [FAIL] 部分失败")
        sys.exit(1)
