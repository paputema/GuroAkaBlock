package com.GuroAka.Block;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.GuroAka.Block.Twitter.GuroAkaTwitter;
import com.GuroAka.Block.data.AlertTagSearchResultData;
import com.GuroAka.Block.repositories.AlertTagSearchResultDataRepository;
import com.carrotsearch.sizeof.RamUsageEstimator;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.RateLimitStatus;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.Configuration;
import twitter4j.conf.PropertyConfiguration;



@Service
@EnableScheduling
public class AlertTagSearchCron {
	@Autowired
	GuroAkaTwitter guroAkaTwitter;
	@Autowired
	private AlertTagSearchResultDataRepository alertTagSearchResultDataRepository;
	private RateLimitStatus rateLimitStatusSearch;


	private static final Log LOG = LogFactory.getLog(AlertTagSearchCron.class);


	@Scheduled(initialDelay = 0 * 60 * 1000, fixedDelay = 10 * 60 * 1000)
	public void alertTagSearch() {
		LOG.info("注意喚起タグ検索開始");
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Calendar daySince = Calendar.getInstance();
		Calendar dayUntil = Calendar.getInstance();
		dayUntil.add(Calendar.DAY_OF_MONTH, 1);



		//Query query = new Query("#このリプ欄にはグロ画像があります exclude:retweets filter:replies");
		Query query = new Query("#このリプ欄にはグロ画像があります exclude:retweets ");

		//query.setLang("ja");
		query.setCount(100);
		query.setResultType(Query.MIXED);
		query.setSince(dateFormat.format(daySince.getTime()));
		query.setUntil(dateFormat.format(dayUntil.getTime()));

		QueryResult queryResult;
		if(RateLimitChecker.checkRateLimit(rateLimitStatusSearch)){
			try {
				LOG.info(query.getQuery());
				do {
					queryResult =  guroAkaTwitter.getManagerTwitter().search(query);
					for (Status status : queryResult.getTweets()) {
						//Search(status.getUser().getScreenName(), status.getId());
						if(status.getInReplyToStatusId() > 0)
						{
							try {
								Search("to:" + status.getInReplyToScreenName(), status.getInReplyToStatusId(),status);
							} catch (TwitterException e) {
								// TODO 自動生成された catch ブロック
								e.printStackTrace();
								rateLimitStatusSearch = e.getRateLimitStatus();
							}
						}
						if(status.getQuotedStatusId() >0)
						{
							try {
								Search("to:" + status.getQuotedStatus().getUser().getScreenName(), status.getQuotedStatusId(),status);
							} catch (TwitterException e) {
								// TODO 自動生成された catch ブロック
								e.printStackTrace();
								rateLimitStatusSearch = e.getRateLimitStatus();
							}
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
		LOG.info("注意喚起タグ検索終了");
	}
	private void Search(String screenName,long searchSinceId, Status alertTagStatus) throws TwitterException  {
		AlertTagSearchResultData  alertTagSearchResultData  = alertTagSearchResultDataRepository.findByTargetStatusId(searchSinceId);
		if(alertTagSearchResultData == null)
		{
			alertTagSearchResultData = new AlertTagSearchResultData(searchSinceId,guroAkaTwitter.getStatus(searchSinceId));
		}
		alertTagSearchResultData.setAlertTagStatus(alertTagStatus);

		Query query = new Query(screenName + " (filter:images OR filter:links)  exclude:retweets -#このリプ欄にはグロ画像があります");
		query.setCount(100);
		query.setResultType(Query.RECENT);
		query.setSinceId(searchSinceId);
		query.setMaxId(alertTagStatus.getId());
		QueryResult queryResult = null;
		String lastQuery ="";
		if(RateLimitChecker.checkRateLimit(rateLimitStatusSearch)){
			lastQuery = query.getQuery();
			LOG.info(query.getQuery());
			do  {
				try {
					queryResult =  guroAkaTwitter.getManagerTwitter().search(query);
					for (Status status : queryResult.getTweets()) {
						if(status.getInReplyToStatusId() == searchSinceId)
						{
							alertTagSearchResultData.setGuroStatus(status);
						}
					}
					LOG.debug("sinceId = " + queryResult.getSinceId() + "/ maxId = " + queryResult.getMaxId());
					rateLimitStatusSearch = queryResult.getRateLimitStatus();
					RateLimitChecker.SleepRateLimit(rateLimitStatusSearch);
					query = queryResult.nextQuery();
				} catch (TwitterException e) {
					// TODO 自動生成された catch ブロック
					e.printStackTrace();
				}
			}while (queryResult != null && queryResult.hasNext());
		}
		if(alertTagSearchResultData.hasNewGuroStatus() == Boolean.TRUE)
		{
			LOG.info(alertTagSearchResultData.toString());
			alertTagSearchResultDataRepository.saveAndFlush(alertTagSearchResultData);

			try {
				String searchUrl = "https://twitter.com/search?f=tweets&q=" + URLEncoder.encode(lastQuery, "UTF-8");
				String status = "(新機能試験中)" + ls
						+ "被害者 https://twitter.com/-/status/"  + searchSinceId + ls
						+ "タグ https://twitter.com/-/status/" + alertTagStatus.getId() + ls
						+ "http://chupacabrasmon.ddns.net/GuroAkaBlock/Status/" + searchSinceId;
				StatusUpdate statusUpdate = new StatusUpdate(status);
				reportTweet(statusUpdate);
			} catch (UnsupportedEncodingException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}
		}
		LOG.debug("サイズ ("+ this.getClass().getName() +")：" + RamUsageEstimator.sizeOf(this));
	}
	private Status reportTweet(StatusUpdate statusUpdate)
	{
		Status ret = null;
		try {
			Resource resource = new ClassPathResource("/GuroAkaSearchResultData.properties");
			Configuration conf = new PropertyConfiguration(resource.getInputStream());
			Twitter twitter= new TwitterFactory(conf).getInstance();
			ret = twitter.updateStatus(statusUpdate);
		} catch (TwitterException | IOException e) {
			// TODO 自動生成された catch ブロック
			//e.printStackTrace();
		}
		return ret;
	}
	private static final String ls = System.lineSeparator();

}
