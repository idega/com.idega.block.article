/*
 * $Id: WFUtilArticle.java,v 1.6 2005/01/10 10:25:30 joakim Exp $
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
import com.idega.webface.WFUtil;


/**
 * Util class to set the bundle for the localization 
 * <p>
 * Last modified: $Date: 2005/01/10 10:25:30 $ by $Author: joakim $
 *
 * @author Joakim Johnson
 * @version $Revision: 1.6 $
 */
public class WFUtilArticle{
	private static final String ARTICLE_BUNDLE = "com.idega.block.article";
	private static final String CONTENT_BUNDLE = "com.idega.content";
	
	private String bundle = "com.idega.block.article";

	private WFUtilArticle(String s) {
		bundle = s;
	}
	
	public static WFUtilArticle getWFUtilArticle() {
		return new WFUtilArticle(ARTICLE_BUNDLE);
	}

	public static WFUtilArticle getWFUtilContent() {
		return new WFUtilArticle(CONTENT_BUNDLE);
	}

	public String getBundleString() {
		return bundle;
	}

	/**
	 * Returns a localized HtmlOutputText as a header
	 */
	public HtmlOutputText getHeaderTextVB(String localizationKey) {
		return WFUtil.getHeaderTextVB(bundle, localizationKey);
	}

	/**
	 * Returns a localized HtmlOutputText
	 */
	public HtmlOutputText getTextVB(String localizationKey) {
		return WFUtil.getTextVB(bundle, localizationKey);
	}

	/**
	 * Returns an html list text with value binding.
	 */
	public HtmlOutputText getListTextVB(String localizationKey) {
		return WFUtil.getListTextVB(bundle, localizationKey);
	}

	/**
	 * Returns a localized HtmlCommand Button
	 */
	public HtmlCommandButton getButtonVB(String id, String localizationKey, ActionListener actionListener) {
		return WFUtil.getButtonVB(id, bundle, localizationKey, actionListener);
	}
	
	/**
	 * Adds a UIParameter with value binding to the specified component. 
	 */
	public void addParameterVB(UIComponent component, String name, String localizationKey) {
		WFUtil.addParameterVB(component, name, bundle, localizationKey);
	}
	
	/**
	 * Adds a message with value binding for the specified component. 
	 */
	public void addMessageVB(UIComponent component, String localizationKey) {
		WFUtil.addMessageVB(component, bundle, localizationKey);
	}
}
