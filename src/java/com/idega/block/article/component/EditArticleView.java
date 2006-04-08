/*
 * $Id: EditArticleView.java,v 1.19 2006/04/08 10:47:30 laddi Exp $
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
import javax.faces.component.html.HtmlInputText;
import javax.faces.component.html.HtmlInputTextarea;
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
import com.idega.block.article.bean.ArticleItemBean;
import com.idega.block.article.bean.ArticleStoreException;
import com.idega.block.article.business.ArticleUtil;
import com.idega.content.bean.ManagedContentBeans;
import com.idega.content.data.ContentItemCase;
import com.idega.content.presentation.ContentItemToolbar;
import com.idega.content.presentation.ContentViewer;
import com.idega.content.presentation.WebDAVCategories;
import com.idega.core.localisation.business.ICLocaleBusiness;
import com.idega.presentation.IWBaseComponent;
import com.idega.presentation.IWContext;
import com.idega.presentation.text.Paragraph;
import com.idega.presentation.ui.FieldSet;
import com.idega.user.data.User;
import com.idega.webface.WFComponentSelector;
import com.idega.webface.WFContainer;
import com.idega.webface.WFFormItem;
import com.idega.webface.WFMessages;
import com.idega.webface.WFPage;
import com.idega.webface.WFResourceUtil;
import com.idega.webface.WFTabbedPane;
import com.idega.webface.WFUtil;
import com.idega.webface.htmlarea.HTMLArea;

/**
 * <p>
 * This is the part for the editor of article is inside the admin interface
 * </p>
 * Last modified: $Date: 2006/04/08 10:47:30 $ by $Author: laddi $
 *
 * @author Joakim,Tryggvi Larusson
 * @version $Revision: 1.19 $
 */
public class EditArticleView extends IWBaseComponent implements ManagedContentBeans, ActionListener, ValueChangeListener {
	public final static String EDIT_ARTICLE_BLOCK_ID = "edit_article_view";
	private final static String P = EDIT_ARTICLE_BLOCK_ID+"_"; // Id prefix
	
	public final static String EDIT_ARTICLES_BEAN_ID = "editArticlesBean";
	public final static String ref = ARTICLE_ITEM_BEAN_ID + ".";
	
//	public final static String ARTICLE_BLOCK_ID = "article_block";
	
	//public final static String TASK_ID_EDIT = P + "t_edit";
	//public final static String TASK_ID_PREVIEW = P + "t_preview";
	//public final static String TASK_ID_LIST = P + "t_list";
	//public final static String TASK_ID_DETAILS = P + "t_details";
	//public final static String TASK_ID_MESSAGES = P + "t_messages";
	
	private final static String HEADLINE_ID = P + "headline";
	private final static String LOCALE_ID = P + "locale";
	private final static String TEASER_ID = P + "teaser";
	public final static String BODY_ID = P + "body";
	//private final static String FOLDER_ID = P + "folder_location";
	private final static String AUTHOR_ID = P + "author";
	private final static String SOURCE_ID = P + "source";
	private final static String COMMENT_ID = P + "comment";
	//private final static String PUBLISHED_FROM_DATE_ID = P + "published_from_date";
	//private final static String PUBLISHED_TO_DATE_ID = P + "published_to_date";
	
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

	private static final String EDIT_MODE_CREATE = "create";
	private static final String EDIT_MODE_EDIT = "edit";
	private static final String EDIT_MODE_DELETE = "delete";

	boolean clearOnInit = false;
	private String editMode;

	public EditArticleView() {
	}

	protected void initializeComponent(FacesContext context) {
		setId(EDIT_ARTICLE_BLOCK_ID);
		/*if(clearOnInit){
			ArticleItemBean bean = getArticleItemBean();
			if(bean!=null){
				bean.clear();
			}
		}*/
//		WFUtil.invoke(EDIT_ARTICLES_BEAN_ID, "setArticleLinkListener", this, ActionListener.class);
		if(isInCreateMode()||isInEditMode()){
			add(getEditContainer());
		}
		else if (isInDeleteMode()){
			add(getDeleteContainer());
		}
	}
	
