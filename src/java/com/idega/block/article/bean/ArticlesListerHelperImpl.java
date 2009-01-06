package com.idega.block.article.bean;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.idega.block.article.business.ArticleUtil;
import com.idega.block.web2.business.Web2Business;
import com.idega.content.business.ContentUtil;
import com.idega.util.CoreConstants;

@Scope("singleton")
@Service(ArticlesListerHelper.SPRING_BEAN_IDENTIFIER)
public class ArticlesListerHelperImpl implements ArticlesListerHelper {

	public Web2Business getWeb2() {
		return web2;
	}

	public void setWeb2(Web2Business web2) {
		this.web2 = web2;
	}

	private static final long serialVersionUID = -5926785111775072654L;

	@Autowired
	private Web2Business web2;
	
	public String getJavaScriptUris() {
		return new StringBuilder(web2.getBundleURIToJQueryLib()).append(CoreConstants.COMMA)
				.append(ArticleUtil.getBundle().getVirtualPathWithFileNameString("javascript/article-list.js")).toString();
	}

	public String getStyleSheetsUris() {
		return new StringBuilder(ContentUtil.getBundle().getVirtualPathWithFileNameString("style/content.css")).append(CoreConstants.COMMA)
				.append(ArticleUtil.getBundle().getVirtualPathWithFileNameString("style/article.css")).toString();
	}

	
}
