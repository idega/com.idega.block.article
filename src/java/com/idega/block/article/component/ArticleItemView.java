/*
 * $Id: ArticleItemView.java,v 1.3 2004/12/15 15:47:16 joakim Exp $
 *
 * Copyright (C) 2004 Idega. All Rights Reserved.
 *
 * This software is the proprietary information of Idega.
 * Use is subject to license terms.
 */
package com.idega.block.article.component;

import java.io.IOException;
import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlPanelGrid;
import javax.faces.context.FacesContext;
import com.idega.presentation.IWBaseComponent;
import com.idega.webface.WFComponentSelector;
import com.idega.webface.WFContainer;
import com.idega.webface.WFPage;
import com.idega.webface.WFPanelUtil;
import com.idega.webface.WFUtil;
import com.idega.webface.test.bean.ManagedContentBeans;

/**
 * Last modified: $Date: 2004/12/15 15:47:16 $ by $Author: joakim $
 *
 * @author Joakim
 * @version $Revision: 1.3 $
 */
public class ArticleItemView extends IWBaseComponent implements ManagedContentBeans {

	private final static String P = "article_item_view_"; // Id prefix

	private final static String COMPONENT_SELECTOR_ID = P + "component_selector";
	private final static String NO_ARTICLE_ID = P + "no_article";
	private final static String ARTICLE_VIEW_ID = P + "article_view";

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
		String bref = WFPage.CONTENT_BUNDLE + ".";

		WFComponentSelector cs = new WFComponentSelector();
		cs.setId(COMPONENT_SELECTOR_ID);
		
		HtmlPanelGrid p = WFPanelUtil.getPlainFormPanel(1);
		p.setId(NO_ARTICLE_ID);
		p.getChildren().add(WFUtil.getHeaderTextVB(bref + "no_article_selected"));
		cs.add(p);

		WFContainer c = new WFContainer();
		c.setId(ARTICLE_VIEW_ID);
		c.add(WFUtil.getTextVB(ref + "author"));
		c.add(WFUtil.getText(" : "));
		c.add(WFUtil.getTextVB(ref + "creationDate"));
		c.add(WFUtil.getBreak(1));
		c.add(WFUtil.getTextVB(ref + "headline"));
		c.add(WFUtil.getBreak(2));
		c.add(WFUtil.getTextVB(ref + "body"));
		c.add(WFUtil.getBreak(1));
		cs.add(c);

		cs.setSelectedId(NO_ARTICLE_ID, true);
		return cs;
	}

	private void selectComponent() {
		WFComponentSelector cs = (WFComponentSelector) findComponent(COMPONENT_SELECTOR_ID);

		String headline = WFUtil.getStringValue(ARTICLE_ITEM_BEAN_ID, "headline");
		if (cs != null) {
			if (headline == null || headline.length() == 0) {
				cs.setSelectedId(NO_ARTICLE_ID, true);
				cs.setSelectedId(ARTICLE_VIEW_ID, false);
			}else {
				cs.setSelectedId(NO_ARTICLE_ID, false);
				cs.setSelectedId(ARTICLE_VIEW_ID, true);
			}
		}else {
			cs.setSelectedId(NO_ARTICLE_ID, false);
			cs.setSelectedId(ARTICLE_VIEW_ID, true);
			System.out.println("Could not find the Component selector!!!");
		}
	}

	/**
	 * @see javax.faces.component.UIComponent#encodeBegin(javax.faces.context.FacesContext)
	 */
	public void encodeBegin(FacesContext context) throws IOException {
		super.encodeBegin(context);
		selectComponent();
	}
}
