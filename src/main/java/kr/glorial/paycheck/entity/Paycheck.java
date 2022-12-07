package kr.glorial.paycheck.entity;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(callSuper = false)
public class Paycheck {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long paycheckId;
	private String paycheckMonth;
	private String companyName;
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
	private BigDecimal specialAmount;
	private BigDecimal vehicleAmount;
	private BigDecimal educationAmount;
	private BigDecimal mealAmount;
	private BigDecimal childAmount;
	private BigDecimal payTotalAmount;

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

	//발송일
	private LocalDateTime sendDate;

	@PrePersist
	@PreUpdate
	public void prePersist() {
		this.basicAmount          = this.basicAmount           == null ? new BigDecimal(0) : this.basicAmount;
		this.extendAmount         = this.extendAmount          == null ? new BigDecimal(0) : this.extendAmount;
		this.holidayAmount        = this.holidayAmount         == null ? new BigDecimal(0) : this.holidayAmount;
		this.bonusAmount          = this.bonusAmount           == null ? new BigDecimal(0) : this.bonusAmount;
		this.specialAmount        = this.specialAmount         == null ? new BigDecimal(0) : this.specialAmount;
		this.vehicleAmount        = this.vehicleAmount         == null ? new BigDecimal(0) : this.vehicleAmount;
		this.educationAmount      = this.educationAmount       == null ? new BigDecimal(0) : this.educationAmount;
		this.mealAmount           = this.mealAmount            == null ? new BigDecimal(0) : this.mealAmount;
		this.childAmount          = this.childAmount           == null ? new BigDecimal(0) : this.childAmount;
		this.payTotalAmount       = this.payTotalAmount        == null ? new BigDecimal(0) : this.payTotalAmount;
		this.pensionAmount        = this.pensionAmount         == null ? new BigDecimal(0) : this.pensionAmount;
		this.healthAmount         = this.healthAmount          == null ? new BigDecimal(0) : this.healthAmount;
		this.careAmount           = this.careAmount            == null ? new BigDecimal(0) : this.careAmount;
		this.hireAmount           = this.hireAmount            == null ? new BigDecimal(0) : this.hireAmount;
		this.incomeTaxAmount      = this.incomeTaxAmount       == null ? new BigDecimal(0) : this.incomeTaxAmount;
		this.localTaxAmount       = this.localTaxAmount        == null ? new BigDecimal(0) : this.localTaxAmount;
		this.supportPensionAmount = this.supportPensionAmount  == null ? new BigDecimal(0) : this.supportPensionAmount;
		this.supportHireAmount    = this.supportHireAmount     == null ? new BigDecimal(0) : this.supportHireAmount;
		this.deductionTotalAmount = this.deductionTotalAmount  == null ? new BigDecimal(0) : this.deductionTotalAmount;
		this.yearEndIncomeTaxAmount = this.yearEndIncomeTaxAmount  == null ? new BigDecimal(0) : this.yearEndIncomeTaxAmount;
		this.yearEndLocalTaxAmount = this.yearEndLocalTaxAmount  == null ? new BigDecimal(0) : this.yearEndLocalTaxAmount;
	}

	public Map toMap() {
		DecimalFormat df = (DecimalFormat) NumberFormat.getNumberInstance(Locale.getDefault());
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());

		Map<String, Object> info = mapper.convertValue(this, Map.class);

		for (String key : info.keySet()) {
			Object val = info.get(key);
			if (val instanceof BigDecimal) {
				info.put(key, df.format(val));
			}
		}

		String paycheckMonth = (String)info.get("paycheckMonth");
		info.put("paycheckMonth", paycheckMonth.substring(0, 4) + "-" + paycheckMonth.substring(4));

		return info;
	}
}
