package com.GuroAka.Block.data;

import java.io.IOException;
import java.io.InputStream;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.DigestUtils;

import lombok.NoArgsConstructor;
import twitter4j.Status;

@Entity
@Table(name = "guroakasearchtrack")
@NoArgsConstructor
public class DateSearchTrack {
	@Id
	@Column(name = "guroimagemd5")
	private String guroImageMD5;
	@Column(name = "guroakauserid")
	private Long guroakauserid;
	@Column(name = "originalstatus")
	private Long originalstatus;
	public DateSearchTrack(Status status, String guroImageMD5) {
		super();
		this.guroakauserid = status.getUser().getId();
		this.originalstatus = status.getId();
		setGuroImageURL(guroImageMD5);
	}
	@Transient
	static public String GetMD5(String string)  {
		ResourceLoader resourceLoader = new DefaultResourceLoader();
		Resource resource = resourceLoader.getResource(string);
		String ret = new String(string);
		InputStream inputStream ;
		try {
			inputStream = resource.getInputStream();
			ret = DigestUtils.md5DigestAsHex(inputStream);
			inputStream.close();
		} catch (IOException e) {

		}finally {

			resourceLoader = null;
			resource = null;
			inputStream = null;
		}
		return ret;
	}
	@Transient
	public void setGuroImageURL(String guroImageURL) {
		this.guroImageMD5 = GetMD5(guroImageURL);
	}
	public String getGuroImageMD5() {
		return guroImageMD5;
	}
	public void setGuroImageMD5(String guroImageMD5) {
		this.guroImageMD5 = guroImageMD5;
	}
	public Long getGuroakauserid() {
		return guroakauserid;
	}
	public void setGuroakauserid(Long guroakauserid) {
		this.guroakauserid = guroakauserid;
	}
	public Long getOriginalstatus() {
		return originalstatus;
	}
	public void setOriginalstatus(Long originalstatus) {
		this.originalstatus = originalstatus;
	}
}