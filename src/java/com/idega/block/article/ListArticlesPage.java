/*
 * $Id: ListArticlesPage.java,v 1.3 2004/12/17 14:22:06 joakim Exp $
 *
 * Copyright (C) 2004 Idega. All Rights Reserved.
 *
 * This software is the proprietary information of Idega.
 * Use is subject to license terms.
 *
 */
package com.idega.block.article;

import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlPanelGrid;
import com.idega.block.article.component.ListArticlesBlock;
import com.idega.webface.WFPanelUtil;
import com.idega.webface.WFUtil;

/**
 * Search article test/demo page. 
 * <p>
 * Last modified: $Date: 2004/12/17 14:22:06 $ by $Author: joakim $
 *
 * @author Anders Lindman
 * @version $Revision: 1.3 $
 */
public class ListArticlesPage extends CMSPage {

	/**
	 * Creates the page content. 
	 */
	protected void createContent() {
		add(WFUtil.getBannerBox());
		add(getListArticlesBlock());
	}
	
	/**
	 * Returns a list articles block.
	 */
	protected UIComponent getListArticlesBlock() {
		HtmlPanelGrid ap = WFPanelUtil.getApplicationPanel();
		ap.getChildren().add(getFunctionBlock());
		ListArticlesBlock block = new ListArticlesBlock();
		ap.getChildren().add(block);
		return ap;
	}
}
