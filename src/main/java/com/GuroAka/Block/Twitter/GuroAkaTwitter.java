package com.GuroAka.Block.Twitter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.supercsv.prefs.CsvPreference;

import com.GuroAka.Block.Blockresult;
import com.GuroAka.Block.csv.GuroAkaCsv;
import com.GuroAka.Block.data.DataBlockLog;
import com.GuroAka.Block.data.DataGuroAccount;
import com.GuroAka.Block.data.DataGuroAccountList;
import com.GuroAka.Block.data.DataSearchResult;
import com.GuroAka.Block.data.DataUserAccount;
import com.GuroAka.Block.data.DataVolunteerList;
import com.GuroAka.Block.data.DataWhiteListAccount;
import com.GuroAka.Block.data.SauceData;
import com.GuroAka.Block.repositories.BlockLogDataRepository;
import com.GuroAka.Block.repositories.GuroAccountDataRepository;
import com.GuroAka.Block.repositories.GuroAkaSearchResultDataRepository;
import com.GuroAka.Block.repositories.ListDataRepository;
import com.GuroAka.Block.repositories.RepositoryDataBlockedHistory;
import com.GuroAka.Block.repositories.SauceDataRepository;
import com.GuroAka.Block.repositories.UserAccountDataRepository;
import com.GuroAka.Block.repositories.VolunteerDataRepository;
import com.GuroAka.Block.repositories.WhiteListAccountDataRepository;
import com.github.mygreen.supercsv.io.CsvAnnotationBeanReader;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import twitter4j.PagableResponseList;
import twitter4j.Paging;
import twitter4j.RateLimitStatus;
import twitter4j.Relationship;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.UserList;
import twitter4j.auth.AccessToken;
@Service
public class GuroAkaTwitter {
	public enum Verifi
	{
		True,False,NoChange
	}
	private static final Log LOG = LogFactory.getLog(GuroAkaTwitter.class);
	@Autowired
	private SauceDataRepository sauceDataRepository;
	@Autowired
	private VolunteerDataRepository volunteerDataRepository;
	@Autowired
	private  ListDataRepository listDataRepository;
	@Autowired
	private  GuroAccountDataRepository guroAccountDataRepository;
	@Autowired
	private  WhiteListAccountDataRepository whiteListAccountDataRepository;
	@Autowired
	private  BlockLogDataRepository blockLogDataRepository;
	@Autowired
	private  GuroAkaSearchResultDataRepository guroAkaSearchResultDataRepository;
	@Autowired
	private RepositoryDataBlockedHistory repositoryDataBlockedHistory;
	@Autowired
	private UserAccountDataRepository userAccountDataRepository;
	private  String lineCd = System.getProperty("line.separator");
	@Value("${scv.path}")
	private String dir;
	private TwitterBlocker managerTwitter;
	public Twitter getManagerTwitter()
	{
		return managerTwitter.twitter;
	}
	@PostConstruct
	private void construct()
	{
		managerTwitter = new TwitterBlocker();
		ConstructListDetails();
	}

	public ResponseList<Status> getRetweets(Status status)
	{
		return managerTwitter.getRetweets(status);
	}
	public TwitterBlocker TwitterBlockerGetInstance(Twitter twitter, Verifi verifi) throws TwitterException
	{
		TwitterBlocker ret = new TwitterBlocker(twitter,verifi);
		return ret;
	}

	public class TwitterBlocker{

		private DataUserAccount dataUserAccount;
		public TwitterBlocker()
		{
			this.twitter = TwitterFactory.getSingleton();
			this.getGuroAkaUsers();
		}
		public TwitterBlocker(Twitter twitter, Verifi verifi) throws TwitterException
		{
			this.twitter = twitter;

			DataUserAccount dataUserAccount = userAccountDataRepository.findByUserid(twitter.getId());
			if(dataUserAccount == null)
			{
				dataUserAccount = new DataUserAccount();
				dataUserAccount.setUserid(twitter.getId());
			}
			AccessToken accessToken = twitter.getOAuthAccessToken();
			dataUserAccount.setAccessToken(accessToken.getToken());
			dataUserAccount.setAccessTokenSecret(accessToken.getTokenSecret());
			switch (verifi) {
			case True:
				dataUserAccount.setVerify(true);
				break;
			case False:
				dataUserAccount.setVerify(false);
				break;
			default:
				break;
			}
			this.dataUserAccount = dataUserAccount;
		}

