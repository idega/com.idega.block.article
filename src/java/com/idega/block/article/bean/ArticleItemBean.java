/*
 * $Id: ArticleItemBean.java,v 1.12 2005/01/13 14:02:04 joakim Exp $
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
import java.util.Iterator;
import java.util.List;
import org.apache.webdav.lib.WebdavResource;
import org.apache.xmlbeans.XmlException;
import com.idega.business.IBOLookup;
import com.idega.data.IDOStoreException;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWUserContext;
import com.idega.presentation.IWContext;
import com.idega.slide.business.IWSlideService;
import com.idega.slide.business.IWSlideSession;
import com.idega.slide.util.WebdavRootResource;
import com.idega.webface.test.bean.ContentItemBean;
import com.idega.webface.test.bean.ContentItemField;
import com.idega.webface.test.bean.ContentItemFieldBean;
import com.idega.xmlns.block.article.document.ArticleDocument;

/**
 * Bean for idegaWeb article content items.   
 * <p>
 * Last modified: $Date: 2005/01/13 14:02:04 $ by $Author: joakim $
 *
 * @author Anders Lindman
 * @version $Revision: 1.12 $
 */

public class ArticleItemBean extends ContentItemBean implements Serializable {
	
	public final static String KP = "error_"; // Key prefix
	
	public final static String KEY_ERROR_HEADLINE_EMPTY = KP + "headline_empty";
	public final static String KEY_ERROR_BODY_EMPTY = KP + "body_empty";
	public final static String KEY_ERROR_PUBLISHED_FROM_DATE_EMPTY = KP + "published_from_date_empty";	
	
	private boolean _isUpdated = false;
	private List _errorKeys = null;
	
	/**
	 * Default constructor.
	 */
	public ArticleItemBean() {
		clear();
	}
		
	public String getHeadline() { return getItemField("headline").getValue(); }
	public String getTeaser() { return getItemField("teaser").getValue(); }
	public String getBody() { return getItemField("body").getValue(); }
	public String getAuthor() { return getItemField("author").getValue(); }
	public String getSource() { return getItemField("source").getValue(); }
	public String getComment() { return getItemField("comment").getValue(); }
	public List getImages() { return getItemFields("image"); }
	public List getAttachments() { return getItemFields("attachment"); }
	public String getMainCategory() { return getItemField("main_category").getValue(); }
	public List getRelatedContentItems() { return getItemFields("related_items"); }

	public void setHeadline(String s) { setItemField("headline", s); } 
	public void setHeadline(Object o) { setItemField("headline", o.toString()); } 
	public void setTeaser(String s) { setItemField("teaser", s); } 
	public void setBody(String s) { setItemField("body", s); } 
	public void setAuthor(String s) { setItemField("author", s); } 
	public void setSource(String s) { setItemField("source", s); }
	public void setComment(String s) { setItemField("comment", s); }
	public void setImages(List l) { setItemFields("image", l); }
	public void setAttachment(List l) { setItemFields("attachment", l); }
	public void setMainCategory(String l) { setItemField("main_category", l); }
	public void setRelatedContentItems(List l) { setItemFields("related_items", l); }

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
	 * Adds a related content item to this article item.
	 */
	public void addRelatedContentItem(Integer contentItemId) {
		List l = getRelatedContentItems();
		if (l == null) {
			l = new ArrayList();
		}
		ContentItemField field = new ContentItemFieldBean();
		field.setValue(contentItemId.toString());
//		field.setFieldType(contentType);
		field.setName("Content item..." + contentItemId);
		field.setOrderNo(l.size());
		l.add(field);
		setRelatedContentItems(l);
	}
	
