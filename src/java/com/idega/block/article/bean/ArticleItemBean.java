/*
 * $Id: ArticleItemBean.java,v 1.28 2005/02/24 16:51:41 joakim Exp $
 *
 * Copyright (C) 2004 Idega. All Rights Reserved.
 *
 * This software is the proprietary information of Idega.
 * Use is subject to license terms.
 *
 */
package com.idega.block.article.bean;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;
import org.apache.webdav.lib.PropertyName;
import org.apache.webdav.lib.WebdavResource;
import org.apache.webdav.lib.WebdavResources;
import org.apache.xmlbeans.XmlException;
import org.w3c.tidy.Tidy;
import com.idega.block.article.business.ArticleUtil;
import com.idega.business.IBOLookup;
import com.idega.content.bean.ContentItem;
import com.idega.content.bean.ContentItemBean;
import com.idega.content.bean.ContentItemField;
import com.idega.content.bean.ContentItemFieldBean;
import com.idega.data.IDOStoreException;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWUserContext;
import com.idega.presentation.IWContext;
import com.idega.slide.business.IWSlideService;
import com.idega.slide.business.IWSlideSession;
import com.idega.slide.util.WebdavExtendedResource;
import com.idega.slide.util.WebdavRootResource;
import com.idega.xml.XMLDocument;
import com.idega.xml.XMLElement;
import com.idega.xml.XMLException;
import com.idega.xml.XMLNamespace;
import com.idega.xml.XMLOutput;
import com.idega.xml.XMLParser;
import com.idega.xmlns.block.article.document.ArticleDocument;

/**
 * Bean for idegaWeb article content items.   
 * <p>
 * Last modified: $Date: 2005/02/24 16:51:41 $ by $Author: joakim $
 *
 * @author Anders Lindman
 * @version $Revision: 1.28 $
 */

public class ArticleItemBean extends ContentItemBean implements Serializable, ContentItem {
	
	public final static String KP = "error_"; // Key prefix
	
	public final static String KEY_ERROR_HEADLINE_EMPTY = KP + "headline_empty";
	public final static String KEY_ERROR_BODY_EMPTY = KP + "body_empty";
	public final static String KEY_ERROR_PUBLISHED_FROM_DATE_EMPTY = KP + "published_from_date_empty";

	private boolean _isUpdated = false;
	private List _errorKeys = null;
	
	public final static String FIELDNAME_AUTHOR = "author";
	public final static String FIELDNAME_HEADLINE = "headline";
	public final static String FIELDNAME_TEASER = "teaser";
	public final static String FIELDNAME_BODY = "body";
	public final static String FIELDNAME_SOURCE = "source";
	public final static String FIELDNAME_COMMENT = "comment";
	//Note "comment" seems to be a reserved attribute in DAV so use "article_comment" for that!!!
	public final static String FIELDNAME_ARTICLE_COMMENT = "article_comment";
	public final static String FIELDNAME_IMAGES = "images";
	public final static String FIELDNAME_FILENAME = "filename";
	public final static String FIELDNAME_FOLDER_LOCATION = "folder_location";
	public final static String FIELDNAME_CONTENT_LANGUAGE = "content_language";
	
	public final static String ARTICLE_FILENAME_SCOPE = "article";
	public final static String ARTICLE_SUFFIX = ".xml";
	public final static String CONTENT_TYPE = "ContentType";
	
	private final static String[] ATTRIBUTE_ARRAY = new String[] {FIELDNAME_AUTHOR,FIELDNAME_CREATION_DATE,FIELDNAME_HEADLINE,FIELDNAME_TEASER,FIELDNAME_BODY};

	XMLNamespace idegans = new XMLNamespace("http://xmlns.idega.com/block/article/document");
	
	/**
	 * Default constructor.
	 */
	public ArticleItemBean() {
		clear();
	}
	
	public String[] getContentFieldNames(){
		return ATTRIBUTE_ARRAY;
	}
	