		private RateLimitStatus rateLimitStatusGetRtw = null;
		public ResponseList<Status> getRetweets(Status status) {
			ResponseList<Status> ret = null;
			if(checkRateLimit(rateLimitStatusGetRtw))
			{
				try {
					ret = twitter.getRetweets(status.getId());
					rateLimitStatusGetRtw = ret.getRateLimitStatus();
				} catch (TwitterException e) {
				}
			}
			return ret;
		}
		private Twitter twitter;
		private List<ListDetail> listDetails;

		private void GuroAkaCsv2Db() {
			Path fileDir =  Paths.get(dir);
			if(Files.exists(fileDir))
			{
				LOG.info(dir);
				Set<GuroAkaCsv> guroAkaCsvs = new HashSet<>();
				try {
					List<Path> files =  Files.list(fileDir).collect(Collectors.toList());
					for (Path path : files) {
						Path fileName;
						if((fileName = path.getFileName()) != null && fileName.toString() != null)
						{
							LOG.info(fileName.toString());
							if(Pattern.matches("^.*.CSV$", fileName.toString().toUpperCase()))
							{
								CsvAnnotationBeanReader<GuroAkaCsv> csvAnnotationBeanReader =
										new CsvAnnotationBeanReader<>(GuroAkaCsv.class,
																		Files.newBufferedReader(path),
																		CsvPreference.STANDARD_PREFERENCE);
								guroAkaCsvs.addAll(csvAnnotationBeanReader.readAll());
								csvAnnotationBeanReader.close();
							}
						}
					}
				} catch (IOException e) {
					// TODO 自動生成された catch ブロック
					e.printStackTrace();
				}
				for (GuroAkaCsv guroAkaCsv : guroAkaCsvs) {
					try {
						User user = showUser(guroAkaCsv.getUserId());
						if(guroAccountDataRepository.findByUserid(user.getId()) == null &&  whiteListAccountDataRepository.findAllByUserid(user.getId()) == null)
						{
							guroAccountDataRepository.saveAndFlush(new DataGuroAccount(user));
						}
					} catch (TwitterException e) {
						// TODO 自動生成された catch ブロック
						LOG.info(e.getErrorMessage() + e.getMessage());
						guroAccountDataRepository.removeByUserid(guroAkaCsv.getUserId());
					}finally {

					}
				}
				guroAkaCsvs = null;
			}
			System.gc();
		}
		private Set<Status> getMediaEntities (User user){
			Set<Status> retStatusSet = new HashSet<>();
			Paging paging = new Paging(1,200);

				ResponseList<Status> statusList = null ;
				do {
					try {
						statusList = twitter.getUserTimeline(user.getId(), paging);
						sleepRateLimit(statusList.getRateLimitStatus());
						for (Status status : statusList) {
							if(status.getMediaEntities().length > 0 && status.isRetweeted() == false)
							{
								retStatusSet.add(status);
								sauceDataRepository.saveAndFlush(new SauceData(status.getSource()));
							}
						}
						paging.setPage(paging.getPage() + 1);
					} catch (TwitterException e) {
						// TODO 自動生成された catch ブロック
						checkRateLimit(e.getRateLimitStatus());
					}

				} while (statusList != null && statusList.size() > 0);

			return retStatusSet;
		}
		private Set<Status> getStatus (User user){
			Set<Status> retStatusSet = new HashSet<>();
			Paging paging = new Paging(1,200);

				ResponseList<Status> statusList = null ;
				do {
					try {
						statusList = twitter.getUserTimeline(user.getId(), paging);
						sleepRateLimit(statusList.getRateLimitStatus());
						for (Status status : statusList) {
							if(status.isRetweeted() == false)
							{
								retStatusSet.add(status);
							}
						}
						paging.setPage(paging.getPage() + 1);
					} catch (TwitterException e) {
						// TODO 自動生成された catch ブロック
						checkRateLimit(e.getRateLimitStatus());
					}

				} while (statusList != null && statusList.size() > 0);

			return retStatusSet;
		}
		public Map<Long, User> GetGuroAkaByList(Long listId){
			Map<Long, User> returnUsers = new HashMap<>();
			long cursor = -1L;
			PagableResponseList<User> users = null;
			do {
				try {
					users = twitter.getUserListMembers(listId, cursor);
					sleepRateLimit(users.getRateLimitStatus());
					for (User user : users) {
						returnUsers.put(user.getId(), user);
					}
					cursor = users.getNextCursor();
				} catch (TwitterException e) {
					e.printStackTrace();
				}
			} while (users != null && users.hasNext());
			return returnUsers;
		}
		private Map<Long, User> GetGuroAkaByFollowee(DataVolunteerList dataVolunteerList) {
			long cursor = -1L;
			Map<Long, User> returnUsers = new HashMap<>();
			PagableResponseList<User> users = null;
			do {
				try {
					users = twitter.getFriendsList(dataVolunteerList.getUserid(), cursor);
					sleepRateLimit(users.getRateLimitStatus());
					cursor = users.getNextCursor();
					for (User user : users) {
						returnUsers.put(user.getId(), user);
					}
				} catch (TwitterException e) {
					//e.printStackTrace();
				}
			} while (users != null && users.hasNext());
			return returnUsers;
		}
		private User showUser(Long id) throws TwitterException {
			//User returnUser = guroAkaUsers.get(id);
			User returnUser = null;
			if(returnUser== null){
				returnUser = twitter.showUser(id);
				sleepRateLimit(returnUser.getRateLimitStatus());
			}
			return returnUser;
		}
		private User showUser(String screenname) throws TwitterException {
			User returnUser = twitter.showUser(screenname);
			checkRateLimit(returnUser.getRateLimitStatus());
			return returnUser;
		}

