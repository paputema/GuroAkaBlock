package com.GuroAka.Block.Twitter;

import com.GuroAka.Block.Blockresult;

import lombok.Data;
import twitter4j.User;
@Data
public class Result
{
	public Result(User guroAkaUser, String resulttext,Blockresult blockedHistory) {
		super();
		this.username = guroAkaUser.getName();
		this.userscreen = guroAkaUser.getScreenName();
		this.resulttext = resulttext;
		this.blockedHistory = blockedHistory;
	}



	public Result(String targetUserScreenName, String resulttext, Blockresult blockedHistory) {
		// TODO 自動生成されたコンストラクター・スタブ
		super();
		this.userscreen = targetUserScreenName;
		this.resulttext = resulttext;
		this.blockedHistory = blockedHistory;
	}



	private Blockresult blockedHistory;
	private String username = "";
	private String userscreen = "";
	private String resulttext = "";
	@Override
	public String toString() {
		return username + "/@" + userscreen + "/" + resulttext;
	}
}