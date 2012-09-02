package com.idega.block.article.data.dao.impl;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.idega.block.article.data.ArticleEntity;
import com.idega.block.article.data.CategoryEntity;
import com.idega.block.article.data.dao.ArticleDaoTemplate;
import com.idega.block.article.data.dao.CategoryDao;
import com.idega.core.persistence.Param;
import com.idega.core.persistence.Query;
import com.idega.core.persistence.impl.GenericDaoImpl;
import com.idega.util.CoreConstants;
import com.idega.util.ListUtil;
import com.idega.util.StringUtil;

public abstract class ArticleDaoTemplateImpl<T extends ArticleEntity> extends GenericDaoImpl implements ArticleDaoTemplate<T> {

	@Autowired
	private CategoryDao categoryDao;

	private static Logger LOGGER = Logger.getLogger(ArticleDaoImpl.class.getName());

	
	/**
	 * @see com.idega.block.article.data.dao.ArticleDao#updateArticle(Date, String, List)
	 */
	@Override
	public boolean updateArticle(Date timestamp, String uri, Collection<String> categories) {
		return updateArticle(timestamp, uri, categories, null);
	}

	/**
     * @see com.idega.block.article.data.dao.ArticleDao#getByUri(String)
     */
	@Override
	public T getByUri(String uri){
		if (StringUtil.isEmpty(uri)) {
			LOGGER.warning("Aricle URI is not provided");
			return null;
		}

		if (uri.startsWith(CoreConstants.WEBDAV_SERVLET_URI))
			uri = uri.substring(CoreConstants.WEBDAV_SERVLET_URI.length());
		if (uri.endsWith(CoreConstants.SLASH))
			uri = uri.substring(0, uri.lastIndexOf(CoreConstants.SLASH));

		return this.getSingleResult(ArticleEntity.GET_BY_URI, getEntityClass(), new Param(ArticleEntity.uriProp, uri));
	}

	/**
     * @see com.idega.block.article.data.dao.ArticleDao#getArticleIdByURI(String)
     */
	@Override
	public Long getArticleIdByURI(String uri){
		ArticleEntity article = getByUri(uri);
		return article == null ? Long.valueOf(-1) : article.getId();
	}

	/**
     * @see com.idega.block.article.data.dao.ArticleDao#delete(String)
     */
	@Override
	@Transactional(readOnly=false)
	public boolean delete(String uri) {
		final ArticleEntity article = getByUri(uri);
		if (article == null)
			return false;

		try {
			this.remove(article);
			return true;
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, "Failed to remove article from database: " + article, e);
		}

		return false;
	}

	protected abstract Class<T> getEntityClass();
	/**
     * @see com.idega.block.article.data.dao.ArticleDao#getByCategories(List, String, int)
     */
	@Override
	public List <T> getByCategories(List<String> categories, String uriFrom, int maxResults) {
		Class<T> entityClass = getEntityClass();
		String entityName = entityClass.getSimpleName();
		StringBuilder inlineQuery = new StringBuilder("SELECT DISTINCT a").append(" FROM ").append(entityName).append(" a");
		boolean addedWhere = false;
		if(!ListUtil.isEmpty(categories)){
			inlineQuery.append(" JOIN a.").append(ArticleEntity.categoriesProp).append(" c WHERE " +
					"c.").append(CategoryEntity.categoryProp).append(" IN (:").append(ArticleEntity.categoriesProp).append(")");
			addedWhere = true;
		}

		if(!StringUtil.isEmpty(uriFrom)){
			if(addedWhere){
				inlineQuery.append(" AND ");
			}else{
				inlineQuery.append(" WHERE ");
				addedWhere = true;
			}
			inlineQuery.append("a.").append(ArticleEntity.modificationDateProp).append(" <= (SELECT art." + ArticleEntity.modificationDateProp +
					" FROM ArticleEntity art where art.").append(ArticleEntity.uriProp).append(" = :").append(ArticleEntity.uriProp).append(")");
		}

		if(addedWhere){
			inlineQuery.append(" AND ");
		}else{
			inlineQuery.append(" WHERE ");
			addedWhere = true;
		}
		inlineQuery.append("(a.class = ").append(entityName).append(")");
		inlineQuery.append(" ORDER BY a.").append(ArticleEntity.modificationDateProp).append(" DESC");

		Query query = this.getQueryInline(inlineQuery.toString());
		if(maxResults > 0){
			query.setMaxResults(maxResults);
		}

		List <T> entities = null;
		if(!ListUtil.isEmpty(categories)){
			if(StringUtil.isEmpty(uriFrom)){
				entities = query.getResultList(entityClass, new Param(ArticleEntity.categoriesProp,categories));
			}else{
				entities = query.getResultList(entityClass, new Param(ArticleEntity.categoriesProp,categories),
						new Param(ArticleEntity.uriProp, uriFrom));
			}
		}else{
			if(StringUtil.isEmpty(uriFrom)){
				entities = query.getResultList(entityClass);
			}else{
				entities = query.getResultList(entityClass, new Param(ArticleEntity.uriProp, uriFrom));
			}
		}

		return entities;
	}

	@Override
	@Transactional
	public boolean updateArticle(Date timestamp, String uri,
			Collection<String> categories, Collection<Integer> editors) {
		if (timestamp == null || StringUtil.isEmpty(uri)) {
			LOGGER.warning("Can not update article because URI (" + uri + ") or modification date (" + timestamp + ") are not provided!");
			return false;
		}

		boolean result = true;
		if (!ListUtil.isEmpty(categories)) {
		    this.categoryDao.addCategories(categories);
		}

		ArticleEntity articleEntity = this.getByUri(uri);
		if(articleEntity == null){
			articleEntity = new ArticleEntity();
		}
		List<CategoryEntity> categoriesForTheArticle = this.categoryDao.getCategories(categories);
		if(!ListUtil.isEmpty(categoriesForTheArticle)){
			articleEntity.setCategories(categoriesForTheArticle);
		}
		
		articleEntity.setUri(uri);
		articleEntity.setModificationDate(timestamp);
		if(!ListUtil.isEmpty(editors)){
			articleEntity.setEditors(new HashSet<Integer>(editors));
		}
			
		try {
				merge(articleEntity);
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, "Failed to add article to database: " + articleEntity + " with the categories: " + categoriesForTheArticle, e);
			return false;
		}
		if (result)
			return articleEntity != null && articleEntity.getId() != null;

		return result;
	}
	
	
}
