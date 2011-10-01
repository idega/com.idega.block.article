/*
 * $Id: EditArticleView.java,v 1.58 2009/05/05 09:09:46 valdas Exp $
 *
 * Copyright (C) 2004 Idega. All Rights Reserved.
 *
 * This software is the proprietary information of Idega.
 * Use is subject to license terms.
 */
package com.idega.block.article.component;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.component.UIComponent;
import javax.faces.component.UISelectItems;
import javax.faces.component.html.HtmlCommandButton;
import javax.faces.component.html.HtmlForm;
import javax.faces.component.html.HtmlInputText;
import javax.faces.component.html.HtmlInputTextarea;
import javax.faces.component.html.HtmlMessage;
import javax.faces.component.html.HtmlOutputLabel;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.component.html.HtmlSelectOneMenu;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import javax.faces.event.ValueChangeEvent;
import javax.faces.event.ValueChangeListener;
import javax.faces.model.SelectItem;

import org.apache.myfaces.custom.stylesheet.Stylesheet;
import org.springframework.beans.factory.annotation.Autowired;

import com.idega.block.article.IWBundleStarter;
import com.idega.block.article.bean.ArticleItemBean;
import com.idega.block.article.bean.ArticleStoreException;
import com.idega.block.article.business.ArticleConstants;
import com.idega.block.article.business.ArticleUtil;
import com.idega.block.article.data.dao.ArticleDao;
import com.idega.business.IBOLookup;
import com.idega.content.bean.ManagedContentBeans;
import com.idega.content.business.ContentConstants;
import com.idega.content.business.WebDAVMetadataResource;
import com.idega.content.data.ContentItemCase;
import com.idega.content.presentation.ContentItemToolbar;
import com.idega.content.presentation.ContentViewer;
import com.idega.content.presentation.WebDAVCategories;
import com.idega.core.accesscontrol.business.StandardRoles;
import com.idega.core.localisation.business.ICLocaleBusiness;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.IWBaseComponent;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.Script;
import com.idega.presentation.text.Paragraph;
import com.idega.presentation.ui.FieldSet;
import com.idega.presentation.ui.HiddenInput;
import com.idega.user.data.User;
import com.idega.util.CoreConstants;
import com.idega.util.CoreUtil;
import com.idega.util.LocaleUtil;
import com.idega.util.PresentationUtil;
import com.idega.util.StringUtil;
import com.idega.util.expression.ELUtil;
import com.idega.webface.WFBlock;
import com.idega.webface.WFComponentSelector;
import com.idega.webface.WFContainer;
import com.idega.webface.WFDateInput;
import com.idega.webface.WFFormItem;
import com.idega.webface.WFMessages;
import com.idega.webface.WFResourceUtil;
import com.idega.webface.WFTabbedPane;
import com.idega.webface.WFUtil;
import com.idega.webface.htmlarea.HTMLArea;

/**
 * <p>
 * This is the part for the editor of article is inside the admin interface
 * </p>
 * Last modified: $Date: 2009/05/05 09:09:46 $ by $Author: valdas $
 *
 * @author Joakim,Tryggvi Larusson
 * @version $Revision: 1.58 $
 */
public class EditArticleView extends IWBaseComponent implements ManagedContentBeans, ActionListener, ValueChangeListener {
	private static final Logger LOGGER = Logger.getLogger(EditArticleView.class.getName());

	public final static String EDIT_ARTICLE_BLOCK_ID = "edit_article_view";
	private final static String P = EDIT_ARTICLE_BLOCK_ID+"_"; // Id prefix

	public final static String EDIT_ARTICLES_BEAN_ID = "editArticlesBean";
	public final static String ref = ARTICLE_ITEM_BEAN_ID + ".";

	private final static String HEADLINE_ID = P + "headline";
	private final static String LOCALE_ID = P + "locale";
	private final static String TEASER_ID = P + "teaser";
	public final static String BODY_ID = P + "body";
	private final static String AUTHOR_ID = P + "author";
	private final static String SOURCE_ID = P + "source";
	private final static String COMMENT_ID = P + "comment";
	private final static String PUBLISHED_DATE_ID = P + "published_date";

	private final static String USER_MESSAGE_ID = P + "user_message";

	private final static String SAVE_ID = P + "save";
	private final static String DELETE_ID = P + "delete";
	private final static String FOR_REVIEW_ID = P + "for_review";
	private final static String PUBLISH_ID = P + "publish";
	private final static String REWRITE_ID = P + "rewrite";
	private final static String REJECT_ID = P + "reject";

	private final static String TASKBAR_ID = P + "taskbar";

	private final static String BUTTON_SELECTOR_ID = P + "button_selector";
	private final static String EDITOR_SELECTOR_ID = P + "editor_selector";
	private final static String ARTICLE_EDITOR_ID = P + "article_editor";
	private final static String CATEGORY_EDITOR_ID = P + "category_editor";
	private final static String RELATED_CONTENT_ITEMS_EDITOR_ID = P + "related_items_editor";

	private String editMode;
	private String resourcePath = null;
	private String baseFolderPath = null;
	private String editArticleCategoriesSelectionBlockId = "editArticleCategoriesSelectionBlockId";
	private String containerId;

	boolean clearOnInit = false;
	private boolean fromArticleItemListViewer = false;
	private boolean needsForm = false;

	private String editorOpener = "window.parent";

	@Autowired
	ArticleDao articleDAO;

	public EditArticleView() {
		ELUtil.getInstance().autowire(this);
	}

