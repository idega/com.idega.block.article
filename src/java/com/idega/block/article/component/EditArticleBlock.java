/*
 * $Id: EditArticleBlock.java,v 1.20 2005/02/24 16:52:55 joakim Exp $
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
import javax.faces.component.UIColumn;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.UISelectItems;
import javax.faces.component.html.HtmlCommandButton;
import javax.faces.component.html.HtmlDataTable;
import javax.faces.component.html.HtmlInputText;
import javax.faces.component.html.HtmlInputTextarea;
import javax.faces.component.html.HtmlOutputLink;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.component.html.HtmlPanelGrid;
import javax.faces.component.html.HtmlSelectManyListbox;
import javax.faces.component.html.HtmlSelectOneMenu;
import javax.faces.context.FacesContext;
import javax.faces.convert.IntegerConverter;
import javax.faces.el.ValueBinding;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import javax.faces.model.SelectItem;
import com.idega.block.article.component.reference.FileUploadForm;
import com.idega.content.bean.CaseListBean;
import com.idega.content.bean.ManagedContentBeans;
import com.idega.content.data.ContentItemCase;
import com.idega.core.localisation.business.ICLocaleBusiness;
import com.idega.presentation.IWBaseComponent;
import com.idega.webface.WFComponentSelector;
import com.idega.webface.WFContainer;
import com.idega.webface.WFErrorMessages;
import com.idega.webface.WFList;
import com.idega.webface.WFPage;
import com.idega.webface.WFPanelUtil;
import com.idega.webface.WFPlainOutputText;
import com.idega.webface.WFResourceUtil;
import com.idega.webface.WFTabbedPane;
import com.idega.webface.WFUtil;
import com.idega.webface.htmlarea.HTMLArea;

/**
 * Last modified: $Date: 2005/02/24 16:52:55 $ by $Author: joakim $
 *
 * @author Joakim
 * @version $Revision: 1.20 $
 */
public class EditArticleBlock extends IWBaseComponent implements ManagedContentBeans, ActionListener {
	public final static String EDIT_ARTICLE_BLOCK_ID = "edit_articles_block";
	private final static String P = "list_articles_block_"; // Id prefix
	
	public final static String EDIT_ARTICLES_BEAN_ID = "editArticlesBean";
	
//	public final static String ARTICLE_BLOCK_ID = "article_block";
	
	public final static String TASK_ID_EDIT = P + "t_edit";
	public final static String TASK_ID_PREVIEW = P + "t_preview";
	public final static String TASK_ID_LIST = P + "t_list";
	public final static String TASK_ID_DETAILS = P + "t_details";
	public final static String TASK_ID_MESSAGES = P + "t_messages";
	
	private final static String HEADLINE_ID = P + "headline";
	private final static String LOCALE_ID = P + "locale";
	private final static String TEASER_ID = P + "teaser";
	public final static String BODY_ID = P + "body";
	private final static String FOLDER_ID = P + "folder_location";
	private final static String AUTHOR_ID = P + "author";
	private final static String SOURCE_ID = P + "source";
	private final static String COMMENT_ID = P + "comment";
	private final static String PUBLISHED_FROM_DATE_ID = P + "published_from_date";
	private final static String PUBLISHED_TO_DATE_ID = P + "published_to_date";
	
	private final static String USER_MESSAGE_ID = P + "user_message";
	
	private final static String SAVE_ID = P + "save";
	private final static String FOR_REVIEW_ID = P + "for_review";
	private final static String PUBLISH_ID = P + "publish";
	private final static String REWRITE_ID = P + "rewrite";
	private final static String REJECT_ID = P + "reject";
	private final static String DELETE_ID = P + "delete";
	private final static String CANCEL_ID = P + "cancel";
	private final static String EDIT_CATEGORIES_ID = P + "edit_categories";
	private final static String ADD_IMAGE_ID = P + "add_image";
	private final static String REMOVE_IMAGE_ID = P + "remove_image";
	private final static String ADD_ATTACHMENT_ID = P + "add_attachment";
	private final static String REMOVE_ATTACHMENT_ID = P + "remove_attachment";
	private final static String FILE_UPLOAD_FORM_ID = P + "file_upload_form";
	private final static String FILE_UPLOAD_ID = P + "file_upload";
	private final static String FILE_UPLOAD_CANCEL_ID = P + "file_upload_cancel";
	private final static String ADD_RELATED_CONTENT_ITEM_ID = P + "add_related_item";
	private final static String REMOVE_RELATED_CONTENT_ITEM_ID = P + "remove_related_item";
	private final static String RELATED_CONTENT_ITEMS_CANCEL_ID = P + "related_items_cancel";
	private final static String EDIT_HTML_ID = P + "edit_html";
	
