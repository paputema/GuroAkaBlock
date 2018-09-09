package com.GuroAka.Block;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.GuroAka.Block.Twitter.GuroAkaTwitter;
import com.GuroAka.Block.data.DataSearchResult;
import com.GuroAka.Block.data.DataWhiteListAccount;
import com.GuroAka.Block.data.DateSearchTrack;
import com.GuroAka.Block.repositories.GuroAkaSearchResultDataRepository;
import com.GuroAka.Block.repositories.GuroAkaSearchTrackDataRepository;
import com.GuroAka.Block.repositories.UserAccountDataRepository;
import com.GuroAka.Block.repositories.WhiteListAccountDataRepository;

import twitter4j.User;

@Service
@EnableScheduling
public class WhiteListUpdateScheduler {
	@Autowired
	private UserAccountDataRepository userAccountDataRepository;

	@Autowired
	private GuroAkaTwitter guroAkaTwitter;

	@Autowired
	private WhiteListAccountDataRepository whiteListAccountDataRepository;
	@Autowired
	private GuroAkaSearchResultDataRepository guroAkaSearchResultDataRepository;
	@Autowired
	private GuroAkaSearchTrackDataRepository guroAkaSearchTrackDataRepository;
	private static final Log LOG = LogFactory.getLog(WhiteListUpdateScheduler.class);


	@Scheduled(initialDelay = 0 * 60 * 1000, fixedDelay = 30 * 60 * 1000)
	public void WhiteListUpdate() {
		LOG.debug("ホワイトリスト更新開始");
		try {
			Map<Long, User> map = guroAkaTwitter.GetGuroAkaByList(981127195153215489L);
			for (User user : map.values()) {
				whiteListAccountDataRepository.save(new DataWhiteListAccount(user));
				List<DataSearchResult> dsr = guroAkaSearchResultDataRepository.findAllByUserid(user.getId());
				for (DataSearchResult dataSearchResult : dsr) {
					guroAkaSearchResultDataRepository.delete(dataSearchResult);
				}
				for (DateSearchTrack iterable_element : guroAkaSearchTrackDataRepository.findAllByGuroakauserid( user.getId())) {
					guroAkaSearchTrackDataRepository.delete(iterable_element);
				}
			}
		}finally{
		}
		whiteListAccountDataRepository.flush();
		userAccountDataRepository.flush();
		guroAkaTwitter.getGuroAkaUsers();
		LOG.debug("ホワイトリスト更新完了");
		//System.gc();
	}

}