	public String getHeadline() { return (String)getValue(FIELDNAME_HEADLINE); }
	public String getTeaser() { return (String)getValue(FIELDNAME_TEASER); }
	public String getBody() { return (String)getValue(FIELDNAME_BODY); }
	public String getAuthor() { return (String)getValue(FIELDNAME_AUTHOR); }
	public String getSource() { return (String)getValue(FIELDNAME_SOURCE); }
	public String getComment() { return (String)getValue(FIELDNAME_COMMENT); }
	public List getImages() { return getItemFields(FIELDNAME_IMAGES); }
	public String getFilename() { return (String)getValue(FIELDNAME_FILENAME); }
	public String getFolderLocation() {
		String resourcePath = getResourcePath();
		if(null!=resourcePath) {
			return new File(resourcePath).getParent();
		}
		return null;
//		return (String)getValue(FIELDNAME_FOLDER_LOCATION); 
		}
	public String getContentLanguage() { return (String)getValue(FIELDNAME_CONTENT_LANGUAGE); }

	public void setHeadline(String s) { setValue(FIELDNAME_HEADLINE, s); } 
	public void setHeadline(Object o) { setValue(FIELDNAME_HEADLINE, o.toString()); } 
	public void setTeaser(String s) { setValue(FIELDNAME_TEASER, s); } 
	public void setBody(String articleIn) {
		if (null != articleIn) {
//			System.out.println("ArticleIn = "+articleIn);
			//Use JTidy to clean up the html
			Tidy tidy = new Tidy();
			tidy.setXHTML(true);
			tidy.setXmlOut(true);
			ByteArrayInputStream bais = new ByteArrayInputStream(articleIn.getBytes());
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			tidy.parse(bais, baos);
			String articleOut = baos.toString();
//			System.out.println("ArticleOut = "+articleOut);
			setValue(FIELDNAME_BODY, articleOut);
//			setValue(FIELDNAME_BODY, articleIn);
		}
		else {
			setValue(FIELDNAME_BODY, null);
		}
	} 
	public void setAuthor(String s) { setValue(FIELDNAME_AUTHOR, s); } 
	public void setSource(String s) { setValue(FIELDNAME_SOURCE, s); }
	public void setComment(String s) { setValue(FIELDNAME_COMMENT, s); }
	public void setImages(List l) { setItemFields(FIELDNAME_IMAGES, l); }
	public void setFilename(String l) { setValue(FIELDNAME_FILENAME, l); }
//	public void setFolderLocation(String l) { setValue(FIELDNAME_FOLDER_LOCATION, l); }
	public void setContentLanguage(String s) { setValue(FIELDNAME_CONTENT_LANGUAGE, s); }

	public boolean isUpdated() { return _isUpdated; }
	public void setUpdated(boolean b) { _isUpdated = b; }
	public void setUpdated(Boolean b) { _isUpdated = b.booleanValue(); }
	
	/**
	 * Clears all all attributes for this bean. 
	 */
	public void clear() {
		super.clear();

		setHeadline(null);
		setTeaser(null);
		setBody(null);
		setAuthor(null);
		setSource(null);
		setComment(null);
		setImages(null);
		setFilename(null);
//		setFolderLocation(null);
		_isUpdated = false;
	}
	
	/**
	 * Adds an image to this article item.
	 */
	public void addImage(byte[] imageData, String contentType) {
		List l = getImages();
		if (l == null) {
			l = new ArrayList();
		}
		ContentItemField field = new ContentItemFieldBean();
		field.setBinaryValue(imageData);
		field.setFieldType(contentType);
		field.setOrderNo(l.size());
		l.add(field);
		setImages(l);
	}
	
	/**
	 * Removes the image with the specified image number from this article item.
	 */
	public void removeImage(Integer imageNumber) {
		int imageNo = imageNumber.intValue();
		try {
			List l = getImages();
			l.remove(imageNo);
			for (int i = 0; i < l.size(); i++) {
				ContentItemField field = (ContentItemField) l.get(i);
				field.setOrderNo(i);
			}
		} catch (Exception e) {}
	}
	
	
	/**
	 * Returns localization keys for error messages.  
	 */
	public List getErrorKeys() {
		return _errorKeys;
	}
	
