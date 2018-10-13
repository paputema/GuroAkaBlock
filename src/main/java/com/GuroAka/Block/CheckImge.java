package com.GuroAka.Block;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PreDestroy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.GuroAka.Block.data.DataSearchResult;
import com.GuroAka.Block.data.DateSearchTrack;
import com.GuroAka.Block.data.URLSearchTrackDate;
import com.GuroAka.Block.repositories.GuroAkaSearchResultDataRepository;
import com.GuroAka.Block.repositories.GuroAkaSearchTrackDataRepository;
import com.GuroAka.Block.repositories.URLSearchTrackDateRepository;

import twitter4j.ExtendedMediaEntity;
import twitter4j.Status;
import twitter4j.URLEntity;

@Service
public class CheckImge {
	private static final Log LOG = LogFactory.getLog(CheckImge.class);

	@Autowired
	GuroAkaSearchTrackDataRepository trackDataRepository;
	@Autowired
	GuroAkaSearchResultDataRepository resultDataRepository;
	@Autowired
	private URLSearchTrackDateRepository urlSearchTrackDateRepository;
	@PreDestroy
	private void destroy() {
		if (exec != null && !exec.isShutdown()) {
			exec.shutdownNow();
		}
	}

	private ExecutorService exec = Executors.newSingleThreadExecutor();

	public void checkImge (Status status)
	{
		if(resultDataRepository.exists(status.getUser().getId()) == false)
		{
			exec.execute(new CheckImgeThread(status));
		}
	}
	private class CheckImgeThread extends Thread {
		final private Status status;
		private CheckImgeThread(Status status) {
			super();
			this.status = status;
		}
		public void run() {
			// TODO 自動生成されたメソッド・スタブ

			ExtendedMediaEntity[] mediaEntities = status.getExtendedMediaEntities();
			for (ExtendedMediaEntity mediaEntity : mediaEntities) {
				DateSearchTrack dateSearchTrack = trackDataRepository.findByGuroImageURL(mediaEntity.getMediaURLHttps());
				if (dateSearchTrack != null && dateSearchTrack.getGuroakauserid().equals(status.getUser().getId()) == false) {
					LOG.info(status.getUser().getName() +"(@" + status.getUser().getScreenName() + ")" + status.getText());
					resultDataRepository.save(new DataSearchResult(mediaEntity, status,dateSearchTrack.getOriginalstatus()));
					if(dateSearchTrack.getOriginalstatus() == null )
					{
						dateSearchTrack.setGuroakauserid(status.getUser().getId());
						dateSearchTrack.setOriginalstatus(status.getId());
						trackDataRepository.saveAndFlush(dateSearchTrack);
					}
				}
			}


			for (URLEntity urlEntity : status.getURLEntities())
			{
				URLSearchTrackDate urlSearchTrackDate = urlSearchTrackDateRepository.findByurl(urlEntity.getExpandedURL());
				if(urlSearchTrackDate != null && urlSearchTrackDate.getGuroakauserid().equals(status.getUser().getId()) == false)
				{
					LOG.info(status.getUser().getName() +"(@" + status.getUser().getScreenName() + ")" + status.getText());
					resultDataRepository.save(new DataSearchResult(urlSearchTrackDate, status,urlSearchTrackDate.getOriginalstatus()));
				}
			}
			resultDataRepository.flush();
		}
	}

}
