/*
 * $Id: CreateArticlePageTag.java,v 1.2 2005/03/07 15:31:54 joakim Exp $
 *
 * Copyright (C) 2004 Idega. All Rights Reserved.
 *
 * This software is the proprietary information of Idega.
 * Use is subject to license terms.
 *
 */
package com.idega.block.article.taglib;

import javax.faces.component.UIComponent;
import javax.faces.webapp.UIComponentTag;
import com.idega.block.article.ArticlePage;

/**
 * JSP tag for create article test/demo page. 
 * <p>
 * Last modified: $Date: 2005/03/07 15:31:54 $ by $Author: joakim $
 *
 * @author Anders Lindman
 * @version $Revision: 1.2 $
 */
public class CreateArticlePageTag extends UIComponentTag {
	String mode=null;
	
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
		return "CreateArticlePage";
	}
	
	public void release() {      
		super.release();      
		
		mode=null;
	}

	protected void setProperties(UIComponent component) {      
		super.setProperties(component);
		if (component != null) {
			ArticlePage articlePage = ((ArticlePage)component);
			articlePage.setEditMode(mode);
		}
	}
	
	public void setMode(String mode) {
		this.mode = mode;
	}
	
}
