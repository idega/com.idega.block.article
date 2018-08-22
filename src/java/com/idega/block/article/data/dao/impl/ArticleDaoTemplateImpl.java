package com.idega.block.article.data.dao.impl;

import java.util.ArrayList;
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
import com.idega.util.expression.ELUtil;

@Transactional(readOnly = false)
public abstract class ArticleDaoTemplateImpl<T extends ArticleEntity> extends GenericDaoImpl implements ArticleDaoTemplate<T> {

	@Autowired
	private CategoryDao categoryDao;

	private CategoryDao getCategoryDao() {
		if (categoryDao == null) {
			ELUtil.getInstance().autowire(this);
		}
		return categoryDao;
	}

	private static Logger LOGGER = Logger.getLogger(ArticleDaoImpl.class.getName());

	@Override
	public boolean updateArticle(Date timestamp, String uri, Collection<String> categories) {
		return updateArticle(timestamp, uri, categories, null);
	}

	/**
     * @see com.idega.block.article.data.dao.ArticleDao#getByUri(String)
     */
	@Override
	public T getByUri(String uri) {
		if (StringUtil.isEmpty(uri)) {
			LOGGER.warning("Aricle URI is not provided");
			return null;
		}

		if (uri.startsWith(CoreConstants.WEBDAV_SERVLET_URI)) {
			uri = uri.substring(CoreConstants.WEBDAV_SERVLET_URI.length());
		}
		if (uri.endsWith(CoreConstants.SLASH)) {
			uri = uri.substring(0, uri.lastIndexOf(CoreConstants.SLASH));
		}

		List<T> results = getResultList(ArticleEntity.GET_BY_URI, getEntityClass(), new Param(ArticleEntity.uriProp, uri));
		return ListUtil.isEmpty(results) ? null : results.get(0);
	}

	/**
     * @see com.idega.block.article.data.dao.ArticleDao#getArticleIdByURI(String)
     */
	@Override
	public Long getArticleIdByURI(String uri) {
		ArticleEntity article = getByUri(uri);
		return article == null ? Long.valueOf(-1) : article.getId();
	}

	/**
     * @see com.idega.block.article.data.dao.ArticleDao#delete(String)
     */
	@Override
	public boolean delete(String uri) {
		final T article = getByUri(uri);
		if (article == null) {
			return false;
		}

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
	public List<T> getByCategories(List<String> categories, String uriFrom, int maxResults) {
		Class<T> entityClass = getEntityClass();
		String entityName = entityClass.getSimpleName();
		StringBuilder inlineQuery = new StringBuilder("SELECT DISTINCT a").append(" FROM ").append(entityName).append(" a");
		boolean addedWhere = false;

		List<Param> params = new ArrayList<Param>();
		if (!ListUtil.isEmpty(categories)) {
			inlineQuery.append(" JOIN a.").append(ArticleEntity.categoriesProp).append(" c WHERE " +
					"c.").append(CategoryEntity.categoryProp).append(" IN (:").append(ArticleEntity.categoriesProp).append(")");
			addedWhere = true;
			params.add(new Param(ArticleEntity.categoriesProp, categories));
		}

		if (!StringUtil.isEmpty(uriFrom)) {
			if (addedWhere) {
				inlineQuery.append(" AND ");
			} else {
				inlineQuery.append(" WHERE ");
				addedWhere = true;
			}
			inlineQuery.append("a.").append(ArticleEntity.modificationDateProp).append(" <= (SELECT art." + ArticleEntity.modificationDateProp +
					" FROM ArticleEntity art where art.").append(ArticleEntity.uriProp).append(" = :").append(ArticleEntity.uriProp).append(")");
			params.add(new Param(ArticleEntity.uriProp, uriFrom));
		}

		if (addedWhere) {
			inlineQuery.append(" AND ");
		} else {
			inlineQuery.append(" WHERE ");
			addedWhere = true;
		}
		inlineQuery.append("(a.class = ").append(entityName).append(")");

		inlineQuery.append(" ORDER BY a.").append(ArticleEntity.modificationDateProp).append(" DESC");

		Query query = this.getQueryInline(inlineQuery.toString());
		if (maxResults > 0) {
			query.setMaxResults(maxResults);
		}

		List<T> entities = null;
		try {
			entities = ListUtil.isEmpty(params) ? query.getResultList(entityClass) : query.getResultList(entityClass, params);
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, "Error loading entities of type " + getEntityClass().getName() + " by query " + inlineQuery.toString(), e);
		}

		return entities;
	}

