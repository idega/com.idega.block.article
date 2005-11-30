/*
 * $Id: SearchArticlePage.java,v 1.2 2005/11/30 09:34:52 laddi Exp $
 *
 * Copyright (C) 2004 Idega. All Rights Reserved.
 *
 * This software is the proprietary information of Idega.
 * Use is subject to license terms.
 *
 */
package com.idega.block.article.old;

import javax.faces.component.UIComponent;
import com.idega.block.article.component.SearchArticleBlock;

/**
 * Search article test/demo page. 
 * <p>
 * Last modified: $Date: 2005/11/30 09:34:52 $ by $Author: laddi $
 *
 * @author Anders Lindman
 * @version $Revision: 1.2 $
 */
public class SearchArticlePage extends CMSPage {
	
	//private static String FACET_HEAD="ws_head";

	/**
	 * Creates the page content. 
	 */
	protected void createContent() {
		//add(WFUtil.getBannerBox());
//		ArticleBar bar = new ArticleBar();
//		add(bar);

		add(getSearchArticleBlock());		
	}
	
	/**
	 * Returns a search article block.
	 */
	protected UIComponent getSearchArticleBlock() {
		//HtmlPanelGrid ap = WFPanelUtil.getApplicationPanel();
		//ap.getChildren().add(getFunctionBlock());
		SearchArticleBlock sab = new SearchArticleBlock();
		//ap.getChildren().add(sab);
		//return ap;
		return sab;
	}
}
