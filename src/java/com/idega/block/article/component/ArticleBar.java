/*
 * $Id: ArticleBar.java,v 1.6 2005/10/26 11:44:04 tryggvil Exp $
 *
 * Copyright (C) 2004 Idega. All Rights Reserved.
 *
 * This software is the proprietary information of Idega.
 * Use is subject to license terms.
 */
package com.idega.block.article.component;

import java.io.Serializable;
import java.util.Iterator;
import javax.faces.component.html.HtmlOutputLink;
import javax.faces.context.FacesContext;
import com.idega.core.view.KeyboardShortcut;
import com.idega.core.view.ViewManager;
import com.idega.core.view.ViewNode;
import com.idega.webface.WFContainer;
import com.idega.webface.WFMenu;
import com.idega.webface.WFTabBar;

/**
 * Last modified: $Date: 2005/10/26 11:44:04 $ by $Author: tryggvil $
 *
 * @author Joakim
 * @version $Revision: 1.6 $
 */
public class ArticleBar extends WFContainer implements  Serializable{

	private final static String P = "article_page_"; // Parameter prefix
	private final static String MAIN_TASKBAR_ID = P + "main_taskbar";
	private static String STYLE_CLASS="wf_workspacebar";
	
	public ArticleBar() {
		super();
	}
	
	public void initializeComponent(FacesContext context){
		setStyleClass(STYLE_CLASS);
		addApplicationDecoration();
		addTabbar();
	}

	private void addLogin() {
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
		
		for (Iterator iter = workspaceNode.getParent().getChildren().iterator(); iter.hasNext();) {
			ViewNode subNode = (ViewNode) iter.next();
			String url = subNode.getURI();
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