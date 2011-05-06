package com.idega.block.article.data.dao.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.idega.block.article.data.CategoryEntity;
import com.idega.block.article.data.dao.CategoryDao;
import com.idega.content.business.categories.event.CategoryAddedEvent;
import com.idega.content.business.categories.event.CategoryDeletedEvent;
import com.idega.core.persistence.Param;
import com.idega.core.persistence.impl.GenericDaoImpl;
import com.idega.util.ListUtil;
import com.idega.util.StringUtil;

/**
 * 
 * @author martynas
 *
 */
// TODO: Optimize logic
@Repository
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class CategoryDaoImpl extends GenericDaoImpl implements CategoryDao, ApplicationListener {
	
	@Override
	@Transactional(readOnly = false)
	public boolean addCategory(String category) {
		if (StringUtil.isEmpty(category)) {
			return false;
		}
		
		if (!this.isCategoryExists(category)) {
			CategoryEntity categoryEntity = new CategoryEntity();
			categoryEntity.setCategory(category);
			this.persist(categoryEntity);
			return categoryEntity.getId() != null;
		}
		return false;
	}

	@Override
	@Transactional(readOnly = false)
	public List<String> addCategories(List<String> categories) {
		if (ListUtil.isEmpty(categories))
			return null;

		List<String> categoriesNotAdded = new ArrayList<String>();
		for (String s : categories) {
			if (!this.addCategory(s)) {
				categoriesNotAdded.add(s);
			}
		}
		return categoriesNotAdded;
	}

	@Override
	@Transactional(readOnly = false)
	public boolean deleteCategory(String category) {
		if (StringUtil.isEmpty(category)) {
			return false;
		}
		
		if (ListUtil.isEmpty(this.deleteCategories(Arrays.asList(category)))) {
			return true;
		}
		
		return false;
	}

	@Override
	@Transactional(readOnly = false)
	public List<String> deleteCategories(List<String> categories) {
		List<String> categoriesNotDeleted = new ArrayList<String>();
		if (ListUtil.isEmpty(categories))
			return null;

		List<CategoryEntity> categoryEntitiesToDelete = this.getCategories(categories);
		
		for (CategoryEntity s : categoryEntitiesToDelete) {
			if (!categories.contains(s.getCategory())) {
				categoriesNotDeleted.add(s.getCategory());
			}
			
			this.remove(s);
		}
		return categoriesNotDeleted;
	}

	@Override
	public List<CategoryEntity> getCategories() {
		return this.getResultList(CategoryEntity.GET_ALL, CategoryEntity.class);
	}

	@Override
	public List<CategoryEntity> getCategories(List<String> categories) {
		if (ListUtil.isEmpty(categories))
			return null;

		return getResultList(CategoryEntity.GET_BY_NAMES, CategoryEntity.class,
				new Param(CategoryEntity.categoryProp, categories));
	}

	@Override
	public CategoryEntity getCategory(String category) {
		CategoryEntity categoryEntity = null;
		categoryEntity = this.getSingleResult(CategoryEntity.GET_BY_NAME,
				CategoryEntity.class, new Param(CategoryEntity.categoryProp,
						category));
		
		return categoryEntity;
	}

	@Override
	public boolean isCategoryExists(String category) {
		if (StringUtil.isEmpty(category))
			return false;

		List<String> categoryEntities = this.getNotExistingCategoriesFromThisList(Arrays.asList(category));

		if (ListUtil.isEmpty(categoryEntities))
			return true;
		 
		return false;
	}

	@Override
	public List<String> getNotExistingCategoriesFromThisList(List<String> categories) {
		if (ListUtil.isEmpty(categories))
			return null;
		
		List<String> nonExistingCategories = new ArrayList<String>(categories);
		List<CategoryEntity> categoryEntities = this.getCategories();
		for (CategoryEntity s : categoryEntities) {
			if (categories.contains(s.getCategory())) {
				nonExistingCategories.remove(s.getCategory());
			}
		}
		
		return nonExistingCategories;
	}

	@Override
	@Transactional(readOnly = false)
	public void onApplicationEvent(ApplicationEvent event) {

		if (event instanceof CategoryDeletedEvent) {
			System.out.println("Ar kategorija ištrinta?:" + this.deleteCategory(((CategoryDeletedEvent) event).getCategoryId()));
		}
		
		if (event instanceof CategoryAddedEvent) {
			System.out.println("Ar kategorija įdėta?:" + this.addCategory(((CategoryAddedEvent) event).getCategoryId()));
		}
	}

}
