/*
 * $Id: ArticleItemViewerTag.java,v 1.3 2008/02/20 14:09:55 laddi Exp $
 * Created on 21.2.2005
 *
 * Copyright (C) 2005 Idega Software hf. All Rights Reserved.
 *
 * This software is the proprietary information of Idega hf.
 * Use is subject to license terms.
 */
package com.idega.block.article.component;

import javax.faces.component.UIComponent;

import com.idega.content.presentation.ContentItemViewerTag;

/**
 * 
 *  Last modified: $Date: 2008/02/20 14:09:55 $ by $Author: laddi $
 * 
 * @author <a href="mailto:gummi@idega.com">Gudmundur Agust Saemundsson</a>
 * @version $Revision: 1.3 $
 */
public class ArticleItemViewerTag extends ContentItemViewerTag {

	private boolean headlineAsLink = false;
	private boolean showAuthor = true;
	private boolean showCreationDate = true;
	private String datePattern = null;
	
	/**
	 * 
	 */
	public ArticleItemViewerTag() {
		super();
	}

	/* (non-Javadoc)
	 * @see javax.faces.webapp.UIComponentTag#getComponentType()
	 */
	@Override
	public String getComponentType() {
		return "ArticleItemView";
	}

	@Override
	protected void setProperties(UIComponent component) {
		if (component instanceof ArticleItemViewer) {
			super.setProperties(component);
			ArticleItemViewer article = (ArticleItemViewer) component;
			article.setHeadlineAsLink(isHeadlineAsLink());
			article.setShowAuthor(isShowAuthor());
			article.setShowCreationDate(isShowCreationDate());
			article.setDatePattern(getDatePattern());
		}
	}
	
	@Override
	public void release() {   
		super.release();
		showAuthor = true;
		showCreationDate = true;
		datePattern = null;
	}
	
	public boolean isHeadlineAsLink() {
		return headlineAsLink;
	}

	public void setHeadlineAsLink(boolean headlineAsLink) {
		this.headlineAsLink = headlineAsLink;
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
	
	public String getDatePattern() {
		return datePattern;
	}
	
	public void setDatePattern(String datePattern) {
		this.datePattern = datePattern;
	}
}