	@Override
	protected void initializeComponent(FacesContext context) {
		IWContext iwc = IWContext.getIWContext(context);

		IWBundle iwb = getBundle(context, "com.idega.block.article");
		Stylesheet sheet = new Stylesheet();
		sheet.setPath(iwb.getResourcesPath() + "/style/article.css");
		add(sheet);

		UIComponent managementComponent = null;
		if (isInCreateMode() || isInEditMode()) {
			managementComponent = getEditContainer(iwc);
		}
		else if (isInDeleteMode()) {
			managementComponent = getDeleteContainer();
		}
		this.setId(EDIT_ARTICLE_BLOCK_ID);

		WFResourceUtil localizer = WFResourceUtil.getResourceUtilArticle();
		UIComponent titleText = null;
		if (isInCreateMode()) {
			titleText = localizer.getHeaderTextVB("create_article");
		}
		else if (isInEditMode()) {
			titleText = localizer.getHeaderTextVB("edit_article");
		}
		else if (isInDeleteMode()) {
			titleText = localizer.getHeaderTextVB("delete_article");
		}

		if (needsForm) {
			//	Form
			HtmlForm f = new HtmlForm();
			f.setStyleClass("editArticleForm");
			if (titleText != null) {
				f.getChildren().add(titleText);
			}
			f.getChildren().add(managementComponent);
			add(f);

			f.getChildren().add(new HiddenInput(ContentViewer.PARAMETER_CONTENT_RESOURCE, resourcePath));
			f.getChildren().add(new HiddenInput(ContentViewer.PARAMETER_ACTION, editMode));
			f.getChildren().add(new HiddenInput(ContentViewer.CONTENT_VIEWER_EDITOR_NEEDS_FORM, Boolean.TRUE.toString()));
			if (baseFolderPath != null) {
				f.getChildren().add(new HiddenInput(ContentItemToolbar.PARAMETER_BASE_FOLDER_PATH, baseFolderPath));
			}
			f.getChildren().add(new HiddenInput(ContentConstants.CONTENT_LIST_ITEMS_IDENTIFIER, containerId));

			//	JavaScript
			Script script = new Script();
			script.addScriptLine("function addActionAfterArticleIsSavedAndEditorClosed() {"+editorOpener+
					".ArticleEditorHelper.addActionAfterArticleIsSavedAndEditorClosed();}");
			f.getChildren().add(script);

			//	Save state for bean
			f.getChildren().add(ArticleUtil.getBeanSaveState(ARTICLE_ITEM_BEAN_ID));
		}
		else {
			add(managementComponent);
		}

		//	JavaScript
		Script closeLoadingMessages = new Script();
		closeLoadingMessages.addScriptLine("try {"+editorOpener+".closeAllLoadingMessages();} catch(e) {}");
		managementComponent.getChildren().add(closeLoadingMessages);

		Script checkFieldsIfNotEmpty = new Script();
		StringBuffer checkAction = new StringBuffer("function checkIfValidArticleEditorFields(ids, message) {\n");
			checkAction.append("return true;");
		checkAction.append("}");
		checkFieldsIfNotEmpty.addScriptLine(checkAction.toString());
		managementComponent.getChildren().add(checkFieldsIfNotEmpty);
	}

	public ArticleAdminBlock getArticleAdminBlock(){
		WFTabbedPane tabPane = (WFTabbedPane) getParent();
		return (ArticleAdminBlock) tabPane.getParent();
	}

	/*
	 * Creates an edit container for the article.
	 */
	public UIComponent getDeleteContainer() {

		getArticleAdminBlock().setMaximizedVertically(false);

		WFResourceUtil localizer = WFResourceUtil.getResourceUtilArticle();

		UIComponent mainContainer = getMainContainer();

		HtmlCommandButton deleteButton = localizer.getButtonVB(DELETE_ID, "delete", this);

		Paragraph message = new Paragraph();
		message.setStyleClass("message");
		mainContainer.getChildren().add(message);

		UIComponent confirmationText1 = localizer.getTextVB("delete_confirmation1");
		String headline = getArticleItemBean().getHeadline();
		headline = " "+headline+"?";
		HtmlOutputText hText= new HtmlOutputText();
		hText.setValue(headline);
		message.add(confirmationText1);
		message.add(hText);

		FieldSet buttonFieldSet = new FieldSet();
		buttonFieldSet.setStyleClass("buttons");
		buttonFieldSet.add(deleteButton);

		mainContainer.getChildren().add(buttonFieldSet);
		return mainContainer;

	}

	private WFContainer getMainContainer(){
		WFContainer mainContainer = new WFContainer();
		mainContainer.setId(ARTICLE_EDITOR_ID);
		mainContainer.setStyleClass(mainContainer.getStyleClass() + " " + "editArticleContainer");

		WFMessages em = new WFMessages();
		em.addMessageToDisplay(HEADLINE_ID);
		if (fromArticleItemListViewer) {
			em.addMessageToDisplay(TEASER_ID);
		}
		em.addMessageToDisplay(SAVE_ID);
		em.addMessageToDisplay(DELETE_ID);

		mainContainer.add(em);

		return mainContainer;
	}

