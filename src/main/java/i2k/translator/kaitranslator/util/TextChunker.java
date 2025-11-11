package i2k.translator.kaitranslator.util;

import java.util.ArrayList;
import java.util.List;

/**
 List<String> cacPhanNhoVanBan = new ArrayList<>();
 */
public class TextChunker {
    public static List<String> chunk(String text, int maxChars) {
        List<String> chunks = new ArrayList<>();
        if (text == null || text.isEmpty()) return chunks;

        // Soft split by paragraphs first
        String[] paragraphs = text.split("\n\s*\n");
        StringBuilder buf = new StringBuilder();
        for (String p : paragraphs) {
            String block = p.trim();
            if (block.isEmpty()) continue;
            if (block.length() > maxChars) {
                // hard split
                int i = 0;
                while (i < block.length()) {
                    int end = Math.min(i + maxChars, block.length());
                    chunks.add(block.substring(i, end));
                    i = end;
                }
            } else {
                if (buf.length() + block.length() + 2 > maxChars) {
                    chunks.add(buf.toString());
                    buf.setLength(0);
                }
                if (buf.length() > 0) buf.append("\n\n");
                buf.append(block);
            }
        }
        if (buf.length() > 0) chunks.add(buf.toString());
        return chunks;
    }
}
