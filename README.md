# spring-ai-translator-webflux

Ứng dụng Spring Boot 3 (WebFlux, JDK 21) dịch tài liệu **PDF/DOCX/TXT** sang ngôn ngữ đích (mặc định: `vi`).
- Upload file qua API reactive.
- Tách văn bản theo chunk, gửi song song tới OpenAI (có thể thay thế provider).
- Hợp nhất theo đúng thứ tự, xuất ra **TXT/DOCX/PDF**.
  
## Chạy nhanh

```bash
# Yêu cầu: JDK 21 + Maven
export OPENAI_API_KEY=sk-xxxx
export TRANSLATOR_PROVIDER=openai        # hoặc để trống cũng mặc định openai
export TRANSLATOR_MODEL=gpt-4o-mini      # tuỳ chọn
export TRANSLATOR_TARGET_LANG=vi         # ngôn ngữ đích mặc định

mvn -q spring-boot:run
```

Gọi API:
```bash
curl -X POST "http://localhost:8080/api/translate?targetLang=vi&out=docx"   -H "Content-Type: multipart/form-data"   -F "file=@/path/to/input.pdf"   --output translated.docx
```

## Endpoint

- `POST /api/translate?targetLang=vi&out=txt|docx|pdf`  
  Body: `multipart/form-data` với `file` là tài liệu cần dịch.
  Trả về file đã dịch (kiểu octet-stream).

- `GET /api/health` kiểm tra trạng thái.

## Cấu hình (application.yml)

- `translator.provider`: `openai` (mặc định)
- `translator.model`: `gpt-4o-mini`
- `translator.targetLang`: `vi`
- `translator.chunk.maxChars`: 1800
- `translator.parallelism`: 4

Hoặc qua biến môi trường: `TRANSLATOR_PROVIDER`, `TRANSLATOR_MODEL`, `TRANSLATOR_TARGET_LANG`.

## Lưu ý
- PDF có layout phức tạp sẽ chỉ trích xuất text theo dòng (không giữ định dạng gốc).
- Nếu muốn dùng Ollama/local LLM, tạo client khác implement `TranslatorClient`.
