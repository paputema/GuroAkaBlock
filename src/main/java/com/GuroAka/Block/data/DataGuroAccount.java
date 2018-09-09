package com.GuroAka.Block.data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import twitter4j.User;

@Entity
@Table(name = "guroaccount")
public class DataGuroAccount {
	public DataGuroAccount(User user) {
		setUserid(user.getId());
		setScreenname( user.getScreenName()) ;
		setUsername(user.getName());
		setIconurl(user.getProfileImageURLHttps());
	}

	public DataGuroAccount() {
		super();
		// TODO 自動生成されたコンストラクター・スタブ
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

	public String getScreenname() {
		return screenname;
	}

	public void setScreenname(String screenname) {
		this.screenname = screenname;
	}

	public Long getUserid() {
		return userid;
	}

	public void setUserid(Long userid) {
		this.userid = userid;
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

}
