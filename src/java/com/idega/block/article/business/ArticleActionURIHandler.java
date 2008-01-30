/*
 * $Id: ArticleActionURIHandler.java,v 1.11 2008/01/30 13:49:42 valdas Exp $
 * Created on Jan 31, 2005
 *
 * Copyright (C) 2005 Idega Software hf. All Rights Reserved.
 *
 * This software is the proprietary information of Idega hf.
 * Use is subject to license terms.
 */
package com.idega.block.article.business;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import com.idega.block.article.component.ArticleDeleter;
import com.idega.block.article.component.ArticleEditor;
import com.idega.builder.bean.AdvancedProperty;
import com.idega.content.business.ContentConstants;
import com.idega.content.presentation.ContentViewer;
import com.idega.core.builder.business.BuilderService;
import com.idega.core.builder.business.BuilderServiceFactory;
import com.idega.core.uri.DefaultIWActionURIHandler;
import com.idega.core.uri.IWActionURI;
import com.idega.core.uri.IWActionURIHandler;
import com.idega.idegaweb.IWMainApplication;




/**
 * <p>
 * An IWActionURIHandler handler that handles the actions for the article module (edit/delete).
 * </p>
 *  Last modified: $Date: 2008/01/30 13:49:42 $ by $Author: valdas $
 * 
 * 
 * @author <a href="mailto:eiki@idega.com">eiki</a>
 * @version $Revision: 1.11 $
 */
public class ArticleActionURIHandler extends DefaultIWActionURIHandler implements IWActionURIHandler {

	public static final String HANDLER_IDENTIFIER="article";
	
	/**
	 * 
	 */
	public ArticleActionURIHandler() {
		super();
	}

	/* (non-Javadoc)
	 * @see com.idega.core.uri.IWActionURIHandler#canHandleIWActionURI(com.idega.core.uri.IWActionURI)
	 */
	public boolean canHandleIWActionURI(IWActionURI uri) {
		if(getHandlerIdentifier().equals(uri.getHandlerIdentifier())){
			return true;
		}
		return uri.toString().indexOf(".article/")>=0;
	}
	
	public String getHandlerIdentifier(){
		return HANDLER_IDENTIFIER;
	}
	
	/* (non-Javadoc)
	 * @see com.idega.core.uri.IWActionURIHandler#getRedirectURI(com.idega.core.uri.IWActionURI)
	 */
	public String getRedirectURI(IWActionURI uri) {
		//todo set to previewer or editor depending on action
		//todo register actions as subnodes of article
		String action = uri.getActionPart();
		String subjectParameterName = ContentViewer.PARAMETER_CONTENT_RESOURCE;
		
		String pathPart = uri.getPathPart();
		
		StringBuffer redirectURI = new StringBuffer();
		redirectURI.append(uri.getContextURI());
		if ("preview".equals(action)) {
			redirectURI.append(ContentConstants.PAGES_START_URI_WITHOUT_FIRST_SLASH).append(ContentConstants.ARTICLE_VIEWER_URI);
			redirectURI.append("?");
		}
		else if (action.equals(ContentConstants.CONTENT_ITEM_ACTION_EDIT) || action.equals(ContentConstants.CONTENT_ITEM_ACTION_CREATE)) {
			return addQueryPart(uri, new StringBuffer(getBuilderService().getUriToObject(ArticleEditor.class, getDefaultParameters(pathPart, action)))).toString();
		}
		else if (action.equals(ContentConstants.CONTENT_ITEM_ACTION_DELETE)) {
			return addQueryPart(uri, new StringBuffer(getBuilderService().getUriToObject(ArticleDeleter.class, getDefaultParameters(pathPart, action)))).toString();
		}
		else {
			redirectURI.append("workspace/content/article/");
			redirectURI.append(action);
			redirectURI.append("/?");
		}
		if(pathPart!=null && !pathPart.equals("")){
			redirectURI.append(subjectParameterName);
			redirectURI.append("=");
			redirectURI.append(pathPart);
			redirectURI.append("&");
		}
		redirectURI.append(ContentViewer.PARAMETER_ACTION);
		redirectURI.append("=");
		redirectURI.append(action);
		
		return addQueryPart(uri, redirectURI).toString();
	}
	
	private List<AdvancedProperty> getDefaultParameters(String pathPart, String action) {
		List<AdvancedProperty> parameters = new ArrayList<AdvancedProperty>();
		parameters.add(new AdvancedProperty(ContentViewer.PARAMETER_CONTENT_RESOURCE, pathPart));
		parameters.add(new AdvancedProperty(ContentViewer.PARAMETER_ACTION, action));
		return parameters;
	}
	
	private BuilderService getBuilderService() {
		try {
			return BuilderServiceFactory.getBuilderService(IWMainApplication.getDefaultIWApplicationContext());
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private StringBuffer addQueryPart(IWActionURI uri, StringBuffer redirectURI) {
		String queryString = uri.getQueryString();
		if (queryString == null) {
			return redirectURI;
		}
			
		if (!queryString.startsWith("&")) {
			if (queryString.startsWith("?")) {
				queryString = queryString.substring(1);
			}
			redirectURI.append("&");
		}
		redirectURI.append(queryString);
	
		return redirectURI;
	}
	
	/* (non-Javadoc)
	 * @see com.idega.core.uri.IWActionURIHandler#getIWActionURI(java.lang.String)
	 */
	public IWActionURI getIWActionURI(String requestURI,String queryString) {
		return new ArticleIWActionURI(requestURI,queryString);
	}
}
