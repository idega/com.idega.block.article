/*
 * $Id: ArticleUtil.java,v 1.4 2005/02/18 16:39:31 joakim Exp $
 * Created on 7.2.2005
 *
 * Copyright (C) 2005 Idega Software hf. All Rights Reserved.
 *
 * This software is the proprietary information of Idega hf.
 * Use is subject to license terms.
 */
package com.idega.block.article.business;

import java.io.File;
import com.idega.content.business.ContentUtil;
import com.idega.util.IWTimestamp;


/**
 * 
 *  Last modified: $Date: 2005/02/18 16:39:31 $ by $Author: joakim $
 * 
 * @author <a href="mailto:gummi@idega.com">Gudmundur Agust Saemundsson</a>
 * @version $Revision: 1.4 $
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

	/**
	 * @return the path where a new article by default should be created
	 */
	public static String getArticleYearMonthPath() {
		IWTimestamp now = new IWTimestamp();
		String folderString = ArticleUtil.getArticleRootPath()+"/"+now.getYear()+"/"+now.getDateString("MM");
		return folderString;
	}
	
	public static String getFilenameFromPath(String path) {
		File file = new File(path);
		return file.getName();
	}
}
