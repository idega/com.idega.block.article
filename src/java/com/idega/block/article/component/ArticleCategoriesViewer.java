package com.idega.block.article.component;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;

import com.idega.block.article.bean.ArticleListManagedBean;
import com.idega.block.article.business.ArticleConstants;
import com.idega.block.article.business.ArticleUtil;
import com.idega.content.business.CategoryBean;
import com.idega.content.business.ContentUtil;
import com.idega.content.data.ContentCategory;
import com.idega.content.presentation.ContentItemListViewer;
import com.idega.core.builder.business.BuilderService;
import com.idega.idegaweb.IWUserContext;
import com.idega.presentation.Block;
import com.idega.presentation.IWContext;
import com.idega.presentation.Page;
import com.idega.presentation.PresentationObjectUtil;
import com.idega.presentation.text.Break;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.CheckBox;
import com.idega.webface.WFDivision;

public class ArticleCategoriesViewer extends Block {
	
	private static final String BLOG_CATEGORIES = "blog-categories";
	private static final String BLOG_LINK_DISABLED = "blog-category-link-disabled";
	private static final String BLOG_LINK_ENABLED = "blog-category-link-enabled";
	private static final String BLOG_CHECKBOX_DISABLED = "blog-category-checkbox-disabled";
	private static final String BLOG_CHECKBOX_ENABLED = "blog-category-checkbox-enabled";
	private static final String OPENER = "(";
	private static final String CLOSER = ")";
	
	private Map<String, Integer> countedCategories = null;
	private Map<String, Boolean> availableCategories = null;
	
	private Boolean isFirstTime = Boolean.TRUE;
	private Boolean hasUserValidRights = Boolean.FALSE;
	
	public ArticleCategoriesViewer() {
		countedCategories = new HashMap<String, Integer>();
		availableCategories = new HashMap<String, Boolean>();
	}
	
	@SuppressWarnings("unchecked")
	public void main(IWContext iwc) {
		hasUserValidRights = ContentUtil.hasContentEditorRoles(iwc);
		
		CategoryBean categoriesBean = CategoryBean.getInstance();
		Collection<ContentCategory> categories = categoriesBean.getCategories(iwc.getCurrentLocale());

		countCategories(categories, iwc);
		
		String pageKey = getThisPageKey(iwc);
		BuilderService service = null;
		try {
			service = getBuilderService(iwc);
		} catch (RemoteException e) {
			e.printStackTrace();
			return;
		}
		List<String> moduleIds = service.getModuleId(pageKey, ArticleListViewer.class.getName());
		if (moduleIds != null) {
			if (moduleIds.size() > 0) {
				markAvailableCategories(categories, pageKey, moduleIds.get(0), service);
			}
		}
		
		String lang = iwc.getCurrentLocale().toString();
		
		WFDivision container = new WFDivision();
		container.setId(BLOG_CATEGORIES);
		WFDivision disabledCategoryContainer = null;
		
		boolean isCheckedAnyCategory = isCheckedAnyCategory();
		for (ContentCategory category : categories) {
			String categoryKey = category.getId();
			Integer count = countedCategories.get(categoryKey);
			if (count == null) {
				break;
			}
			StringBuffer value = new StringBuffer();
			value.append(category.getName(lang)).append(ArticleConstants.SPACE).append(OPENER).append(count).append(CLOSER);
			String nameWithCount = value.toString();
			if (count > 0) {
				if (hasUserValidRights) {
					addLinkToCategory(pageKey, moduleIds, container, iwc, categoryKey, false, nameWithCount);
				}
				else {
					if (isCheckedAnyCategory) {
						if (isCheckedCategory(categoryKey)) {
							addLinkToCategory(pageKey, moduleIds, container, iwc, categoryKey, false, nameWithCount);
						}

					}
					else {
						addLinkToCategory(pageKey, moduleIds, container, iwc, categoryKey, false, nameWithCount);
					}
				}
			}
			else {
				if (hasUserValidRights) {
					Text text = new Text(nameWithCount);
					disabledCategoryContainer = new WFDivision();
					addCheckBox(pageKey, moduleIds, disabledCategoryContainer, iwc, categoryKey, true);
					disabledCategoryContainer.setStyleClass(BLOG_LINK_DISABLED);
					disabledCategoryContainer.add(text);
					container.add(disabledCategoryContainer);
				}
			}
		}
		this.add(container);
	}

	@SuppressWarnings("unchecked")
	public void restoreState(FacesContext context, Object state) {
		Object values[] = (Object[]) state;
		super.restoreState(context, values[0]);
		this.countedCategories = (Map<String, Integer>) values[1];
		this.isFirstTime = (Boolean) values[2];
		this.hasUserValidRights = (Boolean) values[3];
		this.availableCategories = (Map<String, Boolean>) values[4];
	}

