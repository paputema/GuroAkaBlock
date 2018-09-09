package com.GuroAka.Block.data;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import twitter4j.Status;

@Entity
@Table(name = "alert_tag_search_result")
@AllArgsConstructor
@Data
public class AlertTagSearchResultData
{
	public AlertTagSearchResultData(long TargetStatusId, Status TargetStatus ) {
		this.targetStatus = TargetStatus;
		this.targetStatusId = TargetStatusId;
		this.guroStatusMap=(new HashMap<Long, Status>());
		this.alertTagStatusMap=(new HashMap<Long, Status>());
		this.guroStatusMapCount = guroStatusMap.size();
		this.alertTagStatusMapCount = alertTagStatusMap.size();
	}
	public AlertTagSearchResultData() {
		this.guroStatusMap=(new HashMap<Long, Status>());
		this.alertTagStatusMap=(new HashMap<Long, Status>());
		this.guroStatusMapCount = guroStatusMap.size();
		this.alertTagStatusMapCount = alertTagStatusMap.size();
	}



	//被害ツイート
	@Id
	@Column(name="target_status_id")
	private Long targetStatusId;
	@Column(name="target_status")
	private Status targetStatus;

	//グロ垢候補ツイート
	//@Column(name="guro_status_map")
	@ElementCollection(fetch=FetchType.EAGER)
    @CollectionTable(
        name="alert_tag_search_result_guro_status_map",
        joinColumns=@JoinColumn(name="target_status_id")
    )
	private Map<Long, Status> guroStatusMap;


	@Column(name="guro_status_map_count")
	private Integer guroStatusMapCount;


	//注意喚起タグツイート
	//@Column(name="alert_tag_status_map")
	@ElementCollection(fetch=FetchType.EAGER)
    @CollectionTable(
        name="alert_tag_search_result_alert_tag_status_map",
        joinColumns=@JoinColumn(name="target_status_id")
    )
	private Map<Long, Status>  alertTagStatusMap;
	@Column(name="alert_tag_status_map_count")
	private Integer alertTagStatusMapCount;

	@PreUpdate
	@PrePersist
	void prePersist ()
	{
		guroStatusMapCount = guroStatusMap.size();
		alertTagStatusMapCount = alertTagStatusMap.size();
	}

	public boolean hasNewGuroStatus()
	{
		Boolean ret;
		if(guroStatusMapCount.intValue() < guroStatusMap.size() && 0 < guroStatusMap.size())
		{
			ret = Boolean.TRUE;
		}else
		{
			ret = Boolean.FALSE;
		}
		prePersist ();
		return ret;
	}
	public void setGuroStatus(Status Status)
	{
		guroStatusMap.put( Status.getId(), Status);
	}
	public void setAlertTagStatus(Status Status)
	{
		alertTagStatusMap.put( Status.getId(), Status);
	}
	public Collection<Status> getGuroStatus()
	{
		return guroStatusMap.values();
	}
	public Collection<Status> getAlertTagStatus()
	{
		return alertTagStatusMap.values();
	}
}
