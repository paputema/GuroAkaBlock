package com.GuroAka.Block;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import twitter4j.RateLimitStatus;

public class RateLimitChecker {
	private static final Log LOG = LogFactory.getLog(RateLimitChecker.class);
	static boolean checkRateLimit(RateLimitStatus rateLimitStatus) {
		if(rateLimitStatus != null){
			Date dateNow = new Date();
			Date dateResetTime = new Date(((rateLimitStatus.getResetTimeInSeconds() + 60) * 1000L));
			if (rateLimitStatus != null && rateLimitStatus.getRemaining() <= 0 && dateNow.before(dateResetTime)) {
				LOG.info(rateLimitStatus.toString() + " NOW:" + dateNow + " RESR:" + dateResetTime);
				return false;
			}
		}
		return true;
	}
	static void SleepRateLimit(RateLimitStatus rateLimitStatus) {
		if (!checkRateLimit(rateLimitStatus)) {
			long time = new Date(((rateLimitStatus.getResetTimeInSeconds() + 60) * 1000L)).getTime() -  new Date().getTime();
			LOG.info("スリープ入ります" + time / 1000L + " 秒 " + rateLimitStatus.toString());
			try {
				if(time > 0)
				Thread.sleep(time);
			} catch (InterruptedException | IllegalArgumentException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}
		}
		return;
	}
}
