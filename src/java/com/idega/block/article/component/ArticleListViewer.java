/*
 * $Id: ArticleListViewer.java,v 1.17 2008/02/20 15:48:11 laddi Exp $
 * Created on 24.1.2005
 *
 * Copyright (C) 2005 Idega Software hf. All Rights Reserved.
 *
 * This software is the proprietary information of Idega hf.
 * Use is subject to license terms.
 */
package com.idega.block.article.component;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import com.idega.block.article.ArticleCacher;
import com.idega.block.article.bean.ArticleListManagedBean;
import com.idega.block.article.business.ArticleUtil;
import com.idega.content.business.ContentConstants;
import com.idega.content.business.ContentUtil;
import com.idega.content.presentation.ContentItemListViewer;
import com.idega.content.presentation.ContentItemViewer;
import com.idega.content.renderkit.ContentListViewerRenderer;
import com.idega.core.builder.business.BuilderService;
import com.idega.core.builder.business.BuilderServiceFactory;
import com.idega.core.cache.UIComponentCacher;
import com.idega.idegaweb.IWMainApplication;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.Script;
import com.idega.util.CoreUtil;
import com.idega.util.PresentationUtil;


/**
 * <p>
 * Specialized implementation of contentItemListViewer that sets a few default properties
 * for the article module.
 * </p>
 * 
 *  Last modified: $Date: 2008/02/20 15:48:11 $ by $Author: laddi $
 * 
 * @author <a href="mailto:tryggvi@idega.com">Tryggvi Larusson</a>
 * @version $Revision: 1.17 $
 */
public class ArticleListViewer extends ContentItemListViewer {

	//constants:
	final static String ARTICLE_LIST_BEAN="articleItemListBean";
	final static String DEFAULT_RESOURCE_PATH=ArticleUtil.getArticleBaseFolderPath();
	//instance variables:
	boolean headlineAsLink=false;
	String datePattern = null;
	boolean showDate = true;
	boolean showTime = true;

	private boolean showAuthor = true;
	private boolean showCreationDate = true;
	private boolean showTeaser = true;
	private boolean showBody = true;
	
	private boolean showComments = false;
	private static final String SHOW_COMMENTS_PROPERTY = "showComments";
	
	private String linkToRSS = null;
	
	/**
	 * 
	 */
	public ArticleListViewer() {
		super();
		setBeanIdentifier(ARTICLE_LIST_BEAN);
		setBaseFolderPath(DEFAULT_RESOURCE_PATH);
	}
	
	/**
	 * @see javax.faces.component.UIComponentBase#saveState(javax.faces.context.FacesContext)
	 */
	@Override
	public Object saveState(FacesContext ctx) {
		Object values[] = new Object[5];
		values[0] = super.saveState(ctx);
		values[1] = Boolean.valueOf(this.headlineAsLink);
		values[2] = this.datePattern;
		values[3] = Boolean.valueOf(this.showDate);
		values[4] = Boolean.valueOf(this.showTime);
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
		this.showDate = values[3] == null ? true : ((Boolean) values[3]).booleanValue();
		this.showTime = values[4] == null ? true : ((Boolean) values[4]).booleanValue();
	}
	
	public void setHeadlineAsLink(boolean asLink){
		this.headlineAsLink=asLink;
	}

