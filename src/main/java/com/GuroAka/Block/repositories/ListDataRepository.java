package com.GuroAka.Block.repositories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.GuroAka.Block.data.DataGuroAccountList;


@Repository
public interface ListDataRepository  extends JpaRepository<DataGuroAccountList,Long>{
}
