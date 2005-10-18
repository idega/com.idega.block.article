/*
 * $Id: ArticleItemBean.java,v 1.52 2005/10/18 08:50:16 laddi Exp $
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
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;
import org.apache.webdav.lib.Ace;
import org.apache.webdav.lib.Privilege;
import org.apache.webdav.lib.PropertyName;
import org.apache.webdav.lib.WebdavResource;
import org.apache.webdav.lib.WebdavResources;
import org.w3c.tidy.Configuration;
import org.w3c.tidy.Tidy;
import com.idega.block.article.business.ArticleUtil;
import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.content.bean.ContentItem;
import com.idega.content.bean.ContentItemBean;
import com.idega.content.bean.ContentItemCase;
import com.idega.content.bean.ContentItemField;
import com.idega.content.bean.ContentItemFieldBean;
import com.idega.content.business.ContentUtil;
import com.idega.core.accesscontrol.business.StandardRoles;
import com.idega.data.IDOStoreException;
import com.idega.idegaweb.IWUserContext;
import com.idega.idegaweb.UnavailableIWContext;
import com.idega.presentation.IWContext;
import com.idega.slide.authentication.AuthenticationBusiness;
import com.idega.slide.business.IWSlideService;
import com.idega.slide.business.IWSlideServiceBean;
import com.idega.slide.business.IWSlideSession;
import com.idega.slide.util.AccessControlEntry;
import com.idega.slide.util.AccessControlList;
import com.idega.slide.util.WebdavExtendedResource;
import com.idega.slide.util.WebdavRootResource;
import com.idega.xml.XMLDocument;
import com.idega.xml.XMLElement;
import com.idega.xml.XMLException;
import com.idega.xml.XMLNamespace;
import com.idega.xml.XMLOutput;
import com.idega.xml.XMLParser;

/**
 * Bean for idegaWeb article content items.   
 * <p>
 * Last modified: $Date: 2005/10/18 08:50:16 $ by $Author: laddi $
 *
 * @author Anders Lindman
 * @version $Revision: 1.52 $
 */

public class ArticleItemBean extends ContentItemBean implements Serializable, ContentItem {
	
	public final static String KP = "error_"; // Key prefix
	
	public final static String KEY_ERROR_HEADLINE_EMPTY = KP + "headline_empty";
	public final static String KEY_ERROR_BODY_EMPTY = KP + "body_empty";
	public final static String KEY_ERROR_PUBLISHED_FROM_DATE_EMPTY = KP + "published_from_date_empty";
	public final static String KEY_ERROR_ON_STORE = KP + "error_saving";

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
	public final static String FIELDNAME_FOLDER_LOCATION = "folder_location"; // ../cms/article/YYYY/MM/
	public final static String FIELDNAME_CONTENT_LANGUAGE = "content_language";
	public final static String FIELDNAME_LANGUAGE_CHANGE = "language_change";
	
	public final static String ARTICLE_FILENAME_SCOPE = "article";
	public final static String ARTICLE_SUFFIX = ".xml";
	public final static String CONTENT_TYPE = "ContentType";
	
public static final PropertyName PROPERTY_CONTENT_TYPE = new PropertyName("IW:",CONTENT_TYPE);
	
	private final static String[] ATTRIBUTE_ARRAY = new String[] {FIELDNAME_AUTHOR,FIELDNAME_CREATION_DATE,FIELDNAME_HEADLINE,FIELDNAME_TEASER,FIELDNAME_BODY};
	private final static String[] ACTION_ARRAY = new String[] {"edit","delete"};

	XMLNamespace idegaXMLNameSpace = new XMLNamespace("http://xmlns.idega.com/block/article/xml");
	private String folderLocation = null;

	
	/**
	 * Default constructor.
	 */
	public ArticleItemBean() {
		clear();
	}
	
	public String[] getContentFieldNames(){
		return ATTRIBUTE_ARRAY;
	}
	
	public String[] getToolbarActions(){
		return ACTION_ARRAY;
	}
	
