package com.GuroAka.Block.Twitter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
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
import com.GuroAka.Block.data.DataBlockedHistory;
import com.GuroAka.Block.data.DataBlockedHistoryKeyId;
import com.GuroAka.Block.data.DataGuroAccount;
import com.GuroAka.Block.data.DataGuroAccountList;
import com.GuroAka.Block.data.DataSearchResult;
import com.GuroAka.Block.data.DataVolunteerList;
import com.GuroAka.Block.data.DataWhiteListAccount;
import com.GuroAka.Block.data.SauceData;
import com.GuroAka.Block.repositories.BlockLogDataRepository;
import com.GuroAka.Block.repositories.GuroAccountDataRepository;
import com.GuroAka.Block.repositories.GuroAkaSearchResultDataRepository;
import com.GuroAka.Block.repositories.ListDataRepository;
import com.GuroAka.Block.repositories.RepositoryDataBlockedHistory;
import com.GuroAka.Block.repositories.SauceDataRepository;
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
@Service
public class GuroAkaTwitter {
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
	public TwitterBlocker TwitterBlockerGetInstance(Twitter twitter)
	{
		TwitterBlocker ret = new TwitterBlocker(twitter);
		ret.getGuroAkaUsers();
		return ret;
	}
	public TwitterBlocker TwitterBlockerGetInstanceByManager(Twitter twitter) {
		TwitterBlocker ret = new TwitterBlocker(twitter);
		ret.guroAkaUsers = managerTwitter.guroAkaUsers;
		ret.whiteAkaUsers = managerTwitter.whiteAkaUsers;
		return ret;
	}

