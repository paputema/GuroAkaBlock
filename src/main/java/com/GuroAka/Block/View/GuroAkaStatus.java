package com.GuroAka.Block.View;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.GuroAka.Block.Blockresult;
import com.GuroAka.Block.Twitter.GuroAkaTwitter;
import com.GuroAka.Block.Twitter.GuroAkaTwitter.TwitterBlocker;
import com.GuroAka.Block.Twitter.GuroAkaTwitter.Verifi;
import com.GuroAka.Block.data.AlertTagSearchResultData;
import com.GuroAka.Block.data.DataBlockedHistory;
import com.GuroAka.Block.data.DataUserAccount;
import com.GuroAka.Block.repositories.AlertTagSearchResultDataRepository;
import com.GuroAka.Block.repositories.RepositoryDataBlockedHistory;
import com.GuroAka.Block.repositories.UserAccountDataRepository;

import lombok.Data;
import twitter4j.ExtendedMediaEntity;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.OAuthAuthorization;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;


@Controller
public class GuroAkaStatus {
	@Value("${api.domain}") private String domain;
	@Autowired
	private AlertTagSearchResultDataRepository alertTagSearchResultDataRepository;

	@Autowired
	private RepositoryDataBlockedHistory dataBlockedHistory;
	@Autowired
	private HttpSession session;
	@Autowired
	private HttpServletRequest request;

	@RequestMapping(value= {"/Status/{targetId}"} , method = RequestMethod.GET)
	public ModelAndView index(@PathVariable Long targetId,ModelAndView mav) {
		index(mav);
		TwitterBlocker twitterBlocker = (TwitterBlocker) session.getAttribute("twitterBlocker");
		Map<Long, Blockresult> mapBlockedHistory = new HashMap<>();
		AlertTagSearchResultData alertTagSearchResultData =  alertTagSearchResultDataRepository.findByTargetStatusId(targetId);
		session.setAttribute("statusId", targetId);
		if(alertTagSearchResultData != null)
		{
			if(twitterBlocker != null)
			{
				mapBlockedHistory = twitterBlocker.getMapBlockedHistory();
			}

			String ogTitle = null,ogDescription = null;
			ogTitle = "グロ画像リプライアカウントを一括でスパブロするやつ";
			ogDescription = "注意喚起タグを元にグロ画像の可能性あるリプライを一覧表示しています。";
			mav.addObject("TargetStatus", new StatusObject(alertTagSearchResultData.getTargetStatus(),mapBlockedHistory.get(alertTagSearchResultData.getTargetStatus().getUser().getId())));
			User user = alertTagSearchResultData.getTargetStatus().getUser();


			Collection<StatusObject> alertTagStatus = new ArrayList<StatusObject>();
			String alertTagUsersName = "";
			for (Status status : alertTagSearchResultData.getAlertTagStatus()) {
				alertTagStatus.add(new StatusObject(status,mapBlockedHistory.get(status.getUser().getId())));
				alertTagUsersName = status.getUser().getName() + "(@" + status.getUser().getScreenName() + ")";
			}
			mav.addObject("AlertTagStatus", alertTagStatus);

			Collection<StatusObject> guroStatus = new ArrayList<StatusObject>();
			for (Status status : alertTagSearchResultData.getGuroStatus()) {
				guroStatus.add(new StatusObject(status,mapBlockedHistory.get(status.getUser().getId())));
			}
			mav.addObject("GuroStatus", guroStatus);



			if(user != null)
			{
				ogTitle = "グロ画像リプライアカウントを一括でスパブロするやつ グロ画像リプの可能性のあるツイート表示";
				ogDescription = alertTagUsersName + "さんの注意喚起タグツイートを元に自動的に" + user.getName() + "(@" + user.getScreenName() + ")さん宛へのグロ画像の可能性あるリプライを一覧表示しています。";
			}

			mav.addObject("TargetStatusUrl", "http://chupacabrasmon.ddns.net/GuroAkaBlock/Status/" + targetId);
			mav.addObject("ogTitle", ogTitle);
			mav.addObject("ogDescription", ogDescription);
			mav.setViewName("Status");
		}
		return mav;
	}
	@RequestMapping(value= {"/Status"} , method = RequestMethod.GET)
	public ModelAndView index(ModelAndView mav) {
		String ogTitle = null,ogDescription = null;
		ogTitle = "グロ画像リプライアカウントを一括でスパブロするやつ";
		ogDescription = "注意喚起タグを元にグロ画像の可能性あるリプライを一覧表示しています。";

		Collection<StatusObject> targetStatus = new ArrayList<StatusObject>();
		for (AlertTagSearchResultData alertTagSearchResultData : alertTagSearchResultDataRepository.findAll()) {
			targetStatus.add(new StatusObject (alertTagSearchResultData.getTargetStatus()));
		}

		mav.addObject("TargetStatusList",targetStatus );
		mav.addObject("TargetStatusUrl", "http://chupacabrasmon.ddns.net/GuroAkaBlock/Status" );
		mav.addObject("ogTitle", ogTitle);
		mav.addObject("ogDescription", ogDescription);
		mav.setViewName("Status");
		return mav;
	}

