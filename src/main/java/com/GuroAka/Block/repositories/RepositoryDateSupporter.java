package com.GuroAka.Block.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.GuroAka.Block.data.DateSupporter;


@Repository
public interface RepositoryDateSupporter  extends JpaRepository<DateSupporter,Long>{

	List<DateSupporter> findTop10ByOrderByLastsearchdate();
}
