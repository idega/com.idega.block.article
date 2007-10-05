package com.idega.block.article.business;

import com.idega.block.article.component.ArticleItemViewer;
import com.idega.business.SpringBeanName;
import com.idega.util.CoreConstants;

@SpringBeanName("articleConstants")
public class ArticleConstants {
	
	public static final String IW_BUNDLE_IDENTIFIER = "com.idega.block.article";
	public static final String MODULE_PREFIX = "article_";
	public static final String ARTICLE_CATEGORY_PROPERTY_NAME = "categories";
	
	public static final String EMPTY = CoreConstants.EMPTY;
	public static final String SPACE = CoreConstants.SPACE;
	public static final String SLASH = CoreConstants.SLASH;
	public static final Class<ArticleItemViewer> ARTICLE_ITEM_VIEWER = ArticleItemViewer.class;

	public static Class<ArticleItemViewer> getArticleItemViewerClass() {
		return ARTICLE_ITEM_VIEWER;
	}
	
}
