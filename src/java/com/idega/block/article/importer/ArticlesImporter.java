package com.idega.block.article.importer;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;

import org.directwebremoting.annotations.Param;
import org.directwebremoting.annotations.RemoteMethod;
import org.directwebremoting.annotations.RemoteProxy;
import org.directwebremoting.spring.SpringCreator;
import org.jdom2.Document;
import org.jdom2.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.idega.block.article.data.dao.ArticleDao;
import com.idega.block.article.data.dao.CategoryDao;
import com.idega.builder.bean.AdvancedProperty;
import com.idega.content.business.categories.CategoriesEngine;
import com.idega.content.business.categories.CategoryBean;
import com.idega.content.data.ContentCategory;
import com.idega.core.business.DefaultSpringBean;
import com.idega.core.localisation.business.ICLocaleBusiness;
import com.idega.idegaweb.IWMainApplicationSettings;
import com.idega.idegaweb.RepositoryStartedEvent;
import com.idega.idegaweb.UnavailableIWContext;
import com.idega.presentation.IWContext;
import com.idega.repository.bean.RepositoryItem;
import com.idega.repository.jcr.JCRItem;
import com.idega.util.CoreConstants;
import com.idega.util.CoreUtil;
import com.idega.util.IOUtil;
import com.idega.util.ListUtil;
import com.idega.util.StringHandler;
import com.idega.util.StringUtil;
import com.idega.util.xml.XmlUtil;

/**
 * Imports articles and their categories to database.
 * @author martynas
 * Last changed: 2011.05.18
 * You can report about problems to: <a href="mailto:martynas@idega.com">Martynas Stakė</a>
 * You can expect to find some test cases notice in the end of the file.
 */

@Service(ArticlesImporter.BEAN_NAME)
@Scope(BeanDefinition.SCOPE_SINGLETON)
@RemoteProxy(creator=SpringCreator.class, creatorParams={
	@Param(name="beanName", value=ArticlesImporter.BEAN_NAME),
	@Param(name="javascript", value=ArticlesImporter.DWR_OBJECT)
}, name=ArticlesImporter.DWR_OBJECT)
public class ArticlesImporter extends DefaultSpringBean implements ApplicationListener<RepositoryStartedEvent> {

	static final String BEAN_NAME = "iwArticlesImporter",
						DWR_OBJECT = "ArticlesImporter";

	@Autowired
    private CategoriesEngine categoryEngine;

    @Autowired
    private CategoryDao categoryDao;

    @Autowired
    private ArticleDao articleDao;

    public static final String	CATEGORIES_IMPORTED_APP_PROP = "is_categories_imported",
    							ARTICLES_IMPORTED_APP_PROP = "is_articles_imported",
    							CATEGORIES_BUG_FIXED_PROP = "categories_bug_fixed";
    
    private SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd'T'HH:mm:ss'Z'");

    @Override
    public void onApplicationEvent(RepositoryStartedEvent event) {
    	IWMainApplicationSettings settings = getApplication().getSettings();
    	doImport(settings.getBoolean(CATEGORIES_IMPORTED_APP_PROP, Boolean.FALSE), settings.getBoolean(ARTICLES_IMPORTED_APP_PROP, Boolean.FALSE), null);
    }

    @RemoteMethod
    public boolean doImportCategoriesAndArticles(String oldRepo) {
    	IWContext iwc = CoreUtil.getIWContext();
    	if (iwc == null) {
    		return false;
    	}

    	boolean superAdmin = iwc.isLoggedOn() && iwc.isSuperAdmin();
    	if (superAdmin) {
    		doImport(false, false, oldRepo);
    		return true;
    	}

    	getLogger().warning("Insufficient rights");
    	return false;
    }

