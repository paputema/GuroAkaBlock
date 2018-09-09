package com.GuroAka.Block.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.GuroAka.Block.data.DataUserAccount;


@Repository
public interface UserAccountDataRepository  extends JpaRepository<DataUserAccount,Long>{
	DataUserAccount findByUserid(Long id);
	List<DataUserAccount> findTop100ByVerifyIsTrueOrderByLastblockdateAsc();
	List<DataUserAccount> findTop100ByVerifyIsTrueAndLastblockdateIsNull();
	default List<DataUserAccount> getUsers()
	{
		List<DataUserAccount> ret = findTop100ByVerifyIsTrueAndLastblockdateIsNull();
		if(ret.isEmpty())
		{
			ret = findTop100ByVerifyIsTrueOrderByLastblockdateAsc();
		}


		return ret;

	}

}
