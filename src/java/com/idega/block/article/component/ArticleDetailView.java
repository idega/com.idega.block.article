/*
 * $Id: ArticleDetailView.java,v 1.4 2004/12/15 15:47:16 joakim Exp $
 * 
 * Copyright (C) 2004 Idega. All Rights Reserved.
 * 
 * This software is the proprietary information of Idega. Use is subject to
 * license terms.
 */
package com.idega.block.article.component;

import java.io.IOException;
import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.component.html.HtmlPanelGrid;
import javax.faces.context.FacesContext;
import com.idega.presentation.IWBaseComponent;
import com.idega.webface.WFComponentSelector;
import com.idega.webface.WFPage;
import com.idega.webface.WFPanelUtil;
import com.idega.webface.WFPlainOutputText;
import com.idega.webface.WFUtil;
import com.idega.webface.convert.WFCommaSeparatedListConverter;
import com.idega.webface.test.bean.ManagedContentBeans;

/**
 * Last modified: $Date: 2004/12/15 15:47:16 $ by $Author: joakim $
 * 
 * @author Joakim
 * @version $Revision: 1.4 $
 */
public class ArticleDetailView extends IWBaseComponent implements ManagedContentBeans {

	private final static String P = "article_detail_view_"; // Id prefix

	private final static String COMPONENT_SELECTOR_ID = P + "component_selector";
	private final static String NO_ARTICLE_ID = P + "no_article";
	private final static String ARTICLE_LIST_ID = P + "article_list";

	private final static String ref = ARTICLE_ITEM_BEAN_ID + ".";
	private final static String bref = WFPage.CONTENT_BUNDLE + ".";
	
	public ArticleDetailView() {
	}

	protected void initializeContent() {
		add(getDetailPanel());
	}

	/*
	 * Creates a preview container for the article.
	 */
	private UIComponent getDetailPanel() {
		HtmlPanelGrid dp = WFPanelUtil.getPlainFormPanel(1);
		WFComponentSelector cs = new WFComponentSelector();
		cs.setId(COMPONENT_SELECTOR_ID);
		String headline = WFUtil.getTextVB(ref + "headline").getTitle();
		System.out.println("Headline:'" + WFUtil.getTextVB(ref + "headline").getTitle() + "'");
		HtmlPanelGrid p = WFPanelUtil.getPlainFormPanel(1);
		p.setId(NO_ARTICLE_ID);
		p.getChildren().add(WFUtil.getHeaderTextVB(bref + "no_article_selected"));
		
		cs.add(p);
		
		p = WFPanelUtil.getPlainFormPanel(1);
		p.setId(ARTICLE_LIST_ID);
		//		System.out.println("Headline:"+WFUtil.getTextVB(ref +
		// "headline").getTitle());
		//		System.out.println("Headline length:"+WFUtil.getTextVB(ref +
		// "headline").getTitle().length());
		p.getChildren().add(WFUtil.getHeaderTextVB(ref + "headline"));
		p.getChildren().add(WFUtil.getText(" "));
		p.getChildren().add(WFUtil.getTextVB(ref + "teaser"));
		p.getChildren().add(WFUtil.getText(" "));
		WFPlainOutputText bodyText = new WFPlainOutputText();
		WFUtil.setValueBinding(bodyText, "value", ref + "body");
		p.getChildren().add(bodyText);
		p.getChildren().add(WFUtil.getBreak());
		p.getChildren().add(new WFPlainOutputText("<hr/>"));
		UIComponent g = WFUtil.group(WFUtil.getHeaderTextVB(bref + "author"), WFUtil.getHeaderText(": "));
		g.getChildren().add(WFUtil.getTextVB(ref + "author"));
		p.getChildren().add(g);
		p.getChildren().add(WFUtil.getText(" "));
		g = WFUtil.group(WFUtil.getHeaderTextVB(bref + "created"), WFUtil.getHeaderText(": "));
		g.getChildren().add(WFUtil.getTextVB(ref + "creationDate"));
		p.getChildren().add(g);
		p.getChildren().add(WFUtil.getText(" "));
		g = WFUtil.group(WFUtil.getHeaderTextVB(bref + "status"), WFUtil.getHeaderText(": "));
		g.getChildren().add(WFUtil.getTextVB(ref + "status"));
		p.getChildren().add(g);
		p.getChildren().add(WFUtil.getText(" "));
		HtmlOutputText t = WFUtil.getTextVB(ref + "categoryNames");
		t.setConverter(new WFCommaSeparatedListConverter());
		g = WFUtil.group(WFUtil.getHeaderTextVB(bref + "categories"), WFUtil.getHeaderText(": "));
		g.getChildren().add(t);
		p.getChildren().add(g);
		p.getChildren().add(WFUtil.getText(" "));
		g = WFUtil.group(WFUtil.getHeaderTextVB(bref + "current_version"), WFUtil.getHeaderText(": "));
		g.getChildren().add(WFUtil.getTextVB(ref + "versionId"));
		//		g.getChildren().add(WFUtil.getText("1.5"));
		p.getChildren().add(g);
		p.getChildren().add(WFUtil.getText(" "));
		g = WFUtil.group(WFUtil.getHeaderTextVB(bref + "comment"), WFUtil.getHeaderText(": "));
		g.getChildren().add(WFUtil.getTextVB(ref + "comment"));
		p.getChildren().add(g);
		p.getChildren().add(WFUtil.getText(" "));
		g = WFUtil.group(WFUtil.getHeaderText("source"), WFUtil.getHeaderText(": "));
		g.getChildren().add(WFUtil.getTextVB(ref + "source"));
		p.getChildren().add(g);
		p.getChildren().add(WFUtil.getText(" "));

		cs.add(p);
		cs.setSelectedId(NO_ARTICLE_ID, true);
		dp.getChildren().add(cs);
		return dp;
	}
	
	private void selectComponent() {
		WFComponentSelector cs = (WFComponentSelector) findComponent(COMPONENT_SELECTOR_ID);

		String headline = WFUtil.getStringValue(ARTICLE_ITEM_BEAN_ID, "headline");
		if (cs != null) {
			if (headline == null || headline.length() == 0) {
				cs.setSelectedId(NO_ARTICLE_ID, true);
				cs.setSelectedId(ARTICLE_LIST_ID, false);
			}
			else {
				cs.setSelectedId(NO_ARTICLE_ID, false);
				cs.setSelectedId(ARTICLE_LIST_ID, true);
			}
		}
		else {
			cs.setSelectedId(NO_ARTICLE_ID, false);
			cs.setSelectedId(ARTICLE_LIST_ID, true);
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