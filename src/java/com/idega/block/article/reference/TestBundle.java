package com.idega.block.article.reference;

import java.util.ListResourceBundle;

/**
 * @author al
 */
public class TestBundle extends ListResourceBundle {

	static final Object[][] contents = {
		{"name", "Name"},
		{"author", "Phone"},
		{"company", "Company"},
		{"edit_article", "Edit Article"}
	};

	public Object[][] getContents() {
	  return contents;
	}
}
