/*
 * $Id: PreviewArticlePage.java,v 1.2 2004/12/03 14:43:31 joakim Exp $
 *
 * Copyright (C) 2004 Idega. All Rights Reserved.
 *
 * This software is the proprietary information of Idega.
 * Use is subject to license terms.
 *
 */
package com.idega.block.article;

import javax.faces.component.UIComponent;
import com.idega.webface.WFContainer;
import com.idega.webface.WFUtil;

/**
 * Preview article test/demo page. 
 * <p>
 * Last modified: $Date: 2004/12/03 14:43:31 $ by $Author: joakim $
 *
 * @author Anders Lindman
 * @version $Revision: 1.2 $
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

		WFContainer c = new WFContainer();
		c.add(WFUtil.getTextVB(ref + "author"));
		c.add(WFUtil.getText(" : "));
		c.add(WFUtil.getTextVB(ref + "creationDate"));
		c.add(WFUtil.getBreak(1));
		c.add(WFUtil.getTextVB(ref + "headline"));
		c.add(WFUtil.getBreak(2));
		c.add(WFUtil.getTextVB(ref + "body"));
		c.add(WFUtil.getBreak(1));
		return c;
	}

}
