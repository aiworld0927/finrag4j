# Git 文件历史查询指南

本文档说明如何查询 Git 仓库中文件的历史变更，以及如何诊断文件丢失或配置错误问题。

## 1. 案例背景

### 问题描述
在 commit `c437283` (2026年6月20日) 中，`docker-compose-base.yml` 被更新以引用以下 SQL 文件：
- `deploy/sql/01_create_nacos_user.sql`
- `deploy/sql/02_pg_schema.sql`
- `deploy/sql/03_pg_grant.sql`

但这些文件实际上是**空目录**，并未作为 SQL 文件存在。

### 问题根源
这些 `.sql` 文件被 `.gitignore` 第 83 行的 `*.sql` 规则忽略，因此：
- 从未被 Git 跟踪
- 无法在 Git 历史中查询到
- docker-compose 引用了不存在的文件

## 2. Git 查询命令

### 2.1 查询文件在 Git 历史中的变更

```bash
# 查询特定文件的所有历史记录
git log --oneline --all -- <文件路径>

# 查询文件的详细变更历史
git log -p --follow -- <文件路径>

# 查询何时添加/删除了特定内容
git log -S "关键字" --all -- <文件路径>

# 示例：查询 docker-compose-base.yml 中引用 SQL 文件的历史
git log -p -S "01_create_nacos_user.sql" -- deploy/docker-compose-base.yml
```

### 2.2 查询文件在特定提交中的状态

```bash
# 查看文件在某次提交中的内容
git show <提交哈希>:<文件路径>

# 示例：查看 c437283 提交时的 docker-compose-base.yml
git show c437283:deploy/docker-compose-base.yml

# 查看某次提交时的目录结构
git ls-tree -r <提交哈希> -- <目录路径>
# 示例
git ls-tree -r c437283 -- deploy/sql/
```

### 2.3 查询被 Git 忽略的文件

```bash
# 检查文件是否被 .gitignore 忽略
git check-ignore -v <文件路径>

# 示例：检查 SQL 文件是否被忽略
git check-ignore -v deploy/sql/01_create_nacos_user.sql
# 输出：.gitignore:83:*.sql     deploy/sql/01_create_nacos_user.sql

# 查看所有被忽略的文件
git status --ignored
```

### 2.4 查询文件的 Git 对象类型

```bash
# 查看文件在 Git 中的对象类型
git cat-file -t <对象哈希>

# 查看文件内容
git cat-file -p <对象哈希>

# 检查文件是否在 Git 中存在
git ls-files <文件路径>
```

### 2.5 查询谁在何时修改了文件

```bash
# 查看文件的详细修改历史（包含作者和日期）
git log --format="%h %an %ad %s" --date=short -- <文件路径>

# 示例
git log --format="%h %an %ad %s" --date=short -- deploy/docker-compose-base.yml

# 查看某次提交的完整信息
git show <提交哈希> --stat
```

### 2.6 比较不同提交中的文件差异

```bash
# 比较文件在两次提交中的差异
git diff <提交1>..<提交2> -- <文件路径>

# 示例：比较 c437283 和前一个提交
git log --oneline c437283^..c437283
# 获取前一个提交的哈希
git diff <前一个提交哈希>..c437283 -- deploy/docker-compose-base.yml
```

## 3. 诊断步骤总结

当发现配置文件引用了不存在的文件时，按以下步骤诊断：

### 步骤 1：检查文件是否存在
```bash
ls -la <文件路径>
# 检查是文件(-)还是目录(d)
```

### 步骤 2：检查 Git 状态
```bash
git status <文件路径>
# 如果显示 "nothing to commit"，说明文件未被跟踪
```

### 步骤 3：检查是否被 .gitignore 忽略
```bash
git check-ignore -v <文件路径>
# 如果有输出，说明文件被忽略
```

### 步骤 4：查询 Git 历史
```bash
git log --all --oneline -- <文件路径>
# 如果没有输出，说明文件从未被提交过

# 查询引用该文件的配置的变更历史
git log -p -S "<文件名>" -- <引用该文件的配置>
```

### 步骤 5：查看 .gitignore 规则
```bash
cat .gitignore | grep -n <文件扩展名或模式>
```

## 4. 解决方案

### 4.1 对于本案例的 SQL 文件问题

有两种解决方案：

#### 方案 A：从官方 Nacos 仓库获取 SQL 文件

Nacos 3.2.2 官方 PostgreSQL 插件仓库：
- GitHub: https://github.com/nacos-group/nacos-plugin
- PostgreSQL 插件路径: `nacos-postgresql-datasource-plugin-ext/src/main/resources/schema`

```bash
# 克隆 Nacos 插件仓库
git clone https://github.com/nacos-group/nacos-plugin.git
# 查找 PostgreSQL schema 文件
find . -name "*pg*.sql"
```

#### 方案 B：从社区插件仓库获取

社区维护的 PostgreSQL 支持：
- https://github.com/wuchubuzai2018/nacos-datasource-extend-plugins

下载 `postgresql-schema.sql` 文件。

### 4.2 修复步骤

1. **获取官方 SQL 文件**（参考上述方案）

2. **创建 SQL 文件**：
   ```bash
   # 创建用户和数据库脚本
   touch deploy/sql/01_create_nacos_user.sql
   
   # 创建表结构脚本
   touch deploy/sql/02_pg_schema.sql
   
   # 创建权限脚本
   touch deploy/sql/03_pg_grant.sql
   ```

3. **更新 .gitignore**（可选）：
   ```bash
   # 在 .gitignore 中添加例外
   # 在 *.sql 行后添加：
   !deploy/sql/01_create_nacos_user.sql
   !deploy/sql/02_pg_schema.sql
   !deploy/sql/03_pg_grant.sql
   ```

4. **提交文件**：
   ```bash
   git add deploy/sql/*.sql
   git commit -m "Add Nacos PostgreSQL initialization scripts"
   git push
   ```

## 5. 相关文档

- [Nacos PostgreSQL 配置说明](./37_Nacos_PostgreSQL配置说明.md)
- [中间件依赖关系说明](./39_中间件依赖关系说明.md)
- [多环境运行指南](./35_多环境运行指南.md)

## 6. 参考链接

- [Nacos 官方数据源插件文档](https://nacos.io/docs/latest/plugin/datasource-plugin/)
- [Nacos PostgreSQL 支持社区插件](https://github.com/wuchubuzai2018/nacos-datasource-extend-plugins)
- [Nacos 官方插件仓库](https://github.com/nacos-group/nacos-plugin)
