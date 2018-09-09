package com.GuroAka.Block.data;



import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import java.util.Date;



@Entity
@Table(name = "blocklog")
public class DataBlockLog {

	@Id
	@Column
	private Long userid;
	@Column(name = "lastblockdate")
	private Date LastBlockDate;


	public DataBlockLog() {
		super();
		// TODO 自動生成されたコンストラクター・スタブ
	}
	public DataBlockLog(Long userid) {
		super();
		this.userid = userid;
		this.LastBlockDate = new Date();
	}

	public Date getLastBlockDate() {
		return LastBlockDate;
	}
	public Long getUserid() {
		return userid;
	}
	public void setLastBlockDate(Date lastBlockDate) {
		LastBlockDate = lastBlockDate;
	}
	public void setUserid(Long userid) {
		this.userid = userid;
	}

}
