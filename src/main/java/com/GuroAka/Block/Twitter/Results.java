package com.GuroAka.Block.Twitter;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
@AllArgsConstructor
public class Results {
	@Getter
	final List<Result> ListResultBlock ;
	@Getter
	final List<Result> ListResultNotBlock;
	String ResultsText;
	public String getResultsText() {
		return ResultsText;
	}
	public void setResultsText(String resultsText) {
		ResultsText = resultsText;
	}





}