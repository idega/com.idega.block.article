/*
 * $Id: CMSPage.java,v 1.10 2005/01/04 14:30:58 joakim Exp $
 *
 * Copyright (C) 2004 Idega. All Rights Reserved.
 *
 * This software is the proprietary information of Idega.
 * Use is subject to license terms.
 *
 */
package com.idega.block.article;

import java.io.IOException;
import java.io.Serializable;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import org.apache.xmlbeans.XmlException;
import com.idega.block.article.bean.ArticleItemBean;
import com.idega.block.article.component.ArticleVersionBlock;
import com.idega.block.article.component.EditArticleBlock;
import com.idega.webface.WFBlock;
import com.idega.webface.WFList;
import com.idega.webface.WFPage;
import com.idega.webface.WFTab;
import com.idega.webface.WFTabbedPane;
import com.idega.webface.WFUtil;
import com.idega.webface.WFViewMenu;
import com.idega.webface.event.WFTabEvent;
import com.idega.webface.event.WFTabListener;
import com.idega.webface.test.bean.ContentItemCase;
import com.idega.webface.test.bean.ManagedContentBeans;

/**
 * Content management system test/demo page. 
 * <p>
 * Last modified: $Date: 2005/01/04 14:30:58 $ by $Author: joakim $
 *
 * @author Anders Lindman
 * @version $Revision: 1.10 $
 */
public class CMSPage extends WFPage implements  ManagedContentBeans, WFTabListener, ActionListener, Serializable {
	
	private final static String P = "cms_page_"; // Parameter prefix
	
	private final static String TASK_ID_CONTENT = P + "t_content";
	private final static String TASK_ID_EDIT = P + "t_edit";

	private final static String MAIN_TASKBAR_ID = P + "main_taskbar";
	private final static String ARTICLE_LIST_ID = P + "article_list";
	private final static String CASE_LIST_ID = P + "case_list";
	
	private boolean isInitalized=false;
	
	/**
	 * @see javax.faces.component.UIComponent#encodeBegin(javax.faces.context.FacesContext)
	 */
	public void encodeBegin(FacesContext context) throws IOException {
		if (!isInitalized) {
			createContent();
			isInitalized=true;
		}
		else {
			//debug
			UIComponent button1 = findComponent(TASK_ID_CONTENT);
			WFTab contentButton = (WFTab)button1;
			if(contentButton!=null) {
				ActionListener[] listeners = contentButton.getActionListeners();
				String st = listeners.toString();
			}
			
		}
		super.encodeBegin(context);
	}

	/* (non-Javadoc)
	 * @see javax.faces.component.UIComponent#encodeChildren(javax.faces.context.FacesContext)
	 */
	public void encodeChildren(FacesContext context) throws IOException {
		// TODO Auto-generated method stub
		super.encodeChildren(context);
	}
	/* (non-Javadoc)
	 * @see javax.faces.component.UIComponent#encodeEnd(javax.faces.context.FacesContext)
	 */
	public void encodeEnd(FacesContext arg0) throws IOException {
		// TODO Auto-generated method stub
		super.encodeEnd(arg0);
	}
	/* (non-Javadoc)
	 * @see javax.faces.component.UIComponent#getRendersChildren()
	 */
	public boolean getRendersChildren() {
		// TODO Auto-generated method stub
		return super.getRendersChildren();
	}
	/**
	 * Creates the page content. 
	 */
	protected void createContent() {
		boolean isArticleBeanUpdated = WFUtil.getBooleanValue(ARTICLE_ITEM_BEAN_ID, "updated");
		WFUtil.invoke(ARTICLE_ITEM_BEAN_ID, "setUpdated", new Boolean(false));
		
		WFUtil.invoke(ARTICLE_LIST_BEAN_ID, "setArticleLinkListener", this, ActionListener.class);
		
		WFUtil.invoke(CASE_LIST_BEAN_ID, "setCaseLinkListener", this, ActionListener.class);
		
		//getChildren().add(WFUtil.getBannerBox());
		//getChildren().add(getMainTaskbar());
		
		getChildren().add(getCaseList());
		getChildren().add(getArticleList());
		
		//HtmlInputSecret passwdInput = new HtmlInputSecret();
		//passwdInput.setValue("test");
		//passwdInput.setTitle("Testtitle");
		//getChildren().add(passwdInput);
		
		if (isArticleBeanUpdated) {
			setEditMode();
		}
	}
	