	public Object saveState(FacesContext context) {
		Object values[] = new Object[5];
		values[0] = super.saveState(context);
		values[1] = this.countedCategories;
		values[2] = this.isFirstTime;
		values[3] = this.hasUserValidRights;
		values[4] = this.availableCategories;
		return values;
	}
	
	private void countCategories(Collection<ContentCategory> categories, IWContext iwc) {
		if (categories == null) {
			return;
		}
		countedCategories = new HashMap<String, Integer>();

		ArticleListManagedBean bean = new ArticleListManagedBean();
		List<String> categoriesList = null;
		Collection results = null;
		Integer value = null;
		for (ContentCategory category : categories) {
			categoriesList = new ArrayList<String>();
			categoriesList.add(category.getId());
			results = bean.getArticleSearcResults(ArticleUtil.getArticleBaseFolderPath(), categoriesList, iwc);
			if (results == null) {
				break;
			}
			value = results.size();
			countedCategories.put(category.getId(), value);
		}
	}
	
	private void markAvailableCategories(Collection<ContentCategory> categories, String pageKey, String moduleId, BuilderService service) {
		if (categories == null) {
			return;
		}
		for (ContentCategory category : categories) {
			boolean categoriesSet = service.isPropertyValueSet(pageKey, moduleId, "categories", category.getId());
			availableCategories.put(category.getId(), Boolean.valueOf(categoriesSet));
		}
	}
	
	private void addCheckBox(String pageKey, List<String> moduleIds, WFDivision container, IWContext iwc, String category, boolean setDisabled) {
		if (container == null || iwc == null) {
			return;
		}
		if (!hasUserValidRights) {
			return;
		}
		addDwrScript();
		CheckBox box = new CheckBox(category);
		if (setDisabled) {
			box.setStyleClass(BLOG_CHECKBOX_DISABLED);
		}
		else {
			box.setStyleClass(BLOG_CHECKBOX_ENABLED);
		}
		box.setDisabled(setDisabled);
		
		if (pageKey != null && moduleIds != null) {
			if (moduleIds.size() > 0) {
				Boolean enabled = availableCategories.get(category);
				if (enabled != null) {
					box.setChecked(enabled.booleanValue());
				}
				
				String separator = "', '";
				StringBuffer action = new StringBuffer();
				action.append("setDisplayArticleCategory(this, '").append(pageKey).append(separator).append(moduleIds.get(0));
				action.append(separator).append(ArticleUtil.getBundle().getLocalizedString("saving")).append("')");
				box.setOnClick(action.toString());
			}
		}
		container.add(box);
	}
	
	private void addDwrScript() {
		if (!isFirstTime) {
			return;
		}
		Page parent = PresentationObjectUtil.getParentPage(this);
		if (parent == null) {
			return;
		}
		isFirstTime = false;
		parent.addJavascriptURL("/dwr/interface/BuilderService.js");
		parent.addJavascriptURL("/dwr/engine.js");
		parent.addJavascriptURL(ArticleUtil.getBundle().getResourcesPath() + "/javascript/ArticleCategoriesHelper.js");
	}
	
	private String getThisPageKey(IWContext iwc) {
		if (iwc == null) {
			return null;
		}
		int id = iwc.getCurrentIBPageID();
		String pageKey = null;
		try {
			pageKey = String.valueOf(id);
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return null;
		}
		return pageKey;
	}
	
	private boolean isCheckedCategory(String categoryKey) {
		Boolean enabledCategoryForCommonUser = availableCategories.get(categoryKey);
		if (enabledCategoryForCommonUser != null) {
			if (enabledCategoryForCommonUser.booleanValue()) {
				return true;
			}
		}
		return false;
	}
	
	private boolean isCheckedAnyCategory() {
		return availableCategories.containsValue(Boolean.TRUE);
	}
	
	private void addLinkToCategory(String pageKey, List<String> moduleIds, WFDivision container, IWContext iwc, String category, boolean setDisabled, String linkValue) {
		addCheckBox(pageKey, moduleIds, container, iwc, category, setDisabled);
		Link link = new Link(linkValue);
		link.setStyleClass(BLOG_LINK_ENABLED);
		link.addFirstParameter(ContentItemListViewer.ITEMS_CATEGORY_VIEW, category);
		container.add(link);
		container.add(new Break());
	}
	
	public String getBuilderName(IWUserContext iwuc) {
		String name = ArticleUtil.getBundle().getComponentName(ArticleCategoriesViewer.class);
		if (name == null || ArticleConstants.EMPTY.equals(name)){
			return "ArticleCategories";
		}
		return name;
	}

}