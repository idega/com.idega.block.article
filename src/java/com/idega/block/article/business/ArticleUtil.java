/*
 * $Id: ArticleUtil.java,v 1.18 2008/11/17 18:07:16 valdas Exp $
 * Created on 7.2.2005
 *
 * Copyright (C) 2005 Idega Software hf. All Rights Reserved.
 *
 * This software is the proprietary information of Idega hf.
 * Use is subject to license terms.
 */
package com.idega.block.article.business;

import java.io.File;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;

import org.apache.myfaces.custom.savestate.UISaveState;

import com.idega.block.web2.business.Web2Business;
import com.idega.content.business.ContentUtil;
import com.idega.core.builder.business.BuilderService;
import com.idega.core.builder.business.BuilderServiceFactory;
import com.idega.core.builder.data.ICPage;
import com.idega.idegaweb.IWBundle;
import com.idega.presentation.IWContext;
import com.idega.util.CoreConstants;
import com.idega.util.PresentationUtil;
import com.idega.util.expression.ELUtil;
import com.idega.webface.WFUtil;

/**
 * 
 *  Last modified: $Date: 2008/11/17 18:07:16 $ by $Author: valdas $
 * 
 * @author <a href="mailto:gummi@idega.com">Gudmundur Agust Saemundsson</a>
 * @version $Revision: 1.18 $
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
	
	public static final String getSourcesAndActionForArticleEditor(IWContext iwc) {
		if (iwc == null) {
			return null;
		}
		
		List<String> sources = new ArrayList<String>();
		Web2Business web2 = ELUtil.getInstance().getBean(Web2Business.class);
		
		//	CSS
		try {
			sources.add(web2.getThickboxStyleFilePath());			//	ThickBox
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		sources.add(ContentUtil.getBundle().getVirtualPathWithFileNameString("style/content-admin.css"));
		
		//	JavaScript
		sources.add(getBundle().getVirtualPathWithFileNameString("javascript/ArticleEditorHelper.js"));
		sources.add(CoreConstants.DWR_ENGINE_SCRIPT);
		sources.add("/dwr/interface/ThemesEngine.js");
		sources.add(web2.getBundleURIToJQueryLib());				//	jQuery
		try {
			sources.add(web2.getThickboxScriptFilePath());			//	ThickBox
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		sources.add(ContentUtil.getBundle().getVirtualPathWithFileNameString("javascript/ContentAdmin.js"));
		
		return PresentationUtil.getJavaScriptAction(PresentationUtil.getJavaScriptLinesLoadedLazily(sources,
				"ArticleEditorHelper.initializeJavaScriptActionsForEditingAndCreatingArticles();"));
	}
	
	public static UISaveState getBeanSaveState(String beanId) {
		UISaveState beanSaveState = new UISaveState();
		ValueBinding binding = WFUtil.createValueBinding(new StringBuilder("#{").append(beanId).append("}").toString());
		beanSaveState.setId(new StringBuilder(beanId).append("SaveState").toString());
		beanSaveState.setValueBinding("value", binding);
		return beanSaveState;
	}
}