		private RateLimitStatus rateLimitStatusRelationship = null;
		private Blockresult isBlocked(Long id)
		{
			Blockresult blockresult = dataUserAccount.getBlockedHistory().get(id);

			if(blockresult == null)
			{
				blockresult = Blockresult.UnBlocked;

				try {
					sleepRateLimit(rateLimitStatusRelationship);
					Relationship relationship;

					relationship = twitter.showFriendship(dataUserAccount.getUserid(), id);

					rateLimitStatusRelationship = relationship.getRateLimitStatus();
					if(relationship.isSourceBlockingTarget())
					{
						if (whiteListAccountDataRepository.existsByUserid(id) == true)
						{
							blockresult = Blockresult.InWhiteListBlocked;
						}else
						{
							blockresult = Blockresult.Blocked;
						}
					}else if(relationship.isSourceFollowingTarget())
					{
						blockresult = Blockresult.FF;
					}else if(whiteListAccountDataRepository.existsByUserid(id) == true)
					{
						blockresult = Blockresult.InWhiteListUnBlocked;
					}

				} catch (TwitterException e) {
					// TODO 自動生成された catch ブロック
					e.printStackTrace();
				}
			}
			return blockresult;
		}

		public Result doBlock(Long targetId) throws TwitterException
		{
			User guroAkaUser = twitter.showUser(targetId);
			Result ret = doBlock(guroAkaUser);
			userAccountDataRepository.saveAndFlush(dataUserAccount);
			return ret;
		}
		private void setBlocked(User guroAkaUser,Blockresult blockedHistory) {
			dataUserAccount.getBlockedHistory().put(guroAkaUser.getId(), blockedHistory);
		}