	public boolean getHeadlineAsLink(){
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
	
	@Override
	protected void notifyManagedBeanOfVariableValues(){
		super.notifyManagedBeanOfVariableValues();
		getArticleListBean().setHeadlineAsLink(getHeadlineAsLink());
		getArticleListBean().setDatePattern(getDatePattern());
		getArticleListBean().setShowDate(isShowDate());
		getArticleListBean().setShowTime(isShowTime());
		getArticleListBean().setShowAuthor(isShowAuthor());
		getArticleListBean().setShowCreationDate(isShowCreationDate());
		getArticleListBean().setShowTeaser(isShowTeaser());
		getArticleListBean().setShowBody(isShowBody());
	}
	
	public ArticleListManagedBean getArticleListBean(){
		return (ArticleListManagedBean)super.getManagedBean();
	}

	/* (non-Javadoc)
	 * @see com.idega.content.presentation.ContentItemListViewer#getCacher(javax.faces.context.FacesContext)
	 */
	@Override
	public UIComponentCacher getCacher(FacesContext context) {
		IWMainApplication iwma = IWMainApplication.getIWMainApplication(context);
		return ArticleCacher.getInstance(iwma);
	}

	public boolean isShowComments() {
		return showComments;
	}

	public void setShowComments(boolean showComments) {
		this.showComments = showComments;
	}

	public String getLinkToRSS() {
		return linkToRSS;
	}

	public void setLinkToRSS(String linkToRSS) {
		this.linkToRSS = linkToRSS;
	}
	
	protected void initializeComponent(FacesContext context) {
		addFeed(context);
	}
	
	@SuppressWarnings("unchecked")
	private void addCommentsController(IWContext iwc, CommentsViewer comments) {
		if (!ContentUtil.hasContentEditorRoles(iwc) || !ArticleUtil.isPageTypeBlog(iwc)) {
			return;
		}
		
		BuilderService builder = null;
		try {
			builder = BuilderServiceFactory.getBuilderService(iwc);
		} catch (RemoteException e) {
			e.printStackTrace();
			return;
		}
		
		String moduleId = builder.getInstanceId(this);
		if (moduleId == null) {
			moduleId = this.getId();
		}
		
		ArticleCacher cacher = ArticleCacher.getInstance(iwc.getIWMainApplication());
		if (cacher == null) {
			return;
		}
		
		UIComponent commentsController = comments.getCommentsController(iwc, cacher.getCacheKey(this, iwc), moduleId, isShowComments(), SHOW_COMMENTS_PROPERTY);
		getFacets().put(ContentListViewerRenderer.FACET_ITEM_COMMENTS_CONTROLLER, commentsController);
	}
	
	private void addFeed(FacesContext context){
		IWContext iwc = IWContext.getIWContext(context);
		BuilderService bservice = null;
		try {
			bservice = BuilderServiceFactory.getBuilderService(iwc);
		} catch (RemoteException e) {
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
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	private boolean addFeedJavaScript(String linkToFeed, String feedType, String feedTitle) {
		Script script = new Script();
		script.addScriptLine("registerEvent(window, 'load', function(){addFeedSymbolInHeader('"+linkToFeed+"', '"+feedType+"', '"+feedTitle+"');});");		
		getFacets().put(ContentItemViewer.FACET_FEED_SCRIPT, script);
		return true;
	}
	
	@SuppressWarnings("unchecked")
	private void addCommentsScript(IWContext iwc, CommentsViewer comments) {
		if (ArticleUtil.isPageTypeBlog(iwc)) {
			List<String> sources = comments.getJavaScriptSources(iwc);
			List<String> actions = comments.getJavaScriptActions();
			if (CoreUtil.isSingleComponentRenderingProcess(iwc)) {
				Layer script = new Layer();
				script.add(PresentationUtil.getJavaScriptSourceLines(sources));
				script.add(PresentationUtil.getJavaScriptActions(actions));
				getFacets().put(ContentItemViewer.FACET_COMMENTS_SCRIPTS, script);
			}
			else {
				PresentationUtil.addJavaScriptSourcesLinesToHeader(iwc, sources);
				PresentationUtil.addJavaScriptActionsToBody(iwc, actions);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void encodeBegin(FacesContext context) throws IOException {
		IWContext iwc = IWContext.getIWContext(context);
		
		CommentsViewer comments = new CommentsViewer();
		addCommentsScript(iwc, comments);
		addCommentsController(iwc, comments);
		
		if (ContentUtil.hasContentEditorRoles(iwc)) {
			iwc.setSessionAttribute(ContentConstants.RENDERING_COMPONENT_OF_ARTICLE_LIST, Boolean.TRUE);

			if (CoreUtil.isSingleComponentRenderingProcess(iwc)) {
				Layer script = new Layer();
				script.add(PresentationUtil.getJavaScriptSourceLines(ArticleUtil.getJavaScriptSourcesForArticleEditor(iwc, true)));
				getFacets().put(ContentItemViewer.FACET_JAVA_SCRIPT, script);
			}
			else {
				PresentationUtil.addJavaScriptSourcesLinesToHeader(iwc, ArticleUtil.getJavaScriptSourcesForArticleEditor(iwc, false));
				PresentationUtil.addStyleSheetsToHeader(iwc, ArticleUtil.getStyleSheetsSourcesForArticleEditor(iwc));
			}
		}
		
		super.encodeBegin(context);
	}
	
	@Override
	public void encodeEnd(FacesContext context) throws IOException {
		super.encodeEnd(context);
		
		IWContext iwc = IWContext.getIWContext(context);
		if (ContentUtil.hasContentEditorRoles(iwc)) {
			iwc.removeSessionAttribute(ContentConstants.RENDERING_COMPONENT_OF_ARTICLE_LIST);
		}
	}
		
	@Override
	public void encodeChildren(FacesContext context) throws IOException {
		super.encodeChildren(context);
	}
	
	@Override
	protected void addContentItemViewer(ContentItemViewer viewer) {
		FacesContext context = FacesContext.getCurrentInstance();
		IWContext iwc = IWContext.getIWContext(context);
		boolean addComments = false;
		if (ArticleUtil.isPageTypeBlog(iwc) && ContentUtil.hasContentEditorRoles(iwc)) {
			addComments = true;
		}
		else {
			addComments = isShowComments();
		}
		if (addComments) {
			ArticleItemViewer article = null;
			if (viewer instanceof ArticleItemViewer) {
				article = (ArticleItemViewer) viewer;
				article.setAddCommentsViewer(true);
				super.addContentItemViewer(article);
				return;
			}
		}
		super.addContentItemViewer(viewer);
	}

}