package com.idega.block.article.data.dao.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.idega.core.search.business.SearchResult;
import com.idega.core.search.data.BasicSearchResult;
import com.idega.data.SimpleQuerier;
import com.idega.util.ListUtil;
import java.util.Iterator;



/**
 * Class for speeding up Articles searching
 * @author martynas
 * 
 */
@Repository
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class ArticleDaoImpl extends GenericDaoImpl implements ArticleDao {

	@Autowired
	private CategoryDao categoryDao;
	
	@Override
	@Transactional(readOnly=false)
	public boolean updateArticle(Date timestamp, String uri, List<String> categories) {
		// TODO Sutvarkyti tuos result
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
			persist(articleEntity);
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
			this.merge(articleEntity);
		}
		
		if(result){
			result = articleEntity != null && articleEntity.getId() != null;
		}
		
		return result;
	}
	
	/**
	 * Returns one ArticleEntity from database
	 * @param uri Path to article file named *.xml 
	 * @return ArticleEntity having passed URI. 
	 */
	private ArticleEntity getArticle(String uri){
		if(uri == null){
			return null;
		}
		List<ArticleEntity> articleEntityList = getResultList(ArticleEntity.GET_BY_URI, ArticleEntity.class, new Param(ArticleEntity.uriProp, uri));
		
		if(ListUtil.isEmpty(articleEntityList)){
			return null;
		} else {
			return articleEntityList.get(0);
		}
	}

	@Override
	@Transactional(readOnly=false)
	public boolean deleteArticle(String uri) {
		ArticleEntity articleEntity = this.getArticle(uri);
		if(articleEntity == null){
			return false;
		} else {
			this.remove(articleEntity);
			return true;
		}
	}

	@Override
	public String[] getUrisByCategoriesAndAmount(
			List<String> categories, int firstResult, int maxResults) {
		// TODO Auto-generated method stub
		
		String inlineQuery = "SELECT a.URI FROM ic_article a";;
		
		
		if(!ListUtil.isEmpty(categories)){
			String set = "";
			for (Iterator<String>iter = categories.iterator(); iter.hasNext(); ) {
				String category = iter.next();
			    set = set + "'" + category + "'";
			    if(iter.hasNext()){
			    	set = set + ", ";
			    }
			} 
			inlineQuery += ", ic_category c, jnd_article_category j WHERE c.CATEGORY IN (" 
				+ set +") AND a.id = j.ARTICLE_FK";
		}
		
		inlineQuery = inlineQuery + " ORDER BY a.MODIFICATION_DATE DESC";
		
		if(firstResult <= 0){
			firstResult = 0;
		}
		if(maxResults > 0){
			inlineQuery = inlineQuery + " LIMIT " + String.valueOf(firstResult) + ", " +  String.valueOf(maxResults);
		}
		
		String[] uris = null;
		try{
			uris = SimpleQuerier.executeStringQuery(inlineQuery);
		}
		catch(Exception e){
			e.printStackTrace();
			return null;
		}
		
		return uris;
	}
	

}