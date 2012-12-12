package com.idega.block.article.importer;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.httpclient.HttpException;
import org.apache.webdav.lib.PropertyName;
import org.apache.webdav.lib.WebdavResource;
import org.apache.webdav.lib.WebdavResources;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.idega.block.article.data.CategoryBugRemover;
import com.idega.block.article.data.dao.ArticleDao;
import com.idega.block.article.data.dao.CategoryDao;
import com.idega.content.business.categories.CategoriesEngine;
import com.idega.content.business.categories.CategoryBean;
import com.idega.content.data.ContentCategory;
import com.idega.core.business.DefaultSpringBean;
import com.idega.core.localisation.business.ICLocaleBusiness;
import com.idega.idegaweb.IWMainApplicationSettings;
import com.idega.idegaweb.IWMainSlideStartedEvent;
import com.idega.idegaweb.UnavailableIWContext;
import com.idega.slide.business.IWSlideService;
import com.idega.util.ArrayUtil;
import com.idega.util.CoreConstants;
import com.idega.util.ListUtil;
import com.idega.util.StringUtil;
import com.idega.util.expression.ELUtil;


/**
 * Imports articles and their categories to database.
 * @author martynas
 * Last changed: 2011.05.18
 * You can report about problems to: <a href="mailto:martynas@idega.com">Martynas Stakė</a>
 * You can expect to find some test cases notice in the end of the file.
 */

