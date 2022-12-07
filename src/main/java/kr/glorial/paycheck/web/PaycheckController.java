package kr.glorial.paycheck.web;

import kr.glorial.paycheck.entity.Paycheck;
import kr.glorial.paycheck.service.PaycheckService;
import kr.glorial.paycheck.util.PdfUtil;
import kr.glorial.paycheck.web.dto.PayCheckDTO;
import kr.glorial.paycheck.web.dto.RestApiDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.util.CollectionUtils;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/paycheck/v1")
@RestController
public class PaycheckController {

    private final PaycheckService service;

    private final JavaMailSender javaMailSender;

    private static final String NOREPLY_ADDRESS = "takeit@take-it.co.kr";

    @GetMapping("/email/{paycheckMonth}/{paycheckId}")
    public RestApiDTO sendEmail(@PathVariable String paycheckMonth, @PathVariable Long paycheckId) throws MessagingException, IOException {
        Optional<Paycheck> paycheck = service.paycheck(paycheckId);
        if (!paycheck.isPresent()) {
            RestApiDTO result = new RestApiDTO();
            result.setErrCode(-1);
            result.setErrMessage("ERROR");
            return result;
        }

        sendEmail(paycheck.get());

        RestApiDTO result = new RestApiDTO();
        result.setErrCode(0);
        result.setErrMessage("SUCC");
        return result;
    }

    @GetMapping("/email/{paycheckMonth}")
    public RestApiDTO sendEmail(@PathVariable String paycheckMonth) throws MessagingException, IOException {
        List<Paycheck> list = service.paychecks(paycheckMonth);
        if (CollectionUtils.isEmpty(list)) {
            RestApiDTO result = new RestApiDTO();
            result.setErrCode(-1);
            result.setErrMessage("ERROR");
            return result;
        }

        for (Paycheck paycheck : list) {
            sendEmail(paycheck);
        }

        RestApiDTO result = new RestApiDTO();
        result.setErrCode(0);
        result.setErrMessage("SUCC");
        return result;
    }

    private void sendEmail(Paycheck paycheck) throws IOException, MessagingException {
        String paycheckMonth = paycheck.getPaycheckMonth();
        String fileName = paycheckMonth.substring(0, 4) + "년 " + paycheckMonth.substring(4) + "월 급여명세서";
        File dirPath = new File(System.getProperty("java.io.tmpdir"));
        File pdfFile = File.createTempFile("temp_", ".tmp", dirPath);

        try (
            FileOutputStream fos = new FileOutputStream(pdfFile);
        ) {
            createPdf(paycheck, fos);
        } catch (IOException e) {
            e.printStackTrace();
        }

        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

        if (ObjectUtils.isEmpty(paycheck.getUserEmail())) return;

        helper.setFrom(NOREPLY_ADDRESS);
        helper.setTo(paycheck.getUserEmail());
        helper.setTo("glorial@take-it.co.kr");
        helper.setSubject(MimeUtility.encodeText(fileName, "UTF-8", "B"));
        helper.setText("안녕하세요\n\n" + fileName + "입니다.\n이번달도 수고 많으셨습니다.\n\n고맙습니다.");
        helper.addAttachment(MimeUtility.encodeText(fileName + ".pdf", "UTF-8", "B"), pdfFile);

        javaMailSender.send(mimeMessage);

        pdfFile.deleteOnExit();
    }