    private void doImport(Boolean isCategoriesImported,  Boolean isArticlesImported, String oldRepo) {
    	if (!isCategoriesImported) {
    		isCategoriesImported = importCategories();
    		getApplication().getSettings().setProperty(CATEGORIES_IMPORTED_APP_PROP, isCategoriesImported.toString());
    	}

    	if (!isArticlesImported && isCategoriesImported) {
    		isArticlesImported = this.importArticles(oldRepo);
    		getApplication().getSettings().setProperty(ARTICLES_IMPORTED_APP_PROP, isArticlesImported.toString());
        }
    }

    /**
     * Method for importing categories, which are in categories.xml, but not in database
     * @return true, if imported, false, if at least one category was not imported
     */
    private boolean importCategories(){
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
                getLogger().log(Level.WARNING,  "Failed to import because categories.xml deos not exist",  e);
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
    private boolean importArticles(String oldRepo) {
        try {
            /*Getting the articles folders*/
            RepositoryItem resource = getRepositoryService().getRepositoryItemAsRootUser(CoreConstants.CONTENT_PATH+CoreConstants.ARTICLE_CONTENT_PATH);
            return this.importArticleFolders(resource, oldRepo);
        } catch (RepositoryException e) {
            getLogger().log(Level.WARNING, "Failed to import articles from " + CoreConstants.CONTENT_PATH+CoreConstants.ARTICLE_CONTENT_PATH, e);
        }
        return Boolean.FALSE;
    }

    /**
     * Browse recursively thought folders, searches for articles folders, imports articles if found some.
     * @param resource Path where to start looking for articles to import.
     * @return true, if articles found and imported, false if not all articles imported.
     */
    private boolean importArticleFolders(RepositoryItem resource, String oldRepo) {
        if (resource == null) {
            return Boolean.FALSE;
        }

        if (!resource.exists()) {
            return Boolean.FALSE;
        }

        importArticles(resource, oldRepo);

        try {
        	Collection<RepositoryItem> foldersAndFilesResources = resource.getChildResources();
        	if (ListUtil.isEmpty(foldersAndFilesResources)) {
        		return Boolean.FALSE;
        	}

        	boolean result = Boolean.FALSE;
        	for (RepositoryItem wr: foldersAndFilesResources) {
                result = importArticleFolders(wr, oldRepo);
        	}

        	/*Trying to solve out of memory exception*/
        	foldersAndFilesResources = null;
        	resource = null;

        	return result;
        } catch (Exception e) {
        	getLogger().log(Level.WARNING, "Failed to import articles from: " + resource.getPath(), e);
        }

        return Boolean.FALSE;
    }

    /**
     * Checks, if it is folder containing article folders.
     * @param resource Folder, that might be containing article folders.
     * @return true, if it is folder of articles, false, if not.
     */
    private boolean isArticlesFolder(RepositoryItem resource){
        if (resource == null || !resource.exists()) {
            return Boolean.FALSE;
        }

        /*Checking is it folder*/
        if (!resource.isCollection()) {
            return Boolean.FALSE;
        }

        try {
            Collection<RepositoryItem> webdavResources = resource.getChildResources();
            if (ListUtil.isEmpty(webdavResources)) {
                return Boolean.FALSE;
            }

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
    private boolean importArticles(RepositoryItem resource, String oldRepo) {
    	
        if (!this.isArticlesFolder(resource)) {
            return Boolean.FALSE;
        }

        try {
            Collection<RepositoryItem> filesAndFolders = resource.getChildResources();
            if (ListUtil.isEmpty(filesAndFolders)) {
                return Boolean.FALSE;
            }

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

                    AdvancedProperty properties = null;
                    String creationDate = null;
                    if (StringUtil.isEmpty(categoriesProperty) && !StringUtil.isEmpty(oldRepo)) {
                    	properties = getCategoriesAndCreationDate(jcrItem, oldRepo);
                    	if (properties != null) {
                    		categoriesProperty = properties.getId();
                    		creationDate = properties.getValue();
                    	}
                    }
                    if (!StringUtil.isEmpty(categoriesProperty) && !CoreConstants.COMMA.equals(categoriesProperty.trim())) {
                    	categories = CategoryBean.getCategoriesFromString(categoriesProperty);
                    	getLogger().info("Found categories: " + categories + " for " + item);
                    }

                    if (!this.articleDao.updateArticle(
                    		StringUtil.isEmpty(creationDate) ? new Date(item.getCreationDate()) : sdf.parse(creationDate),
                    		uri,
                    		ListUtil.isEmpty(categories) ? null : new ArrayList<String>(categories))
                    ) {
                    	getLogger().warning("Failed to save article in DB: " + uri);
                        isImportSuccesful = Boolean.FALSE;
                        break;
                    }
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

    private AdvancedProperty getCategoriesAndCreationDate(JCRItem jcrItem, String oldRepo) {
    	if (StringUtil.isEmpty(oldRepo) || jcrItem == null) {
    		return null;
    	}

    	Collection<RepositoryItem> children = jcrItem.getChildResources();
    	if (ListUtil.isEmpty(children)) {
    		return null;
    	}

    	String path = null;
    	InputStream stream = null;
    	String folderPath = oldRepo + jcrItem.getAbsolutePath();
    	
    	//	From version 4.x
    	
    	try {
    		path = getFirstFilePathFromFolder(folderPath);
    		if(path == null) {
    			return null;
    		}
    		stream = new FileInputStream(new File(path));
    		String content = StringHandler.getContentFromInputStream(stream);
    		String start = "<category term=\"";
    		int categoriesIndex = content.indexOf(start);
    		if (categoriesIndex == -1) {
    			getLogger().warning("No categories defined in " + content + " for " + jcrItem);
    		} else {
    			int end = categoriesIndex + 1;
    			while (end < content.length() && !CoreConstants.EQ.equals(content.substring(end, end + 1))) {
    				end++;
    			}
    			return new AdvancedProperty(content.substring(categoriesIndex + start.length() + 1, end));
    		}
    	} catch (Exception e) {
    		getLogger().log(Level.WARNING, "Error getting categories from " + path + " for " + jcrItem, e);
    	} finally {
    		IOUtil.close(stream);
    	}

    	//	From version 3.x
    	try {
    		folderPath = StringHandler.replace(folderPath, "content", "metadata");
    		path = getFirstFilePathFromFolder(folderPath);
    		if(path == null) {
    			return null;
    		}
    		/*
    		if(files.length != 1) {
    			getLogger().warning("Folder contains more than one file!");
    		}
    		*/
    		stream = new FileInputStream(new File(path));
    		Document doc = XmlUtil.getJDOMXMLDocument(stream);
    		Element rootElement = doc.getRootElement();
    		List<Element> properties = XmlUtil.getElementsByXPath(rootElement, "property", "DAV");
    		if (ListUtil.isEmpty(properties)) {
    			getLogger().warning("There are no properties for " + path);
    		} else {
    			String categories = null, creationDate = null;
    			for (Element property: properties) {
    				String propertyName = property.getAttributeValue("name");
    				if ("categories".equals(propertyName)) {
    					categories = property.getAttributeValue("value");
    					getLogger().info("Found categories '" + categories + "' at " + path + " for " + jcrItem);
       				} else if ("modificationdate".equals(propertyName)) {
    					creationDate = property.getAttributeValue("value");
       				}
    			}
    			return new AdvancedProperty(categories, creationDate);
    		}
    	} catch (Exception e) {
    		getLogger().log(Level.WARNING, "Error getting categories from " + path + " for " + jcrItem, e);
    	} finally {
    		IOUtil.close(stream);
    	}

    	return null;
    }

    public String getFirstFilePathFromFolder(String folderPath) {
    	if(StringUtil.isEmpty(folderPath)) {
    		return null;
    	}
		File folder = new File(folderPath);
		File[] files = folder.listFiles();
		if(files == null) {
			return null;
		}
    	return files[0].getAbsolutePath();
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