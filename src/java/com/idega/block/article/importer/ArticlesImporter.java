package com.idega.block.article.importer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.idega.block.article.data.dao.ArticleDao;
import com.idega.block.article.data.dao.CategoryDao;
import com.idega.content.business.categories.CategoriesEngine;
import com.idega.content.business.categories.CategoryBean;
import com.idega.content.data.ContentCategory;
import com.idega.core.business.DefaultSpringBean;
import com.idega.core.localisation.business.ICLocaleBusiness;
import com.idega.idegaweb.RepositoryStartedEvent;
import com.idega.idegaweb.UnavailableIWContext;
import com.idega.repository.bean.RepositoryItem;
import com.idega.repository.jcr.JCRItem;
import com.idega.util.CoreConstants;
import com.idega.util.ListUtil;
import com.idega.util.StringUtil;

/**
 * Imports articles and their categories to database.
 * @author martynas
 * Last changed: 2011.05.18
 * You can report about problems to: <a href="mailto:martynas@idega.com">Martynas Stakė</a>
 * You can expect to find some test cases notice in the end of the file.
 */

@Service
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class ArticlesImporter extends DefaultSpringBean implements ApplicationListener<RepositoryStartedEvent> {

	@Autowired
    private CategoriesEngine categoryEngine;

    @Autowired
    private CategoryDao categoryDao;

    @Autowired
    private ArticleDao articleDao;

    public static final String	CATEGORIES_IMPORTED_APP_PROP = "is_categories_imported",
    							ARTICLES_IMPORTED_APP_PROP = "is_articles_imported",
    							CATEGORIES_BUG_FIXED_PROP = "categories_bug_fixed";

    @Override
    public void onApplicationEvent(RepositoryStartedEvent event) {
            Boolean isCategoriesImported = getApplication().getSettings().getBoolean(CATEGORIES_IMPORTED_APP_PROP, Boolean.FALSE);

            if (!isCategoriesImported) {
                isCategoriesImported = importCategories();
                getApplication().getSettings().setProperty(CATEGORIES_IMPORTED_APP_PROP,
                        isCategoriesImported.toString());
            }

            Boolean isArticlesImported = getApplication().getSettings()
                    .getBoolean(ARTICLES_IMPORTED_APP_PROP, Boolean.FALSE);

            if (!isArticlesImported && isCategoriesImported) {
                isArticlesImported = this.importArticles();
                getApplication().getSettings().setProperty(ARTICLES_IMPORTED_APP_PROP,
                        isArticlesImported.toString());
            }
    }

    /**
     * Method for importing categories, which are in categories.xml, but not in database
     * @return true, if imported, false, if at least one category was not imported
     */
    public boolean importCategories(){
        List<Locale> localeList = ICLocaleBusiness.getListOfLocalesJAVA();
        if (localeList == null) {
            return Boolean.FALSE;
        }

        if (this.categoryEngine == null) {
            return Boolean.FALSE;
        }

        for(Locale locale : localeList){
            List<ContentCategory> categoryList = null;
            try {
                categoryList = this.categoryEngine.getCategoriesByLocale(locale.toString());
            } catch (UnavailableIWContext e){
                getLogger().log(Level.WARNING,
                        "Failed to import because categories.xml deos not exist",
                        e);
                return Boolean.FALSE;
            }

            if (ListUtil.isEmpty(categoryList)) {
                continue;
            }

            int numberOfImportedCategories = 0;
            for(ContentCategory category : categoryList){
                if (category == null || category.getId() == null)
                    continue;

                String categoryId = category.getId();
                if (StringUtil.isEmpty(categoryId)) {
                	getLogger().warning("Category " + category + " has no ID, unable to import it!");
                	continue;
                }

                Boolean isAdded = categoryDao.addCategory(categoryId) != null;
                if (isAdded) {
                	getLogger().info("Category with ID '" + categoryId + "' was imported");
                	numberOfImportedCategories++;
                } else {
                	getLogger().warning("Failed to import the category with ID '" + categoryId + "'. Number of successfully imported categories: " + numberOfImportedCategories);
                    return Boolean.FALSE;
                }
            }
        }
        return Boolean.TRUE;
    }

    /**
     * Method for importing articles and their categories from default /files/cms/article path.
     * @return true, if imported, false if at least one article was not imported.
     */
    public boolean importArticles(){
        try {
            /*Getting the articles folders*/
            RepositoryItem resource = getRepositoryService().getRepositoryItemAsRootUser(CoreConstants.CONTENT_PATH+CoreConstants.ARTICLE_CONTENT_PATH);
            return this.importArticleFolders(resource);
        } catch (RepositoryException e) {
            getLogger().log(Level.WARNING, "Failed to import articles cause of:", e);
        }
        return Boolean.FALSE;
    }

    /**
     * Browse recursively thought folders, searches for articles folders, imports articles if found some.
     * @param resource Path where to start looking for articles to import.
     * @return true, if articles found and imported, false if not all articles imported.
     */
    public boolean importArticleFolders(RepositoryItem resource) {
        if (resource == null) {
            return Boolean.FALSE;
        }

        if (!resource.exists()) {
            return Boolean.FALSE;
        }

        if (this.importArticles(resource)) {
            return Boolean.TRUE;
        } else {
            try {
                Collection<RepositoryItem> foldersAndFilesResources = resource.getChildResources();
                if (ListUtil.isEmpty(foldersAndFilesResources)) {
                    return Boolean.FALSE;
                }

                boolean result = Boolean.FALSE;
                for (RepositoryItem wr : foldersAndFilesResources) {
                    if (this.importArticleFolders(wr))
                        result = Boolean.TRUE;
                }

                /*Trying to solve out of memory exception*/
                foldersAndFilesResources = null;
                resource = null;

                return result;
            } catch (Exception e) {
                getLogger().log(Level.WARNING, "Failed to import articles from: " + resource.getPath(), e);
            }
        }

        return Boolean.FALSE;
    }

    /**
     * Checks, if it is folder containing article folders.
     * @param resource Folder, that might be containing article folders.
     * @return true, if it is folder of articles, false, if not.
     */
    public boolean isArticlesFolder(RepositoryItem resource){
        if (resource == null || !resource.exists())
            return Boolean.FALSE;

        /*Checking is it folder*/
        if (!resource.isCollection())
            return Boolean.FALSE;

        try {
            Collection<RepositoryItem> webdavResources = resource.getChildResources();
            if (ListUtil.isEmpty(webdavResources))
                return Boolean.FALSE;

            for (RepositoryItem child: webdavResources) {
                if (child.getPath().endsWith(CoreConstants.ARTICLE_FILENAME_SCOPE)) {
                    return Boolean.TRUE;
                }
            }

            /*Trying to solve out of memory exception*/
            resource = null;
            webdavResources = null;
        } catch (Exception e) {
            getLogger().log(Level.WARNING, "Failed to detect if provided resource (" + resource.getPath() + ") is a folder", e);
        }

        return Boolean.FALSE;
    }

    /**
     * Imports articles to database table "IC_ARTICLE" or updates it if exist.
     * @param resource Folder of articles folder.
     * @return true, if imported, false if failed or not articles folder.
     */
    public boolean importArticles(RepositoryItem resource){
        if (!this.isArticlesFolder(resource))
            return Boolean.FALSE;

        try {
            Collection<RepositoryItem> filesAndFolders = resource.getChildResources();
            if (ListUtil.isEmpty(filesAndFolders))
                return Boolean.FALSE;

            int size = filesAndFolders.size();
            boolean isImportSuccesful = Boolean.TRUE;
            String propertyPrefix = "DAV";
            String propertyName = "categories";
            for (RepositoryItem item: filesAndFolders) {
                if (item instanceof JCRItem && item.getName().endsWith(CoreConstants.ARTICLE_FILENAME_SCOPE)) {
                    String uri = item.getPath();

                    if (uri.contains(CoreConstants.WEBDAV_SERVLET_URI)) {
                        uri = uri.substring(CoreConstants.WEBDAV_SERVLET_URI.length());
                    }

                    if (uri.endsWith(CoreConstants.SLASH)) {
                        uri = uri.substring(0, uri.lastIndexOf(CoreConstants.SLASH));
                    }

                    JCRItem jcrItem = (JCRItem) item;
                    Collection<String> categories = null;
                    String categoriesProperty = jcrItem.getPropertyValue(propertyPrefix, propertyName, PropertyType.STRING);
                    if (!StringUtil.isEmpty(categoriesProperty)) {
                    	categories = CategoryBean.getCategoriesFromString(categoriesProperty);
                    }

                    if (!this.articleDao.updateArticle(new Date(item.getCreationDate()), uri, ListUtil.isEmpty(categories) ? null : new ArrayList<String>(categories))) {
                        isImportSuccesful = Boolean.FALSE;
                        break;
                    }
                    size = size-1;
                }
            }

            /*Trying to solve out of memory exception*/
            filesAndFolders = null;
            resource = null;
            return isImportSuccesful;
        } catch (Exception e) {
            getLogger().log(Level.WARNING, "Failed to import an article: " + resource.getPath(), e);
        }

        return Boolean.FALSE;
    }

    /*
     * Test cases isArticlesFolder(WebdavResource resource):
     * Passed articles folder, returned: true
     * Passed not articles folder, returned: false
     * Passed null, returned: false
     *
     * Test cases importArticles(WebdavResource resource):
     * Passed articles folder, without categories,
     * returned: true;
     * imported: true;
     *
     * Test cases importArticles(WebdavResource resource):
     * Passed articles folder, with category,
     * returned: true;
     * imported: true;
     *
     * Test cases importArticles(WebdavResource resource):
     * Passed articles folder, with categories,
     * returned: true;
     * imported: true;
     *
     * Test cases importArticles(WebdavResource resource):
     * Passed same as before articles folder, with categories,
     * returned: true;
     * imported: false, changed nothing, but executed;
     *
     * Passed not articles folder,
     * returned: false;
     * imported: false;
     *
     * Passed null,
     * returned: false
     * imported: false
     *
     * Test cases importArticles():
     * Passed directory /files/cms/article/ with 669 articles in different folders, ~3 articles in one folder
     * returned: true
     * imported: true
     *
     * Passed directory /files/cms/article/ with 999 articles in different folders, ~3 articles in one folder. Also different categories or no categories
     * returned: true
     * imported: true
     *
     * Test cases importCategories():
     * Passed empty directory,
     * imported: false;
     * returned: true;
     */
}