    @GetMapping("/paychecks/{paycheckMonth}/{paycheckId}")
    public void preview(@PathVariable String paycheckMonth, @PathVariable Long paycheckId, HttpServletResponse response) {
        Optional<Paycheck> paycheck = service.paycheck(paycheckId);
        if (!paycheck.isPresent()) {
            return;
        }

        try (
                ServletOutputStream os = response.getOutputStream();
        ) {
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "inline;");

            createPdf(paycheck.get(), os);
        } catch (IOException e) {
        }
    }

    @GetMapping("/paychecks/{paycheckMonth}")
    public RestApiDTO<List<PayCheckDTO>> paycheck(@PathVariable String paycheckMonth) {
        List<Paycheck> list = service.paychecks(paycheckMonth);

        //dto로 변환
        List<PayCheckDTO> dtoList = new ArrayList<>();
        for (Paycheck paycheck : list) {
            dtoList.add(PayCheckDTO.toDTO(paycheck));
        }

        RestApiDTO<List<PayCheckDTO>> result = new RestApiDTO<>();
        result.setErrCode(0);
        result.setErrMessage("SUCC");
        result.setData(dtoList);
        return result;
    }

    @PostMapping("/batch-save/{paycheckMonth}")
    public RestApiDTO save(@PathVariable String paycheckMonth, @RequestBody RestApiDTO<List<PayCheckDTO>> dtoList) {
        List<Paycheck> paycheckList = new ArrayList<>();
        for (PayCheckDTO dto : dtoList.getData()) {
            Paycheck paycheck = Paycheck.builder().paycheckId(dto.getPaycheckId())
                    .paycheckMonth(dto.getPaycheckMonth())
                    .companyName(dto.getCompanyName())
                    .userName(dto.getUserName())
                    .userNo(dto.getUserNo())
                    .userEmail(dto.getUserEmail())
                    .birthDay(dto.getBirthDay())
                    .enterYmd(dto.getEnterYmd())
                    .positionName(dto.getPositionName())
                    .basicAmount(dto.getBasicAmount())
                    .extendAmount(dto.getExtendAmount())
                    .holidayAmount(dto.getHolidayAmount())
                    .bonusAmount(dto.getBonusAmount())
                    .specialAmount(dto.getSpecialAmount())
                    .vehicleAmount(dto.getVehicleAmount())
                    .educationAmount(dto.getEducationAmount())
                    .mealAmount(dto.getMealAmount())
                    .childAmount(dto.getChildAmount())
                    .payTotalAmount(dto.getPayTotalAmount())
                    .pensionAmount(dto.getPensionAmount())
                    .healthAmount(dto.getHealthAmount())
                    .careAmount(dto.getCareAmount())
                    .hireAmount(dto.getHireAmount())
                    .incomeTaxAmount(dto.getIncomeTaxAmount())
                    .localTaxAmount(dto.getLocalTaxAmount())
                    .supportPensionAmount(dto.getSupportPensionAmount())
                    .supportHireAmount(dto.getSupportHireAmount())
                    .yearEndIncomeTaxAmount(dto.getYearEndIncomeTaxAmount())
                    .yearEndLocalTaxAmount(dto.getYearEndLocalTaxAmount())
                    .deductionTotalAmount(dto.getDeductionTotalAmount())
                    .realAmount(dto.getRealAmount()).build();

            paycheckList.add(paycheck);
        }

        service.deleteByPaycheckMonth(paycheckMonth);
        service.saveAll(paycheckList);

        RestApiDTO result = new RestApiDTO();
        result.setErrCode(0);
        result.setErrMessage("SUCC");
        return result;
    }

    private String makeMessage(String message, Map messageParams) {
        StringBuilder formatter = new StringBuilder(message);
        List<Object> valueList = new ArrayList<Object>();

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

    private void createPdf(Paycheck paycheck, OutputStream os) {
        Map info = paycheck.toMap();
        info.put("issueDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일")));

        Stream<String> lines = null;
        String stylePath = null;
        String fontPath = null;

        try {
            lines = Files.lines(Paths.get(PdfUtil.class.getClassLoader().getResource("static/pdf/paycheck.html").toURI()));
            stylePath = getClass().getClassLoader().getResource("static/pdf/style.css").toURI().toString();
            fontPath = getClass().getClassLoader().getResource("static/pdf/NanumGothic.ttf").toURI().toString();
        } catch (IOException | URISyntaxException e) {
        }

        String fileContents = lines.collect(Collectors.joining("\n"));
        lines.close();

        String parsedFileContents = makeMessage(fileContents, info);

        try (
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ) {
            PdfUtil.makePDF(parsedFileContents, stylePath, fontPath, bos, paycheck.getBirthDay());
            byte[] fileBinary = bos.toByteArray();

            FileCopyUtils.copy(fileBinary, os);
            os.flush();
        } catch (IOException ignored) {
        }
    }
}
