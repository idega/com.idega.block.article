/*
 * $Id: ArticleUtil.java,v 1.1 2005/02/14 15:16:34 gummi Exp $
 * Created on 7.2.2005
 *
 * Copyright (C) 2005 Idega Software hf. All Rights Reserved.
 *
 * This software is the proprietary information of Idega hf.
 * Use is subject to license terms.
 */
package com.idega.block.article.business;

import com.idega.content.business.ContentUtil;


/**
 * 
 *  Last modified: $Date: 2005/02/14 15:16:34 $ by $Author: gummi $
 * 
 * @author <a href="mailto:gummi@idega.com">Gudmundur Agust Saemundsson</a>
 * @version $Revision: 1.1 $
 */
public class ArticleUtil extends ContentUtil{
	

	protected static final String ARTICLE_CONTENT_PATH = "/article";
	
	/**
	 * 
	 */
	public ArticleUtil() {
		super();
	}
	
	public static String getArticleRootPath(){
		return ContentUtil.getContentRootPath()+ARTICLE_CONTENT_PATH;
	}
	
}
