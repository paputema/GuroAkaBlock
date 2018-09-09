package com.GuroAka.Block.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.GuroAka.Block.data.SauceData;


@Repository
public interface SauceDataRepository  extends JpaRepository<SauceData,String>{
}