	/**
	 * Adds an error message localization key.  
	 */
	public void addErrorKey(String key) {
		if (_errorKeys == null) {
			_errorKeys = new ArrayList();
		}
		_errorKeys.add(key);
	}
	
	/**
	 * Clears all error message localization keys.  
	 */
	public void clearErrorKeys() {
		_errorKeys = new ArrayList();
	}
	
	public Boolean storeArticle() {
		try {
			store();
		}catch(ArticleStoreException e) {
			return new Boolean(false);
		}
		return new Boolean(true);
	}
	
	/**
	 * Constructs the path for the article file as follows
	 * First it gets the folder location of the article, this is typically generated by
	 * ArticleUtil.getArticleYearMonthPath()
	 * Then it appends a unique filename based on time, followed by .article
	 * Example returnstring: /files/cms/article/2005/02/20050222-1246.article/en.xml
	 * @param service
	 * @return
	 */
	protected String getArticlePath(IWSlideService service){
		String folderLocation = getFolderLocation();
		if(null==folderLocation || "".equalsIgnoreCase(folderLocation)) {
			folderLocation=ArticleUtil.getArticleYearMonthPath();
		}
		StringBuffer path = new StringBuffer(folderLocation);
		path.append("/");
		path.append(service.createUniqueFileName(ARTICLE_FILENAME_SCOPE));
		path.append(".");
		path.append(ARTICLE_FILENAME_SCOPE);
		path.append("/");
		path.append(getArticleName());
		return path.toString();
	}
	
	protected String getArticleName(){
		return getArticleName(getContentLanguage());
	}
	
	protected String getArticleName(Locale locale){
		return getArticleName(locale.getLanguage());
	}
	
	protected String getArticleName(String language){
		return language+ARTICLE_SUFFIX;
	}

