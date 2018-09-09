package com.GuroAka.Block.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.GuroAka.Block.data.AlertTagSearchResultData;


@Repository
public interface AlertTagSearchResultDataRepository  extends JpaRepository<AlertTagSearchResultData,Long>{



	public AlertTagSearchResultData findByTargetStatusId(Long searchSinceId);
}
