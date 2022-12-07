package kr.glorial.paycheck.web;

import kr.glorial.paycheck.entity.Paycheck;
import kr.glorial.paycheck.service.PaycheckService;
import kr.glorial.paycheck.web.dto.PayCheckDTO;
import kr.glorial.paycheck.web.dto.RestApiDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/paycheck/v1")
@RestController
public class PaycheckController {

    private final PaycheckService service;

    @GetMapping("/email/{paycheckMonth}/{paycheckId}")
    public RestApiDTO sendEmail(@PathVariable String paycheckMonth, @PathVariable Long paycheckId) throws MessagingException, IOException {
        boolean sendEmailResult = service.sendEmail(paycheckId);

        RestApiDTO result = new RestApiDTO();
        result.setErrCode(-1);
        result.setErrMessage("ERROR - " + paycheckId);

        if (sendEmailResult) {
            result.setErrCode(0);
            result.setErrMessage("SUCC");
        }

        return result;
    }

    /*@GetMapping("/email/{paycheckMonth}")
    public RestApiDTO sendEmail(@PathVariable String paycheckMonth) throws MessagingException, IOException {
        List<Paycheck> list = service.paychecks(paycheckMonth);
        if (CollectionUtils.isEmpty(list)) {
            RestApiDTO result = new RestApiDTO();
            result.setErrCode(-1);
            result.setErrMessage("ERROR");
            return result;
        }

        try {
            for (Paycheck paycheck : list) {
                service.sendEmail(paycheck);
            }
        } catch (URISyntaxException e) {
            log.error("EMAIL 전송 오류", e);

            RestApiDTO result = new RestApiDTO();
            result.setErrCode(-1);
            result.setErrMessage("ERROR");
            return result;
        }

        RestApiDTO result = new RestApiDTO();
        result.setErrCode(0);
        result.setErrMessage("SUCC");
        return result;
    }*/

    @GetMapping("/paychecks/{paycheckMonth}/{paycheckId}")
    public void preview(@PathVariable String paycheckMonth, @PathVariable Long paycheckId, HttpServletResponse response) {
        Optional<Paycheck> paycheck = service.paycheck(paycheckId);
        if (!paycheck.isPresent()) {
            return;
        }

        try (
            ServletOutputStream os = response.getOutputStream()
        ) {
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "inline;");

            service.createPdf(paycheck.get(), os);
        } catch (IOException e) {
            log.error("미리보기 오류", e);
        }
    }

    @GetMapping("/paychecks/{paycheckMonth}")
    public RestApiDTO<List<PayCheckDTO>> paycheck(@PathVariable String paycheckMonth) {
        List<Paycheck> list = service.paychecks(paycheckMonth);

        // dto로 변환
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
                    .realAmount(dto.getRealAmount())
                    .sendDate(dto.getSendDate()).build();

            paycheckList.add(paycheck);
        }

        service.deleteByPaycheckMonth(paycheckMonth);
        service.saveAll(paycheckList);

        RestApiDTO result = new RestApiDTO();
        result.setErrCode(0);
        result.setErrMessage("SUCC");
        return result;
    }
}
