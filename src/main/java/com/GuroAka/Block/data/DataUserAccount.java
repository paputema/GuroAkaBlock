package com.GuroAka.Block.data;

import java.util.Date;
import java.util.Map;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.Table;

import com.GuroAka.Block.Blockresult;

import lombok.Data;

@Entity
@Table(name = "useraccount")
@Data
public class DataUserAccount {
	public DataUserAccount() {
		super();
		lastblockdate = new Date();
		setVerify(true);
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


	@ElementCollection(fetch=FetchType.EAGER)
	@MapKeyColumn(name="guroakaid")
    @Column(name="blocked")
    @CollectionTable(
        name="blockedhistory",
        joinColumns=@JoinColumn(name="userid")
    )
	private Map<Long, Blockresult>  blockedHistory;

}
