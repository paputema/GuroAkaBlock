package com.GuroAka.Block.csv;

import com.github.mygreen.supercsv.annotation.CsvBean;
import com.github.mygreen.supercsv.annotation.CsvColumn;

@CsvBean(header=false)
public class GuroAkaCsv {
	public GuroAkaCsv() {
		super();
		// TODO 自動生成されたコンストラクター・スタブ
	}

	@CsvColumn(number=1)
	private Long UserId;

	public Long getUserId() {
		return UserId;
	}

	public void setUserId(Long userId) {
		UserId = userId;
	}
}
