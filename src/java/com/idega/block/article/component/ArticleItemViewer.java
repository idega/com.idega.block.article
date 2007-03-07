/*
 * $Id: ArticleItemViewer.java,v 1.26 2007/03/07 17:14:48 justinas Exp $
 *
 * Copyright (C) 2004 Idega. All Rights Reserved.
 *
 * This software is the proprietary information of Idega.
 * Use is subject to license terms.
 */
package com.idega.block.article.component;

import java.rmi.RemoteException;
import java.sql.Timestamp;

import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.context.FacesContext;

import com.idega.block.article.ArticleCacher;
import com.idega.block.article.bean.ArticleItemBean;
import com.idega.block.article.bean.ArticleLocalizedItemBean;
import com.idega.block.article.business.ArticleActionURIHandler;
import com.idega.content.bean.ContentItem;
import com.idega.content.presentation.ContentItemToolbar;
import com.idega.content.presentation.ContentItemViewer;
import com.idega.core.builder.business.BuilderService;
import com.idega.core.builder.business.BuilderServiceFactory;
import com.idega.core.cache.UIComponentCacher;
import com.idega.idegaweb.IWMainApplication;
import com.idega.presentation.IWContext;
import com.idega.presentation.Script;
import com.idega.webface.WFHtml;
import com.idega.webface.convert.WFTimestampConverter;

/**
 * Last modified: $Date: 2007/03/07 17:14:48 $ by $Author: justinas $
 *
 * Displays the article item
 *
 * @author <a href="mailto:gummi@idega.com">Gudmundur Agust Saemundsson</a>
 * @version $Revision: 1.26 $
 */
public class ArticleItemViewer extends ContentItemViewer {
	
	//constants:
	private final static String ATTRIBUTE_AUTHOR = "author";
	private final static String ATTRIBUTE_CREATION_DATE = "creation_date";
	private final static String ATTRIBUTE_HEADLINE = "headline";
	private final static String ATTRIBUTE_TEASER = "teaser";
	private final static String ATTRIBUTE_BODY = "body";
	private final static String[] ATTRIBUTE_ARRAY = new String[] {ATTRIBUTE_AUTHOR,ATTRIBUTE_CREATION_DATE,ATTRIBUTE_HEADLINE,ATTRIBUTE_TEASER,ATTRIBUTE_BODY};
	private final static String facetIdPrefix = "article_";
	private final static String styleClassPrefix = "article_";
	private final static String DEFAULT_STYLE_CLASS = styleClassPrefix + "item";
	//instance variables:
	private boolean headlineAsLink;
	private boolean cacheEnabled=true;
	
	private boolean showAuthor = true;
	private boolean showCreationDate = true;
//	private boolean showComments = false;
//	private boolean showCommentsList = false;
//	private boolean forumPage = false;
//	private boolean showCommentsForAllUsers = true;
	
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
	
	
	
	public String[] getViewerFieldNames(){
		return ATTRIBUTE_ARRAY;
	}
	
	/**
	 * @return Returns the facetIdPrefix.
	 */
	protected String getFacetIdPrefix() {
		return facetIdPrefix;
	}

	/**
	 * @return Returns the styleClassPrefix.
	 */
	protected String getDefaultStyleClassPrefix() {
		return styleClassPrefix;
	}
	
	@SuppressWarnings("unchecked")
	protected UIComponent createFieldComponent(String attribute){
		if(ATTRIBUTE_BODY.equals(attribute)){
			return new WFHtml();
		}
		else if(ATTRIBUTE_TEASER.equals(attribute)){
			return new WFHtml();
		}
		else if(attribute.equals(ATTRIBUTE_HEADLINE)&&this.getHeadlineAsLink()){
			UIComponent link = getEmptyMoreLink();
			HtmlOutputText text = new HtmlOutputText();
			link.getChildren().add(text);
			return link;
		}
		else {
			return new HtmlOutputText();
		}
	}
	
	protected void initializeComponent(FacesContext context) {
		String attr[] = getViewerFieldNames();
		if (attr == null) {
			return;
		}
		for (int i = 0; i < attr.length; i++) {
			if (canInitField(attr[i])) {
				initializeComponent(attr[i]);
			}
		}
		initializeToolbar();
		initializeComments(context);
		if (isShowCreationDate()) {
			((HtmlOutputText)getFieldViewerComponent(ATTRIBUTE_CREATION_DATE)).setConverter(new WFTimestampConverter());
		}
		addFeed(context);
	}

