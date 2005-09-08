/*
 * $Id: ArticleListViewer.java,v 1.1 2005/09/08 23:00:57 tryggvil Exp $
 * Created on 24.1.2005
 *
 * Copyright (C) 2005 Idega Software hf. All Rights Reserved.
 *
 * This software is the proprietary information of Idega hf.
 * Use is subject to license terms.
 */
package com.idega.block.article.component;

import com.idega.content.presentation.ContentItemListViewer;


/**
 * <p>
 * Specialized implementation of contentItemListViewer that sets a few default properties
 * for the article module.
 * </p>
 * 
 *  Last modified: $Date: 2005/09/08 23:00:57 $ by $Author: tryggvil $
 * 
 * @author <a href="mailto:tryggvi@idega.com">Tryggvi Larusson</a>
 * @version $Revision: 1.1 $
 */
public class ArticleListViewer extends ContentItemListViewer {

	final static String ARTICLE_LIST_BEAN="articleItemListBean";
	final static String DEFAULT_RESOURCE_PATH="/files/cms/articles";
	
	/**
	 * 
	 */
	public ArticleListViewer() {
		super();
		setBeanIdentifier(ARTICLE_LIST_BEAN);
		setResourcePath(DEFAULT_RESOURCE_PATH);
	}
}
