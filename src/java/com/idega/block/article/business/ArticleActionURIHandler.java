/*
 * $Id: ArticleActionURIHandler.java,v 1.2 2005/02/25 15:06:38 eiki Exp $
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
 * 
 *  Last modified: $Date: 2005/02/25 15:06:38 $ by $Author: eiki $
 * 
 * An IWActionURIHandler handler that handles a
 * @author <a href="mailto:eiki@idega.com">eiki</a>
 * @version $Revision: 1.2 $
 */
public class ArticleActionURIHandler extends DefaultIWActionURIHandler implements IWActionURIHandler {

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
		return uri.toString().indexOf(".article/")>=0;
	}
	
	/* (non-Javadoc)
	 * @see com.idega.core.uri.IWActionURIHandler#getRedirectURI(com.idega.core.uri.IWActionURI)
	 */
	public String getRedirectURI(IWActionURI uri) {
		//String identifier = uri.getIdentifierPart();
		String redirectURI = "/workspace/content/article/list/preview?"+ContentViewer.PARAMETER_CONTENT_RESOURCE+"="+uri.getPathPart();
		//todo set to previewer or editor depending on action
		
		return redirectURI;
	}
	/* (non-Javadoc)
	 * @see com.idega.core.uri.IWActionURIHandler#getIWActionURI(java.lang.String)
	 */
	public IWActionURI getIWActionURI(String requestURI) {
		return new ArticleIWActionURI(requestURI);
	}
}
