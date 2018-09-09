package com.GuroAka.Block;

import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.GuroAka.Block.Twitter.GuroAkaTwitter;
import com.GuroAka.Block.data.DateSupporter;
import com.GuroAka.Block.data.DateSupporterList;
import com.GuroAka.Block.repositories.RepositoryDateSupporter;
import com.GuroAka.Block.repositories.SupportListDataRepository;
import com.carrotsearch.sizeof.RamUsageEstimator;

import twitter4j.Paging;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.RateLimitStatus;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;
@Service
@EnableScheduling
public class GuroAkaSearchBySup {

	private static final Log LOG = LogFactory.getLog(GuroAkaSearchBySup.class);
	private RateLimitStatus rateLimitStatus = null;
	private RateLimitStatus rateLimitUserTimeLine = null;


	@Autowired
	RepositoryDateSupporter supportDataRepository;
	@Autowired
	SupportListDataRepository supportListDataRepository;
	@Autowired
	GuroAkaTwitter guroAkaTwitter;
	@Autowired
	CheckImge  checkImge;

	@PostConstruct
	@Scheduled(initialDelay = 12 * 60 * 60 * 1000, fixedDelay = 12 * 60 * 60 * 1000)
	/**
	 * 支援垢更新
	 */
	private void upDateSupport() {
		for (DateSupporterList dateSupporterList : supportListDataRepository.findAll()) {
			for (User user : guroAkaTwitter.GetGuroAkaByList(dateSupporterList.getListid()).values()) {
				if (!supportDataRepository.exists(user.getId()) && user.getStatusesCount() > 0) {
					supportDataRepository.save(new DateSupporter(user));
				}
			}
		}
		supportDataRepository.flush();
	}
	/**
	 * 支援垢検索
	 */
	@Scheduled(initialDelay = 0 * 60 * 1000, fixedDelay = 15 * 60 * 1000)
	private void SearchBySupport() {

		LOG.info("開始：支援垢検索");
		Twitter twitter = guroAkaTwitter.getManagerTwitter();
		Map<Long, User> guroAkaUsers = guroAkaTwitter.getGuroAkaUsers();
		for (User guroAkaSearchResultData : guroAkaUsers.values()) {
			Query query = new Query("to:" + guroAkaSearchResultData.getScreenName());
			query.setCount(100);
			query.setResultType(Query.RECENT);
			QueryResult queryResult;
			//グロ垢へのリプライを検索
			if (checkRateLimit(rateLimitStatus)) {
				try {
					LOG.info(query.getQuery());
					do {
						SleepRateLimit(rateLimitStatus);
						queryResult = twitter.search(query);
						rateLimitStatus = queryResult.getRateLimitStatus();
						for (Status status : queryResult.getTweets()) {
							DateSupporter supportDate = supportDataRepository.findOne(status.getUser().getId());
							if (supportDate == null) {
								DateSupporter newSupportDate = new DateSupporter(status.getUser());
								supportDataRepository.saveAndFlush(newSupportDate);
							}

						}
						queryResult.getMaxId();
						LOG.debug("sinceId = " + queryResult.getSinceId() + " / maxId = " + queryResult.getMaxId());
						SleepRateLimit(rateLimitStatus);
						query = queryResult.nextQuery();
					} while (queryResult.hasNext());
				} catch (TwitterException e) {
					// TODO 自動生成された catch ブロック
					e.printStackTrace();
					rateLimitStatus = e.getRateLimitStatus();
				}
			}
		}

		Set<Long> GuroAka = new HashSet<Long>();
		SleepRateLimit(rateLimitUserTimeLine);
		for (DateSupporter searchSupportDate : supportDataRepository.findTop10ByOrderByLastsearchdate()) {
			LOG.debug("getUserTimeline:" + searchSupportDate.getUserName());
			try {
				Paging paging = new Paging(1, 200);
				ResponseList<Status> esponseStatus;
				if (searchSupportDate.getSinceId() > 0) {
					paging.setSinceId(searchSupportDate.getSinceId());
				}
				do {
					SleepRateLimit(rateLimitUserTimeLine);
					esponseStatus = twitter.getUserTimeline(searchSupportDate.getUserId(), paging);
					for (Status status : esponseStatus) {
						if(status.getInReplyToUserId() > 0)
						{
							checkImge.checkImge(status);
							GuroAka.add(status.getInReplyToUserId());
						}
						paging.setSinceId(Long.max(status.getId(), paging.getSinceId()));
					}
					rateLimitUserTimeLine = esponseStatus.getRateLimitStatus();
				} while (paging.getCount() == esponseStatus.size());
				searchSupportDate.setSinceId(paging.getSinceId());
				searchSupportDate.preUpdate();
				supportDataRepository.save(searchSupportDate);
			} catch (TwitterException e) {
				// TODO 自動生成された catch ブロック
				rateLimitUserTimeLine = e.getRateLimitStatus();
				SleepRateLimit(rateLimitUserTimeLine);
				if(checkRateLimit(rateLimitUserTimeLine))
				{
					supportDataRepository.delete(searchSupportDate);
				}
			}
		}
		SleepRateLimit(rateLimitUserTimeLine);
		for (Long guroAkaId : GuroAka) {
			try {
				Paging paging = new Paging(1, 200);
				ResponseList<Status> responseStatus;
				LOG.debug("getUserTimeline:" + guroAkaId);
				do {
					responseStatus = twitter.getUserTimeline(guroAkaId, paging);
					rateLimitUserTimeLine = responseStatus.getRateLimitStatus();
					SleepRateLimit(rateLimitUserTimeLine);
					for (Status status : responseStatus) {
							checkImge.checkImge(status);
						paging.setSinceId(status.getId());
					}
					SleepRateLimit(rateLimitUserTimeLine);
				} while (paging.getCount() == responseStatus.size());
			} catch (TwitterException e) {
				// TODO 自動生成された catch ブロック
				LOG.error(guroAkaId + e.toString());
				rateLimitUserTimeLine = e.getRateLimitStatus();
				SleepRateLimit(rateLimitUserTimeLine);
			}
		}
		supportDataRepository.flush();
		LOG.info("サイズ ("+  this.getClass().getName() + ")：" + RamUsageEstimator.sizeOf(this));
		GuroAka = null;
		//System.gc();
		LOG.info("終了：支援垢検索");
	}



	boolean checkRateLimit(RateLimitStatus rateLimitStatus) {
		if (rateLimitStatus != null) {
			Date dateNow = new Date();
			Date dateResetTime = new Date(((rateLimitStatus.getResetTimeInSeconds() + 60) * 1000L));
			if (rateLimitStatus != null && rateLimitStatus.getRemaining() <= 0 && dateNow.before(dateResetTime)) {
				LOG.info(rateLimitStatus.toString() + " NOW:" + dateNow + " RESR:" + dateResetTime);
				return false;
			}
		}
		return true;
	}

	private void SleepRateLimit(RateLimitStatus rateLimitStatus) {
		if (!checkRateLimit(rateLimitStatus)) {
			long time = new Date(((rateLimitStatus.getResetTimeInSeconds() + 60) * 1000L)).getTime()
					- new Date().getTime();
			LOG.info("スリープ入ります" + time / 1000L + " 秒 " + rateLimitStatus.toString());
			try {
				if (time > 0)
					Thread.sleep(time);
			} catch (InterruptedException | IllegalArgumentException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}
		}
		return;
	}
}
