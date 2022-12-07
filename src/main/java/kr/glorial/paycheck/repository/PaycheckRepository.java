package kr.glorial.paycheck.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import kr.glorial.paycheck.entity.Paycheck;

@Repository
public interface PaycheckRepository extends JpaRepository<Paycheck, Long>{
	List<Paycheck> findByPaycheckMonth(String paycheckMonth);

	void deleteByPaycheckMonth(String paycheckMonth);
}
