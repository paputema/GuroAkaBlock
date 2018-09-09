package com.GuroAka.Block;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.GuroAka.Block.Twitter.GuroAkaTwitter;
import com.GuroAka.Block.data.DataSuspectList;
import com.GuroAka.Block.repositories.GuroAkaSearchResultDataRepository;
import com.GuroAka.Block.repositories.GuroAkaSearchTrackDataRepository;
import com.GuroAka.Block.repositories.RepositoryDataSuspectList;
import com.carrotsearch.sizeof.RamUsageEstimator;

import twitter4j.Paging;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.RateLimitStatus;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.TwitterException;

@Service
@EnableScheduling
public class GuroAkaSearchListener {
	@Autowired
	GuroAkaSearchTrackDataRepository guroAkaSearchTrackDataRepository;
	@Autowired
	GuroAkaSearchResultDataRepository guroAkaSearchResultDataRepository;
	@Autowired
	RepositoryDataSuspectList repositoryDataSuspectList;
	private static final Log LOG = LogFactory.getLog(GuroAkaSearchListener.class);
	RateLimitStatus rateLimitStatusSearch = null;
	RateLimitStatus rateLimitStatusList = null;
	Long sinceIdSearch = -1L;
	@Autowired
	CheckImge  checkImge;
	@Autowired
	GuroAkaTwitter guroAkaTwitter;

	//@Scheduled(initialDelay = 15 *  60 * 1000, fixedDelay = 60 *  60 * 1000)
	public void CheckSuspectList() throws InterruptedException {
		if(RateLimitChecker.checkRateLimit(rateLimitStatusList)){
			for (DataSuspectList suspectList : repositoryDataSuspectList.findAll()) {
				LOG.debug("容疑者TL開始:" + suspectList.getId());
				Paging paging = new Paging();
				paging.setCount(100);
				if(suspectList.getId() > 0)
				{
					paging.setSinceId(suspectList.getId());
				}
				ResponseList<Status> statusList = null;
				do {
					try {
						statusList = guroAkaTwitter.getManagerTwitter().getUserListStatuses(suspectList.getId(), paging );
						rateLimitStatusList = statusList.getRateLimitStatus();
						for (Status status : statusList) {

								checkImge.checkImge(status);

							if(status.getId() > paging.getSinceId())
							{
								paging.setSinceId(status.getId());
							}
						}
					} catch (TwitterException e) {
						// TODO 自動生成された catch ブロック
						e.printStackTrace();
						rateLimitStatusList = e.getRateLimitStatus();
					}
					RateLimitChecker.SleepRateLimit(rateLimitStatusList);
				} while (statusList != null && statusList.size() > 0);
				suspectList.setSinceid(paging.getSinceId());
				repositoryDataSuspectList.saveAndFlush(suspectList);
				LOG.debug("容疑者TL終了:" + suspectList.getId());
				suspectList = null;
			}
		}
		//System.gc();
		LOG.debug("サイズ ("+ this.getClass().getName() +")：" + RamUsageEstimator.sizeOf(this));
	}
	@Scheduled(initialDelay = 0 *  60 * 1000, fixedDelay = 5 *  60 * 1000)
	public void SearchGuroAkaSearchTrackCron() throws InterruptedException {
		//Query query = new Query("min_retweets:1000 min_faves:1000 min_replies:10");
		Query query = new Query("#このリプ欄にはグロ画像があります exclude:retweets filter:replies");
		query.setCount(100);
		query.setResultType(Query.RECENT);
		QueryResult queryResult;
		query.setSinceId(sinceIdSearch);
		Set<Long> checked = new HashSet<>();
		if(RateLimitChecker.checkRateLimit(rateLimitStatusSearch)){
			try {
				LOG.info(query.getQuery());
				do {
					queryResult =  guroAkaTwitter.getManagerTwitter().search(query);
					for (Status status : queryResult.getTweets()) {
						//Search(status.getUser().getScreenName(), status.getId());
						if(!checked.contains(status.getInReplyToStatusId()) && (status.getInReplyToStatusId() > 0))
						{
							Search("to:" + status.getInReplyToScreenName(), status.getInReplyToStatusId(),status.getId());
							checked.add(status.getInReplyToStatusId());
						}

					}
					//sinceIdSearch = queryResult.getMaxId();
					LOG.debug("sinceId = " + queryResult.getSinceId() + " / maxId = " + queryResult.getMaxId());
					rateLimitStatusSearch = queryResult.getRateLimitStatus();
					RateLimitChecker.SleepRateLimit(rateLimitStatusSearch);
					query = queryResult.nextQuery();
				}while (queryResult.hasNext());
			} catch (TwitterException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
				rateLimitStatusSearch = e.getRateLimitStatus();
			}
		}
		LOG.debug("サイズ ("+ this.getClass().getName() +")：" + RamUsageEstimator.sizeOf(this));
		checked = null;
		//System.gc();
	}