	/*
	 * Creates an edit container for the article.
	 */
	public UIComponent getEditContainer(IWContext iwc) {
		WFResourceUtil localizer = WFResourceUtil.getResourceUtilArticle();
		WFContainer mainContainer = getMainContainer();

		WFContainer section = new WFContainer();
		section.setStyleClass("formsection");
		mainContainer.add(section);

		//	Languages menu
		UIComponent langDropdown = getLanguageDropdownMenu(iwc);
		UIComponent languageText = localizer.getTextVB("language");
		HtmlOutputLabel languageLabel = new HtmlOutputLabel();
		languageLabel.getChildren().add(languageText);
		languageLabel.setFor(langDropdown.getClientId(iwc));

		WFFormItem languageItem = new WFFormItem();
		languageItem.setStyleClass("formitem articleLanguage");
		languageItem.add(languageLabel);
		languageItem.add(langDropdown);
		section.add(languageItem);

		//	Headline input
		HtmlInputText headlineInput = WFUtil.getInputText(HEADLINE_ID, ref + "headline");
		int size = 70;
		if (needsForm) {
			size = 54;
		}
		headlineInput.setSize(size);
		headlineInput.setImmediate(true);
		headlineInput.addValueChangeListener(this);
		UIComponent headlineText = localizer.getTextVB("headline");
		HtmlOutputLabel headlineLabel = new HtmlOutputLabel();
		headlineLabel.getChildren().add(headlineText);
		headlineLabel.setFor(headlineInput.getClientId(iwc));

		WFFormItem headlineItem = new WFFormItem();
		headlineItem.setStyleClass("formitem articleHeadline");
		headlineItem.add(headlineLabel);
		headlineItem.add(headlineInput);
		section.add(headlineItem);

		if (fromArticleItemListViewer) {
			//	Author input
			HtmlInputText authorInput = WFUtil.getInputText(AUTHOR_ID, ref + "author");
			authorInput.setImmediate(true);
			authorInput.addValueChangeListener(this);
			User user = iwc.getCurrentUser();
			if(user!=null){
				String userName = user.getName();
				getArticleItemBean().setAuthor(userName);
			}
			UIComponent authorText = localizer.getTextVB("author");
			HtmlOutputLabel authorLabel = new HtmlOutputLabel();
			authorLabel.getChildren().add(authorText);
			authorLabel.setFor(authorInput.getClientId(iwc));

			WFFormItem authorItem = new WFFormItem();
			authorItem.setStyleClass("formitem articleAuthor");
			authorItem.add(authorLabel);
			authorItem.add(authorInput);
			section.add(authorItem);
		}

		String htmlAreaWidth = "640px";
		if (needsForm) {
			htmlAreaWidth = "440px";
		}
		//	Article body
		HTMLArea bodyArea = WFUtil.getHtmlAreaTextArea(BODY_ID, ref + "body", null, null, needsForm);
		bodyArea.addValueChangeListener(this);
		bodyArea.setImmediate(true);

		bodyArea.setAllowFontSelection(true);
		bodyArea.addPlugin(HTMLArea.PLUGIN_TABLE_OPERATIONS,"3");
		bodyArea.addPlugin(HTMLArea.PLUGIN_CONTEXT_MENU);
		bodyArea.addPlugin(HTMLArea.PLUGIN_FULL_SCREEN);
		bodyArea.addPlugin(HTMLArea.PLUGIN_STYLIST);
		bodyArea.addPlugin("DoubleClick");

		UIComponent bodyText = localizer.getTextVB("body");
		HtmlOutputLabel bodyLabel = new HtmlOutputLabel();
		bodyLabel.getChildren().add(bodyText);
		bodyLabel.setFor(bodyArea.getClientId(iwc));

		WFFormItem bodyItem = new WFFormItem();
		bodyItem.setStyleClass("formitem articleBody");
		bodyItem.add(bodyLabel);
		bodyItem.add(bodyArea);
		section.add(bodyItem);

		if (fromArticleItemListViewer) {
			//	Teaser input
			HTMLArea teaserArea = WFUtil.getHtmlAreaTextArea(TEASER_ID, ref + "teaser", htmlAreaWidth, "100px");
			teaserArea.addValueChangeListener(this);
			teaserArea.setImmediate(true);
			teaserArea.setAllowFontSelection(false);
			UIComponent teaserText = localizer.getTextVB("teaser");
			HtmlOutputLabel teaserLabel = new HtmlOutputLabel();
			teaserLabel.getChildren().add(teaserText);
			teaserLabel.setFor(teaserArea.getClientId(iwc));

			WFFormItem teaserItem = new WFFormItem();
			teaserItem.setStyleClass("formitem articleTeaser");
			teaserItem.add(teaserLabel);
			teaserItem.add(teaserArea);
			section.add(teaserItem);

			//Source input
			HtmlInputText sourceInput = WFUtil.getInputText(SOURCE_ID, ref + "source");
			UIComponent sourceText = localizer.getTextVB("source");
			HtmlOutputLabel sourceLabel = new HtmlOutputLabel();
			sourceLabel.getChildren().add(sourceText);
			sourceLabel.setFor(sourceInput.getClientId(iwc));

			WFFormItem sourceItem = new WFFormItem();
			sourceItem.setStyleClass("formitem articleSource");
			sourceItem.add(sourceLabel);
			sourceItem.add(sourceInput);
			section.add(sourceItem);

			//	Comment input
			HtmlInputTextarea commentArea = WFUtil.getTextArea(COMMENT_ID, ref + "comment", htmlAreaWidth, "60px");
			UIComponent commentText = localizer.getTextVB("comment");
			HtmlOutputLabel commentLabel = new HtmlOutputLabel();
			commentLabel.getChildren().add(commentText);
			commentLabel.setFor(commentArea.getClientId(iwc));

			WFFormItem commentItem = new WFFormItem();
			commentItem.setStyleClass("formitem articleComment");
			commentItem.add(commentLabel);
			commentItem.add(commentArea);
			section.add(commentItem);

			if (iwc.hasRole(StandardRoles.ROLE_KEY_EDITOR)) {
				//		Published date
				WFDateInput publishedInput = WFUtil.getDateInput(PUBLISHED_DATE_ID, ref + "publishedDate");
				publishedInput.setShowTime(true);
				UIComponent publishedText = localizer.getTextVB("publishing_date");
				HtmlOutputLabel publishedLabel = new HtmlOutputLabel();
				publishedLabel.getChildren().add(publishedText);

				WFFormItem publishDateItem = new WFFormItem();
				publishDateItem.setStyleClass("formitem articlePublishDate");
				publishDateItem.add(publishedLabel);
				publishDateItem.add(publishedInput);
				section.add(publishDateItem);
			}
		}

		//	Categories input
		WebDAVCategories categoriesContainer = getCategoryEditor(iwc);
		UIComponent categoriesText = localizer.getTextVB("categories");
		HtmlOutputLabel categoriesLabel = new HtmlOutputLabel();
		categoriesLabel.getChildren().add(categoriesText);
		categoriesLabel.setFor(categoriesContainer.getClientId(iwc));

		WFFormItem categoriesItem = new WFFormItem();
		categoriesItem.setStyleClass("formitem articleCategories");
		categoriesItem.setId(editArticleCategoriesSelectionBlockId);
		categoriesItem.add(categoriesLabel);
		categoriesItem.add(categoriesContainer);
		if (!categoriesContainer.isNeedDisplayCategoriesSelection(iwc)) {
			categoriesItem.setStyleAttribute("display", "none");
		}
		section.add(categoriesItem);

		//	Button
		FieldSet buttons = new FieldSet();
		buttons.setStyleClass("buttons");
		HtmlCommandButton saveButton = localizer.getButtonVB(SAVE_ID, "save", this);
		buttons.getChildren().add(saveButton);
		if (needsForm) {
			IWResourceBundle iwrb = iwc.getIWMainApplication().getBundle(ArticleConstants.IW_BUNDLE_IDENTIFIER).getResourceBundle(iwc);
			StringBuffer action = new StringBuffer("if (checkIfValidArticleEditorFields(['").append(headlineInput.getId()).append("'], '");
			action.append(iwrb.getLocalizedString("error_headline_empty", "Heading must be entered.")).append("')) {");
			action.append(editorOpener).append(".showLoadingMessage('").append(iwrb.getLocalizedString("saving", "Saving...")).append("');");
			action.append(" addActionAfterArticleIsSavedAndEditorClosed();} else {return false;}");
			saveButton.setOnclick(action.toString());
		}
		mainContainer.getChildren().add(buttons);

		if (needsForm) {
			WFBlock block = new WFBlock();
			block.add(mainContainer);
			return block;
		}

		return mainContainer;
	}

