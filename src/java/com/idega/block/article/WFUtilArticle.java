/*
 * $Id: WFUtilArticle.java,v 1.2 2004/12/17 14:40:29 joakim Exp $
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
 * Last modified: $Date: 2004/12/17 14:40:29 $ by $Author: joakim $
 *
 * @author Joakim Johnson
 * @version $Revision: 1.2 $
 */
public class WFUtilArticle extends WFUtil{
	private final static String BUNDLE = "com.idega.block.article";

	/**
	 * Returns a localized HtmlOutputText as a header
	 */
	public static HtmlOutputText getHeaderTextVB(String localizationKey) {
		return getHeaderTextVB(BUNDLE, localizationKey);
	}

	/**
	 * Returns a localized HtmlOutputText
	 */
	public static HtmlOutputText getTextVB(String localizationKey) {
		return getTextVB(BUNDLE, localizationKey);
	}

	/**
	 * Returns an html list text with value binding.
	 */
	public static HtmlOutputText getListTextVB(String localizationKey) {
		return getListTextVB(BUNDLE, localizationKey);
	}

	/**
	 * Returns a localized HtmlCommand Button
	 */
	public static HtmlCommandButton getButtonVB(String id, String localizationKey) {
		return getButtonVB(id, BUNDLE, localizationKey);
	}
	
	/**
	 * Adds a UIParameter with value binding to the specified component. 
	 */
	public static void addParameterVB(UIComponent component, String name, String localizationKey) {
		addParameterVB(component, name, BUNDLE, localizationKey);
	}
	
	/**
	 * Adds a message with value binding for the specified component. 
	 */
	public static void addMessageVB(UIComponent component, String localizationKey) {
		addMessageVB(component, BUNDLE, localizationKey);
	}
}
