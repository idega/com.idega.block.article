/*
 * Created on Dec 1, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.idega.block.article;

import javax.faces.component.UIComponent;
import com.idega.block.article.component.ArticleBarBlock;
import com.idega.webface.WFPage;


/**
 * @author Joakim
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ArticlePage extends CMSPage {
	ArticleBarBlock abb=null;
	
	public ArticlePage(){
		super();
	}

	/**
	 * Creates the page content. 
	 */
	protected void createContent() {
		add(getEditPerspective());
		//When using the HtmlOutputLink, this code is executed. Either have to set a flag,
		//or use some other methode to cleane the bean when necesary.
//		WFUtil.invoke(ARTICLE_ITEM_BEAN_ID, "clear");
	}
	
	/**
	 * Returns the main task bar selector. 
	 */
	protected UIComponent getMainTaskbar() {
		return getEditPerspective();
	}
	
	/**
	 * Returns the content edit perspective.
	 */
	protected UIComponent getEditPerspective() {
		//TODO (JJ) This should be changed to ARTICLE_BUNDLE
		String bref = WFPage.CONTENT_BUNDLE + ".";
		//Has to be set to value bound for this to work...
//		String bref = WFUtilArticle.getBundleString() + ".";
		abb = new ArticleBarBlock(bref + "article");
		abb.setId("article_block");
		return abb;
	}

	/**
	 * @param mode
	 */
	public void setEditMode(String mode) {
		if(abb!=null) {
			abb.setEditMode(mode);
		}
	}
}