/*
 * $Id: ListArticlesBlockTag.java,v 1.1 2005/01/04 11:42:51 joakim Exp $
 *
 * Copyright (C) 2004 Idega. All Rights Reserved.
 *
 * This software is the proprietary information of Idega.
 * Use is subject to license terms.
 *
 */
package com.idega.block.article.taglib;

import javax.faces.webapp.UIComponentTag;

/**
 * JSP tag for content system test/demo page. 
 * <p>
 * Last modified: $Date: 2005/01/04 11:42:51 $ by $Author: joakim $
 *
 * @author Anders Lindman
 * @version $Revision: 1.1 $
 */
public class ListArticlesBlockTag extends UIComponentTag {
	
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
		return "ListArticlesBlock";
	}
}