	public ArticleAdminBlock getArticleAdminBlock(){
		//return (ArticleAdminBlock) FacesContext.getCurrentInstance().getViewRoot().findComponent(ArticleAdminBlock.ARTICLE_BLOCK_ID);
		//return (ArticleAdminBlock)findComponent(ArticleAdminBlock.ARTICLE_BLOCK_ID);
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
		message.getChildren().add(confirmationText1);
		message.getChildren().add(hText);
		
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
		em.addMessageToDisplay(TEASER_ID);
//		em.addErrorMessage(PUBLISHED_FROM_DATE_ID);
//		em.addErrorMessage(PUBLISHED_TO_DATE_ID);
		em.addMessageToDisplay(SAVE_ID);
		em.addMessageToDisplay(DELETE_ID);
		
		mainContainer.add(em);
		
		return mainContainer;
		
	}
	
	/*
	 * Creates an edit container for the article.
	 */
	public UIComponent getEditContainer() {
		
		FacesContext context = FacesContext.getCurrentInstance();
		IWContext iwc = IWContext.getIWContext(context);
		WFResourceUtil localizer = WFResourceUtil.getResourceUtilArticle();
//		String bref = WFPage.CONTENT_BUNDLE + ".";

		WFContainer mainContainer = getMainContainer();
		
		//Language dropdown
		UIComponent langDropdown = getLanguageDropdownMenu();
		UIComponent languageText = WFUtil.group(localizer.getTextVB("language"), WFUtil.getText(":"));
		HtmlOutputLabel languageLabel = new HtmlOutputLabel();
		languageLabel.getChildren().add(languageText);
		languageLabel.setFor(langDropdown.getClientId(context));
		
		
		WFFormItem languageItem = new WFFormItem();
		languageItem.add(languageLabel);
		languageItem.add(langDropdown);
		mainContainer.add(languageItem);
		
		//Headline input
		HtmlInputText headlineInput = WFUtil.getInputText(HEADLINE_ID, ref + "headline");
		headlineInput.setSize(70);
		headlineInput.setImmediate(true);
		headlineInput.addValueChangeListener(this);
		UIComponent headlineText = WFUtil.group(localizer.getTextVB("headline"), WFUtil.getText(":"));
		HtmlOutputLabel headlineLabel = new HtmlOutputLabel();
		headlineLabel.getChildren().add(headlineText);
		headlineLabel.setFor(headlineInput.getClientId(context));
		
		WFFormItem headlineItem = new WFFormItem();
		headlineItem.add(headlineLabel);
		headlineItem.add(headlineInput);
		mainContainer.add(headlineItem);

		//HtmlSelectOneMenu localeMenu = WFUtil.getSelectOneMenu(LOCALE_ID, ref + "allLocales", ref + "pendingLocaleId");
		//localeMenu.setOnchange("document.forms[0].submit();");
		//p.getChildren().add(localeMenu);
		
		//Author input
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
		authorLabel.setFor(authorInput.getClientId(context));
		
		WFFormItem authorItem = new WFFormItem();
		authorItem.add(authorLabel);
		authorItem.add(authorInput);
		mainContainer.add(authorItem);
		

		//Article body
		HTMLArea bodyArea = WFUtil.getHtmlAreaTextArea(BODY_ID, ref + "body", "500px", "400px");
		bodyArea.addValueChangeListener(this);
		bodyArea.setImmediate(true);
		//HTMLArea bodyArea = WFUtil.getHtmlAreaTextArea(BODY_ID, ref + "body");
		
		bodyArea.setAllowFontSelection(false);
//		bodyArea.addPlugin(HTMLArea.PLUGIN_TABLE_OPERATIONS);
//		bodyArea.addPlugin(HTMLArea.PLUGIN_DYNAMIC_CSS, "3");
//		bodyArea.addPlugin(HTMLArea.PLUGIN_CSS, "3");
//		bodyArea.addPlugin(HTMLArea.PLUGIN_CONTEXT_MENU);
//		bodyArea.addPlugin(HTMLArea.PLUGIN_LIST_TYPE);
//		bodyArea.addPlugin(HTMLArea.PLUGIN_CHARACTER_MAP);
//		bodyArea.addPlugin("TableOperations");
//		bodyArea.addPlugin("Template");
//		bodyArea.addPlugin("Forms");
//		bodyArea.addPlugin("FormOperations");
//		bodyArea.addPlugin("EditTag");
//		bodyArea.addPlugin("Stylist");
//		bodyArea.addPlugin("CSS");
//		bodyArea.addPlugin("DynamicCSS");
//		bodyArea.addPlugin("FullPage");
//		bodyArea.addPlugin("NoteServer");
//		bodyArea.addPlugin("QuickTag");
//		bodyArea.addPlugin("InsertSmiley");
//		bodyArea.addPlugin("InsertWords");
//		bodyArea.addPlugin("ContextMenu");
//		bodyArea.addPlugin("LangMarks");
//		bodyArea.addPlugin("DoubleClick");
//		bodyArea.addPlugin("ListType");
//		bodyArea.addPlugin("ImageManager");
		
		UIComponent bodyText = WFUtil.group(localizer.getTextVB("body"), WFUtil.getText(":"));
		HtmlOutputLabel bodyLabel = new HtmlOutputLabel();
		bodyLabel.getChildren().add(bodyText);
		bodyLabel.setFor(bodyArea.getClientId(context));
		
		WFFormItem bodyItem = new WFFormItem();
		bodyItem.add(bodyLabel);
		bodyItem.add(bodyArea);
		mainContainer.add(bodyItem);
		
		//Teaser input
		HTMLArea teaserArea = WFUtil.getHtmlAreaTextArea(TEASER_ID, ref + "teaser", "500px", "150px");
		teaserArea.addValueChangeListener(this);
		teaserArea.setImmediate(true);
		teaserArea.setAllowFontSelection(false);
		UIComponent teaserText = WFUtil.group(localizer.getTextVB("teaser"), WFUtil.getText(":"));
		HtmlOutputLabel teaserLabel = new HtmlOutputLabel();
		teaserLabel.getChildren().add(teaserText);
		teaserLabel.setFor(teaserArea.getClientId(context));
		
		WFFormItem teaserItem = new WFFormItem();
		teaserItem.add(teaserLabel);
		teaserItem.add(teaserArea);
		mainContainer.add(teaserItem);
		
		//Source input
		HtmlInputText sourceInput = WFUtil.getInputText(SOURCE_ID, ref + "source");
		UIComponent sourceText = WFUtil.group(localizer.getTextVB("source"), WFUtil.getText(":"));
		HtmlOutputLabel sourceLabel = new HtmlOutputLabel();
		sourceLabel.getChildren().add(sourceText);
		sourceLabel.setFor(sourceInput.getClientId(context));
		
		WFFormItem sourceItem = new WFFormItem();
		sourceItem.add(sourceLabel);
		sourceItem.add(sourceInput);
		mainContainer.add(sourceItem);
		

		//Status field
		HtmlOutputText statusValue = WFUtil.getTextVB(ref + "status");
		WFContainer statusContainer = new WFContainer();
		statusContainer.getChildren().add(statusValue);
		UIComponent statusText = WFUtil.group(localizer.getTextVB("status"), WFUtil.getText(":"));
		HtmlOutputLabel statusLabel = new HtmlOutputLabel();
		statusLabel.getChildren().add(statusText);
		statusLabel.setFor(statusValue.getClientId(context));
		
		WFFormItem statusItem = new WFFormItem();
		statusItem.add(statusLabel);
		statusItem.add(statusContainer);
		mainContainer.add(statusItem);
		
		//Version field
		HtmlOutputText versionValue = WFUtil.getTextVB(ref + "versionName");
		WFContainer versionContainer = new WFContainer();
		versionContainer.add(versionValue);
		UIComponent versionText = WFUtil.group(localizer.getTextVB("current_version"), WFUtil.getText(":"));
		HtmlOutputLabel versionLabel = new HtmlOutputLabel();
		versionLabel.getChildren().add(versionText);
		versionLabel.setFor(versionValue.getClientId(context));
		
		WFFormItem versionItem = new WFFormItem();
		versionItem.add(versionLabel);
		versionItem.add(versionContainer);
		mainContainer.add(versionItem);
		
		//Comment input
		HtmlInputTextarea commentArea = WFUtil.getTextArea(COMMENT_ID, ref + "comment", "500px", "60px");
		UIComponent commentText = WFUtil.group(localizer.getTextVB("comment"), WFUtil.getText(":"));
		HtmlOutputLabel commentLabel = new HtmlOutputLabel();
		commentLabel.getChildren().add(commentText);
		commentLabel.setFor(commentArea.getClientId(context));
		
		WFFormItem commentItem = new WFFormItem();
		commentItem.add(commentLabel);
		commentItem.add(commentArea);
		mainContainer.add(commentItem);
		

//		WFContainer attachmentContainer = new WFContainer();
//		attachmentContainer.add(WFUtil.getButtonVB(ADD_ATTACHMENT_ID, bref + "add_attachment", this));
//		attachmentContainer.add(WFUtil.getBreak());
//		attachmentContainer.add(getAttachmentList());
//		p.getChildren().add(attachmentContainer);		
//		mainContainer.add(p);
//		p = WFPanelUtil.getFormPanel(1);
//		p.getChildren().add(WFUtil.group(WFUtil.getTextVB(bref + "related_content_items"), WFUtil.getText(":")));		
//		WFContainer contentItemContainer = new WFContainer();		
//		contentItemContainer.add(WFUtil.getButtonVB(ADD_RELATED_CONTENT_ITEM_ID, bref + "add_content_item", this));
//		contentItemContainer.add(WFUtil.getBreak());
//		contentItemContainer.add(getRelatedContentItemsList());
//		p.getChildren().add(contentItemContainer);
//		p.getChildren().add(WFUtil.group(WFUtil.getTextVB(bref + "publishing_date"), WFUtil.getText(":")));		
//		WFDateInput publishedFromInput = WFUtil.getDateInput(PUBLISHED_FROM_DATE_ID, ref + "case.publishedFromDate");
//		publishedFromInput.setShowTime(true);
//		p.getChildren().add(publishedFromInput);
//		p.getChildren().add(WFUtil.group(WFUtil.getTextVB(bref + "expiration_date"), WFUtil.getText(":")));		
//		WFDateInput publishedToInput = WFUtil.getDateInput(PUBLISHED_TO_DATE_ID, ref + "case.publishedToDate");
//		publishedToInput.setShowTime(true);
//		p.getChildren().add(publishedToInput);
//		mainContainer.add(p);
//		mainContainer.add(WFUtil.getBreak());
//		p = WFPanelUtil.getPlainFormPanel(1);
//		HtmlCommandButton editCategoriesButton = WFUtil.getButtonVB(EDIT_CATEGORIES_ID, bref + "edit_categories", this);
//		p.getChildren().add(editCategoriesButton);
//		Temporary taking away folderr location
//		p.getChildren().add(WFUtil.group(localizer.getTextVB("folder"), WFUtil.getText(":")));
//		HtmlInputText folderInput = WFUtil.getInputText(FOLDER_ID, ref + "folderLocation");
//		if(null==folderInput.getValue() || "".equals(folderInput.getValue())) {
//			String FolderString = ArticleUtil.getArticleYearMonthPath();
//		System.out.println("Folder "+FolderString);
//			folderInput.setValue(FolderString);
//		} else {
//			File file = new File(folderInput.getValue().toString());
//			folderInput.setValue(file.getParentFile().getParent());
//		}
//		folderInput.setSize(70);
//		p.getChildren().add(folderInput);		
//		p.getChildren().add(WFUtil.getBreak());
//		Categories
//		WebDAVCategories categoriesUI = new WebDAVCategories();
//		ArticleItemBean articleItemBean = (ArticleItemBean) getArticleItemBean();
//		String resourcePath = articleItemBean.getArticleResourcePath();
		
		
		//Categories input
		UIComponent categoriesContainer = getCategoryEditor();
		UIComponent categoriesText = WFUtil.group(localizer.getTextVB("categories"), WFUtil.getText(":"));
		HtmlOutputLabel categoriesLabel = new HtmlOutputLabel();
		categoriesLabel.getChildren().add(categoriesText);
		categoriesLabel.setFor(categoriesContainer.getClientId(context));
		
		WFFormItem categoriesItem = new WFFormItem();
		categoriesItem.add(categoriesLabel);
		categoriesItem.add(categoriesContainer);
		mainContainer.add(categoriesItem);
		
		
//		WFComponentSelector cs = new WFComponentSelector();
//		cs.setId(BUTTON_SELECTOR_ID);
//		cs.setDividerText(" ");
//		HtmlCommandButton saveButton = WFUtil.getButtonVB(SAVE_ID, WFPage.CONTENT_BUNDLE + "save", this);
//		cs.add(saveButton);
//		cs.add(WFUtil.getButtonVB(FOR_REVIEW_ID, bref + "for_review", this));
//		cs.add(WFUtil.getButtonVB(PUBLISH_ID, bref + "publish", this));
//		cs.add(WFUtil.getButtonVB(REWRITE_ID, bref + "rewrite", this));
//		cs.add(WFUtil.getButtonVB(REJECT_ID, bref + "reject", this));
//		cs.add(WFUtil.getButtonVB(DELETE_ID, bref + "delete", this));
//		cs.add(WFUtil.getButtonVB(CANCEL_ID, bref + "cancel", this));
//		cs.setSelectedId(CANCEL_ID, true);
//		p.getChildren().add(cs);

		FieldSet buttons = new FieldSet();
		buttons.setStyleClass("buttons");
		
		HtmlCommandButton saveButton = localizer.getButtonVB(SAVE_ID, "save", this);
		buttons.getChildren().add(saveButton);
		
		mainContainer.getChildren().add(buttons);
		
		return mainContainer;
	}

