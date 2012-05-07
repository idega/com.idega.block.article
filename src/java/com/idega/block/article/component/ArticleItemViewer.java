/*
 * $Id: ArticleItemViewer.java,v 1.53 2009/05/27 16:10:06 valdas Exp $
 *
 * Copyright (C) 2004 Idega. All Rights Reserved.
 *
 * This software is the proprietary information of Idega.
 * Use is subject to license terms.
 */
package com.idega.block.article.component;

import java.io.IOException;
import java.sql.Timestamp;

import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.context.FacesContext;

import com.idega.block.article.ArticleCacher;
import com.idega.block.article.bean.ArticleItemBean;
import com.idega.block.article.bean.ArticleLocalizedItemBean;
import com.idega.block.article.business.ArticleActionURIHandler;
import com.idega.block.article.business.ArticleUtil;
import com.idega.content.bean.ContentItem;
import com.idega.content.business.ContentConstants;
import com.idega.content.business.ContentUtil;
import com.idega.content.presentation.ContentItemToolbar;
import com.idega.content.presentation.ContentItemViewer;
import com.idega.content.presentation.ContentViewer;
import com.idega.core.cache.UIComponentCacher;
import com.idega.idegaweb.IWMainApplication;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.ui.HiddenInput;
import com.idega.util.CoreConstants;
import com.idega.util.CoreUtil;
import com.idega.util.PresentationUtil;
import com.idega.util.StringUtil;
import com.idega.webface.WFHtml;
import com.idega.webface.convert.WFTimestampConverter;

/**
 * Last modified: $Date: 2009/05/27 16:10:06 $ by $Author: valdas $
 *
 * Displays the article item
 *
 * @author <a href="mailto:gummi@idega.com">Gudmundur Agust Saemundsson</a>
 * @version $Revision: 1.53 $
 */
public class ArticleItemViewer extends ContentItemViewer {

	//	Constants:
	private final static String ATTRIBUTE_AUTHOR = "author";
	private final static String[] ATTRIBUTE_ARRAY = new String[] {ContentConstants.ATTRIBUTE_HEADLINE, ContentConstants.ATTRIBUTE_CREATION_DATE, ATTRIBUTE_AUTHOR,
		ContentConstants.ATTRIBUTE_TEASER, ContentConstants.ATTRIBUTE_BODY};
	private final static String facetIdPrefix = "article_";
	private final static String styleClassPrefix = "article_";
	private final static String DEFAULT_STYLE_CLASS = "article_item";

	//	Instance variables:
	private boolean headlineAsLink;
	private boolean showDate = true;
	private boolean showTime = true;
	private boolean cacheEnabled = true;
	private boolean showAuthor = true;
	private boolean showCreationDate = true;
	private boolean showHeadline = true;
	private boolean showTeaser = true;
	private boolean showBody = true;
	private boolean addCommentsViewer = false;

	private String datePattern;

	//	Other
	private boolean canModifyRenderingAttribute = false;
	private boolean partOfArticlesList = false;
	private boolean canInitAnyField = true;

	/**
	 * @return Returns the cacheEnabled.
	 */
	public boolean isCacheEnabled() {
		return this.cacheEnabled;
	}

	/**
	 * @param cacheEnabled The cacheEnabled to set.
	 */
	public void setCacheEnabled(boolean cacheEnabled) {
		this.cacheEnabled = cacheEnabled;
	}

	public ArticleItemViewer() {
		super();
		this.setStyleClass(DEFAULT_STYLE_CLASS);
	}

	@Override
	public String[] getViewerFieldNames(){
		return ATTRIBUTE_ARRAY;
	}

	/**
	 * @return Returns the facetIdPrefix.
	 */
	@Override
	protected String getFacetIdPrefix() {
		return facetIdPrefix;
	}

	/**
	 * @return Returns the styleClassPrefix.
	 */
	@Override
	protected String getDefaultStyleClassPrefix() {
		return styleClassPrefix;
	}

