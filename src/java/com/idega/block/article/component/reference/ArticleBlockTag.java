/*
 * $Id: ArticleBlockTag.java,v 1.1 2004/12/21 15:47:12 joakim Exp $
 *
 * Copyright (C) 2004 Idega. All Rights Reserved.
 *
 * This software is the proprietary information of Idega.
 * Use is subject to license terms.
 *
 */
package com.idega.block.article.component.reference;

import javax.faces.webapp.UIComponentTag;

/**
 * JSP tag for content system test/demo page. 
 * <p>
 * Last modified: $Date: 2004/12/21 15:47:12 $ by $Author: joakim $
 *
 * @author Anders Lindman
 * @version $Revision: 1.1 $
 */
public class ArticleBlockTag extends UIComponentTag {
	
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
		return "ArticleBlock";
	}
}