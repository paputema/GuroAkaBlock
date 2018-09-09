package com.GuroAka.Block.data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.NoArgsConstructor;
import twitter4j.User;

@Entity
@Table(name = "whitelistaccount")
@NoArgsConstructor
public class DataWhiteListAccount {
	public DataWhiteListAccount(User user)
	{
		userid = user.getId();
		username = user.getName();
		screenname = user.getScreenName();
		iconurl = user.getBiggerProfileImageURLHttps();

	}
	public Long getUserid() {
		return userid;
	}

	public void setUserid(Long userid) {
		this.userid = userid;
	}

	public String getScreenname() {
		return screenname;
	}

	public void setScreenname(String screenname) {
		this.screenname = screenname;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getIconurl() {
		return iconurl;
	}

	public void setIconurl(String iconurl) {
		this.iconurl = iconurl;
	}

	@Id
	@Column
	private String screenname;
	@Column
	private Long userid;
	@Column
	private String username;
	@Column
	private String iconurl;
	@Column
	private String imgurl;

	public String getImgurl() {
		return imgurl;
	}

	public void setImgurl(String imgurl) {
		this.imgurl = imgurl;
	}

}
