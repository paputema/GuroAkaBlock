package com.GuroAka.Block.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.GuroAka.Block.data.DataVolunteerList;


@Repository
public interface VolunteerDataRepository  extends JpaRepository<DataVolunteerList,Long>{
}
