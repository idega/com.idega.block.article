/*
 * $Id: CreateArticlePage.java,v 1.4 2004/12/21 15:43:17 joakim Exp $
 *
 * Copyright (C) 2004 Idega. All Rights Reserved.
 *
 * This software is the proprietary information of Idega.
 * Use is subject to license terms.
 *
 */
package com.idega.block.article;

import javax.faces.component.UIComponent;
import com.idega.block.article.component.EditArticleBlock;
import com.idega.webface.WFPage;
import com.idega.webface.WFUtil;


/**
 * Created article test/demo page. 
 * <p>
 * Last modified: $Date: 2004/12/21 15:43:17 $ by $Author: joakim $
 *
 * @author Anders Lindman
 * @version $Revision: 1.4 $
 */
public class CreateArticlePage extends CMSPage {
	
	
	public CreateArticlePage(){
//		super();
	}

	/**
	 * Creates the page content. 
	 */
	protected void createContent() {
		//add(WFUtil.getBannerBox());
		//add(getMainTaskbar());
		add(getEditPerspective());
		WFUtil.invoke(ARTICLE_ITEM_BEAN_ID, "clear");
	}
	
	/**
	 * Returns the main task bar selector. 
	 */
	protected UIComponent getMainTaskbar() {
		return getEditPerspective();
	}
	
	/**
	 * Returns the content edit perspective.
	 */
	protected UIComponent getEditPerspective() {
		String bref = WFPage.CONTENT_BUNDLE + ".";
		EditArticleBlock ab = new EditArticleBlock();
		ab.setId("article_block");
		return ab;
	}
}
