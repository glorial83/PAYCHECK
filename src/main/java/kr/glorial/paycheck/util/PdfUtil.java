package kr.glorial.paycheck.util;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.html.HtmlParser;
import com.lowagie.text.html.StyleSheet;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PdfUtil {

    /**
     * HTML을 PDF로 변환
     *
     * @param contents
     * @param cssContents
     * @param fontPath
     * @param os
     * @throws IOException
     */
    public static void makePDF(String contents, String cssContents, String fontPath, OutputStream os, String password) throws IOException {
        Document document = new Document(PageSize.A4, 36, 36, 36, 10);
        try {
            FontFactory.register(fontPath);

            StyleSheet style = new StyleSheet(cssContents);
            style.addStyle("body", "face", "NanumGothic");
            style.addStyle("body", "encoding", BaseFont.IDENTITY_H);

            PdfWriter writer = PdfWriter.getInstance(document, os);
            if (StringUtils.hasText(password)) {
                writer.setEncryption(password.getBytes(),
                        password.getBytes(),
                        PdfWriter.ALLOW_PRINTING,
                        PdfWriter.ENCRYPTION_AES_128);
            }

            document.open();

            HtmlParser parser = new HtmlParser(document);
            parser.setEncoding(BaseFont.IDENTITY_H);
            parser.setStyleSheet(style);
            parser.parse(new StringReader(contents));
        } catch (DocumentException de) {
            throw new IOException(de.getMessage());
        } finally {
            document.close();
        }
    }

    public static String makeMessage(String message, Map messageParams) {
        StringBuilder formatter = new StringBuilder(message);
        List<Object> valueList = new ArrayList<>();

        Matcher matcher = Pattern.compile("\\$\\{(\\w+)}").matcher(message);

        while (matcher.find()) {
            String key = matcher.group(1);

            String formatKey = String.format("${%s}", key);
            int index = formatter.indexOf(formatKey);

            if (index != -1) {
                formatter.replace(index, index + formatKey.length(), "%s");
                valueList.add(messageParams.get(key));
            }
        }

        return String.format(formatter.toString(), valueList.toArray());
    }
}
