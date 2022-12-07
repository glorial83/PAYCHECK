package kr.glorial.paycheck.service;

import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import kr.glorial.paycheck.entity.Paycheck;
import kr.glorial.paycheck.repository.PaycheckRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class PaycheckService {

	private final PaycheckRepository repository;

	public List<Paycheck> paychecks(){
		return repository.findAll();
	}

	public List<Paycheck> paychecks(String paycheckMonth){
		return repository.findByPaycheckMonth(paycheckMonth);
	}

	public Optional<Paycheck> paycheck(Long paycheckId){
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

}