	public <E> List<List<E>> subList(List<E> list, int count)
	{
		int from = 0;
		int to = count;
		List<List<E>> retList  = new ArrayList<>();
		while (from < list.size() && to < list.size()) {
			retList.add(list.subList(from, to));
			from = from + count;
			to = to + count;
		}
		retList.add(list.subList(from, list.size()));

		return retList;

	}
	@Scheduled(initialDelay = 2  * 60 * 60 * 1000, fixedDelay = 2  * 60 * 60 * 1000)
	public void SearchGuroAkaSearchTrackCron2() throws InterruptedException {
		Query query = new Query("min_retweets:2500 min_replies:25 exclude:retweets");
		//Query query = new Query("#このリプ欄にはグロ画像があります exclude:retweets");
		query.setLang("ja");
		query.setCount(100);
		query.setResultType(Query.RECENT);
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_MONTH, -1);
		query.setSince(dateFormat.format(calendar.getTime()));
		Set<Long> checked = new HashSet<>();

		QueryResult queryResult;
		if(RateLimitChecker.checkRateLimit(rateLimitStatusSearch)){
			try {
				LOG.debug(query.getQuery());
				do {
					queryResult =  guroAkaTwitter.getManagerTwitter().search(query);
					List<List<Status>> lists = subList(queryResult.getTweets(),25);
					for (List<Status> list : lists) {
						String string = "";
						Long sincid = -1L;
						for (Status status : list) {
							if(!checked.contains(status.getId()) && !status.getUser().isVerified() )
							{
								//Search(status.getUser().getScreenName(), status.getId(),-1L);
								if(string.length() > 0)
								{
									string = string + " OR ";
								}
								if(sincid == -1L || sincid > status.getId())
								{
									sincid = status.getId();
								}
								string = string + "to:" + status.getUser().getScreenName();
								checked.add(status.getId());
							}
						}
						if(string.length() > 0)
						{
							string = "(" + string + ") AND";
							Search(string, sincid,-1);
						}
					}
					LOG.debug("sinceId = " + queryResult.getSinceId() + " / maxId = " + queryResult.getMaxId());
					rateLimitStatusSearch = queryResult.getRateLimitStatus();
					RateLimitChecker.SleepRateLimit(rateLimitStatusSearch);
					query = queryResult.nextQuery();
					lists = null;
				}while (queryResult.hasNext());
			} catch (TwitterException e) {
				// TODO 自動生成された catch ブロック
				if(e.getErrorCode() == 88)
				{
					rateLimitStatusSearch = e.getRateLimitStatus();
				}else
				{
					e.printStackTrace();
				}

			}
		}
		LOG.debug("サイズ ("+ this.getClass().getName() +")：" + RamUsageEstimator.sizeOf(this));
		//System.gc();
	}

	public void Search(String screenName,long searchSinceId, long searchMaxId) throws TwitterException, InterruptedException {
		Query query = new Query(screenName + " filter:images OR filter:links -#このリプ欄にはグロ画像があります");
		query.setCount(100);
		query.setResultType(Query.RECENT);
		query.setSinceId(searchSinceId);
		query.setMaxId(searchMaxId);
		QueryResult queryResult;
		if(RateLimitChecker.checkRateLimit(rateLimitStatusSearch)){
			LOG.info(query.getQuery());
			do  {
				queryResult =  guroAkaTwitter.getManagerTwitter().search(query);
				for (Status status : queryResult.getTweets()) {
					LOG.debug(status.getUser().getName() + "(@" + status.getUser().getScreenName() + ")" + status.getId() + " " + status.getLang() + ":" + status.getRetweetCount() + ":" + status.getText() );
					if(status.getInReplyToStatusId() != -1 && status.getRetweetedStatus() == null)
					{
						checkImge.checkImge(status);
					}
				}
				LOG.debug("sinceId = " + queryResult.getSinceId() + "/ maxId = " + queryResult.getMaxId());
				rateLimitStatusSearch = queryResult.getRateLimitStatus();
				RateLimitChecker.SleepRateLimit(rateLimitStatusSearch);
				query = queryResult.nextQuery();
			}while (queryResult.hasNext());
		}
		LOG.debug("サイズ ("+ this.getClass().getName() +")：" + RamUsageEstimator.sizeOf(this));
	}



}

