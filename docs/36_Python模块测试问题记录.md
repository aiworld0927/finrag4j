# Python 模块测试问题记录

## 文档版本
- 版本：v1.0
- 创建日期：2026-06-19
- 更新日期：2026-06-19

---

## 1. 测试环境

| 项目 | 版本/配置 |
|------|----------|
| Python | 3.12 |
| uv | 0.11.7 |
| 操作系统 | Windows |
| PaddlePaddle | 3.3.1 |
| PaddleOCR | 3.7.0 |

---

## 2. 测试过程中发现的问题

### 2.1 pyproject.toml 配置问题

#### 问题1：uv 工具配置字段错误

**错误信息**：
```
TOML parse error at line 50, column 1
  |
50 | resolver = "pip"
     | ^^^^^^^^
unknown field `resolver`
```

**原因**：`[tool.uv]` 段存在未知字段，`resolver` 不是有效的 uv 配置项。

**解决方案**：移除以下不兼容的字段：
- `resolver = "pip"` → 移除（uv 使用自己的解析器）
- `required-version = "3.12"` → 移除（这是指定 uv 版本而非 Python 版本）

**正确配置**：
```toml
[tool.uv]
python-preference = "managed"
venv = ".venv"
```

---

#### 问题2：License Classifier 冲突

**错误信息**：
```
setuptools.errors.InvalidConfigError: License classifiers have been superseded by license expressions.
Please remove: License :: OSI Approved :: Apache Software License
```

**原因**：新版 setuptools 不允许同时使用 `license` 字段和 `License` classifier。

**解决方案**：移除 classifier 中的 License 项：
```toml
classifiers = [
    "Development Status :: 4 - Beta",
    "Intended Audience :: Financial and Insurance Industry",
    "Programming Language :: Python :: 3.12",
    "Operating System :: OS Independent",
]
```

---

#### 问题3：依赖声明格式错误

**错误信息**：
```
invalid type: map, expected a sequence
```

**原因**：`dependencies` 使用了 map 格式而非列表格式。

**解决方案**：使用列表格式声明依赖：
```toml
dependencies = [
    "fastapi>=0.110.0",
    "uvicorn>=0.29.0",
    # ...
]
```

---

### 2.2 运行时依赖问题

#### 问题4：pdfminer.six 缺失

**错误信息**：
```
ModuleNotFoundError: No module named 'pdfminer'
```

**原因**：`unstructured` 库需要 `pdfminer.six` 作为依赖，但未自动安装。

**解决方案**：手动添加依赖：
```bash
uv add pdfminer.six
```

---

#### 问题5：pi-heif 缺失

**错误信息**：
```
ModuleNotFoundError: No module named 'pi_heif'
```

**原因**：`unstructured` 库需要 `pi-heif` 处理 HEIF 格式图片。

**解决方案**：手动添加依赖：
```bash
uv add pi-heif
```

---

#### 问题6：python-magic-bin 缺失

**错误信息**：
```
ModuleNotFoundError: No module named 'magic'
```

**原因**：文件类型检测需要 `python-magic-bin`。

**解决方案**：
```bash
uv add python-magic-bin
```

---

### 2.3 代码兼容性问题

#### 问题7：PaddleOCR API 参数不兼容

**错误信息**：
```
ValueError: Unknown argument: use_gpu
```

**原因**：新版 PaddleOCR API 不支持 `use_gpu`、`det_model_dir` 等参数。

**解决方案**：简化初始化参数：
```python
self.ocr = PaddleOCR(
    use_angle_cls=True,
    lang=OCR_LANG
)
```

---

#### 问题8：PaddleOCR ocr 方法参数不兼容

**错误信息**：
```
PaddleOCR.predict() got an unexpected keyword argument 'cls'
```

**原因**：新版 PaddleOCR API 不支持 `cls=True` 参数。

**解决方案**：移除 `cls` 参数：
```python
ocr_result = self.ocr.ocr(temp_path)
```

---

#### 问题9：OCRService.recognize_bytes 方法参数缺失

**错误信息**：
```
OCRService.recognize_bytes() takes from 2 to 3 positional arguments but 4 were given
```

**原因**：`recognize_bytes` 方法缺少 `preprocess` 参数定义。

**解决方案**：添加 `preprocess` 参数：
```python
def recognize_bytes(self, file_bytes: bytes, file_type: str = "image", preprocess: bool = True) -> Dict[str, Any]:
```

---

### 2.4 unstructured 库依赖链问题

#### 问题10：unstructured 库依赖过多

**问题描述**：
`unstructured` 库有大量传递依赖，在 Windows 环境下安装复杂：
- pdfminer.six
- pi-heif
- python-magic-bin
- msgpack
- unstructured-inference
- 等数十个依赖

**影响**：
- 安装时间长（158+ 依赖包）
- 部分依赖在 Windows 下编译困难
- 版本兼容性问题多