	public String getHeadline() { return (String)getValue(FIELDNAME_HEADLINE); }
	public String getTeaser() { return (String)getValue(FIELDNAME_TEASER); }
	public String getBody() { return (String)getValue(FIELDNAME_BODY); }
	public String getAuthor() { return (String)getValue(FIELDNAME_AUTHOR); }
	public String getSource() { return (String)getValue(FIELDNAME_SOURCE); }
	public String getComment() { return (String)getValue(FIELDNAME_COMMENT); }
	public List getImages() { return getItemFields(FIELDNAME_IMAGES); }
	public String getFilename() { return (String)getValue(FIELDNAME_FILENAME); }
	
	
	/**
	 * This method returns the path of the article without the language part:
	 * ../cms/article/YYYY/MM/YYYYMMDD-HHmm.article while ContentItem#getResourcePath()
	 * returns the path to the actual resource ../cms/article/YYYY/MM/YYYYMMDD-HHmm.article/lang.xml
	 * 
	 * @see ContentItem#getResourcePath()
	 * @return
	 */
	public synchronized String getArticlePath() {
		String resourcePath = getArticleResourcePath();
		int index = resourcePath.indexOf("."+ARTICLE_FILENAME_SCOPE);
		if(index>-1){
			String articlePath = resourcePath.substring(0,index+ARTICLE_FILENAME_SCOPE.length()+1);
			System.out.println("Article path returned: "+articlePath);
			return articlePath;
		} else {
			Logger log = Logger.getLogger(this.getClass().toString());
			log.warning("Resource path for article '"+resourcePath+"' does not contain article filename scope '."+ARTICLE_FILENAME_SCOPE+"'.  The resource path is returned unchanged.");
			return resourcePath;  
		}
	}
	
	public String getArticleResourcePath() {
		makesureStandardFolderisCreated();
		String path = (String)getValue(FIELDNAME_RESOURCE_PATH);
		if(path==null){
			try {
				IWUserContext iwc = getIWUserContext();
				IWSlideService service = (IWSlideService)IBOLookup.getServiceInstance(iwc.getApplicationContext(),IWSlideService.class);
				path = createArticleResourcePath(service);
				setArticleResourcePath(path);
			}
			catch (IBOLookupException e) {
				e.printStackTrace();
			}
			catch (UnavailableIWContext e) {
				e.printStackTrace();
			}
		}
		return path;
	}
	
	protected IWUserContext getIWUserContext(){
		IWContext iwc = IWContext.getInstance();
		return iwc;
	}
	
