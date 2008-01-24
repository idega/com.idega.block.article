/*
 * $Id: EditArticleView.java,v 1.35 2008/01/24 11:42:51 valdas Exp $
 *
 * Copyright (C) 2004 Idega. All Rights Reserved.
 *
 * This software is the proprietary information of Idega.
 * Use is subject to license terms.
 */
package com.idega.block.article.component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

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
import javax.faces.el.ValueBinding;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import javax.faces.event.ValueChangeEvent;
import javax.faces.event.ValueChangeListener;
import javax.faces.model.SelectItem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.idega.block.article.IWBundleStarter;
import com.idega.block.article.bean.ArticleItemBean;
import com.idega.block.article.bean.ArticleStoreException;
import com.idega.block.article.business.ArticleConstants;
import com.idega.block.article.business.ArticleUtil;
import com.idega.content.bean.ManagedContentBeans;
import com.idega.content.business.ContentConstants;
import com.idega.content.data.ContentItemCase;
import com.idega.content.presentation.ContentItemToolbar;
import com.idega.content.presentation.ContentViewer;
import com.idega.content.presentation.WebDAVCategories;
import com.idega.core.localisation.business.ICLocaleBusiness;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.IWBaseComponent;
import com.idega.presentation.IWContext;
import com.idega.presentation.Script;
import com.idega.presentation.text.Paragraph;
import com.idega.presentation.ui.FieldSet;
import com.idega.presentation.ui.HiddenInput;
import com.idega.user.data.User;
import com.idega.util.CoreConstants;
import com.idega.util.CoreUtil;
import com.idega.util.LocaleUtil;
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
 * Last modified: $Date: 2008/01/24 11:42:51 $ by $Author: valdas $
 *
 * @author Joakim,Tryggvi Larusson
 * @version $Revision: 1.35 $
 */
public class EditArticleView extends IWBaseComponent implements ManagedContentBeans, ActionListener, ValueChangeListener {
	private static final Log log = LogFactory.getLog(EditArticleView.class);
	
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
	
	boolean clearOnInit = false;
	private boolean fromArticleItemListViewer = false;
	private boolean needsForm = false;

	public EditArticleView() {}

