/*
 * $Id: ArticleItemBean.java,v 1.19 2005/02/14 15:13:19 gummi Exp $
 *
 * Copyright (C) 2004 Idega. All Rights Reserved.
 *
 * This software is the proprietary information of Idega.
 * Use is subject to license terms.
 *
 */
package com.idega.block.article.bean;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.apache.xmlbeans.XmlException;
import com.idega.business.IBOLookup;
import com.idega.content.bean.ContentItemBean;
import com.idega.content.bean.ContentItemField;
import com.idega.content.bean.ContentItemFieldBean;
import com.idega.content.bean.ContentItem;
import com.idega.data.IDOStoreException;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWUserContext;
import com.idega.presentation.IWContext;
import com.idega.slide.business.IWSlideService;
import com.idega.slide.business.IWSlideSession;
import com.idega.slide.util.WebdavExtendedResource;
import com.idega.slide.util.WebdavRootResource;
import com.idega.util.IWTimestamp;
import com.idega.xmlns.block.article.document.ArticleDocument;

/**
 * Bean for idegaWeb article content items.   
 * <p>
 * Last modified: $Date: 2005/02/14 15:13:19 $ by $Author: gummi $
 *
 * @author Anders Lindman
 * @version $Revision: 1.19 $
 */

public class ArticleItemBean extends ContentItemBean implements Serializable, ContentItem {
	
	public final static String KP = "error_"; // Key prefix
	
	public final static String KEY_ERROR_HEADLINE_EMPTY = KP + "headline_empty";
	public final static String KEY_ERROR_BODY_EMPTY = KP + "body_empty";
	public final static String KEY_ERROR_PUBLISHED_FROM_DATE_EMPTY = KP + "published_from_date_empty";
	public final static String ARTICLE_SUFFIX = ".article";
	
	private boolean _isUpdated = false;
	private List _errorKeys = null;
	
	public final static String FIELDNAME_AUTHOR = "author";
	public final static String FIELDNAME_HEADLINE = "headline";
	public final static String FIELDNAME_TEASER = "teaser";
	public final static String FIELDNAME_BODY = "body";
	public final static String FIELDNAME_SOURCE = "source";
	public final static String FIELDNAME_COMMENT = "comment";
	public final static String FIELDNAME_IMAGES = "images";
	public final static String FIELDNAME_FOLDER_LOCATION = "folder_location";
	
	public final static String ARTICLE_FILENAME_SCOPE = "article";
	
	private final static String[] ATTRIBUTE_ARRAY = new String[] {FIELDNAME_AUTHOR,FIELDNAME_CREATION_DATE,FIELDNAME_HEADLINE,FIELDNAME_TEASER,FIELDNAME_BODY};

	
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
	public String getFolderLocation() { return (String)getValue(FIELDNAME_FOLDER_LOCATION); }

	public void setHeadline(String s) { setValue(FIELDNAME_HEADLINE, s); } 
	public void setHeadline(Object o) { setValue(FIELDNAME_HEADLINE, o.toString()); } 
	public void setTeaser(String s) { setValue(FIELDNAME_TEASER, s); } 
	public void setBody(String s) { setValue(FIELDNAME_BODY, s); } 
	public void setAuthor(String s) { setValue(FIELDNAME_AUTHOR, s); } 
	public void setSource(String s) { setValue(FIELDNAME_SOURCE, s); }
	public void setComment(String s) { setValue(FIELDNAME_COMMENT, s); }
	public void setImages(List l) { setItemFields(FIELDNAME_IMAGES, l); }
	public void setFolderLocation(String l) { setValue(FIELDNAME_FOLDER_LOCATION, l); }

	public boolean isUpdated() { return _isUpdated; }
	public void setUpdated(boolean b) { _isUpdated = b; }
	public void setUpdated(Boolean b) { _isUpdated = b.booleanValue(); }
	
