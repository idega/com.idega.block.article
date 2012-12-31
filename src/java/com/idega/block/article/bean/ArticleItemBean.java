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
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ValueChangeEvent;
import javax.faces.event.ValueChangeListener;
import javax.jcr.RepositoryException;

import com.idega.block.article.ArticleCacher;
import com.idega.block.article.business.ArticleConstants;
import com.idega.block.article.business.ArticleUtil;
import com.idega.block.article.data.ArticleEntity;
import com.idega.block.article.data.dao.ArticleDao;
import com.idega.business.IBOLookup;
import com.idega.content.bean.ContentItem;
import com.idega.content.bean.ContentItemBean;
import com.idega.content.bean.ContentItemCase;
import com.idega.content.bean.ContentItemField;
import com.idega.content.business.ContentConstants;
import com.idega.content.business.ContentUtil;
import com.idega.core.accesscontrol.business.AccessController;
import com.idega.core.builder.business.BuilderService;
import com.idega.core.builder.business.BuilderServiceFactory;
import com.idega.data.IDOStoreException;
import com.idega.idegaweb.IWMainApplication;
import com.idega.idegaweb.IWMainApplicationSettings;
import com.idega.idegaweb.UnavailableIWContext;
import com.idega.presentation.IWContext;
import com.idega.repository.bean.RepositoryItem;
import com.idega.user.business.UserBusiness;
import com.idega.user.data.Group;
import com.idega.user.data.User;
import com.idega.util.CoreConstants;
import com.idega.util.CoreUtil;
import com.idega.util.ListUtil;
import com.idega.util.StringHandler;
import com.idega.util.StringUtil;
import com.idega.util.expression.ELUtil;
import com.idega.xml.XMLException;

/**
 * <p>
 * This is a JSF managed bean that manages each article instance and delegates
 * all calls to the correct localized instance. You can find all the articles at http://localhost:8080/workspace/content/article/
 * <p>
 * Last modified: $Date: 2009/01/10 12:24:10 $ by $Author: valdas $
 *
 * @author Anders Lindman,<a href="mailto:tryggvi@idega.com">Tryggvi Larusson</a>
 * @version $Revision: 1.88 $
 */
public class ArticleItemBean extends ContentItemBean implements Serializable, ContentItem, ValueChangeListener {

	private static final long serialVersionUID = 4514851565086272678L;

	private static final Logger LOGGER = Logger.getLogger(ArticleItemBean.class.getName());

	private final static String ARTICLE_FILE_SUFFIX = ".xml";

	public final static String TYPE_PREFIX = "IW:";
	public final static String CONTENT_TYPE = "ContentType";
	public final static String CONTENT_TYPE_WITH_PREFIX = TYPE_PREFIX+CONTENT_TYPE;

	private ArticleLocalizedItemBean localizedArticle;
	private String baseFolderLocation;
	private String languageChange;
	private boolean allowFallbackToSystemLanguage=false;
	private String resourcePath;
	private boolean availableInRequestedLanguage=false;

	private boolean partOfArticleList = false;

	//Request scope so this should work well and fast
	private Boolean allowedToEditByCurrentUser = null;

	public ArticleItemBean() {
		super();
	}

	private ArticleDao getArticleDAO() {
		ArticleDao articleDAO = ELUtil.getInstance().getBean(ArticleDao.BEAN_NAME);
		return articleDAO;
	}

	public Boolean isAllowedToEditByCurrentUser() {
		if(allowedToEditByCurrentUser != null){
			return allowedToEditByCurrentUser;
		}

		//This is expensive operation, it is better to have called isAllowedToEditByCurrentUser(iwc) first

		IWContext iwc = CoreUtil.getIWContext();
		return isAllowedToEditByCurrentUser(iwc);

	}