		public Result doBlock(User guroAkaUser) throws TwitterException
		{
				Result ret = null;
				Blockresult blockedHistory = dataUserAccount.getBlockedHistory().get(guroAkaUser.getId());
				if(blockedHistory == null)
				{
					blockedHistory = isBlocked(guroAkaUser.getId());
				}
				switch (blockedHistory) {
				case UnBlocked:
				case Failure:
					checkRateLimit(twitter.createBlock(guroAkaUser.getId()).getRateLimitStatus());
					checkRateLimit(twitter.reportSpam(guroAkaUser.getId()).getRateLimitStatus());
					Calendar calendar = Calendar.getInstance();
					calendar.add(Calendar.MONTH, -3);
					if (guroAkaUser.getCreatedAt().before(calendar.getTime())) {
						ret = (new Result(guroAkaUser,"ブロックしました(3ヶ月以上前から存在するアカウント：" + guroAkaUser.getCreatedAt().toString(), blockedHistory));
					} else {
						ret = (new Result(guroAkaUser, "ブロックしました", blockedHistory));
					}
					setBlocked(guroAkaUser,Blockresult.Blocked);
					break;
				case InWhiteListBlocked:
					checkRateLimit(twitter.destroyBlock(guroAkaUser.getId()).getRateLimitStatus());
					ret = (new Result(guroAkaUser,"ホワイトリスト入りのアカウントですブロックを解除しました", blockedHistory));
					setBlocked(guroAkaUser,Blockresult.InWhiteListUnBlocked);
					break;
				case InWhiteListUnBlocked:
					ret = (new Result(guroAkaUser,"ホワイトリスト入りのアカウントです", blockedHistory));
					break;
				case FF:
					ret = (new Result(guroAkaUser, "現在フォロー中のアカウントです", blockedHistory));
					break;
				case Blocked:
				case Success:
					ret = (new Result(guroAkaUser, "ブロック済みです", blockedHistory));
				default:
					break;
				}
			return ret;

		}
		public Map<Long, Blockresult> getMapBlockedHistory()
		{
			Map<Long, Blockresult> ret = dataUserAccount.getBlockedHistory();
			return ret;
		}

