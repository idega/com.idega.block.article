/*
 * $Id: ArticleListViewerTag.java,v 1.6 2008/04/29 10:59:50 valdas Exp $
 * Created on 1.9.2005 in project com.idega.block.article
 *
 * Copyright (C) 2005 Idega Software hf. All Rights Reserved.
 *
 * This software is the proprietary information of Idega hf.
 * Use is subject to license terms.
 */
package com.idega.block.article.taglib;

import javax.faces.component.UIComponent;

import com.idega.block.article.component.ArticleListViewer;
import com.idega.content.presentation.ContentItemListViewerTag;


/**
 * <p>
 * TODO tryggvil Describe Type ListArticlesViewerTag
 * </p>
 *  Last modified: $Date: 2008/04/29 10:59:50 $ by $Author: valdas $
 * 
 * @author <a href="mailto:tryggvil@idega.com">tryggvil</a>
 * @version $Revision: 1.6 $
 */
public class ArticleListViewerTag extends ContentItemListViewerTag {

	private boolean showAuthor = true;
	private boolean showCreationDate = true;
	private boolean showHeadline = true;
	private boolean showTeaser = true;
	private boolean showBody = true;
	private boolean headlineAsLink = false;
	private boolean showComments = false;
	private boolean showDate = true;
	private boolean showTime = true;
	private boolean showAllItems = false;
	
	private Boolean showDetailsCommand = null;
	
	private String datePattern = null;
	private String articleItemViewerFilter = null;

	public static final String COMPONENT_TYPE="ArticleListViewer";
	
	/**
	 * 
	 */
	public ArticleListViewerTag() {
		super();
	}

	/* (non-Javadoc)
	 * @see com.idega.content.presentation.ContentItemListViewerTag#getComponentType()
	 */
	@Override
	public String getComponentType() {
		return COMPONENT_TYPE;
	}
	
	@Override
	public void release() {      
		super.release();      
		this.datePattern = null; 
		this.headlineAsLink = false;
		this.showComments = false;
		this.articleItemViewerFilter = null;
		this.showAllItems = false;
	}

	@Override
	protected void setProperties(UIComponent component) {      
		super.setProperties(component);
		if (component instanceof ArticleListViewer) {
			ArticleListViewer viewer = (ArticleListViewer) component;
			viewer.setShowAuthor(isShowAuthor());
			viewer.setShowCreationDate(isShowCreationDate());
			viewer.setShowHeadline(isShowHeadline());
			viewer.setShowTeaser(isShowTeaser());
			viewer.setShowBody(isShowBody());
			if (isShowDetailsCommand() != null) {
				viewer.setShowDetailsCommand(isShowDetailsCommand().booleanValue());
			}
			viewer.setDatePattern(getDatePattern());
			viewer.setHeadlineAsLink(getHeadlineAsLink());
			viewer.setShowComments(getShowComments());
			viewer.setShowDate(isShowDate());
			viewer.setShowTime(isShowTime());
			viewer.setArticleItemViewerFilter(getArticleItemViewerFilter());
			viewer.setShowAllItems(isShowAllItems());
		}
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

	public Boolean isShowDetailsCommand() {
		return showDetailsCommand;
	}

	public void setShowDetailsCommand(boolean showDetailsCommand) {
		this.showDetailsCommand = Boolean.valueOf(showDetailsCommand);
	}

	public void setDatePattern(String datePattern) {
		this.datePattern = datePattern;
	}
	
	public String getDatePattern() {
		return this.datePattern;
	}
	
	public void setHeadlineAsLink(boolean headlineAsLink) {
		this.headlineAsLink = headlineAsLink;
	}
	
	public boolean getHeadlineAsLink() {
		return this.headlineAsLink;
	}

	public void setShowComments(boolean showComments) {
		this.showComments = showComments;
	}
	
	public boolean getShowComments() {
		return this.showComments;
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

	public String getArticleItemViewerFilter() {
		return articleItemViewerFilter;
	}

	public void setArticleItemViewerFilter(String articleItemViewerFilter) {
		this.articleItemViewerFilter = articleItemViewerFilter;
	}

	public boolean isShowAllItems() {
		return showAllItems;
	}

	public void setShowAllItems(boolean showAllItems) {
		this.showAllItems = showAllItems;
	}
	
}