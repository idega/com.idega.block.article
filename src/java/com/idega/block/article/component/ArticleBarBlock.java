/*
 * $Id: ArticleBarBlock.java,v 1.2 2004/12/09 14:42:15 joakim Exp $
 *
 * Copyright (C) 2004 Idega. All Rights Reserved.
 *
 * This software is the proprietary information of Idega.
 * Use is subject to license terms.
 */
package com.idega.block.article.component;

import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import com.idega.webface.WFBlock;
import com.idega.webface.WFContainer;
import com.idega.webface.WFPage;
import com.idega.webface.WFTabbedPane;
import com.idega.webface.event.WFTabListener;
import com.idega.webface.test.bean.ManagedContentBeans;


/**
 * Last modified: $Date: 2004/12/09 14:42:15 $ by $Author: joakim $
 *
 * @author Joakim
 * @version $Revision: 1.2 $
 */
public class ArticleBarBlock extends WFBlock implements ActionListener, ManagedContentBeans {

	public final static String ARTICLE_BLOCK_ID = "article_bar_block";
	private final static String P = "article_bar_block_"; // Id prefix
	
	private final static String TASKBAR_ID = P + "taskbar";
	public final static String TASK_ID_EDIT = P + "t_edit";
	public final static String TASK_ID_PREVIEW = P + "t_preview";
	public final static String TASK_ID_LIST = P + "t_list";
	public final static String TASK_ID_DETAILS = P + "t_details";
	
	private ArticleBlock articleBlock;

	public ArticleBarBlock() {
	}
	
	/**
	 * Constructs an ArticleBlock with the specified title key. 
	 */
	public ArticleBarBlock(String titleKey, WFTabListener taskbarListener) {
		super(titleKey);
		setId(ARTICLE_BLOCK_ID);
		getTitlebar().setValueRefTitle(true);
		setMainAreaStyleClass(null);
		
		String bref = WFPage.CONTENT_BUNDLE + ".";
		
		WFTabbedPane tb = new WFTabbedPane();
		tb.setId(TASKBAR_ID);
		add(tb);
		tb.addTabVB(TASK_ID_EDIT, bref + "edit", new EditArticleBlock());
//		tb.addTabVB(TASK_ID_PREVIEW, bref + "details", getPreviewContainer());
		tb.addTabVB(TASK_ID_PREVIEW, bref + "details", new ArticleDetailView());
		tb.addTabVB(TASK_ID_LIST, bref + "list", new ListArticlesBlock());
//		tb.addTabVB(TASK_ID_EDIT, bref + "edit2", articleBlock.getEditContainer());
		tb.addTabVB(TASK_ID_DETAILS, bref + "preview", new ArticleItemView());
		tb.setSelectedMenuItemId(TASK_ID_EDIT);
		if (taskbarListener != null) {
			tb.addTabListener(taskbarListener);
		}
		
		WFContainer mainArea = new WFContainer();
		mainArea.setStyleClass("wf_blockmainarea");
	}
	
	/**
	 * Constructs an ArticleBlock with the specified title key and taskbar listener. 
	 */
	public ArticleBarBlock(String titleKey) {
		this(titleKey, null);
	}

	/* (non-Javadoc)
	 * @see javax.faces.event.ActionListener#processAction(javax.faces.event.ActionEvent)
	 */
	public void processAction(ActionEvent arg0) throws AbortProcessingException {
		// TODO Auto-generated method stub
	}
	
	/**
	 * @see javax.faces.component.UIComponent#encodeBegin(javax.faces.context.FacesContext)
	 */
/*
	public void encodeBegin(FacesContext context) throws IOException {
		super.encodeBegin(context);
	}
*/
}