	/**
	 * <p>
	 * TODO tryggvil describe method getLanguageDropdownMenu
	 * </p>
	 * @return
	 */
	private UIComponent getLanguageDropdownMenu() {
		
		
		Iterator iter = ICLocaleBusiness.getListOfLocalesJAVA().iterator();
		HtmlSelectOneMenu langDropdown = new HtmlSelectOneMenu();
		List arrayList = new ArrayList();
		while(iter.hasNext()) {
			Locale locale = (Locale)iter.next();
			String keyStr = locale.getLanguage();
			String langStr = locale.getDisplayLanguage();
			SelectItem itemTemp = new SelectItem(keyStr, langStr, keyStr, false);
			arrayList.add(itemTemp);
		}
		
		UISelectItems items = new UISelectItems();
		items.setId(LOCALE_ID);
		items.setValue(arrayList);
		langDropdown.getChildren().add(items);
		ValueBinding vb = WFUtil.createValueBinding("#{" + ref +"language" + "}");
		langDropdown.setValueBinding("value", vb);
		langDropdown.getValue();
		langDropdown.addValueChangeListener(this);
		langDropdown.setImmediate(true);
		langDropdown.setOnchange("submit();");
		
		return langDropdown;
	}

	/*
	 * Returns container with form for editing categories.
	 */
	private UIComponent getCategoryEditor() {
		WebDAVCategories categoriesUI = (WebDAVCategories)findComponent(CATEGORY_EDITOR_ID);//WebDAVCategories.CATEGORIES_BLOCK_ID);
		if(categoriesUI==null){
			//id on the component is set implicitly
			categoriesUI=new WebDAVCategories();
			categoriesUI.setId(CATEGORY_EDITOR_ID);
			//we want to set the categories also on the parent ".article" folder:
			categoriesUI.setCategoriesOnParent(true);
			categoriesUI.setDisplaySaveButton(false);
			categoriesUI.setDisplayHeader(false);
			//categoriesUI.setId(CATEGORY_EDITOR_ID);
			
			FacesContext context = getFacesContext();
			
			String setCategories = (String) context.getExternalContext().getRequestParameterMap().get(ContentItemToolbar.PARAMETER_CATEGORIES);
			if(setCategories!=null){
				categoriesUI.setCategories(setCategories);
			}
			//Categories are set in encodeBegin:
			//if(!isInCreateMode()){
				//there is no resourcepath set for the article if it's about to be created
			//	categoriesUI.setResourcePath(resourcePath);
			//}
			//parent.getChildren().add(categoriesUI);
		}
		return categoriesUI;
	}
	
	
	/*
	 * Returns a list with realted content item links for the article.
	 *
	private UIComponent getRelatedContentItemsList() {
		String bref = WFPage.CONTENT_BUNDLE + ".";
		String var = "article_related_items";
		HtmlDataTable t = new HtmlDataTable();
		t.setVar(var);
		WFUtil.setValueBinding(t, "value", ARTICLE_ITEM_BEAN_ID + ".relatedContentItems");
		t.setColumnClasses("wf_valign_middle");
		
		UIColumn col = new UIColumn();
		HtmlOutputLink link = new HtmlOutputLink();
		WFUtil.setValueBinding(link, "tabindex", var + ".value");
		link.setOnclick("wurl='previewarticle.jsf?" + PREVIEW_ARTICLE_ITEM_ID + 
					"='+this.tabindex;window.open(wurl,'Preview','height=300,width=500,status=no,toolbar=no,menubar=no,location=no,scrollbars=yes');return false;");
		HtmlOutputText txt = new HtmlOutputText();
		WFUtil.setValueBinding(txt, "value", var + ".name");
		link.getChildren().add(txt);
		col.getChildren().add(link);
		t.getChildren().add(col);
		
		col = new UIColumn();
		HtmlCommandButton removeButton = WFUtil.getButtonVB(REMOVE_RELATED_CONTENT_ITEM_ID, bref + "remove", this);
		WFUtil.addParameterVB(removeButton, "item_id", var + ".value");
		removeButton.setValueBinding("onclick", WFUtil.createValueBinding("#{" + bref + "onclick_remove_related_content_item}"));
		WFUtil.addParameterVB(removeButton, "related_item_no", var + ".orderNoString");
		col.getChildren().add(removeButton);
		t.getChildren().add(col);
		
		return t;
	}*/
	