	/**
	 * Clears all all attributes for this bean. 
	 */
	public void clear() {
		super.clear();
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
	
	protected String getArticlePath(String filename){
		return getFolderLocation()+"/"+filename+ARTICLE_SUFFIX;
	}
	
	private String createFileName(IWSlideService service) {
		StringBuffer name = new StringBuffer("");
		name.append(service.createUniqueFileName(ARTICLE_FILENAME_SCOPE));
		IWUserContext iwuc = IWContext.getInstance();
		name.append("-").append(iwuc.getCurrentLocale().getLanguage());
		System.out.println("FileName is "+name);
		return name.toString();
	}
	
	/**
	 * This is a temporary holder for the Slide implementation
	 * This should be replace as soon as Slide is working
	 */
	public void store() throws IDOStoreException{
//	public Boolean store() {
		boolean storeOk = true;
		clearErrorKeys();

		ArticleDocument articleDoc = ArticleDocument.Factory.newInstance();
	    
	    ArticleDocument.Article article =  articleDoc.addNewArticle();
	    
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
		
		article.setHeadline(getHeadline());
		article.setBody(getBody());
		article.setTeaser(getTeaser());
		article.setAuthor(getAuthor());
		article.setSource(getSource());
		article.setComment(getComment());
//	    article.setImage(getImages());
//	    article.setAttachment(getAttachments());
//	    article.setRelatedItems(getRelatedContentItems());
//Need to create	    article.setCategory(getCategory());

		
		
//		String filename = getHeadline();
//		if(null==filename || filename.length()==0) {
//			filename = "empty";
//		}
/*	    
	    File path = new File(getFolder());
	    if(!path.exists()) {
	    	path.mkdirs();
	    }
*/
		try {
			IWUserContext iwuc = IWContext.getInstance();
			IWApplicationContext iwac = iwuc.getApplicationContext();
			
			IWSlideSession session = (IWSlideSession)IBOLookup.getSessionInstance(iwuc,IWSlideSession.class);
			IWSlideService service = (IWSlideService)IBOLookup.getServiceInstance(iwac,IWSlideService.class);
		
			WebdavRootResource rootResource = session.getWebdavRootResource();
			String uri = service.getWebdavServerURI();
			String folderLoaction = getFolderLocation();
//			String filePath = service.getWebdavServerURI()+getFolderLocation();
//			boolean success = rootResource.mkcolMethod(filePath);
//			createPath(getFolderLocation());
			IWSlideService slideService = (IWSlideService)IBOLookup.getServiceInstance(iwac,IWSlideService.class);
			slideService.createAllFoldersInPath(getFolderLocation());
			
			System.out.println("URI = "+uri);
			System.out.println("Folder location = "+folderLoaction);
			
//			System.out.println("success "+success);
//			success = 
			
			String filename = createFileName(service);
			
			rootResource.putMethod(session.getURI(getArticlePath(filename)),articleDoc.toString());
			try {
				load(getArticlePath(filename));
			}
			catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

//			System.out.println("success "+success);
//			String webdavServletURL = getWebdavServletURL(iwuc)+"/"+getFolder();
//			System.out.println("webdavServletURL = "+webdavServletURL);
//			System.out.println("webdavServerURL = "+service.getWebdavServerURL());
//			WebdavResource webdavResource = session.getWebdavResource(session.getWebdavServerURL().toString());
//			WebdavResource webdavResource = session.getWebdavResource("http://localhost:8080/servlet/webdav");
//			WebdavResource webdavResource = new WebdavResource("http://localhost:8080/servlet/webdav/");
			
//			WebdavFile webdavFile = session.getWebdavFile();
//			WebdavFile path = new WebdavFile(webdavFile, getFolder());
//			path.mkdirs();

//			webdavResource = session.getWebdavResource(getWebdavServletURL(iwuc)+getFolder()+"/"+filename+ARTICLE_SUFFIX);
//			WebdavResource webdavResource = session.getWebdavResource("http://localhost:8080/servlet/webdav/"+filename+ARTICLE_SUFFIX);

//			IWSlideService iwss = new IWSlideService();
//			HttpURL root = new HttpURL("http://localhost:8080/servlet/webdav/files/");
//			WebdavResource webdavResource = new WebdavResource("http://localhost:8080/servlet/webdav/files/"+filename+ARTICLE_SUFFIX);
//			WebdavFile webdavFile = session.getWebdavFile();
//			webdavResource.putMethod(new File(filename+ARTICLE_SUFFIX));

			/*
			HttpURL root = new HttpURL("http://localhost:8080/servlet/webdav/files/"+filename+ARTICLE_SUFFIX);
			root.setUserinfo("root","root");
			WebdavFile webdavFile = new WebdavFile(webdavResource.getHttpURL());
	    	webdavResource.close();
			*/
//			HttpURL root = new HttpURL("http://localhost:8080/servlet/webdav/files/"+filename+ARTICLE_SUFFIX, "root", "root");
//			root.setUserinfo("root","root");
			
//			WebdavFile webdavFile = new WebdavFile("http://localhost:8080/servlet/webdav/files/"+filename+ARTICLE_SUFFIX, "root", "root");
//			webdavFile.createNewFile();
//			if(!webdavFile.exists()) {
//			}
			
//			articleDoc.save(new File(getFolder()+"/"+filename+ARTICLE_SUFFIX));
//	    	articleDoc.save(webdavFile);
//	    	webdavFile.close();
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
	 * @param folder
	 * @return List containing ArticleItemBean
	 * @throws XmlException
	 * @throws IOException
	 */
	public void load(WebdavExtendedResource webdavResource) throws XmlException, IOException {
	
		ArticleDocument articleDoc;
		
		articleDoc = ArticleDocument.Factory.parse(webdavResource.getMethodDataAsString());
		
	    ArticleDocument.Article article =  articleDoc.getArticle();
	    setHeadline(article.getHeadline());
	    setBody(article.getBody());
	    setTeaser(article.getTeaser());
	    setAuthor(article.getAuthor());
	    setSource(article.getSource());
	    setComment(article.getComment());
		String folder = webdavResource.getParentPath();
	    setFolderLocation(folder);
	    
	}
	
	public void loadOld(File file) throws XmlException, IOException{
		ArticleDocument articleDoc;
		
		articleDoc = ArticleDocument.Factory.parse(file);
		
	    ArticleDocument.Article article =  articleDoc.getArticle();
//	    ArticleItemBean articleBean = new ArticleItemBean();
	    setHeadline(article.getHeadline());
	    setBody(article.getBody());
	    setTeaser(article.getTeaser());
	    setAuthor(article.getAuthor());
	    setSource(article.getSource());
	    setComment(article.getComment());
//Need to create this	    setFolder(article.getFolder());
	    
//		System.out.println("loaded "+getBody());
	}

	/* (non-Javadoc)
	 * @see com.idega.content.bean.ContentItem#getContentItemPrefix()
	 */
	public String getContentItemPrefix() {
		return "article_";
	}
	
//	public List getAll() throws XmlException, IOException{
//		List list = new ArrayList();
//		
//		File root = new File("/Test/article/");
//		
//		File[] articleFile = root.listFiles();
//		
//		for(int i=0;i<articleFile.length;i++){
//			System.out.println("Attempting to load "+articleFile[i].toString());
//				list.add(load(articleFile[i]));
//		}
//		
//		return list;
//	}
	
	/**
	 * Stores this article item to the database. 
	 */
/*	public Boolean store() {
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
		if (getRequestedStatus() != null && getRequestedStatus().equals(ContentItemCase.STATUS_PUBLISHED)) {
			if (getCase().getPublishedFromDate() == null) {
				addErrorKey(KEY_ERROR_PUBLISHED_FROM_DATE_EMPTY);
				storeOk = false;
			}
		}
		
		if (storeOk) {
			if (getRequestedStatus() != null) {
				setStatus(getRequestedStatus());
				setRequestedStatus(null);
			}
		}
		
		return new Boolean(storeOk);
	}
*/
}
