/**
 *
 */
package com.idega.block.article.importer;

import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.ArrayList;
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
import org.directwebremoting.export.Data;
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
import com.idega.content.data.ContentCategory;
import com.idega.core.business.DefaultSpringBean;
import com.idega.core.localisation.business.ICLocaleBusiness;
import com.idega.idegaweb.IWMainSlideStartedEvent;

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
		// TODO Imformuoja, kad galima naudotis /cms/file...
		// TODO Sukurti application property bus naudojamas nustayti, pažiūrėti, ar jau importuota.
		// TODO DefaultSpringBean pasinaudojant paslaugimis
		// TODO Importuotojai turi būti du: kategorijoms ir articles
		// TODO Pakomitinti
		if (event instanceof IWMainSlideStartedEvent){
			// TODO Šioje vietoje paleisti importerį, jei neimportuota.
			if(!this.getApplication().getSettings().getBoolean("is_categories_imported", Boolean.FALSE)){
				Boolean isImported = this.importCategories();
				this.getApplication().getSettings().setProperty("is_categories_imported",isImported.toString());
			}
		}

		// TODO Šioje klasėje padarome metoodus categorijoms ir articlams importinti
	}

	public boolean importCategories(){
		List<Locale> localeList = ICLocaleBusiness.getListOfLocalesJAVA();
		for(Locale locale : localeList){
			List<ContentCategory> categoryList = this.categoryEngine.getCategoriesByLocale(locale.toString());
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

    public boolean importArticles(){
		 
        IWSlideService iWSlideService = getServiceInstance(IWSlideService.class);
        try {
        /*Getting the articles folders*/
            WebdavResource resource1 = iWSlideService.getWebdavResourceAuthenticatedAsRoot("/files/cms/article");
            
            /*/files/cms/article resources*/
            WebdavResource[] folderResources1 = resource1.listWebdavResources();
            this.importArticles(folderResources1[0]);
            
            /*/files/cms/article/2011 resources*/
            WebdavResource[] folderResources2 = folderResources1[0].listWebdavResources();
            this.importArticles(folderResources2[0]);
            
            /*/files/cms/article/2011/05 resources*/
            WebdavResource[] folderResources3 = folderResources2[0].listWebdavResources();
            this.importArticles(folderResources3[0]);
            
            return Boolean.TRUE;
        } catch (HttpException e) {
            LOGGER.log(Level.WARNING, "Failed to import articles cause of:", e);
        } catch (RemoteException e) {
            LOGGER.log(Level.WARNING, "Failed to import articles cause of:", e);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to import articles cause of:", e);
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
     * Imports articles to database table "IC_ARTICLE".
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
                    
                    Enumeration enumeration = r.propfindMethod(uri, new PropertyName("DAV","categories").toString());
                    List<String> enumerationList = Collections.list(enumeration);
                    this.articleDao.updateArticle(new Date(r.getCreationDate()), uri, null);
                    size = size-1;
                    System.out.println("Liko: " + size + " elementų");
                }
            }
            
            return Boolean.TRUE;
        } catch (HttpException e) {
            LOGGER.log(Level.WARNING, "Failed to import articles cause of:", e);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to import articles cause of:", e);
        }
        
        return Boolean.FALSE;
    }
    
    /*
     * Test cases isArticlesFolder(WebdavResource resource):
     * Passed articles folder, returned: true
     * Passed not articles folder, returned: false
     * Passed null, returned: 
     * 
     * Test cases importArticles(WebdavResource resource):
     * Passed articles folder, without categories, 
     * returned: true;
     * imported: true;
     * 
     * Test cases importArticles(WebdavResource resource):
     * Passed articles folder, with categories, 
     * returned: true;
     * imported: true;
     * 
     * Passed not articles folder, 
     * returned: false;
     * imported: false;
     * 
     * Passed null,
     * returned:
     * imported:
     */
}