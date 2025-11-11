package i2k.translator.kaitranslator.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "translator")
public class AppProperties {
    private String provider = "openai";
    private String model = "gpt-4o-mini";
    private String targetLang = "vi";
    private Chunk chunk = new Chunk();
    private int parallelism = 4;
    private OpenAi openai = new OpenAi();

    public static class Chunk {
        private int maxChars = 1800;
        public int getMaxChars() { return maxChars; }
        public void setMaxChars(int maxChars) { this.maxChars = maxChars; }
    }

    public static class OpenAi {
        private String baseUrl = "https://api.openai.com/v1";
        private String apiKey;
        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
        public String getApiKey() { return apiKey; }
        public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public String getTargetLang() { return targetLang; }
    public void setTargetLang(String targetLang) { this.targetLang = targetLang; }
    public Chunk getChunk() { return chunk; }
    public void setChunk(Chunk chunk) { this.chunk = chunk; }
    public int getParallelism() { return parallelism; }
    public void setParallelism(int parallelism) { this.parallelism = parallelism; }
    public OpenAi getOpenai() { return openai; }
    public void setOpenai(OpenAi openai) { this.openai = openai; }
}
