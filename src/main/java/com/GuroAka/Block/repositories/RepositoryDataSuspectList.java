package com.GuroAka.Block.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.GuroAka.Block.data.DataSuspectList;


@Repository
public interface RepositoryDataSuspectList  extends JpaRepository<DataSuspectList,Long>{


}
