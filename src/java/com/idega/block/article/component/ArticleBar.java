/*
 * $Id: ArticleBar.java,v 1.9 2009/05/27 16:09:37 valdas Exp $
 *
 * Copyright (C) 2004 Idega. All Rights Reserved.
 *
 * This software is the proprietary information of Idega.
 * Use is subject to license terms.
 */
package com.idega.block.article.component;

import java.io.Serializable;
import javax.faces.component.html.HtmlOutputLink;
import javax.faces.context.FacesContext;
import com.idega.core.view.KeyboardShortcut;
import com.idega.core.view.ViewManager;
import com.idega.core.view.ViewNode;
import com.idega.webface.WFContainer;
import com.idega.webface.WFMenu;
import com.idega.webface.WFTabBar;

/**
 * Last modified: $Date: 2009/05/27 16:09:37 $ by $Author: valdas $
 *
 * @author Joakim
 * @version $Revision: 1.9 $
 */
public class ArticleBar extends WFContainer implements  Serializable{

	private static final long serialVersionUID = 6585576699010701325L;
	
	private final static String P = "article_page_"; // Parameter prefix
	private final static String MAIN_TASKBAR_ID = P + "main_taskbar";
	private static String STYLE_CLASS="wf_workspacebar";
	
	public ArticleBar() {
		super();
	}
	
	@Override
	public void initializeComponent(FacesContext context){
		setStyleClass(STYLE_CLASS);
		addApplicationDecoration();
		addTabbar();
	}

	private void addTabbar() {
		WFMenu bar = getMainTaskbar();
		add(bar);
	}

	private void addApplicationDecoration() {
		WFContainer div = new WFContainer();
		div.setStyleClass("wf_appdecor");
		add(div);
	}

	/**
	 * Returns the main task bar selector. 
	 */
	protected WFMenu getMainTaskbar() {

		WFTabBar tb = new WFTabBar();
		tb.setId(MAIN_TASKBAR_ID);
		
		FacesContext context = FacesContext.getCurrentInstance();
		
		ViewManager viewManager = ViewManager.getInstance(context);
		
		ViewNode workspaceNode = viewManager.getViewNodeForContext(context);
//		IWMainApplication iwma = IWMainApplication.getIWMainApplication(context);
		
		for (ViewNode subNode: workspaceNode.getParent().getChildren()) {
			String url = subNode.getURIWithContextPath();
			HtmlOutputLink link =  tb.addLink(subNode.getName(),url);
			
			//Add a shortcut key if the view node has one
			KeyboardShortcut shortCut = subNode.getKeyboardShortcut();
			if(shortCut!=null){
				link.setAccesskey(shortCut.getActionKey());
			}
		}
		
		return tb;
	}
	
	/**
	 * Called when the edit mode in the article block changes.
	 * @see com.idega.webface.event.WFTabListener#taskbarButtonPressed() 
	 */
//	public void tabPressed(WFTabEvent e) {
//		WFTabbedPane t = e.getTaskbar();
//	}

	
	/**
	 * @see javax.faces.event.ActionListener#processAction(javax.faces.event.ActionEvent)
	 */
//	public void processAction(ActionEvent event) {
//		UIComponent link = event.getComponent();
//		String id = WFUtil.getParameter(link, "id");
//	}
}