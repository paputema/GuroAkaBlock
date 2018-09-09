package com.GuroAka.Block;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Blockresult
{
	Success ("成功"),
	Failure ("失敗"),
	UnBlocked ("未ブロック"),
	Blocked ("ブロック済み"),
	FF("フォロー中"),
	InWhiteListBlocked("ホワイトリスト入ブロック済み"),
	InWhiteListUnBlocked("ホワイトリスト入未ブロック");
	
	private final String property;
}