	/*
	 * Returns container with form for selecting related content items.
	 *
	private UIComponent getRelatedContentItemsContainer() {
		String bref = WFPage.CONTENT_BUNDLE + ".";
		WFContainer c = new WFContainer();
		c.setId(RELATED_CONTENT_ITEMS_EDITOR_ID);
		WFUtil.invoke(RELATED_ITEMS_LIST_BEAN_ID, "setCaseLinkListener", this, ActionListener.class);
		WFList l = new WFList(RELATED_ITEMS_LIST_BEAN_ID, 0, 10);
		l.setId(RELATED_CONTENT_ITEMS_LIST_ID);
		c.add(l);
		c.add(WFUtil.getBreak());
		c.add(new WFPlainOutputText("&nbsp;&nbsp;&nbsp;"));
		c.add(WFUtil.getButtonVB(RELATED_CONTENT_ITEMS_CANCEL_ID, bref + "cancel", this));
		return c;
	}*/
	
	/**
	 * javax.faces.event.ActionListener#processAction()
	 */
	public void processAction(ActionEvent event) {
		String id = event.getComponent().getId();
		UIComponent rootParent = event.getComponent().getParent().getParent().getParent();
		EditArticleView ab = (EditArticleView) rootParent.findComponent(EDIT_ARTICLE_BLOCK_ID);
		if (id.equals(SAVE_ID)) {
			//We have the save button pressed
			boolean saveSuccessful=false;
			saveSuccessful = ab.storeArticle();
			if(saveSuccessful){
				ArticleItemBean articleItemBean = getArticleItemBean();
				String fileResourcePath = articleItemBean.getLocalizedArticle().getResourcePath();
				WebDAVCategories categoriesUI = (WebDAVCategories) ab.getCategoryEditor();
				if(categoriesUI!=null){
					categoriesUI.setResourcePath(fileResourcePath);
					//WebDAVCategories.saveCategoriesSettings(fileResourcePath, categoriesUI);
					categoriesUI.saveCategoriesSettings();
				}
			}
			clearOnInit=false;
		}
		else if (id.equals(DELETE_ID)) {
			//we are deleting
			ArticleItemBean articleItemBean = getArticleItemBean();
			articleItemBean.delete();
			WFUtil.addMessageVB(ab.findComponent(DELETE_ID),ArticleUtil.IW_BUNDLE_IDENTIFIER, "delete_successful");
		}
		
/*		else if (id.equals(FOR_REVIEW_ID)) {
			WFUtil.invoke(ARTICLE_ITEM_BEAN_ID, "setRequestedStatus", ContentItemCase.STATUS_READY_FOR_REVIEW);
			ab.storeArticle();
		} else if (id.equals(PUBLISH_ID)) {
			WFUtil.invoke(ARTICLE_ITEM_BEAN_ID, "setRequestedStatus", ContentItemCase.STATUS_PUBLISHED);
			ab.storeArticle();
		} else if (id.equals(REWRITE_ID)) {
			WFUtil.invoke(ARTICLE_ITEM_BEAN_ID, "setRequestedStatus", ContentItemCase.STATUS_REWRITE);
			ab.storeArticle();
		} else if (id.equals(REJECT_ID)) {
			WFUtil.invoke(ARTICLE_ITEM_BEAN_ID, "setRequestedStatus", ContentItemCase.STATUS_DELETED);
			ab.storeArticle();
		} else if (id.equals(DELETE_ID)) {
			WFUtil.invoke(ARTICLE_ITEM_BEAN_ID, "setRequestedStatus", ContentItemCase.STATUS_DELETED);
			ab.storeArticle();
		} else if (id.equals(ADD_IMAGE_ID)) {
			ab.setEditView(FILE_UPLOAD_FORM_ID);
		} else if (id.equals(FILE_UPLOAD_CANCEL_ID)) {
			ab.setEditView(ARTICLE_EDITOR_ID);
		} else if (id.equals(FILE_UPLOAD_ID)) {
			ab.setEditView(ARTICLE_EDITOR_ID);
		} else if (id.equals(CaseListBean.CASE_ID)){
			String itemId = WFUtil.getParameter(event.getComponent(), "id");
			WFUtil.invoke(ARTICLE_ITEM_BEAN_ID, "addRelatedContentItem", new Integer(itemId));
			ab.setEditView(ARTICLE_EDITOR_ID);
		}
*/
	}

