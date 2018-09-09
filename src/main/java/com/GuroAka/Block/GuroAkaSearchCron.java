package com.GuroAka.Block;

import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.GuroAka.Block.Twitter.GuroAkaTwitter;
import com.GuroAka.Block.data.DateSearchTrack;
import com.GuroAka.Block.data.URLSearchTrackDate;
import com.GuroAka.Block.repositories.GuroAkaSearchTrackDataRepository;
import com.GuroAka.Block.repositories.URLSearchTrackDateRepository;

import twitter4j.MediaEntity;
import twitter4j.Status;
import twitter4j.URLEntity;
import twitter4j.User;



@Service
@EnableScheduling
public class GuroAkaSearchCron {
	@Autowired
	GuroAkaTwitter guroAkaTwitter;
	@Autowired
	private GuroAkaSearchTrackDataRepository guroAkaSearchTrackDataRepository;
	@Autowired
	private URLSearchTrackDateRepository urlSearchTrackDateRepository;


	private static final Log LOG = LogFactory.getLog(GuroAkaSearchCron.class);


	@Scheduled(initialDelay = 0 * 60 * 1000, fixedDelay = 1 * 60 * 60 * 1000)
	public void updateGuroAkaSearchTrack() {
		LOG.info("グロ垢リスト更新開始");
		Collection<User> users  = guroAkaTwitter.getGuroAkaUsers().values();
		for (User user : users) {
			for (Status status : guroAkaTwitter.getStatus(user)) {

				for (MediaEntity mediaEntity : status.getMediaEntities()) {
					DateSearchTrack dateSearchTrack = guroAkaSearchTrackDataRepository.findByGuroImageURL(mediaEntity.getMediaURLHttps());
					if (dateSearchTrack == null) {
						guroAkaSearchTrackDataRepository.save(new DateSearchTrack(status, mediaEntity.getMediaURLHttps()));
					}else if(dateSearchTrack.getOriginalstatus() == null)
					{
						dateSearchTrack.setGuroakauserid(status.getUser().getId());
						dateSearchTrack.setOriginalstatus(status.getId());
						guroAkaSearchTrackDataRepository.save(dateSearchTrack);
					}
					LOG.debug(user.getName() + " : " + mediaEntity.getMediaURLHttps());
				}
				for (URLEntity urlEntity : status.getURLEntities())
				{
					urlSearchTrackDateRepository.save(new URLSearchTrackDate(status,urlEntity.getURL()));
					urlSearchTrackDateRepository.save(new URLSearchTrackDate(status,urlEntity.getExpandedURL()));
					urlSearchTrackDateRepository.save(new URLSearchTrackDate(status,urlEntity.getDisplayURL()));
				}
			}

		}
		urlSearchTrackDateRepository.flush();
		guroAkaSearchTrackDataRepository.flush();
		System.gc();
		LOG.info("グロ垢リスト更新終了");
	}
	@Scheduled(initialDelay = 7200000, fixedDelay = 7200000)
	public void GuroAkaCsv2Db() {
		guroAkaTwitter.GuroAkaCsv2Db();
	}


}
