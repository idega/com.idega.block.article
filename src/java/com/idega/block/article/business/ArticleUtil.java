/*
 * $Id: ArticleUtil.java,v 1.24 2009/01/13 10:44:22 valdas Exp $
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
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;

import org.apache.myfaces.custom.savestate.UISaveState;

import com.idega.block.web2.business.Web2Business;
import com.idega.content.business.ContentUtil;
import com.idega.content.presentation.ContentItemViewer;
import com.idega.core.builder.business.BuilderService;
import com.idega.core.builder.business.BuilderServiceFactory;
import com.idega.core.builder.data.ICPage;
import com.idega.core.localisation.business.LocaleSwitcher;
import com.idega.idegaweb.IWBundle;
import com.idega.presentation.IWContext;
import com.idega.presentation.Script;
import com.idega.util.CoreConstants;
import com.idega.util.PresentationUtil;
import com.idega.util.StringUtil;
import com.idega.util.expression.ELUtil;
import com.idega.webface.WFUtil;

/**
 * 
 *  Last modified: $Date: 2009/01/13 10:44:22 $ by $Author: valdas $
 * 
 * @author <a href="mailto:gummi@idega.com">Gudmundur Agust Saemundsson</a>
 * @version $Revision: 1.24 $
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
	
	public static final List<String> getCSSFilesForArticle() {
		List<String> css = new ArrayList<String>(2);
		Web2Business web2 = ELUtil.getInstance().getBean(Web2Business.class);
		
		css.add(web2.getBundleURIToFancyBoxStyleFile());		//	FancyBox
		css.add(ContentUtil.getBundle().getVirtualPathWithFileNameString("style/content-admin.css"));
		return css;
	}
	
	public static final List<String> getJavaScriptFilesForArticle() {
		List<String> javaScript = new ArrayList<String>(6);
		Web2Business web2 = ELUtil.getInstance().getBean(Web2Business.class);
		
		javaScript.add(getBundle().getVirtualPathWithFileNameString("javascript/ArticleEditorHelper.js"));
		javaScript.add(getBundle().getVirtualPathWithFileNameString("javascript/ArticleCategoriesHelper.js"));
		javaScript.add(CoreConstants.DWR_ENGINE_SCRIPT);
		javaScript.add("/dwr/interface/LucidEngine.js");
		javaScript.add(web2.getBundleURIToJQueryLib());					//	jQuery
		javaScript.addAll(web2.getBundleURIsToFancyBoxScriptFiles());	//	FancyBox
		javaScript.add(ContentUtil.getBundle().getVirtualPathWithFileNameString("javascript/ContentAdmin.js"));
		
		return javaScript;
	}
	
	public static final String getArticleEditorInitializerAction(boolean executeOnLoad) {
		String action = "ArticleEditorHelper.initializeJavaScriptActionsForEditingAndCreatingArticles();";
		if (executeOnLoad) {
			action = new StringBuilder("jQuery(window).load(function() {").append(action).append("});").toString();
		}
		return action;
	}
	
	public static final String getSourcesAndActionForArticleEditor(IWContext iwc) {
		if (iwc == null) {
			return null;
		}
		
		List<String> sources = new ArrayList<String>();
		//	CSS
		sources.addAll(getCSSFilesForArticle());
		
		//	JavaScript
		sources.addAll(getJavaScriptFilesForArticle());
		
		return PresentationUtil.getJavaScriptAction(PresentationUtil.getJavaScriptLinesLoadedLazily(sources, getArticleEditorInitializerAction(false)));
	}
	
	public static UISaveState getBeanSaveState(String beanId) {
		UISaveState beanSaveState = new UISaveState();
		ValueBinding binding = WFUtil.createValueBinding(new StringBuilder("#{").append(beanId).append("}").toString());
		beanSaveState.setId(new StringBuilder(beanId).append("SaveState").toString());
		beanSaveState.setValueBinding("value", binding);
		return beanSaveState;
	}
	
	public static final boolean addArticleFeedFacet(IWContext iwc, Map<String, UIComponent> facets) {
		if (facets == null) {
			return false;
		}
		
		BuilderService bservice = null;
		try {
			bservice = BuilderServiceFactory.getBuilderService(iwc);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		if (bservice == null) {
			return false;
		}
		
		String serverName = iwc.getServerURL();
		
		String feedUri = null;
		try {
			feedUri = bservice.getCurrentPageURI(iwc);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		if (StringUtil.isEmpty(feedUri)) {
			return false;
		}
		if (!feedUri.endsWith(CoreConstants.SLASH)) {
			feedUri = new StringBuilder(feedUri).append(CoreConstants.SLASH).toString();
		}
		
		String linkToFeed = new StringBuilder(serverName).append("rss/article").append(feedUri).append("?").append(LocaleSwitcher.languageParameterString)
							.append(CoreConstants.EQ).append(iwc.getCurrentLocale().toString()).toString();
		
		Script script = new Script();
		script.addScriptLine(new StringBuilder("registerEvent(window, 'load', function(){addFeedSymbolInHeader('").append(linkToFeed)
								.append("', 'atom', 'Atom 1.0');});").toString());
		facets.put(ContentItemViewer.FACET_FEED_SCRIPT, script);
		
		return true;
	}
}
