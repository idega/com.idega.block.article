package com.idega.block.article.data.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
import com.idega.core.persistence.impl.GenericDaoImpl;
import com.idega.util.ListUtil;

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

}