	private final static String TASKBAR_ID = P + "taskbar";

	private final static String BUTTON_SELECTOR_ID = P + "button_selector";
	private final static String EDITOR_SELECTOR_ID = P + "editor_selector";
	private final static String ARTICLE_EDITOR_ID = P + "article_editor";
	private final static String CATEGORY_EDITOR_ID = P + "category_editor";
	private final static String RELATED_CONTENT_ITEMS_EDITOR_ID = P + "related_items_editor";

	private final static String RELATED_CONTENT_ITEMS_LIST_ID = P + "related_items_list";

	private final static String AVAILABLE_CATEGORIES_ID = P + "avaliable_categories";
	private final static String ARTICLE_CATEGORIES_ID = P + "article_categories";
	private final static String ADD_CATEGORIES_ID = P + "add_categories";
	private final static String SUB_CATEGORIES_ID = P + "sub_categories";
	private final static String CATEGORY_BACK_ID = P + "category_back";

	public EditArticleBlock() {
	}

	protected void initializeContent() {
		setId(EDIT_ARTICLE_BLOCK_ID);
//		WFUtil.invoke(EDIT_ARTICLES_BEAN_ID, "setArticleLinkListener", this, ActionListener.class);
		add(getEditContainer());
	}
	
	/*
	 * Creates an edit container for the article.
	 */
	public UIComponent getEditContainer() {
		
		WFResourceUtil localizer = WFResourceUtil.getResourceUtilArticle();
		String ref = ARTICLE_ITEM_BEAN_ID + ".";
//		String bref = WFPage.CONTENT_BUNDLE + ".";

		WFContainer mainContainer = new WFContainer();
		mainContainer.setId(ARTICLE_EDITOR_ID);

		WFErrorMessages em = new WFErrorMessages();
		em.addErrorMessage(HEADLINE_ID);
		em.addErrorMessage(TEASER_ID);
//		em.addErrorMessage(PUBLISHED_FROM_DATE_ID);
//		em.addErrorMessage(PUBLISHED_TO_DATE_ID);
		em.addErrorMessage(SAVE_ID);
		
		mainContainer.add(em);

		HtmlPanelGrid p = WFPanelUtil.getPlainFormPanel(2);
		p.getChildren().add(WFUtil.group(localizer.getTextVB("headline"), WFUtil.getText(":")));
//		p.getChildren().add(WFUtil.group(WFUtil.getTextVB(bref + "language"), WFUtil.getText(":")));
		HtmlInputText headlineInput = WFUtil.getInputText(HEADLINE_ID, ref + "headline");
		headlineInput.setSize(70);
		p.getChildren().add(headlineInput);
		//HtmlSelectOneMenu localeMenu = WFUtil.getSelectOneMenu(LOCALE_ID, ref + "allLocales", ref + "pendingLocaleId");
		//localeMenu.setOnchange("document.forms[0].submit();");
		//p.getChildren().add(localeMenu);
		p.getChildren().add(WFUtil.group(localizer.getTextVB("teaser"), WFUtil.getText(":")));
		HtmlInputTextarea teaserArea = WFUtil.getTextArea(TEASER_ID, ref + "teaser", "600px", "60px");
		p.getChildren().add(teaserArea);
		p.getChildren().add(WFUtil.group(localizer.getTextVB("author"), WFUtil.getText(":")));
		HtmlInputText authorInput = WFUtil.getInputText(AUTHOR_ID, ref + "author");
		authorInput.setSize(70);
		p.getChildren().add(authorInput);

		//Article dropdown
		p.getChildren().add(WFUtil.group(localizer.getTextVB("language"), WFUtil.getText(":")));
		
		Iterator iter = ICLocaleBusiness.getListOfLocalesJAVA().iterator();
		UIInput langDropdown = new HtmlSelectOneMenu();
		List arrayList = new ArrayList();
		while(iter.hasNext()) {
			Locale locale = (Locale)iter.next();
			String keyStr = locale.getLanguage();
			String langStr = locale.getDisplayLanguage();
			SelectItem itemTemp = new SelectItem(keyStr, langStr, keyStr, false);
			arrayList.add(itemTemp);
		}
		
		UISelectItems items = new UISelectItems();
		items.setValue(arrayList);
		langDropdown.getChildren().add(items);
		ValueBinding vb = WFUtil.createValueBinding("#{" + ref +"contentLanguage" + "}");
		langDropdown.setValueBinding("value", vb);
		langDropdown.getValue();

		p.getChildren().add(langDropdown);

		//Article body
		p.getChildren().add(WFUtil.group(localizer.getTextVB("body"), WFUtil.getText(":")));
		HTMLArea bodyArea = WFUtil.getHtmlAreaTextArea(BODY_ID, ref + "body", "100%", "400px");
		bodyArea.addPlugin(HTMLArea.PLUGIN_TABLE_OPERATIONS);
		bodyArea.addPlugin(HTMLArea.PLUGIN_DYNAMIC_CSS, "3");
		bodyArea.addPlugin(HTMLArea.PLUGIN_CSS, "3");
		bodyArea.addPlugin(HTMLArea.PLUGIN_CONTEXT_MENU);
		bodyArea.addPlugin(HTMLArea.PLUGIN_LIST_TYPE);
		bodyArea.addPlugin(HTMLArea.PLUGIN_CHARACTER_MAP);
		bodyArea.setAllowFontSelection(false);
		
		p.getChildren().add(WFUtil.group(bodyArea, WFUtil.getBreak()));
//		HtmlCommandButton editButton = localizer.getButtonVB(EDIT_HTML_ID, "edit", this);
//		editButton.setOnclick("wurl='htmlarea/webface/htmledit.jsp?" + PREVIEW_ARTICLE_ITEM_ID + 
//					"='+this.tabindex;window.open(wurl,'Edit','height=450,width=600,resizable=yes,status=no,toolbar=no,menubar=no,location=no,scrollbars=no');return false;");
//		p.getChildren().add(WFUtil.group(WFUtil.group(bodyArea, WFUtil.getBreak()), editButton));
/*
		WFContainer imageContainer = new WFContainer();
		imageContainer.add(WFUtil.getButtonVB(ADD_IMAGE_ID, bref + "add_image", this));
		imageContainer.add(WFUtil.getBreak());
		imageContainer.add(getImageList());
		p.getChildren().add(imageContainer);
		p.getChildren().add(WFUtil.group(WFUtil.getTextVB(bref + "source"), WFUtil.getText(":")));
		p.getChildren().add(WFUtil.group(WFUtil.getTextVB(bref + "main_category"), WFUtil.getText(":")));
		HtmlInputTextarea sourceArea = WFUtil.getTextArea(SOURCE_ID, ref + "source", "440px", "30px");
		p.getChildren().add(sourceArea);		
		HtmlSelectOneMenu mainCategoryMenu = WFUtil.getSelectOneMenu(MAIN_CATEGORY_ID, ref + "categories", ref + "mainCategoryId");
		p.getChildren().add(mainCategoryMenu);		
*/
//		mainContainer.add(p);
//		mainContainer.add(WFUtil.getBreak());
		p.getChildren().add(WFUtil.getBreak());
		p.getChildren().add(WFUtil.getBreak());
		
//		p = WFPanelUtil.getPlainFormPanel(1);
/*
		p.getChildren().add(WFUtil.group(WFUtil.getHeaderTextVB(bref + "created"), 
				WFUtil.getTextVB(ref + "creationDate")
				));
*/
//		p.getChildren().add(WFUtil.getText(" "));
//		UIComponent t = WFUtil.group(localizer.getHeaderTextVB("status"), WFUtil.getText(": "));
//		t.getChildren().add(WFUtil.getTextVB(ref + "status"));
//		p.getChildren().add(t);
		p.getChildren().add(WFUtil.group(localizer.getTextVB("status"), WFUtil.getText(":")));
		p.getChildren().add(WFUtil.getTextVB(ref + "status"));
		p.getChildren().add(WFUtil.group(localizer.getTextVB("current_version"), WFUtil.getText(":")));
		p.getChildren().add(WFUtil.getTextVB(ref + "versionId"));
//		p.getChildren().add(WFUtil.getText(" "));
//		p.getChildren().add(WFUtil.group(localizer.getHeaderTextVB("current_version"), 
//				WFUtil.getTextVB(ref + "versionId")
//				));
		
//		mainContainer.add(p);
		p.getChildren().add(WFUtil.getBreak());
		p.getChildren().add(WFUtil.getBreak());
		
//		p = WFPanelUtil.getFormPanel(2);
		p.getChildren().add(WFUtil.group(localizer.getTextVB("comment"), WFUtil.getText(":")));
//		p.getChildren().add(WFUtil.group(WFUtil.getTextVB(bref + "attachments"), WFUtil.getText(":")));	
		HtmlInputTextarea commentArea = WFUtil.getTextArea(COMMENT_ID, ref + "comment", "600px", "60px");
		p.getChildren().add(commentArea);
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
		p.getChildren().add(WFUtil.getBreak());
		p.getChildren().add(WFUtil.getBreak());
//		mainContainer.add(WFUtil.getBreak());

//		p = WFPanelUtil.getPlainFormPanel(1);
//		HtmlCommandButton editCategoriesButton = WFUtil.getButtonVB(EDIT_CATEGORIES_ID, bref + "edit_categories", this);
//		p.getChildren().add(editCategoriesButton);

		//Temporary taking away folderr location
/*
		p.getChildren().add(WFUtil.group(localizer.getTextVB("folder"), WFUtil.getText(":")));
		HtmlInputText folderInput = WFUtil.getInputText(FOLDER_ID, ref + "folderLocation");
		if(null==folderInput.getValue() || "".equals(folderInput.getValue())) {
			String FolderString = ArticleUtil.getArticleYearMonthPath();
//			System.out.println("Folder "+FolderString);
			folderInput.setValue(FolderString);
		} else {
			File file = new File(folderInput.getValue().toString());
			folderInput.setValue(file.getParentFile().getParent());
		}
		folderInput.setSize(70);
		p.getChildren().add(folderInput);		
*/		
		p.getChildren().add(WFUtil.getBreak());
		p.getChildren().add(WFUtil.getBreak());
		p.getChildren().add(WFUtil.getBreak());
//		WFComponentSelector cs = new WFComponentSelector();
//		cs.setId(BUTTON_SELECTOR_ID);
//		cs.setDividerText(" ");
		HtmlCommandButton saveButton = localizer.getButtonVB(SAVE_ID, "save", this);
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
		p.getChildren().add(saveButton);
		
		mainContainer.add(p);
		
		WFComponentSelector editorSelector = new WFComponentSelector();
		editorSelector.setId(EDITOR_SELECTOR_ID);
		editorSelector.add(mainContainer);
		editorSelector.add(getCategoryEditContainer());
//		FileUploadForm f = new FileUploadForm(this, FILE_UPLOAD_ID, FILE_UPLOAD_CANCEL_ID);
		FileUploadForm f = new FileUploadForm();
		f.setId(FILE_UPLOAD_FORM_ID);
		editorSelector.add(f);
		editorSelector.add(getRelatedContentItemsContainer());
		editorSelector.setSelectedId(ARTICLE_EDITOR_ID, true);
		
		return editorSelector;
	}

