package com.GuroAka.Block.View;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.GuroAka.Block.repositories.GuroAkaSearchResultDataRepository;


@Controller
public class GuroAkaSearch {
	@Value("${api.domain}") private String domain;
	@Autowired
	private HttpSession session;

	@Autowired
	private GuroAkaSearchResultDataRepository guroAkaSearchResultDataRepository;

	private void addObjectBySession(ModelAndView mav,String attName,Object defobject)
	{
		Object object = session.getAttribute(attName);
		mav.addObject(attName,(object != null) ? object : defobject);
		session.removeAttribute(attName);
	}

	@RequestMapping("/Search")
	public ModelAndView index(ModelAndView mav) {

		addObjectBySession(mav,"ret","検索にヒットしたアカウントの一覧です");
		addObjectBySession(mav,"Resultlist",guroAkaSearchResultDataRepository.findAll());
		String sDomain = domain + "/Search";
		addObjectBySession(mav,"sdomain",sDomain);
		addObjectBySession(mav,"domain",domain);
		mav.setViewName("Search");

		return mav;
	}
}
