package i2k.translator.kaitranslator.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

public class FileTextExtractor {

    public static String extract(String filename, InputStream input) throws IOException {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".pdf")) {
            return extractPdf(input);
        } else if (lower.endsWith(".docx")) {
            return extractDocx(input);
        } else if (lower.endsWith(".txt")) {
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        } else {
            throw new IllegalArgumentException("Định dạng chưa hỗ trợ: " + filename);
        }
    }

    private static String extractPdf(InputStream in) throws IOException {
        try (PDDocument doc = PDDocument.load(in)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            return stripper.getText(doc);
        }
    }

    private static String extractDocx(InputStream in) throws IOException {
        try (XWPFDocument doc = new XWPFDocument(in)) {
            return doc.getParagraphs().stream()
                    .map(p -> p.getText())
                    .collect(Collectors.joining("\n"));
        }
    }
}
