/*
 * $Id: ArticleListViewer.java,v 1.5.2.2 2008/01/03 10:44:20 civilis Exp $ Created on 24.1.2005
 * 
 * Copyright (C) 2005 Idega Software hf. All Rights Reserved.
 * 
 * This software is the proprietary information of Idega hf. Use is subject to license terms.
 */
package com.idega.block.article.component;

import javax.faces.context.FacesContext;
import com.idega.block.article.ArticleCacher;
import com.idega.block.article.bean.ArticleListManagedBean;
import com.idega.block.article.business.ArticleUtil;
import com.idega.content.presentation.ContentItemListViewer;
import com.idega.core.cache.UIComponentCacher;
import com.idega.idegaweb.IWMainApplication;

/**
 * <p>
 * Specialized implementation of contentItemListViewer that sets a few default properties for the article module.
 * </p>
 * 
 * Last modified: $Date: 2008/01/03 10:44:20 $ by $Author: civilis $
 * 
 * @author <a href="mailto:tryggvi@idega.com">Tryggvi Larusson</a>
 * @version $Revision: 1.5.2.2 $
 */
public class ArticleListViewer extends ContentItemListViewer {

	// constants:
	final static String ARTICLE_LIST_BEAN = "articleItemListBean";
	final static String DEFAULT_RESOURCE_PATH = ArticleUtil.getArticleBaseFolderPath();
	// instance variables:
	boolean headlineAsLink = false;
	String datePattern = null;

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
		Object values[] = new Object[3];
		values[0] = super.saveState(ctx);
		values[1] = Boolean.valueOf(this.headlineAsLink);
		values[2] = this.datePattern;
		return values;
	}

	/**
	 * @see javax.faces.component.UIComponentBase#restoreState(javax.faces.context.FacesContext, java.lang.Object)
	 */
	public void restoreState(FacesContext ctx, Object state) {
		Object values[] = (Object[]) state;
		super.restoreState(ctx, values[0]);
		this.headlineAsLink = values[1] == null ? false : ((Boolean) values[1]).booleanValue();
		this.datePattern = (String)values[2];
	}

	public void setHeadlineAsLink(boolean asLink) {
		this.headlineAsLink = asLink;
	}

	public boolean getHeadlineAsLink() {
		return this.headlineAsLink;
	}

	public void setDatePattern(String pattern) {
		this.datePattern = pattern;
	}

	public String getDatePattern() {
		return this.datePattern;
	}

	protected void notifyManagedBeanOfVariableValues() {
		super.notifyManagedBeanOfVariableValues();
		getArticleListBean().setHeadlineAsLink(getHeadlineAsLink());
		getArticleListBean().setDatePattern(getDatePattern());
	}

	public ArticleListManagedBean getArticleListBean() {
		return (ArticleListManagedBean) super.getManagedBean();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.idega.content.presentation.ContentItemListViewer#getCacher(javax.faces.context.FacesContext)
	 */
	public UIComponentCacher getCacher(FacesContext context) {
		IWMainApplication iwma = IWMainApplication.getIWMainApplication(context);
		return ArticleCacher.getInstance(iwma);
	}
}
