package com.idega.block.article.component;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.apache.myfaces.custom.div.Div;

import com.idega.block.article.bean.ArticleListManagedBean;
import com.idega.block.article.business.ArticleUtil;
import com.idega.content.business.CategoryBean;
import com.idega.content.business.ContentUtil;
import com.idega.content.presentation.ContentItemListViewer;
import com.idega.content.themes.helpers.ThemesConstants;
import com.idega.presentation.Block;
import com.idega.presentation.IWContext;
import com.idega.presentation.text.Break;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.CheckBox;

public class ArticleCategoriesViewer extends Block {
	
	private static final String BLOG_CATEGORIES = "blog-categories";
	private static final String BLOG_LINK_DISABLED = "blog-category-link-disabled";
	private static final String BLOG_LINK_ENABLED = "blog-category-link-enabled";
	private static final String BLOG_CHECKBOX_DISABLED = "blog-category-checkbox-disabled";
	private static final String BLOG_CHECKBOX_ENABLED = "blog-category-checkbox-enabled";
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
		initCategories(categories, iwc);

		countCategories(categories, iwc);
		
		Div container = new Div();
		container.setId(BLOG_CATEGORIES);
		Div disabledCategoryContainer = null;
		
		StringBuffer value = null;
		Text text = null;
		Link link = null;
		Integer count = null;
		for (int i = 0; i < categories.size(); i++) {
			count = countedCategories.get(categories.get(i));
			if (count == null) {
				break;
			}
			value = new StringBuffer();
			value.append(categories.get(i)).append(ThemesConstants.SPACE).append(OPENER).append(count).append(CLOSER);
			if (count > 0) {
				addCheckBox(container, iwc, categories.get(i), false);
				link = new Link(value.toString());
				link.setStyleClass(BLOG_LINK_ENABLED);
				link.addFirstParameter(ContentItemListViewer.ITEMS_CATEGORY_VIEW, categories.get(i));
				container.getChildren().add(link);
				container.getChildren().add(new Break());
			}
			else {
				text = new Text(value.toString());
				disabledCategoryContainer = new Div();
				addCheckBox(disabledCategoryContainer, iwc, categories.get(i), true);
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
		this.countedCategories = (Map<String, Integer>) values[1];
	}

	public Object saveState(FacesContext context) {
		Object values[] = new Object[2];
		values[0] = super.saveState(context);
		values[1] = this.countedCategories;
		return values;
	}
	
	private void initCategories(List <String> categories, IWContext iwc) {
		if (categories == null) {
			return;
		}
		if (countedCategories == null) {
			countedCategories = new HashMap<String, Integer>();
		}
		Collator collator = null;
		if (iwc.getCurrentLocale() != null) {
			collator = Collator.getInstance(iwc.getCurrentLocale());
		}
		else {
			collator = Collator.getInstance();
		}
		Collections.sort(categories, collator);
		for (int i = 0; i < categories.size(); i++) {
			countedCategories.put(categories.get(i), 0);
		}
	}
	
	private void countCategories(List<String> categories, IWContext iwc) {
		if (categories == null || countedCategories == null) {
			return;
		}
		ArticleListManagedBean bean = new ArticleListManagedBean();
		List<String> categoriesList = null;
		Collection results = null;
		Integer value = null;
		for (int i = 0; i < categories.size(); i++) {
			categoriesList = new ArrayList<String>();
			categoriesList.add(categories.get(i));
			results = bean.getArticleSearcResults(ArticleUtil.getArticleBaseFolderPath(), categoriesList, iwc);
			if (results == null) {
				break;
			}
			value = countedCategories.get(categories.get(i));
			if (value != null) {
				value = results.size();
				countedCategories.put(categories.get(i), value);
			}
		}
	}
	
	private void addCheckBox(Div container, IWContext iwc, String category, boolean setDisabled) {
		if (container == null || iwc == null) {
			return;
		}
		if (!ContentUtil.hasContentEditorRoles(iwc)) {
			return;
		}
		CheckBox box = new CheckBox(category);
		if (setDisabled) {
			box.setStyleClass(BLOG_CHECKBOX_DISABLED);
		}
		else {
			box.setStyleClass(BLOG_CHECKBOX_ENABLED);
		}
		box.setDisabled(setDisabled);
		container.getChildren().add(box);
	}

}