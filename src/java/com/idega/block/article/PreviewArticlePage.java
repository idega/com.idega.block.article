/*
 * $Id: PreviewArticlePage.java,v 1.4 2005/02/21 16:16:19 gummi Exp $
 *
 * Copyright (C) 2004 Idega. All Rights Reserved.
 *
 * This software is the proprietary information of Idega.
 * Use is subject to license terms.
 *
 */
package com.idega.block.article;

import javax.faces.component.UIComponent;
import com.idega.block.article.component.ArticleItemViewer;
import com.idega.webface.WFUtil;

/**
 * Preview article test/demo page. 
 * <p>
 * Last modified: $Date: 2005/02/21 16:16:19 $ by $Author: gummi $
 *
 * @author Anders Lindman
 * @version $Revision: 1.4 $
 */
public class PreviewArticlePage extends CMSPage {
	
	public PreviewArticlePage() {
	}
	
	/**
	 * Creates the page content. 
	 */
	protected void createContent() {
		add(getArticlePreviewContent());
	}
	
	/**
	 * Returns the article preview content. 
	 */
	
	protected UIComponent getArticlePreviewContent() {
		String ref = ARTICLE_ITEM_BEAN_ID + ".";


		ArticleItemViewer item = new ArticleItemViewer();
		
		WFUtil.setValueBinding(item, "author", ref+"author");
		WFUtil.setValueBinding(item, "creation_date", ref+"creationDate");
		WFUtil.setValueBinding(item, "headline", ref+"headline");
		WFUtil.setValueBinding(item, "teaser", ref+"teaser");
		WFUtil.setValueBinding(item, "body", ref+"body");
		
		return item;
	}

}