		public Results doBlock() {
			List<Result> ListResultBlock = new ArrayList<>();
			List<Result> ListResultNotBlock = new ArrayList<>();
			String ResultsText = new String();
			try {
				long UserId = twitter.getId();



				for (User guroAkaUser : getGuroAkaUsers().values()) {
					try {
						Result result = doBlock(guroAkaUser);
						setBlocked(guroAkaUser, result.getBlockedHistory());
						ListResultBlock.add(result);

					} catch (TwitterException e) {
						// TODO 自動生成された catch ブロック
						//e.printStackTrace();
						ResultsText  = "ブロック実行中にエラーが発生しました、しばらくたってから再度実行してみてください。" + lineCd;
						setBlocked(guroAkaUser, Blockresult.Failure);
						ListResultBlock.add(new Result(guroAkaUser,e.getErrorMessage() + ":" + e.getMessage(), Blockresult.Failure));
						checkRateLimit(e.getRateLimitStatus());
					}
				}
				for (User whiteAkaUser : GetWhiteAkaByDb().values()) {
					Blockresult blockedHistory =  dataUserAccount.getBlockedHistory().get(whiteAkaUser.getId());
					if(blockedHistory == null)
					{
						blockedHistory = isBlocked(whiteAkaUser.getId());
						setBlocked(whiteAkaUser, blockedHistory);
					}
					if (blockedHistory == Blockresult.InWhiteListBlocked) {
						checkRateLimit(twitter.destroyBlock(whiteAkaUser.getId()).getRateLimitStatus());
						blockedHistory = (Blockresult.InWhiteListUnBlocked);
						setBlocked(whiteAkaUser, blockedHistory);
						ListResultNotBlock.add(new Result(whiteAkaUser,"ホワイトリスト入りのアカウントですブロックを解除しました", blockedHistory));

					}
				}
				repositoryDataBlockedHistory.flush();
				blockLogDataRepository.saveAndFlush(new DataBlockLog(UserId));
			} catch (TwitterException e) {
				// TODO 自動生成された catch ブロック
				//e.printStackTrace();
				ResultsText = "ブロック実行中にエラーが発生しました、しばらくたってから再度実行してみてください。" + lineCd;
			}
			userAccountDataRepository.saveAndFlush(dataUserAccount);
			return new Results(ListResultBlock,ListResultNotBlock,ResultsText);
		}
		public Map<Long,User> getGuroAkaUsers() {
			Map<Long,User> guroAkaUsers = new HashMap<Long,User>();
			GetGuroAkaByList(guroAkaUsers);
			GetGuroAkaByDb(guroAkaUsers);
			GetGuroAkaByFollowee(guroAkaUsers);
			getGuroAkaBySearch(guroAkaUsers);


			for (Long key : GetWhiteAkaByDb().keySet()) {
				guroAkaUsers.remove(key);
			}


			return guroAkaUsers;
		}
		private void getGuroAkaBySearch(Map<Long,User> guroAkaUsers) {
			for (DataSearchResult guroAka : guroAkaSearchResultDataRepository.findAll()) {
				try {
					User guroUser = twitter.showUser(guroAka.getUserid());
					guroAkaUsers.put(guroAka.getUserid(), guroUser);
					sleepRateLimit(guroUser.getRateLimitStatus());
				} catch (TwitterException e) {
					if(e.getErrorCode() == 63)
					{
						guroAkaSearchResultDataRepository.delete(guroAka);
					}
				}
			}
		}
		private void deletelisted(Map<Long, User> users)
		{
			for(User user : users.values())
			{
				DataGuroAccount dataGuroAccount = guroAccountDataRepository.findByUserid(user.getId());
				if(dataGuroAccount != null)
				{
					guroAccountDataRepository.delete(dataGuroAccount);
				}
			}
			guroAccountDataRepository.flush();
		}
		private boolean GetGuroAkaByList(Map<Long,User> guroAkaUsers) {
			for (DataGuroAccountList dataGuroAccountList : listDataRepository.findAll()) {
				Map<Long, User> users = GetGuroAkaByList(dataGuroAccountList.getListid());
				deletelisted(users);
				guroAkaUsers.putAll(users);
			}
			return true;
		}
		private boolean GetGuroAkaByFollowee(Map<Long,User> guroAkaUsers) {
			for (DataVolunteerList dataVolunteerList : volunteerDataRepository.findAll()) {
				Map<Long, User> users = GetGuroAkaByFollowee(dataVolunteerList);
				deletelisted(users);
				guroAkaUsers.putAll(users);
			}
			return true;
		}
		private boolean GetGuroAkaByDb(Map<Long,User> guroAkaUsers) {
			for (DataGuroAccount dataGuroAccount : guroAccountDataRepository.findAll()) {

				User user;
				try {
					if (dataGuroAccount.getUserid() != null) {
						user = showUser(dataGuroAccount.getUserid());
					} else {
						user = showUser(dataGuroAccount.getScreenname());
					}
					dataGuroAccount.setUserid(user.getId());
					dataGuroAccount.setUsername(user.getName());
					dataGuroAccount.setIconurl(user.getProfileImageURL());
					Status status = user.getStatus();
					if (status != null && status.getMediaEntities().length > 0) {
						dataGuroAccount.setImgurl(status.getMediaEntities()[0].getMediaURL());
					}
					guroAccountDataRepository.save(dataGuroAccount);
					guroAkaUsers.put(user.getId(),user);
				} catch (TwitterException e) {
					// TODO 自動生成された catch ブロック
					if (e.getErrorCode() == 63) {
						if (dataGuroAccount.getUserid() != null) {
							guroAccountDataRepository.delete(dataGuroAccount);
						}else
						{
							guroAccountDataRepository.delete(dataGuroAccount);
						}
					} else {
						e.printStackTrace();
					}


				} finally {
					guroAccountDataRepository.flush();
				}
			}
			return true;
		}
		private Map<Long,User> GetWhiteAkaByDb() {
			Map<Long,User> whiteAkaUsers = new HashMap<Long,User>();

			List<DataWhiteListAccount>  dataWhiteListAccountList =  whiteListAccountDataRepository.findAll();

			for (DataWhiteListAccount dataWhiteListAccount : dataWhiteListAccountList) {
				if (dataWhiteListAccount.getUserid() != null && dataWhiteListAccount.getUser() != null) {
					whiteAkaUsers.put(dataWhiteListAccount.getUserid(),(dataWhiteListAccount.getUser()));
				} else {
					try {
						User user = showUser(dataWhiteListAccount.getScreenname());
						dataWhiteListAccount.setUserid(user.getId());
						dataWhiteListAccount.setUsername(user.getName());
						dataWhiteListAccount.setIconurl(user.getProfileImageURL());
						dataWhiteListAccount.setUser(user);
						Status status = user.getStatus();
						if (status != null && status.getMediaEntities().length > 0) {
							dataWhiteListAccount.setImgurl(status.getMediaEntities()[0].getMediaURL());
						}
						whiteListAccountDataRepository.save(dataWhiteListAccount);
						whiteAkaUsers.put(user.getId(),user);
					} catch (TwitterException e) {
						// TODO 自動生成された catch ブロック
						e.printStackTrace();
						whiteListAccountDataRepository.delete(dataWhiteListAccount);
					}
				}
			}
			whiteListAccountDataRepository.flush();
			return whiteAkaUsers;
		}
		public Results doBlockDestroy() {
			// TODO 自動生成されたメソッド・スタブ
			final List<Result> ListResultBlock = new ArrayList<>();
			final List<Result> ListResultNotBlock = new ArrayList<>();
			Map<Long, User> guroAkaUsers = getGuroAkaUsers();
			String ResultsText= new String();
			ExecutorService executorService = Executors.newCachedThreadPool();
			try {
				final long UserId = twitter.getId();
				final Calendar calendar = Calendar.getInstance();
				calendar.add(Calendar.MONTH, -3);
				ArrayList<Long> removeidList = new ArrayList<>();
				executorService.submit(new Runnable() {
					public void run() {
						for (final Long guroAkaUserId : dataUserAccount.getBlockedHistory().keySet()) {
							Blockresult blockedHistory = isBlocked(guroAkaUserId);
							try {
								User guroAkaUser = twitter.destroyBlock(guroAkaUserId);
								sleepRateLimit(guroAkaUser.getRateLimitStatus());
								blockedHistory = Blockresult.UnBlocked;
								ListResultBlock.add(new Result(guroAkaUser, "ブロック解除しました", blockedHistory));
								removeidList.add(guroAkaUserId);
								//LOG.debug(guroAkaUser.getName() + guroAkaUser.getScreenName() + "ブロック解除しました");
							} catch (TwitterException e) {
								// TODO 自動生成された catch ブロック
								Relationship relationship;
								try {
									switch (e.getErrorCode()) {
									case 34:
										ListResultBlock.add(new Result("intent/user?user_id=" + guroAkaUserId, "凍結済みのアカウントです。", blockedHistory));
										removeidList.add(guroAkaUserId);
										break;

									default:
										relationship = twitter.showFriendship(UserId, guroAkaUserId);
										if(relationship.isSourceBlockingTarget())
										{
											ListResultBlock.add(new Result(relationship.getTargetUserScreenName(), "ブロック解除に失敗しました", blockedHistory));
										}else
										{
											ListResultBlock.add(new Result(relationship.getTargetUserScreenName(), "ブロックしていません", blockedHistory));
										}
										blockedHistory = (Blockresult.Failure);
										break;
									}
								} catch (TwitterException e1) {
									// TODO 自動生成された catch ブロック
									e1.printStackTrace();
									ListResultBlock.add(new Result("エラーが起こりました:", e1.getErrorMessage(), blockedHistory));
								}
							}finally {

							}
						}
						for (Long removeid : removeidList) {
							dataUserAccount.getBlockedHistory().remove(removeid);
						}
						userAccountDataRepository.saveAndFlush(dataUserAccount);
					};
				});

				for (User whiteAkaUser : GetWhiteAkaByDb().values()) {
					Blockresult blockedHistory = isBlocked(whiteAkaUser.getId());
					if (blockedHistory == Blockresult.InWhiteListBlocked) {
						checkRateLimit(twitter.destroyBlock(whiteAkaUser.getId()).getRateLimitStatus());
						blockedHistory = (Blockresult.InWhiteListUnBlocked);
						ListResultNotBlock.add(new Result(whiteAkaUser,"ホワイトリスト入りのアカウントですブロックを解除しました", blockedHistory));
					}
				}
				blockLogDataRepository.saveAndFlush(new DataBlockLog(UserId));
				executorService.shutdown();
				for (long time = 1 ;  !executorService.isTerminated() && time < 60 ; time++) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO 自動生成された catch ブロック
						e.printStackTrace();
					}
				}

