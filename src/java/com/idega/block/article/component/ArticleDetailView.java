/*
 * $Id: ArticleDetailView.java,v 1.3 2004/12/09 14:42:15 joakim Exp $
 *
 * Copyright (C) 2004 Idega. All Rights Reserved.
 *
 * This software is the proprietary information of Idega.
 * Use is subject to license terms.
 */
package com.idega.block.article.component;

import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.component.html.HtmlPanelGrid;
import com.idega.presentation.IWBaseComponent;
import com.idega.webface.WFPage;
import com.idega.webface.WFPanelUtil;
import com.idega.webface.WFPlainOutputText;
import com.idega.webface.WFUtil;
import com.idega.webface.convert.WFCommaSeparatedListConverter;
import com.idega.webface.test.bean.ManagedContentBeans;

/**
 * Last modified: $Date: 2004/12/09 14:42:15 $ by $Author: joakim $
 *
 * @author Joakim
 * @version $Revision: 1.3 $
 */
public class ArticleDetailView extends IWBaseComponent implements ManagedContentBeans {
	
	public ArticleDetailView() {
	}

	protected void initializeContent() {
		add(getDetailPanel());
	}

	/*
	 * Creates a preview container for the article.
	 */
	private UIComponent getDetailPanel() {
		String ref = ARTICLE_ITEM_BEAN_ID + ".";
		String bref = WFPage.CONTENT_BUNDLE + ".";

		HtmlPanelGrid p = WFPanelUtil.getPlainFormPanel(1);
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
				
		return p;
	}
}
