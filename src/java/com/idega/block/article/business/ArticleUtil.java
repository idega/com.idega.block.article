/*
 * $Id: ArticleUtil.java,v 1.10 2007/08/02 13:35:11 valdas Exp $
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
import java.rmi.RemoteException;

import com.idega.content.business.ContentUtil;
import com.idega.core.builder.business.BuilderService;
import com.idega.core.builder.business.BuilderServiceFactory;
import com.idega.core.builder.data.ICPage;
import com.idega.idegaweb.IWBundle;
import com.idega.presentation.IWContext;
import com.idega.util.CoreConstants;

/**
 * 
 *  Last modified: $Date: 2007/08/02 13:35:11 $ by $Author: valdas $
 * 
 * @author <a href="mailto:gummi@idega.com">Gudmundur Agust Saemundsson</a>
 * @version $Revision: 1.10 $
 */
public class ArticleUtil {

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
		bundle = iwContext.getIWMainApplication().getBundle(ArticleConstants.IW_BUNDLE_IDENTIFIER);
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
		return ContentUtil.getContentBaseFolderPath() + CoreConstants.ARTICLE_CONTENT_PATH;
	}
	
	public static String getFilenameFromPath(String path) {
		File file = new File(path);
		return file.getName();
	}
	
	public static boolean isPageTypeBlog(IWContext iwc) {
		if (iwc == null) {
			return false;
		}
		int id = iwc.getCurrentIBPageID();
		if (id < 0) {
			return false;
		}
		BuilderService builder = null;
		try {
			builder = BuilderServiceFactory.getBuilderService(iwc);
		} catch (RemoteException e) {
			e.printStackTrace();
			return false;
		}
		return isPageTypeBlog(builder.getICPage(String.valueOf(id)));
	}
	
	public static boolean isPageTypeBlog(ICPage page) {
		if (page == null) {
			return false;
		}
		if ("blog".equals(page.getSubType())) {
			return true;
		}
		return false;
	}
}
