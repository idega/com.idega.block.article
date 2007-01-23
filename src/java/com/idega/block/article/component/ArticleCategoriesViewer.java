package com.idega.block.article.component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.apache.myfaces.custom.div.Div;

import com.idega.block.article.bean.ArticleItemBean;
import com.idega.content.business.CategoryBean;
import com.idega.content.presentation.ContentItemListViewer;
import com.idega.content.themes.helpers.ThemesConstants;
import com.idega.presentation.Block;
import com.idega.presentation.IWContext;
import com.idega.presentation.text.Break;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;

public class ArticleCategoriesViewer extends Block {
	
	private static final String BLOG_CATEGORIES = "blog-categories";
	private static final String BLOG_LINK_DISABLED = "blog-category-link-disabled";
	private static final String BLOG_LINK_ENABLED = "blog-category-link-enabled";
	private static final String OPENER = "(";
	private static final String CLOSER = ")";
	
	private Map<String, Integer> countedCategories = null;
	
	public ArticleCategoriesViewer() {
		countedCategories = new HashMap<String, Integer>();
	}
	
	public void main(IWContext iwc) {
		CategoryBean categoriesBean = CategoryBean.getInstance();
		Collection<String> availableCategories = categoriesBean.getCategories();
		if (availableCategories == null) {
			return;
		}
		List <String> categories = new ArrayList<String>(availableCategories);
		initCategories(categories);
		
		Div container = new Div();
		container.setId(BLOG_CATEGORIES);
		
		ArticleListViewer articles = new ArticleListViewer();
		articles.setCategoriesList(null);
		countCategories(articles.getArticleListBean().getContentItems());
		
		Div disabledCategoryContainer = null;
		StringBuffer value = null;
		Text text = null;
		Link link = null;
		Map.Entry<String, Integer> entry = null;
		for (Iterator<Map.Entry<String, Integer>> it = countedCategories.entrySet().iterator(); it.hasNext();) {
			entry = it.next();
			value = new StringBuffer();
			value.append(entry.getKey()).append(ThemesConstants.SPACE).append(OPENER).append(entry.getValue()).append(CLOSER);
			if (entry.getValue() > 0) {
				link = new Link(value.toString());
				link.setStyleClass(BLOG_LINK_ENABLED);
				link.addFirstParameter(ContentItemListViewer.ITEMS_CATEGORY_VIEW, entry.getKey());
				container.getChildren().add(link);
				container.getChildren().add(new Break());
			}
			else {
				text = new Text(value.toString());
				disabledCategoryContainer = new Div();
				disabledCategoryContainer.setStyleClass(BLOG_LINK_DISABLED);
				disabledCategoryContainer.getChildren().add(text);
				container.getChildren().add(disabledCategoryContainer);
			}
		}
		this.add(container);
	}
	
	public void restoreState(FacesContext context, Object state) {
		Object values[] = (Object[]) state;
		super.restoreState(context, values[0]);
	}

	public Object saveState(FacesContext context) {
		Object values[] = new Object[1];
		values[0] = super.saveState(context);
		return values;
	}
	
	private void initCategories(List <String> categories) {
		if (categories == null) {
			return;
		}
		if (countedCategories == null) {
			countedCategories = new HashMap<String, Integer>();
		}
		for (int i = 0; i < categories.size(); i++) {
			countedCategories.put(categories.get(i), 0);
		}
	}
	
	private void countCategories(List articles) {
		if (articles == null || countedCategories == null) {
			return;
		}
		Object o = null;
		ArticleItemBean article = null;
		Enumeration allCategories = null;
		for (int i = 0; i < articles.size(); i++) {
			o = articles.get(i);
			if (o instanceof ArticleItemBean) {
				article = (ArticleItemBean) o;
				allCategories = article.getWebDavResourceCategories();
				if (allCategories != null) {
					String categories = null;
					while (allCategories.hasMoreElements()) {
						categories = allCategories.nextElement().toString();
						String[] entries = null;
						if (categories != null) {
							entries = categories.split(CategoryBean.CATEGORY_DELIMETER);
							Integer value = null;
							for (int j = 0; j < entries.length; j++) {
								value = countedCategories.get(entries[j]);
								if (value != null) {
									value++;
									countedCategories.put(entries[j], value);
								}
							}
						}
				     }
				}
			}
		}
	}

}
