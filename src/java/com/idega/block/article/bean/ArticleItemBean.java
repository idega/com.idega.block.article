/*
 * $Id: ArticleItemBean.java,v 1.88 2009/01/10 12:24:10 valdas Exp $
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
import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ValueChangeEvent;
import javax.faces.event.ValueChangeListener;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

import com.idega.block.article.ArticleCacher;
import com.idega.block.article.business.ArticleConstants;
import com.idega.block.article.business.ArticleUtil;
import com.idega.builder.bean.AdvancedProperty;
import com.idega.content.bean.ContentItem;
import com.idega.content.bean.ContentItemBean;
import com.idega.content.bean.ContentItemCase;
import com.idega.content.bean.ContentItemField;
import com.idega.content.business.ContentConstants;
import com.idega.content.business.ContentUtil;
import com.idega.core.builder.business.BuilderService;
import com.idega.core.builder.business.BuilderServiceFactory;
import com.idega.data.IDOStoreException;
import com.idega.idegaweb.IWMainApplication;
import com.idega.idegaweb.UnavailableIWContext;
import com.idega.presentation.IWContext;
import com.idega.util.CoreConstants;
import com.idega.util.CoreUtil;
import com.idega.util.StringHandler;
import com.idega.util.StringUtil;
import com.idega.xml.XMLException;

/**
 * <p>
 * This is a JSF managed bean that manages each article instance and delegates
 * all calls to the correct localized instance.
 * <p>
 * Last modified: $Date: 2009/01/10 12:24:10 $ by $Author: valdas $
 *
 * @author Anders Lindman,<a href="mailto:tryggvi@idega.com">Tryggvi Larusson</a>
 * @version $Revision: 1.88 $
 */
public class ArticleItemBean extends ContentItemBean implements Serializable, ContentItem, ValueChangeListener {

	private static final long serialVersionUID = 4514851565086272678L;
	private final static String ARTICLE_FILE_SUFFIX = ".xml";

	public final static String TYPE_PREFIX = "IW:";
	public final static String CONTENT_TYPE = "ContentType";
	public final static String CONTENT_TYPE_WITH_PREFIX = TYPE_PREFIX+CONTENT_TYPE;

//	public static final PropertyName PROPERTY_CONTENT_TYPE = new PropertyName(TYPE_PREFIX,CONTENT_TYPE);

	private ArticleLocalizedItemBean localizedArticle;
	private String baseFolderLocation;
	private String languageChange;
	private boolean allowFallbackToSystemLanguage=false;
	private String resourcePath;
	private boolean availableInRequestedLanguage=false;

	private boolean isPartOfArticleList = false;

	public boolean isPartOfArticleList() {
		return isPartOfArticleList;
	}

	public void setPartOfArticleList(boolean isPartOfArticleList) {
		this.isPartOfArticleList = isPartOfArticleList;
	}

	public ArticleLocalizedItemBean getLocalizedArticle(){
		if (this.localizedArticle == null || getLanguageChange() != null) {
			this.localizedArticle = new ArticleLocalizedItemBean(getLocale());
			this.localizedArticle.setArticleItem(this);
			/*try {
				if(isPersistToJCR() && this.getSession()!=null){
					this.localizedArticle.setSession(this.getSession());
				}
			} catch (RepositoryException e) {
				e.printStackTrace();
			}*/
		}
		else {
			String thisLanguage = getLanguage();
			String fileLanguage = this.localizedArticle.getLanguage();
			if (!thisLanguage.equals(fileLanguage)) {
				throw new RuntimeException("Locale ("+getLocale()+") inconsistency for article: "+getResourcePath()+" and localizedArticle: " +
						this.localizedArticle.getResourcePath());
			}
		}
		return this.localizedArticle;
	}

	public void addImage(byte[] imageData, String contentType) {
		getLocalizedArticle().addImage(imageData, contentType);
	}

	@Override
	public void clear() {
		getLocalizedArticle().clear();
		this.resourcePath=null;
		this.availableInRequestedLanguage=false;
	}

	public String getAsXML() throws IOException, XMLException {
		return getLocalizedArticle().getAsXML();
	}

	public String getAuthor() {
		if (isDummyContentItem()) {
			return ArticleConstants.EMPTY;
		}
		return getLocalizedArticle().getAuthor();
	}

