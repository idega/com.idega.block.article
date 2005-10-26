/*
 * $Id: SearchArticleBlock.java,v 1.12 2005/10/26 11:44:04 tryggvil Exp $
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
import javax.faces.component.html.HtmlInputText;
import javax.faces.component.html.HtmlPanelGrid;
import javax.faces.component.html.HtmlSelectOneMenu;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import org.apache.myfaces.custom.calendar.HtmlInputCalendar;
import org.apache.myfaces.custom.stylesheet.Stylesheet;
import com.idega.content.bean.ManagedContentBeans;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWMainApplication;
import com.idega.presentation.Script;
import com.idega.webface.WFBlock;
import com.idega.webface.WFContainer;
import com.idega.webface.WFErrorMessages;
import com.idega.webface.WFList;
import com.idega.webface.WFPanelUtil;
import com.idega.webface.WFUtil;

/**
 * Block for searching articles.   
 * <p>
 * Last modified: $Date: 2005/10/26 11:44:04 $ by $Author: tryggvil $
 *
 * @author Anders Lindman
 * @version $Revision: 1.12 $
 */
public class SearchArticleBlock extends WFBlock implements ManagedContentBeans, ActionListener, Serializable {

	private static final long serialVersionUID = 3256441400122814775L;

	public final static String SEARCH_ARTICLE_BLOCK_ID = "search_article_block";

	
	private final static String P = "search_article_block_"; // Id prefix
	 
	private final static String SEARCH_TEXT_ID = P + "search_text";
	private final static String SEARCH_AUTHOR_ID = P + "search_author";
	private final static String SEARCH_CATEGORY_ID = P + "search_category";
	private final static String SEARCH_PUBLISHED_FROM_ID = P + "search_published_from";
	private final static String SEARCH_PUBLISHED_TO_ID = P + "search_published_to";

	private final static String SEARCH_BUTTON_ID = P + "search_button";

	private final static String RESULT_LIST_ID = P + "result_list";
	
	/**
	 * Default contructor.
	 */
	public SearchArticleBlock() {
		super("Article Search");
		setId(SEARCH_ARTICLE_BLOCK_ID);
		WFUtil.invoke(SEARCH_ARTICLE_BEAN_ID, "setArticleLinkListener", this, ActionListener.class);
	}
		
	/* (non-Javadoc)
	 * @see com.idega.presentation.IWBaseComponent#initializeContent()
	 */
	protected void initializeComponent(FacesContext context) {
		add(getSearchPanel());
		add(WFUtil.getBreak());
		add(getSearchResultList());
		add(WFUtil.getBreak());
	}