	/**
	 * <p>
	 * </p>
	 * @return
	 */
	private UIComponent getLanguageDropdownMenu(IWContext iwc) {
		HtmlSelectOneMenu langDropdown = new HtmlSelectOneMenu();
		langDropdown.setId(LOCALE_ID);
		List<SelectItem> arrayList = new ArrayList<SelectItem>();
		for (Locale locale: ICLocaleBusiness.getListOfLocalesJAVA()) {
			String keyStr = locale.getLanguage();
			String langStr = locale.getDisplayLanguage();
			SelectItem itemTemp = new SelectItem(keyStr, langStr, keyStr, false);
			arrayList.add(itemTemp);
		}

		UISelectItems items = new UISelectItems();
		items.setValue(arrayList);
		langDropdown.getChildren().add(items);
		langDropdown.setValueExpression("value", WFUtil.createValueExpression(iwc.getELContext(), "#{" + ref +"language" + "}", String.class));
		langDropdown.addValueChangeListener(this);
		langDropdown.setImmediate(true);
		StringBuffer action = new StringBuffer();
		if (needsForm) {
			IWResourceBundle iwrb = iwc.getIWMainApplication().getBundle(ArticleConstants.IW_BUNDLE_IDENTIFIER).getResourceBundle(iwc);
			action.append(editorOpener).append(".showLoadingMessage('").append(iwrb.getLocalizedString("loading", "Loading...")).append("'); ");
		}
		action.append("submit();");
		langDropdown.setOnchange(action.toString());

		return langDropdown;
	}

	/*
	 * Returns container with form for editing categories.
	 */
	private WebDAVCategories getCategoryEditor(IWContext iwc) {
		WebDAVCategories categoriesUI = null;
		Object o = findEditArticleComponent(this.getChildren(), WebDAVCategories.class, CATEGORY_EDITOR_ID, true);
		if (o instanceof WebDAVCategories) {
			categoriesUI = (WebDAVCategories) o;
		}
		if (categoriesUI == null) {
			Locale l = getArticleItemBean().getLocale();
			if (l == null) {
				l = iwc.getCurrentLocale();
			}
			categoriesUI = new WebDAVCategories(resourcePath, l.toString());
			categoriesUI.setSelectAnyCategory(fromArticleItemListViewer);

			//	Id on the component is set implicitly
			categoriesUI.setId(CATEGORY_EDITOR_ID);

			String setCategories = iwc.getExternalContext().getRequestParameterMap().get(ContentItemToolbar.PARAMETER_CATEGORIES);
			if (setCategories != null) {
				categoriesUI.setCategories(setCategories);
			}
		}
		else {
			categoriesUI.getSelectedAndNotSelectedCategories(iwc);
			categoriesUI.setLocaleIdentity(getArticleItemBean().getLocale().toString());
		}

		categoriesUI.setAddCategoryCreator(false);
		//	We want to set the categories also on the parent ".article" folder:
		categoriesUI.setCategoriesOnParent(true);
		categoriesUI.setDisplaySaveButton(false);
		categoriesUI.setDisplayHeader(false);

		return categoriesUI;
	}

	private UIComponent findEditArticleComponent(UIComponent comp, Class<? extends UIComponent> classToSearch, String id) {
		if (comp == null) {
			return null;
		}

		if (comp.getClass().equals(classToSearch) && id.equals(comp.getId())) {
			return comp;
		}

		Object o = comp.findComponent(id);
		if (o instanceof UIComponent) {
			UIComponent founded = (UIComponent) o;
			if (founded.getClass().equals(classToSearch) && id.equals(founded.getId())) {
				return founded;
			}
		}

		UIComponent fromChildren = findEditArticleComponent(comp.getChildren(), classToSearch, id, false);
		if (fromChildren != null) {
			return fromChildren;
		}

		return findEditArticleComponent(comp.getParent(), classToSearch, id);
	}