	/**
	 * Removes the related content item with the specified item id from this article item.
	 */
	public void removeRelatedContentItem(Integer contentItemId) {
		String itemId = contentItemId.toString();
		try {
			List l = getRelatedContentItems();
			for (Iterator iter = l.iterator(); iter.hasNext();) {
				ContentItemField field = (ContentItemField) iter.next();
				if (field.getValue().equals(itemId)) {
					l.remove(field);
					break;
				}
			}
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

		String filename = getHeadline();
		if(null==filename || filename.length()==0) {
			filename = "empty";
		}
/*	    
	    File path = new File(getMainCategory());
	    if(!path.exists()) {
	    	path.mkdirs();
	    }
*/
		try {
			IWUserContext iwuc = IWContext.getInstance();
			IWApplicationContext iwac = iwuc.getApplicationContext();
			
			IWSlideSession session = (IWSlideSession)IBOLookup.getSessionInstance(iwuc,IWSlideSession.class);
			IWSlideService service = (IWSlideService)IBOLookup.getServiceInstance(iwac,IWSlideService.class);
		
			System.out.println("webdavServerURL = "+service.getWebdavServerURL());
			System.out.println("webdavServletURL = "+getWebdavServletURL(iwuc));
			System.out.println("Main category = "+getMainCategory());


			WebdavRootResource rootResource = session.getWebdavRootResource();
//			boolean success = webdavResource.mkcolMethod("/servlet/webdav/files/test/test2");
			String filePath = service.getWebdavServerURI()+getMainCategory();
			boolean success = rootResource.mkcolMethod(filePath);
			System.out.println("success "+success);
//			boolean success = rootResource.mkcolMethod(getWebdavServletURL(iwuc)+getMainCategory());
			System.out.println(filePath);
			success = rootResource.putMethod(getWebdavServletURL(iwuc)+getMainCategory()+"/"+filename+".xml",articleDoc.toString());
			System.out.println("success "+success);

//			String webdavServletURL = getWebdavServletURL(iwuc)+"/"+getMainCategory();
//			System.out.println("webdavServletURL = "+webdavServletURL);
//			System.out.println("webdavServerURL = "+service.getWebdavServerURL());
//			WebdavResource webdavResource = session.getWebdavResource(session.getWebdavServerURL().toString());
//			WebdavResource webdavResource = session.getWebdavResource("http://localhost:8080/servlet/webdav");
//			WebdavResource webdavResource = new WebdavResource("http://localhost:8080/servlet/webdav/");
			
//			WebdavFile webdavFile = session.getWebdavFile();
//			WebdavFile path = new WebdavFile(webdavFile, getMainCategory());
//			path.mkdirs();

//			webdavResource = session.getWebdavResource(getWebdavServletURL(iwuc)+getMainCategory()+"/"+filename+".xml");
//			WebdavResource webdavResource = session.getWebdavResource("http://localhost:8080/servlet/webdav/"+filename+".xml");

//			IWSlideService iwss = new IWSlideService();
//			HttpURL root = new HttpURL("http://localhost:8080/servlet/webdav/files/");
//			WebdavResource webdavResource = new WebdavResource("http://localhost:8080/servlet/webdav/files/"+filename+".xml");
//			WebdavFile webdavFile = session.getWebdavFile();
//			webdavResource.putMethod(new File(filename+".xml"));

			/*
			HttpURL root = new HttpURL("http://localhost:8080/servlet/webdav/files/"+filename+".xml");
			root.setUserinfo("root","root");
			WebdavFile webdavFile = new WebdavFile(webdavResource.getHttpURL());
	    	webdavResource.close();
			*/
//			HttpURL root = new HttpURL("http://localhost:8080/servlet/webdav/files/"+filename+".xml", "root", "root");
//			root.setUserinfo("root","root");
			
//			WebdavFile webdavFile = new WebdavFile("http://localhost:8080/servlet/webdav/files/"+filename+".xml", "root", "root");
//			webdavFile.createNewFile();
//			if(!webdavFile.exists()) {
//			}
			
//			articleDoc.save(new File(getMainCategory()+"/"+filename+".xml"));
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
	
    public String getWebdavServletURL(IWUserContext iwuc){
    	String root = iwuc.getApplicationContext().getIWMainApplication().getApplicationContextURI();
    	if(null!=root && root.length()>1) {
    		return root + WEBDAV_SERVLET_URI;
    	}else {
    		return WEBDAV_SERVLET_URI;
    	}
	}
    
//    protected static final String WEBDAV_SERVLET_URI = "/servlet/webdav";
    protected static final String WEBDAV_SERVLET_URI = "/content";
    
	/**
	 * Loads all xml files in the given folder
	 * @param folder
	 * @return List containing ArticleItemBean
	 * @throws XmlException
	 * @throws IOException
	 */
	public void load(String path) throws XmlException, IOException{
		System.out.println("Attempting to load path "+path);
		IWUserContext iwuc = IWContext.getInstance();
		IWApplicationContext iwac = iwuc.getApplicationContext();
		
		IWSlideSession session = (IWSlideSession)IBOLookup.getSessionInstance(iwuc,IWSlideSession.class);
		IWSlideService service = (IWSlideService)IBOLookup.getServiceInstance(iwac,IWSlideService.class);

		WebdavResource webdavResource = session.getWebdavResource(path);
		
		ArticleDocument articleDoc;
		
		articleDoc = ArticleDocument.Factory.parse(webdavResource.getMethodDataAsString());
		
	    ArticleDocument.Article article =  articleDoc.getArticle();
//	    ArticleItemBean articleBean = new ArticleItemBean();
	    setHeadline(article.getHeadline());
	    setBody(article.getBody());
	    setTeaser(article.getTeaser());
	    setAuthor(article.getAuthor());
	    setSource(article.getSource());
	    setComment(article.getComment());
		String category = "";
		int lastSlash = path.lastIndexOf('/');
		if(lastSlash>0) {
			category = path.substring(0,lastSlash);
		}
	    setMainCategory(category);
	    
//		System.out.println("loaded "+getBody());
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
//Need to create this	    setMainCategory(article.getMainCategory());
	    
//		System.out.println("loaded "+getBody());
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
