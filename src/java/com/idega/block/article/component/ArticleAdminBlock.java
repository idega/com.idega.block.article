/*
 * $Id: ArticleAdminBlock.java,v 1.7 2007/05/30 15:03:03 gediminas Exp $
 *
 * Copyright (C) 2004 Idega. All Rights Reserved.
 *
 * This software is the proprietary information of Idega.
 * Use is subject to license terms.
 */
package com.idega.block.article.component;

import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import org.apache.myfaces.custom.savestate.UISaveState;
import com.idega.block.article.IWBundleStarter;
import com.idega.content.bean.ManagedContentBeans;
import com.idega.webface.WFBlockTabbed;
import com.idega.webface.WFTabbedPane;
import com.idega.webface.WFUtil;

/**
 * <p>
 * This is the main block for administering articles (creating,editing)
 * </p>
 * Last modified: $Date: 2007/05/30 15:03:03 $ by $Author: gediminas $
 *
 * @author Joakim, Tryggvi Larusson
 * @version $Revision: 1.7 $
 */
public class ArticleAdminBlock extends WFBlockTabbed implements ActionListener, ManagedContentBeans {

	public final static String ARTICLE_BLOCK_ID = "article_admin_block";
	private final static String P = "article_admin_block_"; // Id prefix
	
	private final static String TASKBAR_ID = P + "taskbar";
	public final static String TASK_ID_EDIT = P + "t_edit";
	public final static String TASK_ID_PREVIEW = P + "t_preview";
	public final static String TASK_ID_LIST = P + "t_list";
	public final static String TASK_ID_DETAILS = P + "t_details";
	
	private String mode;
	
	public ArticleAdminBlock() {
		super(_("article"));
		setId(ARTICLE_BLOCK_ID);
		setMaximizedVertically(true);
	}
	
	/* (non-Javadoc)
	 * @see com.idega.webface.WFBlockTabbed#initializeTabbedPane(javax.faces.context.FacesContext)
	 */
	protected WFTabbedPane initializeTabbedPane(FacesContext context) {
		WFTabbedPane tb = new WFTabbedPane();
		//tb.setMainAreaStyleClass(WFContainer.DEFAULT_STYLE_CLASS);
		tb.setId(TASKBAR_ID);
		//add(tb);
		EditArticleView editArticleBlock = new EditArticleView();
		tb.addTab(TASK_ID_EDIT, _("edit"), editArticleBlock);
		tb.addTab(TASK_ID_DETAILS, _("details"), new ArticleDetailView());
//		tb.addTab(TASK_ID_LIST, _("list"), new ListArticlesBlock());
		tb.addTab(TASK_ID_PREVIEW, _("preview"), new ArticlePreview());
		tb.setSelectedMenuItemId(TASK_ID_EDIT);
		//if (taskbarListener != null) {
		//	tb.addTabListener(taskbarListener);
		//}
		String editMode = getEditMode();
 		if(editMode!=null){
			editArticleBlock.setEditMode(editMode);
		}
		return tb;
	}
	
	
	public void initializeComponent(FacesContext context){
		super.initializeComponent(context);
		
		//Saving the state of the articleItemBean specially because the scpoe
		//of this bean now is 'request' not 'session'
		UISaveState beanSaveState = new UISaveState();
		ValueBinding binding = WFUtil.createValueBinding("#{"+ARTICLE_ITEM_BEAN_ID+"}");
		beanSaveState.setId("articleItemBeanSaveState");
		beanSaveState.setValueBinding("value",binding);
		add(beanSaveState);
		

	}
	
	protected EditArticleView getEditArticleView(){
		WFTabbedPane tab = getTabbedPane();
		if(tab!=null){
			return (EditArticleView) tab.getTabView(TASK_ID_EDIT);
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
		return this.mode;
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
		values[1] = this.mode;
		return values;
	}

	/**
	 * @see javax.faces.component.UIPanel#restoreState(javax.faces.context.FacesContext,
	 *      java.lang.Object)
	 */
	public void restoreState(FacesContext ctx, Object state) {
		Object values[] = (Object[]) state;
		super.restoreState(ctx, values[0]);
		this.mode = (String)values[1];
		//super.restoreState(ctx,state);
	}
	
	private static String _(String localizationKey) {
		return WFUtil.getLocalizedStringExpr(IWBundleStarter.BUNDLE_IDENTIFIER, localizationKey);
	}

}
