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

	/*
     * Tested cases:
     * Written article with name: "Name";
     * Written article with name: "SecondName" and category: "Name";
     * Added category "English good name" to article "SecondName";
     * Removed category "Name" from article "SecondName";
     * Removed category "English good name" from article "SecondName";
     * Removed "SecondName";
     * Added category "Name" to article "Name";
     * Removed category "Name" and added category "English good name" to article "Name";
     * Changed article name: "Name" to article name: "Surname".
     */
}