	private UIComponent findEditArticleComponent(List<UIComponent> children, Class<? extends UIComponent> classToSearch, String id, boolean checkChildren) {
		if (children == null) {
			return null;
		}

		UIComponent comp = null;
		UIComponent founded = null;
		for (int i = 0; (i < children.size() && founded == null); i++) {
			comp = children.get(i);
			founded = comp;
			if (founded.getClass().equals(classToSearch) && id.equals(founded.getId())) {
				return founded;
			}
			else {
				founded = null;
				if (checkChildren) {
					founded = findEditArticleComponent(comp.getChildren(), classToSearch, id, checkChildren);
					if (founded != null) {
						return founded;
					}
				}
			}
		}

		return null;
	}

	/**
	 * javax.faces.event.ActionListener#processAction()
	 */
	@Override
	public void processAction(ActionEvent event) {
		String id = event.getComponent().getId();
		UIComponent comp = event.getComponent();
		UIComponent founded = findEditArticleComponent(comp, EditArticleView.class, EDIT_ARTICLE_BLOCK_ID);
		if (founded == null) {
			throw new NullPointerException(EditArticleView.class.getName() + " is null");
		}
		EditArticleView editArticle = (EditArticleView) founded;
		founded = null;
		if (id.equals(SAVE_ID)) {
			//	We have the save button pressed

			IWContext iwc = CoreUtil.getIWContext();
			founded = iwc.getViewRoot().findComponent(CATEGORY_EDITOR_ID);
			if (!(founded instanceof WebDAVCategories)) {
				founded = findEditArticleComponent(comp, WebDAVCategories.class, CATEGORY_EDITOR_ID);
			}
			boolean saveSuccessful = false;
			String submittedCategories = null;
			ArticleItemBean articleItemBean = getArticleItemBean();
			WebDAVCategories categoriesUI = null;
			if (founded instanceof WebDAVCategories) {
				categoriesUI = (WebDAVCategories) founded;
				if (categoriesUI != null) {
					submittedCategories = categoriesUI.getEnabledCategories(iwc);
					articleItemBean.setArticleCategories(submittedCategories);
				}
			}
			else {
				LOGGER.warning("categoriesUI == null");
			}
			saveSuccessful = editArticle.storeArticle();
			if (saveSuccessful) {
				if (categoriesUI != null) {
					try {
						categoriesUI.setResourcePath(articleItemBean.getLocalizedArticle().getResourcePath());
						categoriesUI.saveCategoriesSettings();
						categoriesUI.getSelectedAndNotSelectedCategories(iwc);

						Object o = comp.findComponent(editArticleCategoriesSelectionBlockId);
						if (o instanceof WFFormItem) {
							String displayValue = "none";
							if (categoriesUI.getLocalizedCategories() > 0) {
								if (CoreConstants.COMMA.equals(submittedCategories) || categoriesUI.isNeedDisplayCategoriesSelection(iwc)) {
									displayValue = "block";
								}
							}
							((WFFormItem) o).setStyleAttribute("display", displayValue);
						}
					} catch (RuntimeException re) {
						LOGGER.log(Level.WARNING, "Error saving categories", re);
					}
				}

				comp = editArticle.findComponent("articleUpdaterScriptCaller");
				if (comp == null) {
					String action = null;
					if (fromArticleItemListViewer && !StringUtil.isEmpty(containerId)) {
						action = PresentationUtil.getJavaScriptAction(new StringBuilder(editorOpener).append(".ArticleEditorHelper.reloadArticlesList('")
								.append(containerId).append("');").toString());
					}
					else {
						String resourcePath = articleItemBean.getResourcePath();
						if (!StringUtil.isEmpty(resourcePath)) {
							if (resourcePath.startsWith(CoreConstants.WEBDAV_SERVLET_URI)) {
								resourcePath = resourcePath.replaceFirst(CoreConstants.WEBDAV_SERVLET_URI, CoreConstants.EMPTY);
							}

							String mode = null;
							if (iwc != null) {
								mode = iwc.getParameter(ContentViewer.PARAMETER_ACTION);
							}
							if (!StringUtil.isEmpty(mode)) {
								mode = new StringBuilder("'").append(mode).append("'").toString();
							}

							action = PresentationUtil.getJavaScriptAction(new StringBuilder(editorOpener).append(".ArticleEditorHelper.reloadArticle('")
							.append(resourcePath).append("', '").append(ContentConstants.CONTENT_ITEM_IDENTIFIER_STYLE_CLASS).append("', ")
							.append(StringUtil.isEmpty(mode) ? "null" : mode).append(");").toString());
						}
					}
					if (action == null) {
						action = PresentationUtil.getJavaScriptAction(new StringBuilder(editorOpener).append(".ArticleEditorHelper.needReload = true;")
																		.toString());
					}

					Layer script = new Layer();
					script.setId("articleUpdaterScriptCaller");
					script.add(action);
					editArticle.add(script);
				}
			}
			this.clearOnInit = false;
		}
		else if (id.equals(DELETE_ID)) {
			//	We are deleting
			ArticleItemBean articleItemBean = getArticleItemBean();
			articleItemBean.delete();
			WFUtil.addMessageVB(editArticle.findComponent(DELETE_ID),ArticleConstants.IW_BUNDLE_IDENTIFIER, "delete_successful");
		}
	}
	
