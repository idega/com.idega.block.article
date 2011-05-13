package com.idega.block.article.data.dao;

import java.util.Date;
import java.util.List;

import com.idega.core.persistence.GenericDao;

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
}