	/**
	 * Returns the main task bar selector. 
	 */
	/*
	protected UIComponent getMainTaskbar() {
		String bref = WFPage.CONTENT_BUNDLE + ".";
		WFTabbedPane tb = new WFTabbedPane();
		tb.setMainAreaStyleClass(null);
		tb.setId(MAIN_TASKBAR_ID);
		WFTab buttonContent = tb.addTabVB(TASK_ID_CONTENT, bref + "content", getContentPerspective());
		buttonContent.addActionListener(this);
		WFTab buttonEdit = tb.addTabVB(TASK_ID_EDIT, bref + "edit", getEditPerspective());
		buttonEdit.addActionListener(this);
		tb.addTabListener(this);
		
		
		UICommand debugButton = new HtmlCommandButton();
		debugButton.setId("debugbutton");
		debugButton.setValue("debug");
		debugButton.setImmediate(true);
		
		debugButton.addActionListener(this);
		this.getChildren().add(debugButton);
		
		
		return tb;
	}
	*/
	/**
	 * Returns the content admin perspective.
	 */
	/*
	protected UIComponent getContentPerspective() {
		HtmlPanelGrid p = WFPanelUtil.getApplicationPanel();
		p.getChildren().add(getFunctionBlock());		
		WFContainer c = new WFContainer();
		c.add(getArticleList());
		c.add(getCaseList());
		p.getChildren().add(c);
		return p;
	}
	*/
	/**
	 * Returns the content edit perspective.
	 */
	/*
	protected UIComponent getEditPerspective() {
		String bref = WFPage.CONTENT_BUNDLE + ".";
		HtmlPanelGrid ap = WFPanelUtil.getApplicationPanel();
		ap.getChildren().add(getFunctionBlock());
		WFContainer c = new WFContainer();
		ArticleBlock ab = new ArticleBlock(bref + "edit_article", this);
		c.add(ab);
		ArticleVersionBlock av = new ArticleVersionBlock(bref + "previous_article_versions");
		av.getTitlebar().setValueRefTitle(true);
		av.setRendered(false);
		c.add(av);
		ap.getChildren().add(c);
		return ap;
	}
	*/
	/**
	 * Returns the function block containing a view menu.
	 */
	protected UIComponent getFunctionBlock() {

		String bref = WFPage.CONTENT_BUNDLE + ".";
		WFBlock b = new WFBlock(bref + "functions");
		b.getTitlebar().setValueRefTitle(true);

		WFViewMenu vm = new WFViewMenu();
		b = WFUtil.setBlockStyle(b, vm);
		b.setId(bref+"function_block");
		vm.addButtonVB("content_home", bref + "content_home", "/cmspage.jsf");
		
		String createArticleURL = "/idegaweb/bundles/com.idega.block.article.bundle/jsp/createarticle.jsf";
		String listArticlesURL = "/idegaweb/bundles/com.idega.block.article.bundle/jsp/listarticles.jsf";
		String searchArticleURL = "/idegaweb/bundles/com.idega.block.article.bundle/jsp/searcharticle.jsf";
		String userApplicationURL = "/idegaweb/bundles/com.idega.block.article.bundle/jsp/UserApplication.jsf";
		String createPageURL = "/idegaweb/bundles/com.idega.block.article.bundle/jsp/CreatePage.jsf";
		String configureURL = "/idegaweb/bundles/com.idega.block.article.bundle/jsp/Configure.jsf";
		
		vm.addButtonVB("create_article", bref + "create_article", createArticleURL);
		vm.addButtonVB("list_articles", bref + "list_articles", listArticlesURL);
		vm.addButtonVB("search_articles", bref + "search_articles", searchArticleURL);
		vm.addButtonVB("users_groups", bref + "users_and_groups", userApplicationURL);
		vm.addButtonVB("create_page", bref + "create_page", createPageURL);
		vm.addButtonVB("configure", bref + "configure", configureURL);
		b.add(vm);
		return b;
	}
	
	/**
	 * Returns the article list.
	 */
	protected UIComponent getArticleList() {
		String bref = WFPage.CONTENT_BUNDLE + ".";
		WFBlock b = new WFBlock(bref + "article_list");
		b.getTitlebar().setValueRefTitle(true);
		WFList l = new WFList(ARTICLE_LIST_BEAN_ID, 0, 3);
		l.setId(ARTICLE_LIST_ID);
		b.add(l);
		return b;
	}

	/**
	 * Returns the case list.
	 */
	protected UIComponent getCaseList() {
		String bref = WFPage.CONTENT_BUNDLE + ".";
		WFBlock b = new WFBlock(bref + "case_list");
		b.getTitlebar().setValueRefTitle(true);
		WFList l = new WFList(CASE_LIST_BEAN_ID, 0, 3);
		l.setId(CASE_LIST_ID);
		b.add(l);
		return b;
	}

