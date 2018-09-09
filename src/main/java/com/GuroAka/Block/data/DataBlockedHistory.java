package com.GuroAka.Block.data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import com.GuroAka.Block.Blockresult;

import lombok.*;


@Entity
@Table(name = "blockedhistory")
@IdClass(value=DataBlockedHistoryKeyId.class)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class DataBlockedHistory {
	@Id
	@Column
	private Long userid;
	@Id
	@Column
	private Long guroakaid;
	@Column
	private Blockresult blocked;
	public DataBlockedHistory(long userid, long spamid) {
		setUserid(userid);
		setGuroakaid(spamid);
		setBlocked(Blockresult.UnBlocked);
	}

	@Override
	public String toString() {
		// TODO 自動生成されたメソッド・スタブ
		return userid + "/" + guroakaid + "/" + blocked.getProperty();
	}

	public Long getUserid() {
		return userid;
	}

	public void setUserid(Long userid) {
		this.userid = userid;
	}

	public Long getGuroakaid() {
		return guroakaid;
	}

	public void setGuroakaid(Long guroakaid) {
		this.guroakaid = guroakaid;
	}

	public Blockresult getBlocked() {
		return blocked;
	}

	public void setBlocked(Blockresult blocked) {
		this.blocked = blocked;
	}

}