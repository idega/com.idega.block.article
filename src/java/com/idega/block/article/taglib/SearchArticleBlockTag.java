/*
 * $Id: SearchArticleBlockTag.java,v 1.1 2005/03/05 16:27:00 eiki Exp $
 * Created on Mar 2, 2005
 *
 * Copyright (C) 2005 Idega Software hf. All Rights Reserved.
 *
 * This software is the proprietary information of Idega hf.
 * Use is subject to license terms.
 */
package com.idega.block.article.taglib;

import com.idega.webface.WFBlockTag;


public class SearchArticleBlockTag extends WFBlockTag {

	public SearchArticleBlockTag() {
		super();
	}
	
	/**
	 * @see javax.faces.webapp.UIComponentTag#getComponentType()
	 */
	public String getComponentType() {
		return "ArticleSearch";
	}
}