	/**
	 * <p>Test method. Generates random article.</p>
	 * @param index Number of article created.
	 * @param iwc Application context, to find in which app it should work.
	 * @return True if generated, false otherwise.
	 */
	private boolean generateRandomArticle(int index, IWContext iwc, List<String> categorylist){
	    try {
	        ArticleItemBean article = new ArticleItemBean();
	        article.setSetPublishedDateByDefault(fromArticleItemListViewer);
	        article.getLocalizedArticle().setSetPublishedDateByDefault(fromArticleItemListViewer);
	        article.setBody("There is nothing interesting here.");
	        article.setName("Article_d" + index);
	        article.setAuthor("Martynas");
	        article.setHeadline("Article_d" + index);
	        
	        String passingCat = "";
	        for (String s : categorylist){
	            passingCat = passingCat + s + ",";  
	        }
	        
	        Random year = new Random();
	        Random month = new Random();
	        Random date = new Random();
	                
	        int randomYear = year.nextInt(30)+1970;
	        int randomMonth = month.nextInt(11)+1;
	        int randomDay = date.nextInt(25);
	        Calendar calendar = Calendar.getInstance();
	        calendar.set(randomYear, randomMonth, randomDay);
	        article.setCreationDate(new Timestamp(calendar.getTimeInMillis()));
	        article.setResourcePath("/files/cms/article/"+randomYear+"/"+randomMonth+"/"+randomYear+randomMonth+randomDay+"-151"+index+".article");        
	        article.store();
	        WebDAVMetadataResource resource = (WebDAVMetadataResource) IBOLookup.getSessionInstance(iwc, WebDAVMetadataResource.class);
            
	        resource.setCategories(article.getResourcePath(), passingCat, true);
	        
	        setEditMode(ContentConstants.CONTENT_ITEM_ACTION_EDIT);
	        System.out.println("Generated article:" + "/files/cms/article/"+randomYear+"/"+randomMonth+"/"+randomYear+randomMonth+randomDay+"-151"+index+".article");
            System.out.println("With categories: ");
            System.out.println(passingCat);
	        setUserMessage("article_saved");
	        return true;
	    } catch(Exception e){     
	        LOGGER.log(Level.WARNING, "Error storing article", e);
	    }
	        
	    return false;
	}

	/**
	 * Stores the current article.
	 * Returns false if save failed
	 */
	private boolean storeArticle() {
		try {  	    
		    
		    /*This is for testing, I mean generating articles in slide without 
		     * saving them to database */
//		    Random rn = new Random();
//		    int variable = 0;
//		    List<String> categoryList = null;
//		    for (int s=0; s<10; s++) {
//		        
//		        categoryList=new ArrayList<String>();
//		        variable = rn.nextInt(15);	            
//		        for (int k=0; k<variable; k++) {
//	                categoryList.add("Kategorija_"+k);
//	            }
//
//		        this.generateRandomArticle(s, IWContext.getInstance(), categoryList);
//		        categoryList.clear();
//		    }
		    
			ArticleItemBean article = getArticleItemBean();
			article.setSetPublishedDateByDefault(fromArticleItemListViewer);
			article.getLocalizedArticle().setSetPublishedDateByDefault(fromArticleItemListViewer);
			article.store();

			String articleURI = article.getResourcePath();
			if (articleURI.startsWith(CoreConstants.WEBDAV_SERVLET_URI)) {
				articleURI = articleURI.replaceFirst(CoreConstants.WEBDAV_SERVLET_URI, CoreConstants.EMPTY);
			}

			if (articleURI.endsWith(CoreConstants.SLASH)) {
				articleURI = articleURI.substring(0, articleURI.lastIndexOf(CoreConstants.SLASH));
			}

			List<String> categories = article.getCategories();
			boolean result = this.articleDAO.updateArticle(article.getCreationDate(), articleURI, categories);
			setEditMode(ContentConstants.CONTENT_ITEM_ACTION_EDIT);
			setUserMessage(result ? "article_saved" : "error_saving_article");
			return result;
		}
		catch(ArticleStoreException ae){
			String errorKey = ae.getErrorKey();
			UIComponent message = getMessageComponent(SAVE_ID);
			if (message == null) {
				return false;
			}
			WFUtil.addErrorMessageVB(message, ArticleConstants.IW_BUNDLE_IDENTIFIER, errorKey);
		}
		catch(Exception e){
			String errorKey = ArticleStoreException.KEY_ERROR_ON_STORE;
			UIComponent message = getMessageComponent(SAVE_ID);
			if (message == null) {
				return false;
			}
			WFUtil.addErrorMessageVB(message,ArticleConstants.IW_BUNDLE_IDENTIFIER, errorKey);

			LOGGER.log(Level.WARNING, "Error storing article", e);
		}
		return false;
	}

	private UIComponent getMessageComponent(String id) {
		UIComponent c = findComponent(SAVE_ID);
		if (c == null) {
			Object o = findEditArticleComponent(this.getChildren(), HtmlMessage.class, id + WFMessages.MESSAGE_COMPONENT_ID_ENDING, true);
			if (o instanceof HtmlMessage) {
				c = (HtmlMessage) o;
			}
			else {
				return null;
			}
		}

		return c;
	}

	/*
	 * Sets the text in the message task container.
	 */
	private void setUserMessage(String ref) {
		HtmlOutputText t = (HtmlOutputText) findComponent(USER_MESSAGE_ID);
		if(t!=null){
			WFUtil.setLocalizedValue(t, IWBundleStarter.BUNDLE_IDENTIFIER, ref);
			setMessageMode();
		}
		else{
			LOGGER.info("user_message component is null. Message out: " + ref);
		}
	}

	/**
	 * Sets this block to message mode.
	 */
	public void setMessageMode() {
		findComponent(TASKBAR_ID);
	}


