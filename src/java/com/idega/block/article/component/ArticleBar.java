/*
 * Created on Nov 23, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.idega.block.article.component;

import java.io.Serializable;
import java.util.Iterator;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import com.idega.core.view.ViewManager;
import com.idega.core.view.ViewNode;
import com.idega.idegaweb.IWMainApplication;
import com.idega.presentation.text.Text;
import com.idega.webface.WFContainer;
import com.idega.webface.WFMenu;
import com.idega.webface.WFTabBar;
import com.idega.webface.WFTabbedPane;
import com.idega.webface.WFUtil;
import com.idega.webface.event.WFTabEvent;


/**
 * @author Joakim
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ArticleBar  extends WFContainer implements  Serializable{

	private final static String P = "article_page_"; // Parameter prefix
	
	private final static String TASK_ID_CONTENT = P + "t_content";
	private final static String TASK_ID_EDIT = P + "t_edit";
	private final static String TASK_ID_BUILDER = P + "t_builder";
	private final static String TASK_ID_WEBVIEW = P + "t_webview";
	
	private final static String MAIN_TASKBAR_ID = P + "main_taskbar";
	private final static String ARTICLE_LIST_ID = P + "article_list";
	private final static String CASE_LIST_ID = P + "case_list";	
	
	
	private static String STYLE_CLASS="wf_workspacebar";
	
	/**
	 * 
	 */
	public ArticleBar() {
		super();
	}
	
	public void initializeContent(){
		setStyleClass(STYLE_CLASS);
		addApplicationDecoration();
		addTabbar();
//		addLogin();
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
//		WFPlainOutputText text = new WFPlainOutputText();
//		text.setValue("<i>e</i>Platform");
//		div.add(text);
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
		IWMainApplication iwma = IWMainApplication.getIWMainApplication(context);
		
		for (Iterator iter = workspaceNode.getParent().getChildren().iterator(); iter.hasNext();) {
			ViewNode subNode = (ViewNode) iter.next();
			String url = subNode.getURI();
			tb.addLink(subNode.getName(),url);
		}
		
		return tb;
	}
	
	/**
	 * Returns the content admin perspective.
	 */
	protected UIComponent getContentPerspective() {
		UIComponent perspective = new Text("Content");
		return perspective;
	}

	/**
	 * Returns the content admin perspective.
	 */
	protected UIComponent getWebViewPerspective() {
		UIComponent perspective = new Text("Webview");
		return perspective;
	}

	/**
	 * Returns the content admin perspective.
	 */
	protected UIComponent getBuilderPerspective() {
		UIComponent perspective = new Text("Builder");
		return perspective;
	}	
	
	
	/**
	 * Called when the edit mode in the article block changes.
	 * @see com.idega.webface.event.WFTabListener#taskbarButtonPressed() 
	 */
	public void tabPressed(WFTabEvent e) {
		WFTabbedPane t = e.getTaskbar();
	}

	
	/**
	 * @see javax.faces.event.ActionListener#processAction(javax.faces.event.ActionEvent)
	 */
	public void processAction(ActionEvent event) {
		UIComponent link = event.getComponent();
		String id = WFUtil.getParameter(link, "id");
	}
}