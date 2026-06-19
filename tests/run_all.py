"""
FinRag4j - 测试运行器

运行所有测试用例并生成报告
"""
import os
import sys
import subprocess
import time
from datetime import datetime


def run_test(test_file):
    """运行单个测试文件"""
    print(f"\n{'='*60}")
    print(f"运行: {test_file}")
    print(f"{'='*60}")

    try:
        result = subprocess.run(
            [sys.executable, test_file],
            capture_output=True,
            text=True,
            timeout=60
        )

        print(result.stdout)
        if result.stderr:
            print("STDERR:", result.stderr)

        return result.returncode == 0

    except subprocess.TimeoutExpired:
        print(f"[FAIL] 测试超时: {test_file}")
        return False
    except Exception as e:
        print(f"[FAIL] 运行失败: {e}")
        return False


def main():
    """主函数"""
    print("\n" + "=" * 60)
    print("FinRag4j - 测试套件")
    print(f"运行时间: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    print("=" * 60)

    # 获取测试目录
    test_dir = os.path.dirname(os.path.abspath(__file__))

    # 查找所有测试文件
    test_files = [
        "01_health_check.py",
        "02_text_clean.py",
        "03_text_split.py",
        "04_parse_file.py",
        "05_ocr.py",
    ]

    # 过滤存在的测试文件
    existing_tests = []
    for test_file in test_files:
        full_path = os.path.join(test_dir, test_file)
        if os.path.exists(full_path):
            existing_tests.append(full_path)
        else:
            print(f"[WARN] 跳过不存在的测试: {test_file}")

    if not existing_tests:
        print("[FAIL] 未找到任何测试文件")
        sys.exit(1)

    # 运行测试
    results = {}
    for test_path in existing_tests:
        test_name = os.path.basename(test_path)
        print(f"\n\n>>> 开始测试: {test_name}")
        results[test_name] = run_test(test_path)
        time.sleep(0.5)  # 避免请求过快

    # 生成报告
    print("\n" + "=" * 60)
    print("测试报告")
    print("=" * 60)

    passed = sum(1 for v in results.values() if v)
    total = len(results)

    for test_name, success in results.items():
        status = "[PASS]" if success else "[FAIL]"
        print(f"  {status}  {test_name}")

    print("\n" + "-" * 60)
    print(f"总计: {passed}/{total} 通过")

    if passed == total:
        print("\n所有测试通过！")
        sys.exit(0)
    else:
        print(f"\n{total - passed} 个测试失败")
        sys.exit(1)


if __name__ == "__main__":
    main()
