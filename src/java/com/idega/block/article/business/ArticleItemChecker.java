package com.idega.block.article.business;

import java.util.List;
import java.util.Locale;

import com.idega.block.article.bean.ArticleItemBean;
import com.idega.content.business.ContentItemChecker;
import com.idega.core.localisation.business.ICLocaleBusiness;

public class ArticleItemChecker implements ContentItemChecker {

	public boolean deleteDummyArticles(List<String> paths) {
		if (paths == null) {
			return false;
		}
		
		List<Locale> locales = ICLocaleBusiness.getListOfLocalesJAVA();
		if (locales == null) {
			return false;
		}
		
		String resourcePath = null;
		ArticleItemBean dummyArticle = null; 
		for (int i = 0; i < paths.size(); i++) {
			resourcePath = paths.get(i);
			
			dummyArticle = findDummyArticle(resourcePath, locales);
			if (dummyArticle != null) {
				dummyArticle.delete();
			}
		}
		
		return true;
	}
	
	private ArticleItemBean findDummyArticle(String resourcePath, List<Locale> locales) {
		Locale l = null;
		ArticleItemBean article = null;
		for (int i = 0; i < locales.size(); i++) {
			//	Will check all articles by locales
			l = locales.get(i);
			
			article = getLoadedArticleBean(resourcePath, l);
			
			if (article == null) {
				return null;
			}
			
			if (!article.isDummyContentItem()) {
				return null;	//	Can not delete article - at least one localized article is not dummy
			}
		}
		
		return article;	//	Can delete article
	}
	
	private ArticleItemBean getLoadedArticleBean(String resourcePath, Locale l) {
		ArticleItemBean article = new ArticleItemBean();
		
		article.setLocale(l);
		article.setResourcePath(resourcePath);
		try {
			article.load();
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
		
		return article;
	}
	
}
