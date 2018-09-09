package com.GuroAka.Block.Twitter;

import lombok.Data;
import twitter4j.User;
@Data
public class Result
{
	public Result(User guroAkaUser, String resulttext) {
		super();
		this.username = guroAkaUser.getName();
		this.userscreen = guroAkaUser.getScreenName();
		this.resulttext = resulttext;
	}




	private String username = "";
	private String userscreen = "";
	private String resulttext = "";
	@Override
	public String toString() {
		return username + "/@" + userscreen + "/" + resulttext;
	}
}