	/**
	 * Sets if this article is allowed to be edited for the current user, this property is
	 * saved for this instance and later is returned by isAllowedToEditByCurrentUser.
	 *
	 * @param currentUser the current user for which the check will be done
	 * @return True if it is allowed to edit, false otherwise.
	 */
	@SuppressWarnings("unchecked")
	public Boolean isAllowedToEditByCurrentUser(IWContext iwc) {
		IWMainApplicationSettings settings = iwc.getIWMainApplication().getSettings();
		if (!settings.getBoolean(ArticleConstants.USE_ROLES_IN_ARTICLE, Boolean.FALSE)) {
			allowedToEditByCurrentUser = Boolean.TRUE;
			return allowedToEditByCurrentUser;
		}

		if (allowedToEditByCurrentUser != null)
			return allowedToEditByCurrentUser;
		if (!iwc.isLoggedOn()) {
			allowedToEditByCurrentUser = Boolean.FALSE;
			return allowedToEditByCurrentUser;
		}

		AccessController accessController = iwc.getAccessController();
		try{
			if (accessController.isAdmin(iwc)) {
				allowedToEditByCurrentUser = Boolean.TRUE;
				return allowedToEditByCurrentUser;
			}
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, "Failed to check is admin", e);
			return Boolean.FALSE;
		}

		User currentUser = iwc.getCurrentUser();
		ArticleEntity articleEntity = getArticleEntity(false);
		if (articleEntity == null) {
			//By default there was permitted to edit any article by any user
			allowedToEditByCurrentUser = Boolean.TRUE;
			return allowedToEditByCurrentUser;
		}

		Set<Integer> editors = articleEntity.getEditors();
		if (ListUtil.isEmpty(editors)) {
			//By default there was permitted to edit any article by any user
			allowedToEditByCurrentUser = Boolean.TRUE;
			return allowedToEditByCurrentUser;
		}

