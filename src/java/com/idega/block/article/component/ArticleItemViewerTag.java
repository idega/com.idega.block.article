/*
 * $Id: ArticleItemViewerTag.java,v 1.1 2005/02/21 16:16:19 gummi Exp $
 * Created on 21.2.2005
 *
 * Copyright (C) 2005 Idega Software hf. All Rights Reserved.
 *
 * This software is the proprietary information of Idega hf.
 * Use is subject to license terms.
 */
package com.idega.block.article.component;

import com.idega.content.presentation.ContentItemViewerTag;


/**
 * 
 *  Last modified: $Date: 2005/02/21 16:16:19 $ by $Author: gummi $
 * 
 * @author <a href="mailto:gummi@idega.com">Gudmundur Agust Saemundsson</a>
 * @version $Revision: 1.1 $
 */
public class ArticleItemViewerTag extends ContentItemViewerTag {

	/**
	 * 
	 */
	public ArticleItemViewerTag() {
		super();
	}

	/* (non-Javadoc)
	 * @see javax.faces.webapp.UIComponentTag#getComponentType()
	 */
	public String getComponentType() {
		return "ArticleItemView";
	}
}
