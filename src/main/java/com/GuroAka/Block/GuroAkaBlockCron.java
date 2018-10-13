package com.GuroAka.Block;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.GuroAka.Block.Twitter.GuroAkaTwitter;
import com.GuroAka.Block.Twitter.GuroAkaTwitter.TwitterBlocker;
import com.GuroAka.Block.Twitter.GuroAkaTwitter.Verifi;
import com.GuroAka.Block.data.DataUserAccount;
import com.GuroAka.Block.repositories.UserAccountDataRepository;
import com.carrotsearch.sizeof.RamUsageEstimator;

import lombok.AllArgsConstructor;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

@Service
@EnableScheduling
public class GuroAkaBlockCron {
	@Autowired
	private UserAccountDataRepository userAccountDataRepository;

	@Autowired
	private GuroAkaTwitter guroAkaTwitter;

	private static final Log LOG = LogFactory.getLog(GuroAkaBlockCron.class);

	private static final ExecutorService execBlock = Executors.newFixedThreadPool(15);
	@PreDestroy
	private void preDestroy()
	{
		execBlock.shutdown();
		try {
			execBlock.awaitTermination(5, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@AllArgsConstructor
	public class AutoBlockThread extends Thread
	{
		final DataUserAccount userData;
		@Override
		public void run() {
			Twitter twitter;
			ConfigurationBuilder builder;
			TwitterBlocker twitterBlocker;
			try {
				builder = new ConfigurationBuilder();
				builder.setOAuthAccessToken(userData.getAccessToken());
				builder.setOAuthAccessTokenSecret(userData.getAccessTokenSecret());
				twitter = new TwitterFactory(builder.build()).getInstance();
				twitter.showUser(twitter.verifyCredentials().getId());
				twitterBlocker = guroAkaTwitter.TwitterBlockerGetInstance(twitter,Verifi.NoChange);
				twitterBlocker.doBlock();
				userData.setLastblockdate(new Date());
			} catch (TwitterException e) {
				LOG.fatal(e.getMessage());
				userData.setVerify(false);
				userAccountDataRepository.save(userData);
			}

		}
	}

	@Scheduled(initialDelay = 0 * 60 * 1000, fixedDelay = 10 * 1000)
	public void AutoBlock() {
		LOG.info("自動Block開始");
		List<Future<?>> futures = new ArrayList<Future<?>>();
		for (final DataUserAccount userData : userAccountDataRepository.getUsers()) {
			futures.add(execBlock.submit(new  AutoBlockThread(userData)));
		}
		for (Future<?> future : futures) {
			try {
				future.get();
			} catch (InterruptedException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		userAccountDataRepository.flush();
		LOG.debug("サイズ ("+ this.getClass().getName() +")：" + RamUsageEstimator.sizeOf(this));
		LOG.info("自動Block完了");
		futures = null;
		//System.gc();
	}
}
