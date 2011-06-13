/*
 * $Id: ArticleSearchResultBean.java,v 1.4 2005/12/20 16:40:42 tryggvil Exp $
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
 *  Last modified: $Date: 2005/12/20 16:40:42 $ by $Author: tryggvil $
 * 
 * @author <a href="mailto:gummi@idega.com">Gudmundur Agust Saemundsson</a>
 * @version $Revision: 1.4 $
 */
public class ArticleSearchResultBean extends ArticleItemBean implements WFEditableListDataBean {

	private static final long serialVersionUID = -4189675732587747519L;

	public ArticleSearchResultBean() {
		super();
	}
	
	/* (non-Javadoc)
	 * @see com.idega.webface.bean.WFEditableListDataBean#getSelectItemListArray()
	 */
	public Object[] getSelectItemListArray() {
		return new Object[6];
	}

	/* (non-Javadoc)
	 * @see com.idega.webface.bean.WFEditableListDataBean#getValues()
	 */
	public Object[] getValues() {
//		"Headline", "Published", "Author", "Status"
		List<Object> values = new ArrayList<Object>();
		values.add(getHeadline());
		values.add(getAuthor());
		values.add(getSource());
		values.add(getCreationDate());
		values.add(getLanguage());
		values.add(getStatus());
		
		return values.toArray(new Object[values.size()]);
	}
	
	
	
	public String getPreviewIWActionURI(){
		return IWActionURIManager.getInstance().getActionURIPrefixWithContext("preview",getResourcePath());
	}
	
}
