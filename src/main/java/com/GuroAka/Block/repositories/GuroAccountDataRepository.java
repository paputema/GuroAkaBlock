package com.GuroAka.Block.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.GuroAka.Block.data.DataGuroAccount;


@Repository
public interface GuroAccountDataRepository  extends JpaRepository<DataGuroAccount,String>{
	DataGuroAccount findByScreenname(String screenName);
	DataGuroAccount findByUserid (Long id);
	void removeByUserid(Long id);
}
