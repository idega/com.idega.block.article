/*
 * $Id: ArticleUtil.java,v 1.5 2005/02/21 16:16:19 gummi Exp $
 * Created on 7.2.2005
 *
 * Copyright (C) 2005 Idega Software hf. All Rights Reserved.
 *
 * This software is the proprietary information of Idega hf.
 * Use is subject to license terms.
 */
package com.idega.block.article.business;

import javax.faces.context.FacesContext;
import java.io.File;
import com.idega.content.business.ContentUtil;
import com.idega.idegaweb.IWBundle;
import com.idega.presentation.IWContext;
import com.idega.util.IWTimestamp;


/**
 * 
 *  Last modified: $Date: 2005/02/21 16:16:19 $ by $Author: gummi $
 * 
 * @author <a href="mailto:gummi@idega.com">Gudmundur Agust Saemundsson</a>
 * @version $Revision: 1.5 $
 */
public class ArticleUtil {
	

	protected static final String ARTICLE_CONTENT_PATH = "/article";

	public static final String IW_BUNDLE_IDENTIFIER = "com.idega.block.article";
	public static final String MODULE_PREFIX = "article_";

	private static IWBundle bundle = null;
	
	public static IWBundle getBundle() {
		if (bundle == null) {
			setupBundle();
		}
		return bundle;
	}

	private static void setupBundle() {
		FacesContext context = FacesContext.getCurrentInstance();
		IWContext iwContext = IWContext.getIWContext(context);
		bundle = iwContext.getIWMainApplication().getBundle(IW_BUNDLE_IDENTIFIER);
	}
	
	public static String getContentRootPath(){
		return ContentUtil.getContentRootPath();
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