	/*
	 * Returns container with form for editing categories.
	 */
	private UIComponent getCategoryEditContainer() {
		String ref = ARTICLE_ITEM_BEAN_ID + ".";
		String bref = WFPage.CONTENT_BUNDLE + ".";

		HtmlPanelGrid p = WFPanelUtil.getFormPanel(3);
		p.setId(CATEGORY_EDITOR_ID);
		p.getChildren().add(WFUtil.group(WFUtil.getTextVB(bref + "available_categories"), WFUtil.getText(":")));		
		p.getChildren().add(WFUtil.getText(" "));		
		p.getChildren().add(WFUtil.group(WFUtil.getTextVB(bref + "categories_for_this_article"), WFUtil.getText(":")));
		WFContainer c = new WFContainer();
		HtmlSelectManyListbox availableCategories = WFUtil.getSelectManyListbox(AVAILABLE_CATEGORIES_ID,
				ref + "availableCategories", ref + "selectedAvailableCategories");
		availableCategories.setStyle("width:200px;height:160px;");
		availableCategories.setConverter(new IntegerConverter());
		c.add(availableCategories);
		c.add(WFUtil.getBreak(2));
		c.add(WFUtil.getButtonVB(CATEGORY_BACK_ID, bref + "back", this));
		p.getChildren().add(c);		
		c = new WFContainer();
		c.add(WFUtil.getBreak());
		c.add(WFUtil.getButton(ADD_CATEGORIES_ID, ">", this));
		c.add(WFUtil.getBreak(2));
		c.add(WFUtil.getButton(SUB_CATEGORIES_ID, "<", this));
		p.getChildren().add(c);		
		HtmlSelectManyListbox articleCategories = WFUtil.getSelectManyListbox(ARTICLE_CATEGORIES_ID, 
				ref + "categories", ref + "selectedCategories");
		articleCategories.setStyle("width:200px;height:160px;");
		articleCategories.setConverter(new IntegerConverter());
		p.getChildren().add(articleCategories);
				
		return p;
	}
	
