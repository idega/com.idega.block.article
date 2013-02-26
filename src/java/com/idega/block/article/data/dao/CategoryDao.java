package com.idega.block.article.data.dao;

import java.util.Collection;
import java.util.Collections;
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

	public static final String BEAN_NAME = "categoryDAO";
	
	/**
	 * 
	 * <p>Creates new {@link CategoryEntity}, or returns existing one.</p>
	 * @param category {@link CategoryEntity#getCategory()}, 
	 * not <code>null</code>.
	 * @return {@link CategoryEntity}, existing in database or 
	 * <code>null</code> on failure.
	 * @author <a href="mailto:martynas@idega.com">Martynas Stakė</a>
	 */
	public CategoryEntity addCategory(String category);

	/**
	 * 
	 * <p>Adds {@link CategoryEntity}s, which did not exist.</p>
	 * @param categories - {@link CategoryEntity}s names to add or get from 
	 * database.
	 * @return {@link CategoryEntity}s existing in database, by given names.
	 * {@link Collections#emptyList()} on failure.
	 * @author <a href="mailto:martynas@idega.com">Martynas Stakė</a>
	 */
	public Collection<CategoryEntity> addCategories(Collection<String> categories);

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
	public abstract boolean deleteCategories(Collection<String> categories);

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
	public List<CategoryEntity> getCategories(Collection<String> categories);

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
