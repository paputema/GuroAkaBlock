package com.GuroAka.Block.repositories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.GuroAka.Block.data.DateSupporterList;


@Repository
public interface SupportListDataRepository  extends JpaRepository<DateSupporterList,Long>{
}
