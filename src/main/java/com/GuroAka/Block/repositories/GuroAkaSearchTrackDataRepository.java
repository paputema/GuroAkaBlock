package com.GuroAka.Block.repositories;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.GuroAka.Block.data.DateSearchTrack;



@Repository
public interface GuroAkaSearchTrackDataRepository  extends JpaRepository<DateSearchTrack,String>{
	DateSearchTrack findByGuroImageMD5(String MD5);
	default DateSearchTrack findByGuroImageURL(String URL)
	{
		return findByGuroImageMD5(DateSearchTrack.GetMD5(URL));
	}
	void deleteByGuroakauserid(Long guroakauserid);
	List<DateSearchTrack> findAllByGuroakauserid(Long guroakauserid);
}
