/*
 * $Id: ArticleListViewer.java,v 1.3 2005/12/20 16:40:41 tryggvil Exp $
 * Created on 24.1.2005
 *
 * Copyright (C) 2005 Idega Software hf. All Rights Reserved.
 *
 * This software is the proprietary information of Idega hf.
 * Use is subject to license terms.
 */
package com.idega.block.article.component;

import javax.faces.context.FacesContext;
import com.idega.block.article.bean.ArticleListManagedBean;
import com.idega.block.article.business.ArticleUtil;
import com.idega.content.presentation.ContentItemListViewer;


/**
 * <p>
 * Specialized implementation of contentItemListViewer that sets a few default properties
 * for the article module.
 * </p>
 * 
 *  Last modified: $Date: 2005/12/20 16:40:41 $ by $Author: tryggvil $
 * 
 * @author <a href="mailto:tryggvi@idega.com">Tryggvi Larusson</a>
 * @version $Revision: 1.3 $
 */
public class ArticleListViewer extends ContentItemListViewer {

	//constants:
	final static String ARTICLE_LIST_BEAN="articleItemListBean";
	final static String DEFAULT_RESOURCE_PATH=ArticleUtil.getArticleBaseFolderPath();
	//instance variables:
	boolean headlineAsLink=false;
	
	/**
	 * 
	 */
	public ArticleListViewer() {
		super();
		setBeanIdentifier(ARTICLE_LIST_BEAN);
		setBaseFolderPath(DEFAULT_RESOURCE_PATH);
	}
	
	/**
	 * @see javax.faces.component.UIComponentBase#saveState(javax.faces.context.FacesContext)
	 */
	public Object saveState(FacesContext ctx) {
		Object values[] = new Object[2];
		values[0] = super.saveState(ctx);
		values[1] = Boolean.valueOf(headlineAsLink);
		return values;
	}

	/**
	 * @see javax.faces.component.UIComponentBase#restoreState(javax.faces.context.FacesContext,
	 *      java.lang.Object)
	 */
	public void restoreState(FacesContext ctx, Object state) {
		Object values[] = (Object[]) state;
		super.restoreState(ctx, values[0]);
		headlineAsLink=((Boolean)values[1]).booleanValue();
	}
	
	public void setHeadlineAsLink(boolean asLink){
		this.headlineAsLink=asLink;
	}

	public boolean getHeadlineAsLink(){
		return this.headlineAsLink;
	}
	
	protected void notifyManagedBeanOfVariableValues(){
		super.notifyManagedBeanOfVariableValues();
		getArticleListBean().setHeadlineAsLink(getHeadlineAsLink());
	}
	
	public ArticleListManagedBean getArticleListBean(){
		return (ArticleListManagedBean)super.getManagedBean();
	}
}
