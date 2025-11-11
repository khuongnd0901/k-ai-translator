package i2k.translator.kaitranslator.service;

import i2k.translator.kaitranslator.config.AppProperties;
import i2k.translator.kaitranslator.util.TextChunker;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Một lớp service chịu trách nhiệm xử lý các thao tác dịch văn bản. Service này
 * thực hiện việc chia nhỏ các văn bản lớn thành các đoạn có thể quản lý được,
 * dịch chúng song song, và tổng hợp lại kết quả.
 * <p>
 * Service này sử dụng chiến lược phân đoạn và các thuộc tính song song có thể cấu hình
 * được định nghĩa trong {@link AppProperties}. Quá trình dịch được xử lý bởi một
 * {@link TranslatorClient} implementation, thực hiện việc dịch thực tế
 * các đoạn văn bản.
 */
@Service
public class TranslationService {

    private final AppProperties props;
    private final TranslatorClient translator;

    public TranslationService(AppProperties props, TranslatorClient translator) {
        this.props = props;
        this.translator = translator;
    }

    
    public Mono<String> translateText(String text, String sourceLang, String targetLang) {
        int max = props.getChunk().getMaxChars();
        List<String> chunks = TextChunker.chunk(text, max);
        if (chunks.isEmpty()) {
            return Mono.just("");
        }
        int parallelism = Math.max(1, props.getParallelism());

        return Flux.range(0, chunks.size())
                .map(i -> Tuples.of(i, chunks.get(i)))
                .flatMapSequential(t -> translator.translateChunk(t.getT2(), sourceLang, targetLang)
                        .map(res -> Tuples.of(t.getT1(), res)), parallelism)
                .sort(Comparator.comparingInt(t -> t.getT1()))
                .map(t -> t.getT2())
                .collect(Collectors.joining("\n\n"));
    }
}
