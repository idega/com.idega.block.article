/*
 * $Id: ArticlePreview.java,v 1.4 2005/11/30 09:34:52 laddi Exp $
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
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import com.idega.content.bean.ManagedContentBeans;
import com.idega.presentation.IWBaseComponent;
import com.idega.webface.WFComponentSelector;
import com.idega.webface.WFPanelUtil;
import com.idega.webface.WFResourceUtil;
import com.idega.webface.WFUtil;

/**
 * Last modified: $Date: 2005/11/30 09:34:52 $ by $Author: laddi $
 *
 * Displays the article item
 *
 * @author Joakim
 * @version $Revision: 1.4 $
 */
public class ArticlePreview extends IWBaseComponent implements ManagedContentBeans, ActionListener{
	public final static String EDIT_ARTICLE_BLOCK_ID = "edit_articles_block";

	private final static String P = "article_item_view_"; // Id prefix

	private final static String COMPONENT_SELECTOR_ID = P + "component_selector";
	private final static String NO_ARTICLE_ID = P + "no_article";
	private final static String ARTICLE_VIEW_ID = P + "article_view";

	public ArticlePreview() {
	}

	protected void initializeComponent(FacesContext context) {
		add(getPreviewPanel());
	}

	/*
	 * Creates a search form panel.
	 */
	private UIComponent getPreviewPanel() {
		String ref = ARTICLE_ITEM_BEAN_ID + ".";

		WFComponentSelector cs = new WFComponentSelector();
		cs.setId(COMPONENT_SELECTOR_ID);
		
		HtmlPanelGrid p = WFPanelUtil.getPlainFormPanel(1);
		p.setId(NO_ARTICLE_ID);
		p.getChildren().add(WFResourceUtil.getResourceUtilArticle().getHeaderTextVB("no_article_selected"));
		cs.add(p);

		ArticleItemViewer item = new ArticleItemViewer();
		item.setId(ARTICLE_VIEW_ID);
		
		WFUtil.setValueBinding(item, "author", ref+"author");
		WFUtil.setValueBinding(item, "creation_date", ref+"creationDate");
		WFUtil.setValueBinding(item, "headline", ref+"headline");
		WFUtil.setValueBinding(item, "teaser", ref+"teaser");
		WFUtil.setValueBinding(item, "body", ref+"body");
		

		cs.add(item);

		cs.setSelectedId(NO_ARTICLE_ID, true);
		return cs;
	}
	

	/**
	 * Selects what component to display. Either the detaild information 
	 * or the text saying that no article is selected.
	 * The decision is based on the presence of article_item_bean.headline is pressent in the session
	 */
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

	/* (non-Javadoc)
	 * @see javax.faces.event.ActionListener#processAction(javax.faces.event.ActionEvent)
	 */
	public void processAction(ActionEvent event) throws AbortProcessingException {
//		String id = event.getComponent().getId();
//		EditArticleBlock ab = (EditArticleBlock) event.getComponent().getParent().getParent().getParent().findComponent(EDIT_ARTICLE_BLOCK_ID);
//		if (id.equals(EDIT_ID)) {
//			ab.storeArticle();
//		}
	}
}
