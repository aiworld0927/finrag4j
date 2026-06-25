# Git 文件历史查询指南

## 文档信息
- 版本：v1.0
- 更新日期：2026-06-25
- 适用范围：FinRag4j项目团队

---

## 1. 基础查询命令

### 1.1 查看提交历史

```bash
# 查看所有提交历史（简洁版）
git log

# 查看最近N条提交
git log -n 5

# 查看提交历史（单行模式）
git log --oneline

# 查看提交历史（含图形化分支）
git log --oneline --graph --all
```

### 1.2 查看文件修改历史

```bash
# 查看指定文件的提交历史
git log --oneline -- <filename>

# 查看指定文件的详细修改历史（含变更内容）
git log -p -- <filename>

# 查看指定文件的修改统计
git log --stat -- <filename>
```

---

## 2. 详细查询

### 2.1 查看某次提交的详细信息

```bash
# 查看提交详情
git show <commit-hash>

# 查看提交的变更内容
git show <commit-hash> --stat

# 查看提交的具体代码变更
git show <commit-hash> -- <filename>
```

### 2.2 查找特定内容的变更

```bash
# 查找包含特定关键词的提交
git log --all --grep="关键词"

# 查找修改了特定内容的提交
git log -S "搜索内容" -- <filename>

# 查找修改了特定函数/方法的提交
git log -L :function_name:filename
```

### 2.3 查看文件的版本演变

```bash
# 查看文件的每一次修改
git log --follow -p -- <filename>

# 查看文件在不同版本中的内容
git show <commit-hash>:<filename>

# 比较文件在两个版本之间的差异
git diff <commit-hash1> <commit-hash2> -- <filename>
```

---

## 3. 高级查询

### 3.1 按作者查询

```bash
# 查看指定作者的提交
git log --author="作者名"

# 查看指定作者在指定时间范围内的提交
git log --author="作者名" --since="2026-01-01" --until="2026-06-30"
```

### 3.2 按时间查询

```bash
# 查看最近N天的提交
git log --since="7 days ago"

# 查看指定日期范围内的提交
git log --since="2026-06-01" --until="2026-06-30"

# 查看今天的提交
git log --since="00:00" --until="23:59"
```

### 3.3 查看分支合并历史

```bash
# 查看合并提交
git log --merges

# 查看非合并提交
git log --no-merges

# 查看特定分支的提交
git log <branch-name>

# 查看两个分支之间的差异提交
git log master..feature-branch
```

---

## 4. 实用场景

### 4.1 找回误删的文件

```bash
# 查找删除文件的提交
git log --all --full-history -- <deleted-file>

# 恢复删除的文件
git checkout <commit-hash> -- <deleted-file>
```

### 4.2 查看谁修改了某行代码

```bash
# 查看指定文件指定行的修改历史
git blame -L 100,150 <filename>

# 查看指定文件指定行的详细修改历史
git log -L 100,150:<filename>
```

### 4.3 查看某功能的开发历程

```bash
# 通过标签查看版本演进
git log --oneline --tags --simplify-by-decoration

# 查看从某个版本到当前的变更
git log v1.0.0..HEAD
```

---

## 5. 图形化工具

### 5.1 Git GUI

```bash
# 打开图形化界面
git gui

# 打开日志查看器
gitk
```

### 5.2 VS Code 集成

在 VS Code 中：
- 安装 GitLens 插件
- 点击左侧源代码管理图标
- 右键点击文件选择 "查看历史"

---

## 6. 常用命令速查表

| 命令 | 用途 |
|------|------|
| `git log` | 查看提交历史 |
| `git log --oneline` | 单行显示提交历史 |
| `git log -p -- <file>` | 查看文件详细修改历史 |
| `git show <hash>` | 查看提交详情 |
| `git blame <file>` | 查看文件逐行修改记录 |
| `git log --author="name"` | 按作者查询 |
| `git log --grep="keyword"` | 按关键词查询 |
| `git diff <hash1> <hash2>` | 比较两个提交 |
| `git log --since="7 days"` | 按时间查询 |
