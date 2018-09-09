package com.GuroAka.Block.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.GuroAka.Block.data.DataWhiteListAccount;


@Repository
public interface WhiteListAccountDataRepository  extends JpaRepository<DataWhiteListAccount,String>{
	WhiteListAccountDataRepository findByScreenname(String string);
	DataWhiteListAccount findByUserid(Long userId);
	List<DataWhiteListAccount>  findAllByUserid(Long userId);
	List<DataWhiteListAccount>  findAll();
	boolean existsByUserid(Long userId);
}