	@SuppressWarnings("unchecked")
	protected void initializeComponent(FacesContext context) {
		IWContext iwc = IWContext.getIWContext(context);

		UIComponent managementComponent = null;
		if (isInCreateMode() || isInEditMode()) {
			managementComponent = getEditContainer(iwc);
		}
		else if (isInDeleteMode()) {
			managementComponent = getDeleteContainer();
		}
		this.setId(EDIT_ARTICLE_BLOCK_ID);
		
		if (needsForm) {
			//	Form
			HtmlForm f = new HtmlForm();
			f.getChildren().add(managementComponent);
			add(f);
			
			f.getChildren().add(new HiddenInput(ContentViewer.PARAMETER_CONTENT_RESOURCE, resourcePath));
			f.getChildren().add(new HiddenInput(ContentViewer.PARAMETER_ACTION, editMode));
			f.getChildren().add(new HiddenInput(ContentViewer.CONTENT_VIEWER_EDITOR_NEEDS_FORM, Boolean.TRUE.toString()));
			if (baseFolderPath != null) {
				f.getChildren().add(new HiddenInput(ContentItemToolbar.PARAMETER_BASE_FOLDER_PATH, baseFolderPath));
			}
			
			//	JavaScript
			Script script = new Script();
			script.addScriptLine("function addActionAfterArticleIsSavedAndEditorClosed() {window.parent.addActionAfterArticleIsSavedAndEditorClosed();}");
			f.getChildren().add(script);
			
			//	Save state for bean
			f.getChildren().add(ArticleUtil.getBeanSaveState(ARTICLE_ITEM_BEAN_ID));
		}
		else {
			add(managementComponent);
		}
		
		//	JavaScript
		Script closeLoadingMessages = new Script();
		closeLoadingMessages.addScriptLine("try {window.parent.closeAllLoadingMessages();} catch(e) {}");
		managementComponent.getChildren().add(closeLoadingMessages);
		
		Script checkFieldsIfNotEmpty = new Script();
		StringBuffer checkAction = new StringBuffer("function checkIfValidArticleEditorFields(ids, message) {\n");
			checkAction.append("if (ids == null || message == null) return true;\n");
			checkAction.append("for (var i = 0; i < ids.length; i++) {\n");
			checkAction.append("var input = document.getElementById(ids[i]);\n");
				checkAction.append("if (input != null) {\n");
				checkAction.append("if (input.value == null || input.value == '') {alert(message); return false;}");
				checkAction.append("}\n");
			checkAction.append("}\n");
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
	@SuppressWarnings("unchecked")
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
	
	protected WFContainer getMainContainer(){
		WFContainer mainContainer = new WFContainer();
		mainContainer.setId(ARTICLE_EDITOR_ID);
		
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
	@SuppressWarnings("unchecked")
	public UIComponent getEditContainer(IWContext iwc) {
		WFResourceUtil localizer = WFResourceUtil.getResourceUtilArticle();
		WFContainer mainContainer = getMainContainer();
		
		//	Languages menu
		UIComponent langDropdown = getLanguageDropdownMenu(iwc);
		UIComponent languageText = WFUtil.group(localizer.getTextVB("language"), WFUtil.getText(":"));
		HtmlOutputLabel languageLabel = new HtmlOutputLabel();
		languageLabel.getChildren().add(languageText);
		languageLabel.setFor(langDropdown.getClientId(iwc));
		
		WFFormItem languageItem = new WFFormItem();
		languageItem.add(languageLabel);
		languageItem.add(langDropdown);
		mainContainer.add(languageItem);
		
		//	Headline input
		HtmlInputText headlineInput = WFUtil.getInputText(HEADLINE_ID, ref + "headline");
		headlineInput.setSize(70);
		headlineInput.setImmediate(true);
		headlineInput.addValueChangeListener(this);
		UIComponent headlineText = WFUtil.group(localizer.getTextVB("headline"), WFUtil.getText(":"));
		HtmlOutputLabel headlineLabel = new HtmlOutputLabel();
		headlineLabel.getChildren().add(headlineText);
		headlineLabel.setFor(headlineInput.getClientId(iwc));
		
		WFFormItem headlineItem = new WFFormItem();
		headlineItem.add(headlineLabel);
		headlineItem.add(headlineInput);
		mainContainer.add(headlineItem);
		
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
			UIComponent authorText = WFUtil.group(localizer.getTextVB("author"), WFUtil.getText(":"));
			HtmlOutputLabel authorLabel = new HtmlOutputLabel();
			authorLabel.getChildren().add(authorText);
			authorLabel.setFor(authorInput.getClientId(iwc));
			
			WFFormItem authorItem = new WFFormItem();
			authorItem.add(authorLabel);
			authorItem.add(authorInput);
			mainContainer.add(authorItem);
		}

		//	Article body
		HTMLArea bodyArea = WFUtil.getHtmlAreaTextArea(BODY_ID, ref + "body", "640px", "480px", needsForm);
		bodyArea.addValueChangeListener(this);
		bodyArea.setImmediate(true);
		
		bodyArea.setAllowFontSelection(false);
		bodyArea.addPlugin(HTMLArea.PLUGIN_TABLE_OPERATIONS,"3");
		bodyArea.addPlugin(HTMLArea.PLUGIN_CONTEXT_MENU);
		bodyArea.addPlugin(HTMLArea.PLUGIN_FULL_SCREEN);
		bodyArea.addPlugin(HTMLArea.PLUGIN_STYLIST);
		bodyArea.addPlugin("DoubleClick");
		
		UIComponent bodyText = WFUtil.group(localizer.getTextVB("body"), WFUtil.getText(":"));
		HtmlOutputLabel bodyLabel = new HtmlOutputLabel();
		bodyLabel.getChildren().add(bodyText);
		bodyLabel.setFor(bodyArea.getClientId(iwc));
		
		WFFormItem bodyItem = new WFFormItem();
		bodyItem.add(bodyLabel);
		bodyItem.add(bodyArea);
		mainContainer.add(bodyItem);
		
		if (fromArticleItemListViewer) {
			//	Teaser input
			HTMLArea teaserArea = WFUtil.getHtmlAreaTextArea(TEASER_ID, ref + "teaser", "640px", "150px");
			teaserArea.addValueChangeListener(this);
			teaserArea.setImmediate(true);
			teaserArea.setAllowFontSelection(false);
			UIComponent teaserText = WFUtil.group(localizer.getTextVB("teaser"), WFUtil.getText(":"));
			HtmlOutputLabel teaserLabel = new HtmlOutputLabel();
			teaserLabel.getChildren().add(teaserText);
			teaserLabel.setFor(teaserArea.getClientId(iwc));
			
			WFFormItem teaserItem = new WFFormItem();
			teaserItem.add(teaserLabel);
			teaserItem.add(teaserArea);
			mainContainer.add(teaserItem);
		
			//Source input
			HtmlInputText sourceInput = WFUtil.getInputText(SOURCE_ID, ref + "source");
			UIComponent sourceText = WFUtil.group(localizer.getTextVB("source"), WFUtil.getText(":"));
			HtmlOutputLabel sourceLabel = new HtmlOutputLabel();
			sourceLabel.getChildren().add(sourceText);
			sourceLabel.setFor(sourceInput.getClientId(iwc));
			
			WFFormItem sourceItem = new WFFormItem();
			sourceItem.add(sourceLabel);
			sourceItem.add(sourceInput);
			mainContainer.add(sourceItem);
		}
		

		//Status field
		/*HtmlOutputText statusValue = WFUtil.getTextVB(ref + "status");
		WFContainer statusContainer = new WFContainer();
		statusContainer.add(statusValue);
		UIComponent statusText = WFUtil.group(localizer.getTextVB("status"), WFUtil.getText(":"));
		HtmlOutputLabel statusLabel = new HtmlOutputLabel();
		statusLabel.getChildren().add(statusText);
		statusLabel.setFor(statusValue.getClientId(context));
		
		WFFormItem statusItem = new WFFormItem();
		statusItem.add(statusLabel);
		statusItem.add(statusContainer);
		mainContainer.add(statusItem);*/
		
		//Version field
		/*HtmlOutputText versionValue = WFUtil.getTextVB(ref + "versionName");
		WFContainer versionContainer = new WFContainer();
		versionContainer.add(versionValue);
		UIComponent versionText = WFUtil.group(localizer.getTextVB("current_version"), WFUtil.getText(":"));
		HtmlOutputLabel versionLabel = new HtmlOutputLabel();
		versionLabel.getChildren().add(versionText);
		versionLabel.setFor(versionValue.getClientId(context));
		
		WFFormItem versionItem = new WFFormItem();
		versionItem.add(versionLabel);
		versionItem.add(versionContainer);
		mainContainer.add(versionItem);*/
		
		if (fromArticleItemListViewer) {
			//	Comment input
			HtmlInputTextarea commentArea = WFUtil.getTextArea(COMMENT_ID, ref + "comment", "500px", "60px");
			UIComponent commentText = WFUtil.group(localizer.getTextVB("comment"), WFUtil.getText(":"));
			HtmlOutputLabel commentLabel = new HtmlOutputLabel();
			commentLabel.getChildren().add(commentText);
			commentLabel.setFor(commentArea.getClientId(iwc));
			
			WFFormItem commentItem = new WFFormItem();
			commentItem.add(commentLabel);
			commentItem.add(commentArea);
			mainContainer.add(commentItem);

			//	Published date
			WFDateInput publishedInput = WFUtil.getDateInput(PUBLISHED_DATE_ID, ref + "publishedDate");
			publishedInput.setShowTime(true);
			UIComponent publishedText = WFUtil.group(localizer.getTextVB("publishing_date"), WFUtil.getText(":"));
			HtmlOutputLabel publishedLabel = new HtmlOutputLabel();
			publishedLabel.getChildren().add(publishedText);
	
			WFFormItem publisheDateItem = new WFFormItem();
			publisheDateItem.add(publishedLabel);
			publisheDateItem.add(publishedInput);
			mainContainer.add(publisheDateItem);
		}
		
		//	Categories input
		WebDAVCategories categoriesContainer = getCategoryEditor(iwc);
		UIComponent categoriesText = WFUtil.group(localizer.getTextVB("categories"), WFUtil.getText(":"));
		HtmlOutputLabel categoriesLabel = new HtmlOutputLabel();
		categoriesLabel.getChildren().add(categoriesText);
		categoriesLabel.setFor(categoriesContainer.getClientId(iwc));
		
		WFFormItem categoriesItem = new WFFormItem();
		categoriesItem.setId(editArticleCategoriesSelectionBlockId);
		categoriesItem.add(categoriesLabel);
		categoriesItem.add(categoriesContainer);
		if (!categoriesContainer.isNeedDisplayCategoriesSelection(iwc)) {
			categoriesItem.setStyleAttribute("display", "none");
		}
		mainContainer.add(categoriesItem);

		//	Button
		FieldSet buttons = new FieldSet();
		buttons.setStyleClass("buttons");
		HtmlCommandButton saveButton = localizer.getButtonVB(SAVE_ID, "save", this);
		buttons.getChildren().add(saveButton);
		if (needsForm) {
			IWResourceBundle iwrb = iwc.getIWMainApplication().getBundle(ArticleConstants.IW_BUNDLE_IDENTIFIER).getResourceBundle(iwc);
			StringBuffer action = new StringBuffer("if (checkIfValidArticleEditorFields(['").append(headlineInput.getId()).append("'], '");
			action.append(iwrb.getLocalizedString("error_headline_empty", "Heading must be entered.")).append("')) {");
			action.append("window.parent.showLoadingMessage('").append(iwrb.getLocalizedString("saving", "Saving...")).append("');");
			action.append(" addActionAfterArticleIsSavedAndEditorClosed();} else {return false;}");
			saveButton.setOnclick(action.toString());
		}
		mainContainer.getChildren().add(buttons);
		
		return mainContainer;
	}

	/**
	 * <p>
	 * </p>
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private UIComponent getLanguageDropdownMenu(IWContext iwc) {
		Iterator<Locale> iter = ICLocaleBusiness.getListOfLocalesJAVA().iterator();
		HtmlSelectOneMenu langDropdown = new HtmlSelectOneMenu();
		langDropdown.setId(LOCALE_ID);
		List<SelectItem> arrayList = new ArrayList<SelectItem>();
		while(iter.hasNext()) {
			Locale locale = iter.next();
			String keyStr = locale.getLanguage();
			String langStr = locale.getDisplayLanguage();
			SelectItem itemTemp = new SelectItem(keyStr, langStr, keyStr, false);
			arrayList.add(itemTemp);
		}
		
		UISelectItems items = new UISelectItems();
		items.setValue(arrayList);
		langDropdown.getChildren().add(items);
		ValueBinding vb = WFUtil.createValueBinding("#{" + ref +"language" + "}");
		langDropdown.setValueBinding("value", vb);
		langDropdown.getValue();
		langDropdown.addValueChangeListener(this);
		langDropdown.setImmediate(true);
		StringBuffer action = new StringBuffer();;
		if (needsForm) {
			IWResourceBundle iwrb = iwc.getIWMainApplication().getBundle(ArticleConstants.IW_BUNDLE_IDENTIFIER).getResourceBundle(iwc);
			action.append("window.parent.showLoadingMessage('").append(iwrb.getLocalizedString("loading", "Loading...")).append("'); ");
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
			//	Id on the component is set implicitly
			categoriesUI.setId(CATEGORY_EDITOR_ID);
			
			String setCategories = (String) iwc.getExternalContext().getRequestParameterMap().get(ContentItemToolbar.PARAMETER_CATEGORIES);
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
	
	private UIComponent findEditArticleComponent(UIComponent comp, Class<?> classToSearch, String id) {
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
	
	@SuppressWarnings("unchecked")
	private UIComponent findEditArticleComponent(List children, Class<?> classToSearch, String id, boolean checkChildren) {
		if (children == null) {
			return null;
		}
		
		Object o = null;
		UIComponent founded = null;
		for (int i = 0; (i < children.size() && founded == null); i++) {
			o = children.get(i);
			if (o instanceof UIComponent) {
				founded = (UIComponent) o;
				if (founded.getClass().equals(classToSearch) && id.equals(founded.getId())) {
					return founded;
				}
				else {
					founded = null;
					if (checkChildren) {
						founded = findEditArticleComponent(((UIComponent) o).getChildren(), classToSearch, id, checkChildren);
						if (founded != null) {
							return founded;
						}
					}
				}
			}
		}
		
		return null;
	}
	
	/**
	 * javax.faces.event.ActionListener#processAction()
	 */
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
			founded = editArticle.findComponent(CATEGORY_EDITOR_ID);
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
					submittedCategories = categoriesUI.getEnabledCategories();
					articleItemBean.setArticleCategories(submittedCategories);
				}
			}
			else {
				log.warn("categoriesUI == null");
			}
			saveSuccessful = editArticle.storeArticle();
			if (saveSuccessful) {
				if (categoriesUI != null) {
					try {
						IWContext iwc = CoreUtil.getIWContext();
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
						re.printStackTrace();
					}
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
	 * Stores the current article. 
	 * Returns false if save failed
	 */
	private boolean storeArticle() {
		try{
			getArticleItemBean().store();
			setEditMode(ContentConstants.CONTENT_ITEM_ACTION_EDIT);
			setUserMessage("article_saved");
			return true;
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
			e.printStackTrace();
		}
		return false;
	}
	
	private UIComponent getMessageComponent(String id) {
		UIComponent c = findComponent(SAVE_ID);;
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
			log.info("user_message component is null. Message out: " + ref);
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
	public void encodeBegin(FacesContext context) throws IOException {
		IWContext iwc = IWContext.getIWContext(context);
		resourcePath = iwc.getParameter(ContentViewer.PARAMETER_CONTENT_RESOURCE);
		baseFolderPath = iwc.getParameter(ContentItemToolbar.PARAMETER_BASE_FOLDER_PATH);
		String mode = iwc.getParameter(ContentViewer.PARAMETER_ACTION);
		if (mode instanceof String) {
			setEditMode(mode);
		}
		String needForm = iwc.getParameter(ContentViewer.CONTENT_VIEWER_EDITOR_NEEDS_FORM);
		if (needForm instanceof String) {
			needsForm = Boolean.TRUE.toString().equals(needForm);
		}
		String renderingParameter = iwc.getParameter(ContentConstants.RENDERING_COMPONENT_OF_ARTICLE_LIST);
		if (renderingParameter != null) {
			fromArticleItemListViewer = Boolean.TRUE.toString().equals(renderingParameter);
		}
		else {
			fromArticleItemListViewer = false;
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
				e.printStackTrace();
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
				e.printStackTrace();
			}
		}
	}

	/* (non-Javadoc)
	 * @see javax.faces.event.ValueChangeListener#processValueChange(javax.faces.event.ValueChangeEvent)
	 */
	public void processValueChange(ValueChangeEvent event) throws AbortProcessingException {
		if (event.getComponent().getId().equals(LOCALE_ID)) {
			if (event.getOldValue() == null) {
				return;
			}
			if (event.getNewValue() == null) {
				return;
			}
			
			log.debug("Language value has changed from "+event.getOldValue()+" to "+event.getNewValue());
			
			ArticleItemBean bean = getArticleItemBean();
			String articlePath = bean.getResourcePath();
			if (null == articlePath) {
				//Article has not been stored previously, so nothing has to be done
				return;
			}
			log.info("Article path: " + articlePath);
			String langChange = event.getNewValue().toString();
			bean.setLanguageChange(langChange);
			bean.setLocale(LocaleUtil.getLocale(langChange));
			log.info("Changed to other language "+langChange);
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
	public Object saveState(FacesContext ctx) {
		Object values[] = new Object[2];
		values[0] = super.saveState(ctx);
		values[1] = this.editMode;
		return values;
	}

	/**
	 * @see javax.faces.component.UIPanel#restoreState(javax.faces.context.FacesContext,
	 *      java.lang.Object)
	 */
	public void restoreState(FacesContext ctx, Object state) {
		Object values[] = (Object[]) state;
		super.restoreState(ctx, values[0]);
		this.editMode = (String)values[1];
		//super.restoreState(ctx,state);
	}

	/* (non-Javadoc)
	 * @see javax.faces.component.UIComponentBase#processUpdates(javax.faces.context.FacesContext)
	 */
	public void processUpdates(FacesContext context) {
		super.processUpdates(context);
	}
	
	/* (non-Javadoc)
	 * @see javax.faces.component.UIComponentBase#processUpdates(javax.faces.context.FacesContext)
	 */
	public void processValidators(FacesContext context) {
		super.processValidators(context);
	}
	
}