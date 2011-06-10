package com.idega.block.article.data.dao.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.idega.block.article.data.ArticleEntity;
import com.idega.block.article.data.CategoryEntity;
import com.idega.block.article.data.dao.ArticleDao;
import com.idega.block.article.data.dao.CategoryDao;
import com.idega.core.persistence.Param;
import com.idega.core.persistence.Query;
import com.idega.core.persistence.impl.GenericDaoImpl;
import com.idega.util.CoreConstants;
import com.idega.util.ListUtil;
import com.idega.util.StringUtil;


/**
 * Class for speeding up Articles searching
 * @author martynas
 * Last changed: 2011.05.12
 * You can report about problems to: martynas@idega.com
 * AIM: lapiukshtiss
 * Skype: lapiukshtiss
 * You can expect to find some test cases notice in the end of the file.
 */
@Repository
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class ArticleDaoImpl extends GenericDaoImpl implements ArticleDao {

	@Autowired
	private CategoryDao categoryDao;

	private static Logger LOGGER = Logger.getLogger(ArticleDaoImpl.class.getName());

	@Override
	@Transactional(readOnly=false)
	public boolean updateArticle(Date timestamp, String uri, Collection<String> categories) {
		boolean result = true;

		if(!ListUtil.isEmpty(categories)){
			this.categoryDao.addCategories(categories);
		}

		ArticleEntity articleEntity = this.getArticle(uri);
		if(articleEntity == null){
			articleEntity = new ArticleEntity();
			articleEntity.setUri(uri);
			articleEntity.setModificationDate(timestamp);
			articleEntity.setCategories(this.categoryDao.getCategories(categories));

			try {
				persist(articleEntity);
			} catch (Exception e) {
				LOGGER.log(Level.WARNING, "Failed to add article to database: " + articleEntity, e);
				return false;
			}

		} else {
			List<CategoryEntity> categoryEntitiesInArticle = articleEntity.getCategories();
			List<CategoryEntity> categoryEntitiesToRemove = new ArrayList<CategoryEntity>(0);
			/*Deleting all used categories from temporary lists*/
			if((!ListUtil.isEmpty(categoryEntitiesInArticle))&&(!ListUtil.isEmpty(categories))){
				for(CategoryEntity o : categoryEntitiesInArticle){
					if(categories.contains(o.getCategory())){
						categories.remove(o.getCategory());
					} else {
						categoryEntitiesToRemove.add(o);
					}
				}

				/*Performing deletion of unused categories*/
				result = articleEntity.removeCategories(categoryEntitiesToRemove);
				/*Performing addition of new a categories*/
				result = articleEntity.addCategories(this.categoryDao.getCategories(categories));
			} else if((!ListUtil.isEmpty(categoryEntitiesInArticle))&&(ListUtil.isEmpty(categories))){
				result = articleEntity.removeCategories(categoryEntitiesInArticle);
			} else if((ListUtil.isEmpty(categoryEntitiesInArticle))&&(!ListUtil.isEmpty(categories))){
				result = articleEntity.addCategories(this.categoryDao.getCategories(categories));
			}

			try {
				this.merge(articleEntity);
			} catch (Exception e) {
				LOGGER.log(Level.WARNING, "Failed to update article to database: " + articleEntity, e);
				return false;
			}
		}

		if(result){
			return articleEntity != null && articleEntity.getId() != null;
		}

		return result;
	}

	@Override
	public ArticleEntity getArticle(String uri){
		if(StringUtil.isEmpty(uri)){
			return null;
		}

		if (uri.contains(CoreConstants.WEBDAV_SERVLET_URI)) {
			uri = uri.substring(CoreConstants.WEBDAV_SERVLET_URI.length());
		}

		if (uri.endsWith(CoreConstants.SLASH)) {
			uri = uri.substring(0, uri.lastIndexOf(CoreConstants.SLASH));
		}

		return this.getSingleResult(ArticleEntity.GET_BY_URI, ArticleEntity.class, new Param(ArticleEntity.uriProp, uri));
	}

	@Override
	public Long getArticleIdByURI(String uri){
		if (StringUtil.isEmpty(uri)) {
			return new Long(-1);
		}

		if (uri.contains(CoreConstants.WEBDAV_SERVLET_URI)) {
			uri = uri.substring(CoreConstants.WEBDAV_SERVLET_URI.length());
		}

		if (uri.endsWith(CoreConstants.SLASH)) {
			uri = uri.substring(0, uri.lastIndexOf(CoreConstants.SLASH));
		}

		return this.getSingleResult(ArticleEntity.GET_ID_BY_URI, Long.class, new Param(ArticleEntity.uriProp, uri));
	}

	@Override
	@Transactional(readOnly=false)
	public boolean deleteArticle(String uri) {
		final ArticleEntity article = getArticle(uri);
		if (article == null)
			return false;
		try{
			this.remove(article);
			return true;
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, "Failed to remove article from database: " + article, e);
		}

		return false;
	}

	@Override
	public List <String> getUrisByCategoriesAndAmount(List<String> categories, String uriFrom, int maxResults) {
		StringBuilder inlineQuery = new StringBuilder("SELECT DISTINCT a.").append(ArticleEntity.uriProp).append(" FROM ArticleEntity a");
		if(!ListUtil.isEmpty(categories)){
			inlineQuery.append(" JOIN a.").append(ArticleEntity.categoriesProp).append(" c WHERE " +
					"c.").append(CategoryEntity.categoryProp).append(" IN (:").append(ArticleEntity.categoriesProp).append(")");
		}

		if(!StringUtil.isEmpty(uriFrom)){
			inlineQuery.append(ListUtil.isEmpty(categories) ? " WHERE " : " AND ");
			inlineQuery.append("a.").append(ArticleEntity.modificationDateProp).append(" <= (SELECT art." + ArticleEntity.modificationDateProp +
					" FROM ArticleEntity art where art.").append(ArticleEntity.uriProp).append(" = :").append(ArticleEntity.uriProp).append(")");
		}

		inlineQuery.append(" ORDER BY a.").append(ArticleEntity.modificationDateProp);

		Query query = this.getQueryInline(inlineQuery.toString());
		if(maxResults > 0){
			query.setMaxResults(maxResults);
		}

		List <String> uris = null;
		if(!ListUtil.isEmpty(categories)){
			if(StringUtil.isEmpty(uriFrom)){
				uris = query.getResultList(String.class, new Param(ArticleEntity.categoriesProp,categories));
			}else{
				uris = query.getResultList(String.class, new Param(ArticleEntity.categoriesProp,categories),
						new Param(ArticleEntity.uriProp, uriFrom));
			}
		}else{
			if(StringUtil.isEmpty(uriFrom)){
				uris = query.getResultList(String.class);
			}else{
				uris = query.getResultList(String.class, new Param(ArticleEntity.uriProp, uriFrom));
			}
		}

		return uris;
	}

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
