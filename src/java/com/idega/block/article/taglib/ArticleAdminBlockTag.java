/*
 * $Id: ArticleAdminBlockTag.java,v 1.4 2007/06/07 21:17:26 palli Exp $
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

import com.idega.block.article.component.ArticleAdminBlock;

/**
 * JSP tag for create article test/demo page. 
 * <p>
 * Last modified: $Date: 2007/06/07 21:17:26 $ by $Author: palli $
 *
 * @author Anders Lindman
 * @version $Revision: 1.4 $
 */
public class ArticleAdminBlockTag extends UIComponentTag {
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
		return "ArticleAdminBlock";
	}
	
	public void release() {      
		super.release();      
		
		this.mode=null;
	}

	protected void setProperties(UIComponent component) {      
		super.setProperties(component);
		if (component != null) {
			ArticleAdminBlock articleBlock = ((ArticleAdminBlock)component);
			if(this.mode!=null){
				articleBlock.setEditMode(this.mode);
			}
		}
	}
	
	public void setMode(String mode) {
		this.mode = mode;
	}
	
}