	private boolean canInitField(String attribute) {
		if (attribute == null) {
			return false;
		}
		if (ATTRIBUTE_AUTHOR.equals(attribute) && !isShowAuthor()) {
			return false;
		}
		if (ATTRIBUTE_CREATION_DATE.equals(attribute) && !isShowCreationDate()) {
			return false;
		}
		return true;
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

	/**
	 * @return Returns the body.
	 */
	public String getBody() {
		return (String)getValue(ATTRIBUTE_BODY);
	}
	/**
	 * @param body The body to set.
	 */
	public void setBody(String body) {
		setFieldLocalValue(ATTRIBUTE_BODY,body);
	}

	/**
	 * @return Returns the creationDate.
	 */
	public Timestamp getCreationDate() {
		return (Timestamp)getValue(ATTRIBUTE_CREATION_DATE);
	}
	/**
	 * @param creationDate The creationDate to set.
	 */
	public void setCreationDate(Timestamp creationDate) {
		setFieldLocalValue(ATTRIBUTE_CREATION_DATE,creationDate);
	}

	/**
	 * @return Returns the headline.
	 */
	public String getHeadline() {
		return (String)getValue(ATTRIBUTE_HEADLINE);
	}
	/**
	 * @param headline The headline to set.
	 */
	public void setHeadline(String headline) {
		setFieldLocalValue(ATTRIBUTE_HEADLINE,headline);
	}

	/**
	 * @return Returns the teaser.
	 */
	public String getTeaser() {
		return (String)getValue(ATTRIBUTE_TEASER);
	}
	/**
	 * @param teaser The teaser to set.
	 */
	public void setTeaser(String teaser) {
		setFieldLocalValue(ATTRIBUTE_TEASER,teaser);
	}
	
	
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
	
	
	/**
	 * @see javax.faces.component.UIComponentBase#saveState(javax.faces.context.FacesContext)
	 */
	public Object saveState(FacesContext ctx) {
		Object values[] = new Object[3];
		values[0] = super.saveState(ctx);
		values[1] = Boolean.valueOf(this.headlineAsLink);
		values[2] = Boolean.valueOf(this.cacheEnabled);
		return values;
	}

	/**
	 * @see javax.faces.component.UIComponentBase#restoreState(javax.faces.context.FacesContext,
	 *      java.lang.Object)
	 */
	public void restoreState(FacesContext ctx, Object state) {
		Object values[] = (Object[]) state;
		super.restoreState(ctx, values[0]);
		this.headlineAsLink=((Boolean)values[1]).booleanValue();
		this.cacheEnabled=((Boolean)values[2]).booleanValue();
	}
	
	/**
	 * 
	 */
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
	
	@SuppressWarnings("unchecked")
	protected void initializeComments(FacesContext context) {
	}
	
	protected void updateComments() {
		super.updateComments();
	}
	
	public String getLinkToComments() {
		return (String) getValue(ArticleLocalizedItemBean.FIELDNAME_LINK_TO_COMMENT);
	}
	
	public void setLinkToComments(String linkToComments) {
		setValue(ArticleLocalizedItemBean.FIELDNAME_LINK_TO_COMMENT, linkToComments);
	}
	
	@SuppressWarnings("unchecked")
	private boolean addFeedJavaScript(String linkToFeed, String feedType, String feedTitle) {
		Script script = new Script();
		script.addScriptLine("addFeedSymbolInHeader('"+linkToFeed+"', '"+feedType+"', '"+feedTitle+"');");
		getFacets().put(ContentItemViewer.FACET_FEED_SCRIPT, script);
		return true;
	}

	private void addFeed(FacesContext context){
		IWContext iwc = IWContext.getIWContext(context);
		BuilderService bservice = null;
		try {
			bservice = BuilderServiceFactory.getBuilderService(iwc);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			String serverName = iwc.getServerURL();
			serverName.substring(0, serverName.length()-1);
			String feedUri = bservice.getCurrentPageURI(iwc);
			feedUri.substring(1);
			String linkToFeed = serverName+"rss/article"+feedUri;
			addFeedJavaScript(linkToFeed, "atom", "Atom 1.0");
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
//	public boolean isShowCommentsList() {
//		return showCommentsList;
//	}
//
//	public void setShowCommentsList(boolean showCommentsList) {
//		this.showCommentsList = showCommentsList;
//	}
//
//	public boolean isForumPage() {
//		return forumPage;
//	}
//
//	public void setForumPage(boolean forumPage) {
//		this.forumPage = forumPage;
//	}
//
//	public boolean isShowCommentsForAllUsers() {
//		return showCommentsForAllUsers;
//	}
//
//	public void setShowCommentsForAllUsers(boolean showCommentsForAllUsers) {
//		this.showCommentsForAllUsers = showCommentsForAllUsers;
//	}

}
