package com.idega.block.article.data.dao;

import java.util.Date;
import java.util.List;

import com.idega.block.article.data.ArticleEntity;
import com.idega.core.persistence.GenericDao;

/**
 * Data Access Object class for accessing "IC_ARTICLE", "JND_ARTICLE_CATEGORY" tables in class {@link ArticleDaoImpl}
 * @author martynas
 * Last changed: 2011.05.12
 * You can report about problems to: martynas@idega.com
 * AIM: lapiukshtiss
 * Skype: lapiukshtiss
 * You can expect to find some test cases notice in the end of the file.
 */
public interface ArticleDao extends GenericDao{
	
	/**
	 * Writes creation date, path, cateogries of an article to database. 
	 * @param timestamp Date type object with creation or modification date
	 * @param uri Path to article file named *.xml 
	 * @param categories String type list of categories for the specified article
	 * @return true, if modification successfully completed, else false 
	 */
	public abstract boolean updateArticle(Date timestamp, String uri, List<String> categories);
	
	/**
	 * Deletes creation date, path, cateogries of an article from database.
	 * @param uri Path to article file named *.xml
	 * @return true, if desired article deleted
	 */
	public abstract boolean deleteArticle(String uri);
	
	/**
	 * Returns one ArticleEntity from database
	 * @param uri Path to article file named *.xml 
	 * @return ArticleEntity having passed URI. 
	 */
	public abstract ArticleEntity getArticle(String uri);
	
	/**
	 * Returns id of an ArticleEntity from database
	 * @param uri Path to article file named *.xml 
	 * @return ArticleEntity having passed URI. 
	 */	
	public abstract Long getArticleIdByURI(String uri);
	
	/**
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
