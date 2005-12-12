/*
 * $Id: ArticleIWActionURI.java,v 1.2 2005/12/12 11:38:36 tryggvil Exp $
 * Created on Feb 22, 2005
 *
 * Copyright (C) 2005 Idega Software hf. All Rights Reserved.
 *
 * This software is the proprietary information of Idega hf.
 * Use is subject to license terms.
 */
package com.idega.block.article.business;

import com.idega.core.uri.IWActionURI;


/**
 * 
 *  Last modified: $Date: 2005/12/12 11:38:36 $ by $Author: tryggvil $
 * 
 * @author <a href="mailto:eiki@idega.com">eiki</a>
 * @version $Revision: 1.2 $
 */
public class ArticleIWActionURI extends IWActionURI {

	/**
	 * @param requestURI
	 */
	public ArticleIWActionURI(String requestURI,String queryString) {
		super(requestURI,queryString);
	}
	
	
	/**
	 * @param requestURI
	 * @return the identifer part of the requesturi
	 */
	protected String extractIdentifierPath(String requestURI) {
		String tempIdentifier = ".article/";
		String identifier = null;
		//get the identifier part
		int index = requestURI.indexOf(tempIdentifier);
		if(index>=0){
			String temp = requestURI.substring(0,index);
			int indexOfSlashBeforeDot = temp.lastIndexOf("/");
			if(indexOfSlashBeforeDot>=0){
				identifier = requestURI.substring(indexOfSlashBeforeDot);
			}
		}
		
		return identifier;
	}
	
}
