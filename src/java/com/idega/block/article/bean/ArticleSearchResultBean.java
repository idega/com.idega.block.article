/*
 * $Id: ArticleSearchResultBean.java,v 1.2 2005/03/09 14:27:21 eiki Exp $
 * Created on 5.3.2005
 *
 * Copyright (C) 2005 Idega Software hf. All Rights Reserved.
 *
 * This software is the proprietary information of Idega hf.
 * Use is subject to license terms.
 */
package com.idega.block.article.bean;

import java.util.ArrayList;
import java.util.List;
import com.idega.core.uri.IWActionURIManager;
import com.idega.webface.bean.WFEditableListDataBean;


/**
 * 
 *  Last modified: $Date: 2005/03/09 14:27:21 $ by $Author: eiki $
 * 
 * @author <a href="mailto:gummi@idega.com">Gudmundur Agust Saemundsson</a>
 * @version $Revision: 1.2 $
 */
public class ArticleSearchResultBean extends ArticleItemBean implements WFEditableListDataBean {

	/**
	 * 
	 */
	public ArticleSearchResultBean() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	/* (non-Javadoc)
	 * @see com.idega.webface.bean.WFEditableListDataBean#getSelectItemListArray()
	 */
	public Object[] getSelectItemListArray() {
		return new Object[4];
	}

	/* (non-Javadoc)
	 * @see com.idega.webface.bean.WFEditableListDataBean#getValues()
	 */
	public Object[] getValues() {
//		"Headline", "Published", "Author", "Status"
		List values = new ArrayList();
		values.add(getHeadline());
		values.add(getAuthor());
		values.add(getSource());
		values.add(getCreationDate());
		
		return values.toArray(new Object[values.size()]);
	}
	
	
	
	public String getPreviewIWActionURI(){
		return IWActionURIManager.getInstance().getActionURIPrefixWithContext("preview",getResourcePath());
	}
	
}
