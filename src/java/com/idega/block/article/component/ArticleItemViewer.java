/*
 * $Id: ArticleItemViewer.java,v 1.15.2.2 2008/01/03 10:08:32 civilis Exp $
 * 
 * Copyright (C) 2004 Idega. All Rights Reserved.
 * 
 * This software is the proprietary information of Idega. Use is subject to license terms.
 */
package com.idega.block.article.component;

import java.sql.Timestamp;
import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.context.FacesContext;
import com.idega.block.article.ArticleCacher;
import com.idega.block.article.bean.ArticleItemBean;
import com.idega.block.article.business.ArticleActionURIHandler;
import com.idega.content.bean.ContentItem;
import com.idega.content.presentation.ContentItemToolbar;
import com.idega.content.presentation.ContentItemViewer;
import com.idega.core.cache.UIComponentCacher;
import com.idega.idegaweb.IWMainApplication;
import com.idega.webface.WFHtml;
import com.idega.webface.convert.WFTimestampConverter;

/**
 * Last modified: $Date: 2008/01/03 10:08:32 $ by $Author: civilis $
 * 
 * Displays the article item
 * 
 * @author <a href="mailto:gummi@idega.com">Gudmundur Agust Saemundsson</a>
 * @version $Revision: 1.15.2.2 $
 */
public class ArticleItemViewer extends ContentItemViewer {

	// constants:
	private final static String ATTRIBUTE_AUTHOR = "author";
	private final static String ATTRIBUTE_CREATION_DATE = "creation_date";
	private final static String ATTRIBUTE_HEADLINE = "headline";
	private final static String ATTRIBUTE_TEASER = "teaser";
	private final static String ATTRIBUTE_BODY = "body";
	private final static String[] ATTRIBUTE_ARRAY = new String[] { ATTRIBUTE_AUTHOR, ATTRIBUTE_CREATION_DATE, ATTRIBUTE_HEADLINE, ATTRIBUTE_TEASER, ATTRIBUTE_BODY };
	private final static String facetIdPrefix = "article_";
	private final static String styleClassPrefix = "article_";
	private final static String DEFAULT_STYLE_CLASS = styleClassPrefix + "item";
	// instance variables:
	private boolean headlineAsLink;
	private String datePattern;
	private boolean cacheEnabled = true;

	/**
	 * @return Returns the cacheEnabled.
	 */
	public boolean isCacheEnabled() {
		return this.cacheEnabled;
	}

	/**
	 * @param cacheEnabled
	 *          The cacheEnabled to set.
	 */
	public void setCacheEnabled(boolean cacheEnabled) {
		this.cacheEnabled = cacheEnabled;
	}

	public ArticleItemViewer() {
		super();
		this.setStyleClass(DEFAULT_STYLE_CLASS);
	}

	public String[] getViewerFieldNames() {
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

	protected UIComponent createFieldComponent(String attribute) {
		if (ATTRIBUTE_BODY.equals(attribute)) {
			return new WFHtml();
		}
		else if (ATTRIBUTE_TEASER.equals(attribute)) {
			return new WFHtml();
		}
		else if (attribute.equals(ATTRIBUTE_HEADLINE) && this.getHeadlineAsLink()) {
			UIComponent link = getEmptyMoreLink();
			HtmlOutputText text = new HtmlOutputText();
			link.getChildren().add(text);
			return link;
		}
		else {
			return new HtmlOutputText();
		}
	}

	protected void initializeComponent(FacesContext context) {
		super.initializeComponent(context);
		((HtmlOutputText) getFieldViewerComponent(ATTRIBUTE_CREATION_DATE)).setConverter(new WFTimestampConverter(datePattern));
	}

	/**
	 * @return Returns the author.
	 */
	public String getAuthor() {
		return (String) getValue(ATTRIBUTE_AUTHOR);
	}

	/**
	 * @param author
	 *          The author to set.
	 */
	public void setAuthor(String author) {
		setFieldLocalValue(ATTRIBUTE_AUTHOR, author);
	}

	/**
	 * @return Returns the body.
	 */
	public String getBody() {
		return (String) getValue(ATTRIBUTE_BODY);
	}

	/**
	 * @param body
	 *          The body to set.
	 */
	public void setBody(String body) {
		setFieldLocalValue(ATTRIBUTE_BODY, body);
	}

	/**
	 * @return Returns the creationDate.
	 */
	public Timestamp getCreationDate() {
		return (Timestamp) getValue(ATTRIBUTE_CREATION_DATE);
	}

	/**
	 * @param creationDate
	 *          The creationDate to set.
	 */
	public void setCreationDate(Timestamp creationDate) {
		setFieldLocalValue(ATTRIBUTE_CREATION_DATE, creationDate);
	}

	/**
	 * @return Returns the headline.
	 */
	public String getHeadline() {
		return (String) getValue(ATTRIBUTE_HEADLINE);
	}

	/**
	 * @param headline
	 *          The headline to set.
	 */
	public void setHeadline(String headline) {
		setFieldLocalValue(ATTRIBUTE_HEADLINE, headline);
	}

	/**
	 * @return Returns the teaser.
	 */
	public String getTeaser() {
		return (String) getValue(ATTRIBUTE_TEASER);
	}

	/**
	 * @param teaser
	 *          The teaser to set.
	 */
	public void setTeaser(String teaser) {
		setFieldLocalValue(ATTRIBUTE_TEASER, teaser);
	}

	public ContentItem loadContentItem(String itemResourcePath) {
		try {
			ArticleItemBean bean = new ArticleItemBean();
			bean.setAutoCreateResource(isAutoCreateResource());
			bean.setResourcePath(itemResourcePath);
			bean.load();
			return bean;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void setHeadlineAsLink(boolean asLink) {
		this.headlineAsLink = asLink;
	}

	public boolean getHeadlineAsLink() {
		return this.headlineAsLink;
	}

	public void setDatePattern(String pattern) {
		this.datePattern = pattern;
	}

	public String getDatePattern() {
		return this.datePattern;
	}

	/**
	 * @see javax.faces.component.UIComponentBase#saveState(javax.faces.context.FacesContext)
	 */
	public Object saveState(FacesContext ctx) {
		Object values[] = new Object[4];
		values[0] = super.saveState(ctx);
		values[1] = Boolean.valueOf(this.headlineAsLink);
		values[2] = this.datePattern;
		values[3] = Boolean.valueOf(this.cacheEnabled);
		return values;
	}

	/**
	 * @see javax.faces.component.UIComponentBase#restoreState(javax.faces.context.FacesContext, java.lang.Object)
	 */
	public void restoreState(FacesContext ctx, Object state) {
		Object values[] = (Object[]) state;
		super.restoreState(ctx, values[0]);
		this.headlineAsLink = values[1] == null ? false : ((Boolean) values[1]).booleanValue();
		this.datePattern = (String)values[2];
		this.cacheEnabled = values[3] == null ? true : ((Boolean) values[3]).booleanValue();
	}

	/**
	 * 
	 */
	public void updateToolbar() {
		ContentItemToolbar toolbar = getToolbar();
		if (toolbar != null) {
			toolbar.setActionHandlerIdentifier(ArticleActionURIHandler.HANDLER_IDENTIFIER);
		}
		super.updateToolbar();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.idega.content.presentation.ContentItemListViewer#getCacher(javax.faces.context.FacesContext)
	 */
	public UIComponentCacher getCacher(FacesContext context) {
		IWMainApplication iwma = IWMainApplication.getIWMainApplication(context);
		return ArticleCacher.getInstance(iwma);
	}

}
