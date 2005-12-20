/*
 * $Id: ListArticlesBlock.java,v 1.12 2005/12/20 16:40:41 tryggvil Exp $
 *
 * Copyright (C) 2004 Idega. All Rights Reserved.
 *
 * This software is the proprietary information of Idega.
 * Use is subject to license terms.
 *
 */
package com.idega.block.article.component;

import java.io.IOException;
import java.io.Serializable;
import java.util.Locale;
import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlInputText;
import javax.faces.component.html.HtmlPanelGrid;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import org.apache.xmlbeans.XmlException;
import com.idega.block.article.bean.ArticleItemBean;
import com.idega.content.bean.ManagedContentBeans;
import com.idega.content.data.ContentItemCase;
import com.idega.presentation.IWBaseComponent;
import com.idega.webface.WFComponentSelector;
import com.idega.webface.WFContainer;
import com.idega.webface.WFErrorMessages;
import com.idega.webface.WFList;
import com.idega.webface.WFPage;
import com.idega.webface.WFPanelUtil;
import com.idega.webface.WFPlainOutputText;
import com.idega.webface.WFUtil;
import com.idega.webface.convert.WFDateConverter;

/**
 * Block for listing articles.   
 * <p>
 * Last modified: $Date: 2005/12/20 16:40:41 $ by $Author: tryggvil $
 *
 * @author Anders Lindman
 * @version $Revision: 1.12 $
 */