				if(executorService.isTerminated())
				{
					ResultsText = ListResultBlock.size() + "アカウントのブロックを解除しました";
				}else
				{
					ResultsText = guroAkaUsers.size() + "アカウント中" + ListResultBlock.size() + "アカウントにブロックを解除しました、残りを引き続きブロック解除します。ページは閉じて大丈夫です。";
				}
				blockLogDataRepository.flush();
			} catch (TwitterException e) {
				// TODO 自動生成された catch ブロック
				//e.printStackTrace();
				ResultsText = "ブロック実行中にエラーが発生しました、しばらくたってから再度実行してみてください。" + lineCd;
			}
			return new Results(ListResultBlock,ListResultNotBlock,ResultsText);
		}
		public void ConstructListDetails()
		{
			listDetails = new ArrayList<ListDetail>();
			List<DataGuroAccountList> DataGuroAccountLists = listDataRepository.findAll();
			for (DataGuroAccountList dataGuroAccountList : DataGuroAccountLists) {
				try {
					UserList userList = twitter.showUserList(dataGuroAccountList.getListid());
					listDetails.add(new ListDetail(userList));
				} catch (TwitterException e) {
					if(e.getErrorCode() != 88)
					{
						listDataRepository.delete(dataGuroAccountList);
					}else
					{
						sleepRateLimit(e.getRateLimitStatus());
					}
				}
			}
		}
		public Status getStatus(long searchSinceId)  {
			// TODO 自動生成されたメソッド・スタブ
			Status ret = null;
			try {
				ret = twitter.showStatus(searchSinceId);
			} catch (TwitterException e) {
				// TODO 自動生成された catch ブロック
			}
			return ret;
		}
	}
	private Boolean checkRateLimit(RateLimitStatus rateLimitStatus) {

		return (rateLimitStatus != null && rateLimitStatus.getRemaining() <= 0);
	}
	private void sleepRateLimit(RateLimitStatus rateLimitStatus) {
		if (rateLimitStatus != null && rateLimitStatus.getRemaining() <= 0) {
			long time = rateLimitStatus.getSecondsUntilReset();
			//LOG.debug(rateLimitStatus.toString());
			try {
				if (time < 0) {
					time += 120;
				}
				Thread.sleep(time * 1000);
			} catch (InterruptedException | IllegalArgumentException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}
		}
	}
	@Scheduled(cron = "0 0 * * * * ")
	public void ConstructListDetails()
	{
		managerTwitter.ConstructListDetails();
	}
	@AllArgsConstructor
	@NoArgsConstructor
	class ListDetail
	{
		@Getter@Setter
		String userUrl;
		@Getter@Setter
		String listUrl;
		public ListDetail(UserList userList) {
			userUrl = userList.getUser().getName() + "さんの公開リスト";
			listUrl = userList.getURI().toString();
		}

	}
	public List<ListDetail> getListDetails() {
		return managerTwitter.listDetails;
	}
	public Map<Long, User>  getGuroAkaUsers() {
		return managerTwitter.getGuroAkaUsers();
	}
	public Set<Status>  geMediaEntities(User user) {
		// TODO 自動生成されたメソッド・スタブ
		return managerTwitter.getMediaEntities(user);
	}
	public Set<Status>  getStatus(User user) {
		// TODO 自動生成されたメソッド・スタブ
		return managerTwitter.getStatus(user);
	}
	public void GuroAkaCsv2Db() {
		managerTwitter.GuroAkaCsv2Db();

	}
	public Map<Long, User> GetGuroAkaByList(Long listid) {
		// TODO 自動生成されたメソッド・スタブ
		return managerTwitter.GetGuroAkaByList(listid);
	}

	public Status getStatus(long searchSinceId) throws TwitterException {
		// TODO 自動生成されたメソッド・スタブ
		return managerTwitter.getStatus(searchSinceId);
	}







}