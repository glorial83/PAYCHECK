package kr.glorial.paycheck.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import kr.glorial.paycheck.entity.Paycheck;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = false)
public class PayCheckDTO {

	private Long paycheckId;
	private String paycheckMonth;
	private final String companyName = "테이크";
	private String userNo;
	private String userName;
	private String userEmail;
	private String birthDay;
	private String enterYmd;
	private String positionName;
	private String retireYmd;

	//지급액
	private BigDecimal basicAmount;
	private BigDecimal extendAmount;
	private BigDecimal holidayAmount;
	private BigDecimal bonusAmount;
	private BigDecimal specialAmount;	//체제비
	private BigDecimal vehicleAmount;
	private BigDecimal educationAmount;
	private BigDecimal mealAmount;
	private BigDecimal payTotalAmount;
	private BigDecimal childAmount;

	//공제
	private BigDecimal pensionAmount;
	private BigDecimal healthAmount;
	private BigDecimal careAmount;
	private BigDecimal hireAmount;
	private BigDecimal incomeTaxAmount;
	private BigDecimal localTaxAmount;
	private BigDecimal supportPensionAmount;
	private BigDecimal supportHireAmount;
	private BigDecimal deductionTotalAmount;

	//연말정산
	private BigDecimal yearEndIncomeTaxAmount;
	private BigDecimal yearEndLocalTaxAmount;

	//실수령액
	private BigDecimal realAmount;

	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSS")
	private LocalDateTime sendDate;

	private String sendYn = "N";

	public static PayCheckDTO toDTO(Paycheck paycheck) {
		return PayCheckDTO.builder()
			.paycheckId(paycheck.getPaycheckId())
			.paycheckMonth(paycheck.getPaycheckMonth())
			.userName(paycheck.getUserName())
			.userNo(paycheck.getUserNo())
			.userEmail(paycheck.getUserEmail())
			.birthDay(paycheck.getBirthDay())
			.enterYmd(paycheck.getEnterYmd())
			.retireYmd(paycheck.getRetireYmd())
			.positionName(paycheck.getPositionName())
			.basicAmount(paycheck.getBasicAmount())
			.extendAmount(paycheck.getExtendAmount())
			.holidayAmount(paycheck.getHolidayAmount())
			.bonusAmount(paycheck.getBonusAmount())
			.specialAmount(paycheck.getSpecialAmount())
			.vehicleAmount(paycheck.getVehicleAmount())
			.educationAmount(paycheck.getEducationAmount())
			.mealAmount(paycheck.getMealAmount())
			.childAmount(paycheck.getChildAmount())
			.payTotalAmount(paycheck.getPayTotalAmount())
			.pensionAmount(paycheck.getPensionAmount())
			.healthAmount(paycheck.getHealthAmount())
			.careAmount(paycheck.getCareAmount())
			.hireAmount(paycheck.getHireAmount())
			.incomeTaxAmount(paycheck.getIncomeTaxAmount())
			.localTaxAmount(paycheck.getLocalTaxAmount())
			.supportPensionAmount(paycheck.getSupportPensionAmount())
			.supportHireAmount(paycheck.getSupportHireAmount())
			.yearEndIncomeTaxAmount(paycheck.getYearEndIncomeTaxAmount())
			.yearEndLocalTaxAmount(paycheck.getYearEndLocalTaxAmount())
			.deductionTotalAmount(paycheck.getDeductionTotalAmount())
			.realAmount(paycheck.getRealAmount())
			.sendDate(paycheck.getSendDate())
			.build();
	}

}