@Service
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class ArticlesImporter extends DefaultSpringBean implements ApplicationListener {

    @Autowired
    private CategoriesEngine categoryEngine;

    @Autowired
    private CategoryDao categoryDao;

    private static Logger LOGGER = Logger.getLogger(ArticlesImporter.class.getName());

    public static final String CATEGORIES_IMPORTED_APP_PROP = "is_categories_imported",
    							ARTICLES_IMPORTED_APP_PROP = "articles_imported",
    							CATEGORIES_BUG_FIXED_PROP = "is_categories_bug_fixed";

    /**
     * @see org.springframework.context.ApplicationListener#onApplicationEvent(org.springframework.context.ApplicationEvent)
     */
    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof IWMainSlideStartedEvent) {
        	IWMainApplicationSettings settings = getApplication().getSettings();

            CategoryBugRemover cbr = new CategoryBugRemover();
            try {
                if (!cbr.isBadColunmsExist())
                	settings.setProperty(CATEGORIES_BUG_FIXED_PROP, Boolean.TRUE.toString());
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to check for wrong tables", e);
            }

            if (!settings.getBoolean(CATEGORIES_BUG_FIXED_PROP)) {
                cbr.removeBug();

                settings.setProperty(CATEGORIES_IMPORTED_APP_PROP, Boolean.FALSE.toString());
                settings.setProperty(ARTICLES_IMPORTED_APP_PROP, Boolean.FALSE.toString());

                getLogger().info("Articles importer bug is fixed, terminating the import - need to restart the server");
                return;
            }

            Boolean isCategoriesImported = settings.getBoolean(CATEGORIES_IMPORTED_APP_PROP, Boolean.FALSE);
            if (!isCategoriesImported) {
            	getLogger().info("Importing the categories...");
                isCategoriesImported = importCategories();
                getLogger().info("Finished importing the categories. The result is successfull: " + isCategoriesImported);
                settings.setProperty(CATEGORIES_IMPORTED_APP_PROP, isCategoriesImported.toString());
            }

            Boolean isArticlesImported = settings.getBoolean(ARTICLES_IMPORTED_APP_PROP, Boolean.FALSE);
            if (!isArticlesImported && isCategoriesImported) {
            	getLogger().info("Importing the articles...");
                isArticlesImported = importArticles();
                getLogger().info("Finished importing articles. The result is successfull: " + isArticlesImported);
                settings.setProperty(ARTICLES_IMPORTED_APP_PROP, isArticlesImported.toString());
            }

            getLogger().info("Articles importer finished");
        }
    }

    /**
     * Method for importing categories, which are in categories.xml, but not in database
     * @return true, if imported, false, if at least one category was not imported
     */
    public boolean importCategories() {
        List<Locale> localeList = ICLocaleBusiness.getListOfLocalesJAVA();
        if (localeList == null) {
        	getLogger().warning("There are no locales in the system");
            return Boolean.FALSE;
        }

        if (categoryEngine == null) {
        	getLogger().warning("Categories engine is not initialized");
            return Boolean.FALSE;
        }

        int numberOfImportedCategories = 0;
        for (Locale locale : localeList) {
            List<ContentCategory> categoryList = null;
            try {
                categoryList = categoryEngine.getCategoriesByLocale(locale.toString());
            } catch (UnavailableIWContext e){
                LOGGER.log(Level.WARNING,  "Failed to import because categories.xml does not exist", e);
                return Boolean.FALSE;
            }

            if (ListUtil.isEmpty(categoryList))
                continue;

            for (ContentCategory category : categoryList) {
                if (category == null)
                    continue;

                String categoryId = category.getId();
                if (StringUtil.isEmpty(categoryId)) {
                	getLogger().warning("Category " + category + " has no ID, unable to import it!");
                	continue;
                }

                Boolean isAdded = categoryDao.addCategory(categoryId);
                if (isAdded) {
                	getLogger().info("Category with ID '" + categoryId + "' was imported");
                	numberOfImportedCategories++;
                } else {
                	getLogger().warning("Failed to import the category with ID '" + categoryId + "'. Number of successfully imported categories: " + numberOfImportedCategories);
                    return Boolean.FALSE;
                }
            }
        }

        getLogger().info("Number of imported categories: " + numberOfImportedCategories);
        return Boolean.TRUE;
    }

    /**
     * Method for importing articles and their categories from default /files/cms/article path.
     * @return true, if imported, false if at least one article was not imported.
     */
    private boolean importArticles() {
        IWSlideService iWSlideService = getServiceInstance(IWSlideService.class);
        try {
            /*Getting the articles folders*/
            WebdavResource resource = iWSlideService.getWebdavResourceAuthenticatedAsRoot(CoreConstants.CONTENT_PATH
            		.concat(CoreConstants.ARTICLE_CONTENT_PATH));
            boolean importResult = importArticleFolders(resource);
            resource.close();
            return importResult;
        } catch (HttpException e) {
            LOGGER.log(Level.WARNING, "Failed to import articles cause of:", e);
        } catch (RemoteException e) {
            LOGGER.log(Level.WARNING, "Failed to import articles cause of:", e);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "No such folder or you do not have a permission to access it: ", e);
        }
        return Boolean.FALSE;
    }

    /**
     * Browse recursively thought folders, searches for articles folders, imports articles if found some.
     * @param resource Path where to start looking for articles to import.
     * @return true, if articles found and imported, false if not all articles imported.
     */
    private boolean importArticleFolders(WebdavResource resource) {
        if (resource == null) {
        	getLogger().warning("Resource is undefined!");
            return Boolean.FALSE;
        }

        if (!resource.exists()) {
        	getLogger().warning("Resource " + resource.getPath() + " does not exist!");
            return Boolean.FALSE;
        }

        getLogger().info("Will try to import article(s) from " + resource.getPath() + " and it's folder(s)");

        //	Importing articles from the current folder
        importArticles(resource);

        //	Now will try to look for the articles in the current folder
        try {
        	WebdavResource[] foldersAndFilesResources = resource.listWebdavResources();
            if (ArrayUtil.isEmpty(foldersAndFilesResources)) {
            	getLogger().info("There are no files nor folders inside " + resource.getPath());
                return Boolean.TRUE;
            }

            boolean result = Boolean.FALSE;
            for (WebdavResource wr: foldersAndFilesResources) {
            	if (wr == null)
            		continue;

            	if (importArticleFolders(wr)) {
            		result = Boolean.TRUE;
            		getLogger().info("SUCCESSFULLY imported article(s) from the folder: " + wr.getPath());
            	}

            	if (wr != null)
            		wr.close();
            }

            /*Trying to solve out of memory exception*/
            foldersAndFilesResources = null;
            resource = null;

            return result;
        } catch (HttpException e) {
        	LOGGER.log(Level.WARNING, "Http:Exception: ",e);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "No such folder or you do not have a permission to access it: ", e);
        }

        return Boolean.FALSE;
    }

    /**
     * Checks, if it is folder containing article folders.
     * @param resource Folder, that might be containing article folders.
     * @return true, if it is folder of articles, false, if not.
     */
    private boolean isArticlesFolder(WebdavResource resource){
        if (resource == null || !resource.exists()) {
        	getLogger().warning("Resource " + resource + " does not exist!");
        	return Boolean.FALSE;
        }

        /*Checking is it folder*/
        if (!resource.isCollection())
            return Boolean.FALSE;

        try {
            WebdavResources webdavResources = resource.getChildResources();
            if (webdavResources == null) {
            	getLogger().warning("Folder " + resource + " does not have files");
                return Boolean.FALSE;
            }

            String[] arrayOfResourcesInStringRepresentation = webdavResources.list();
            if (ArrayUtil.isEmpty(arrayOfResourcesInStringRepresentation)) {
            	getLogger().warning("Folder " + resource + " does not have files");
                return Boolean.FALSE;
            }

            for (String s: arrayOfResourcesInStringRepresentation) {
            	int index = s.indexOf("?jsessionid");
            	if (index > 0)
            		s = s.substring(0, index);
                if (s.endsWith(CoreConstants.ARTICLE_FILENAME_SCOPE))
                    return Boolean.TRUE;
            }

            getLogger().info("Did not find any folders ending with '" + CoreConstants.DOT + CoreConstants.ARTICLE_FILENAME_SCOPE + "' inside " +
            		resource.getPath());

            /*Trying to solve out of memory exception*/
            arrayOfResourcesInStringRepresentation = null;
            resource = null;
            webdavResources = null;
        } catch (HttpException e) {
            LOGGER.log(Level.WARNING, "Http:Exception: ",e);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "No such folder or you do not have a permission to access it: ", e);
        }

        return Boolean.FALSE;
    }

    /**
     * Imports articles to database table "IC_ARTICLE" or updates it if exist.
     * @param resource Folder of articles folder.
     * @return true, if imported, false if failed or not articles folder.
     */
    private boolean importArticles(WebdavResource resource){
        if (!isArticlesFolder(resource)) {
        	getLogger().warning("Folder " + resource.getPath() + " is not folder of articles!");
            return Boolean.FALSE;
        }

        String uri = null;
        try {
        	uri = resource.getPath();

            WebdavResources filesAndFolders = resource.getChildResources();
            if (filesAndFolders == null) {
            	getLogger().warning("There are no files nor folders inside the current folder: " + resource.getPath() + ". Skipping this folder");
                return Boolean.FALSE;
            }

            WebdavResource[] arrayOfResources = filesAndFolders.listResources();
            if (ArrayUtil.isEmpty(arrayOfResources)) {
            	getLogger().warning("There are no files nor folders inside the current folder: " + resource.getPath() + ". Skipping this folder");
                return Boolean.FALSE;
            }

            boolean isImportSuccesful = Boolean.TRUE;
            String propertyName = new PropertyName("DAV", "categories").toString();
            for (WebdavResource r: arrayOfResources) {
            	uri = null;
            	String name = r.getName();
            	if (StringUtil.isEmpty(name)) {
            		getLogger().warning("Name is undefined for the resource " + resource.getPath());
            		continue;
            	}

                if (name.endsWith(CoreConstants.ARTICLE_FILENAME_SCOPE)) {
                	try {
	                    uri = r.getPath();

	                    //	Loading categories
	                    @SuppressWarnings("unchecked")
	                    Enumeration<String> resourceEnumeration = r.propfindMethod(uri, propertyName);
	                    Collection<String> articleCategories = new ArrayList<String>();
	                    if (resourceEnumeration != null){
	                        while (resourceEnumeration.hasMoreElements()) {
	                            Collection<String> tmpCategories = CategoryBean.getCategoriesFromString(resourceEnumeration.nextElement());
	                            if (!ListUtil.isEmpty(tmpCategories))
	                            	articleCategories.addAll(tmpCategories);
	                        }
	                        if (!ListUtil.isEmpty(articleCategories))
	                        	getLogger().info("Found categories for the article (" + uri + "): " + articleCategories);
	                    }
	                    if (ListUtil.isEmpty(articleCategories))
	                    	articleCategories = Collections.emptyList();
	                    else
	                    	articleCategories = new ArrayList<String>(articleCategories);

	                    //	Fixing URI
	                    if (uri.contains(CoreConstants.WEBDAV_SERVLET_URI))
	                        uri = uri.substring(CoreConstants.WEBDAV_SERVLET_URI.length());
	                    if (uri.endsWith(CoreConstants.SLASH))
	                        uri = uri.substring(0, uri.lastIndexOf(CoreConstants.SLASH));

	                    //	Writing to the DB
	                    getLogger().info("Importing article " + uri);
	                    if (getArticleDao().updateArticle(new Date(r.getCreationDate()), uri, articleCategories)) {
	                    	getLogger().info("Article " + uri + " was SUCCESSFULLY imported");
	                    } else {
	                    	getLogger().warning("FAILED to import the article: " + uri);
	                        isImportSuccesful = Boolean.FALSE;
	                        break;
	                    }
                	} finally {
                		r.close();
                	}
                }
            }

            /*Trying to solve out of memory exception*/
            if (!isImportSuccesful) {
                for (WebdavResource r : arrayOfResources) {
                    if (r != null) {
                        r.close();
                    }
                }
            }

            arrayOfResources = null;
            filesAndFolders = null;
            resource = null;

            return isImportSuccesful;
        } catch (HttpException e) {
            LOGGER.log(Level.WARNING, "Failed to import articles from (" + uri + ")", e);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "No such folder (" + uri + ") or you do not have a permission to access it!", e);
        }

        return Boolean.FALSE;
    }

    private ArticleDao getArticleDao() {
		ArticleDao articleDao = ELUtil.getInstance().getBean(ArticleDao.BEAN_NAME);
		return articleDao;
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