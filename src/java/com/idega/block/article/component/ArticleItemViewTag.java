/*
 * $Id: ArticleItemViewTag.java,v 1.2 2004/12/09 14:42:15 joakim Exp $
 *
 * Copyright (C) 2004 Idega. All Rights Reserved.
 *
 * This software is the proprietary information of Idega.
 * Use is subject to license terms.
 */package com.idega.block.article.component;

import javax.faces.webapp.UIComponentTag;


/**
 * Last modified: $Date: 2004/12/09 14:42:15 $ by $Author: joakim $
 *
 * @author Joakim
 * @version $Revision: 1.2 $
 */
public class ArticleItemViewTag extends UIComponentTag {
	
	/**
	 * @see javax.faces.webapp.UIComponentTag#getRendererType()
	 */
	public String getRendererType() {
		return null;
	}
		
	/**
	 * @see javax.faces.webapp.UIComponentTag#getComponentType()
	 */
	public String getComponentType() {
		return "ArticleItemView";
	}
}
