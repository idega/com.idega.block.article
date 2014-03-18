package com.idega.block.article.bean.article_view;

import java.io.Serializable;

import org.directwebremoting.annotations.DataTransferObject;
import org.directwebremoting.annotations.RemoteProperty;

@DataTransferObject
public class EditArticleListPage implements Serializable{
	private static final long serialVersionUID = -8865690741013010669L;
	private Integer number = null;
	private Integer start = null;
	
	@RemoteProperty
	public Integer getNumber() {
		return number;
	}
	public void setNumber(Integer number) {
		this.number = number;
	}
	
	@RemoteProperty
	public Integer getStart() {
		return start;
	}
	public void setStart(Integer start) {
		this.start = start;
	}
}
