package com.idega.block.article.data.dao;

import com.idega.block.article.data.ArticleEntity;

/**
 * Class description goes here.
 * You can report about problems to: <a href="mailto:martynas@idega.com">Martynas Stakė</a>
 * You can expect to find some test cases notice in the end of the file.
 *
 *
 * @version 1.0.0 2011.08.24
 * @author martynas
 */
public interface ArticleDao extends ArticleDaoTemplate<ArticleEntity> {

	public static final String BEAN_NAME = "articleDAO";

}