	/**
	 * Stores the current article. 
	 * Returns false if save failed
	 */
	public boolean storeArticle() {
		
		try{
			getArticleItemBean().store();
			setEditMode(EDIT_MODE_EDIT);
			setUserMessage("article_saved");
			return true;
		}
		catch(ArticleStoreException ae){
			String errorKey = ae.getErrorKey();
			WFUtil.addErrorMessageVB(findComponent(SAVE_ID),ArticleUtil.IW_BUNDLE_IDENTIFIER, errorKey);
		}
		catch(Exception e){
			String errorKey = ArticleStoreException.KEY_ERROR_ON_STORE;
			WFUtil.addErrorMessageVB(findComponent(SAVE_ID),ArticleUtil.IW_BUNDLE_IDENTIFIER, errorKey);
			//WFUtil.addErrorMessageVB(findComponent(SAVE_ID),e.getClass().getName()+" : "+e.getMessage());
			e.printStackTrace();
		}
		return false;
	}
	/*
	 * Sets the text in the message task container. 
	 */
	private void setUserMessage(String ref) {
		String bref = WFPage.CONTENT_BUNDLE + ".";
		HtmlOutputText t = (HtmlOutputText) findComponent(USER_MESSAGE_ID);
		if(t!=null){
			t.setValueBinding("value", WFUtil.createValueBinding("#{" + bref + ref + "}"));
			setMessageMode();
		}
		else{
			System.out.println("AtricleBlock: t==null: Message out: "+bref + ref);
		}
	}
	
