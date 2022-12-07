package kr.glorial.paycheck.service;

import kr.glorial.paycheck.entity.Paycheck;
import kr.glorial.paycheck.repository.PaycheckRepository;
import kr.glorial.paycheck.util.PdfUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@RequiredArgsConstructor
@Service
public class PaycheckService {

    private final PaycheckRepository repository;

    private final JavaMailSender javaMailSender;

    private static final String NOREPLY_ADDRESS = "takeit@take-it.co.kr";

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
    public boolean sendEmail(Long paycheckId) throws IOException, MessagingException, URISyntaxException {
        try{
            Thread.sleep(1000);
        }catch(InterruptedException e){
            e.printStackTrace();
        }

		Optional<Paycheck> paycheckInfo = repository.findById(paycheckId);
		if (!paycheckInfo.isPresent()) {
			return false;
		}

		Paycheck paycheck = paycheckInfo.get();
        String paycheckMonth = paycheck.getPaycheckMonth();
        String fileName = paycheckMonth.substring(0, 4) + "년 " + paycheckMonth.substring(4) + "월 급여명세서";
        File dirPath = new File(System.getProperty("java.io.tmpdir"));
        File pdfFile = File.createTempFile("temp_", ".tmp", dirPath);

        try (
        	FileOutputStream fos = new FileOutputStream(pdfFile);
        ) {
            createPdf(paycheck, fos);
        } catch (IOException | URISyntaxException e) {
            throw e;
        }

        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

        if (ObjectUtils.isEmpty(paycheck.getUserEmail())) return false;

        helper.setFrom(NOREPLY_ADDRESS);
        helper.setTo(paycheck.getUserEmail());
        helper.setTo("glorial@take-it.co.kr");
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

    public void createPdf(Paycheck paycheck, OutputStream os) throws IOException, URISyntaxException {
        Map info = paycheck.toMap();
        info.put("issueDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일")));

		Stream<String> lines = Files.lines(Paths.get(PdfUtil.class.getClassLoader().getResource("static/pdf/paycheck.html").toURI()));
		String stylePath = getClass().getClassLoader().getResource("static/pdf/style.css").toURI().toString();
		String fontPath = getClass().getClassLoader().getResource("static/pdf/NanumGothic.ttf").toURI().toString();

        String fileContents = lines.collect(Collectors.joining("\n"));
        lines.close();

        String parsedFileContents = PdfUtil.makeMessage(fileContents, info);

        try (
        	ByteArrayOutputStream bos = new ByteArrayOutputStream()
        ) {
            PdfUtil.makePDF(parsedFileContents, stylePath, fontPath, bos, paycheck.getBirthDay());
            byte[] fileBinary = bos.toByteArray();

            FileCopyUtils.copy(fileBinary, os);
            os.flush();
        } catch (IOException e) {
        	throw e;
        }
    }
}
