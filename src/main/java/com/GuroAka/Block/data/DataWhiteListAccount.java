package com.GuroAka.Block.data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;
import lombok.NoArgsConstructor;
import twitter4j.User;


@Entity
@Table(name = "whitelistaccount")
@NoArgsConstructor
@Data
public class DataWhiteListAccount {
	public DataWhiteListAccount(User user)
	{
		userid = user.getId();
		username = user.getName();
		screenname = user.getScreenName();
		iconurl = user.getBiggerProfileImageURLHttps();
		this.user = user;
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
	@Column(name = "white_user")
	private User user;


}