	@Override
	protected UIComponent createFieldComponent(String attribute){
		if (ContentConstants.ATTRIBUTE_BODY.equals(attribute)) {
			WFHtml body = new WFHtml();
			return body;
		}
		else if (ContentConstants.ATTRIBUTE_TEASER.equals(attribute)) {
			return new WFHtml();
		}
		else if (attribute.equals(ContentConstants.ATTRIBUTE_HEADLINE) && this.getHeadlineAsLink()) {
			UIComponent link = getEmptyMoreLink();
			HtmlOutputText text = new HtmlOutputText();
			link.getChildren().add(text);
			return link;
		}
		else {
			return new HtmlOutputText();
		}
	}

	@Override
	protected void initializeComponent(FacesContext context) {
		if (!canInitAnyField) {
			return;
		}

		String attr[] = getViewerFieldNames();
		if (attr == null) {
			return;
		}
		for (int i = 0; i < attr.length; i++) {
			if (canInitField(attr[i])) {
				initializeComponent(attr[i]);
			}
		}
		initializePermissionComponents(context);
		
		initializeComments(context);
		if (isShowCreationDate()) {
			UIComponent creationDate = getFieldViewerComponent(ContentConstants.ATTRIBUTE_CREATION_DATE);
			if (creationDate instanceof HtmlOutputText) {
				((HtmlOutputText) creationDate).setConverter(new WFTimestampConverter(datePattern, showDate, showTime));
			}
		}
		//addFeed(IWContext.getIWContext(context));
	}

	@Override
	protected void updateComponent(FacesContext context) {
		initializePermissionComponents(context);
		super.updateComponent(context);
	}
	
	private void initializePermissionComponents(FacesContext context){
		IWContext iwc = IWContext.getIWContext(context);
		ArticleItemBean articleItemBean = (ArticleItemBean) getContentItem();
		if (articleItemBean.isAllowedToEditByCurrentUser(iwc)){
			initializeToolbar();
		}else{
			ContentItemToolbar toolbar = new ContentItemToolbar(this.getClass().equals(CoreConstants.getArticleItemViewerClass()));
			this.setToolbar(toolbar);
		}
	}

	private boolean canInitField(String attribute) {
		if (attribute == null) {
			return false;
		}
		if (ATTRIBUTE_AUTHOR.equals(attribute) && !isShowAuthor()) {
			return false;
		}
		if (ContentConstants.ATTRIBUTE_CREATION_DATE.equals(attribute) && !isShowCreationDate()) {
			return false;
		}
		if (ContentConstants.ATTRIBUTE_BODY.equals(attribute) && (!isShowBody() && !useTeaserInBodyField())) {
			return false;
		}
		if (ContentConstants.ATTRIBUTE_TEASER.equals(attribute) && (!isShowTeaser() || useTeaserInBodyField())) {
			return false;
		}
		if (ContentConstants.ATTRIBUTE_HEADLINE.equals(attribute) && !isShowHeadline()) {
			return false;
		}
		return true;
	}

	private boolean useTeaserInBodyField() {
		return partOfArticlesList ? (!isShowBody() && isShowTeaser()) : false;
	}

	/**
	 * @return Returns the author.
	 */
	public String getAuthor() {
		return (String)getValue(ATTRIBUTE_AUTHOR);
	}
	/**
	 * @param author The author to set.
	 */
	public void setAuthor(String author) {
		setFieldLocalValue(ATTRIBUTE_AUTHOR,author);
	}

	@Override
	public Object getValue(String fieldName) {
		if (useTeaserInBodyField()) {
			if (ContentConstants.ATTRIBUTE_BODY.equals(fieldName)) {
				fieldName = ContentConstants.ATTRIBUTE_TEASER;
			}
		}

		return super.getValue(fieldName);
	}

	/**
	 * @return Returns the body.
	 */
	public String getBody() {
		return (String)getValue(ContentConstants.ATTRIBUTE_BODY);
	}
	/**
	 * @param body The body to set.
	 */
	public void setBody(String body) {
		setFieldLocalValue(ContentConstants.ATTRIBUTE_BODY,body);
	}

