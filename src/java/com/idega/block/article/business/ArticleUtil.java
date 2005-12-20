/*
 * $Id: ArticleUtil.java,v 1.6 2005/12/20 16:40:42 tryggvil Exp $
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
 *  Last modified: $Date: 2005/12/20 16:40:42 $ by $Author: tryggvil $
 * 
 * @author <a href="mailto:gummi@idega.com">Gudmundur Agust Saemundsson</a>
 * @version $Revision: 1.6 $
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
		return ContentUtil.getContentBaseFolderPath();
	}

	/**
	 * <p>
	 * This article returns the standard root or 'baseFolderPath' for articles.<br/>
	 * By default this is /files/cms/article
	 * </p>
	 * @return
	 */
	public static String getArticleBaseFolderPath(){
		return ContentUtil.getContentBaseFolderPath()+ARTICLE_CONTENT_PATH;
	}

	/**
	 * @return the path where a new article by default should be created
	 */
	public static String getDefaultArticleYearMonthPath() {
		String folderString = ArticleUtil.getArticleBaseFolderPath();
		return getArticleYearMonthPath(folderString);
	}
	
	/**
	 * @return Appends to the path where a new article by default should be created
	 */
	public static String getArticleYearMonthPath(String basePath) {
		IWTimestamp now = new IWTimestamp();
		String folderString = basePath+"/"+now.getYear()+"/"+now.getDateString("MM");
		return folderString;
	}
	
	
	
	public static String getFilenameFromPath(String path) {
		File file = new File(path);
		return file.getName();
	}
}
