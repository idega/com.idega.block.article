package com.idega.block.article.importer;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Collection;
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

import com.idega.block.article.data.dao.ArticleDao;
import com.idega.block.article.data.dao.CategoryDao;
import com.idega.content.business.categories.CategoriesEngine;
import com.idega.content.business.categories.CategoryBean;
import com.idega.content.data.ContentCategory;
import com.idega.core.business.DefaultSpringBean;
import com.idega.core.localisation.business.ICLocaleBusiness;
import com.idega.idegaweb.IWMainSlideStartedEvent;
import com.idega.idegaweb.UnavailableIWContext;
import com.idega.slide.business.IWSlideService;
import com.idega.util.CoreConstants;
import com.idega.util.ListUtil;


/**
 * Imports articles and their categories to database.
 * @author martynas
 * Last changed: 2011.05.18
 * You can report about problems to: <a href="mailto:martynas@idega.com">Martynas Stakė</a>
 * AIM: lapiukshtiss
 * Skype: lapiukshtiss
 * You can expect to find some test cases notice in the end of the file.
 */

@Service
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class ArticlesImporter extends DefaultSpringBean implements ApplicationListener {
    @Autowired
    private CategoriesEngine categoryEngine;

    @Autowired
    private CategoryDao categoryDao;
    
    @Autowired 
    private ArticleDao articleDao;
    
    private static Logger LOGGER = Logger.getLogger(ArticlesImporter.class.getName());

    private static final String CATEGORIES_IMPORTED_APP_PROP = "is_categories_imported",
    							ARTICLES_IMPORTED_APP_PROP = "is_articles_imported";
    
    /* (non-Javadoc)
     * @see org.springframework.context.ApplicationListener#onApplicationEvent(org.springframework.context.ApplicationEvent)
     */
    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof IWMainSlideStartedEvent) {
            Boolean isCategoriesImported = getApplication().getSettings().getBoolean(CATEGORIES_IMPORTED_APP_PROP, Boolean.FALSE);
            if (!isCategoriesImported) {
                isCategoriesImported = importCategories();
                getApplication().getSettings().setProperty(CATEGORIES_IMPORTED_APP_PROP, isCategoriesImported.toString());
            }

            Boolean isArticlesImported = getApplication().getSettings().getBoolean(ARTICLES_IMPORTED_APP_PROP, Boolean.FALSE);
            if (!isArticlesImported && isCategoriesImported) {
                isArticlesImported = this.importArticles();
                getApplication().getSettings().setProperty(ARTICLES_IMPORTED_APP_PROP, isArticlesImported.toString());
            }
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
                LOGGER.log(Level.WARNING, "Failed to import because categories.xml deos not exist", e);
                return Boolean.FALSE;
            }
            
            if(ListUtil.isEmpty(categoryList)){
                continue;
            }
            for(ContentCategory category : categoryList){
                Boolean isAdded = categoryDao.addCategory(category.getId());
                if(!isAdded){
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

        IWSlideService iWSlideService = getServiceInstance(IWSlideService.class);
        try {
            /*Getting the articles folders*/
            WebdavResource resource = iWSlideService.getWebdavResourceAuthenticatedAsRoot(CoreConstants.CONTENT_PATH+CoreConstants.ARTICLE_CONTENT_PATH);
            boolean importResult = this.importArticleFolders(resource);
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
    public boolean importArticleFolders(WebdavResource resource) {
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
                WebdavResource[] foldersAndFilesResources = resource.listWebdavResources();
                if (foldersAndFilesResources == null) {
                    return Boolean.FALSE;
                }
                
                boolean result = Boolean.FALSE;
                for (WebdavResource wr : foldersAndFilesResources) {
                    if (this.importArticleFolders(wr)) {
                        result = Boolean.TRUE;
                        wr.close();
                    } else {
                        if (wr != null) {
                            wr.close();
                        }
                    }
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
        }
        
        // TODO close resources on failure. Does possible to get failure here?
        return Boolean.FALSE;        
    }
    
    /**
     * Checks, if it is folder containing article folders.
     * @param resource Folder, that might be containing article folders.
     * @return true, if it is folder of articles, false, if not.
     */
    public boolean isArticlesFolder(WebdavResource resource){
        if (resource == null || !resource.exists()) {
            return Boolean.FALSE;
        }
        
        /*Checking is it folder*/
        if (!resource.isCollection()) {
            return Boolean.FALSE;
        }
        
        try {
            WebdavResources webdavResources = resource.getChildResources();
            if (webdavResources == null) {
                return Boolean.FALSE;
            }
            
            String[] arrayOfResourcesInStringRepresentation = webdavResources.list();
            if (arrayOfResourcesInStringRepresentation == null) {
                return Boolean.FALSE;
            }
            
            for (String s : arrayOfResourcesInStringRepresentation) {
                if (s.endsWith(CoreConstants.ARTICLE_FILENAME_SCOPE)) {
                    return Boolean.TRUE;
                }
            }
            
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
    public boolean importArticles(WebdavResource resource){
        if (!this.isArticlesFolder(resource)) {
            return Boolean.FALSE;
        }
        
        try {
            WebdavResources filesAndFolders = resource.getChildResources();
            if (filesAndFolders == null) {
                return Boolean.FALSE;
            }
            
            WebdavResource[] arrayOfResources = filesAndFolders.listResources();
            if (arrayOfResources == null) {
                return Boolean.FALSE;
            }
            
            int size = arrayOfResources.length;
            boolean isImportSuccesful = Boolean.TRUE;
            String propertyName = new PropertyName("DAV", "categories").toString();
            for (WebdavResource r : arrayOfResources) {
                if (r.getName().endsWith(CoreConstants.ARTICLE_FILENAME_SCOPE)) {
                    String uri = r.getPath();
                        
                    if (uri.contains(CoreConstants.WEBDAV_SERVLET_URI)) {
                        uri = uri.substring(CoreConstants.WEBDAV_SERVLET_URI.length());
                    }
                        
                    if (uri.endsWith(CoreConstants.SLASH)) {
                        uri = uri.substring(0, uri.lastIndexOf(CoreConstants.SLASH));
                    }
                    
                    @SuppressWarnings("unchecked")
                    Enumeration<String> resourceEnumeration = r.propfindMethod(r.getPath(), propertyName);
                    Collection<String> enumerationList = null;
                    
                    if (resourceEnumeration != null){
                        while (resourceEnumeration.hasMoreElements()) {
                            enumerationList = CategoryBean.getCategoriesFromString(resourceEnumeration.nextElement());
                        }
                    }
                    
                    if(!this.articleDao.updateArticle(new Date(r.getCreationDate()), uri, enumerationList)){
                        isImportSuccesful = Boolean.FALSE;
                        break;
                    }
                    size = size-1;
                    r.close();
                }
            }
            
            /*Trying to solve out of memory exception*/
            if (!isImportSuccesful) {
                for (WebdavResource r : arrayOfResources){
                    if (r != null){
                        r.close();
                    }
                }
            }
            
            arrayOfResources = null;
            filesAndFolders = null;
            resource = null;
            return isImportSuccesful;
        } catch (HttpException e) {
            LOGGER.log(Level.WARNING, "Failed to import articles cause of:", e);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "No such folder or you do not have a permission to access it: ", e);
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
