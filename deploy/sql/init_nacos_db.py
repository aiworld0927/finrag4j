"""
Nacos 数据库初始化脚本
用于在 PostgreSQL 中创建 Nacos 所需的数据库和表结构

使用方式:
1. 自动模式: docker-compose 会自动执行
2. 手动模式: python init_nacos_db.py
"""
import psycopg2
from psycopg2.extensions import ISOLATION_LEVEL_AUTOCOMMIT
import sys
import os

# Nacos 数据库配置
DB_NAME = os.getenv("NACOS_DB", "finrag4j_nacos")
DB_USER = os.getenv("POSTGRES_USER", "postgres")
DB_PASSWORD = os.getenv("POSTGRES_PASSWORD", "postgres")
DB_HOST = os.getenv("POSTGRES_HOST", "localhost")
DB_PORT = int(os.getenv("POSTGRES_PORT", "5432"))

# SQL脚本路径（与当前脚本同目录）
SQL_SCRIPT_PATH = os.path.join(os.path.dirname(__file__), "nacos_postgresql.sql")


def create_database():
    """创建数据库（如果不存在）"""
    print(f"[INFO] 检查数据库 '{DB_NAME}' 是否存在...")

    try:
        # 连接默认数据库 postgres
        conn = psycopg2.connect(
            host=DB_HOST,
            port=DB_PORT,
            user=DB_USER,
            password=DB_PASSWORD,
            database="postgres"
        )
        conn.set_isolation_level(ISOLATION_LEVEL_AUTOCOMMIT)
        cursor = conn.cursor()

        # 检查数据库是否存在
        cursor.execute(f"SELECT 1 FROM pg_database WHERE datname = '{DB_NAME}'")
        exists = cursor.fetchone()

        if not exists:
            print(f"[INFO] 创建数据库 '{DB_NAME}'...")
            cursor.execute(f"CREATE DATABASE {DB_NAME}")
            print(f"[OK] 数据库 '{DB_NAME}' 创建成功")
        else:
            print(f"[INFO] 数据库 '{DB_NAME}' 已存在，跳过创建")

        cursor.close()
        conn.close()
        return True

    except psycopg2.Error as e:
        print(f"[ERROR] 创建数据库失败: {e}")
        return False


def execute_sql_script():
    """执行 SQL 脚本创建表结构"""
    print(f"[INFO] 执行 SQL 脚本创建表结构...")

    if not os.path.exists(SQL_SCRIPT_PATH):
        print(f"[ERROR] SQL 脚本不存在: {SQL_SCRIPT_PATH}")
        return False

    try:
        # 连接目标数据库
        conn = psycopg2.connect(
            host=DB_HOST,
            port=DB_PORT,
            user=DB_USER,
            password=DB_PASSWORD,
            database=DB_NAME
        )
        cursor = conn.cursor()

        # 读取并执行 SQL 脚本
        with open(SQL_SCRIPT_PATH, 'r', encoding='utf-8') as f:
            sql_content = f.read()

        # 分割并执行每条 SQL 语句
        statements = sql_content.split(';')
        for i, statement in enumerate(statements):
            statement = statement.strip()
            if statement and not statement.startswith('--'):
                try:
                    cursor.execute(statement)
                    print(f"[OK] 执行语句 {i+1}: {statement[:50]}...")
                except psycopg2.Error as e:
                    # 忽略已存在的表等错误
                    if "already exists" in str(e):
                        print(f"[WARN] 跳过已存在的对象")
                    else:
                        print(f"[WARN] 语句执行警告: {e}")

        conn.commit()
        cursor.close()
        conn.close()

        print(f"[OK] SQL 脚本执行完成")
        return True

    except psycopg2.Error as e:
        print(f"[ERROR] 执行 SQL 脚本失败: {e}")
        return False
    except Exception as e:
        print(f"[ERROR] 未知错误: {e}")
        return False


def check_connection():
    """检查数据库连接"""
    print(f"[INFO] 检查数据库连接...")
    print(f"       Host: {DB_HOST}")
    print(f"       Port: {DB_PORT}")
    print(f"       User: {DB_USER}")
    print(f"       Database: {DB_NAME}")

    try:
        conn = psycopg2.connect(
            host=DB_HOST,
            port=DB_PORT,
            user=DB_USER,
            password=DB_PASSWORD,
            database="postgres"
        )
        conn.close()
        print(f"[OK] 数据库连接成功")
        return True
    except psycopg2.Error as e:
        print(f"[ERROR] 数据库连接失败: {e}")
        return False


def main():
    """主函数"""
    print("=" * 60)
    print("Nacos 数据库初始化脚本")
    print("=" * 60)

    # 检查连接
    if not check_connection():
        sys.exit(1)

    # 创建数据库
    if not create_database():
        sys.exit(1)

    # 执行 SQL 脚本
    if not execute_sql_script():
        sys.exit(1)

    print("=" * 60)
    print("初始化完成！")
    print("=" * 60)
    print(f"\nNacos 默认用户名: nacos")
    print(f"Nacos 默认密码: nacos")
    print(f"\n请访问: http://{DB_HOST}:8848/nacos")


if __name__ == "__main__":
    main()
