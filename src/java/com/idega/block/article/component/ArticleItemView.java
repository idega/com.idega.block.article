package com.idega.block.article.component;

import javax.faces.component.UIComponent;
import com.idega.presentation.IWBaseComponent;
import com.idega.webface.WFContainer;
import com.idega.webface.WFUtil;
import com.idega.webface.test.bean.ManagedContentBeans;

/**
 * @author Joakim
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
