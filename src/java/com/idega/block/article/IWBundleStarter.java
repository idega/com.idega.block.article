/*
 * $Id: IWBundleStarter.java,v 1.2 2004/12/20 16:50:11 joakim Exp $
 * Created on 2.11.2004
 *
 * Copyright (C) 2004 Idega Software hf. All Rights Reserved.
 *
 * This software is the proprietary information of Idega hf.
 * Use is subject to license terms.
 */
package com.idega.block.article;

import com.idega.content.view.ContentViewManager;
import com.idega.core.view.DefaultViewNode;
import com.idega.core.view.ViewNode;
import com.idega.idegaweb.GlobalIncludeManager;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWBundleStartable;


/**
 * 
 *  Last modified: $Date: 2004/12/20 16:50:11 $ by $Author: joakim $
 * 
 * @author <a href="mailto:tryggvil@idega.com">Tryggvi Larusson</a>
 * @version $Revision: 1.2 $
 */
public class IWBundleStarter implements IWBundleStartable {
	private static final String STYLE_SHEET_URL = "/style/webfacestyle.css";
	private static final String BUNDLE_IDENTIFIER="com.idega.block.article";

	/**
	 * 
	 */
	public IWBundleStarter() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see com.idega.idegaweb.IWBundleStartable#start(com.idega.idegaweb.IWBundle)
	 */
	public void start(IWBundle starterBundle) {
		addArticleViews(starterBundle);
		//Add the stylesheet:
		GlobalIncludeManager.getInstance().addBundleStyleSheet(BUNDLE_IDENTIFIER,STYLE_SHEET_URL);
	}

	/* (non-Javadoc)
	 * @see com.idega.idegaweb.IWBundleStartable#stop(com.idega.idegaweb.IWBundle)
	 */
	public void stop(IWBundle starterBundle) {
		// TODO Auto-generated method stub	
	}
	
	public void addArticleViews(IWBundle bundle){

		ContentViewManager cViewManager = ContentViewManager.getInstance(bundle.getApplication());
		ViewNode contentNode = cViewManager.getContentNode();
		
		DefaultViewNode articleNode = new DefaultViewNode("article",contentNode);
		articleNode.setJspUri(bundle.getJSPURI("articles.jsp"));
		DefaultViewNode createNewArticleNode = new DefaultViewNode("create",articleNode);
		//createNewArticleNode.setJspUri("/idegaweb/bundles/com.idega.webface.bundle/jsp/createarticle.jsp");
		String jspUri = bundle.getJSPURI("createarticle.jsp");
		createNewArticleNode.setJspUri(jspUri);
		
		DefaultViewNode previewArticlesNode = new DefaultViewNode("preview",articleNode);
		//previewArticlesNode.setJspUri("/idegaweb/bundles/com.idega.webface.bundle/jsp/previewarticle.jsp");
		previewArticlesNode.setJspUri(bundle.getJSPURI("previewarticle.jsp"));
		//DefaultViewNode listArticlesNode = new ApplicationViewNode("listarticles",articleNode);
		
		DefaultViewNode listArticlesNode = new DefaultViewNode("list",articleNode);
		//previewArticlesNode.setJspUri("/idegaweb/bundles/com.idega.webface.bundle/jsp/previewarticle.jsp");
		listArticlesNode.setJspUri(bundle.getJSPURI("listarticles.jsp"));
		//DefaultViewNode listArticlesNode = new ApplicationViewNode("listarticles",articleNode);		

		DefaultViewNode searchArticlesNode = new DefaultViewNode("search",articleNode);
		//previewArticlesNode.setJspUri("/idegaweb/bundles/com.idega.webface.bundle/jsp/previewarticle.jsp");
		searchArticlesNode.setJspUri(bundle.getJSPURI("searcharticle.jsp"));
		//DefaultViewNode listArticlesNode = new ApplicationViewNode("listarticles",articleNode);		
				
		

	}
}
