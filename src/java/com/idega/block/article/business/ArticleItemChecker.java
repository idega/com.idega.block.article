package com.idega.block.article.business;

import java.util.List;
import java.util.Locale;

import com.idega.block.article.bean.ArticleItemBean;
import com.idega.content.bean.ContentItemBean;
import com.idega.content.business.ContentItemChecker;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWMainApplication;
import com.idega.presentation.IWContext;
import com.idega.util.CoreUtil;

public class ArticleItemChecker implements ContentItemChecker {

	public boolean deleteDummyArticles(List<String> paths) {
		if (paths == null) {
			return false;
		}

		IWApplicationContext iwac = IWMainApplication.getDefaultIWApplicationContext();
		if (iwac == null) {
			return false;
		}
		
		Locale l = iwac.getIWMainApplication().getDefaultLocale();
		IWContext iwc = CoreUtil.getIWContext();
		if (iwc != null) {
			l = iwc.getLocale();
		}
		if (l == null) {
			return false;
		}
		
		String path = null;
		ContentItemBean bean = new ArticleItemBean();
		for (int i = 0; i < paths.size(); i++) {
			path = paths.get(i);
			
			bean.setLocale(l);
			bean.setResourcePath(path);
			try {
				bean.load();
				if (bean.isDummyContentItem()) {
					bean.delete();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return true;
	}
	
}
