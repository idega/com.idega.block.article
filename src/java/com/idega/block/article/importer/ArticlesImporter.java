/**
 *
 */
package com.idega.block.article.importer;

import java.io.IOException;
import java.rmi.RemoteException;
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
import com.idega.block.article.data.dao.impl.ArticleDaoImpl;
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
    
    private static Logger LOGGER = Logger.getLogger(ArticleDaoImpl.class.getName());

    /* (non-Javadoc)
	 * @see org.springframework.context.ApplicationListener#onApplicationEvent(org.springframework.context.ApplicationEvent)
	 */
    @Override
	public void onApplicationEvent(ApplicationEvent event) {

	    if (event instanceof IWMainSlideStartedEvent){
	      
//	        Boolean isCategoriesImported = false;
//		    if(!this.getApplication().getSettings().getBoolean("is_categories_imported", Boolean.FALSE)){
//		        isCategoriesImported = this.importCategories();
//		        this.getApplication().getSettings().setProperty("is_categories_imported",isCategoriesImported.toString());
//			}
//			
//		    Boolean isArticlesImported = false;
//			if(!this.getApplication().getSettings().getBoolean("is_articles_imported", Boolean.FALSE)&&isCategoriesImported){
//			    isArticlesImported = this.importArticles();
//                this.getApplication().getSettings().setProperty("is_articles_imported",isArticlesImported.toString());
//            }
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
            return this.importArticleFolders(resource);
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
    public boolean importArticleFolders(WebdavResource resource){
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
                    }
                }

                return result;
            } catch (HttpException e) {
                LOGGER.log(Level.WARNING, "Http:Exception: ",e);
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "No such folder or you do not have a permission to access it: ", e);
            }
        }

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
            for (WebdavResource r : arrayOfResources) {
                if (r.getName().endsWith(CoreConstants.ARTICLE_FILENAME_SCOPE)) {
                    String uri = r.getPath();

                    if (uri.contains(CoreConstants.WEBDAV_SERVLET_URI)) {
                        uri = uri.substring(CoreConstants.WEBDAV_SERVLET_URI.length());
                    }

                    if (uri.endsWith(CoreConstants.SLASH)) {
                        uri = uri.substring(0, uri.lastIndexOf(CoreConstants.SLASH));
                    }

                    String propertyName = new PropertyName("DAV","categories").toString();

                    @SuppressWarnings("unchecked")
                    Enumeration<String> resourceEnumeration = r.propfindMethod(r.getPath(), propertyName);
                    if (resourceEnumeration == null) {
                        return Boolean.FALSE;
                    }

                    List<String> enumerationList = null;
                    while (resourceEnumeration.hasMoreElements()) {
                        enumerationList = (List<String>) CategoryBean.getCategoriesFromString(resourceEnumeration.nextElement());
                    }

                    if(!this.articleDao.updateArticle(new Date(r.getCreationDate()), uri, enumerationList)){
                        return Boolean.FALSE;
                    }
                    size = size-1;
                }
            }

            return Boolean.TRUE;
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
     * Test cases importCategories():
     * Passed empty directory,
     * imported: false;
     * returned: false + exception stack trace;
     */
}