	@Override
	public boolean updateArticle(Date timestamp, String uri, Collection<String> categories, Collection<Integer> editors) {
		if (timestamp == null || StringUtil.isEmpty(uri)) {
			LOGGER.warning("Can not create or update article because URI (" + uri + ") and/or modification date (" + timestamp +
					") are not provided!");
			return false;
		}

		//	Checking if all categories exist in DB
		if (!ListUtil.isEmpty(categories)) {
			getCategoryDao().addCategories(categories);
		}

		//	Editing or creating
		ArticleEntity articleEntity = getByUri(uri);
		if (articleEntity == null) {
			articleEntity = new ArticleEntity();
		}

		//	Setting specific categories for the article
		List<CategoryEntity> categoriesForTheArticle = getCategoryDao().getCategories(categories);
		if (!ListUtil.isEmpty(categoriesForTheArticle)) {
			articleEntity.setCategories(categoriesForTheArticle);
		}

		//	URI
		articleEntity.setUri(uri);

		//	Timestamp
		articleEntity.setModificationDate(timestamp);

		//	Editors
		if (!ListUtil.isEmpty(editors)) {
			articleEntity.setEditors(new HashSet<Integer>(editors));
		}

		articleEntity = updateArticle(articleEntity);

		return articleEntity != null && articleEntity.getId() != null;
	}

	@Override
	public <E extends ArticleEntity> E updateArticle(E article) {
		if (article == null) {
			getLogger().warning("Entity is not provided");
			return null;
		}

		boolean editing = article.getId() != null;
		try {
			if (editing) {
				article = merge(article);
			} else {
				persist(article);
			}

			boolean failure = article == null || article.getId() == null;
			if (failure) {
				getLogger().warning("Unable to " + (editing ? "edit" : "create") + " article: " + article);
				return null;
			}

			return article;
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, "Failed to " + (editing ? "edit" : "create") + " article " + article, e);
		}

		return null;
	}

	@Override
	public void remove(T articleEntity) {
		if (articleEntity == null) {
			return;
		}

		Long id = articleEntity.getId();
		if (id == null) {
			return;
		}

		Class<T> entityClass = getEntityClass();
		String entityName = entityClass.getSimpleName();
		StringBuilder inlineQuery = new StringBuilder("SELECT a FROM ").append(entityName).append(" a WHERE id = :").append(ArticleEntity.idProp);
		Query query = this.getQueryInline(inlineQuery.toString());
		List<T> entities = query.getResultList(entityClass, new Param(ArticleEntity.idProp, id));

		if (ListUtil.isEmpty(entities)) {
			return;
		}
		for (T entity : entities) {
			super.remove(entity);
		}
	}

	@Override
	public List<CategoryEntity> getCategories(Long articleId) {
		Class<T> entityClass = getEntityClass();
		String entityName = entityClass.getSimpleName();
		StringBuilder inlineQuery =
				new StringBuilder("SELECT c FROM ").append(entityName).append(" a join a.categories c WHERE a.id = :").append(ArticleEntity.idProp);
		Query query = this.getQueryInline(inlineQuery.toString());
		List<CategoryEntity> results =  query.getResultList(CategoryEntity.class,new Param(ArticleEntity.idProp,articleId));
		return results;
	}

}
