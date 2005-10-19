/*
 * $Id: IWBundleStarter.java,v 1.17 2005/10/19 18:22:12 tryggvil Exp $
 * Created on 2.11.2004
 *
 * Copyright (C) 2004 Idega Software hf. All Rights Reserved.
 *
 * This software is the proprietary information of Idega hf.
 * Use is subject to license terms.
 */
package com.idega.block.article;

import com.idega.block.article.business.ArticleActionURIHandler;
import com.idega.content.view.ContentViewManager;
import com.idega.core.uri.IWActionURIManager;
import com.idega.core.view.DefaultViewNode;
import com.idega.core.view.KeyboardShortcut;
import com.idega.core.view.ViewNode;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWBundleStartable;
import com.idega.idegaweb.include.GlobalIncludeManager;


/**
 * 
 *  Last modified: $Date: 2005/10/19 18:22:12 $ by $Author: tryggvil $
 * 
 * @author <a href="mailto:tryggvil@idega.com">Tryggvi Larusson</a>
 * @version $Revision: 1.17 $
 */
public class IWBundleStarter implements IWBundleStartable {
	private static final String STYLE_SHEET_URL = "/style/article.css";
	private static final String BUNDLE_IDENTIFIER="com.idega.block.article";

	/**
	 * 
	 */
	public IWBundleStarter() {
	}

	/* (non-Javadoc)
	 * @see com.idega.idegaweb.IWBundleStartable#start(com.idega.idegaweb.IWBundle)
	 */
	public void start(IWBundle starterBundle) {
		addArticleIWActionURIHandler();
		addArticleViews(starterBundle);
		//Add the stylesheet:
		GlobalIncludeManager.getInstance().addBundleStyleSheet(BUNDLE_IDENTIFIER,STYLE_SHEET_URL);
	}

	/**
	 * 
	 */
	private void addArticleIWActionURIHandler() {
		IWActionURIManager manager = IWActionURIManager.getInstance();
		
		//so it is called before contenthandler
		manager.registerHandler(0,new ArticleActionURIHandler());
		
	}

	/* (non-Javadoc)
	 * @see com.idega.idegaweb.IWBundleStartable#stop(com.idega.idegaweb.IWBundle)
	 */
	public void stop(IWBundle starterBundle) {
	}
	
	public void addArticleViews(IWBundle bundle){
		ContentViewManager cViewManager = ContentViewManager.getInstance(bundle.getApplication());
		ViewNode contentNode = cViewManager.getContentNode();
		
		DefaultViewNode articleNode = new DefaultViewNode("article",contentNode);
		articleNode.setJspUri(bundle.getJSPURI("createarticle.jsp"));
		articleNode.setKeyboardShortcut(new KeyboardShortcut("a"));
		articleNode.setName("#{localizedStrings['com.idega.block.article']['article']}");
		
		DefaultViewNode createNewArticleNode = new DefaultViewNode("create",articleNode);
		String jspUri = bundle.getJSPURI("createarticle.jsp");
		createNewArticleNode.setJspUri(jspUri);
		createNewArticleNode.setName("#{localizedStrings['com.idega.block.article']['create_article']}");
		
		DefaultViewNode editNewArticleNode = new DefaultViewNode("edit",articleNode);
//		editNewArticleNode.setJspUri(bundle.getJSPURI("createarticle.jsp"));
		editNewArticleNode.setJspUri(bundle.getJSPURI("editarticle.jsp"));
		editNewArticleNode.setVisibleInMenus(false);
		editNewArticleNode.setName("#{localizedStrings['com.idega.block.article']['edit']}");
		
		DefaultViewNode listArticlesNode = new DefaultViewNode("list",articleNode);
		listArticlesNode.setJspUri(bundle.getJSPURI("listarticles.jsp"));
		listArticlesNode.setName("#{localizedStrings['com.idega.block.article']['list_articles']}");
		
		
		DefaultViewNode previewArticlesNode = new DefaultViewNode("preview",articleNode);
		previewArticlesNode.setJspUri(bundle.getJSPURI("previewarticle.jsp"));
		previewArticlesNode.setVisibleInMenus(false);
		previewArticlesNode.setName("#{localizedStrings['com.idega.block.article']['preview']}");

		DefaultViewNode searchArticlesNode = new DefaultViewNode("search",articleNode);
		searchArticlesNode.setJspUri(bundle.getJSPURI("searcharticle.jsp"));
		searchArticlesNode.setName("#{localizedStrings['com.idega.block.article']['search_articles']}");
		searchArticlesNode.setVisibleInMenus(false);
	}
}
