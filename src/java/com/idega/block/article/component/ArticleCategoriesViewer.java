package com.idega.block.article.component;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.jcr.query.QueryResult;

import com.idega.block.article.bean.ArticleListManagedBean;
import com.idega.block.article.business.ArticleConstants;
import com.idega.block.article.business.ArticleUtil;
import com.idega.content.business.ContentConstants;
import com.idega.content.business.ContentUtil;
import com.idega.content.business.categories.CategoryBean;
import com.idega.content.data.ContentCategory;
import com.idega.content.presentation.ContentItemListViewer;
import com.idega.core.builder.business.BuilderService;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.idegaweb.IWUserContext;
import com.idega.presentation.Block;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.CheckBox;
import com.idega.util.CoreConstants;
import com.idega.util.CoreUtil;
import com.idega.util.PresentationUtil;

public class ArticleCategoriesViewer extends Block {

	private final String blogLinkDisabled = "blog-category-link-disabled";
	private final String blogLinkEnabled = "blog-category-link-enabled";
	private final String opener = "(";
	private final String closer = ")";
	private String separator = "', '";

	private Map<String, Integer> countedCategories = null;
	private Map<String, List<String>> availableCategories = null;

	private Boolean hasUserValidRights = Boolean.FALSE;

	public ArticleCategoriesViewer() {
		setCacheable(getCacheKey());

		countedCategories = new HashMap<String, Integer>();
		availableCategories = new HashMap<String, List<String>>();
	}

	@Override
	public String getCacheKey() {
		return ContentConstants.ARTICLE_CATEGORIES_VIEWER_BLOCK_CACHE_KEY;
	}

	@Override
	protected String getCacheState(IWContext iwc, String cacheStatePrefix) {
		return super.getCacheState(iwc, cacheStatePrefix);
	}

	@Override
	public void main(IWContext iwc) {
		Locale currentLocale = iwc.getCurrentLocale();
		if (currentLocale == null) {
			currentLocale = Locale.ENGLISH;
		}

		Layer container = new Layer();
		add(container);

		hasUserValidRights = ContentUtil.hasContentEditorRoles(iwc);
		if (hasUserValidRights) {
			addScript(iwc, container);
		}

		CategoryBean bean = CategoryBean.getInstance();
		Collection<ContentCategory> categories = bean.getCategories(currentLocale);
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
			for (int i = 0; i < moduleIds.size(); i++) {
				markAvailableCategories(categories, pageKey, moduleIds.get(i), service);
			}
		}