	/**
	 * @see javax.faces.event.ActionListener#processAction(javax.faces.event.ActionEvent)
	 */
	public void processAction(ActionEvent event) {
		UIComponent link = event.getComponent();
//		EditArticleBlock ab = null;
		
		String id = WFUtil.getParameter(link, "id");
		UIComponent component = link.getParent().getParent().getParent().findComponent(MAIN_TASKBAR_ID);
//		if(component instanceof WFTabbedPane){
//			WFTabbedPane tb = (WFTabbedPane) component;
//			tb.setSelectedMenuItemId(TASK_ID_EDIT);
//			ab = (EditArticleBlock) tb.findComponent(EditArticleBlock.ARTICLE_BLOCK_ID);
//			ab.setEditMode();
//		}

		ArticleItemBean articleItem = new ArticleItemBean();
		try {
			articleItem.load(id);

			WFUtil.invoke(ARTICLE_ITEM_BEAN_ID, "clear");

			WFUtil.invoke(ARTICLE_ITEM_BEAN_ID, "setLocaleId", "en");
			WFUtil.invoke(ARTICLE_ITEM_BEAN_ID, "setHeadline", notNull(articleItem.getHeadline()));
			WFUtil.invoke(ARTICLE_ITEM_BEAN_ID, "setTeaser", notNull(articleItem.getTeaser()));
			WFUtil.invoke(ARTICLE_ITEM_BEAN_ID, "setBody", notNull(articleItem.getBody()));
			WFUtil.invoke(ARTICLE_ITEM_BEAN_ID, "setAuthor", notNull(articleItem.getAuthor()));
			WFUtil.invoke(ARTICLE_ITEM_BEAN_ID, "setComment", notNull(articleItem.getComment()));
			WFUtil.invoke(ARTICLE_ITEM_BEAN_ID, "setDescription", notNull(articleItem.getDescription()));
			WFUtil.invoke(ARTICLE_ITEM_BEAN_ID, "setStatus", 
//					articleItem.getStatus()
					ContentItemCase.STATUS_PUBLISHED
					);
			WFUtil.invoke(ARTICLE_ITEM_BEAN_ID, "setMainCategoryId", new Integer(3));
			WFUtil.invoke(ARTICLE_ITEM_BEAN_ID, "setMainCategory", articleItem.getMainCategory());
			WFUtil.invoke(ARTICLE_ITEM_BEAN_ID, "setLocaleId", "sv");

			//And one more time since it won't work after just setting the params once...
			
			WFUtil.invoke(ARTICLE_ITEM_BEAN_ID, "setHeadline", notNull(articleItem.getHeadline()));
			WFUtil.invoke(ARTICLE_ITEM_BEAN_ID, "setTeaser", notNull(articleItem.getTeaser()));
			WFUtil.invoke(ARTICLE_ITEM_BEAN_ID, "setBody", notNull(articleItem.getBody()));
			WFUtil.invoke(ARTICLE_ITEM_BEAN_ID, "setAuthor", notNull(articleItem.getAuthor()));
			WFUtil.invoke(ARTICLE_ITEM_BEAN_ID, "setMainCategory", articleItem.getMainCategory());
		}
		catch (XmlException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

//		
//		if(ab!=null){
//			ab.updateEditButtons();
//		}
	}
	
	private String notNull(String str) {
		if(null==str) {
			return "<Empty>";
		}
		return str;
	}

	/**
	 * Sets the page in edit mode.
	 */
	public void setEditMode() {
		WFTabbedPane tb = (WFTabbedPane) findComponent(MAIN_TASKBAR_ID);
		tb.setSelectedMenuItemId(TASK_ID_EDIT);
		EditArticleBlock ab = (EditArticleBlock) tb.findComponent(EditArticleBlock.EDIT_ARTICLE_BLOCK_ID);
		//TODO (JJ) what is this... should not be needed.
//		ab.setEditMode();		
	}
	
	/**
	 * Called when the edit mode in the article block changes.
	 * @see com.idega.webface.event.WFTabListener#taskbarButtonPressed() 
	 */
	public void tabPressed(WFTabEvent e) {
		WFTabbedPane t = e.getTaskbar();
		UIComponent articleVersionBlock = t.findComponent(ArticleVersionBlock.ARTICLE_VERSION_BLOCK_ID);
//		TODO (JJ) what is this... should not be needed.
		if (t.getSelectedMenuItemId().equals(EditArticleBlock.TASK_ID_PREVIEW)) {
			articleVersionBlock.setRendered(true);
		} else {
			articleVersionBlock.setRendered(false);			
		}
	}
	
	/* (non-Javadoc)
	 * @see javax.faces.component.StateHolder#restoreState(javax.faces.context.FacesContext, java.lang.Object)
	 */
	public void restoreState(FacesContext ctx, Object state) {
		Object values[] = (Object[])state;
		super.restoreState(ctx, values[0]);
		Boolean bIsInitalized = (Boolean) values[1];
		this.isInitalized=bIsInitalized.booleanValue();
		//super.restoreState(ctx,state);
	}

	/* (non-Javadoc)
	 * @see javax.faces.component.StateHolder#saveState(javax.faces.context.FacesContext)
	 */
	public Object saveState(FacesContext ctx) {
		Object values[] = new Object[2];
		values[0] = super.saveState(ctx);
		values[1] = Boolean.valueOf(this.isInitalized);
		return values;
		//return super.saveState(ctx);
	}
}