	public class TwitterBlocker{
		public TwitterBlocker()
		{
			this.twitter = TwitterFactory.getSingleton();
			this.guroAkaUsers = Collections.synchronizedMap(new HashMap<Long,User>());
			this.whiteAkaUsers = Collections.synchronizedMap(new HashMap<Long,User>());
			this.getGuroAkaUsers();
		}
		public TwitterBlocker(Twitter twitter)
		{
			this.twitter = twitter;
			this.guroAkaUsers = Collections.synchronizedMap(new HashMap<Long,User>());
			this.whiteAkaUsers = Collections.synchronizedMap(new HashMap<Long,User>());
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
		private Map<Long,User> guroAkaUsers;
		private Map<Long,User> whiteAkaUsers;
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
						if(!guroAkaUsers.containsKey(user.getId()) && !whiteAkaUsers.containsKey(user.getId()))
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
			User returnUser = guroAkaUsers.get(id);
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
		private DataBlockedHistory isBlocked(DataBlockedHistoryKeyId id)
		{
			DataBlockedHistory blockedHistory = repositoryDataBlockedHistory.findOne(id);
			Blockresult blockresult = Blockresult.UnBlocked;
			try {

				if(blockedHistory == null)
				{
					blockedHistory = new DataBlockedHistory(id.getUserid(), id.getGuroakaid());
					sleepRateLimit(rateLimitStatusRelationship);
					Relationship relationship = twitter.showFriendship(blockedHistory.getUserid(), blockedHistory.getGuroakaid());
					rateLimitStatusRelationship = relationship.getRateLimitStatus();
					if(relationship.isSourceBlockingTarget())
					{
						if (whiteListAccountDataRepository.existsByUserid(blockedHistory.getGuroakaid()) == true)
						{
							blockresult = Blockresult.InWhiteListBlocked;
						}else
						{
							blockresult = Blockresult.Blocked;
						}
					}else if(relationship.isSourceFollowingTarget())
					{
						blockresult = Blockresult.FF;
					}else if(whiteListAccountDataRepository.existsByUserid(blockedHistory.getGuroakaid()) == true)
					{
						blockresult = Blockresult.InWhiteListUnBlocked;
					}
				}else
				{
					blockresult = blockedHistory.getBlocked();
				}
			} catch (IllegalStateException | TwitterException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}
			blockedHistory.setBlocked(blockresult);
			return blockedHistory;
		}

		public Results doBlock() {
			List<Result> ListResultBlock = new ArrayList<>();
			List<Result> ListResultNotBlock = new ArrayList<>();
			String ResultsText = new String();
			try {
				long UserId = twitter.getId();
				Calendar calendar = Calendar.getInstance();
				calendar.add(Calendar.MONTH, -3);
				for (User guroAkaUser : guroAkaUsers.values()) {
					DataBlockedHistory blockedHistory = isBlocked(new DataBlockedHistoryKeyId(UserId, guroAkaUser.getId()));
					try {
						switch (blockedHistory.getBlocked()) {
						case UnBlocked:
						case Failure:
							checkRateLimit(twitter.createBlock(guroAkaUser.getId()).getRateLimitStatus());
							checkRateLimit(twitter.reportSpam(guroAkaUser.getId()).getRateLimitStatus());
							if (guroAkaUser.getCreatedAt().before(calendar.getTime())) {
								ListResultBlock.add(new Result(guroAkaUser,"ブロックしました(3ヶ月以上前から存在するアカウント：" + guroAkaUser.getCreatedAt().toString()));
							} else {
								ListResultBlock.add(new Result(guroAkaUser, "ブロックしました"));
							}
							blockedHistory.setBlocked(Blockresult.Blocked);
							break;
						case InWhiteListBlocked:
							checkRateLimit(twitter.destroyBlock(guroAkaUser.getId()).getRateLimitStatus());
							ListResultNotBlock.add(new Result(guroAkaUser,"ホワイトリスト入りのアカウントですブロックを解除しました"));
							blockedHistory.setBlocked(Blockresult.InWhiteListUnBlocked);
							break;
						case InWhiteListUnBlocked:
							ListResultNotBlock.add(new Result(guroAkaUser,"ホワイトリスト入りのアカウントです"));
							break;
						case FF:
							ListResultNotBlock.add(new Result(guroAkaUser, "現在フォロー中のアカウントです"));
							break;
						case Blocked:
						case Success:
							ListResultBlock.add(new Result(guroAkaUser, "ブロック済みです"));
						default:

							break;
						}
					} catch (TwitterException e) {
						// TODO 自動生成された catch ブロック
						//e.printStackTrace();
						ResultsText  = "ブロック実行中にエラーが発生しました、しばらくたってから再度実行してみてください。" + lineCd;
						ListResultBlock.add(new Result(guroAkaUser,e.getErrorMessage() + ":" + e.getMessage()));
						checkRateLimit(e.getRateLimitStatus());
					}
					repositoryDataBlockedHistory.saveAndFlush(blockedHistory);
				}
				for (User whiteAkaUser : whiteAkaUsers.values()) {
					DataBlockedHistory blockedHistory = isBlocked(new DataBlockedHistoryKeyId(UserId, whiteAkaUser.getId()));
					if (blockedHistory.getBlocked() == Blockresult.InWhiteListBlocked) {
						checkRateLimit(twitter.destroyBlock(whiteAkaUser.getId()).getRateLimitStatus());
						ListResultNotBlock.add(new Result(whiteAkaUser,"ホワイトリスト入りのアカウントですブロックを解除しました"));
					}
					repositoryDataBlockedHistory.saveAndFlush(blockedHistory);
				}
				blockLogDataRepository.saveAndFlush(new DataBlockLog(UserId));
			} catch (TwitterException e) {
				// TODO 自動生成された catch ブロック
				//e.printStackTrace();
				ResultsText = "ブロック実行中にエラーが発生しました、しばらくたってから再度実行してみてください。" + lineCd;
			}
			return new Results(ListResultBlock,ListResultNotBlock,ResultsText);
		}
		public Map<Long,User> getGuroAkaUsers() {
			GetGuroAkaByList();
			GetGuroAkaByFollowee();
			GetGuroAkaByDb();
			GttGuroAkaBySearch();
			GetWhiteAkaByDb();
			for (Long key : whiteAkaUsers.keySet()) {
				guroAkaUsers.remove(key);
			}
			return guroAkaUsers;
		}
		private void GttGuroAkaBySearch() {
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
		private boolean GetGuroAkaByList() {
			for (DataGuroAccountList dataGuroAccountList : listDataRepository.findAll()) {
				Map<Long, User> users = GetGuroAkaByList(dataGuroAccountList.getListid());
				deletelisted(users);
				guroAkaUsers.putAll(users);
			}
			return true;
		}
		private boolean GetGuroAkaByFollowee() {
			for (DataVolunteerList dataVolunteerList : volunteerDataRepository.findAll()) {
				Map<Long, User> users = GetGuroAkaByFollowee(dataVolunteerList);
				deletelisted(users);
				guroAkaUsers.putAll(users);
			}
			return true;
		}
		private boolean GetGuroAkaByDb() {
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
		private boolean GetWhiteAkaByDb() {
			for (DataWhiteListAccount dataWhiteListAccount : whiteListAccountDataRepository.findAll()) {

				if (dataWhiteListAccount.getUserid() != null) {
					try {
						whiteAkaUsers.put(dataWhiteListAccount.getUserid(),showUser(dataWhiteListAccount.getUserid()));
					} catch (TwitterException e) {
						// TODO 自動生成された catch ブロック
						switch (e.getErrorCode()) {
						}
					}
				} else {
					try {
						User user = showUser(dataWhiteListAccount.getScreenname());
						dataWhiteListAccount.setUserid(user.getId());
						dataWhiteListAccount.setUsername(user.getName());
						dataWhiteListAccount.setIconurl(user.getProfileImageURL());
						Status status = user.getStatus();
						if (status != null && status.getMediaEntities().length > 0) {
							dataWhiteListAccount.setImgurl(status.getMediaEntities()[0].getMediaURL());
						}
						whiteListAccountDataRepository.saveAndFlush(dataWhiteListAccount);
						whiteAkaUsers.put(user.getId(),user);
					} catch (TwitterException e) {
						// TODO 自動生成された catch ブロック
						e.printStackTrace();
						whiteListAccountDataRepository.delete(dataWhiteListAccount);
					}
				}
			}
			return true;
		}
		public Results doBlockDestroy() {
			// TODO 自動生成されたメソッド・スタブ
			final List<Result> ListResultBlock = new ArrayList<>();
			final List<Result> ListResultNotBlock = new ArrayList<>();
			String ResultsText= new String();
			ExecutorService executorService = Executors.newCachedThreadPool();
			try {
				final long UserId = twitter.getId();
				final Calendar calendar = Calendar.getInstance();
				calendar.add(Calendar.MONTH, -3);

				executorService.submit(new Runnable() {
					public void run() {
						for (final User guroAkaUser : guroAkaUsers.values()) {
							DataBlockedHistory blockedHistory = isBlocked(new DataBlockedHistoryKeyId(UserId, guroAkaUser.getId()));
							try {
								sleepRateLimit(twitter.destroyBlock(guroAkaUser.getId()).getRateLimitStatus());
								blockedHistory.setBlocked(Blockresult.UnBlocked);
								ListResultBlock.add(new Result(guroAkaUser, "ブロック解除しました"));
								//LOG.debug(guroAkaUser.getName() + guroAkaUser.getScreenName() + "ブロック解除しました");
							} catch (TwitterException e) {
								// TODO 自動生成された catch ブロック
								Relationship relationship;
								try {
									relationship = twitter.showFriendship(UserId, guroAkaUser.getId());
									if(relationship.isSourceBlockingTarget())
									{
										ListResultBlock.add(new Result(guroAkaUser, "ブロックに失敗しました"));
									}else
									{
										ListResultBlock.add(new Result(guroAkaUser, "ブロックしていません"));
									}
								blockedHistory.setBlocked(Blockresult.Failure);
								} catch (TwitterException e1) {
									// TODO 自動生成された catch ブロック
									e1.printStackTrace();
								}
							}finally {
								repositoryDataBlockedHistory.saveAndFlush(blockedHistory);
							}
						}
					};
				});

				for (User whiteAkaUser : whiteAkaUsers.values()) {
					DataBlockedHistory blockedHistory = isBlocked(new DataBlockedHistoryKeyId(UserId, whiteAkaUser.getId()));
					if (blockedHistory.getBlocked() == Blockresult.InWhiteListBlocked) {
						checkRateLimit(twitter.destroyBlock(whiteAkaUser.getId()).getRateLimitStatus());
						ListResultNotBlock.add(new Result(whiteAkaUser,"ホワイトリスト入りのアカウントですブロックを解除しました"));
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

	public void BlockAndReport(Twitter twitter,Long guroAkaId)  {
		User guroaka = null ;
		try {
			Long UserId = twitter.getId();
			Relationship relationship = twitter.showFriendship(UserId, guroAkaId);
			sleepRateLimit(relationship.getRateLimitStatus());
			if ((managerTwitter.whiteAkaUsers.containsKey(guroAkaId) || relationship.isSourceFollowingTarget()) && relationship.isSourceBlockingTarget()) {
				twitter.destroyBlock(guroAkaId);
				LOG.debug(twitter.getId() + "が" + guroAkaId + "をブロック解除");
			} else if (!relationship.isSourceBlockingTarget()) {
				guroaka = twitter.createBlock(guroAkaId);
				sleepRateLimit(guroaka.getRateLimitStatus());
				sleepRateLimit(twitter.reportSpam(guroAkaId).getRateLimitStatus());
				LOG.debug(twitter.getId() + "が" + guroAkaId + "をブロック");
			}
		} catch (TwitterException e) {

		}
	}
	public Status getStatus(long searchSinceId) throws TwitterException {
		// TODO 自動生成されたメソッド・スタブ
		return managerTwitter.getStatus(searchSinceId);
	}







}