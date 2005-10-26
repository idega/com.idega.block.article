/*
 * $Id: ArticleAdminBlock.java,v 1.3 2005/10/26 11:44:04 tryggvil Exp $
 *
 * Copyright (C) 2004 Idega. All Rights Reserved.
 *
 * This software is the proprietary information of Idega.
 * Use is subject to license terms.
 */
package com.idega.block.article.component;

import java.util.Collection;
import java.util.Iterator;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import com.idega.content.bean.ManagedContentBeans;
import com.idega.webface.WFBlock;
import com.idega.webface.WFPage;
import com.idega.webface.WFTabbedPane;
import com.idega.webface.event.WFTabListener;


/**
 * <p>
 * This is the main block for administering articles (creating,editing)
 * </p>
 * Last modified: $Date: 2005/10/26 11:44:04 $ by $Author: tryggvil $
 *
 * @author Joakim, Tryggvi Larusson
 * @version $Revision: 1.3 $
 */
public class ArticleAdminBlock extends WFBlock implements ActionListener, ManagedContentBeans {

	
	private static final String STYLE_CLASS = "wf_block BlockStretch";
	public final static String ARTICLE_BLOCK_ID = "article_bar_block";
	private final static String P = "article_bar_block_"; // Id prefix
	
	private final static String TASKBAR_ID = P + "taskbar";
	public final static String TASK_ID_EDIT = P + "t_edit";
	public final static String TASK_ID_PREVIEW = P + "t_preview";
	public final static String TASK_ID_LIST = P + "t_list";
	public final static String TASK_ID_DETAILS = P + "t_details";
	
	private String mode;
	
	public ArticleAdminBlock() {
		this( WFPage.CONTENT_BUNDLE + ".article");
	}
	
	/**
	 * Constructs an ArticleBlock with the specified title key and taskbar listener. 
	 */
	public ArticleAdminBlock(String titleKey) {
		this(titleKey, null);
	}
	public ArticleAdminBlock(String titleKey, WFTabListener taskbarListener) {
		super(titleKey, true);
		setStyleClass(STYLE_CLASS);
		setId(ARTICLE_BLOCK_ID);
		//setMainAreaStyleClass(null);

	}

	
	public void initializeComponent(FacesContext context){
		//TODO Remove this and use newer localization system:
		WFPage.loadResourceBundles(context);
		
		super.initializeComponent(context);

		String bref = WFPage.CONTENT_BUNDLE + ".";
		WFTabbedPane tb = new WFTabbedPane();
		//tb.setMainAreaStyleClass(WFContainer.DEFAULT_STYLE_CLASS);
		tb.setId(TASKBAR_ID);
		add(tb);
		EditArticleView editArticleBlock = new EditArticleView();
		tb.addTabVB(TASK_ID_EDIT, bref + "edit", editArticleBlock);
		tb.addTabVB(TASK_ID_DETAILS, bref + "details", new ArticleDetailView());
//		tb.addTabVB(TASK_ID_LIST, bref + "list", new ListArticlesBlock());
		tb.addTabVB(TASK_ID_PREVIEW, bref + "preview", new ArticlePreview());
		tb.setSelectedMenuItemId(TASK_ID_EDIT);
		//if (taskbarListener != null) {
		//	tb.addTabListener(taskbarListener);
		//}
		String editMode = getEditMode();
		if(editMode!=null){
			editArticleBlock.setEditMode(editMode);
		}
	}

	protected WFTabbedPane getWFTabbedPane(){
		
		Collection children = getChildren();
		Iterator iter = children.iterator();
		if(iter.hasNext()){
			return (WFTabbedPane)iter.next();
		}
		return null;
	}

	
	protected EditArticleView getEditArticleView(){
		WFTabbedPane tab = getWFTabbedPane();
		if(tab!=null){
			return (EditArticleView) tab.getPerspective(TASK_ID_EDIT);
		}
		return null;
	}
	
	public void setEditMode(String mode) {
		//editArticleBlock.setEditMode(mode);
		//getAttributes().put(KEY_EDITMODE,mode);
		this.mode=mode;
		EditArticleView editView = getEditArticleView();
		if(editView!=null){
			editView.setEditMode(mode);
		}
	}
	
	public String getEditMode(){
		//String sReturn = (String) getAttributes().get(KEY_EDITMODE);
		//return sReturn;
		return mode;
	}
	
	/* (non-Javadoc)
	 * @see javax.faces.event.ActionListener#processAction(javax.faces.event.ActionEvent)
	 */
	public void processAction(ActionEvent arg0) throws AbortProcessingException {
		// TODO Auto-generated method stub
	}
	
	/**
	 * @see javax.faces.component.UIPanel#saveState(javax.faces.context.FacesContext)
	 */
	public Object saveState(FacesContext ctx) {
		Object values[] = new Object[2];
		values[0] = super.saveState(ctx);
		values[1] = mode;
		return values;
	}

	/**
	 * @see javax.faces.component.UIPanel#restoreState(javax.faces.context.FacesContext,
	 *      java.lang.Object)
	 */
	public void restoreState(FacesContext ctx, Object state) {
		Object values[] = (Object[]) state;
		super.restoreState(ctx, values[0]);
		mode = (String)values[1];
		//super.restoreState(ctx,state);
	}
	
}
