package kr.glorial.paycheck.web.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@NoArgsConstructor
public class RestApiDTO<E> {

	private int errCode;
	private String errMessage;
	private E data;

}
