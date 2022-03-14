package kr.glorial.paycheck.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.html.HtmlParser;
import com.lowagie.text.html.StyleSheet;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfWriter;

public class PdfUtil {

	static Pattern PATTERN_INPUT = Pattern.compile("\\<input[^\\>]+name\\='([a-zA-Z0-9_]+)[^\\>]*\\>", Pattern.CASE_INSENSITIVE);
	static Pattern PATTERN_ROW = Pattern.compile("\\<\\!\\-\\-\\srowstart\\s--\\>(.*?)\\<\\!\\-\\- rowend \\-\\-\\>", Pattern.DOTALL);

	/**
	 * input 태그를 값으로 치환
	 *
	 * @param template
	 * @param targetString
	 * @param destString
	 * @return
	 */
	public static String setTagColumn(String template, String targetString, String destString) {
		StringBuffer sb = new StringBuffer();

		Matcher matcher = PATTERN_INPUT.matcher(template);
	    int endIndex = 0;
	    while(matcher.find()) {
	    	sb.append(template.substring(endIndex, matcher.start(0)));

	    	if(matcher.group(1).equals(targetString)){
	    		sb.append(destString);
	    	}else{
        		sb.append(matcher.group(0));
        	}

	    	//찾은문자열의 제일 마지막 index
	    	endIndex = matcher.end(0);
	    }

	    if( endIndex == 0 ){
        	sb.append(template);
        }else{
        	sb = sb.append(template.substring(endIndex));
        }

	    return sb.toString();
	}

	/**
	 * map의 key와 일치하는 input태그를 map의 value로 치환
	 *
	 * @param template
	 * @param columnInfo
	 * @return
	 */
	public static String setTagColumns(String template, Map<String, ?> columnInfo) {
		for (String key : columnInfo.keySet()) {
			template = setTagColumn(template, key, (String)columnInfo.get(key));
		}

		return template;
	}

	/**
	 * template의 반복되는 행 부분을 list로 치환
	 *
	 * @param template
	 * @param list
	 * @return
	 */
	@SuppressWarnings({"unchecked", "rawtypes" })
	public static String setTagRows(String template, List<? extends Map> list) {
		int endIndex = 0;
		StringBuffer sb = new StringBuffer();
		Matcher matcher = PATTERN_ROW.matcher(template);
		while(matcher.find()) {
			sb.append(template.substring(endIndex, matcher.start(0)));
			String partTemplate = matcher.group(1);

			//rowtemplate 시작입니다.
			for(Map item : list) {
				sb.append(PdfUtil.setTagColumns(partTemplate, item));
			}

			endIndex = matcher.end(0);
		}

		if( endIndex == 0 ){
        	sb.append(template);
        }else{
        	sb = sb.append(template.substring(endIndex));
        }

		return sb.toString();
	}

	/**
	 * HTML을 PDF로 변환
	 *
	 * @param contents
	 * @param cssPath
	 * @param fontPath
	 * @param pdfFileName
	 * @throws Exception
	 */
	public static void makePDF(String contents, String cssPath, String fontPath, String pdfFileName) throws Exception {
		makePDF(contents, cssPath, fontPath, new FileOutputStream(pdfFileName));
	}

	/**
	 * HTML을 PDF로 변환
	 *
	 * @param contents
	 * @param cssPath
	 * @param fontPath
	 * @param pdfFileName
	 * @throws DocumentException
	 * @throws IOException
	 * @throws Exception
	 */
	public static void makePDF(String contents, String cssPath, String fontPath, OutputStream os) throws IOException{
		Document document = new Document(PageSize.A4, 36, 36, 36, 10);
		try {
			FontFactory.register(fontPath);
			String sTemplateCss = PdfUtil.readFile(cssPath);
			StyleSheet style = new StyleSheet(sTemplateCss);
			style.addStyle("body", "face", "NanumGothic");
			style.addStyle("body", "encoding", BaseFont.IDENTITY_H);

			PdfWriter.getInstance(document, os);
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
		} catch (IOException e) {
		}

		return null;
	}
}