	/**
	 * Returns the Article Item as an XML-formatted string
	 * @return the XML string
	 * @throws IOException
	 */
	public String getAsXML() throws IOException {

		XMLParser builder = new XMLParser();
		XMLDocument bodyDoc = null;
		try {
			bodyDoc = builder.parse(new ByteArrayInputStream(getBody().getBytes()));
		} catch (XMLException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
		XMLElement bodyElement = bodyDoc.getRootElement();

		
		
		XMLElement root = new XMLElement("article",idegans);
		XMLElement headline = new XMLElement(FIELDNAME_HEADLINE,idegans).setText(getHeadline());
		XMLElement teaser = new XMLElement(FIELDNAME_TEASER,idegans).setText(getTeaser());
		XMLElement author = new XMLElement(FIELDNAME_AUTHOR,idegans).setText(getAuthor());
		XMLElement articleComment = new XMLElement("article_comment",idegans).setText(getComment());

		XMLElement body = new XMLElement(FIELDNAME_BODY,idegans).addContent(bodyElement);

		root.addContent(headline);
		root.addContent(teaser);
		root.addContent(body);
		root.addContent(author);
		root.addContent(articleComment);
		XMLDocument doc = new XMLDocument(root);
		XMLOutput outputter = new XMLOutput();
		return outputter.outputString(doc);
	}

	/**
	 * @deprecated use getAsXML() instead. We have dropped XMLBeans, since it insisted on adding CData
	 * @return
	 */
/*	public String getAsXMLFromXMLBeans() {
		ArticleDocument articleDoc = ArticleDocument.Factory.newInstance();
	    
	    ArticleDocument.Article article =  articleDoc.addNewArticle();
	    
		article.setHeadline(getHeadline());
		article.setBody(getBody());
		article.setTeaser(getTeaser());
		article.setAuthor(getAuthor());
		article.setSource(getSource());
		article.setComment(getComment());
		
		return articleDoc.toString();
	}
*/
	/**
	 * This is a temporary holder for the Slide implementation
	 * This should be replace as soon as Slide is working
	 */
	public void store() throws IDOStoreException{
		boolean storeOk = true;
		clearErrorKeys();

		if (getHeadline().trim().equals("")) {
			addErrorKey(KEY_ERROR_HEADLINE_EMPTY);
			storeOk = false;
		}
		if (getBody().trim().equals("")) {
			addErrorKey(KEY_ERROR_BODY_EMPTY);
			storeOk = false;
		}
		
//		if (getRequestedStatus() != null && getRequestedStatus().equals(ContentItemCase.STATUS_PUBLISHED)) {
//			if (getCase().getPublishedFromDate() == null) {
//				addErrorKey(KEY_ERROR_PUBLISHED_FROM_DATE_EMPTY);
//				storeOk = false;
//			}
//		}
		
//		String filename = getHeadline();
//		if(null==filename || filename.length()==0) {
//			filename = "empty";
//		}

		try {
			IWUserContext iwuc = IWContext.getInstance();
			IWApplicationContext iwac = iwuc.getApplicationContext();
			
			IWSlideSession session = (IWSlideSession)IBOLookup.getSessionInstance(iwuc,IWSlideSession.class);
			IWSlideService service = (IWSlideService)IBOLookup.getServiceInstance(iwac,IWSlideService.class);
		
			WebdavRootResource rootResource = session.getWebdavRootResource();

			IWSlideService slideService = (IWSlideService)IBOLookup.getServiceInstance(iwac,IWSlideService.class);
			
			String filePath=getResourcePath();
			String parentPath;
			if(null!=filePath) { //File alrady exist
				parentPath=filePath;
				filePath += "/"+getArticleName();
			}else {	//Create new file(name)
				filePath = getArticlePath(service);
				parentPath = new File(filePath).getParent();
			}
//			File file = new File(path);
			
			slideService.createAllFoldersInPath(parentPath);

			rootResource.proppatchMethod(parentPath,new PropertyName("IW:",CONTENT_TYPE),"LocalizedFile",true);
			
			String article = getAsXML();
			
			rootResource.putMethod(session.getURI(filePath),article);
			rootResource.proppatchMethod(filePath,new PropertyName("IW:",CONTENT_TYPE),ARTICLE_FILENAME_SCOPE,true);
			rootResource.close();
			try {
				load(filePath);
			}
			catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		catch (IOException e1) {
			storeOk = false;
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		if (storeOk) {
			if (getRequestedStatus() != null) {
				setStatus(getRequestedStatus());
				setRequestedStatus(null);
			}
		}else {
			throw new ArticleStoreException();
		}
	}
    
	/**
	 * Loads all xml files in the given folder
	 * @deprecated use load() instead
	 * @param folder
	 * @return List containing ArticleItemBean
	 * @throws XmlException
	 * @throws IOException
	 */
	public void loadOld(WebdavExtendedResource webdavResource) throws XmlException, IOException {
	
		ArticleDocument articleDoc;
		
		articleDoc = ArticleDocument.Factory.parse(webdavResource.getMethodDataAsString());
		
	    ArticleDocument.Article article =  articleDoc.getArticle();
	    setHeadline(article.getHeadline());
	    setBody(article.getBody());
	    setTeaser(article.getTeaser());
	    setAuthor(article.getAuthor());
	    setSource(article.getSource());
	    setComment(article.getComment());
//		String folder = webdavResource.getParentPath();
//	    setFolderLocation(folder);
	    
	}
	
	/**
	 * Loads an xml file specified by the webdav resource
	 * The beans atributes are then set according to the information in the XML file
	 */
	public void load(WebdavExtendedResource webdavResource) throws IOException {
		XMLParser builder = new XMLParser();
		XMLDocument bodyDoc = null;
		try {
			WebdavResource theArticle = null;
			if(webdavResource.isCollection()){
				IWContext iwc = IWContext.getInstance();
				
				WebdavResources resources = webdavResource.getChildResources();
				String userLanguageArticleName = getArticleName(iwc.getCurrentLocale());
				String systemLanguageArticleName = getArticleName(iwc.getIWMainApplication().getDefaultLocale());
				if(resources.isThereResourceName(userLanguageArticleName)){ //the language that the user has selected
					theArticle = resources.getResource(userLanguageArticleName);
				} else if(resources.isThereResourceName(systemLanguageArticleName)){ //the language that is default for the system
					theArticle = resources.getResource(systemLanguageArticleName);
				} else {  // take the first language
					Enumeration en = resources.getResources();
					if(en.hasMoreElements()){
						theArticle = (WebdavResource)en.nextElement();
					}
				}
			} else {
				theArticle = webdavResource;
				setResourcePath(webdavResource.getParentPath());
			}
			if(theArticle!=null){
				bodyDoc = builder.parse(new ByteArrayInputStream(theArticle.getMethodDataAsString().getBytes()));
			}
		} catch (XMLException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
		if(bodyDoc!=null){
			XMLElement rootElement = bodyDoc.getRootElement();
	
			try {
				setHeadline(rootElement.getChild(FIELDNAME_HEADLINE,idegans).getText());
			}catch(Exception e) {		//Nullpointer could occur if field isn't used
				e.printStackTrace();
				setHeadline("");
			}
			try {
				setTeaser(rootElement.getChild(FIELDNAME_TEASER,idegans).getText());
			}catch(Exception e) {		//Nullpointer could occur if field isn't used
				e.printStackTrace();
				setTeaser("");
			}
			try {
				setAuthor(rootElement.getChild(FIELDNAME_AUTHOR,idegans).getText());
			}catch(Exception e) {		//Nullpointer could occur if field isn't used
				e.printStackTrace();
				setAuthor("");
			}
	
			//Parse out the body
			try {
				XMLNamespace htmlNamespace = new XMLNamespace("http://www.w3.org/1999/xhtml");
				XMLElement bodyElement = rootElement.getChild(FIELDNAME_BODY,idegans);
				XMLElement htmlElement = bodyElement.getChild("html",htmlNamespace);
				XMLElement htmlBodyElement = htmlElement.getChild("body",htmlNamespace);
				String bodyValue = new XMLOutput().outputString(htmlBodyElement);
	//			System.out.println("htmlBody value= "+bodyValue);
				setBody(bodyValue);
			}catch(Exception e) {		//Nullpointer could occur if field isn't used
	//			e.printStackTrace();
				Logger log = Logger.getLogger(this.getClass().toString());
				log.warning("Body of article is empty");
				setBody("");
			}
			
			try {
				setComment(rootElement.getChild(FIELDNAME_ARTICLE_COMMENT,idegans).getText());
			}catch(Exception e) {		//Nullpointer could occur if field isn't used
				e.printStackTrace();
				setComment("");
			}
			
		} else {
			//article not found
			Logger log = Logger.getLogger(this.getClass().toString());
			log.warning("Article xml file was not found");
		}
		String folder = webdavResource.getParentPath();
	    setResourcePath(folder);
//	    setFilename();
//		setFolderLocation(bodyElement.getChild(FIELDNAME_FOLDER_LOCATION,idegans).getText());
	}
	
/*
 * The old XMLBean way of loading data
	public void loadOld(File file) throws XmlException, IOException{
		ArticleDocument articleDoc;
		
		articleDoc = ArticleDocument.Factory.parse(file);
		
	    ArticleDocument.Article article =  articleDoc.getArticle();
	    setHeadline(article.getHeadline());
	    setBody(article.getBody());
	    setTeaser(article.getTeaser());
	    setAuthor(article.getAuthor());
	    setSource(article.getSource());
	    setComment(article.getComment());
	}
*/
	/* (non-Javadoc)
	 * @see com.idega.content.bean.ContentItem#getContentItemPrefix()
	 */
	public String getContentItemPrefix() {
		return "article_";
	}
	
}
