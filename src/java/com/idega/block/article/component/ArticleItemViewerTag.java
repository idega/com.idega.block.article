/*
 * $Id: ArticleItemViewerTag.java,v 1.2 2007/01/19 11:39:31 valdas Exp $
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
 *  Last modified: $Date: 2007/01/19 11:39:31 $ by $Author: valdas $
 * 
 * @author <a href="mailto:gummi@idega.com">Gudmundur Agust Saemundsson</a>
 * @version $Revision: 1.2 $
 */
public class ArticleItemViewerTag extends ContentItemViewerTag {

	private boolean showAuthor = true;
	private boolean showCreationDate = true;
	
	/**
	 * 
	 */
	public ArticleItemViewerTag() {
		super();
	}

	/* (non-Javadoc)
	 * @see javax.faces.webapp.UIComponentTag#getComponentType()
	 */
	public String getComponentType() {
		return "ArticleItemView";
	}

	protected void setProperties(UIComponent component) {
		if (component instanceof ArticleItemViewer) {
			super.setProperties(component);
			ArticleItemViewer article = (ArticleItemViewer) component;
			article.setShowAuthor(isShowAuthor());
			article.setShowCreationDate(isShowCreationDate());
		}
	}
	
	public void release() {   
		super.release();
		showAuthor = true;
		showCreationDate = true;
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
	
}