	public String getBody() {
		if (isDummyContentItem()) {
			return ArticleConstants.EMPTY;
		}
		return getLocalizedArticle().getBody();
	}

	public String getComment() {
		return getLocalizedArticle().getComment();
	}

	public String getLinkToComments() {
		return getLocalizedArticle().getLinkToComments();
	}

	@Override
	public String[] getContentFieldNames() {
		return getLocalizedArticle().getContentFieldNames();
	}

	@Override
	public String getContentItemPrefix() {
		return getLocalizedArticle().getContentItemPrefix();
	}

	public String getContentLanguage() {
		return getLocalizedArticle().getContentLanguage();
	}

	public String getHeadline() {
		if (isDummyContentItem()) {
			return ArticleConstants.EMPTY;
		}
		return getLocalizedArticle().getHeadline();
	}

	public List<ContentItemField> getImages() {
		return getLocalizedArticle().getImages();
	}

	public String getSource() {
		return getLocalizedArticle().getSource();
	}

	public String getTeaser() {
		if (isDummyContentItem()) {
			return ArticleConstants.EMPTY;
		}
		return getLocalizedArticle().getTeaser();
	}

	@Override
	public String[] getToolbarActions() {
		return getLocalizedArticle().getToolbarActions();
	}

	public boolean isUpdated() {
		return getLocalizedArticle().isUpdated();
	}

	protected void prettifyBody() {
		getLocalizedArticle().prettifyBody();
	}

	public void removeImage(Integer imageNumber) {
		getLocalizedArticle().removeImage(imageNumber);
	}

	public void setAuthor(String s) {
		getLocalizedArticle().setAuthor(s);
	}

	public void setBody(String body) {
		getLocalizedArticle().setBody(body);
	}

	public void setComment(String s) {
		getLocalizedArticle().setComment(s);
	}

	public void setLinkToComments(String linkToComments) {
		getLocalizedArticle().setLinkToComments(linkToComments);
	}

	public void setHeadline(Object o) {
		getLocalizedArticle().setHeadline(o);
	}

	public void setHeadline(String s) {
		getLocalizedArticle().setHeadline(s);
	}

	public void setImages(List<ContentItemField> l) {
		getLocalizedArticle().setImages(l);
	}

	protected void setPublished() {
		getLocalizedArticle().setPublished();
	}

	public void setSource(String s) {
		getLocalizedArticle().setSource(s);
	}

	public void setTeaser(String s) {
		getLocalizedArticle().setTeaser(s);
	}

	public void setUpdated(boolean b) {
		getLocalizedArticle().setUpdated(b);
	}

	public void setUpdated(Boolean b) {
		getLocalizedArticle().setUpdated(b);
	}

	@Override
	public void store() throws IDOStoreException {
		try {
			storeToJCR();
			getLocalizedArticle().store();

			IWMainApplication iwma = IWMainApplication.getDefaultIWMainApplication();
			if(iwma!=null){
				ArticleCacher cacher = ArticleCacher.getInstance(iwma);
				cacher.getCacheMap().clear();

				ContentUtil.removeCategoriesViewersFromCache();
			}
		} catch(ArticleStoreException ase){
			throw ase;
		} catch(Exception e){
			throw new RuntimeException(e);
		}

		try {
			commitJCRStore();
		} catch (RepositoryException e) {
			throw new IDOStoreException(e.getMessage());
		}
	}

	protected void commitJCRStore() throws RepositoryException{
		//getSession().save();
	}

//	private void storeToWebDav() throws HttpException, IOException,	RemoteException {
//		IWUserContext iwuc = IWContext.getInstance();
//		IWSlideSession session = getIWSlideSession(iwuc);
//		WebdavRootResource rootResource = session.getWebdavRootResource();
//
//		//	Setting the path for creating new file/creating localized version/updating existing file
//		String articleFolderPath = getResourcePath();
//
//		boolean hadToCreate = session.createAllFoldersInPath(articleFolderPath);
//		if (hadToCreate) {
//			String fixedFolderURL = session.getURI(articleFolderPath);
//			rootResource.proppatchMethod(fixedFolderURL, PROPERTY_CONTENT_TYPE, "LocalizedFile", true);
//		}
//		else{
//			rootResource.proppatchMethod(articleFolderPath, PROPERTY_CONTENT_TYPE, "LocalizedFile", true);
//		}
//
//		rootResource.close();
//	}

