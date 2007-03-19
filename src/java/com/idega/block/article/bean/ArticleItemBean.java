/*
 * $Id: ArticleItemBean.java,v 1.71 2007/03/19 08:43:31 valdas Exp $
 *
 * Copyright (C) 2004-2005 Idega. All Rights Reserved.
 *
 * This software is the proprietary information of Idega.
 * Use is subject to license terms.
 *
 */
package com.idega.block.article.bean;

import java.io.IOException;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ValueChangeEvent;
import javax.faces.event.ValueChangeListener;
import org.apache.webdav.lib.Ace;
import org.apache.webdav.lib.Privilege;
import org.apache.webdav.lib.PropertyName;
import org.apache.webdav.lib.WebdavResources;
import com.idega.block.article.ArticleCacher;
import com.idega.block.article.business.ArticleConstants;
import com.idega.block.article.business.ArticleUtil;
import com.idega.content.bean.ContentItem;
import com.idega.content.bean.ContentItemBean;
import com.idega.content.bean.ContentItemCase;
import com.idega.content.themes.helpers.ThemesConstants;
import com.idega.core.accesscontrol.business.StandardRoles;
import com.idega.data.IDOStoreException;
import com.idega.idegaweb.IWMainApplication;
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
import com.idega.util.StringHandler;
import com.idega.xml.XMLException;

/**
 * <p>
 * This is a JSF managed bean that manages each article instance and delegates 
 * all calls to the correct localized instance.
 * <p>
 * Last modified: $Date: 2007/03/19 08:43:31 $ by $Author: valdas $
 *
 * @author Anders Lindman,<a href="mailto:tryggvi@idega.com">Tryggvi Larusson</a>
 * @version $Revision: 1.71 $
 */
public class ArticleItemBean extends ContentItemBean implements Serializable, ContentItem, ValueChangeListener {
	
	/**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 4514851565086272678L;
	final static String ARTICLE_FILENAME_SCOPE = "article";
	private final static String ARTICLE_FILE_SUFFIX = ".xml";
	
	public final static String CONTENT_TYPE = "ContentType";
	public static final PropertyName PROPERTY_CONTENT_TYPE = new PropertyName("IW:",CONTENT_TYPE);
	
	private ArticleLocalizedItemBean localizedArticle;
	private String baseFolderLocation;
	private String languageChange;
	private boolean allowFallbackToSystemLanguage=false;
	private String resourcePath;
	private boolean avilableInRequestedLanguage=false;
	
	public ArticleLocalizedItemBean getLocalizedArticle(){
		if(this.localizedArticle==null){
			this.localizedArticle=new ArticleLocalizedItemBean();
			this.localizedArticle.setLocale(getLocale());
			this.localizedArticle.setArticleItem(this);
		}
		else{
			String thisLanguage = getLanguage();
			String fileLanguage = this.localizedArticle.getLanguage();
			if(!thisLanguage.equals(fileLanguage)){
				throw new RuntimeException("Locale inconsistency for article: "+getResourcePath()+" and localizedArticle: "+this.localizedArticle.getResourcePath());
			}
		}
		return this.localizedArticle;
	}

	/* (non-Javadoc)
	 * @see com.idega.block.article.bean.ArticleLocalizedItemBean#addImage(byte[], java.lang.String)
	 */
	public void addImage(byte[] imageData, String contentType) {
		getLocalizedArticle().addImage(imageData, contentType);
	}

	/* (non-Javadoc)
	 * @see com.idega.block.article.bean.ArticleLocalizedItemBean#clear()
	 */
	public void clear() {
		//isDummyArticleSet = false;
		getLocalizedArticle().clear();
		this.resourcePath=null;
		this.avilableInRequestedLanguage=false;
	}
	/* (non-Javadoc)
	 * @see com.idega.block.article.bean.ArticleLocalizedItemBean#getAsXML()
	 */
	public String getAsXML() throws IOException, XMLException {
		return getLocalizedArticle().getAsXML();
	}

	/* (non-Javadoc)
	 * @see com.idega.block.article.bean.ArticleLocalizedItemBean#getAuthor()
	 */
	public String getAuthor() {
		if (isDummyArticle()) {
			return ArticleConstants.EMPTY;
		}
		return getLocalizedArticle().getAuthor();
	}

	/* 
	 * Method is modified to resolve if dummy article is being edited
	 */
	public String getBody() {
		if (isDummyArticle()) {
			return ArticleConstants.EMPTY;
		}
		return getLocalizedArticle().getBody();
	}

	/* (non-Javadoc)
	 * @see com.idega.block.article.bean.ArticleLocalizedItemBean#getComment()
	 */
	public String getComment() {
		return getLocalizedArticle().getComment();
	}
	
	public String getLinkToComments() {
		return getLocalizedArticle().getLinkToComments();
	}

	/* (non-Javadoc)
	 * @see com.idega.block.article.bean.ArticleLocalizedItemBean#getContentFieldNames()
	 */
	public String[] getContentFieldNames() {
		return getLocalizedArticle().getContentFieldNames();
	}

	/* (non-Javadoc)
	 * @see com.idega.block.article.bean.ArticleLocalizedItemBean#getContentItemPrefix()
	 */
	public String getContentItemPrefix() {
		return getLocalizedArticle().getContentItemPrefix();
	}

	/* (non-Javadoc)
	 * @see com.idega.block.article.bean.ArticleLocalizedItemBean#getContentLanguage()
	 */
	public String getContentLanguage() {
		return getLocalizedArticle().getContentLanguage();
	}

	/* (non-Javadoc)
	 * @see com.idega.block.article.bean.ArticleLocalizedItemBean#getHeadline()
	 */
	public String getHeadline() {
		if (isDummyArticle()) {
			return ArticleConstants.EMPTY;
		}
		return getLocalizedArticle().getHeadline();
	}

	/* (non-Javadoc)
	 * @see com.idega.block.article.bean.ArticleLocalizedItemBean#getImages()
	 */
	public List getImages() {
		return getLocalizedArticle().getImages();
	}

	/* (non-Javadoc)
	 * @see com.idega.block.article.bean.ArticleLocalizedItemBean#getSource()
	 */
	public String getSource() {
		return getLocalizedArticle().getSource();
	}

	/* (non-Javadoc)
	 * @see com.idega.block.article.bean.ArticleLocalizedItemBean#getTeaser()
	 */
	public String getTeaser() {
		if (isDummyArticle()) {
			return ArticleConstants.EMPTY;
		}
		return getLocalizedArticle().getTeaser();
	}

