/*
 * $Id: ArticleActionURIHandler.java,v 1.7 2005/12/13 19:58:13 laddi Exp $
 * Created on Jan 31, 2005
 *
 * Copyright (C) 2005 Idega Software hf. All Rights Reserved.
 *
 * This software is the proprietary information of Idega hf.
 * Use is subject to license terms.
 */
package com.idega.block.article.business;

import com.idega.content.presentation.ContentViewer;
import com.idega.core.uri.DefaultIWActionURIHandler;
import com.idega.core.uri.IWActionURI;
import com.idega.core.uri.IWActionURIHandler;




/**
 * <p>
 * An IWActionURIHandler handler that handles the actions for the article module (edit/delete).
 * </p>
 *  Last modified: $Date: 2005/12/13 19:58:13 $ by $Author: laddi $
 * 
 * 
 * @author <a href="mailto:eiki@idega.com">eiki</a>
 * @version $Revision: 1.7 $
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
		
		StringBuffer redirectURI = new StringBuffer();
		redirectURI.append(uri.getContextURI());
		redirectURI.append("workspace/content/article/");
		redirectURI.append(action);
		redirectURI.append("/?");
		redirectURI.append(subjectParameterName);
		redirectURI.append("=");
		redirectURI.append(uri.getPathPart());
		redirectURI.append("&");
		redirectURI.append(ContentViewer.PARAMETER_ACTION);
		redirectURI.append("=");
		redirectURI.append(action);
		
		String queryString = uri.getQueryString();
		if(queryString!=null){
			/*StringTokenizer tokenizer = new StringTokenizer(queryString,"&");
			while(tokenizer.hasMoreTokens()){
				String token = tokenizer.nextToken();
				if(token.startsWith("?")){
					token = token.substring(1,token.length());
				}
				redirectURI.append("&");
				redirectURI.append(token);
			}*/
			if(!queryString.startsWith("&")){
				if(queryString.startsWith("?")){
					queryString = queryString.substring(1,queryString.length());
				}
				redirectURI.append("&");
			}
			redirectURI.append(queryString);
		}
		
		return redirectURI.toString();
	}
	/* (non-Javadoc)
	 * @see com.idega.core.uri.IWActionURIHandler#getIWActionURI(java.lang.String)
	 */
	public IWActionURI getIWActionURI(String requestURI,String queryString) {
		return new ArticleIWActionURI(requestURI,queryString);
	}
}