	/**
	 * @return Returns the creationDate.
	 */
	public Timestamp getCreationDate() {
		return (Timestamp)getValue(ContentConstants.ATTRIBUTE_CREATION_DATE);
	}
	/**
	 * @param creationDate The creationDate to set.
	 */
	public void setCreationDate(Timestamp creationDate) {
		setFieldLocalValue(ContentConstants.ATTRIBUTE_CREATION_DATE,creationDate);
	}

	/**
	 * @return Returns the headline.
	 */
	public String getHeadline() {
		return (String)getValue(ContentConstants.ATTRIBUTE_HEADLINE);
	}
	/**
	 * @param headline The headline to set.
	 */
	public void setHeadline(String headline) {
		setFieldLocalValue(ContentConstants.ATTRIBUTE_HEADLINE,headline);
	}

	/**
	 * @return Returns the teaser.
	 */
	public String getTeaser() {
		return (String)getValue(ContentConstants.ATTRIBUTE_TEASER);
	}
	/**
	 * @param teaser The teaser to set.
	 */
	public void setTeaser(String teaser) {
		setFieldLocalValue(ContentConstants.ATTRIBUTE_TEASER,teaser);
	}

	@Override
	public ContentItem loadContentItem(String itemResourcePath) {
		try {
			ArticleItemBean bean = new ArticleItemBean();
			bean.setAutoCreateResource(isAutoCreateResource());
			bean.setResourcePath(itemResourcePath);
			bean.load();
			return bean;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void setHeadlineAsLink(boolean asLink) {
		this.headlineAsLink = asLink;
	}

	public boolean getHeadlineAsLink() {
		return this.headlineAsLink;
	}

	public void setDatePattern(String pattern) {
		this.datePattern = pattern;
	}

	public String getDatePattern() {
		return this.datePattern;
	}

	public void setShowDate(boolean showDate) {
		this.showDate = showDate;
	}

	public boolean isShowDate() {
		return this.showDate;
	}

	public void setShowTime(boolean showTime) {
		this.showTime = showTime;
	}

	public boolean isShowTime() {
		return this.showTime;
	}

	/**
	 * @see javax.faces.component.UIComponentBase#saveState(javax.faces.context.FacesContext)
	 */
	@Override
	public Object saveState(FacesContext ctx) {
		Object values[] = new Object[8];
		values[0] = super.saveState(ctx);
		values[1] = Boolean.valueOf(this.headlineAsLink);
		values[2] = this.datePattern;
		values[3] = Boolean.valueOf(this.cacheEnabled);
		values[4] = Boolean.valueOf(this.showDate);
		values[5] = Boolean.valueOf(this.showTime);
		values[6] = Boolean.valueOf(this.partOfArticlesList);
		values[7] = Boolean.valueOf(this.canInitAnyField);
		return values;
	}

	/**
	 * @see javax.faces.component.UIComponentBase#restoreState(javax.faces.context.FacesContext,
	 *      java.lang.Object)
	 */
	@Override
	public void restoreState(FacesContext ctx, Object state) {
		Object values[] = (Object[]) state;
		super.restoreState(ctx, values[0]);
		this.headlineAsLink=((Boolean)values[1]).booleanValue();
		this.datePattern = (String)values[2];
		this.cacheEnabled = values[3] == null ? true : ((Boolean) values[3]).booleanValue();
		this.showDate = values[4] == null ? true : ((Boolean) values[4]).booleanValue();
		this.showTime = values[5] == null ? true : ((Boolean) values[5]).booleanValue();
		this.partOfArticlesList = values[6] == null ? false : Boolean.valueOf(values[6].toString());
		this.canInitAnyField = values[7] == null ? true : Boolean.valueOf(values[7].toString());
	}

	/**
	 *
	 */
	@Override
	public void updateToolbar() {
		ContentItemToolbar toolbar = getToolbar();
		if (toolbar != null) {
			toolbar.setActionHandlerIdentifier(ArticleActionURIHandler.HANDLER_IDENTIFIER);
		}
		super.updateToolbar();
	}
	
	

	/* (non-Javadoc)
	 * @see com.idega.content.presentation.ContentItemListViewer#getCacher(javax.faces.context.FacesContext)
	 */
	@Override
	public UIComponentCacher getCacher(FacesContext context) {
		IWMainApplication iwma = IWMainApplication.getIWMainApplication(context);
		return ArticleCacher.getInstance(iwma);
	}

	public boolean isShowAuthor() {
		return showAuthor;
	}

	public void setShowAuthor(boolean showAuthor) {
		this.showAuthor = showAuthor;
	}

	public boolean isShowCreationDate() {
		return showCreationDate;
	}

	public void setShowCreationDate(boolean showCreationDate) {
		this.showCreationDate = showCreationDate;
	}

	public boolean isShowHeadline() {
		return showHeadline;
	}

	public void setShowHeadline(boolean showHeadline) {
		this.showHeadline = showHeadline;
	}

	public boolean isShowTeaser() {
		return showTeaser;
	}

	public void setShowTeaser(boolean showTeaser) {
		this.showTeaser = showTeaser;
	}

	public boolean isShowBody() {
		return showBody;
	}

	public void setShowBody(boolean showBody) {
		this.showBody = showBody;
	}

	public void setShowDetailsCommand(boolean showDetailsCommand) {
		setRenderDetailsCommand(showDetailsCommand);
	}

	@Override
	protected void initializeComments(FacesContext context) {
		super.initializeComments(context);
	}

	@Override
	protected void updateComments() {
		if (isAddCommentsViewer()) {
			CommentsViewer comments = new CommentsViewer();
			comments.setUsedInArticleList(true);
			comments.setShowCommentsForAllUsers(true);
			comments.setLinkToComments(getLinkToComments());
			getFacets().put(ContentItemViewer.FACET_ITEM_COMMENTS, comments);
		}
		super.updateComments();
	}

	public String getLinkToComments() {
		return (String) getValue(ArticleLocalizedItemBean.FIELDNAME_LINK_TO_COMMENT);
	}

	public void setLinkToComments(String linkToComments) {
		setValue(ArticleLocalizedItemBean.FIELDNAME_LINK_TO_COMMENT, linkToComments);
	}

	/*private boolean addFeed(IWContext iwc) {
		return ArticleUtil.addArticleFeedFacet(iwc, getFacets());
	}*/

	protected boolean isAddCommentsViewer() {
		return addCommentsViewer;
	}

	protected void setAddCommentsViewer(boolean addCommentsViewer) {
		this.addCommentsViewer = addCommentsViewer;
	}

	private void prepareToActAsCustomArticleViewer(String resourcePath, boolean canInitAnyField) {
		setResourcePath(resourcePath);
		setShowBody(true);
		setShowHeadline(true);
		setShowTeaser(false);
		this.canInitAnyField = canInitAnyField;
	}

	@Override
	public void encodeBegin(FacesContext context) throws IOException {
		IWContext iwc = IWContext.getIWContext(context);
		PresentationUtil.addStyleSheetToHeader(iwc, ArticleUtil.getBundle().getVirtualPathWithFileNameString("style/article.css"));

		String resourcePathFromRequest = iwc.getParameter(ContentViewer.PARAMETER_CONTENT_RESOURCE);
		if (getResourcePath() == null && resourcePathFromRequest != null) {
			setResourcePath(resourcePathFromRequest);
		}

		String articleItemViewerFilter = getArticleItemViewerFilter();
		String viewerIdentifierFromRequest = iwc.getParameter(ContentConstants.CONTENT_ITEM_VIEWER_IDENTIFIER_PARAMETER);
		if (viewerIdentifierFromRequest != null) {
			//	Identifier is set in request! Checking if it matches with object's parameter
			if (articleItemViewerFilter != null && viewerIdentifierFromRequest.equals(articleItemViewerFilter)) {
				//	Identifiers match, checking if new resource path is provided
				if (!isPartOfArticlesList()) {	//	If part of list (and identifiers matches) then we can not change list's item
					if (resourcePathFromRequest != null) {
						//	New resource path needs to be set
						prepareToActAsCustomArticleViewer(resourcePathFromRequest, true);
					}
				}
			}
		}
		else {
			if (articleItemViewerFilter == null) {
				//	No identifier set, checking if resource path is provided in request
				if (resourcePathFromRequest != null) {
					//	New resource path is set
					String resourcePath = getResourcePath();
					if (resourcePath == null) {
						prepareToActAsCustomArticleViewer(resourcePathFromRequest, true);
					}
					else if (!resourcePathFromRequest.equals(resourcePath) && viewerIdentifierFromRequest == null) {
						prepareToActAsCustomArticleViewer(resourcePath, false);
					}
				}
			}
		}

		if (ContentUtil.hasContentEditorRoles(iwc)) {
			Object renderingArticlesList = iwc.getSessionAttribute(ContentConstants.RENDERING_COMPONENT_OF_ARTICLE_LIST);
			if (renderingArticlesList == null) {
				iwc.setSessionAttribute(ContentConstants.RENDERING_COMPONENT_OF_ARTICLE_LIST, Boolean.FALSE);
				canModifyRenderingAttribute = true;
			}

			if (CoreUtil.isSingleComponentRenderingProcess(iwc)) {
				Layer script = new Layer();
				script.add(ArticleUtil.getSourcesAndActionForArticleEditor());
				getFacets().put(ContentItemViewer.FACET_JAVA_SCRIPT, script);
			}
			else {
				ArticleUtil.removeLazyScript(iwc, this);

				if (renderingArticlesList == null || !((Boolean) renderingArticlesList)) {
					PresentationUtil.addStyleSheetsToHeader(iwc, ArticleUtil.getCSSFilesForArticle());
					PresentationUtil.addJavaScriptSourcesLinesToHeader(iwc, ArticleUtil.getJavaScriptFilesForArticle());
					PresentationUtil.addJavaScriptActionToBody(iwc, ArticleUtil.getArticleEditorInitializerAction(true));
				}
			}

			String resourcePath = getResourcePath();
			if (!StringUtil.isEmpty(resourcePath)) {
				if (resourcePath.startsWith(CoreConstants.WEBDAV_SERVLET_URI)) {
					resourcePath = resourcePath.replaceFirst(CoreConstants.WEBDAV_SERVLET_URI, CoreConstants.EMPTY);
				}
				if (!resourcePath.endsWith(CoreConstants.SLASH)) {
					resourcePath = new StringBuilder(resourcePath).append(CoreConstants.SLASH).toString();
				}

				HiddenInput identifier = new HiddenInput(ContentConstants.CONTENT_ITEM_IDENTIFIER_NAME, resourcePath);
				identifier.setStyleClass(ContentConstants.CONTENT_ITEM_IDENTIFIER_STYLE_CLASS);
				this.getFacets().put(ContentConstants.CONTENT_ITEM_IDENTIFIER_NAME, identifier);
			}
		}

		super.encodeBegin(context);
	}

	@Override
	public void encodeEnd(FacesContext context) throws IOException {
		super.encodeEnd(context);

		IWContext iwc = IWContext.getIWContext(context);
		if (ContentUtil.hasContentEditorRoles(iwc)) {
			if (canModifyRenderingAttribute) {
				iwc.removeSessionAttribute(ContentConstants.RENDERING_COMPONENT_OF_ARTICLE_LIST);
			}
		}
	}

	public boolean isPartOfArticlesList() {
		return partOfArticlesList;
	}

	public void setPartOfArticlesList(boolean partOfArticlesList) {
		this.partOfArticlesList = partOfArticlesList;
	}

	@Override
	public String getArticleItemViewerFilter() {
		return super.getArticleItemViewerFilter();
	}

	@Override
	public void setArticleItemViewerFilter(String articleItemViewerFilter) {
		super.setArticleItemViewerFilter(articleItemViewerFilter);
	}

	public boolean isCanInitAnyField() {
		return canInitAnyField;
	}

}