	/**
	 * Sets the editor view for this article block.
	 *
	 */
	public void setEditView(String s) {
		WFComponentSelector cs = (WFComponentSelector) findComponent(EDITOR_SELECTOR_ID);
		if(cs!=null){
			cs.setSelectedId(ARTICLE_EDITOR_ID, s.equals(ARTICLE_EDITOR_ID));
			cs.setSelectedId(CATEGORY_EDITOR_ID, s.equals(CATEGORY_EDITOR_ID));
			cs.setSelectedId(RELATED_CONTENT_ITEMS_EDITOR_ID, s.equals(RELATED_CONTENT_ITEMS_EDITOR_ID));
		}
	}

	/**
	 * Updates the buttons in edit mode depending on the status of the current article.
	 */
	public void updateEditButtons() {
		WFComponentSelector cs = (WFComponentSelector) findComponent(BUTTON_SELECTOR_ID);
		String s = WFUtil.getStringValue(ARTICLE_ITEM_BEAN_ID, "status");
		if(cs!=null){
			if (s.equals(ContentItemCase.STATUS_NEW)) {
				cs.setSelectedId(SAVE_ID, false);
				cs.setSelectedId(FOR_REVIEW_ID, true);
				cs.setSelectedId(PUBLISH_ID, true);
				cs.setSelectedId(REWRITE_ID, false);
				cs.setSelectedId(REJECT_ID, false);
				cs.setSelectedId(DELETE_ID, false);
			} else if (s.equals(ContentItemCase.STATUS_READY_FOR_REVIEW)) {
				cs.setSelectedId(SAVE_ID, false);
				cs.setSelectedId(FOR_REVIEW_ID, false);
				cs.setSelectedId(PUBLISH_ID, true);
				cs.setSelectedId(REWRITE_ID, true);
				cs.setSelectedId(REJECT_ID, true);
				cs.setSelectedId(DELETE_ID, false);
			} else if (s.equals(ContentItemCase.STATUS_UNDER_REVIEW)) {
				cs.setSelectedId(SAVE_ID, false);
				cs.setSelectedId(FOR_REVIEW_ID, false);
				cs.setSelectedId(PUBLISH_ID, true);
				cs.setSelectedId(REWRITE_ID, true);
				cs.setSelectedId(REJECT_ID, true);
				cs.setSelectedId(DELETE_ID, false);
			} else if (s.equals(ContentItemCase.STATUS_REWRITE)) {
				cs.setSelectedId(SAVE_ID, false);
				cs.setSelectedId(FOR_REVIEW_ID, true);
				cs.setSelectedId(PUBLISH_ID, true);
				cs.setSelectedId(REWRITE_ID, false);
				cs.setSelectedId(REJECT_ID, false);
				cs.setSelectedId(DELETE_ID, false);
			} else if (s.equals(ContentItemCase.STATUS_PENDING_PUBLISHING)) {
				cs.setSelectedId(SAVE_ID, true);
				cs.setSelectedId(FOR_REVIEW_ID, false);
				cs.setSelectedId(PUBLISH_ID, false);
				cs.setSelectedId(REWRITE_ID, false);
				cs.setSelectedId(REJECT_ID, false);
				cs.setSelectedId(DELETE_ID, true);
			} else if (s.equals(ContentItemCase.STATUS_PUBLISHED)) {
				cs.setSelectedId(SAVE_ID, true);
				cs.setSelectedId(FOR_REVIEW_ID, false);
				cs.setSelectedId(PUBLISH_ID, false);
				cs.setSelectedId(REWRITE_ID, false);
				cs.setSelectedId(REJECT_ID, false);
				cs.setSelectedId(DELETE_ID, true);
			} else if (s.equals(ContentItemCase.STATUS_EXPIRED)) {
				cs.setSelectedId(SAVE_ID, true);
				cs.setSelectedId(FOR_REVIEW_ID, false);
				cs.setSelectedId(PUBLISH_ID, false);
				cs.setSelectedId(REWRITE_ID, false);
				cs.setSelectedId(REJECT_ID, false);
				cs.setSelectedId(DELETE_ID, true);
			} else if (s.equals(ContentItemCase.STATUS_DELETED)) {
				cs.setSelectedId(SAVE_ID, true);
				cs.setSelectedId(FOR_REVIEW_ID, true);
				cs.setSelectedId(PUBLISH_ID, true);
				cs.setSelectedId(REWRITE_ID, true);
				cs.setSelectedId(REJECT_ID, true);
				cs.setSelectedId(DELETE_ID, true);
			}
		}
	}

	/**
	 * @see javax.faces.component.UIComponent#encodeBegin(javax.faces.context.FacesContext)
	 */
	@Override
	public void encodeBegin(FacesContext context) throws IOException {
		IWContext iwc = IWContext.getIWContext(context);
		resourcePath = iwc.getParameter(ContentViewer.PARAMETER_CONTENT_RESOURCE);
		baseFolderPath = iwc.getParameter(ContentItemToolbar.PARAMETER_BASE_FOLDER_PATH);
		String mode = iwc.getParameter(ContentViewer.PARAMETER_ACTION);
		if (mode != null) {
			setEditMode(mode);
		}
		String needForm = iwc.getParameter(ContentViewer.CONTENT_VIEWER_EDITOR_NEEDS_FORM);
		if (needForm != null) {
			needsForm = Boolean.TRUE.toString().equals(needForm);
		}
		String renderingParameter = iwc.getParameter(ContentConstants.RENDERING_COMPONENT_OF_ARTICLE_LIST);
		if (renderingParameter != null) {
			fromArticleItemListViewer = Boolean.TRUE.toString().equals(renderingParameter);
		}
		else {
			fromArticleItemListViewer = false;
		}
		containerId = iwc.getParameter(ContentConstants.CONTENT_LIST_ITEMS_IDENTIFIER);
		if (containerId == null) {
			containerId = String.valueOf(-1);
		}

		ArticleItemBean article = getArticleItemBean();
		String languageChange = article.getLanguageChange();
		if (languageChange != null) {
			article.setLanguageChange(null);
		}

		if (resourcePath != null) {
			if (resourcePath.equals(CoreConstants.EMPTY)) {
				resourcePath = article.getResourcePath();
			}
			else {
				article.setResourcePath(resourcePath);
			}
		}

		if (baseFolderPath != null) {
			article.setBaseFolderLocation(baseFolderPath);
		}

		if (isInDeleteMode()) {
			//	This has to happen before initializeComponent for delete case:
			try {
				article.load();
			}
			catch (Exception e) {
				LOGGER.log(Level.WARNING, "Error loading article", e);
			}
		}

		super.encodeBegin(context);

		WebDAVCategories categoriesUI = getCategoryEditor(iwc);
		if (isInCreateMode()) {
			categoriesUI.reset();
		} else {
			//	We are in edit mode and an article already exits
			try {
				if (languageChange == null) {
					article.load();
				}
				else {
					article.reload();
				}
				//	Update the categoriesUI with the resourcePath given
				categoriesUI.setResourcePath(resourcePath);
			}
			catch (Exception e) {
				LOGGER.log(Level.WARNING, "Error loading article", e);
			}
		}
	}

