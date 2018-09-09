package com.GuroAka.Block.View;

import java.util.ArrayList;
import java.util.Collection;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.GuroAka.Block.data.AlertTagSearchResultData;
import com.GuroAka.Block.repositories.AlertTagSearchResultDataRepository;

import lombok.Data;
import twitter4j.MediaEntity;
import twitter4j.Status;


@Controller
public class GuroAkaStatus {
	@Value("${api.domain}") private String domain;
	@Autowired
	private HttpSession session;

	@Autowired
	private AlertTagSearchResultDataRepository alertTagSearchResultDataRepository;

	@RequestMapping(value="/Status/{targetId}" , method = RequestMethod.GET)
	public ModelAndView index(@PathVariable Long targetId,ModelAndView mav) {
		AlertTagSearchResultData alertTagSearchResultData =  alertTagSearchResultDataRepository.findByTargetStatusId(targetId);
		if(alertTagSearchResultData != null)
		{
			mav.addObject("TargetStatus", new StatusObject(alertTagSearchResultData.getTargetStatus()));

			Collection<StatusObject> a = new ArrayList<StatusObject>();
			for (Status status : alertTagSearchResultData.getAlertTagStatus()) {
				a.add(new StatusObject(status));
			}
			mav.addObject("AlertTagStatus", a);

			Collection<StatusObject> b = new ArrayList<StatusObject>();
			for (Status status : alertTagSearchResultData.getGuroStatus()) {
				b.add(new StatusObject(status));
			}
			mav.addObject("GuroStatus", b);


		}
		mav.setViewName("Status");
		return mav;
	}

	@Data
	private class StatusObject
	{
		public StatusObject(Status status)
		{
			userName = status.getUser().getName();
			iconUrl = status.getUser().getProfileImageURLHttps();
			statusText = status.getText();
			for(MediaEntity mediaEntity : status.getMediaEntities())
			{
				mediaUrlList.add(mediaEntity.getMediaURLHttps());
			}
			userUrl = "https://twitter.com/intent/user?user_id=" + status.getUser().getId();
			statusUrl = "https://twitter.com/GuroAkaBlock/status/" + status.getId();
			screenName = status.getUser().getScreenName();
			hasImage = !mediaUrlList.isEmpty();
		}
		private String userName;
		private String screenName;
		private String iconUrl;
		private String statusText;
		private ArrayList<String> mediaUrlList = new ArrayList<String>();
		private String userUrl;
		private String statusUrl;
		private Boolean hasImage;
	}
}
