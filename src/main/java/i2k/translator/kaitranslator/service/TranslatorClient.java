package i2k.translator.kaitranslator.service;

import reactor.core.publisher.Mono;

public interface TranslatorClient {
    Mono<String> translateChunk(String chunk, String sourceLang, String targetLang);
}