public class ListArticlesBlock extends 
IWBaseComponent
implements ManagedContentBeans, ActionListener, Serializable {

	public final static String LIST_ARTICLES_BLOCK_ID = "list_articles_block";

	public final static String LIST_ARTICLES_BEAN_ID = "listArticlesBeanOld";
	
	private final static String P = "list_articles_block_"; // Id prefix
	
	private final static String SEARCH_PUBLISHED_FROM_ID = P + "search_published_from";
	private final static String SEARCH_PUBLISHED_TO_ID = P + "search_published_to";
	//private final static String SEARCH_CATEGORY_ID = P + "search_category";

	private final static String RESULT_LIST_ID = P + "result_list";
	//private final static String LOCALE_ID = P + "locale_id";
	private final static String LIST_BUTTON_ID = P + "list_button";
	private final static String VIEW_ARTICLE_BACK_BUTTON_ID = P + "view_back_button";

	private final static String LIST_PANEL_ID = P + "list_panel";
	private final static String VIEW_ARTICLE_PANEL_ID = P + "view_article_panel";
	private final static String DISPLAY_SELECTOR_ID = P + "selector";
	
	/**
	 * Constructs a ListArticlesBlock with the specified title key. 
	 */
	public ListArticlesBlock() {
		WFUtil.invoke(LIST_ARTICLES_BEAN_ID, "setArticleLinkListener", this, ActionListener.class);
	}

	protected void initializeComponent(FacesContext context) {
		setId(LIST_ARTICLES_BLOCK_ID);
		WFComponentSelector cs = new WFComponentSelector();
		cs.setId(DISPLAY_SELECTOR_ID);
		cs.add(getListPanel());
		cs.add(getViewArticlePanel());
		cs.setSelectedId(LIST_PANEL_ID, true);
		cs.setSelectedId(VIEW_ARTICLE_PANEL_ID, false);		
		add(cs);
	}
	/*
	 * Creates a search form panel.
	 */
	private UIComponent getListPanel() {
		WFContainer listPanel = new WFContainer();
		listPanel.setId(LIST_PANEL_ID);
		listPanel.add(getSearchPanel());
		listPanel.add(WFUtil.getBreak());
		listPanel.add(getSearchResultList());
		return listPanel;
	}
	
	/*
	 * Creates a search form panel.
	 */
	private UIComponent getSearchPanel() {
		String ref = LIST_ARTICLES_BEAN_ID + ".";
		String bref = WFPage.CONTENT_BUNDLE + ".";

		WFContainer mainContainer = new WFContainer();
		
		WFErrorMessages em = new WFErrorMessages();
		em.addErrorMessage(SEARCH_PUBLISHED_FROM_ID);
		em.addErrorMessage(SEARCH_PUBLISHED_TO_ID);
		
		mainContainer.add(em);
		
		HtmlPanelGrid p = WFPanelUtil.getFormPanel(3);		
		p.getChildren().add(WFUtil.group(WFUtil.getTextVB(bref + "published_from"), WFUtil.getText(":")));		
		p.getChildren().add(WFUtil.group(WFUtil.getTextVB(bref + "published_to"), WFUtil.getText(":")));		
		p.getChildren().add(WFUtil.group(WFUtil.getTextVB(bref + "category"), WFUtil.getText(":")));		
		HtmlInputText searchPublishedFromInput = WFUtil.getInputText(SEARCH_PUBLISHED_FROM_ID, ref + "searchPublishedFrom");		
		searchPublishedFromInput.setSize(20);
		searchPublishedFromInput.setConverter(new WFDateConverter());
		p.getChildren().add(searchPublishedFromInput);		
		HtmlInputText searchPublishedToInput = WFUtil.getInputText(SEARCH_PUBLISHED_TO_ID, ref + "searchPublishedTo");		
		searchPublishedToInput.setSize(20);
		searchPublishedToInput.setConverter(new WFDateConverter());
		p.getChildren().add(searchPublishedToInput);		
		//HtmlSelectOneMenu searchCategoryMenu = WFUtil.getSelectOneMenu(SEARCH_CATEGORY_ID, ref + "categories", ref + "searchCategoryId");
		//searchCategoryMenu.setConverter(new IntegerConverter());
		//p.getChildren().add(searchCategoryMenu);
		
		mainContainer.add(p);
		mainContainer.add(WFUtil.getText(" "));

		p = WFPanelUtil.getPlainFormPanel(1);
		p.getChildren().add(WFUtil.getButtonVB(LIST_BUTTON_ID, bref + "list", this));
		
		mainContainer.add(p);
		
		return mainContainer;
	}
	
	/*
	 * Returns the article list.
	 */
	private UIComponent getSearchResultList() {
		WFList l = new WFList(LIST_ARTICLES_BEAN_ID, 0, 5);
		l.setRowClasses(null);
		l.setId(RESULT_LIST_ID);
		l.setNavigationBelowList(true);
		return l;
	}
	
	/*
	 * Returns the view article panel.
	 */
	private UIComponent getViewArticlePanel() {
		String ref = ARTICLE_ITEM_BEAN_ID + ".";
		String bref = WFPage.CONTENT_BUNDLE + ".";
		WFContainer c = new WFContainer();
		c.setStyleAttribute("padding", "10px");
		c.setId(VIEW_ARTICLE_PANEL_ID);
		c.add(WFUtil.getTextVB(ref + "case.publishedFromDate"));
		c.add(new WFPlainOutputText("&nbsp;&nbsp;"));
		//HtmlSelectOneMenu localeMenu = WFUtil.getSelectOneMenu(LOCALE_ID, ref + "allLocales", ref + "localeId");
		//localeMenu.setOnchange("document.forms[0].submit();");
		//c.add(localeMenu);		
		c.add(WFUtil.getBreak(2));
		c.add(WFUtil.getLinkVB("", ref + "headline", null));
		c.add(WFUtil.getBreak(2));
		WFPlainOutputText t = new WFPlainOutputText();
		WFUtil.setValueBinding(t, "value", ref + "body");
		c.add(t);
		c.add(WFUtil.getBreak(2));
		c.add(WFUtil.getButtonVB(VIEW_ARTICLE_BACK_BUTTON_ID, bref + "back", this));
		return c;
	}
	
	/**
	 * javax.faces.event.ActionListener#processAction()
	 */
	public void processAction(ActionEvent event) {
		if (event.getComponent().getId().equals(LIST_BUTTON_ID)) {
			WFUtil.invoke(LIST_ARTICLES_BEAN_ID, "list");
			return;
		} else if (event.getComponent().getId().equals(VIEW_ARTICLE_BACK_BUTTON_ID)) {
			WFComponentSelector cs = (WFComponentSelector) event.getComponent().findComponent(DISPLAY_SELECTOR_ID);
			cs.setSelectedId(LIST_PANEL_ID, true);
			cs.setSelectedId(VIEW_ARTICLE_PANEL_ID, false);
			return;
		}

		UIComponent link = event.getComponent();
		String id = WFUtil.getParameter(link, "id");
		
		ArticleItemBean articleItem = new ArticleItemBean();
		try {
			articleItem.setResourcePath(id);
			articleItem.load();
			
			ArticleItemBean bean = getArticleItemBean();
			
			//WFUtil.invoke(ARTICLE_ITEM_BEAN_ID, "clear");
			bean.clear();
			
			//WFUtil.invoke(ARTICLE_ITEM_BEAN_ID, "setLocaleId", "en");
			Locale locale = new Locale("en");
			bean.setLocale(locale);
			//WFUtil.invoke(ARTICLE_ITEM_BEAN_ID, "setHeadline", notNull(articleItem.getHeadline()));
			bean.setHeadline(articleItem.getHeadline());
			//WFUtil.invoke(ARTICLE_ITEM_BEAN_ID, "setTeaser", notNull(articleItem.getTeaser()));
			bean.setTeaser(articleItem.getTeaser());
			//WFUtil.invoke(ARTICLE_ITEM_BEAN_ID, "setBody", notNull(articleItem.getBody()));
			bean.setBody(articleItem.getBody());
			//WFUtil.invoke(ARTICLE_ITEM_BEAN_ID, "setAuthor", notNull(articleItem.getAuthor()));
			bean.setAuthor(articleItem.getAuthor());
			//WFUtil.invoke(ARTICLE_ITEM_BEAN_ID, "setComment", notNull(articleItem.getComment()));
			bean.setComment(articleItem.getComment());
			//WFUtil.invoke(ARTICLE_ITEM_BEAN_ID, "setDescription", notNull(articleItem.getDescription()));
			bean.setDescription(articleItem.getDescription());
			//WFUtil.invoke(ARTICLE_ITEM_BEAN_ID, "setStatus", 
//					articleItem.getStatus()
//					ContentItemCase.STATUS_PUBLISHED
//					);
			bean.setStatus(ContentItemCase.STATUS_PUBLISHED);
//			WFUtil.invoke(ARTICLE_ITEM_BEAN_ID, "setMainCategoryId", new Integer(3));
//			WFUtil.invoke(ARTICLE_ITEM_BEAN_ID, "setFolderLocation", notNull(articleItem.getFolderLocation()));
			bean.setBaseFolderLocation(articleItem.getBaseFolderLocation());
			//WFUtil.invoke(ARTICLE_ITEM_BEAN_ID, "setLocaleId", "sv");

			//And one more time since it won't work after just setting the params once...
			
			/*WFUtil.invoke(ARTICLE_ITEM_BEAN_ID, "setHeadline", notNull(articleItem.getHeadline()));
			WFUtil.invoke(ARTICLE_ITEM_BEAN_ID, "setTeaser", notNull(articleItem.getTeaser()));
			WFUtil.invoke(ARTICLE_ITEM_BEAN_ID, "setBody", notNull(articleItem.getBody()));
			WFUtil.invoke(ARTICLE_ITEM_BEAN_ID, "setAuthor", notNull(articleItem.getAuthor()));
			WFUtil.invoke(ARTICLE_ITEM_BEAN_ID, "setFolderLocation", notNull(articleItem.getBaseFolderLocation()));
			*/
			bean.setHeadline(articleItem.getHeadline());
			bean.setTeaser(articleItem.getTeaser());
			bean.setBody(articleItem.getBody());
			bean.setAuthor(articleItem.getAuthor());
			bean.setBaseFolderLocation(articleItem.getBaseFolderLocation());
			
			WFComponentSelector cs = (WFComponentSelector) event.getComponent().getParent().getParent().getParent().findComponent(DISPLAY_SELECTOR_ID);
			cs.setSelectedId(LIST_PANEL_ID, false);
			cs.setSelectedId(VIEW_ARTICLE_PANEL_ID, true);
		}
		catch (XmlException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private String notNull(String str) {
		if(null==str) {
			return "<Empty>";
		}
		return str;
	}
	
	public ArticleItemBean getArticleItemBean(){
		return (ArticleItemBean) WFUtil.getBeanInstance(ARTICLE_ITEM_BEAN_ID);
	}
}
