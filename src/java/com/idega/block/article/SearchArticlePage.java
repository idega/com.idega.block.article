/*
 * $Id: SearchArticlePage.java,v 1.2 2004/11/14 23:39:41 tryggvil Exp $
 *
 * Copyright (C) 2004 Idega. All Rights Reserved.
 *
 * This software is the proprietary information of Idega.
 * Use is subject to license terms.
 *
 */
package com.idega.block.article;

import javax.faces.component.UIComponent;
import com.idega.block.article.component.SearchArticleBlock;

/**
 * Search article test/demo page. 
 * <p>
 * Last modified: $Date: 2004/11/14 23:39:41 $ by $Author: tryggvil $
 *
 * @author Anders Lindman
 * @version $Revision: 1.2 $
 */
public class SearchArticlePage extends CMSPage {

	/**
	 * Creates the page content. 
	 */
	protected void createContent() {
		//add(WFUtil.getBannerBox());
		add(getSearchArticleBlock());		
	}
	
	/**
	 * Returns a search article block.
	 */
	protected UIComponent getSearchArticleBlock() {
		//HtmlPanelGrid ap = WFPanelUtil.getApplicationPanel();
		//ap.getChildren().add(getFunctionBlock());
		SearchArticleBlock sab = new SearchArticleBlock("Search articles");
		//ap.getChildren().add(sab);
		//return ap;
		return sab;
	}
}
