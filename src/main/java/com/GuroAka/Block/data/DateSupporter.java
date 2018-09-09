package com.GuroAka.Block.data;

import java.sql.Timestamp;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

import lombok.Data;
import lombok.NoArgsConstructor;
import twitter4j.User;

@Entity
@Table(name = "guroakasearchsupport")
@NoArgsConstructor
@Data
public class DateSupporter
{
	public DateSupporter(User user) {
		// TODO 自動生成されたコンストラクター・スタブ
		userId = user.getId();
		screenName = user.getScreenName();
		userName = user.getName();
		sinceId = 0L;
	}
	@Id
	@Column(name = "userid")
	private Long userId;


	@Column(name = "screenname")
	private String screenName;


	@Column(name = "username")
	private String userName;


	@Column(name = "sinceid")
	private Long sinceId;

	@Column(name = "lastsearchdate")
	private java.sql.Timestamp lastsearchdate;
	@PrePersist
	@PreUpdate
	public void preUpdate() {
		this.lastsearchdate = new Timestamp(new Date().getTime());
	}
}