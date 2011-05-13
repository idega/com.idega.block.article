package com.idega.block.article.data.dao;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.idega.block.article.data.ArticleEntity;
import com.idega.core.persistence.GenericDao;
import com.idega.core.search.business.SearchResult;
import com.idega.presentation.IWContext;

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
	 * Gets array of uris by categories, the maximum numbers
	 * to select is specified by amount parameter
	 * @param categories List of categories, if it is empty or null, than articles will be selected independent of it's category
	 * @param firstResult the result from which other will be selected (including this), if less than zero then zero will be taken
	 * @param maxResults max amount of articles that will be returned, if less than 1 returns all articles 
	 */
	public abstract String[] getUrisByCategoriesAndAmount(List<String> categories, int firstResult, int maxResults);
}
