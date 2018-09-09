package com.GuroAka.Block.repositories;

import java.util.Collection;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.GuroAka.Block.data.DataBlockedHistory;
import com.GuroAka.Block.data.DataBlockedHistoryKeyId;


@Repository
public interface RepositoryDataBlockedHistory  extends JpaRepository<DataBlockedHistory,DataBlockedHistoryKeyId>{

	Collection<DataBlockedHistory> findByUserid(Long userid);
}
