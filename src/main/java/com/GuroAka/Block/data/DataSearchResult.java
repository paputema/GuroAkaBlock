package com.GuroAka.Block.data;

import java.io.IOException;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PostRemove;
import javax.persistence.PrePersist;
import javax.persistence.Table;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import lombok.Data;
import lombok.NoArgsConstructor;
import twitter4j.MediaEntity;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.conf.Configuration;
import twitter4j.conf.PropertyConfiguration;

@Entity
@Table(name = "guroakasearchresult")
@NoArgsConstructor
@Data
public class DataSearchResult
{
	private static final Log LOG = LogFactory.getLog(DataSearchResult.class);
	private static final String ls = System.lineSeparator();
	public DataSearchResult(MediaEntity mediaEntity, Status status,Long hitstatusid) {
		User user = status.getUser();
		setUserid(user.getId());
		setScreenname(user.getScreenName());
		setUsername(user.getName());
		setIconurl(user.getProfileImageURLHttps());
		setImgurl(mediaEntity.getMediaURLHttps());
		if(hitstatusid != null && hitstatusid.equals(status.getId())== false)
		{
			setHitstatusid(hitstatusid);
		}
	}
	public DataSearchResult(URLSearchTrackDate urlSearchTrackDate, Status status, Long originalstatus) {
		User user = status.getUser();
		setUserid(user.getId());
		setScreenname(user.getScreenName());
		setUsername(user.getName());
		setIconurl(user.getProfileImageURLHttps());
	}
	@Id
	@Column
	private Long userid;
	@Column
	private String screenname;
	@Column
	private String username;
	@Column
	private String iconurl;
	@Column
	private String imgurl;
	@Column
	private Long reportstatusid;
	@Column
	private Long hitstatusid;

	@PrePersist
	void prePersist ()
	{
		LOG.info("追加"  + ":" + username + "(@" + screenname + "):" + imgurl);
		String status = "自動検出：" + username.replaceAll("@", "(a)") + ls
				+ " https://twitter.com/intent/user?user_id=" + userid + ls
				+ " https://twitter.com/" + screenname + ls
				+ " #グロ垢なので閲覧注意  #グロ垢殲滅キャンペーン" + ls;
		if(hitstatusid != null)
		{
			status = status.concat("一致:　https://twitter.com/-/status/" + hitstatusid);
		}
		StatusUpdate statusUpdate = new StatusUpdate(status);
		Status ret =  reportTweet(statusUpdate);
		if(ret != null)
		{
			setReportstatusid(ret.getId());
		}
	}
	@PostRemove
	void preRemove ()
	{
		LOG.info("削除"  + ":" + username + "(@" + screenname + "):" + imgurl);
		String status = "自動削除：" + username.replaceAll("@", "(a)") + ls
				+ " https://twitter.com/intent/user?user_id=" + userid + ls
				+ " https://twitter.com/" + screenname + ls
				+ " 凍結済みまたは誤検知の削除";
		StatusUpdate statusUpdate = new StatusUpdate(status);

		if(reportstatusid != null)
		{
			statusUpdate.setInReplyToStatusId(reportstatusid);
		}
		reportTweet(statusUpdate);
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


}
