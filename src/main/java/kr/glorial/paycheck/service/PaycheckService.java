package kr.glorial.paycheck.service;

import kr.glorial.paycheck.entity.Paycheck;
import kr.glorial.paycheck.repository.PaycheckRepository;
import kr.glorial.paycheck.util.PdfUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.ObjectUtils;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import javax.transaction.Transactional;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class PaycheckService {

    private final PaycheckRepository repository;

    private final JavaMailSender javaMailSender;

    private static final String NOREPLY_ADDRESS = "takeit@take-it.co.kr";

    @Value("classpath:/static/pdf/style.css")
    private Resource style;

    @Value("classpath:/static/pdf/NanumGothic.ttf")
    private Resource font;

    @Value("classpath:/static/pdf/paycheck.html")
    private Resource pdfTemplate;

    public List<Paycheck> paychecks() {
        return repository.findAll();
    }

    public List<Paycheck> paychecks(String paycheckMonth) {
        return repository.findByPaycheckMonth(paycheckMonth);
    }

    public Optional<Paycheck> paycheck(Long paycheckId) {
        return repository.findById(paycheckId);
    }

    @Transactional
    public void save(Paycheck paycheck) {
        repository.save(paycheck);
    }

    @Transactional
    public void saveAll(List<Paycheck> paycheck) {
        repository.saveAll(paycheck);
    }

    @Transactional
    public void deleteByPaycheckMonth(String paycheckMonth) {
        repository.deleteByPaycheckMonth(paycheckMonth);
    }

    @Transactional
    public boolean sendEmail(Long paycheckId) throws IOException, MessagingException {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Optional<Paycheck> paycheckInfo = repository.findById(paycheckId);
        if (!paycheckInfo.isPresent()) {
            return false;
        }

        Paycheck paycheck = paycheckInfo.get();
        String paycheckMonth = paycheck.getPaycheckMonth();
        String fileName = paycheckMonth.substring(0, 4) + "년 " + paycheckMonth.substring(4) + "월 급여명세서";
        File dirPath = new File(getTempPath());
        if (!dirPath.exists()) return false;

        File pdfFile = File.createTempFile("temp_", ".tmp", dirPath);
        try (FileOutputStream fos = new FileOutputStream(pdfFile)) {
            createPdf(paycheck, fos);
        }

        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

        if (ObjectUtils.isEmpty(paycheck.getUserEmail())) return false;

        helper.setFrom(NOREPLY_ADDRESS);
        helper.setTo(paycheck.getUserEmail());
        helper.setSubject(MimeUtility.encodeText(fileName, "UTF-8", "B"));
        helper.setText("안녕하세요\n\n" + fileName + "입니다.\n이번달도 수고 많으셨습니다.\n\n고맙습니다.");
        helper.addAttachment(MimeUtility.encodeText(fileName + ".pdf", "UTF-8", "B"), pdfFile);

        javaMailSender.send(mimeMessage);

        pdfFile.deleteOnExit();

        log.debug("============================================");
        log.debug("발송일시 저장 : {}", paycheck.getUserName());
		paycheck.setSendDate(LocalDateTime.now());
        log.debug("============================================");

        return true;
    }

    public void createPdf(Paycheck paycheck, OutputStream os) throws IOException {
        Map info = paycheck.toMap();
        info.put("issueDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일")));

        String pdfContents = readResourceFile(pdfTemplate);
        String styleContents = readResourceFile(style);
        String fontTempPath = getFontTempPath();

        try (
        	ByteArrayOutputStream bos = new ByteArrayOutputStream()
        ) {
            String parsedFileContents = PdfUtil.makeMessage(pdfContents, info);
            PdfUtil.makePDF(parsedFileContents, styleContents, fontTempPath, bos, paycheck.getBirthDay());
            byte[] fileBinary = bos.toByteArray();

            FileCopyUtils.copy(fileBinary, os);
            os.flush();
        }
    }

    private String getFontTempPath() throws IOException {
        File fontTempFile = new File(getTempPath() + "font.ttf");
        if (!fontTempFile.exists()) {
            Files.copy(font.getInputStream(), fontTempFile.toPath());
        }
        return fontTempFile.getAbsolutePath();
    }

    private String getTempPath() {
        String tmpDir = System.getProperty("java.io.tmpdir");
        if (!tmpDir.endsWith(File.separator)) {
            tmpDir += File.separator;
        }

        File temp = new File(tmpDir + "paycheck" + File.separator);
        if (!temp.exists()) {
            temp.mkdir();
        }

        return tmpDir + "paycheck" + File.separator;
    }

    private String readResourceFile(Resource resource) {
        try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
