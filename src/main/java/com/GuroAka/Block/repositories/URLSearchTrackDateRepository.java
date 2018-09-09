package com.GuroAka.Block.repositories;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.GuroAka.Block.data.URLSearchTrackDate;



@Repository
public interface URLSearchTrackDateRepository  extends JpaRepository<URLSearchTrackDate,String>{
	List<URLSearchTrackDate> findAllByGuroakauserid(Long guroakauserid);
	URLSearchTrackDate findByurl(String url);
}