	/*
	 * Creates a search form panel.
	 */
	private UIComponent getSearchPanel() {

		String ref = SEARCH_ARTICLE_BEAN_ID + ".";

		WFContainer mainContainer = new WFContainer();

		WFErrorMessages em = new WFErrorMessages();
		em.addErrorMessage(SEARCH_PUBLISHED_FROM_ID);
		em.addErrorMessage(SEARCH_PUBLISHED_TO_ID);		

		mainContainer.add(em);

		HtmlPanelGrid p = WFPanelUtil.getFormPanel(3);
		
		//adding myfaces component stuff
		IWMainApplication iwma = IWMainApplication.getDefaultIWMainApplication();
		IWBundle myfacesBundle = iwma.getBundle("org.apache.myfaces");
		String jsPath = myfacesBundle.getResourcesVirtualPath()+"/popcalendar.js";
		String styleSheet1Path = myfacesBundle.getResourcesVirtualPath()+"/style/WH/theme.css";
		String styleSheet2Path = myfacesBundle.getResourcesVirtualPath()+"/style/DB/theme.css";

		Stylesheet stylesheet = new Stylesheet();
		stylesheet.setPath(styleSheet1Path);
		stylesheet.setId("css1");
		mainContainer.add(stylesheet);
		
		Stylesheet stylesheet2 = new Stylesheet();
		stylesheet2.setPath(styleSheet2Path);
		stylesheet2.setId("css2");
		mainContainer.add(stylesheet2);
		
		Script script = new Script();
		script.setScriptSource(jsPath);
		mainContainer.add(script);
		//myfaces done
		
		p.getChildren().add(WFUtil.getText("Article text:"));
		p.getChildren().add(WFUtil.getText("Author:"));
		p.getChildren().add(WFUtil.getText("Category"));
		
		HtmlInputText searchTextInput = WFUtil.getInputText(SEARCH_TEXT_ID, ref + "searchText");		
		searchTextInput.setSize(40);
		p.getChildren().add(searchTextInput);		
		HtmlInputText searchAuthorInput = WFUtil.getInputText(SEARCH_AUTHOR_ID, ref + "searchAuthor");		
		searchAuthorInput.setSize(30);
		p.getChildren().add(searchAuthorInput);		
		HtmlSelectOneMenu searchCategoryMenu = WFUtil.getSelectOneMenu(SEARCH_CATEGORY_ID, ref + "categories", ref + "searchCategory");
		
		p.getChildren().add(searchCategoryMenu);		
		p.getChildren().add(WFUtil.getText("Published from:"));		
		p.getChildren().add(WFUtil.getText("Published to:"));		
		p.getChildren().add(WFUtil.getText(" "));		

//		HtmlInputText searchPublishedFromInput = WFUtil.getInputText(SEARCH_PUBLISHED_FROM_ID, ref + "searchPublishedFrom");		
//		searchPublishedFromInput.setSize(20);
//		searchPublishedFromInput.setConverter(new WFDateConverter());
//		
		HtmlInputCalendar searchPublishedFromInput = new HtmlInputCalendar();
		searchPublishedFromInput.setId("fromDate");
		searchPublishedFromInput.setRenderAsPopup(true);
		
		WFUtil.setValueBinding(searchPublishedFromInput,"value",ref + "searchPublishedFrom");
		p.getChildren().add(searchPublishedFromInput);		
		
//		HtmlInputText searchPublishedToInput = WFUtil.getInputText(SEARCH_PUBLISHED_TO_ID, ref + "searchPublishedTo");		
//		searchPublishedToInput.setSize(20);
//		searchPublishedToInput.setConverter(new WFDateConverter());
		
		HtmlInputCalendar searchPublishedToInput = new HtmlInputCalendar();
		searchPublishedToInput.setId("toDate");
		searchPublishedToInput.setRenderAsPopup(true);
		WFUtil.setValueBinding(searchPublishedToInput,"value",ref + "searchPublishedTo");
		
		p.getChildren().add(searchPublishedToInput);		
		p.getChildren().add(WFUtil.getText(" "));

		mainContainer.add(p);
		
		p = WFPanelUtil.getPlainFormPanel(1);
		p.getChildren().add(WFUtil.getText(" "));
		p.getChildren().add(WFUtil.getButton(SEARCH_BUTTON_ID, "Search", this));
		
		mainContainer.add(p);
				
		return mainContainer;
	}
	
	/**
	 * Returns the article list.
	 */
	protected UIComponent getSearchResultList() {
		WFList l = new WFList(SEARCH_ARTICLE_BEAN_ID, 0, 0);
		l.setId(RESULT_LIST_ID);
		WFUtil.setValueBinding(l,"rendered",SEARCH_ARTICLE_BEAN_ID+".showSearchResults");
		
		return l;
	}
	
	/**
	 * javax.faces.event.ActionListener#processAction()
	 */
	public void processAction(ActionEvent event) {
//		String date = "no date";
//		date = "" + WFUtil.getValue(SEARCH_ARTICLE_BEAN_ID, "searchPublishedFrom");
//		
		if (event.getComponent().getId().equals(SEARCH_BUTTON_ID)) {
			WFUtil.invoke(SEARCH_ARTICLE_BEAN_ID, "search");
			return;
		}
		
//		UIComponent link = event.getComponent();
//		String id = WFUtil.getParameter(link, "id");
//		WFUtil.invoke(ARTICLE_ITEM_BEAN_ID, "clear");
//		WFUtil.invoke(ARTICLE_ITEM_BEAN_ID, "setLocaleId", "sv");
//		WFUtil.invoke(ARTICLE_ITEM_BEAN_ID, "setHeadline", "search result, date =" + date);
//		WFUtil.invoke(ARTICLE_ITEM_BEAN_ID, "setTeaser", "Teaser");
//		WFUtil.invoke(ARTICLE_ITEM_BEAN_ID, "setBody", "Article " + id);
//		WFUtil.invoke(ARTICLE_ITEM_BEAN_ID, "setAuthor", "author");
//		WFUtil.invoke(ARTICLE_ITEM_BEAN_ID, "setComment", "comment");
//		WFUtil.invoke(ARTICLE_ITEM_BEAN_ID, "setDescription", "description");
//		WFUtil.invoke(ARTICLE_ITEM_BEAN_ID, "setStatus", ContentItemCase.STATUS_PUBLISHED);
////		WFUtil.invoke(ARTICLE_ITEM_BEAN_ID, "setMainCategoryId", new Integer(3));
//		WFUtil.invoke(ARTICLE_ITEM_BEAN_ID, "setUpdated", new Boolean(true));
//				
//		WFUtil.setViewRoot("/cmspage.jsf");
	}
}
