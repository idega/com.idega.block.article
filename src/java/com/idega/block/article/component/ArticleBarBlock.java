/*
 * $Id: ArticleBarBlock.java,v 1.8 2005/03/30 21:31:19 eiki Exp $
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
import com.idega.content.bean.ManagedContentBeans;
import com.idega.webface.WFBlock;
import com.idega.webface.WFContainer;
import com.idega.webface.WFPage;
import com.idega.webface.WFTabbedPane;
import com.idega.webface.event.WFTabListener;


/**
 * Last modified: $Date: 2005/03/30 21:31:19 $ by $Author: eiki $
 *
 * @author Joakim
 * @version $Revision: 1.8 $
 */
public class ArticleBarBlock extends WFBlock implements ActionListener, ManagedContentBeans {

	
	private static final String STYLE_CLASS = "wf_block BlockStretch";
	
	public final static String ARTICLE_BLOCK_ID = "article_bar_block";
	private final static String P = "article_bar_block_"; // Id prefix
	
	private final static String TASKBAR_ID = P + "taskbar";
	public final static String TASK_ID_EDIT = P + "t_edit";
//	public final static String TASK_ID_PREVIEW = P + "t_preview";
	public final static String TASK_ID_LIST = P + "t_list";
	public final static String TASK_ID_DETAILS = P + "t_details";
	EditArticleBlock editArticleBlock = new EditArticleBlock();

	public ArticleBarBlock() {
		setStyleClass(STYLE_CLASS);
	}
	
	/**
	 * Constructs an ArticleBlock with the specified title key and taskbar listener. 
	 */
	public ArticleBarBlock(String titleKey) {
		this(titleKey, null);
	}
	
	/**
	 * Constructs an ArticleBlock with the specified title key. 
	 */
	public ArticleBarBlock(String titleKey, WFTabListener taskbarListener) {
		super(titleKey, true);
		
		setStyleClass(STYLE_CLASS);
		
		setId(ARTICLE_BLOCK_ID);
		setMainAreaStyleClass(null);
		
		String bref = WFPage.CONTENT_BUNDLE + ".";
		
		WFTabbedPane tb = new WFTabbedPane();
		tb.setId(TASKBAR_ID);
		add(tb);
		tb.addTabVB(TASK_ID_EDIT, bref + "edit", editArticleBlock);
		tb.addTabVB(TASK_ID_DETAILS, bref + "details", new ArticleDetailView());
		tb.addTabVB(TASK_ID_LIST, bref + "list", new ListArticlesBlock());
//		tb.addTabVB(TASK_ID_PREVIEW, bref + "preview", new ArticlePreview());
		tb.setSelectedMenuItemId(TASK_ID_EDIT);
		if (taskbarListener != null) {
			tb.addTabListener(taskbarListener);
		}
	}

	public void setEditMode(String mode) {
		editArticleBlock.setEditMode(mode);
	}
	
	/* (non-Javadoc)
	 * @see javax.faces.event.ActionListener#processAction(javax.faces.event.ActionEvent)
	 */
	public void processAction(ActionEvent arg0) throws AbortProcessingException {
		// TODO Auto-generated method stub
	}
}
