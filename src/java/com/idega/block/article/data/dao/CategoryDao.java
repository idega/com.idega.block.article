package com.idega.block.article.data.dao;

import java.util.List;

import com.idega.block.article.data.CategoryEntity;
import com.idega.block.article.data.dao.impl.CategoryDaoImpl;
import com.idega.core.persistence.GenericDao;

/**
 * Data Access Object class for accessing "ART_CATEGORY" table in class {@link CategoryDaoImpl}
 * @author martynas
 * Last changed: 2011.05.12
 * You can report about problems to: martynas@idega.com
 * AIM: lapiukshtiss
 * Skype: lapiukshtiss
 * You can expect to find some test cases notice in the end of the file.
 */
public interface CategoryDao extends GenericDao {

	/**
	 * Creates new Category
	 * @param category New category id
	 * @return true, if modification successfully completed, else false 
	 */
	public abstract boolean addCategory(String category);
	
	/**
	 * Adds new categories, returns not added or existing ones
	 * @param categories New categories IDs
	 * @return null, if empty list passed, empty list, if all categories added, list of missing categories, if some categories not added
	 */
	public abstract List<String> addCategories(List<String> categories);
	
	/**
	 * Deletes category
	 * @param category Category name
	 * @return true, if category deleted
	 */
	public abstract boolean deleteCategory(String category);
	
	/**
	 * Deletes categories
	 * @param categories Category name
	 * @return true, if all categories deleted
	 */
	public abstract boolean deleteCategories(List<String> categories);
	
	/**
	 * Returns all categories from database table "IC_CATEGORY"
	 * @return Returns list of CategoryEntity objects
	 */
	public abstract List<CategoryEntity> getCategories();
	
	/**
	 * Returns categories form database table "IC_CATEGORY", which matches given list of categories
	 * @param categories String type list of one article category names.
	 * @return Returns list of CategoryEntity objects, null if empty list is passed
	 */
	public List<CategoryEntity> getCategories(List<String> categories);
	
	/**
	 * Returns category form database table "IC_CATEGORY", which matches given string
	 * @param category Category name
	 * @return CategoryEntity object or null, if empty string is passed or such category does not exist
	 */
	public CategoryEntity getCategory(String category);

	/**
	 * Checks weather there is such category in database;
	 * @param category Category name
	 * @return true, if such category exists
	 */
	public abstract boolean isCategoryExists(String category);

	/**
	 * Checks, if there is such categories in database, if not, returns list of missing categories
	 * @param categories Category names to compare
	 * @return Categories which does not exist in database, null, if empty list is passed, empty list, if all categories exist
	 */
	public abstract List<String> getNotExistingCategoriesFromThisList(List<String> categories);
	
	/*
	 * Tested cases:
	 * Created category with name: "Name";
	 * Created category with name: "English fine name";
	 * Modified category "Name" to category "Surname";
	 * Modified category "English fine name" to "Spanish good surname";
	 * Deleted category "Surname";
	 * Deleted category "Spanish good surname";
	 * Deleted category "SomeCategory" while not removed from article "New article"
	 */
}