	/**
	 * <p>
	 * TODO tryggvil describe method getIWSlideService
	 * </p>
	 * @param iwuc
	 * @return
	 */
	private IWSlideService getIWSlideService(IWUserContext iwuc) {
		try {
			IWSlideService slideService = (IWSlideService) IBOLookup.getServiceInstance(iwuc.getApplicationContext(),IWSlideService.class);
			return slideService;
		}
		catch (IBOLookupException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * <p>
	 * Makes sure the standard folder /cms/articles is created and that it has correct permissions.
	 * </p>
	 */
	private void makesureStandardFolderisCreated() {
		IWUserContext iwuc = getIWUserContext();
		IWSlideService slideService = getIWSlideService(iwuc);
		String contentFolderPath = ArticleUtil.getContentRootPath();
		String articlePath = ArticleUtil.getArticleRootPath();
		
		
		try {
			//first make the folder:
			slideService.createAllFoldersInPathAsRoot(articlePath);
			
			WebdavResource resource = slideService.getWebdavResourceAuthenticatedAsRoot(contentFolderPath);
			AccessControlList aclList = slideService.getAccessControlList(contentFolderPath);
			AuthenticationBusiness authBusiness = ((IWSlideServiceBean)slideService).getAuthenticationBusiness();
			
			String editorRoleUri = authBusiness.getRoleURI(StandardRoles.ROLE_KEY_EDITOR);
			Ace editorAce = new Ace(editorRoleUri);
			editorAce.addPrivilege(Privilege.READ);
			editorAce.addPrivilege(Privilege.WRITE);
			//editorAce.addPrivilege(Privilege.READ);
			//editorAce.setInherited(true);
			AccessControlEntry editorEntry = new AccessControlEntry(editorAce,AccessControlEntry.PRINCIPAL_TYPE_ROLE);
			aclList.add(editorEntry);
			
			String authorRoleUri = authBusiness.getRoleURI(StandardRoles.ROLE_KEY_AUTHOR);
			Ace authorAce = new Ace(authorRoleUri);
			authorAce.addPrivilege(Privilege.READ);
			authorAce.addPrivilege(Privilege.WRITE);
			//editorAce.addPrivilege(Privilege.READ);
			//editorAce.setInherited(true);
			AccessControlEntry authorEntry = new AccessControlEntry(authorAce,AccessControlEntry.PRINCIPAL_TYPE_ROLE);
			aclList.add(authorEntry);
			
			
			slideService.storeAccessControlList(aclList);
			
			//debug:
			aclList = slideService.getAccessControlList(contentFolderPath);
			
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}


	public void setArticleResourcePath(String path) {
		if(path!=null){
			if(path.indexOf("."+ARTICLE_FILENAME_SCOPE) < 0 || !path.endsWith(ARTICLE_SUFFIX)){
				throw new RuntimeException("["+this.getClass().getName()+"]: setArticleResourcePath("+path+") path is not valid article path!");
			}
		}
		setResourcePath(path);
	}
	
	
	
	public String getFolderLocation() {
		if(folderLocation!=null) return folderLocation;
		
		String articlePath = getResourcePath();
		if(articlePath!=null){
			return ContentUtil.getParentPath(ContentUtil.getParentPath(articlePath));
		} else {
			return null;
		}
	}
	
	public void setFolderLocation(String path) {
		folderLocation = path;
	}
	
	public String getContentLanguage() { return (String)getValue(FIELDNAME_CONTENT_LANGUAGE); }
	public String getLanguageChange() { return (String)getValue(FIELDNAME_LANGUAGE_CHANGE); }

	public void setHeadline(String s) { setValue(FIELDNAME_HEADLINE, s); }
	public void setHeadline(Object o) { setValue(FIELDNAME_HEADLINE, o.toString()); } 
	public void setTeaser(String s) { setValue(FIELDNAME_TEASER, s); } 
	public void setBody(String body) {
		setValue(FIELDNAME_BODY, body);
//		if (null != articleIn) {
////			System.out.println("ArticleIn = "+articleIn);
//			//Use JTidy to clean up the html
//			Tidy tidy = new Tidy();
//			tidy.setXHTML(true);
//			tidy.setXmlOut(true);
//			ByteArrayInputStream bais = new ByteArrayInputStream(articleIn.getBytes());
//			ByteArrayOutputStream baos = new ByteArrayOutputStream();
//			tidy.parse(bais, baos);
//			String articleOut = baos.toString();
////			System.out.println("ArticleOut = "+articleOut);
//			setValue(FIELDNAME_BODY, articleOut);
////			setValue(FIELDNAME_BODY, articleIn);
//		}
//		else {
//			setValue(FIELDNAME_BODY, null);
//		}
	} 
	public void setAuthor(String s) { setValue(FIELDNAME_AUTHOR, s); } 
	public void setSource(String s) { setValue(FIELDNAME_SOURCE, s); }
	public void setComment(String s) { setValue(FIELDNAME_COMMENT, s); }
	public void setImages(List l) { setItemFields(FIELDNAME_IMAGES, l); }
	public void setFilename(String l) { setValue(FIELDNAME_FILENAME, l); }
//	public void setFolderLocation(String l) { setValue(FIELDNAME_FOLDER_LOCATION, l); }
	public void setContentLanguage(String s) { setValue(FIELDNAME_CONTENT_LANGUAGE, s); }
	public void setLanguageChange(String s) { setValue(FIELDNAME_LANGUAGE_CHANGE, s); }

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
		setFolderLocation(null);
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
			tryPublish();
		}catch(ArticleStoreException e) {
			addErrorKey(KEY_ERROR_ON_STORE);
			e.printStackTrace();
			return new Boolean(false);
		}
		return new Boolean(true);
	}
	
	/**
	 * <p>
	 * Try to publish the article if it's
	 * </p>
	 */
	protected void tryPublish(){
		//TODO: Implement publishing here:
		setPublished();
	}
	
	protected void setPublished(){
		setStatus(ContentItemCase.STATUS_PUBLISHED);
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
	protected String createArticleResourcePath(IWSlideService service){
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
	 * @throws XMLException
	 */
	public String getAsXML() throws IOException, XMLException {

		XMLParser builder = new XMLParser();
		
		XMLElement bodyElement = null;
		
		prettifyBody();
		
		String bodyString = getBody();
		if(bodyString != null && !bodyString.trim().equals("")){
			XMLDocument bodyDoc = builder.parse(new ByteArrayInputStream(bodyString.getBytes("UTF-8")));
			bodyElement = bodyDoc.getRootElement();
		}
		

		
		
		XMLElement root = new XMLElement("article",idegaXMLNameSpace);
		XMLElement contentLanguage = new XMLElement(FIELDNAME_CONTENT_LANGUAGE,idegaXMLNameSpace).setText(getContentLanguage());
		XMLElement headline = new XMLElement(FIELDNAME_HEADLINE,idegaXMLNameSpace).setText(getHeadline());
		XMLElement teaser = new XMLElement(FIELDNAME_TEASER,idegaXMLNameSpace).setText(getTeaser());
		XMLElement author = new XMLElement(FIELDNAME_AUTHOR,idegaXMLNameSpace).setText(getAuthor());
		XMLElement source = new XMLElement(FIELDNAME_SOURCE,idegaXMLNameSpace).setText(getSource());
		XMLElement articleComment = new XMLElement("article_comment",idegaXMLNameSpace).setText(getComment());

		XMLElement body = new XMLElement(FIELDNAME_BODY,idegaXMLNameSpace);
		if(bodyElement != null){
			body.addContent(bodyElement);
		}

		root.addContent(contentLanguage);
		root.addContent(headline);
		root.addContent(teaser);
		root.addContent(body);
		root.addContent(author);
		root.addContent(source);
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
//		if (getBody().trim().equals("")) {
//			addErrorKey(KEY_ERROR_BODY_EMPTY);
//			storeOk = false;
//		}
		
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
		if(storeOk){
			try {
				IWUserContext iwuc = IWContext.getInstance();
				IWSlideSession session = (IWSlideSession)IBOLookup.getSessionInstance(iwuc,IWSlideSession.class);
				WebdavRootResource rootResource = session.getWebdavRootResource();
	
				//Setting the path for creating new file/creating localized version/updating existing file
				String filePath=getResourcePath();
				String articleFolderPath=getArticlePath();
				if(articleFolderPath!=null) {
					filePath=articleFolderPath+"/"+getArticleName();
				}else {
					filePath=getArticleResourcePath();
					articleFolderPath = getArticlePath();
				}
		
				boolean hadToCreate = session.createAllFoldersInPath(articleFolderPath);
	
				if(hadToCreate){
					String fixedFolderURL = session.getURI(articleFolderPath);
					rootResource.proppatchMethod(fixedFolderURL,PROPERTY_CONTENT_TYPE,"LocalizedFile",true);
				}
				else{
					rootResource.proppatchMethod(articleFolderPath,PROPERTY_CONTENT_TYPE,"LocalizedFile",true);
				}
				
				
				String article = getAsXML();
				
				ByteArrayInputStream utf8stream = new ByteArrayInputStream(article.getBytes("UTF-8"));
				
	//			System.out.println(article);
				
				//Conflict fix: uri for creating but path for updating
				//Note! This is a patch to what seems to be a bug in WebDav
				//Apparently in verion below works in some cases and the other in other cases.
				//Seems to be connected to creating files in folders created in same tomcat session or similar
				//not quite clear...
				
				if(rootResource.putMethod(filePath,utf8stream)){
					rootResource.proppatchMethod(filePath,PROPERTY_CONTENT_TYPE,ARTICLE_FILENAME_SCOPE,true);
				}
				else{
					utf8stream = new ByteArrayInputStream(article.getBytes("UTF-8"));
					String fixedURL = session.getURI(filePath);
					rootResource.putMethod(fixedURL,utf8stream);
					rootResource.proppatchMethod(fixedURL,PROPERTY_CONTENT_TYPE,ARTICLE_FILENAME_SCOPE,true);
				}
				
				rootResource.close();
				try {
					//load(filePath);
					ArticleItemBean newBean = new ArticleItemBean();
					newBean.load(filePath);
				}
				catch (Exception e) {
					//storeOk = false;
					//e.printStackTrace();
					throw new ArticleStoreException(e.getMessage());
				}
	
			}
			catch (IOException e1) {
				storeOk = false;
				e1.printStackTrace();
			}
			catch (XMLException e) {
				storeOk = false;
				e.printStackTrace();
			}
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
	 * 
	 */
	protected void prettifyBody() {
		String body = getBody();
		if(body!=null){
//		System.out.println("ArticleIn = "+articleIn);
			//Use JTidy to clean up the html
			Tidy tidy = new Tidy();
			tidy.setXHTML(true);
			tidy.setXmlOut(true);
			tidy.setCharEncoding(Configuration.UTF8);
			ByteArrayInputStream bais;
			try {
				bais = new ByteArrayInputStream(body.getBytes("UTF-8"));
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				
				tidy.parse(bais, baos);
				body = baos.toString();
			}
			catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			
//			System.out.println("ArticleOut = "+articleOut);
			setBody(body);
		}
	}

	/**
	 * Loads an xml file specified by the webdav resource
	 * The beans atributes are then set according to the information in the XML file
	 */
	public boolean load(WebdavExtendedResource webdavResource) throws IOException {
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
			}
			if(theArticle!=null && !theArticle.isCollection()){
				setArticleResourcePath(theArticle.getPath());
				bodyDoc = builder.parse(new ByteArrayInputStream(theArticle.getMethodDataAsString().getBytes("UTF-8")));
			} else {
				bodyDoc = null;
			}
		} catch (XMLException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
		if(bodyDoc!=null){
			XMLElement rootElement = bodyDoc.getRootElement();
	
			try {
				XMLElement language  = rootElement.getChild(FIELDNAME_CONTENT_LANGUAGE,idegaXMLNameSpace);
				if(language != null){
					setContentLanguage(language.getText());
				} else {
					setContentLanguage(null);
				}
			}catch(Exception e) {	
				setContentLanguage(null);
			}
			try {
				XMLElement headline = rootElement.getChild(FIELDNAME_HEADLINE,idegaXMLNameSpace);
				if(headline != null){
					setHeadline(headline.getText());
				} else {
					setHeadline("");
				}
			}catch(Exception e) {		//Nullpointer could occur if field isn't used
				e.printStackTrace();
				setHeadline("");
			}
			try {
				XMLElement teaser = rootElement.getChild(FIELDNAME_TEASER,idegaXMLNameSpace);
				if(teaser != null){
					setTeaser(teaser.getText());
				} else {
					setTeaser("");
				}
			}catch(Exception e) {		//Nullpointer could occur if field isn't used
				e.printStackTrace();
				setTeaser("");
			}
			try {
				XMLElement author = rootElement.getChild(FIELDNAME_AUTHOR,idegaXMLNameSpace);
				if(author != null){
					setAuthor(author.getText());
				} else {
					setAuthor("");
				}
			}catch(Exception e) {		//Nullpointer could occur if field isn't used
				e.printStackTrace();
				setAuthor("");
			}
	
			//Parse out the body
			try {
				XMLNamespace htmlNamespace = new XMLNamespace("http://www.w3.org/1999/xhtml");
				XMLElement bodyElement = rootElement.getChild(FIELDNAME_BODY,idegaXMLNameSpace);
				XMLElement htmlElement = bodyElement.getChild("html",htmlNamespace);
				XMLElement htmlBodyElement = htmlElement.getChild("body",htmlNamespace);
				
				String bodyValue = htmlBodyElement.getContentAsString();
				setBody(bodyValue);
			}catch(Exception e) {		//Nullpointer could occur if field isn't used
	//			e.printStackTrace();
				Logger log = Logger.getLogger(this.getClass().toString());
				log.warning("Body of article is empty");
				setBody("");
			}
			
			try {
				XMLElement source = rootElement.getChild(FIELDNAME_SOURCE,idegaXMLNameSpace);
				if(source != null){
					setSource(source.getText());
				} else {
					setSource("");
				}
			}catch(Exception e) {		//Nullpointer could occur if field isn't used
				setSource("");
			}
			try {
				XMLElement comment = rootElement.getChild(FIELDNAME_ARTICLE_COMMENT,idegaXMLNameSpace);
				if(comment != null){
					setComment(comment.getText());
				} else {
					setComment("");
				}
			}catch(Exception e) {		//Nullpointer could occur if field isn't used
				e.printStackTrace();
				setComment("");
			}
			
		} else {
			//article not found
			Logger log = Logger.getLogger(this.getClass().toString());
			log.warning("Article xml file was not found");
			setRendered(false);
			return false;
		}
		return true;
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
	
	/* (non-Javadoc)
	 * @see com.idega.data.IDOEntity#setDatasource(java.lang.String)
	 */
	public void setDatasource(String datasource) {
		// TODO Auto-generated method stub
	}	
}