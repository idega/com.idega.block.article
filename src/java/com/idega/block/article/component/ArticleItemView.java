/*
 * $Id: ArticleItemView.java,v 1.14 2005/02/07 10:59:53 gummi Exp $
 *
 * Copyright (C) 2004 Idega. All Rights Reserved.
 *
 * This software is the proprietary information of Idega.
 * Use is subject to license terms.
 */
package com.idega.block.article.component;

import java.sql.Timestamp;
import javax.faces.component.UIOutput;
import javax.faces.component.html.HtmlOutputText;
import com.idega.content.presentation.ContentItemViewer;
import com.idega.webface.WFHtml;
import com.idega.webface.convert.WFTimestampConverter;

/**
 * Last modified: $Date: 2005/02/07 10:59:53 $ by $Author: gummi $
 *
 * Displays the article item
 *
 * @author Joakim
 * @version $Revision: 1.14 $
 */
public class ArticleItemView extends ContentItemViewer {
	
	private final static String ATTRIBUTE_AUTHOR = "author";
	private final static String ATTRIBUTE_CREATION_DATE = "creation_date";
	private final static String ATTRIBUTE_HEADLINE = "headline";
	private final static String ATTRIBUTE_TEASER = "teaser";
	private final static String ATTRIBUTE_BODY = "body";
	
	private final static String[] ATTRIBUTE_ARRAY = new String[] {ATTRIBUTE_AUTHOR,ATTRIBUTE_CREATION_DATE,ATTRIBUTE_HEADLINE,ATTRIBUTE_TEASER,ATTRIBUTE_BODY};
	private final static String facetIdPrefix = "article_";
	private final static String styleClassPrefix = "article_";
	
	
	public ArticleItemView() {
		super();
		this.setStyleClass("article_item");
	}
	
	
	
	protected String[] getViewerAttributes(){
		return ATTRIBUTE_ARRAY;
	}
	
	/**
	 * @return Returns the facetIdPrefix.
	 */
	protected String getFacetIdPrefix() {
		return facetIdPrefix;
	}

	/**
	 * @return Returns the styleClassPrefix.
	 */
	protected String getDefaultStyleClassPrefix() {
		return styleClassPrefix;
	}
	
	protected UIOutput createUIOutputComponent(String attribute){
		if(ATTRIBUTE_BODY.equals(attribute)){
			return new WFHtml();
		} else {
			return new HtmlOutputText();
		}
	}
	
	protected void initializeContent() {	
		super.initializeContent();
		((HtmlOutputText)getFieldViewerComponent(ATTRIBUTE_CREATION_DATE)).setConverter(new WFTimestampConverter());  /////----------------------------------------------
		
		
//		HtmlOutputLink link = new HtmlOutputLink();
//		link.setValue("http://localhost:8080/cms/workspace/content/article/create");
//		link.setStyleClass("wf_listlink");
//		link.setId(getId() + "_dl");
//		link.getChildren().add(WFUtil.getText("Edit"));
//		this.add(link);

	}




	/**
	 * @return Returns the author.
	 */
	public String getAuthor() {
		return (String)getValue(ATTRIBUTE_AUTHOR);
	}	
	/**
	 * @param author The author to set.
	 */
	public void setAuthor(String author) {
		setFieldLocalValue(ATTRIBUTE_AUTHOR,author);
	}

	/**
	 * @return Returns the body.
	 */
	public String getBody() {
		return (String)getValue(ATTRIBUTE_BODY);
	}
	/**
	 * @param body The body to set.
	 */
	public void setBody(String body) {
		setFieldLocalValue(ATTRIBUTE_BODY,body);
	}

	/**
	 * @return Returns the creationDate.
	 */
	public Timestamp getCreationDate() {
		return (Timestamp)getValue(ATTRIBUTE_CREATION_DATE);
	}
	/**
	 * @param creationDate The creationDate to set.
	 */
	public void setCreationDate(Timestamp creationDate) {
		setFieldLocalValue(ATTRIBUTE_CREATION_DATE,creationDate);
	}

	/**
	 * @return Returns the headline.
	 */
	public String getHeadline() {
		return (String)getValue(ATTRIBUTE_HEADLINE);
	}
	/**
	 * @param headline The headline to set.
	 */
	public void setHeadline(String headline) {
		setFieldLocalValue(ATTRIBUTE_HEADLINE,headline);
	}

	/**
	 * @return Returns the teaser.
	 */
	public String getTeaser() {
		return (String)getValue(ATTRIBUTE_TEASER);
	}
	/**
	 * @param teaser The teaser to set.
	 */
	public void setTeaser(String teaser) {
		setFieldLocalValue(ATTRIBUTE_TEASER,teaser);
	}
	

	
}