	/*
	 * Returns a list with realted content item links for the article.
	 */
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
	}
	
	/*
	 * Returns container with form for selecting related content items.
	 */
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
	}
	
	/**
	 * javax.faces.event.ActionListener#processAction()
	 */
	public void processAction(ActionEvent event) {
		String id = event.getComponent().getId();
		EditArticleBlock ab = (EditArticleBlock) event.getComponent().getParent().getParent().getParent().findComponent(EDIT_ARTICLE_BLOCK_ID);
		if (id.equals(SAVE_ID)) {
			ab.storeArticle();
		} else if (id.equals(FOR_REVIEW_ID)) {
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
		} else if (id.equals(EDIT_CATEGORIES_ID)) {
			ab.setEditView(CATEGORY_EDITOR_ID);
		} else if (id.equals(ADD_CATEGORIES_ID)) {
			WFUtil.invoke(ARTICLE_ITEM_BEAN_ID, "addSelectedCategories");
		} else if (id.equals(SUB_CATEGORIES_ID)) {
			WFUtil.invoke(ARTICLE_ITEM_BEAN_ID, "removeSelectedCategories");
		} else if (id.equals(CATEGORY_BACK_ID)) {
			ab.setEditView(ARTICLE_EDITOR_ID);
		} else if (id.equals(ADD_RELATED_CONTENT_ITEM_ID)) {
			ab.setEditView(RELATED_CONTENT_ITEMS_EDITOR_ID);
		} else if (id.equals(ADD_IMAGE_ID)) {
			ab.setEditView(FILE_UPLOAD_FORM_ID);
		} else if (id.equals(FILE_UPLOAD_CANCEL_ID)) {
			ab.setEditView(ARTICLE_EDITOR_ID);
		} else if (id.equals(FILE_UPLOAD_ID)) {
			ab.setEditView(ARTICLE_EDITOR_ID);
		} else if (id.equals(REMOVE_IMAGE_ID)) {
			int imageNo = WFUtil.getIntParameter(event.getComponent(), "image_no");
			WFUtil.invoke(ARTICLE_ITEM_BEAN_ID, "removeImage", new Integer(imageNo));
		} else if (id.equals(CaseListBean.CASE_ID)){
			String itemId = WFUtil.getParameter(event.getComponent(), "id");
			WFUtil.invoke(ARTICLE_ITEM_BEAN_ID, "addRelatedContentItem", new Integer(itemId));
			ab.setEditView(ARTICLE_EDITOR_ID);
		} else if (id.equals(RELATED_CONTENT_ITEMS_CANCEL_ID)) {
			ab.setEditView(ARTICLE_EDITOR_ID);
		} else if (id.equals(REMOVE_RELATED_CONTENT_ITEM_ID)) {
			int itemId = WFUtil.getIntParameter(event.getComponent(), "item_id");
			WFUtil.invoke(ARTICLE_ITEM_BEAN_ID, "removeRelatedContentItem", new Integer(itemId));
		}
	}

	/**
	 * Stores the current article. 
	 */
	public void storeArticle() {
		String bref = WFPage.CONTENT_BUNDLE + ".";
		boolean storeOk = ((Boolean) WFUtil.invoke(ARTICLE_ITEM_BEAN_ID, "storeArticle")).booleanValue();
		if (!storeOk) {
			List errorKeys = (List) WFUtil.getValue(ARTICLE_ITEM_BEAN_ID, "errorKeys");
			if (errorKeys != null) {
				for (Iterator iter = errorKeys.iterator(); iter.hasNext();) {
					String errorKey = (String) iter.next();
					WFUtil.addMessageVB(findComponent(SAVE_ID), bref + errorKey);
				}
			}
			return;
		}
		setUserMessage("article_saved");
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
		WFTabbedPane tb = (WFTabbedPane) findComponent(TASKBAR_ID);
		tb.setSelectedMenuItemId(TASK_ID_MESSAGES);
	}
	

	/**
	 * Sets the editor view for this article block.
	 *
	 */
	public void setEditView(String s) {
		WFComponentSelector cs = (WFComponentSelector) findComponent(EDITOR_SELECTOR_ID);
		cs.setSelectedId(ARTICLE_EDITOR_ID, s.equals(ARTICLE_EDITOR_ID));
		cs.setSelectedId(CATEGORY_EDITOR_ID, s.equals(CATEGORY_EDITOR_ID));
		cs.setSelectedId(FILE_UPLOAD_FORM_ID, s.equals(FILE_UPLOAD_FORM_ID));
		cs.setSelectedId(RELATED_CONTENT_ITEMS_EDITOR_ID, s.equals(RELATED_CONTENT_ITEMS_EDITOR_ID));
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
		WFUtil.invoke(ARTICLE_ITEM_BEAN_ID, "updateLocale");
//		updateEditButtons();
		super.encodeBegin(context);
	}
}
