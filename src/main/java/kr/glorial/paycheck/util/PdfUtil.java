package kr.glorial.paycheck.util;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.html.HtmlParser;
import com.lowagie.text.html.StyleSheet;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfWriter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PdfUtil {

    /**
     * HTML을 PDF로 변환
     *
     * @param contents
     * @param cssPath
     * @param fontPath
     * @param pdfFileName
     * @param password
     * @throws Exception
     */
    public static void makePDF(String contents, String cssPath, String fontPath, String pdfFileName, String password) throws IOException {
        makePDF(contents, cssPath, fontPath, new FileOutputStream(pdfFileName), password);
    }

    /**
     * HTML을 PDF로 변환
     *
     * @param contents
     * @param cssPath
     * @param fontPath
     * @param os
     * @throws IOException
     */
    public static void makePDF(String contents, String cssPath, String fontPath, OutputStream os, String password) throws IOException {
        Document document = new Document(PageSize.A4, 36, 36, 36, 10);
        try {
            FontFactory.register(fontPath);
            String sTemplateCss = readFile(cssPath);
            StyleSheet style = new StyleSheet(sTemplateCss);
            style.addStyle("body", "face", "NanumGothic");
            style.addStyle("body", "encoding", BaseFont.IDENTITY_H);

            PdfWriter writer = PdfWriter.getInstance(document, os);

            if (password != null) {
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
            de.printStackTrace();
            throw new IOException(de.getMessage());
        } finally {
            document.close();
        }
    }

    public static String readFile(String filePath) {
        try {
            Stream<String> lines = Files.lines(Paths.get(URI.create(filePath)));
            String fileContents = lines.collect(Collectors.joining("\n"));
            lines.close();

            return fileContents;
        } catch (IOException ignored) {
        }

        return null;
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
