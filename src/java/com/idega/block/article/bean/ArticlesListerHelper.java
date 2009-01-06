package com.idega.block.article.bean;

import java.io.Serializable;

public interface ArticlesListerHelper extends Serializable {

	public static final String SPRING_BEAN_IDENTIFIER = "articlesListerHelper";
	
	public String getJavaScriptUris();
	
	public String getStyleSheetsUris();
	
}
