/**
 *
 */
package com.GuroAka.Block.data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * @author paputema
 * グロ垢の可能性があるアカウントのリスト
 */

@Entity
@Table(name = "suspect")
@NoArgsConstructor
@AllArgsConstructor
public class DataSuspectList {
	@Id
	@Column
	private Long id;
	@Column
	private Long sinceid = 0L;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getSinceid() {
		return sinceid;
	}
	public void setSinceid(Long sinceid) {
		this.sinceid = sinceid;
	}
}
