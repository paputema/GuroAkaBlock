package com.GuroAka.Block.data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
@Entity
@Table(name = "list")
public class DataGuroAccountList {
	public Long getListid() {
		return listid;
	}
	public void setUserid(Long listid) {
		this.listid = listid;
	}
	@Id
	@Column
	private Long listid;
	public void setListid(Long listid) {
		this.listid = listid;
	}

}
