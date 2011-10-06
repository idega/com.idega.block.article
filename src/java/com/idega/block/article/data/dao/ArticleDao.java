package com.idega.block.article.data.dao;

import java.util.Date;
import java.util.List;

import com.idega.block.article.data.ArticleEntity;
import com.idega.core.persistence.GenericDao;

/**
 * Class description goes here.
 * You can report about problems to: <a href="mailto:martynas@idega.com">Martynas Stakė</a>
 * You can expect to find some test cases notice in the end of the file.
 *
 *
 * @version 1.0.0 2011.08.24
 * @author martynas
 */
public interface ArticleDao extends GenericDao {

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
	 * Gets List of ArticleEntity by categories, the maximum numbers
	 * to select is specified by maxResults parameter, results are ordered
	 * by modification date
	 * @param categories List of categories, if it is empty or null, than articles will be selected independent of it's category
	 * @param firstResult uri of the article from which the results will be taken
	 * @param maxResults max amount of articles that will be returned, if less than 1 returns all articles
	 *
	 * @return the return is always List<ArticleEntity>, it is empty if no results were found
	 */
	public abstract List<ArticleEntity> getArticlesByCategoriesAndAmount(List<String> categories, String uriFrom, int maxResults);


	 /** Returns one ArticleEntity from database
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