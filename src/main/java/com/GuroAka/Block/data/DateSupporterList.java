package com.GuroAka.Block.data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "supportlist")
public class DateSupporterList {
	public Long getListid() {
		return listid;
	}
	public void setUserid(Long listid) {
		this.listid = listid;
	}
	@Id
	@Column
	private Long listid;

}