	/* (non-Javadoc)
	 * @see javax.faces.event.ValueChangeListener#processValueChange(javax.faces.event.ValueChangeEvent)
	 */
	@Override
	public void processValueChange(ValueChangeEvent event) throws AbortProcessingException {
		if (event.getComponent().getId().equals(LOCALE_ID)) {
			if (event.getOldValue() == null) {
				return;
			}
			if (event.getNewValue() == null) {
				return;
			}

			LOGGER.info("Language value has changed from "+event.getOldValue()+" to "+event.getNewValue());

			ArticleItemBean bean = getArticleItemBean();
			String articlePath = bean.getResourcePath();
			if (null == articlePath) {
				//Article has not been stored previously, so nothing has to be done
				return;
			}
			LOGGER.info("Article path: " + articlePath);
			String langChange = event.getNewValue().toString();
			bean.setLanguageChange(langChange);
			bean.setLocale(LocaleUtil.getLocale(langChange));
			LOGGER.info("Changed to other language "+langChange);
		}
		else if(event.getComponent().getId().equals(BODY_ID)){
			String newBodyValue = event.getNewValue().toString();
			getArticleItemBean().setBody(newBodyValue);
		}
		else if(event.getComponent().getId().equals(HEADLINE_ID)){
			String newValue = event.getNewValue().toString();
			getArticleItemBean().setHeadline(newValue);
		}
		else if(event.getComponent().getId().equals(AUTHOR_ID)){
			String newValue = event.getNewValue().toString();
			getArticleItemBean().setAuthor(newValue);
		}
		else if(event.getComponent().getId().equals(TEASER_ID)){
			String newValue = event.getNewValue().toString();
			getArticleItemBean().setTeaser(newValue);
		}
	}

	/**
	 * @param mode
	 */
	public void setEditMode(String mode) {
		this.clearOnInit = mode.equalsIgnoreCase(ContentConstants.CONTENT_ITEM_ACTION_CREATE);
		this.editMode=mode;
	}

	public String getEditMode(){
		return this.editMode;
	}

	private ArticleItemBean getArticleItemBean() {
		return (ArticleItemBean) WFUtil.getBeanInstance(ARTICLE_ITEM_BEAN_ID);
	}

	/**
	 * <p>
	 *	Returns if mode==create
	 * </p>
	 * @return
	 */
	private boolean isInCreateMode(){
		String mode = getEditMode();
		if (mode != null) {
			return mode.equals(ContentConstants.CONTENT_ITEM_ACTION_CREATE);
		}
		return false;
	}

	/**
	 * <p>
	 *	Returns if mode==edit
	 * </p>
	 * @return
	 */
	private boolean isInEditMode(){
		String mode = getEditMode();
		if (mode!=null) {
			return mode.equals(ContentConstants.CONTENT_ITEM_ACTION_EDIT);
		}
		return false;
	}

	/**
	 * <p>
	 *	Returns if mode==delete
	 * </p>
	 * @return
	 */
	private boolean isInDeleteMode(){
		String mode = getEditMode();
		if(mode!=null){
			return mode.equals(ContentConstants.CONTENT_ITEM_ACTION_DELETE);
		}
		return false;
	}

	/**
	 * @see javax.faces.component.UIPanel#saveState(javax.faces.context.FacesContext)
	 */
	@Override
	public Object saveState(FacesContext ctx) {
		Object values[] = new Object[4];
		values[0] = super.saveState(ctx);
		values[1] = this.editMode;
		values[2] = this.fromArticleItemListViewer;
		values[3] = this.containerId;
		return values;
	}

	/**
	 * @see javax.faces.component.UIPanel#restoreState(javax.faces.context.FacesContext,
	 *      java.lang.Object)
	 */
	@Override
	public void restoreState(FacesContext ctx, Object state) {
		Object values[] = (Object[]) state;
		super.restoreState(ctx, values[0]);
		this.editMode = (String)values[1];
		this.fromArticleItemListViewer = Boolean.valueOf(values[2].toString());
		this.containerId = values[3].toString();
		//super.restoreState(ctx,state);
	}

	/* (non-Javadoc)
	 * @see javax.faces.component.UIComponentBase#processUpdates(javax.faces.context.FacesContext)
	 */
	@Override
	public void processUpdates(FacesContext context) {
		super.processUpdates(context);
	}

	/* (non-Javadoc)
	 * @see javax.faces.component.UIComponentBase#processUpdates(javax.faces.context.FacesContext)
	 */
	@Override
	public void processValidators(FacesContext context) {
		super.processValidators(context);
	}

}