	@RequestMapping(value= {"/Block/{targetId}"} , method = RequestMethod.GET)
	public ModelAndView block(@PathVariable Long targetId,ModelAndView mav) {
		session.setAttribute("blockId", targetId);
		TwitterBlocker twitterBlocker = (TwitterBlocker) session.getAttribute("twitterBlocker");
		if(twitterBlocker == null )
		{
			mav = getRequestToken(mav);
		}else
		{
			DataBlockedHistory blockedHistory = null;
			try {
				twitterBlocker.doBlock(targetId);
				mav.setViewName("redirect:/Status/" + session.getAttribute("statusId") + "#" + targetId);
			} catch (TwitterException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}
		}
		return mav;
	}
	public ModelAndView getRequestToken(ModelAndView mav)
	{
		try {
			session.setMaxInactiveInterval(3600);
			mav.addObject("force_login", true);
			OAuthAuthorization oauth = createOAuthAuthorization();


			String callbackURL = request.getRequestURL().toString().replaceAll(request.getServletPath(), "");
			callbackURL += "/BlockAccessToken";
			session.setAttribute("requestToken", oauth.getOAuthRequestToken(callbackURL)); //$NON-NLS-1$
			RequestToken requestToken = (RequestToken) session.getAttribute("requestToken");
			mav.setViewName("redirect:" + requestToken.getAuthenticationURL()); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (TwitterException e) {
			// TODO 自動生成された catch ブロック
			mav.setViewName("/"); //$NON-NLS-1$
		}
		return mav;
	}
	@Autowired
	private GuroAkaTwitter guroAkaTwitter;
	@Autowired
	private UserAccountDataRepository userAccountDataRepository;
	@RequestMapping(value= {"/BlockAccessToken"} , method = RequestMethod.GET)
	public ModelAndView getAccessToken(ModelAndView mav) {

		try {
			RequestToken requestToken = (RequestToken) session.getAttribute("requestToken"); //$NON-NLS-1$

			AccessToken accessToken = new AccessToken(requestToken.getToken(), requestToken.getTokenSecret());

			OAuthAuthorization oath = createOAuthAuthorization();
			oath.setOAuthAccessToken(accessToken);
			String verifier = request.getParameter("oauth_verifier"); //$NON-NLS-1$
			accessToken = oath.getOAuthAccessToken(verifier);
			Twitter twitter = new TwitterFactory(createConfiguration()).getInstance(accessToken);
			TwitterBlocker twitterBlocker = guroAkaTwitter.TwitterBlockerGetInstance(twitter, Verifi.NoChange);
			session.setAttribute("twitterBlocker", twitterBlocker);

			DataUserAccount dataUserAccount = userAccountDataRepository.findByUserid(twitter.getId());
			if(dataUserAccount == null)
			{
				dataUserAccount = new DataUserAccount();
				dataUserAccount.setUserid(twitter.getId());
				dataUserAccount.setVerify(false);
			}
			dataUserAccount.setAccessToken(accessToken.getToken());
			dataUserAccount.setAccessTokenSecret(accessToken.getTokenSecret());
			userAccountDataRepository.saveAndFlush(dataUserAccount);


			session.setMaxInactiveInterval(3600);
			User user = twitter.verifyCredentials();
			session.setAttribute("user", user.getName() + "(@" + user.getScreenName() + ")");

		} catch (TwitterException  |  NumberFormatException e) {
			// TODO 自動生成された catch ブロック
			session.setAttribute("ret", e.getMessage());
		}

		mav.setViewName("redirect:/Block/" + session.getAttribute("blockId"));
		return mav;
	}




	private OAuthAuthorization createOAuthAuthorization() {
		return new OAuthAuthorization(createConfiguration());
	}

	private Configuration createConfiguration() {
		ConfigurationBuilder builder = new ConfigurationBuilder();
		builder.setOAuthAccessToken(null);
		builder.setOAuthAccessTokenSecret(null);
		return (builder.build());
	}





	@Data
	private class StatusObject
	{
		public StatusObject(Status status, Blockresult blockresult)
		{
			if(blockresult != null)
			{
				this.blockedHistory = blockresult;
			}
			setStatus(status);
		}
		public StatusObject(Status status) {
			setStatus(status);
			this.blockedHistory = null;
		}
		private void setStatus(Status status)
		{
			userName = status.getUser().getName();
			iconUrl = status.getUser().getBiggerProfileImageURLHttps();
			userId = status.getUser().getId();
			statusText = status.getText();
			for(ExtendedMediaEntity mediaEntity : status.getExtendedMediaEntities())
			{
				mediaUrlList.add(mediaEntity.getMediaURLHttps());
			}
			userUrl = "https://twitter.com/intent/user?user_id=" + status.getUser().getId();
			statusUrl = "https://twitter.com/" + status.getUser().getScreenName() + "/status/" + status.getId();
			screenName = status.getUser().getScreenName();
			hasImage = !mediaUrlList.isEmpty();
			id = status.getId();
			this.via = status.getSource();
			isBlocked = (blockedHistory != null && blockedHistory == Blockresult.Blocked);

			blockedCount = dataBlockedHistory.findByGuroakaid(status.getUser().getId()).stream().filter(c -> c.getBlocked() == Blockresult.Blocked).count();


		}



		private Long id;
		private Long userId;
		private String userName;
		private String screenName;
		private String iconUrl;
		private String statusText;
		private String via;
		private ArrayList<String> mediaUrlList = new ArrayList<String>();
		private String userUrl;
		private String statusUrl;
		private Boolean hasImage;
		private Blockresult blockedHistory;
		private Boolean isBlocked;
		private Long blockedCount;
	}
}
