package com.GuroAka.Block.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.GuroAka.Block.data.DataSearchResult;


@Repository
public interface GuroAkaSearchResultDataRepository  extends JpaRepository<DataSearchResult,Long>{
	DataSearchResult findByScreenname(String string);
	List<DataSearchResult> findAllByUserid(Long userid);
}