	/**
	 * Sets this block to message mode. 
	 */
	public void setMessageMode() {
		/*WFTabbedPane tb = (WFTabbedPane)*/ findComponent(TASKBAR_ID);
		//tb.setSelectedMenuItemId(TASK_ID_MESSAGES);
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
		/*if(clearOnInit) {
			ArticleItemBean bean = getArticleItemBean();
			if(bean!=null){
				bean.clear();
			}
		}*/

		IWContext iwc = IWContext.getIWContext(context);
		String resourcePath = iwc.getParameter(ContentViewer.PARAMETER_CONTENT_RESOURCE);
		String baseFolderPath = iwc.getParameter(ContentItemToolbar.PARAMETER_BASE_FOLDER_PATH);
		
		if(resourcePath!=null){
			getArticleItemBean().setResourcePath(resourcePath);
		}
		
		if(baseFolderPath!=null){
			getArticleItemBean().setBaseFolderLocation(baseFolderPath);
		}
		
		if(isInDeleteMode()){
			//this has to happen before initializeComponent for delet case:
			try {
				getArticleItemBean().load();
			}
			catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
//		WFUtil.invoke(ARTICLE_ITEM_BEAN_ID, "updateLocale");
//		updateEditButtons();
		
		super.encodeBegin(context);

		
			if(isInCreateMode()){
			//if("create".equals(iwc.getParameter(ContentViewer.PARAMETER_ACTION))){
				//WFUtil.invoke(ARTICLE_ITEM_BEAN_ID,"setFolderLocation",resourcePath,String.class);
				//getArticleItemBean().setFolderLocation(resourcePath);

				WebDAVCategories categoriesUI = (WebDAVCategories)getCategoryEditor();
				categoriesUI.reset();
				
			} else {
				//We are in edit mode and an article already exits
				
				//WFUtil.invoke(ARTICLE_ITEM_BEAN_ID,"load",resourcePath,String.class);
				try {
					//getArticleItemBean().load(resourcePath);
					getArticleItemBean().load();

					WebDAVCategories categoriesUI = (WebDAVCategories)getCategoryEditor();
					//Update the categoriesUI with the resourcePath given:
					if(categoriesUI!=null){
						//there is no resourcepath set for the article if it's about to be created
						categoriesUI.setResourcePath(resourcePath);
					}
					
				}
				catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			
		//}
		//else{
			
		//}
//		if(((Boolean)WFUtil.invoke(ARTICLE_ITEM_BEAN_ID,"getLanguageChange")).booleanValue()) {
		//String languageChange=(String)WFUtil.invoke(ARTICLE_ITEM_BEAN_ID,"getLanguageChange");
		String languageChange = getArticleItemBean().getLanguageChange();
		if(languageChange!=null) {
			/*String articlePath = (String)WFUtil.invoke(ARTICLE_ITEM_BEAN_ID, "getArticlePath");
			if(null!=articlePath && articlePath.length()>0) {
				boolean result = ((Boolean)WFUtil.invoke(ARTICLE_ITEM_BEAN_ID, "load",articlePath+"/"+languageChange+ArticleItemBean.ARTICLE_FILE_SUFFIX)).booleanValue();
				if(!result) {
					System.out.println("Warning loading new language did not work!");
				}
			}
			WFUtil.invoke(ARTICLE_ITEM_BEAN_ID, "setLanguageChange","");
			*/
			getArticleItemBean().setLanguageChange(languageChange);
		}
	}

	/* (non-Javadoc)
	 * @see javax.faces.event.ValueChangeListener#processValueChange(javax.faces.event.ValueChangeEvent)
	 */
	public void processValueChange(ValueChangeEvent event) throws AbortProcessingException {

		
		if(event.getComponent().getId().equals(LOCALE_ID)){
			if(event.getOldValue()==null) {
				return;
			}
			if(event.getNewValue()==null) {
				return;
			}
			
			System.out.println("Language value has changed from "+event.getOldValue()+" to "+event.getNewValue());
			
			ArticleItemBean bean = getArticleItemBean();
			//String articlePath = (String)WFUtil.invoke(ARTICLE_ITEM_BEAN_ID, "getArticlePath");
			String articlePath = bean.getResourcePath();
			//String language = (String)WFUtil.invoke(ARTICLE_ITEM_BEAN_ID, "getContentLanguage");
			//String language = bean.getContentLanguage();
			if(null==articlePath) {
				//Article has not been stored previously, so nothing has to be done
				return;
			}
			System.out.println("processValueChange: Article path: "+articlePath);
			//boolean result = ((Boolean)WFUtil.invoke(ARTICLE_ITEM_BEAN_ID, "load",articlePath+"/"+arg0.getNewValue()+ArticleItemBean.ARTICLE_SUFFIX)).booleanValue();
			//System.out.println("loading other language "+result);
			//if(result) {
			//WFUtil.invoke(ARTICLE_ITEM_BEAN_ID, "setLanguageChange",arg0.getNewValue().toString());
			String langChange = event.getNewValue().toString();
			bean.setLanguageChange(langChange);
			Locale locale = new Locale(langChange);
			bean.setLocale(locale);
			System.out.println("processValueChange: changint to other language "+langChange);
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
		
		//}else {
			//if(null!=language) {
				//result = ((Boolean)WFUtil.invoke(ARTICLE_ITEM_BEAN_ID, "load",articlePath+"/"+language+ArticleItemBean.ARTICLE_SUFFIX)).booleanValue();
				//bean.setLanguageChange(language);
				//bean.setLocale(new Locale(language));
				//System.out.println("loading other language "+result);
			//}
		//}
	}
	
	/**
	 * @param mode
	 */
	public void setEditMode(String mode) {
		clearOnInit = mode.equalsIgnoreCase("create");
		this.editMode=mode;
	}
	
	public String getEditMode(){
		return editMode;
	}
	
	protected ArticleItemBean getArticleItemBean(){
		return (ArticleItemBean)WFUtil.getBeanInstance(ARTICLE_ITEM_BEAN_ID);
	}
	
	/**
	 * <p>
	 *	Returns if mode==create
	 * </p>
	 * @return
	 */
	private boolean isInCreateMode(){
		String mode = getEditMode();
		if(mode!=null){
			if(mode.equals(EDIT_MODE_CREATE)){
				return true;
			}
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
		if(mode!=null){
			if(mode.equals(EDIT_MODE_EDIT)){
				return true;
			}
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
			if(mode.equals(EDIT_MODE_DELETE)){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * @see javax.faces.component.UIPanel#saveState(javax.faces.context.FacesContext)
	 */
	public Object saveState(FacesContext ctx) {
		Object values[] = new Object[2];
		values[0] = super.saveState(ctx);
		values[1] = editMode;
		return values;
	}

	/**
	 * @see javax.faces.component.UIPanel#restoreState(javax.faces.context.FacesContext,
	 *      java.lang.Object)
	 */
	public void restoreState(FacesContext ctx, Object state) {
		Object values[] = (Object[]) state;
		super.restoreState(ctx, values[0]);
		editMode = (String)values[1];
		//super.restoreState(ctx,state);
	}

	/* (non-Javadoc)
	 * @see javax.faces.component.UIComponentBase#processUpdates(javax.faces.context.FacesContext)
	 */
	public void processUpdates(FacesContext context) {
		// TODO Auto-generated method stub
		super.processUpdates(context);
	}
	
	/* (non-Javadoc)
	 * @see javax.faces.component.UIComponentBase#processUpdates(javax.faces.context.FacesContext)
	 */
	public void processValidators(FacesContext context) {
		// TODO Auto-generated method stub
		super.processValidators(context);
	}
	
}
