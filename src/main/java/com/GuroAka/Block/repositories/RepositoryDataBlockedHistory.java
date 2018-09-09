package com.GuroAka.Block.repositories;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.GuroAka.Block.data.DataBlockedHistory;
import com.GuroAka.Block.data.DataBlockedHistoryKeyId;


@Repository
public interface RepositoryDataBlockedHistory  extends JpaRepository<DataBlockedHistory,DataBlockedHistoryKeyId>{

	Collection<DataBlockedHistory> findByUserid(Long userid);
	public default Map<Long,DataBlockedHistory> findByUseridMap(Long userid)
	{
		Map<Long,DataBlockedHistory> ret = new HashMap<Long, DataBlockedHistory>();

		for (DataBlockedHistory dataBlockedHistory : findByUserid( userid)) {
			ret.put(dataBlockedHistory.getGuroakaid(), dataBlockedHistory);
		}
		return ret;

	}

}
