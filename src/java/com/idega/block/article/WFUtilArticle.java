/*
 * $Id: WFUtilArticle.java,v 1.4 2004/12/21 16:28:44 joakim Exp $
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
import com.idega.webface.WFUtil;


/**
 * Util class to set the bundle for the localization 
 * <p>
 * Last modified: $Date: 2004/12/21 16:28:44 $ by $Author: joakim $
 *
 * @author Joakim Johnson
 * @version $Revision: 1.4 $
 */
public class WFUtilArticle{
	private String BUNDLE = "com.idega.block.article";

	public WFUtilArticle(String s) {
		BUNDLE = s;
	}
	
	public static WFUtilArticle getWFUtilArticle() {
		return new WFUtilArticle("com.idega.block.article");
	}

	public String getBundleString() {
		return BUNDLE;
	}

	/**
	 * Returns a localized HtmlOutputText as a header
	 */
	public HtmlOutputText getHeaderTextVB(String localizationKey) {
		return WFUtil.getHeaderTextVB(BUNDLE, localizationKey);
	}

	/**
	 * Returns a localized HtmlOutputText
	 */
	public HtmlOutputText getTextVB(String localizationKey) {
		return WFUtil.getTextVB(BUNDLE, localizationKey);
	}

	/**
	 * Returns an html list text with value binding.
	 */
	public HtmlOutputText getListTextVB(String localizationKey) {
		return WFUtil.getListTextVB(BUNDLE, localizationKey);
	}

	/**
	 * Returns a localized HtmlCommand Button
	 */
	public HtmlCommandButton getButtonVB(String id, String localizationKey) {
		return WFUtil.getButtonVB(id, BUNDLE, localizationKey);
	}
	
	/**
	 * Adds a UIParameter with value binding to the specified component. 
	 */
	public void addParameterVB(UIComponent component, String name, String localizationKey) {
		WFUtil.addParameterVB(component, name, BUNDLE, localizationKey);
	}
	
	/**
	 * Adds a message with value binding for the specified component. 
	 */
	public void addMessageVB(UIComponent component, String localizationKey) {
		WFUtil.addMessageVB(component, BUNDLE, localizationKey);
	}
}
