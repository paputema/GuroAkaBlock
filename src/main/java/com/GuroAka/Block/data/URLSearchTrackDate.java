package com.GuroAka.Block.data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;
import lombok.NoArgsConstructor;
import twitter4j.Status;

@Data
@Entity
@Table(name = "url_searchtrack")
@NoArgsConstructor
public class URLSearchTrackDate {
	public URLSearchTrackDate(Status status, String url2) {
		// TODO 自動生成されたコンストラクター・スタブ
		setGuroakauserid(status.getUser().getId());
		setOriginalstatus(status.getId());
		setUrl(url2);
	}
	@Id
	@Column(name = "url")
	private String url;
	@Column(name = "guroakauserid")
	private Long guroakauserid;
	@Column(name = "originalstatus")
	private Long originalstatus;





}