	/* (non-Javadoc)
	 * @see com.idega.block.article.bean.ArticleLocalizedItemBean#getToolbarActions()
	 */
	public String[] getToolbarActions() {
		return getLocalizedArticle().getToolbarActions();
	}

	/* (non-Javadoc)
	 * @see com.idega.block.article.bean.ArticleLocalizedItemBean#isUpdated()
	 */
	public boolean isUpdated() {
		return getLocalizedArticle().isUpdated();
	}

	/* (non-Javadoc)
	 * @see com.idega.block.article.bean.ArticleLocalizedItemBean#prettifyBody()
	 */
	protected void prettifyBody() {
		getLocalizedArticle().prettifyBody();
	}

	/* (non-Javadoc)
	 * @see com.idega.block.article.bean.ArticleLocalizedItemBean#removeImage(java.lang.Integer)
	 */
	public void removeImage(Integer imageNumber) {
		getLocalizedArticle().removeImage(imageNumber);
	}

	/* (non-Javadoc)
	 * @see com.idega.block.article.bean.ArticleLocalizedItemBean#setAuthor(java.lang.String)
	 */
	public void setAuthor(String s) {
		getLocalizedArticle().setAuthor(s);
	}

	/* (non-Javadoc)
	 * @see com.idega.block.article.bean.ArticleLocalizedItemBean#setBody(java.lang.String)
	 */
	public void setBody(String body) {
		getLocalizedArticle().setBody(body);
	}

	/* (non-Javadoc)
	 * @see com.idega.block.article.bean.ArticleLocalizedItemBean#setComment(java.lang.String)
	 */
	public void setComment(String s) {
		getLocalizedArticle().setComment(s);
	}
	
	public void setLinkToComments(String linkToComments) {
		getLocalizedArticle().setLinkToComments(linkToComments);
	}

	/* (non-Javadoc)
	 * @see com.idega.block.article.bean.ArticleLocalizedItemBean#setHeadline(java.lang.Object)
	 */
	public void setHeadline(Object o) {
		getLocalizedArticle().setHeadline(o);
	}

	/* (non-Javadoc)
	 * @see com.idega.block.article.bean.ArticleLocalizedItemBean#setHeadline(java.lang.String)
	 */
	public void setHeadline(String s) {
		getLocalizedArticle().setHeadline(s);
	}

	/* (non-Javadoc)
	 * @see com.idega.block.article.bean.ArticleLocalizedItemBean#setImages(java.util.List)
	 */
	public void setImages(List l) {
		getLocalizedArticle().setImages(l);
	}

	/* (non-Javadoc)
	 * @see com.idega.block.article.bean.ArticleLocalizedItemBean#setPublished()
	 */
	protected void setPublished() {
		getLocalizedArticle().setPublished();
	}

	/* (non-Javadoc)
	 * @see com.idega.block.article.bean.ArticleLocalizedItemBean#setSource(java.lang.String)
	 */
	public void setSource(String s) {
		getLocalizedArticle().setSource(s);
	}

	/* (non-Javadoc)
	 * @see com.idega.block.article.bean.ArticleLocalizedItemBean#setTeaser(java.lang.String)
	 */
	public void setTeaser(String s) {
		getLocalizedArticle().setTeaser(s);
	}

	/* (non-Javadoc)
	 * @see com.idega.block.article.bean.ArticleLocalizedItemBean#setUpdated(boolean)
	 */
	public void setUpdated(boolean b) {
		getLocalizedArticle().setUpdated(b);
	}

	/* (non-Javadoc)
	 * @see com.idega.block.article.bean.ArticleLocalizedItemBean#setUpdated(java.lang.Boolean)
	 */
	public void setUpdated(Boolean b) {
		getLocalizedArticle().setUpdated(b);
	}

