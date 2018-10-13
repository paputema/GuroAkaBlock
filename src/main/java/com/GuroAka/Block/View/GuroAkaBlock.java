package com.GuroAka.Block.View;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.GuroAka.Block.Twitter.GuroAkaTwitter;
import com.GuroAka.Block.Twitter.GuroAkaTwitter.TwitterBlocker;
import com.GuroAka.Block.Twitter.GuroAkaTwitter.Verifi;
import com.GuroAka.Block.Twitter.Results;
import com.GuroAka.Block.data.DataGuroAccount;
import com.GuroAka.Block.data.DataWhiteListAccount;
import com.GuroAka.Block.repositories.GuroAccountDataRepository;
import com.GuroAka.Block.repositories.WhiteListAccountDataRepository;

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
public class GuroAkaBlock {
    //@Value("${api.domain}") private String domain;

	@Autowired
	private HttpSession session;
	@Autowired
	private HttpServletRequest request;
	@Autowired
	private GuroAccountDataRepository guroAccountDataRepository;
	@Autowired
	private WhiteListAccountDataRepository whiteListAccountDataRepository;

	private List<DataGuroAccount> dataGuroAccounts = new ArrayList<>();
	@Autowired
	private GuroAkaTwitter guroAkaTwitter;



	private List<DataGuroAccount> getGuroAkaDateList() {
		Twitter twitterMonchu = null;
		List<DataGuroAccount> accountDatas = guroAccountDataRepository.findAll();
		for (DataGuroAccount accountData : accountDatas) {
			if (accountData.getScreenname() == null || accountData.getIconurl() == null
					|| accountData.getUserid() == null) {
				if (twitterMonchu == null) {

					ConfigurationBuilder builder = new ConfigurationBuilder();

					twitterMonchu = new TwitterFactory(builder.build()).getInstance();
				}
				User user;
				try {
					user = twitterMonchu.showUser(accountData.getScreenname());
					accountData.setUserid(user.getId());
					accountData.setUsername(user.getName());
					accountData.setIconurl(user.getProfileImageURL());
					Status status = user.getStatus();
					if (status != null && status.getExtendedMediaEntities().length > 0) {
						accountData.setImgurl(status.getExtendedMediaEntities()[0].getMediaURL());
					}
					guroAccountDataRepository.saveAndFlush(accountData);
				} catch (TwitterException e) {
					// TODO 自動生成された catch ブロック
					e.printStackTrace();
				}

			}
		}
		return accountDatas;
	}
	private List<DataWhiteListAccount> getwhiteAkaDateList() {
		Twitter twitterMonchu = null;
		List<DataWhiteListAccount> accountDatas = whiteListAccountDataRepository.findAll();
		for (DataWhiteListAccount accountData : accountDatas) {
			if (accountData.getScreenname() == null || accountData.getIconurl() == null
					|| accountData.getUserid() == null) {
				if (twitterMonchu == null) {

					ConfigurationBuilder builder = new ConfigurationBuilder();

					twitterMonchu = new TwitterFactory(builder.build()).getInstance();
				}
				User user;
				try {
					user = twitterMonchu.showUser(accountData.getScreenname());
					accountData.setUserid(user.getId());
					accountData.setUsername(user.getName());
					accountData.setIconurl(user.getProfileImageURL());
					Status status = user.getStatus();
					if (status != null && status.getExtendedMediaEntities().length > 0) {
						accountData.setImgurl(status.getExtendedMediaEntities()[0].getMediaURL());
					}
					whiteListAccountDataRepository.saveAndFlush(accountData);
				} catch (TwitterException e) {
					// TODO 自動生成された catch ブロック
					e.printStackTrace();
				}

			}
		}
		return accountDatas;
	}
	private void addObjectBySession(ModelAndView mav,String attName,Object defobject)
	{
		Object object = session.getAttribute(attName);
		mav.addObject(attName,(object != null) ? object : defobject);
	}

	@RequestMapping({"/","index"})
	public ModelAndView index(ModelAndView mav) {
		addObjectBySession(mav,"retnotblocklist",null);
		addObjectBySession(mav,"ret","実行後ここにブロック結果が表示されます");
		addObjectBySession(mav,"retblocklist",null);
		addObjectBySession(mav,"indblist",dataGuroAccounts);
		addObjectBySession(mav,"lists",guroAkaTwitter.getListDetails());

		//addObjectBySession(mav,"domain",domain);
		mav.setViewName("index");

		return mav;
	}


	@RequestMapping("InDbList")
	public ModelAndView inDbList(ModelAndView mav) {

		addObjectBySession(mav,"indblist",getGuroAkaDateList());
		addObjectBySession(mav,"ret","実行後ここにブロック結果が表示されます");
		//addObjectBySession(mav,"domain",domain);
		mav.setViewName("index");

		return mav;
	}
	@RequestMapping("WhiteList")
	public ModelAndView whDbList(ModelAndView mav) {

		addObjectBySession(mav,"whitelist",getwhiteAkaDateList());
		addObjectBySession(mav,"ret","実行後ここにブロック結果が表示されます");
		//addObjectBySession(mav,"domain",domain);
		mav.setViewName("index");

		return mav;
	}

