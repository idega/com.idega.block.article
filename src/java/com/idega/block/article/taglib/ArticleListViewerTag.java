/*
 * $Id: ArticleListViewerTag.java,v 1.1 2005/09/08 23:00:57 tryggvil Exp $
 * Created on 1.9.2005 in project com.idega.block.article
 *
 * Copyright (C) 2005 Idega Software hf. All Rights Reserved.
 *
 * This software is the proprietary information of Idega hf.
 * Use is subject to license terms.
 */
package com.idega.block.article.taglib;

import javax.faces.component.UIComponent;
import com.idega.content.presentation.ContentItemListViewerTag;


/**
 * <p>
 * TODO tryggvil Describe Type ListArticlesViewerTag
 * </p>
 *  Last modified: $Date: 2005/09/08 23:00:57 $ by $Author: tryggvil $
 * 
 * @author <a href="mailto:tryggvil@idega.com">tryggvil</a>
 * @version $Revision: 1.1 $
 */
public class ArticleListViewerTag extends ContentItemListViewerTag {

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
	public String getComponentType() {
		return COMPONENT_TYPE;
	}
	
	protected void setProperties(UIComponent component) {      
		super.setProperties(component);
	}
}