	/* (non-Javadoc)
	 * @see com.idega.block.article.bean.ArticleLocalizedItemBean#store()
	 */
	public void store() throws IDOStoreException {
		
		//First validate headline and body:
		
		if ( (getHeadline()==null) || (getHeadline().trim().equals("")) ) {
			ArticleStoreException exception = new ArticleStoreException();
			exception.setErrorKey(ArticleStoreException.KEY_ERROR_HEADLINE_EMPTY);
			throw exception;
		}
		if (getBody().trim().equals("")) {
			ArticleStoreException exception = new ArticleStoreException();
			exception.setErrorKey(ArticleStoreException.KEY_ERROR_BODY_EMPTY);
			throw exception;
		}
		
		try {
			IWUserContext iwuc = IWContext.getInstance();
			IWSlideSession session = getIWSlideSession(iwuc);
			WebdavRootResource rootResource = session.getWebdavRootResource();

			//Setting the path for creating new file/creating localized version/updating existing file
			String articleFolderPath = getResourcePath();
	
			boolean hadToCreate = session.createAllFoldersInPath(articleFolderPath);

			if(hadToCreate){
				String fixedFolderURL = session.getURI(articleFolderPath);
				rootResource.proppatchMethod(fixedFolderURL,PROPERTY_CONTENT_TYPE,"LocalizedFile",true);
			}
			else{
				rootResource.proppatchMethod(articleFolderPath,PROPERTY_CONTENT_TYPE,"LocalizedFile",true);
			}
			
			rootResource.close();
			getLocalizedArticle().store();
			
			ArticleCacher cacher = ArticleCacher.getInstance(IWMainApplication.getDefaultIWMainApplication());
			cacher.getCacheMap().clear();
			
		}
		catch(ArticleStoreException ase){
			throw ase;
		}
		catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	
	/* (non-Javadoc)
	 * @see com.idega.block.article.bean.ArticleLocalizedItemBean#tryPublish()
	 */
	protected void tryPublish() {
		getLocalizedArticle().tryPublish();
	}

	/* (non-Javadoc)
	 * @see com.idega.content.bean.ContentItemBean#getName()
	 */
	public String getName() {
		return getLocalizedArticle().getName();
	}

	/* (non-Javadoc)
	 * @see com.idega.content.bean.ContentItemBean#getDescription()
	 */
	public String getDescription() {
		return getLocalizedArticle().getDescription();
	}

	/* (non-Javadoc)
	 * @see com.idega.content.bean.ContentItemBean#getItemType()
	 */
	public String getItemType() {
		return getLocalizedArticle().getItemType();
	}

	/* (non-Javadoc)
	 * @see com.idega.content.bean.ContentItemBean#getCreatedByUserId()
	 */
	public int getCreatedByUserId() {
		return getLocalizedArticle().getCreatedByUserId();
	}

	/* (non-Javadoc)
	 * @see com.idega.content.bean.ContentItemBean#setName(java.lang.String)
	 */
	public void setName(String s) {
		getLocalizedArticle().setName(s);
	}

	/* (non-Javadoc)
	 * @see com.idega.content.bean.ContentItemBean#setDescription(java.lang.String)
	 */
	public void setDescription(String s) {
		getLocalizedArticle().setDescription(s);
	}

	/* (non-Javadoc)
	 * @see com.idega.content.bean.ContentItemBean#setItemType(java.lang.String)
	 */
	public void setItemType(String s) {
		getLocalizedArticle().setItemType(s);
	}

	/* (non-Javadoc)
	 * @see com.idega.content.bean.ContentItemBean#setCreatedByUserId(int)
	 */
	public void setCreatedByUserId(int id) {
		getLocalizedArticle().setCreatedByUserId(id);
	}

	/* (non-Javadoc)
	 * @see com.idega.content.bean.ContentItemBean#getLocales()
	 */
	public Map getLocales() {
		return getLocalizedArticle().getLocales();
	}

	/* (non-Javadoc)
	 * @see com.idega.content.bean.ContentItemBean#getPendingLocaleId()
	 */
	public String getPendingLocaleId() {
		return getLocalizedArticle().getPendingLocaleId();
	}

	/* (non-Javadoc)
	 * @see com.idega.content.bean.ContentItemBean#setPendingLocaleId(java.lang.String)
	 */
	public void setPendingLocaleId(String localeId) {
		getLocalizedArticle().setPendingLocaleId(localeId);
	}

	/* (non-Javadoc)
	 * @see com.idega.content.bean.ContentItemBean#getRequestedStatus()
	 */
	public String getRequestedStatus() {
		return getLocalizedArticle().getRequestedStatus();
	}

	/* (non-Javadoc)
	 * @see com.idega.content.bean.ContentItemBean#setRequestedStatus(java.lang.String)
	 */
	public void setRequestedStatus(String requestedStatus) {
		getLocalizedArticle().setRequestedStatus(requestedStatus);
	}

	/* (non-Javadoc)
	 * @see com.idega.content.bean.ContentItemBean#getAttachments()
	 */
	public List getAttachments() {
		return getLocalizedArticle().getAttachments();
	}

	/* (non-Javadoc)
	 * @see com.idega.content.bean.ContentItemBean#setAttachment(java.util.List)
	 */
	public void setAttachment(List l) {
		getLocalizedArticle().setAttachment(l);
	}

	/* (non-Javadoc)
	 * @see com.idega.content.bean.ContentItemBean#getCase()
	 */
	public ContentItemCase getCase() {
		return getLocalizedArticle().getCase();
	}

	/* (non-Javadoc)
	 * @see com.idega.content.bean.ContentItemBean#setCase(com.idega.content.bean.ContentItemCase)
	 */
	public void setCase(ContentItemCase caseBean) {
		getLocalizedArticle().setCase(caseBean);
	}

	/* (non-Javadoc)
	 * @see com.idega.content.bean.ContentItemBean#getStatus()
	 */
	public String getStatus() {
		return getLocalizedArticle().getStatus();
	}

	/* (non-Javadoc)
	 * @see com.idega.content.bean.ContentItemBean#setStatus(java.lang.String)
	 */
	public void setStatus(String status) {
		getLocalizedArticle().setStatus(status);
	}

	/* (non-Javadoc)
	 * @see com.idega.content.bean.ContentItemBean#updateLocale()
	 */
	public void updateLocale() {
		getLocalizedArticle().updateLocale();
	}

	/* (non-Javadoc)
	 * @see com.idega.content.bean.ContentItemBean#getCreationDate()
	 */
	public Timestamp getCreationDate() {
		return getLocalizedArticle().getCreationDate();
	}

	/* (non-Javadoc)
	 * @see com.idega.content.bean.ContentItemBean#getVersionName()
	 */
	public String getVersionName() {
		return getLocalizedArticle().getVersionName();
	}

	/* (non-Javadoc)
	 * @see com.idega.content.bean.ContentItemBean#setVersionName(java.lang.String)
	 */
	public void setVersionName(String name) {
		getLocalizedArticle().setVersionName(name);
	}

	/* (non-Javadoc)
	 * @see com.idega.content.bean.ContentItemBean#getRendered()
	 */
	public Boolean getRendered() {
		return getLocalizedArticle().getRendered();
	}

	/* (non-Javadoc)
	 * @see com.idega.content.bean.ContentItemBean#setRendered(boolean)
	 */
	public void setRendered(boolean render) {
		getLocalizedArticle().setRendered(render);
	}

	/* (non-Javadoc)
	 * @see com.idega.content.bean.ContentItemBean#setRendered(java.lang.Boolean)
	 */
	public void setRendered(Boolean render) {
		getLocalizedArticle().setRendered(render);
	}
//
//	/* (non-Javadoc)
//	 * @see com.idega.content.bean.ContentItemBean#getDeleted()
//	 */
//	public boolean getDeleted() {
//		return getLocalizedArticle().getDeleted();
//	}
//
//	/* (non-Javadoc)
//	 * @see com.idega.content.bean.ContentItemBean#getDeletedByUserId()
//	 */
//	public int getDeletedByUserId() {
//		return getLocalizedArticle().getDeletedByUserId();
//	}
//
//	/* (non-Javadoc)
//	 * @see com.idega.content.bean.ContentItemBean#getDeletedWhen()
//	 */
//	public Timestamp getDeletedWhen() {
//		return getLocalizedArticle().getDeletedWhen();
//	}
//
//	/* (non-Javadoc)
//	 * @see com.idega.content.bean.ContentItemBean#getFileSize()
//	 */
//	public Integer getFileSize() {
//		return getLocalizedArticle().getFileSize();
//	}
//
//	/* (non-Javadoc)
//	 * @see com.idega.content.bean.ContentItemBean#getFileValue()
//	 */
//	public InputStream getFileValue() {
//		return getLocalizedArticle().getFileValue();
//	}
//
//	/* (non-Javadoc)
//	 * @see com.idega.content.bean.ContentItemBean#getFileValueForWrite()
//	 */
//	public OutputStream getFileValueForWrite() {
//		return getLocalizedArticle().getFileValueForWrite();
//	}
//
//	/* (non-Javadoc)
//	 * @see com.idega.content.bean.ContentItemBean#getICLocale()
//	 */
//	public ICLocale getICLocale() {
//		return getLocalizedArticle().getICLocale();
//	}
//
//	/* (non-Javadoc)
//	 * @see com.idega.content.bean.ContentItemBean#getLanguage()
//	 */
//	public int getLanguage() {
//		return getLocalizedArticle().getLanguage();
//	}
//
//	/* (non-Javadoc)
//	 * @see com.idega.content.bean.ContentItemBean#getMimeType()
//	 */
//	public String getMimeType() {
//		return getLocalizedArticle().getMimeType();
//	}
//
//	/* (non-Javadoc)
//	 * @see com.idega.content.bean.ContentItemBean#getModificationDate()
//	 */
//	public Timestamp getModificationDate() {
//		return getLocalizedArticle().getModificationDate();
//	}
//
//	/* (non-Javadoc)
//	 * @see com.idega.content.bean.ContentItemBean#initializeAttributes()
//	 */
//	public void initializeAttributes() {
//		getLocalizedArticle().initializeAttributes();
//	}
//
//	/* (non-Javadoc)
//	 * @see com.idega.content.bean.ContentItemBean#isLeaf()
//	 */
//	public boolean isLeaf() {
//		return getLocalizedArticle().isLeaf();
//	}
//
//	/* (non-Javadoc)
//	 * @see com.idega.content.bean.ContentItemBean#setCreationDate(java.sql.Timestamp)
//	 */
//	public void setCreationDate(Timestamp date) {
//		getLocalizedArticle().setCreationDate(date);
//	}
//
//	/* (non-Javadoc)
//	 * @see com.idega.content.bean.ContentItemBean#setResourcePath(java.lang.String)
//	 */
//	public void setResourcePath(String path) {
//		getLocalizedArticle().setResourcePath(path);
//	}
//
//	/* (non-Javadoc)
//	 * @see com.idega.content.bean.ContentItemBean#setDeleted(boolean)
//	 */
//	public void setDeleted(boolean p0) {
//		getLocalizedArticle().setDeleted(p0);
//	}
//
//	/* (non-Javadoc)
//	 * @see com.idega.content.bean.ContentItemBean#setFileSize(java.lang.Integer)
//	 */
//	public void setFileSize(Integer p0) {
//		
//		getLocalizedArticle().setFileSize(p0);
//	}
//
//	/* (non-Javadoc)
//	 * @see com.idega.content.bean.ContentItemBean#setFileSize(int)
//	 */
//	public void setFileSize(int p0) {
//		
//		getLocalizedArticle().setFileSize(p0);
//	}
//
//	/* (non-Javadoc)
//	 * @see com.idega.content.bean.ContentItemBean#setFileValue(java.io.InputStream)
//	 */
//	public void setFileValue(InputStream p0) {
//		
//		getLocalizedArticle().setFileValue(p0);
//	}
//
//	/* (non-Javadoc)
//	 * @see com.idega.content.bean.ContentItemBean#setLanguage(int)
//	 */
//	public void setLanguage(int p0) {
//		
//		getLocalizedArticle().setLanguage(p0);
//	}
//
//	/* (non-Javadoc)
//	 * @see com.idega.content.bean.ContentItemBean#setLocale()
//	 */
//	public void setLocale() {
//		
//		getLocalizedArticle().setLocale();
//	}
//
//	/* (non-Javadoc)
//	 * @see com.idega.content.bean.ContentItemBean#setMimeType(java.lang.String)
//	 */
//	public void setMimeType(String p0) {
//		
//		getLocalizedArticle().setMimeType(p0);
//	}
//
//	/* (non-Javadoc)
//	 * @see com.idega.content.bean.ContentItemBean#setModificationDate(java.sql.Timestamp)
//	 */
//	public void setModificationDate(Timestamp p0) {
//		
//		getLocalizedArticle().setModificationDate(p0);
//	}
//
//	/* (non-Javadoc)
//	 * @see com.idega.content.bean.ContentItemBean#superDelete()
//	 */
//	public void superDelete() throws SQLException {
//		
//		getLocalizedArticle().superDelete();
//	}
//
//	/* (non-Javadoc)
//	 * @see com.idega.content.bean.ContentItemBean#unDelete(boolean)
//	 */
//	public void unDelete(boolean p0) throws SQLException {
//		
//		getLocalizedArticle().unDelete(p0);
//	}
//
//	/* (non-Javadoc)
//	 * @see com.idega.content.bean.ContentItemBean#delete()
//	 */
//	public void delete() throws SQLException {
//		
//		getLocalizedArticle().delete();
//	}
//
//	/* (non-Javadoc)
//	 * @see com.idega.content.bean.ContentItemBean#isFolder()
//	 */
//	public boolean isFolder() {
//		
//		return getLocalizedArticle().isFolder();
//	}
//
//	/* (non-Javadoc)
//	 * @see com.idega.content.bean.ContentItemBean#isEmpty()
//	 */
//	public boolean isEmpty() {
//		
//		return getLocalizedArticle().isEmpty();
//	}
//
//	/* (non-Javadoc)
//	 * @see com.idega.content.bean.ContentItemBean#getLocalizationKey()
//	 */
//	public String getLocalizationKey() {
//		
//		return getLocalizedArticle().getLocalizationKey();
//	}
//
//	/* (non-Javadoc)
//	 * @see com.idega.content.bean.ContentItemBean#setLocalizationKey(java.lang.String)
//	 */
//	public void setLocalizationKey(String key) {
//		
//		getLocalizedArticle().setLocalizationKey(key);
//	}
//
//	/* (non-Javadoc)
//	 * @see com.idega.content.bean.ContentItemBean#getEntityDefinition()
//	 */
//	public IDOEntityDefinition getEntityDefinition() {
//		
//		return getLocalizedArticle().getEntityDefinition();
//	}
//
//	/* (non-Javadoc)
//	 * @see com.idega.content.bean.ContentItemBean#decode(java.lang.String)
//	 */
//	public Object decode(String pkString) {
//		
//		return getLocalizedArticle().decode(pkString);
//	}
//
//	/* (non-Javadoc)
//	 * @see com.idega.content.bean.ContentItemBean#decode(java.lang.String[])
//	 */
//	public Collection decode(String[] pkString) {
//		
//		return getLocalizedArticle().decode(pkString);
//	}
//
//	/* (non-Javadoc)
//	 * @see com.idega.content.bean.ContentItemBean#getEJBLocalHome()
//	 */
//	public EJBLocalHome getEJBLocalHome() throws EJBException {
//		
//		return getLocalizedArticle().getEJBLocalHome();
//	}
//
//	/* (non-Javadoc)
//	 * @see com.idega.content.bean.ContentItemBean#getPrimaryKey()
//	 */
//	public Object getPrimaryKey() throws EJBException {
//		
//		return getLocalizedArticle().getPrimaryKey();
//	}
//
//	/* (non-Javadoc)
//	 * @see com.idega.content.bean.ContentItemBean#remove()
//	 */
//	public void remove() throws RemoveException, EJBException {
//		
//		getLocalizedArticle().remove();
//	}
//
//	/* (non-Javadoc)
//	 * @see com.idega.content.bean.ContentItemBean#isIdentical(javax.ejb.EJBLocalObject)
//	 */
//	public boolean isIdentical(EJBLocalObject arg0) throws EJBException {
//		
//		return getLocalizedArticle().isIdentical(arg0);
//	}
//
//	/* (non-Javadoc)
//	 * @see com.idega.content.bean.ContentItemBean#compareTo(java.lang.Object)
//	 */
//	public int compareTo(Object o) {
//		
//		return getLocalizedArticle().compareTo(o);
//	}
//
//	/* (non-Javadoc)
//	 * @see com.idega.content.bean.ContentItemBean#addChild(com.idega.data.TreeableEntity)
//	 */
//	public void addChild(TreeableEntity p0) throws SQLException {
//		
//		getLocalizedArticle().addChild(p0);
//	}
//
//	/* (non-Javadoc)
//	 * @see com.idega.content.bean.ContentItemBean#getChildrenIterator(java.lang.String)
//	 */
//	public Iterator getChildrenIterator(String p0) {
//		
//		return getLocalizedArticle().getChildrenIterator(p0);
//	}
//
//	/* (non-Javadoc)
//	 * @see com.idega.content.bean.ContentItemBean#getChildrenIterator(java.lang.String, boolean)
//	 */
//	public Iterator getChildrenIterator(String p0, boolean p1) {
//		
//		return getLocalizedArticle().getChildrenIterator(p0, p1);
//	}
//
//	/* (non-Javadoc)
//	 * @see com.idega.content.bean.ContentItemBean#getIndex(com.idega.core.data.ICTreeNode)
//	 */
//	public int getIndex(ICTreeNode p0) {
//		
//		return getLocalizedArticle().getIndex(p0);
//	}
//
//	/* (non-Javadoc)
//	 * @see com.idega.content.bean.ContentItemBean#getParentEntity()
//	 */
//	public TreeableEntity getParentEntity() {
//		
//		return getLocalizedArticle().getParentEntity();
//	}
//
//	/* (non-Javadoc)
//	 * @see com.idega.content.bean.ContentItemBean#getTreeRelationshipChildColumnName(com.idega.data.TreeableEntity)
//	 */
//	public String getTreeRelationshipChildColumnName(TreeableEntity p0) {
//		
//		return getLocalizedArticle().getTreeRelationshipChildColumnName(p0);
//	}
//
//	/* (non-Javadoc)
//	 * @see com.idega.content.bean.ContentItemBean#getTreeRelationshipTableName(com.idega.data.TreeableEntity)
//	 */
//	public String getTreeRelationshipTableName(TreeableEntity p0) {
//		
//		return getLocalizedArticle().getTreeRelationshipTableName(p0);
//	}
//
//	/* (non-Javadoc)
//	 * @see com.idega.content.bean.ContentItemBean#moveChildrenFrom(com.idega.data.TreeableEntity)
//	 */
//	public void moveChildrenFrom(TreeableEntity p0) throws SQLException {
//		
//		getLocalizedArticle().moveChildrenFrom(p0);
//	}
//
//	/* (non-Javadoc)
//	 * @see com.idega.content.bean.ContentItemBean#removeChild(com.idega.data.TreeableEntity)
//	 */
//	public void removeChild(TreeableEntity p0) throws SQLException {
//		
//		getLocalizedArticle().removeChild(p0);
//	}
//
//	/* (non-Javadoc)
//	 * @see com.idega.content.bean.ContentItemBean#leafsFirst()
//	 */
//	public boolean leafsFirst() {
//		
//		return getLocalizedArticle().leafsFirst();
//	}
//
//	/* (non-Javadoc)
//	 * @see com.idega.content.bean.ContentItemBean#sortLeafs()
//	 */
//	public boolean sortLeafs() {
//		
//		return getLocalizedArticle().sortLeafs();
//	}
//
//	/* (non-Javadoc)
//	 * @see com.idega.content.bean.ContentItemBean#setLeafsFirst(boolean)
//	 */
//	public void setLeafsFirst(boolean b) {
//		
//		getLocalizedArticle().setLeafsFirst(b);
//	}
//
//	/* (non-Javadoc)
//	 * @see com.idega.content.bean.ContentItemBean#setToSortLeafs(boolean)
//	 */
//	public void setToSortLeafs(boolean b) {
//		
//		getLocalizedArticle().setToSortLeafs(b);
//	}
//
//	/* (non-Javadoc)
//	 * @see com.idega.content.bean.ContentItemBean#getChildren()
//	 */
//	public Collection getChildren() {
//		
//		return getLocalizedArticle().getChildren();
//	}
//
//	/* (non-Javadoc)
//	 * @see com.idega.content.bean.ContentItemBean#getChildrenIterator()
//	 */
//	public Iterator getChildrenIterator() {
//		
//		return getLocalizedArticle().getChildrenIterator();
//	}
//
//	/* (non-Javadoc)
//	 * @see com.idega.content.bean.ContentItemBean#getAllowsChildren()
//	 */
//	public boolean getAllowsChildren() {
//		
//		return getLocalizedArticle().getAllowsChildren();
//	}
//
//	/* (non-Javadoc)
//	 * @see com.idega.content.bean.ContentItemBean#getChildAtIndex(int)
//	 */
//	public ICTreeNode getChildAtIndex(int childIndex) {
//		
//		return getLocalizedArticle().getChildAtIndex(childIndex);
//	}
//
//	/* (non-Javadoc)
//	 * @see com.idega.content.bean.ContentItemBean#getChildCount()
//	 */
//	public int getChildCount() {
//		
//		return getLocalizedArticle().getChildCount();
//	}
//
//	/* (non-Javadoc)
//	 * @see com.idega.content.bean.ContentItemBean#getParentNode()
//	 */
//	public ICTreeNode getParentNode() {
//		
//		return getLocalizedArticle().getParentNode();
//	}
//
//	/* (non-Javadoc)
//	 * @see com.idega.content.bean.ContentItemBean#getNodeName()
//	 */
//	public String getNodeName() {
//		
//		return getLocalizedArticle().getNodeName();
//	}
//
//	/* (non-Javadoc)
//	 * @see com.idega.content.bean.ContentItemBean#getNodeName(java.util.Locale)
//	 */
//	public String getNodeName(Locale locale) {
//		
//		return getLocalizedArticle().getNodeName(locale);
//	}
//
//	/* (non-Javadoc)
//	 * @see com.idega.content.bean.ContentItemBean#getNodeName(java.util.Locale, com.idega.idegaweb.IWApplicationContext)
//	 */
//	public String getNodeName(Locale locale, IWApplicationContext iwac) {
//		
//		return getLocalizedArticle().getNodeName(locale, iwac);
//	}
//
//	/* (non-Javadoc)
//	 * @see com.idega.content.bean.ContentItemBean#getNodeID()
//	 */
//	public int getNodeID() {
//		
//		return getLocalizedArticle().getNodeID();
//	}
//
//	/* (non-Javadoc)
//	 * @see com.idega.content.bean.ContentItemBean#getSiblingCount()
//	 */
//	public int getSiblingCount() {
//		
//		return getLocalizedArticle().getSiblingCount();
//	}
//
//	/* (non-Javadoc)
//	 * @see com.idega.content.bean.ContentItemBean#setMetaDataAttributes(java.util.Map)
//	 */
//	public void setMetaDataAttributes(Map map) {
//		
//		getLocalizedArticle().setMetaDataAttributes(map);
//	}
//
//	/* (non-Javadoc)
//	 * @see com.idega.content.bean.ContentItemBean#getMetaDataAttributes()
//	 */
//	public Map getMetaDataAttributes() {
//		
//		return getLocalizedArticle().getMetaDataAttributes();
//	}
//
//	/* (non-Javadoc)
//	 * @see com.idega.content.bean.ContentItemBean#getMetaDataTypes()
//	 */
//	public Map getMetaDataTypes() {
//		
//		return getLocalizedArticle().getMetaDataTypes();
//	}
//
//	/* (non-Javadoc)
//	 * @see com.idega.content.bean.ContentItemBean#setMetaData(java.lang.String, java.lang.String)
//	 */
//	public void setMetaData(String metaDataKey, String value) {
//		
//		getLocalizedArticle().setMetaData(metaDataKey, value);
//	}
//
//	/* (non-Javadoc)
//	 * @see com.idega.content.bean.ContentItemBean#setMetaData(java.lang.String, java.lang.String, java.lang.String)
//	 */
//	public void setMetaData(String metaDataKey, String value, String type) {
//		
//		getLocalizedArticle().setMetaData(metaDataKey, value, type);
//	}
//
//	/* (non-Javadoc)
//	 * @see com.idega.content.bean.ContentItemBean#getMetaData(java.lang.String)
//	 */
//	public String getMetaData(String metaDataKey) {
//		
//		return getLocalizedArticle().getMetaData(metaDataKey);
//	}
//
//	/* (non-Javadoc)
//	 * @see com.idega.content.bean.ContentItemBean#renameMetaData(java.lang.String, java.lang.String)
//	 */
//	public void renameMetaData(String oldKeyName, String newKeyName) {
//		
//		getLocalizedArticle().renameMetaData(oldKeyName, newKeyName);
//	}
//
//	/* (non-Javadoc)
//	 * @see com.idega.content.bean.ContentItemBean#renameMetaData(java.lang.String, java.lang.String, java.lang.String)
//	 */
//	public void renameMetaData(String oldKeyName, String newKeyName, String value) {
//		
//		getLocalizedArticle().renameMetaData(oldKeyName, newKeyName, value);
//	}
//
//	/* (non-Javadoc)
//	 * @see com.idega.content.bean.ContentItemBean#removeMetaData(java.lang.String)
//	 */
//	public boolean removeMetaData(String metaDataKey) {
//		
//		return getLocalizedArticle().removeMetaData(metaDataKey);
//	}
//
//	/* (non-Javadoc)
//	 * @see com.idega.content.bean.ContentItemBean#updateMetaData()
//	 */
//	public void updateMetaData() throws SQLException {
//		
//		getLocalizedArticle().updateMetaData();
//	}
//
//	/* (non-Javadoc)
//	 * @see com.idega.content.bean.ContentItemBean#getLocaleId()
//	 */
//	public int getLocaleId() {
//		
//		return getLocalizedArticle().getLocaleId();
//	}
//
//	/* (non-Javadoc)
//	 * @see com.idega.content.bean.ContentItemBean#getDatasource()
//	 */
//	public String getDatasource() {
//		
//		return getLocalizedArticle().getDatasource();
//	}
//
//	/* (non-Javadoc)
//	 * @see com.idega.content.bean.ContentItemBean#write(com.idega.io.serialization.ObjectWriter, com.idega.presentation.IWContext)
//	 */
//	public Object write(ObjectWriter writer, IWContext iwc) throws RemoteException {
//		
//		return getLocalizedArticle().write(writer, iwc);
//	}
//
//	/* (non-Javadoc)
//	 * @see com.idega.content.bean.ContentItemBean#read(com.idega.io.serialization.ObjectReader, com.idega.presentation.IWContext)
//	 */
//	public Object read(ObjectReader reader, IWContext iwc) throws RemoteException {
//		
//		return getLocalizedArticle().read(reader, iwc);
//	}

	/* (non-Javadoc)
	 * @see com.idega.content.bean.ContentItemBean#isAutoCreateResource()
	 */
	public boolean isAutoCreateResource() {
		
		return getLocalizedArticle().isAutoCreateResource();
	}

	/* (non-Javadoc)
	 * @see com.idega.content.bean.ContentItemBean#setAutoCreateResource(boolean)
	 */
	public void setAutoCreateResource(boolean autoCreateResource) {
		
		getLocalizedArticle().setAutoCreateResource(autoCreateResource);
	}

	/* (non-Javadoc)
	 * @see com.idega.content.bean.ContentItemBean#getExists()
	 */
	public boolean getExists() {
		
		return getLocalizedArticle().getExists();
	}

	/* (non-Javadoc)
	 * @see com.idega.content.bean.ContentItemBean#setExists(boolean)
	 */
	public void setExists(boolean exists) {
		
		getLocalizedArticle().setExists(exists);
	}
	
	
	public Object getValue(String fieldName) {
		return getLocalizedArticle().getValue(fieldName);
	}

	public void setValue(String fieldName, Object value) {
		getLocalizedArticle().setValue(fieldName,value);
	}
	
	/*
	 * If this is set this should return the base folder for storing articles.<br/> 
	 * By default it is '/files/cms/article'
	 */
	public String getBaseFolderLocation() {
		if(this.baseFolderLocation!=null){
			return this.baseFolderLocation;
		}
		else{
			return ArticleUtil.getArticleBaseFolderPath();
		}
		/*
		String articlePath = getResourcePath();
		if(articlePath!=null){
			return ContentUtil.getParentPath(ContentUtil.getParentPath(articlePath));
		}
		return null;*/
	}
	/*
	 * This sets the base folder for storing articles, 
	 * something like '/files/cms/article'
	 */
	public void setBaseFolderLocation(String path) {
		this.baseFolderLocation = path;
	}
	
	public Locale getLocale(){
		return super.getLocale();
	}
	
	public void setLocale(Locale locale){
		super.setLocale(locale);
	}
	
	public void setResourcePath(String resourcePath){
		//super.setResourcePath(resourcePath);
		this.resourcePath=resourcePath;
		updateLocalizedArticleBean();
	}
	
	protected void updateLocalizedArticleBean(){
		//isDummyArticleSet = false;
		String resourcePath = getResourcePath();
		String localizedResourcePath = calculateLocalizedResourcePath(resourcePath);
		getLocalizedArticle().setResourcePath(localizedResourcePath);
	}
	
	/**
	 * <p>
	 * Creates the resourcePath for the localized file (.../myarticle.article/en.xml)
	 * </p>
	 * @param resourcePath
	 * @return
	 */
	private String calculateLocalizedResourcePath(String resourcePath,String language) {
		if(!resourcePath.endsWith(StringHandler.SLASH)){
			resourcePath+=StringHandler.SLASH;
		}
		resourcePath += getArticleDefaultLocalizedFileName(language);
		return resourcePath;
	}
	
	private String calculateLocalizedResourcePath(String resourcePath) {
		return calculateLocalizedResourcePath(resourcePath,getLanguage());
	}

	private String getArticleDefaultLocalizedFileName(String language){
		return language+ARTICLE_FILE_SUFFIX;
	}
	
	private String getArticleDefaultLocalizedResourcePath(){
		return getArticleDefaultLocalizedResourcePath(getLanguage());
	}
	
	private String getArticleDefaultLocalizedResourcePath(String language){
		String resourcePath = getResourcePath();
		return calculateLocalizedResourcePath(resourcePath);
	}

	/**
	 * This method returns the path of the article without the language part:
	 * ../cms/article/YYYY/MM/YYYYMMDD-HHmm.article while ContentItem#getResourcePath()
	 * returns the path to the actual resource ../cms/article/YYYY/MM/YYYYMMDD-HHmm.article/lang.xml
	 * 
	 * @see ContentItem#getResourcePath()
	 * @return
	 */
	public String getResourcePath(){
		//String resourcePath = super.getResourcePath();
		String sResourcePath = this.resourcePath;
		if(sResourcePath==null){
			sResourcePath = createArticlePath();
			setResourcePath(sResourcePath);
		}
		return sResourcePath;
	}
	
	
	public synchronized String createArticlePath() {
		String resourcePath = getGeneratedArticleResourcePath();
		int index = resourcePath.indexOf("."+ARTICLE_FILENAME_SCOPE);
		if(index>-1){
			String articlePath = resourcePath.substring(0,index+ARTICLE_FILENAME_SCOPE.length()+1);
			System.out.println("Article path returned: "+articlePath);
			return articlePath;
		}
		Logger log = Logger.getLogger(this.getClass().toString());
		log.warning("Resource path for article '"+resourcePath+"' does not contain article filename scope '."+ARTICLE_FILENAME_SCOPE+"'.  The resource path is returned unchanged.");
		return resourcePath;
	}
	
	/**
	 * Returns the path to the actual resource (xml-file) ../cms/article/YYYY/MM/YYYYMMDD-HHmm.article/lang.xml
	 * 
	 * @see ContentItem#getResourcePath()
	 * @return
	 */
	private String getGeneratedArticleResourcePath() {
		makesureStandardFolderisCreated();
		String path = null;
		//String path = getResourcePath();
		//String path = (String)getValue(FIELDNAME_RESOURCE_PATH);
		//if(path==null){
			try {
				IWUserContext iwc = getIWUserContext();
				IWSlideService service = getIWSlideService(iwc);
				path =  generateArticleResourcePath(service);
				//setArticleResourcePath(path);
			}
			catch (UnavailableIWContext e) {
				e.printStackTrace();
			}
		//}
		return path;
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
	protected String generateArticleResourcePath(IWSlideService service){
		String baseFolderLocation = getBaseFolderLocation();
		/*if(null==folderLocation || "".equalsIgnoreCase(folderLocation)) {
			folderLocation=ArticleUtil.getDefaultArticleYearMonthPath();
		}*/
		String dateFolderLocation = ArticleUtil.getArticleYearMonthPath(baseFolderLocation);
		StringBuffer path = new StringBuffer(dateFolderLocation);
		path.append("/");
		path.append(service.createUniqueFileName(ARTICLE_FILENAME_SCOPE));
		path.append(".");
		path.append(ARTICLE_FILENAME_SCOPE);
		//path.append("/");
		//path.append(getArticleName());
		return path.toString();
	}
	
	/* (non-Javadoc)
	 * @see com.idega.block.article.bean.ArticleLocalizedItemBean#setLanguageChange(java.lang.String)
	 */
	public void setLanguageChange(String s) {
		this.languageChange=s;
	}
	
	/* (non-Javadoc)
	 * @see com.idega.block.article.bean.ArticleLocalizedItemBean#getLanguageChange()
	 */
	public String getLanguageChange() {
		return this.languageChange;
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
		String articlePath = ArticleUtil.getArticleBaseFolderPath();
		
		
		try {
			//first make the folder:
			slideService.createAllFoldersInPathAsRoot(articlePath);
			
			//was not used? slideService.getWebdavResourceAuthenticatedAsRoot(contentFolderPath);
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
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Loads the article (folder)
	 */
	protected boolean load(String path) throws IOException {
		//isDummyArticleSet = false;
		return super.load(path);
	}
	
	private void setLanguageFromFilePath(String filePath){
		String fileEnding = ARTICLE_FILE_SUFFIX;
		if(filePath.endsWith(fileEnding)){
			String filePathWithoutEnding = filePath.substring(0,filePath.lastIndexOf(fileEnding));
			int lastIndexofSlash = filePathWithoutEnding.lastIndexOf("/");
			String language = filePathWithoutEnding.substring(lastIndexofSlash,filePathWithoutEnding.length());
			setLanguage(language);
		}
		else{
			throw new RuntimeException("filePath: "+filePath+" does not contain expected file ending: "+fileEnding);
		}
	}
	
	/**
	 * Loads the article (folder)
	 */
	protected boolean load(WebdavExtendedResource webdavResource) throws IOException {
		//isDummyArticleSet = false;
		WebdavExtendedResource localizedArticleFile = null;
		//First check if the resource is a folder, as it should be
		if(webdavResource.isCollection()){
			//IWContext iwc = IWContext.getInstance();
			
			WebdavResources resources = webdavResource.getChildResources();
			String userLanguageArticleResourcePath = getArticleDefaultLocalizedResourcePath();
			if(resources.isThereResourceName(userLanguageArticleResourcePath)){ //the language that the user has selected
				localizedArticleFile = (WebdavExtendedResource) resources.getResource(userLanguageArticleResourcePath);
				setAvilableInSelectedLanguage();
			}
			else{
				//selected language not available:
				if(getAllowFallbackToSystemLanguage()){
					String systemLanguageArticleResourcePath = getArticleDefaultLocalizedResourcePath(getSystemLanguage());//getArticleName(iwc.getIWMainApplication().getDefaultLocale());
					if(resources.isThereResourceName(systemLanguageArticleResourcePath)){ //the language default in the system.
						localizedArticleFile = (WebdavExtendedResource) resources.getResource(systemLanguageArticleResourcePath);
						setAvilableInSelectedLanguage();
					}
					else{
						setNotAvilableInSelectedLanguage();
					}
				}
				else{
					setNotAvilableInSelectedLanguage();
				}
				
			}
				
			/*} else if(resources.isThereResourceName(systemLanguageArticleName)){ //the language that is default for the system
				articleFile = (WebdavExtendedResource) resources.getResource(systemLanguageArticleName);
			} else {  // take the first language
				Enumeration en = resources.getResources();
				if(en.hasMoreElements()){
					articleFile = (WebdavExtendedResource)en.nextElement();
				}
			}*/
		} else {
			//theArticle = webdavResource;
			//throw new RuntimeException(webdavResource+" is directory, but should be a file");
			String path = getResourcePath();
			setLanguageFromFilePath(path);
			String parentFolder = webdavResource.getParentPath();
			setResourcePath(parentFolder);
			return load(parentFolder);
			
			//WebdavResource folder = webdavResource.getParentPath();
		}
		if(localizedArticleFile!=null){
			return getLocalizedArticle().load(localizedArticleFile);
		}
		return false;
	}

	protected IWUserContext getIWUserContext(){
		IWContext iwc = IWContext.getInstance();
		return iwc;
	}
	
	/*public boolean getShowOnlyArticleInSelectedLanguage(){
		return true;
	}*/
	
	/**
	 * <p>
	 * Returns the language set as default in the System
	 * </p>
	 * @return
	 */
	public String getSystemLanguage(){
		Locale systemLocale = getSystemLocale();
		if(systemLocale!=null){
			return systemLocale.getLanguage();
		}
		return null;
	}
	
	public Locale getSystemLocale(){
		FacesContext context = FacesContext.getCurrentInstance();
		if(context!=null){
			return context.getApplication().getDefaultLocale();
		}
		return null;
	}
	
	public boolean getAllowFallbackToSystemLanguage(){
		return this.allowFallbackToSystemLanguage;
	}
	
	public void setAllowFallbackToSystemLanguage(boolean allow){
		this.allowFallbackToSystemLanguage=allow;
	}
	
	public void setAvilableInSelectedLanguage(){
		this.avilableInRequestedLanguage=true;
	}
	
	public void setNotAvilableInSelectedLanguage(){
		this.avilableInRequestedLanguage=false;
		if(!getAllowFallbackToSystemLanguage()){
			getLocalizedArticle().setHeadline("Article not avilable");
			getLocalizedArticle().setBody("The article you have chosen is not available in the selected language");
			setExists(true);
			setRendered(true);
			getLocalizedArticle().setRendered(true);
			getLocalizedArticle().setExists(true);
		}
	}

	/**
	 * <p>
	 * Returns true if this article is available for the language of the 
	 * locale set on this article item.
	 * </p>
	 * @return
	 */
	public boolean getAvilableInRequestedLanguage() {
		return this.avilableInRequestedLanguage;
	}

	/* (non-Javadoc)
	 * @see com.idega.content.bean.ContentItemBean#delete()
	 */
	public void delete() {
		getLocalizedArticle().delete();
		this.localizedArticle=null;
		super.delete();
		
		ArticleCacher cacher = ArticleCacher.getInstance(IWMainApplication.getDefaultIWMainApplication());
		cacher.getCacheMap().clear();
	}

	/* (non-Javadoc)
	 * @see javax.faces.event.ValueChangeListener#processValueChange(javax.faces.event.ValueChangeEvent)
	 */
	public void processValueChange(ValueChangeEvent event) throws AbortProcessingException {
		
	}
	
	protected boolean isDummyArticle() {
		boolean isDummyArticle = false;
		String body = getLocalizedArticle().getBody();
		if (body == null) {
			return false;
		}
		String tempValue = body;
		// Removing needless characters
		tempValue = tempValue.replaceAll("\b", ArticleConstants.EMPTY);
		tempValue = tempValue.replaceAll("\t", ArticleConstants.EMPTY);
		tempValue = tempValue.replaceAll("\f", ArticleConstants.EMPTY);
		tempValue = tempValue.replaceAll("\r", ArticleConstants.SPACE);
		tempValue = tempValue.replaceAll("\n", ArticleConstants.EMPTY);
		for (int i = 0; (i < ThemesConstants.DUMMY_ARTICLES.size() && !isDummyArticle); i++) {
			if (tempValue.indexOf(ThemesConstants.DUMMY_ARTICLES.get(i)) != -1) {
				isDummyArticle = true;
			}
		}
		return isDummyArticle;
	}

	public Timestamp getPublishedDate() {
		return getLocalizedArticle().getPublishedDate();
	}

	public void setPublishedDate(Timestamp date) {
		getLocalizedArticle().setPublishedDate(date);
	}
	
	public void setArticleCategories(String articleCategories) {
		getLocalizedArticle().setArticleCategories(articleCategories);
	}

}