	private void storeToJCR() throws IOException, RepositoryException{
		//	Setting the path for creating new file/creating localized version/updating existing file
		String articleFolderPath = getResourcePath();

//		helper.createAllFoldersInPath(session,articleFolderPath);
//		Node node = getNode();
//		node.setProperty(CONTENT_TYPE_WITH_PREFIX, "LocalizedFile");
//		node.save();

		Node articleFolder = getRepositoryService().getNode(articleFolderPath);
		getRepositoryService().setProperties(articleFolder, new AdvancedProperty(CONTENT_TYPE_WITH_PREFIX, "LocalizedFile"));
	}

	protected void tryPublish() {
		getLocalizedArticle().tryPublish();
	}

	@Override
	public String getName() {
		return getLocalizedArticle().getName();
	}

	@Override
	public String getDescription() {
		return getLocalizedArticle().getDescription();
	}

	@Override
	public String getItemType() {
		return getLocalizedArticle().getItemType();
	}

	@Override
	public int getCreatedByUserId() {
		return getLocalizedArticle().getCreatedByUserId();
	}

	@Override
	public void setName(String s) {
		getLocalizedArticle().setName(s);
	}

	@Override
	public void setDescription(String s) {
		getLocalizedArticle().setDescription(s);
	}

	@Override
	public void setItemType(String s) {
		getLocalizedArticle().setItemType(s);
	}

	@Override
	public void setCreatedByUserId(int id) {
		getLocalizedArticle().setCreatedByUserId(id);
	}

	@Override
	public Map<String, Locale> getLocales() {
		return getLocalizedArticle().getLocales();
	}

	@Override
	public String getPendingLocaleId() {
		return getLocalizedArticle().getPendingLocaleId();
	}

	@Override
	public void setPendingLocaleId(String localeId) {
		getLocalizedArticle().setPendingLocaleId(localeId);
	}

	@Override
	public String getRequestedStatus() {
		return getLocalizedArticle().getRequestedStatus();
	}

	@Override
	public void setRequestedStatus(String requestedStatus) {
		getLocalizedArticle().setRequestedStatus(requestedStatus);
	}

	@Override
	public List<ContentItemField> getAttachments() {
		return getLocalizedArticle().getAttachments();
	}

	@Override
	public void setAttachment(List<ContentItemField> l) {
		getLocalizedArticle().setAttachment(l);
	}

	@Override
	public ContentItemCase getCase() {
		return getLocalizedArticle().getCase();
	}

	@Override
	public void setCase(ContentItemCase caseBean) {
		getLocalizedArticle().setCase(caseBean);
	}

	@Override
	public String getStatus() {
		return getLocalizedArticle().getStatus();
	}

	@Override
	public void setStatus(String status) {
		getLocalizedArticle().setStatus(status);
	}

	@Override
	public void updateLocale() {
		getLocalizedArticle().updateLocale();
	}

	@Override
	public Timestamp getCreationDate() {
		return getLocalizedArticle().getCreationDate();
	}

	@Override
	public String getVersionName() {
		return getLocalizedArticle().getVersionName();
	}

	@Override
	public void setVersionName(String name) {
		getLocalizedArticle().setVersionName(name);
	}

	@Override
	public Boolean getRendered() {
		return getLocalizedArticle().getRendered();
	}

	@Override
	public void setRendered(boolean render) {
		getLocalizedArticle().setRendered(render);
	}

	@Override
	public void setRendered(Boolean render) {
		getLocalizedArticle().setRendered(render);
	}

	@Override
	public boolean isAutoCreateResource() {

		return getLocalizedArticle().isAutoCreateResource();
	}

	@Override
	public void setAutoCreateResource(boolean autoCreateResource) {

		getLocalizedArticle().setAutoCreateResource(autoCreateResource);
	}

	@Override
	public boolean getExists() {
		return getLocalizedArticle().getExists();
	}

	@Override
	public void setExists(boolean exists) {
		getLocalizedArticle().setExists(exists);
	}

	@Override
	public Object getValue(String fieldName) {
		return getLocalizedArticle().getValue(fieldName);
	}

