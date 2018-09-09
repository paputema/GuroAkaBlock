package com.GuroAka.Block.data;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "useraccount")
public class DataUserAccount {
	public DataUserAccount() {
		super();
		lastblockdate = new Date();
	}
	@Id
	@Column
	private Long userid;
	@Column(name = "accesstoken")
	private String AccessToken;
	@Column(name = "accesstokensecret")
	private String AccessTokenSecret;
	@Column(name = "lastblockdate")
	private Date lastblockdate;
	@Column(name = "verify")
	private boolean verify;
	public Long getUserid() {
		return userid;
	}
	public void setUserid(Long userid) {
		this.userid = userid;
	}
	public String getAccessToken() {
		return AccessToken;
	}
	public void setAccessToken(String accessToken) {
		AccessToken = accessToken;
	}
	public String getAccessTokenSecret() {
		return AccessTokenSecret;
	}
	public void setAccessTokenSecret(String accessTokenSecret) {
		AccessTokenSecret = accessTokenSecret;
	}
	public Date getLastblockdate() {
		return lastblockdate;
	}
	public void setLastblockdate(Date lastblockdate) {
		this.lastblockdate = lastblockdate;
	}
	public boolean isVerify() {
		return verify;
	}
	public void setVerify(boolean verify) {
		this.verify = verify;
	}

}
