/*
 * $Id: ArticleVersionBlock.java,v 1.6 2007/05/30 15:03:03 gediminas Exp $
 *
 * Copyright (C) 2004 Idega. All Rights Reserved.
 *
 * This software is the proprietary information of Idega.
 * Use is subject to license terms.
 *
 */
package com.idega.block.article.component;

import java.io.Serializable;
import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import com.idega.block.article.IWBundleStarter;
import com.idega.content.bean.ManagedContentBeans;
import com.idega.content.data.ContentItemCase;
import com.idega.webface.WFBlock;
import com.idega.webface.WFContainer;
import com.idega.webface.WFList;
import com.idega.webface.WFPlainOutputText;
import com.idega.webface.WFUtil;
import com.idega.webface.convert.WFCommaSeparatedListConverter;

/**
 * Block for listing article versions.   
 * <p>
 * Last modified: $Date: 2007/05/30 15:03:03 $ by $Author: gediminas $
 *
 * @author Anders Lindman
 * @version $Revision: 1.6 $
 */
public class ArticleVersionBlock extends WFBlock implements ManagedContentBeans, ActionListener, Serializable {

	public final static String ARTICLE_VERSION_BLOCK_ID = "search_article_block";
	
	private final static String P = "article_version_block_"; // Id prefix
	
	private final static String ARTICLE_VERSION_LIST_ID = P + "list";
	private final static String PREVIEW_PANEL_ID = P + "preview_panel";
	
	/**
	 * Default contructor.
	 */
	public ArticleVersionBlock() {
		setId(ARTICLE_VERSION_BLOCK_ID);
		
		WFUtil.invoke(ARTICLE_VERSION_LIST_BEAN_ID, "setArticleLinkListener", this, ActionListener.class);

		add(getVersionList());
		UIComponent c = getPreviewContainer();
		c.setRendered(false);
		add(c);
	}
	
	/*
	 * Creates an article version list.
	 */
	private UIComponent getVersionList() {
		WFList l = new WFList(ARTICLE_VERSION_LIST_BEAN_ID);
		l.setId(ARTICLE_VERSION_LIST_ID);
		return l;
	}
	
	/*
	 * Creates an article preview container.
	 */
	private UIComponent getPreviewContainer() {
		WFContainer c = new WFContainer();
		c.setId(PREVIEW_PANEL_ID);
		c.setStyleAttribute("padding", "14px");

		String ref = ARTICLE_VERSION_ITEM_BEAN_ID + ".";
		String bid = IWBundleStarter.BUNDLE_IDENTIFIER;
		
		c.add(WFUtil.getHeaderTextVB(ref + "headline"));
		c.add(WFUtil.getBreak(2));
		c.add(WFUtil.getTextVB(ref + "teaser"));
		c.add(WFUtil.getBreak(2));
		WFPlainOutputText bodyText = new WFPlainOutputText();
		WFUtil.setValueBinding(bodyText, "value", ref + "body");
		c.add(bodyText);
		c.add(WFUtil.getBreak(4));
		
		c.add(WFUtil.getHeaderTextVB(bid, "author"));
		c.add(WFUtil.getHeaderText(": "));
		c.add(WFUtil.getTextVB(ref + "author"));
		c.add(WFUtil.getBreak(2));
		c.add(WFUtil.getHeaderTextVB(bid, "created"));
		c.add(WFUtil.getHeaderText(": "));
		c.add(WFUtil.getText("4/20/04 3:04 PM"));
		c.add(WFUtil.getBreak(2));
		c.add(WFUtil.getHeaderTextVB(bid, "status"));
		c.add(WFUtil.getHeaderText(": "));
		c.add(WFUtil.getTextVB(ref + "status"));
		c.add(WFUtil.getBreak(2));
		c.add(WFUtil.getHeaderTextVB(bid, "categories"));
		c.add(WFUtil.getHeaderText(": "));
		HtmlOutputText t = WFUtil.getTextVB(ref + "categoryNames");
		t.setConverter(new WFCommaSeparatedListConverter());		
		c.add(t);
		c.add(WFUtil.getBreak(2));
		c.add(WFUtil.getHeaderTextVB(bid, "current_version"));
		c.add(WFUtil.getHeaderText(": "));
		c.add(WFUtil.getText("1.5"));
		c.add(WFUtil.getBreak(2));
		c.add(WFUtil.getHeaderTextVB(bid, "comment"));
		c.add(WFUtil.getHeaderText(": "));
		c.add(WFUtil.getTextVB(ref + "comment"));
		c.add(WFUtil.getBreak(2));
		c.add(WFUtil.getHeaderTextVB(bid, "source"));
		c.add(WFUtil.getHeaderText(": "));
		c.add(WFUtil.getTextVB(ref + "source"));
		
		c.add(WFUtil.getBreak());
				
		return c;
	}
	
	/**
	 * javax.faces.event.ActionListener#processAction()
	 */
	public void processAction(ActionEvent event) {
		
		UIComponent link = event.getComponent();
		String id = WFUtil.getParameter(link, "id");
		
		WFUtil.invoke(ARTICLE_VERSION_ITEM_BEAN_ID, "clear");
		WFUtil.invoke(ARTICLE_VERSION_ITEM_BEAN_ID, "setLocaleId", "sv");
		WFUtil.invoke(ARTICLE_VERSION_ITEM_BEAN_ID, "setHeadline", "Previous version");
		WFUtil.invoke(ARTICLE_VERSION_ITEM_BEAN_ID, "setTeaser", "Teaser");
		WFUtil.invoke(ARTICLE_VERSION_ITEM_BEAN_ID, "setBody", "Article " + id);
		WFUtil.invoke(ARTICLE_VERSION_ITEM_BEAN_ID, "setAuthor", "author");
		WFUtil.invoke(ARTICLE_VERSION_ITEM_BEAN_ID, "setComment", "comment");
		WFUtil.invoke(ARTICLE_VERSION_ITEM_BEAN_ID, "setDescription", "description");
		WFUtil.invoke(ARTICLE_VERSION_ITEM_BEAN_ID, "setSource", "source");
		WFUtil.invoke(ARTICLE_VERSION_ITEM_BEAN_ID, "setStatus", ContentItemCase.STATUS_PUBLISHED);
//		WFUtil.invoke(ARTICLE_VERSION_ITEM_BEAN_ID, "setMainCategoryId", new Integer(3));
		
		WFUtil.invoke(ARTICLE_VERSION_ITEM_BEAN_ID, "setUpdated", new Boolean(true));
		
		link.getParent().getParent().getParent().findComponent(PREVIEW_PANEL_ID).setRendered(true);		
	}
}
