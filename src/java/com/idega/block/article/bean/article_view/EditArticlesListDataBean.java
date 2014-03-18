package com.idega.block.article.bean.article_view;

import java.io.Serializable;

import org.directwebremoting.annotations.DataTransferObject;
import org.directwebremoting.annotations.RemoteProperty;


@DataTransferObject
public class EditArticlesListDataBean   implements Serializable {
	
	private static final long serialVersionUID = -5885081251572920949L;

	private Long id;
	
	private String title;
	
	private String editUri;
	
	private String uriToArticle;
	
	@RemoteProperty
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	
	@RemoteProperty
	public String getEditUri() {
		return editUri;
	}
	public void setEditUri(String editUri) {
		this.editUri = editUri;
	}
	
	@RemoteProperty
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	@RemoteProperty
	public String getUriToArticle() {
		return uriToArticle;
	}
	public void setUriToArticle(String uriToArticle) {
		this.uriToArticle = uriToArticle;
	}
	
}