		Collection<Group> parentGroups;
		try {
			UserBusiness userBusiness = IBOLookup.getServiceInstance(iwc, UserBusiness.class);
			parentGroups = userBusiness.getUserGroups(currentUser);

			//TODO: this probably never happens
			if(ListUtil.isEmpty(parentGroups)){
				allowedToEditByCurrentUser = Boolean.FALSE;
				return allowedToEditByCurrentUser;
			}

		} catch (Exception e) {
			LOGGER.log(Level.WARNING, "Failed getting is allowed to edit by current user "
					+ currentUser.getId() + " article " + getResourcePath(), e);
			return Boolean.FALSE;
		}
		allowedToEditByCurrentUser = Boolean.FALSE;
		for(Group group : parentGroups){
			if(editors.contains(group.getPrimaryKey())){
				allowedToEditByCurrentUser = Boolean.TRUE;
				break;
			}
		}
		return allowedToEditByCurrentUser;
	}


	public void setAllowedToEditByGroup(Group group){
		List<Integer> groupsIds = Arrays.asList(Integer.valueOf(group.getId()));
		setAllowedToEditByGroupsIds(groupsIds);
	}
	public void setAllowedToEditByGroups(Collection<Group> groups){
		Collection<Integer> groupsIds = new HashSet<Integer>(groups.size());
		for(Group group : groups){
			groupsIds.add(Integer.valueOf(group.getId()));
		}
		setAllowedToEditByGroupsIds(groupsIds);
	}
	public void setAllowedToEditByGroupsIds(Collection<Integer> groupsIds){
		if(ListUtil.isEmpty(groupsIds)){
			return;
		}
		allowedToEditByCurrentUser = null;
		ArticleEntity articleEntity = getArticleEntity(false);
		if (articleEntity != null) {
			if (groupsIds instanceof Set) {
				articleEntity.setEditors((Set<Integer>) groupsIds);
			} else {
				articleEntity.setEditors(new HashSet<Integer>(groupsIds));
			}
		}
	}

	public ArticleEntity getArticleEntity(boolean createIfNotFound) {
		ArticleEntity article = getArticleDAO().getByUri(getResourcePath());

		if (createIfNotFound && article == null) {
			article = new ArticleEntity();
			article.setUri(getResourcePath());
			article.setModificationDate(new Date(System.currentTimeMillis()));
		}

		return article;
	}

	public void setArticleEntity(ArticleEntity articleEntity) {
//		this.articleEntity = articleEntity;
	}

	public boolean isPartOfArticleList() {
		return partOfArticleList;
	}

	public void setPartOfArticleList(boolean partOfArticleList) {
		this.partOfArticleList = partOfArticleList;
	}

	public ArticleLocalizedItemBean getLocalizedArticle(){
		if (this.localizedArticle == null || getLanguageChange() != null) {
			this.localizedArticle = new ArticleLocalizedItemBean(getLocale());
			this.localizedArticle.setArticleItem(this);
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

	/**
	 * @return Author name expressed in string
	 * @see com.idega.block.article.bean.ArticleLocalizedItemBean#getAuthor()
	 */
	public String getAuthor() {
		if (isDummyContentItem()) {
			return ArticleConstants.EMPTY;
		}
		return getLocalizedArticle().getAuthor();
	}

	/**
	 * @return Text body of an article
	 * Method is modified to resolve if dummy article is being edited
	 */
	public String getBody() {
		if (isDummyContentItem()) {
			return ArticleConstants.EMPTY;
		}
		return getLocalizedArticle().getBody();
	}

	/**
	 * @return Comments of an article
	 * @see com.idega.block.article.bean.ArticleLocalizedItemBean#getComment()
	 */
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

	/**
	 * @return Text of article header.
	 * @see com.idega.block.article.bean.ArticleLocalizedItemBean#getHeadline()
	 */
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

	/**
	 * Add or change author name here.
	 * @see com.idega.block.article.bean.ArticleLocalizedItemBean#setAuthor(java.lang.String)
	 */
	public void setAuthor(String s) {
		getLocalizedArticle().setAuthor(s);
	}

	/**
	 * Add or change an article body text here.
	 * @param body Text, that should be saved.
	 * @see com.idega.block.article.bean.ArticleLocalizedItemBean#setBody(java.lang.String)
	 */
	public void setBody(String body) {
		getLocalizedArticle().setBody(body);
	}

	/**
	 * Add or change an article comments here.
	 * @param s Text, that should be saved.
	 * @see com.idega.block.article.bean.ArticleLocalizedItemBean#setComment(java.lang.String)
	 */
	public void setComment(String s) {
		getLocalizedArticle().setComment(s);
	}

	public void setLinkToComments(String linkToComments) {
		getLocalizedArticle().setLinkToComments(linkToComments);
	}

	/**
	 * Add or change an article header here, which shown in article creation dialog.
	 * @param o Some object. Article header will be set as o.toString() value.
	 * @see com.idega.block.article.bean.ArticleLocalizedItemBean#setHeadline(java.lang.Object)
	 */
	public void setHeadline(Object o) {
		getLocalizedArticle().setHeadline(o);
	}

	/**
	 * Add or change an article header here, which shown in article creation dialog.
	 * @param s Text of article header.
	 * @see com.idega.block.article.bean.ArticleLocalizedItemBean#setHeadline(java.lang.String)
	 */
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

	/**
	 * Add or change an article teaser text here. Teaser is shown in the article creation form.
	 * @param s Teaser text.
	 * @see com.idega.block.article.bean.ArticleLocalizedItemBean#setTeaser(java.lang.String)
	 */
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
		ArticleEntity article = getArticleEntity(true);
		article.setUri(getResourcePath());
		article = getArticleDAO().updateArticle(article);
		if (article == null || article.getId() == null)
			throw new IDOStoreException("Unable to create/edit article at " + getResourcePath());

		setArticleEntity(article);
		try {
			getLocalizedArticle().store();
		} catch(Exception e){
			throw new IDOStoreException("Failed storing to slide", e);
		}
	}

	protected void tryPublish() {
		getLocalizedArticle().tryPublish();
	}

	/**
	 * @return Article name
	 * @see com.idega.content.bean.ContentItemBean#getName()
	 */
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

	/**
	 * Add or change an article name
	 * @param s Article name
	 * @see com.idega.content.bean.ContentItemBean#setName(java.lang.String)
	 */
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

	public List<String> getAttachmentsUris() {
		return getLocalizedArticle().getConvertedAttachments();
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
		if (this.baseFolderLocation!=null) {
			return this.baseFolderLocation;
		} else {
			return ArticleUtil.getArticleBaseFolderPath();
		}
	}

	/**
	/*
	 * This sets the base folder for storing articles,
	 * something like '/files/cms/article'
	 **/
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
	 * @return Partial path of the article without the language part.
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
		int index = resourcePath.indexOf(CoreConstants.DOT + CoreConstants.ARTICLE_FILENAME_SCOPE);
		if (index >- 1) {
			String articlePath = resourcePath.substring(0,index+CoreConstants.ARTICLE_FILENAME_SCOPE.length()+1);
			LOGGER.info("Article path returned: "+articlePath);
			return articlePath;
		}
		LOGGER.warning("Resource path for article '"+resourcePath+"' does not contain article filename scope '."+CoreConstants.ARTICLE_FILENAME_SCOPE+"'.  The resource path is returned unchanged.");
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
		} catch (UnavailableIWContext e) {
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

	/**
	 * Loads the article (folder)
	 * @throws RepositoryException
	 */
	@Override
	protected boolean load(RepositoryItem article) throws IOException, RepositoryException {
		RepositoryItem localizedArticleFile = null;
		String userLanguageArticleResourcePath = null;

		// First check if the resource is a folder, as it should be
		if (article.isCollection()) {
			Collection<RepositoryItem> resources = article.getChildResources();
			userLanguageArticleResourcePath = getArticleDefaultLocalizedResourcePath();
			if (containsChildResourceWithPath(resources, userLanguageArticleResourcePath)) { //the language that the user has selected
				localizedArticleFile = getRepositoryService().getRepositoryItemAsRootUser(userLanguageArticleResourcePath);
				setAvailableInSelectedLanguage();
			} else {
				//selected language not available:
				if (getAllowFallbackToSystemLanguage()) {
					String systemLanguageArticleResourcePath = getArticleDefaultLocalizedResourcePath(getSystemLanguage());
					if (containsChildResourceWithPath(resources, systemLanguageArticleResourcePath)){ //the language default in the system.
						localizedArticleFile = getRepositoryService().getRepositoryItemAsRootUser(systemLanguageArticleResourcePath);
						setAvailableInSelectedLanguage();
					} else {
						setNotAvailableInSelectedLanguage();
					}
				} else {
					setNotAvailableInSelectedLanguage();
				}

			}
		} else {
			String path = getResourcePath();
			setLanguageFromFilePath(path);
			String parentFolder;
			parentFolder = article.getParentPath();
			setResourcePath(parentFolder);

			return load(parentFolder);
		}
		if (localizedArticleFile == null) {
			Logger.getLogger(ArticleItemBean.class.getName()).warning("Error loading localized article " + userLanguageArticleResourcePath +
					" from article " + article);
			return false;
		}

		return getLocalizedArticle().load(localizedArticleFile);
	}

	protected boolean containsChildResourceWithPath(Collection<RepositoryItem> resources, String childFullPath) {
		if (ListUtil.isEmpty(resources))
			return false;

		for (RepositoryItem child: resources) {
			if (child.getPath().equals(childFullPath))
				return true;
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
<<<<<<< HEAD
	 * <p>
	 * Returns true if this article is available for the language of the
	 * locale set on this article item.
	 * </p>
=======
>>>>>>> f9ed451cecd64d0cc302ec5227678cda502339e8
	 * @return true if this article is available for the language of the locale set on this article item.
	 */
	public boolean getAvailableInRequestedLanguage() {
		return this.availableInRequestedLanguage;
	}

	@Override
	public void delete() {
		getArticleDAO().remove(getArticleEntity(false));
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

	/**
	 * Add or change categories of article here. Do not use UPPER case letters, use "-" instead of spaces. There should not be any simple spaces.
	 * @param articleCategories Selected categories. Example: ",first-category,category,last-category,"
	 */
	public void setArticleCategories(String articleCategories) {
		getLocalizedArticle().setArticleCategories(articleCategories);
	}

	public void setArticleCategories(Collection<String> categories){
		if(ListUtil.isEmpty(categories)){
			setArticleCategories(CoreConstants.EMPTY);
		}
		StringBuilder categoryString = new StringBuilder();
		for(java.util.Iterator<String> iter = categories.iterator();iter.hasNext(); ){
			categoryString.append(iter.next());
			if(iter.hasNext()){
				categoryString.append(CoreConstants.COMMA);
			}
		}
		setArticleCategories(categoryString.toString());
	}

	@Override
	public boolean isSetPublishedDateByDefault() {
		return super.isSetPublishedDateByDefault();
	}

	@Override
	public void setSetPublishedDateByDefault(boolean setPublishedDateByDefault) {
		super.setSetPublishedDateByDefault(setPublishedDateByDefault);
	}

	/**
	 *
	 * @return Categories of an article
	 */
	public List<String> getCategories() {
		return getLocalizedArticle().getCategories();
	}

	public String getFilesResourcePath(){
		String resourcesPath = getResourcePath();
		if(!resourcesPath.endsWith("/")){
			resourcesPath = resourcesPath + "/";
		}
		resourcesPath = resourcesPath + "article-files/";
		return resourcesPath;
	}

}