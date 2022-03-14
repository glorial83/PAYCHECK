package kr.glorial.paycheck.web;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import kr.glorial.paycheck.entity.Paycheck;
import kr.glorial.paycheck.service.PaycheckService;
import kr.glorial.paycheck.util.PdfUtil;
import kr.glorial.paycheck.web.dto.PayCheckDTO;
import kr.glorial.paycheck.web.dto.RestApiDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/paycheck/v1")
@RestController
public class PaycheckController {

	private final PaycheckService service;

	@GetMapping("/paychecks/{paycheckMonth}/{paycheckId}")
	public void preview(@PathVariable String paycheckMonth, @PathVariable Long paycheckId, HttpServletResponse response) {
		log.debug("paycheckMonth : {}", paycheckMonth);
		log.debug("paycheckId : {}", paycheckId);

		try {
			Stream<String> lines = Files.lines(Paths.get(PdfUtil.class.getClassLoader().getResource("static/pdf/paycheck.html").toURI()));
			String fileContents = lines.collect(Collectors.joining("\n"));
			lines.close();

			String stylePath = getClass().getClassLoader().getResource("static/pdf/style.css").toURI().toString();
			String fontPath = getClass().getClassLoader().getResource("static/pdf/NanumGothic.ttf").toURI().toString();

			log.debug("fileContents : {}", fileContents);
			log.debug("stylePath : {}", stylePath);
			log.debug("fontPath : {}", fontPath);

			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			PdfUtil.makePDF(fileContents, stylePath, fontPath, bos);
			byte[] fileBinary = bos.toByteArray();

			response.setContentType("application/pdf");
			response.setHeader("Content-Disposition", "inline;");
			response.setContentLength(fileBinary.length);

			try(
				ServletOutputStream os = response.getOutputStream();
			){
				FileCopyUtils.copy(fileBinary, os);
				os.flush();
			} catch (IOException ioe) {
			}

		} catch (URISyntaxException | IOException e) {
			e.printStackTrace();
		}
	}

	@GetMapping("/paychecks/{paycheckMonth}")
	public RestApiDTO<List<PayCheckDTO>> paycheck(@PathVariable String paycheckMonth) {
		List<Paycheck> list = service.paychecks(paycheckMonth);

		//dto로 변환
		List<PayCheckDTO> dtoList = new ArrayList<PayCheckDTO>();
		for(Paycheck paycheck : list) {
			dtoList.add(PayCheckDTO.toDTO(paycheck));
		}

		RestApiDTO<List<PayCheckDTO>> result = new RestApiDTO<>();
		result.setErrCode(0);
		result.setErrMessage("SUCC");
		result.setData(dtoList);
		return result;
	}

	@PostMapping("/batch-save")
	public RestApiDTO save(@RequestBody RestApiDTO<List<PayCheckDTO>> dtoList) {
		List<Paycheck> paycheckList = new ArrayList<Paycheck>();
		for(PayCheckDTO dto : dtoList.getData()) {
			Paycheck paycheck = Paycheck.builder().paycheckId(dto.getPaycheckId())
			.paycheckMonth(dto.getPaycheckMonth())
			.companyName(dto.getCompanyName())
			.userName(dto.getUserName())
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
			.payTotalAmount(dto.getPayTotalAmount())
			.pensionAmount(dto.getPensionAmount())
			.healthAmount(dto.getHealthAmount())
			.careAmount(dto.getCareAmount())
			.hireAmount(dto.getHireAmount())
			.incomeTaxAmount(dto.getIncomeTaxAmount())
			.localTaxAmount(dto.getLocalTaxAmount())
			.supportPensionAmount(dto.getSupportPensionAmount())
			.supportHireAmount(dto.getSupportHireAmount())
			.deductionTotalAmount(dto.getDeductionTotalAmount())
			.realAmount(dto.getRealAmount()).build();

			paycheckList.add(paycheck);
		}

		log.debug("save : {}", paycheckList);

		service.saveAll(paycheckList);

		RestApiDTO result = new RestApiDTO();
		result.setErrCode(0);
		result.setErrMessage("SUCC");
		return result;
	}
}