	@Override
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
	}
	/*
	 * This sets the base folder for storing articles,
	 * something like '/files/cms/article'
	 */
	public void setBaseFolderLocation(String path) {
		this.baseFolderLocation = path;
	}

	@Override
	public Locale getLocale(){
		return super.getLocale();
	}

	@Override
	public void setLocale(Locale locale){
		super.setLocale(locale);
	}

	@Override
	public void setResourcePath(String resourcePath){
		this.resourcePath=resourcePath;
		updateLocalizedArticleBean();
	}

	protected void updateLocalizedArticleBean(){
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
	@Override
	public String getResourcePath() {
		String sResourcePath = this.resourcePath;
		if (StringUtil.isEmpty(sResourcePath) || sResourcePath.equals(CoreConstants.SLASH)) {
			sResourcePath = createArticlePath();
			setResourcePath(sResourcePath);
		}
		return sResourcePath;
	}


	public synchronized String createArticlePath() {
		String resourcePath = getGeneratedArticleResourcePath();
		int index = resourcePath.indexOf("."+CoreConstants.ARTICLE_FILENAME_SCOPE);
		if(index>-1){
			String articlePath = resourcePath.substring(0,index+CoreConstants.ARTICLE_FILENAME_SCOPE.length()+1);
			System.out.println("Article path returned: "+articlePath);
			return articlePath;
		}
		Logger log = Logger.getLogger(this.getClass().toString());
		log.warning("Resource path for article '"+resourcePath+"' does not contain article filename scope '."+CoreConstants.ARTICLE_FILENAME_SCOPE+
				"'.  The resource path is returned unchanged.");
		return resourcePath;
	}

	/**
	 * Returns the path to the actual resource (xml-file) ../cms/article/YYYY/MM/YYYYMMDD-HHmm.article/lang.xml
	 *
	 * @see ContentItem#getResourcePath()
	 * @return
	 */
	private String getGeneratedArticleResourcePath() {
		String path = null;
		try {
			path = generateArticleResourcePath(CoreUtil.getIWContext());
		}
		catch (UnavailableIWContext e) {
			e.printStackTrace();
		}
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
	protected String generateArticleResourcePath(IWContext iwc) {
		BuilderService service = null;
		try {
			service = BuilderServiceFactory.getBuilderService(iwc);
		} catch (RemoteException e) {
			e.printStackTrace();
			return null;
		}

		return service.generateResourcePath(getBaseFolderLocation(), CoreConstants.ARTICLE_FILENAME_SCOPE, CoreConstants.ARTICLE_FILENAME_SCOPE);
	}

	public void setLanguageChange(String s) {
		this.languageChange=s;
	}

	public String getLanguageChange() {
		return this.languageChange;
	}


	/**
	 * Loads the article (folder)
	 */
	@Override
	protected boolean load(String path) throws IOException {
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

//	/**
//	 * Loads the article (folder)
//	 */
//	protected boolean load(RepositoryItem repositoryItem) throws IOException {
//		RepositoryItem localizedArticleFile = null;
//		//First check if the resource is a folder, as it should be
//		if (repositoryItem.isCollection()) {
//			Collection<RepositoryItem> resources = repositoryItem.getChildResources();
//			String userLanguageArticleResourcePath = getArticleDefaultLocalizedResourcePath();
//			if(resources.isThereResourceName(userLanguageArticleResourcePath)){ //the language that the user has selected
//				localizedArticleFile = (WebdavExtendedResource) resources.getResource(userLanguageArticleResourcePath);
//				setAvailableInSelectedLanguage();
//			}
//			else{
//				//selected language not available:
//				if(getAllowFallbackToSystemLanguage()){
//					String systemLanguageArticleResourcePath = getArticleDefaultLocalizedResourcePath(getSystemLanguage());//getArticleName(iwc.getIWMainApplication().getDefaultLocale());
//					if(resources.isThereResourceName(systemLanguageArticleResourcePath)){ //the language default in the system.
//						localizedArticleFile = (WebdavExtendedResource) resources.getResource(systemLanguageArticleResourcePath);
//						setAvailableInSelectedLanguage();
//					}
//					else{
//						setNotAvailableInSelectedLanguage();
//					}
//				}
//				else{
//					setNotAvailableInSelectedLanguage();
//				}
//
//			}
//		} else {
//			String path = getResourcePath();
//			setLanguageFromFilePath(path);
//			String parentFolder = webdavResource.getParentPath();
//			setResourcePath(parentFolder);
//			return load(parentFolder);
//		}
//		if(localizedArticleFile!=null){
//			return getLocalizedArticle().load(localizedArticleFile);
//		}
//		return false;
//	}

	/**
	 * Loads the article (folder)
	 * @throws RepositoryException
	 */
	@Override
	protected boolean load(Node articleNode) throws IOException, RepositoryException {
		Node localizedArticleFile = null;
		//First check if the resource is a folder, as it should be
		if (articleNode.getPrimaryNodeType().isNodeType(getRepositoryService().getRepositoryConstantFolderType())) {
			NodeIterator resources = articleNode.getNodes();
			String userLanguageArticleResourcePath = getArticleDefaultLocalizedResourcePath();
			if(containsChildResourceWithPath(resources,userLanguageArticleResourcePath)){ //the language that the user has selected
				localizedArticleFile = getRepositoryService().getNode(userLanguageArticleResourcePath);//articleNode.getSession().getRootNode().getNode(userLanguageArticleResourcePath);
				setAvailableInSelectedLanguage();
			}
			else{
				//selected language not available:
				if(getAllowFallbackToSystemLanguage()){
					String systemLanguageArticleResourcePath = getArticleDefaultLocalizedResourcePath(getSystemLanguage());
					if(containsChildResourceWithPath(resources,systemLanguageArticleResourcePath)){ //the language default in the system.
						localizedArticleFile = getRepositoryService().getNode(systemLanguageArticleResourcePath);//articleNode.getSession().getRootNode().getNode(systemLanguageArticleResourcePath);
						setAvailableInSelectedLanguage();
					}
					else{
						setNotAvailableInSelectedLanguage();
					}
				}
				else{
					setNotAvailableInSelectedLanguage();
				}

			}
		} else {
			String path = getResourcePath();
			setLanguageFromFilePath(path);
			String parentFolder;
			parentFolder = articleNode.getParent().getPath();
			setResourcePath(parentFolder);

			return load(parentFolder);
		}
		if(localizedArticleFile!=null){
			return getLocalizedArticle().load(localizedArticleFile);
		}
		return false;
	}


	protected boolean containsChildResourceWithPath(NodeIterator resources,	String childFullPath) {
		while (resources.hasNext()) {
			Node child = resources.nextNode();
			try {
				if(child.getPath().equals(childFullPath)){
					return true;
				}
			} catch (RepositoryException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

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

	public void setAvailableInSelectedLanguage(){
		this.availableInRequestedLanguage=true;
	}

	public void setNotAvailableInSelectedLanguage(){
		this.availableInRequestedLanguage = false;
		if (!getAllowFallbackToSystemLanguage()) {
			getLocalizedArticle().setHeadline("Article not available");
			getLocalizedArticle().setBody(ContentConstants.ARTICLE_NOT_AVAILABLE_BODY);
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
	public boolean getAvailableInRequestedLanguage() {
		return this.availableInRequestedLanguage;
	}

	@Override
	public void delete() {
		getLocalizedArticle().delete();
		this.localizedArticle=null;
		super.delete();

		ArticleCacher cacher = ArticleCacher.getInstance(IWMainApplication.getDefaultIWMainApplication());
		cacher.getCacheMap().clear();

		ContentUtil.removeCategoriesViewersFromCache();
	}

	@Override
	public void processValueChange(ValueChangeEvent event) throws AbortProcessingException {
	}

	@Override
	public Timestamp getPublishedDate() {
		return getLocalizedArticle().getPublishedDate();
	}

	@Override
	public void setPublishedDate(Timestamp date) {
		getLocalizedArticle().setPublishedDate(date);
	}

	public void setArticleCategories(String articleCategories) {
		getLocalizedArticle().setArticleCategories(articleCategories);
	}

	@Override
	public boolean isSetPublishedDateByDefault() {
		return super.isSetPublishedDateByDefault();
	}

	@Override
	public void setSetPublishedDateByDefault(boolean setPublishedDateByDefault) {
		super.setSetPublishedDateByDefault(setPublishedDateByDefault);
	}

}