package i2k.translator.kaitranslator.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class FileWriters {

    public static byte[] writeTxt(String content) {
        return content.getBytes(StandardCharsets.UTF_8);
    }

    public static byte[] writeDocx(String content) throws IOException {
        try (XWPFDocument doc = new XWPFDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            for (String line : content.split("\n\n")) {
                XWPFParagraph p = doc.createParagraph();
                p.createRun().setText(line);
            }
            doc.write(out);
            return out.toByteArray();
        }
    }

    // Very simple PDF text writer (no advanced layout). Splits into lines per page height.
    public static byte[] writePdf(String content) throws IOException {
        try (PDDocument pdf = new PDDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            float margin = 40;
            float leading = 14;
            float fontSize = 12;
            PDType1Font font = PDType1Font.HELVETICA;

            String[] lines = content.split("\n");
            int idx = 0;
            while (idx < lines.length) {
                PDPage page = new PDPage();
                pdf.addPage(page);

                try (PDPageContentStream cs = new PDPageContentStream(pdf, page)) {
                    cs.setFont(font, fontSize);
                    float y = page.getMediaBox().getHeight() - margin;

                    cs.beginText();
                    cs.newLineAtOffset(margin, y);
                    int linesPerPage = (int)((y - margin) / leading);
                    for (int i = 0; i < linesPerPage && idx < lines.length; i++, idx++) {
                        String line = lines[idx];
                        cs.showText(line);
                        cs.newLineAtOffset(0, -leading);
                    }
                    cs.endText();
                }
            }
            pdf.save(out);
            return out.toByteArray();
        }
    }
}
