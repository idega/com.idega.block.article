/*
 * $Id: ArticleItemView.java,v 1.2 2004/12/09 14:42:15 joakim Exp $
 *
 * Copyright (C) 2004 Idega. All Rights Reserved.
 *
 * This software is the proprietary information of Idega.
 * Use is subject to license terms.
 */
package com.idega.block.article.component;

import javax.faces.component.UIComponent;
import com.idega.presentation.IWBaseComponent;
import com.idega.webface.WFContainer;
import com.idega.webface.WFUtil;
import com.idega.webface.test.bean.ManagedContentBeans;

/**
 * Last modified: $Date: 2004/12/09 14:42:15 $ by $Author: joakim $
 *
 * @author Joakim
 * @version $Revision: 1.2 $
 */
public class ArticleItemView extends IWBaseComponent implements ManagedContentBeans {

	public ArticleItemView() {
	}

	protected void initializeContent() {
		add(getPreviewPanel());
	}

	/*
	 * Creates a search form panel.
	 */
	private UIComponent getPreviewPanel() {
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
