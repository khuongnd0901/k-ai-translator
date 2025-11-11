package i2k.translator.kaitranslator.service;

import i2k.translator.kaitranslator.config.AppProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public class OpenAiTranslatorClient implements TranslatorClient {

    private final WebClient client;
    private final AppProperties props;

    public OpenAiTranslatorClient(AppProperties props, WebClient.Builder builder) {
        this.props = props;
        String apiKey = props.getOpenai().getApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            System.err.println("[WARN] OPENAI_API_KEY chưa được cấu hình. API gọi sẽ thất bại.");
        }
        this.client = builder
                .baseUrl(props.getOpenai().getBaseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + (apiKey == null ? "" : apiKey))
                .build();
    }

    @Override
    public Mono<String> translateChunk(String chunk, String sourceLang, String targetLang) {
        String system = "You are a professional translator. Translate the user's content to '" + targetLang + "'. " +
                "Preserve meaning and tone. Keep original line breaks. Return ONLY the translated text.";
        String user = (sourceLang != null && !sourceLang.isBlank()
                        ? ("[Source language: " + sourceLang + "]\n") : "") + chunk;

        Map<String, Object> body = Map.of(
                "model", props.getModel(),
                "messages", new Object[]{
                        Map.of("role", "system", "content", system),
                        Map.of("role", "user", "content", user)
                },
                "temperature", 0.2
        );

        return client.post()
                .uri("/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .map(resp -> {
                    // Extract choices[0].message.content
                    Object choices = resp.get("choices");
                    if (choices instanceof java.util.List<?> list && !list.isEmpty()) {
                        Object first = list.get(0);
                        if (first instanceof Map<?,?> m) {
                            Object msg = m.get("message");
                            if (msg instanceof Map<?,?> mm) {
                                Object content = mm.get("content");
                                if (content != null) return content.toString();
                            }
                        }
                    }
                    throw new RuntimeException("Phản hồi OpenAI không hợp lệ: " + resp);
                });
    }
}
