/*
 * $Id: WFUtilArticle.java,v 1.8 2007/06/08 16:01:54 valdas Exp $
 *
 * Copyright (C) 2004 Idega. All Rights Reserved.
 *
 * This software is the proprietary information of Idega.
 * Use is subject to license terms.
 *
 */
package com.idega.block.article;

import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlCommandButton;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.event.ActionListener;

import com.idega.content.business.ContentConstants;
import com.idega.webface.WFUtil;


/**
 * Util class to set the bundle for the localization 
 * <p>
 * Last modified: $Date: 2007/06/08 16:01:54 $ by $Author: valdas $
 *
 * @author Joakim Johnson
 * @version $Revision: 1.8 $
 */
public class WFUtilArticle{
	private static final String ARTICLE_BUNDLE = "com.idega.block.article";
	
	private String bundle = "com.idega.block.article";

	private WFUtilArticle(String s) {
		this.bundle = s;
	}
	
	public static WFUtilArticle getWFUtilArticle() {
		return new WFUtilArticle(ARTICLE_BUNDLE);
	}

	public static WFUtilArticle getWFUtilContent() {
		return new WFUtilArticle(ContentConstants.CONTENT_BUNDLE);
	}

	public String getBundleString() {
		return this.bundle;
	}

	/**
	 * Returns a localized HtmlOutputText as a header
	 */
	public HtmlOutputText getHeaderTextVB(String localizationKey) {
		return WFUtil.getHeaderTextVB(this.bundle, localizationKey);
	}

	/**
	 * Returns a localized HtmlOutputText
	 */
	public HtmlOutputText getTextVB(String localizationKey) {
		return WFUtil.getTextVB(this.bundle, localizationKey);
	}

	/**
	 * Returns an html list text with value binding.
	 */
	public HtmlOutputText getListTextVB(String localizationKey) {
		return WFUtil.getListTextVB(this.bundle, localizationKey);
	}

	/**
	 * Returns a localized HtmlCommand Button
	 */
	public HtmlCommandButton getButtonVB(String id, String localizationKey, ActionListener actionListener) {
		return WFUtil.getButtonVB(id, this.bundle, localizationKey, actionListener);
	}
	
	/**
	 * Adds a UIParameter with value binding to the specified component. 
	 */
	public void addParameterVB(UIComponent component, String name, String localizationKey) {
		WFUtil.addParameterVB(component, name, this.bundle, localizationKey);
	}
	
	/**
	 * Adds a message with value binding for the specified component. 
	 */
	public void addMessageVB(UIComponent component, String localizationKey) {
		WFUtil.addMessageVB(component, this.bundle, localizationKey);
	}
}
