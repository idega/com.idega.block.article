/*
 * Created on Dec 16, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.idega.block.article;

import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlCommandButton;
import javax.faces.component.html.HtmlOutputText;
import com.idega.webface.WFUtil;


/**
 * @author Joakim
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class WFUtilArticle extends WFUtil{
	private final static String BUNDLE = "com.idega.block.article";

	public static HtmlOutputText getHeaderTextVB(String localizationKey) {
		return getHeaderTextVB(BUNDLE, localizationKey);
	}

	public static HtmlOutputText getTextVB(String localizationKey) {
		return getTextVB(BUNDLE, localizationKey);
	}

	/**
	 * Returns an html list text with value binding.
	 */
	public static HtmlOutputText getListTextVB(String localizationKey) {
		return getListTextVB(BUNDLE, localizationKey);
	}

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