**解决方案**：
移除 `unstructured` 依赖，使用基础库直接实现文档解析：
- PDF：使用 `PyPDF2`
- Word：使用 `python-docx`
- Excel：使用 `openpyxl`
- PPT：使用 `python-pptx`
- TXT：直接读取

---

### 2.5 PaddlePaddle oneDNN 兼容性问题

#### 问题11：OCR 识别失败（环境特定）

**错误信息**：
```
(Unimplemented) ConvertPirAttribute2RuntimeAttribute not support [pir::ArrayAttribute<pir::DoubleAttribute>]
(at paddle/fluid/framework/new_executor/instruction/onednn/onednn_instruction.cc:118)
```

**原因**：PaddlePaddle oneDNN 库与当前 CPU 架构不兼容。

**影响**：OCR 接口无法在当前环境正常工作。

**解决方案**：
1. 在支持 AVX2 的 CPU 环境下运行
2. 或安装纯 CPU 版本的 PaddlePaddle（不带 oneDNN）
3. 或使用 GPU 版本

**临时替代方案**：
文档解析服务使用 PyPDF2/python-docx 等库进行文本提取，OCR 功能需要修复环境后使用。

---

## 3. 问题汇总表

| 序号 | 问题类型 | 问题描述 | 严重程度 | 状态 |
|------|---------|---------|---------|------|
| 1 | 配置错误 | uv 配置字段不兼容 | 高 | 已修复 |
| 2 | 配置错误 | License classifier 冲突 | 高 | 已修复 |
| 3 | 配置错误 | 依赖格式错误 | 高 | 已修复 |
| 4 | 依赖缺失 | pdfminer.six 缺失 | 中 | 已修复 |
| 5 | 依赖缺失 | pi-heif 缺失 | 中 | 已修复 |
| 6 | 依赖缺失 | python-magic-bin 缺失 | 中 | 已修复 |
| 7 | API 不兼容 | PaddleOCR 参数不兼容 | 中 | 已修复 |
| 8 | API 不兼容 | PaddleOCR cls 参数不兼容 | 中 | 已修复 |
| 9 | 代码缺陷 | recognize_bytes 参数缺失 | 中 | 已修复 |
| 10 | 依赖复杂 | unstructured 依赖过多 | 中 | 已修复 |
| 11 | 环境问题 | oneDNN 兼容性 | 低 | 待环境修复 |

---

## 4. 修复后的 pyproject.toml

```toml
[build-system]
requires = ["setuptools>=61.0"]
build-backend = "setuptools.build_meta"

[project]
name = "finrag4j-python"
version = "1.0.0"
description = "FinRag4j Python预处理微服务 - 文档解析、OCR识别、文本分块"
readme = "README.md"
requires-python = ">=3.12"
license = "Apache-2.0"
authors = [
    { name = "FinRag4j Team", email = "wangjn1130@163.com" }
]
classifiers = [
    "Development Status :: 4 - Beta",
    "Intended Audience :: Financial and Insurance Industry",
    "Programming Language :: Python :: 3.12",
    "Operating System :: OS Independent",
]
keywords = ["finrag4j", "rag", "ocr", "document-processing", "finance"]
dependencies = [
    "fastapi>=0.110.0",
    "uvicorn>=0.29.0",
    "python-multipart>=0.0.9",
    "pydantic>=2.6.4",
    "requests>=2.31.0",
    "python-dotenv>=1.0.1",
    "loguru>=0.7.2",
    "nacos-sdk-python>=1.4.0",
    "python-docx>=1.1.0",
    "openpyxl>=3.1.2",
    "python-pptx>=0.6.23",
    "PyPDF2>=3.0.1",
    "paddlepaddle>=2.6.1",
    "paddleocr>=2.8.2",
    "opencv-python>=4.9.0",
    "pillow>=10.2.0",
    "jieba>=0.42.1",
    "nltk>=3.8.1",
    "regex>=2023.12.25",
]

[project.scripts]
finrag4j-python = "main:main"

[project.optional-dependencies]
dev = [
    "pytest>=7.4.0",
    "pytest-asyncio>=0.21.0",
    "httpx>=0.27.0",
    "black>=23.10.0",
    "isort>=5.12.0",
    "flake8>=6.1.0",
]

[tool.black]
line-length = 88
target-version = ["py312"]

[tool.isort]
profile = "black"
multi_line_output = 3
include_trailing_comma = true
```

---

## 5. 后续建议

1. **OCR 方案替代**：考虑使用 `EasyOCR` 或 `Tesseract` 作为 PaddleOCR 的替代方案
2. **依赖管理**：定期更新 `pyproject.toml`，清理不必要的依赖
3. **测试覆盖**：增加单元测试和集成测试，提高代码质量
4. **环境标准化**：使用 Docker 确保开发环境一致性
