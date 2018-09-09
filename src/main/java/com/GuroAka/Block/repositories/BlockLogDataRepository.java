package com.GuroAka.Block.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.GuroAka.Block.data.DataBlockLog;


@Repository
public interface BlockLogDataRepository  extends JpaRepository<DataBlockLog,Long>{
}
