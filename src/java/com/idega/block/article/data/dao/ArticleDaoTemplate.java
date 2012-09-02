package com.idega.block.article.data.dao;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.idega.block.article.data.ArticleEntity;
import com.idega.core.persistence.GenericDao;

public interface ArticleDaoTemplate<T extends ArticleEntity> extends GenericDao {

	public static final String BEAN_NAME = "articleDAO";

	
	public abstract boolean updateArticle(Date timestamp, String uri, Collection<String> categories,Collection<Integer> editors);
	
	/**
	 * Writes creation date, path, cateogries of an article to database.
	 * @param timestamp Date type object with creation or modification date
	 * @param uri Path to article file named *.xml
	 * @param categories String type list of categories for the specified article
	 * @return true, if modification successfully completed, else false
	 */
	public abstract boolean updateArticle(Date timestamp, String uri, Collection<String> categories);

	/**
	 * Deletes creation date, path, cateogries of an article from database.
	 * @param uri Path to article file named *.xml
	 * @return true, if desired article deleted
	 */
	public abstract boolean delete(String uri);

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
	public abstract List<T> getByCategories(List<String> categories, String uriFrom, int maxResults);


	 /** Returns one ArticleEntity from database
	 * @param uri Path to article file named *.xml
	 * @return ArticleEntity having passed URI.
	 */
	public abstract T getByUri(String uri);

	/**
	 * Returns id of an ArticleEntity from database
	 * @param uri Path to article file named *.xml
	 * @return ArticleEntity having passed URI.
	 */
	public abstract Long getArticleIdByURI(String uri);

}
