package com.idega.block.article.business;

import com.idega.block.article.component.ArticleItemViewer;
import com.idega.business.SpringBeanName;
import com.idega.util.CoreConstants;

@SpringBeanName("articleConstants")
public class ArticleConstants {

	public static final String	IW_BUNDLE_IDENTIFIER = "com.idega.block.article",
								MODULE_PREFIX = "article_",
								ARTICLE_CATEGORY_PROPERTY_NAME = "categories",
								USE_ROLES_IN_ARTICLE = "use_roles_in_article",

								EMPTY = CoreConstants.EMPTY,
								SPACE = CoreConstants.SPACE,
								SLASH = CoreConstants.SLASH;

	public static final Class<ArticleItemViewer> ARTICLE_ITEM_VIEWER = ArticleItemViewer.class;

	public static Class<ArticleItemViewer> getArticleItemViewerClass() {
		return ARTICLE_ITEM_VIEWER;
	}

}