		String lang = currentLocale.toString();
		Layer disabledCategoryContainer = null;
		boolean isCheckedAnyCategory = isCheckedAnyCategory();
		String categoryName = null;
		String categoryKey = null;
		String nameWithCount = null;
		Integer count = null;
		for (ContentCategory category : categories) {
			categoryKey = category.getId();
			categoryName = bean.getCategoryName(category, lang, iwc);
			count = countedCategories.get(categoryKey);
			if (count != null && categoryName != null) {
				nameWithCount = new StringBuilder(categoryName).append(ArticleConstants.SPACE).append(opener).append(count).append(closer).toString();
				if (count > 0 && !category.isDisabled()) {
					if (hasUserValidRights) {
						addLinkToCategory(pageKey, moduleIds, container, iwc, categoryKey, nameWithCount);	//	Full rights
					}
					else {
						if (isCheckedAnyCategory) {
							if (isCheckedCategory(categoryKey)) {
								addLinkToCategory(pageKey, moduleIds, container, iwc, categoryKey, nameWithCount);	// This category is visible
							}

						}
						else {
							addLinkToCategory(pageKey, moduleIds, container, iwc, categoryKey, nameWithCount);	//	All categories visible
						}
					}
				}
				else {
					if (isCheckedCategory(categoryKey) || hasUserValidRights) {
						Text text = new Text(nameWithCount);
						disabledCategoryContainer = new Layer();
						if (hasUserValidRights) {
							addCheckBox(pageKey, moduleIds, disabledCategoryContainer, iwc, categoryKey);
						}
						disabledCategoryContainer.setStyleClass(blogLinkDisabled);
						disabledCategoryContainer.add(text);
						if (hasUserValidRights || !category.isDisabled()) {
							container.add(disabledCategoryContainer);
						}
					}
				}
			}
		}
	}

	private void addScript(IWContext iwc, Layer container) {
		List<String> scripts = new ArrayList<String>();
		scripts.add("/dwr/interface/BuilderService.js");
		scripts.add(CoreConstants.DWR_ENGINE_SCRIPT);
		scripts.add(ArticleUtil.getBundle().getVirtualPathWithFileNameString("javascript/ArticleCategoriesHelper.js"));

		if (CoreUtil.isSingleComponentRenderingProcess(iwc)) {
			container.add(PresentationUtil.getJavaScriptSourceLines(scripts));
			return;
		}
		PresentationUtil.addJavaScriptSourcesLinesToHeader(iwc, scripts);
	}

	@Override
	public String getBundleIdentifier() {
		return ArticleConstants.IW_BUNDLE_IDENTIFIER;
	}

	private void countCategories(Collection<ContentCategory> categories, IWContext iwc) {
		if (categories == null) {
			return;
		}
		countedCategories = new HashMap<String, Integer>();

		ArticleListManagedBean bean = new ArticleListManagedBean();
		List<String> categoriesList = null;
		Collection<QueryResult> results = null;
		for (ContentCategory category : categories) {
			categoriesList = new ArrayList<String>();
			categoriesList.add(category.getId());
			results = bean.getArticleSearcResults(ArticleUtil.getArticleBaseFolderPath(), categoriesList, iwc);
			if (results != null) {
				countedCategories.put(category.getId(), results.size());
			}
		}
	}

	private void markAvailableCategories(Collection<ContentCategory> categories, String pageKey, String moduleId, BuilderService service) {
		if (categories == null) {
			return;
		}
		for (ContentCategory category : categories) {
			if (service.isPropertyValueSet(pageKey, moduleId, ArticleConstants.ARTICLE_CATEGORY_PROPERTY_NAME, category.getId())) {
				List<String> modules = availableCategories.get(category.getId());
				if (modules == null) {
					modules = new ArrayList<String>();
				}
				if (!modules.contains(moduleId)) {
					modules.add(moduleId);
				}
				availableCategories.put(category.getId(), modules);
			}
		}
	}

	private void addCheckBox(String pageKey, List<String> moduleIds, Layer container, IWContext iwc, String category) {
		if (!hasUserValidRights) {
			return;
		}
		if (container == null || iwc == null || moduleIds == null || pageKey == null) {
			return;
		}
		if (moduleIds.size() == 0) {
			return;
		}

		IWResourceBundle iwrb = getResourceBundle(iwc);
		CheckBox box = new CheckBox(category);

		for (int i = 0; i < moduleIds.size(); i++) {
			List<String> modules = availableCategories.get(category);
			if (modules == null) {
				box.setTitle(iwrb.getLocalizedString("set_category_visible", "Set category visible for common users"));
			}
			else {
				box.setChecked(true);
				box.setTitle(iwrb.getLocalizedString("set_category_invisible", "Set category invisible for common users"));
			}

			StringBuilder action = new StringBuilder("setDisplayArticleCategory('").append(box.getId()).append(separator).append(pageKey);
			action.append(getModulesParameter(moduleIds.get(i), modules)).append(iwrb.getLocalizedString("saving", "Saving...")).append(separator);
			action.append(ContentConstants.ARTICLE_CATEGORIES_VIEWER_BLOCK_CACHE_KEY).append("');");
			box.setOnClick(action.toString());
		}
		container.add(box);
	}

	private String getModulesParameter(String currentModuleId, List<String> otherIds) {
		StringBuilder param = new StringBuilder("', ['");

		if (otherIds == null || otherIds.size() == 0) {
			param.append(currentModuleId);
		}
		else {
			for (int i = 0; i < otherIds.size(); i++) {
				param.append(otherIds.get(i));
				if (i + 1 < otherIds.size()) {
					param.append(separator);
				}
			}
		}

		return param.append("'], '").toString();
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
		List<String> moduleIds = availableCategories.get(categoryKey);
		if (moduleIds == null || moduleIds.size() == 0) {
			return false;
		}
		return true;
	}

	private boolean isCheckedAnyCategory() {
		Collection<List<String>> checked = availableCategories.values();
		if (checked == null) {
			return false;
		}
		return checked.size() > 0;
	}

	private void addLinkToCategory(String pageKey, List<String> moduleIds, Layer container, IWContext iwc, String category, String linkValue) {
		Layer categoryContainer = new Layer();
		container.add(categoryContainer);
		addCheckBox(pageKey, moduleIds, categoryContainer, iwc, category);
		Link link = new Link(linkValue);
		link.setStyleClass(blogLinkEnabled);
		link.addFirstParameter(ContentItemListViewer.ITEMS_CATEGORY_VIEW, category);
		categoryContainer.add(link);
	}

	@Override
	public String getBuilderName(IWUserContext iwuc) {
		String name = ArticleUtil.getBundle().getComponentName(ArticleCategoriesViewer.class);
		if (name == null || ArticleConstants.EMPTY.equals(name)){
			return "ArticleCategories";
		}
		return name;
	}

}