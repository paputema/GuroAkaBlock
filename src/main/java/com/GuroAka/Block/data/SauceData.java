package com.GuroAka.Block.data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "sauce")
public class SauceData {

	public SauceData(String saucestr) {
		super();
		this.saucestr = saucestr;
	}
	public SauceData( ) {
		super();
	}
	@Id
	@Column
	private String saucestr;

	public String getSaucestr() {
		return saucestr;
	}

	public void setSaucestr(String saucestr) {
		this.saucestr = saucestr;
	}



}
