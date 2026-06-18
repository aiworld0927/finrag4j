# FinRag4j Python 预处理微服务 API 文档

本文档详细描述了 FinRag4j Python 预处理微服务的所有 API 接口。

---

## 服务概述

FinRag4j Python 预处理微服务提供以下核心能力：

1. **文档解析** - 支持 PDF/Word/Excel/TXT 等多种格式文档解析
2. **离线 OCR** - 支持图片和扫描 PDF 的文字识别
3. **文本分块** - 支持金融文档专用分块策略

**服务地址**: `http://localhost:8001`
**API文档**: `http://localhost:8001/docs` (Swagger UI)

---

## 接口列表

| API路径 | 方法 | 功能描述 |
|---------|------|---------|
| `/health` | GET | 健康检查 |
| `/api/parse` | POST | 文档解析 |
| `/api/ocr` | POST | OCR识别 |
| `/api/chunk` | POST | 文本分块 |
| `/api/chunk/strategies` | GET | 获取分块策略 |

---

## 1. 健康检查接口

### 请求

```
GET /health
```

### 响应

```json
{
    "status": "healthy",
    "service": "finrag4j-python",
    "timestamp": "2024-01-15T10:30:00"
}
```

---

## 2. 文档解析接口

### 请求

```
POST /api/parse
```

#### 参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| file | File | 否 | 上传的文件（与file_path二选一） |
| file_path | String | 否 | 文件路径（与file二选一） |
| file_type | String | 否 | 文件类型（自动检测） |

#### 支持的文件类型

| 文件类型 | 扩展名 | MIME类型 |
|---------|--------|----------|
| PDF | .pdf | application/pdf |
| Word | .docx | application/vnd.openxmlformats-officedocument.wordprocessingml.document |
| Word | .doc | application/msword |
| Excel | .xlsx | application/vnd.openxmlformats-officedocument.spreadsheetml.sheet |
| Excel | .xls | application/vnd.ms-excel |
| TXT | .txt | text/plain |
| PPT | .pptx | application/vnd.openxmlformats-officedocument.presentationml.presentation |

### 响应

```json
{
    "success": true,
    "file_type": "pdf",
    "content": {
        "text": "文档的纯文本内容...",
        "tables": ["表格内容1", "表格内容2"],
        "pages": [
            {"page_number": 1, "text": "第一页内容"},
            {"page_number": 2, "text": "第二页内容"}
        ]
    },
    "message": "解析成功"
}
```

---

## 3. OCR识别接口

### 请求

```
POST /api/ocr
```

#### 参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| file | File | 否 | 上传的图片/PDF文件 |
| file_path | String | 否 | 文件路径 |
| preprocess | Boolean | 否 | 是否进行图像预处理（默认true） |

#### 支持的文件类型

| 文件类型 | 扩展名 |
|---------|--------|
| 图片 | .jpg, .jpeg, .png, .bmp, .tiff |
| 扫描PDF | .pdf |

### 响应

```json
{
    "success": true,
    "text": "识别到的完整文本内容...",
    "pages": [
        {
            "page_number": 1,
            "text": "第一页识别内容",
            "boxes": [
                {"box": [[0,0], [100,0], [100,20], [0,20]], "text": "文本", "confidence": 0.95}
            ]
        }
    ],
    "boxes": [],
    "message": "识别成功"
}
```

---

## 4. 文本分块接口

### 请求

```
POST /api/chunk
```

#### 参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| text | String | 是 | 待分块的文本内容 |
| strategy | String | 否 | 分块策略（默认regulation） |
| chunk_size | Integer | 否 | 自定义分块大小 |
| chunk_overlap | Integer | 否 | 自定义重叠大小 |

#### 分块策略说明

| 策略名称 | 适用场景 | 默认块大小 | 默认重叠 |
|---------|---------|-----------|---------|
| regulation | 监管文件、法律法规 | 600字符 | 150字符 |
| contract | 信贷合同、担保协议 | 800字符 | 200字符 |
| notice | 内部通知、公告 | 400字符 | 80字符 |

