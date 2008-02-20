/*
 * $Id: ArticleListViewerTag.java,v 1.2 2008/02/20 14:09:55 laddi Exp $
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
 *  Last modified: $Date: 2008/02/20 14:09:55 $ by $Author: laddi $
 * 
 * @author <a href="mailto:tryggvil@idega.com">tryggvil</a>
 * @version $Revision: 1.2 $
 */
public class ArticleListViewerTag extends ContentItemListViewerTag {

	private String datePattern = null;
	private boolean headlineAsLink = false;
	private boolean showComments = false;

	public static final String COMPONENT_TYPE="ArticleListViewer";
	
	/**
	 * 
	 */
	public ArticleListViewerTag() {
		super();
		// TODO Auto-generated constructor stub
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
	}

	@Override
	protected void setProperties(UIComponent component) {      
		super.setProperties(component);
		if (component != null) {
			ArticleListViewer viewer = ((ArticleListViewer)component);
			viewer.setDatePattern(getDatePattern());
			viewer.setHeadlineAsLink(getHeadlineAsLink());
			viewer.setShowComments(getShowComments());
		}
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
}