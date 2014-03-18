package com.idega.block.article.bean.article_view;

import java.util.List;

import org.directwebremoting.annotations.DataTransferObject;
import org.directwebremoting.annotations.RemoteProperty;

@DataTransferObject
public class ArticleSearchResponce extends Responce{
	private static final long serialVersionUID = 3716301108565847284L;

	private List<EditArticlesListDataBean> articles;
	
	private List<EditArticleListPage> pages;
	
	private Integer totalCount;


	@RemoteProperty
	public List<EditArticleListPage> getPages() {
		return pages;
	}

	public void setPages(List<EditArticleListPage> pages) {
		this.pages = pages;
	}

	@RemoteProperty
	public Integer getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(Integer totalCount) {
		this.totalCount = totalCount;
	}

	@RemoteProperty
	public List<EditArticlesListDataBean> getArticles() {
		return articles;
	}

	public void setArticles(List<EditArticlesListDataBean> articles) {
		this.articles = articles;
	}
}