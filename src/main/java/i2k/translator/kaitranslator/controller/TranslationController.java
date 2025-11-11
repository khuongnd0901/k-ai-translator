package i2k.translator.kaitranslator.controller;

import i2k.translator.kaitranslator.config.AppProperties;
import i2k.translator.kaitranslator.service.FileTextExtractor;
import i2k.translator.kaitranslator.service.FileWriters;
import i2k.translator.kaitranslator.service.TranslationService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

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
            @RequestPart("file") MultipartFile file
    ) {
        String tgt = (targetLang == null || targetLang.isBlank()) ? props.getTargetLang() : targetLang;
        String filename = file.getOriginalFilename();
        if (filename == null) filename = "input";

        final String finalFilename = filename;
        return Mono.defer(() -> {
            try (InputStream in = file.getInputStream()) {
                String text = FileTextExtractor.extract(finalFilename, in);
                return translationService.translateText(text, sourceLang, tgt)
                        .flatMap(translated -> {
                            try {
                                String base = stripExt(finalFilename) + ".translated." + out.toLowerCase();
                                byte[] bytes;
                                MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
                                switch (out.toLowerCase()) {
                                    case "docx" -> {
                                        bytes = FileWriters.writeDocx(translated);
                                        mediaType = MediaType.valueOf("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
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
                                headers.setContentDisposition(ContentDisposition.attachment().filename(base).build());
                                return Mono.just(new ResponseEntity<>(bytes, headers, HttpStatus.OK));
                            } catch (Exception e) {
                                return Mono.error(e);
                            }
                        });
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