### 响应

```json
{
    "success": true,
    "chunks": [
        {
            "content": "第一个块的内容...",
            "section": "第一章",
            "start_pos": 0,
            "end_pos": 500,
            "sentence_count": 5
        },
        {
            "content": "第二个块的内容...",
            "section": "第二章",
            "start_pos": 400,
            "end_pos": 900,
            "sentence_count": 6
        }
    ],
    "strategy": "regulation",
    "total_chunks": 2,
    "message": "分块完成，共2个块"
}
```

---

## 5. 获取分块策略接口

### 请求

```
GET /api/chunk/strategies?strategy=regulation
```

#### 参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| strategy | String | 否 | 策略名称（不传返回所有） |

### 响应

```json
{
    "strategies": ["regulation", "contract", "notice"],
    "details": {
        "regulation": {
            "chunk_size": 600,
            "chunk_overlap": 150,
            "priority_sections": ["第一章", "第二章", "第一节", "第二条"]
        },
        "contract": {
            "chunk_size": 800,
            "chunk_overlap": 200,
            "priority_sections": ["第一条", "第二条", "甲方", "乙方"]
        },
        "notice": {
            "chunk_size": 400,
            "chunk_overlap": 80,
            "priority_sections": ["通知", "要求", "决定", "事项"]
        }
    }
}
```

---

## Java后端调用示例

以下是 Java 后端调用 Python 预处理服务的代码示例：

### 使用 RestTemplate 调用文档解析接口

```java
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

// 配置RestTemplate
RestTemplate restTemplate = new RestTemplate();
String pythonServiceUrl = "http://localhost:8001";

// 调用文档解析接口
public String parseDocument(String filePath) {
    String url = pythonServiceUrl + "/api/parse";
    
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    
    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add("file", new FileSystemResource(filePath));
    body.add("file_type", "pdf");
    
    HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);
    
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
    
    return response.getBody();
}
```

### 调用 OCR 识别接口

```java
public String recognizeOCR(String imagePath) {
    String url = pythonServiceUrl + "/api/ocr";
    
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    
    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add("file", new FileSystemResource(imagePath));
    body.add("preprocess", true);
    
    HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);
    
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
    
    return response.getBody();
}
```

### 调用文本分块接口

```java
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

public String chunkText(String text, String strategy) {
    String url = pythonServiceUrl + "/api/chunk";
    
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    
    // 构建请求体
    String requestBody = String.format(
        "{\"text\": \"%s\", \"strategy\": \"%s\", \"chunk_size\": 600, \"chunk_overlap\": 150}",
        text.replace("\"", "\\\""),
        strategy
    );
    
    HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
    
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
    
    return response.getBody();
}
```

---

## Python服务边界说明

### 职责范围（可实现）

1. ✅ 文档解析：PDF/Word/Excel/TXT/PPT 格式解析
2. ✅ 离线OCR识别：图片、扫描PDF文字识别
3. ✅ 图像预处理：矫正、去噪、增强
4. ✅ 文本清洗：去除停用词、特殊字符、空白
5. ✅ 文本分块：金融专用分块策略

### 职责边界（不可实现）

1. ❌ 数据库操作：无数据库依赖
2. ❌ 用户/租户/权限逻辑：不处理认证授权
3. ❌ 大模型调用：不直接调用大模型
4. ❌ 业务判断逻辑：不做业务决策
5. ❌ 对外暴露：仅接收Java后端调用
6. ❌ 前端接口：不直接对接前端

---

## 错误码说明

| HTTP状态码 | 错误类型 | 说明 |
|-----------|---------|------|
| 400 | Bad Request | 请求参数错误 |
| 404 | Not Found | 文件不存在 |
| 500 | Internal Server Error | 服务器内部错误 |

---

## 部署说明

### 使用 Docker Compose

```bash
cd deploy/docker-compose
docker-compose up -d
```

### 直接运行

```bash
cd backend/finrag4j-python
pip install -r requirements.txt
python main.py
```

---

**文档版本**: v1.0  
**最后更新**: 2024年