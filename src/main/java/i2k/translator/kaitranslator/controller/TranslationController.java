package i2k.translator.kaitranslator.controller;

import i2k.translator.kaitranslator.config.AppProperties;
import i2k.translator.kaitranslator.service.FileTextExtractor;
import i2k.translator.kaitranslator.service.FileWriters;
import i2k.translator.kaitranslator.service.TranslationService;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.*;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.InputStream;

@Controller
@RestController
@RequestMapping("/api")
public class TranslationController {

    private final TranslationService translationService;
    private final AppProperties props;

    public TranslationController(TranslationService translationService, AppProperties props) {
        this.translationService = translationService;
        this.props = props;
    }

    @GetMapping("/health")
    public Mono<ResponseEntity<String>> health() {
        return Mono.just(ResponseEntity.ok("OK"));
    }

    @PostMapping(value = "/translate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<ResponseEntity<byte[]>> translate(
            @RequestParam(name = "targetLang", required = false) String targetLang,
            @RequestParam(name = "sourceLang", required = false) String sourceLang,
            @RequestParam(name = "out", defaultValue = "txt") String out,
            @RequestPart("file") FilePart file
    ) {
        String tgt = (targetLang == null || targetLang.isBlank()) ? props.getTargetLang() : targetLang;
        final String finalFilename = file.filename();

        // Gộp toàn bộ buffer vào 1 DataBuffer
        return DataBufferUtils.join(file.content())
                .flatMap(dataBuffer ->
                        Mono.fromCallable(() -> {
                            try (InputStream in = dataBuffer.asInputStream()) {
                                // Đọc nội dung file bằng extractor cũ
                                String text = FileTextExtractor.extract(finalFilename, in);
                                System.out.println("PDF content: " + text);
                                return text;
                            } finally {
                                DataBufferUtils.release(dataBuffer);
                            }
                        }).subscribeOn(Schedulers.boundedElastic()) // tránh block event-loop
                )
                .flatMap(text -> translationService.translateText(text, sourceLang, tgt))
                .flatMap(translated -> {
                    try {
                        String base = stripExt(finalFilename) + ".translated." + out.toLowerCase();
                        byte[] bytes;
                        MediaType mediaType;

                        switch (out.toLowerCase()) {
                            case "docx" -> {
                                bytes = FileWriters.writeDocx(translated);
                                mediaType = MediaType.valueOf(
                                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                                );
                            }
                            case "pdf" -> {
                                bytes = FileWriters.writePdf(translated);
                                mediaType = MediaType.APPLICATION_PDF;
                            }
                            default -> {
                                bytes = FileWriters.writeTxt(translated);
                                mediaType = MediaType.TEXT_PLAIN;
                            }
                        }

                        HttpHeaders headers = new HttpHeaders();
                        headers.setContentType(mediaType);
                        headers.setContentDisposition(
                                ContentDisposition.attachment().filename(base).build()
                        );

                        return Mono.just(new ResponseEntity<>(bytes, headers, HttpStatus.OK));

                    } catch (Exception e) {
                        return Mono.error(e);
                    }
                });
    }


    private String stripExt(String name) {
        int i = name.lastIndexOf('.');
        return (i > 0) ? name.substring(0, i) : name;
    }
}