	@RequestMapping("requestToken")
	public ModelAndView requestToken(ModelAndView mav) {
		try {
			session.setMaxInactiveInterval(3600);
			mav.addObject("force_login", true);
			OAuthAuthorization oauth = createOAuthAuthorization();


			String callbackURL = request.getRequestURL().toString().replaceAll(request.getServletPath(), "");
			callbackURL += "/accessToken";
			session.setAttribute("requestToken", oauth.getOAuthRequestToken(callbackURL)); //$NON-NLS-1$
			RequestToken requestToken = (RequestToken) session.getAttribute("requestToken");
			mav.setViewName("redirect:" + requestToken.getAuthenticationURL()); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (TwitterException e) {
			// TODO 自動生成された catch ブロック
			mav.setViewName("/"); //$NON-NLS-1$
		}
		return mav;
	}

	@RequestMapping("/ReleaserequestToken")
	public ModelAndView releaseRequestToken(ModelAndView mav) {
		try {
			mav.addObject("force_login", true);
			OAuthAuthorization oauth = createOAuthAuthorization();
			String callbackURL = request.getRequestURL().toString().replaceAll(request.getServletPath(), "");
			callbackURL += "/releaseaccessToken";
			session.setAttribute("requestToken", oauth.getOAuthRequestToken(callbackURL)); //$NON-NLS-1$
			RequestToken requestToken = (RequestToken) session.getAttribute("requestToken");
			mav.setViewName("redirect:" + requestToken.getAuthenticationURL()); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (TwitterException e) {
			// TODO 自動生成された catch ブロック
			mav.setViewName("/"); //$NON-NLS-1$
		}
		return mav;
	}

	@RequestMapping(value ="accessToken")
	public ModelAndView accessToken(ModelAndView mav) {

		try {
			RequestToken requestToken = (RequestToken) session.getAttribute("requestToken"); //$NON-NLS-1$

			AccessToken accessToken = new AccessToken(requestToken.getToken(), requestToken.getTokenSecret());

			OAuthAuthorization oath = createOAuthAuthorization();
			oath.setOAuthAccessToken(accessToken);
			String verifier = request.getParameter("oauth_verifier"); //$NON-NLS-1$
			accessToken = oath.getOAuthAccessToken(verifier);
			Twitter twitter = new TwitterFactory(createConfiguration()).getInstance(accessToken);
			TwitterBlocker twitterBlocker = guroAkaTwitter.TwitterBlockerGetInstance(twitter,Verifi.True);
			session.setAttribute("twitterBlocker", twitterBlocker);


			session.setMaxInactiveInterval(3600);
			User user = twitter.verifyCredentials();
			session.setAttribute("user", user.getName() + "(@" + user.getScreenName() + ")");

			Results doBlock = twitterBlocker.doBlock();

			session.setMaxInactiveInterval(3600);

			session.setAttribute("retnotblocklist",doBlock.getListResultNotBlock());
			session.setAttribute("ret", "ブロック実施結果" + doBlock.getResultsText());
			session.setAttribute("retblocklist", doBlock.getListResultBlock());

		} catch (TwitterException  |  NumberFormatException e) {
			// TODO 自動生成された catch ブロック
			session.setAttribute("ret", e.getMessage());
		}
		session.removeAttribute("id");
		mav.setViewName("redirect:/index/#result");
		return mav;
	}

	@RequestMapping(value ="releaseaccessToken")
	public ModelAndView releaseaccessToken(ModelAndView mav) {

		try {
			RequestToken requestToken = (RequestToken) session.getAttribute("requestToken"); //$NON-NLS-1$

			AccessToken accessToken = new AccessToken(requestToken.getToken(), requestToken.getTokenSecret());

			OAuthAuthorization oath = createOAuthAuthorization();
			oath.setOAuthAccessToken(accessToken);
			String verifier = request.getParameter("oauth_verifier"); //$NON-NLS-1$
			accessToken = oath.getOAuthAccessToken(verifier);
			Twitter twitter = new TwitterFactory(createConfiguration()).getInstance(accessToken);
			TwitterBlocker twitterBlocker = guroAkaTwitter.TwitterBlockerGetInstance(twitter,GuroAkaTwitter.Verifi.False);
			session.setAttribute("twitterBlocker", twitterBlocker);



			session.setMaxInactiveInterval(3600);
			User user = twitter.verifyCredentials();
			session.setAttribute("user", user.getName() + "(@" + user.getScreenName() + ")");
			Results doBlock = twitterBlocker.doBlockDestroy();
			session.setMaxInactiveInterval(3600);

			session.setAttribute("retnotblocklist",doBlock.getListResultNotBlock());
			session.setAttribute("ret", "ブロック解除実施結果" + doBlock.getResultsText());
			session.setAttribute("retblocklist", doBlock.getListResultBlock());

		} catch (TwitterException  |  NumberFormatException e) {
			// TODO 自動生成された catch ブロック
			session.setAttribute("ret", e.getMessage());
		}
		session.removeAttribute("id");
		mav.setViewName("redirect:/index/#result");
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

}
