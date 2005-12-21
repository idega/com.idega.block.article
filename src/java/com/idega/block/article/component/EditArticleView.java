/*
 * $Id: EditArticleView.java,v 1.12 2005/12/21 16:33:18 laddi Exp $
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
import javax.faces.component.html.HtmlOutputText;
import javax.faces.component.html.HtmlPanelGrid;
import javax.faces.component.html.HtmlSelectOneMenu;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import javax.faces.event.ValueChangeEvent;
import javax.faces.event.ValueChangeListener;
import javax.faces.model.SelectItem;
import org.apache.myfaces.custom.savestate.UISaveState;
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
import com.idega.user.data.User;
import com.idega.webface.WFComponentSelector;
import com.idega.webface.WFContainer;
import com.idega.webface.WFErrorMessages;
import com.idega.webface.WFPage;
import com.idega.webface.WFPanelUtil;
import com.idega.webface.WFResourceUtil;
import com.idega.webface.WFTabbedPane;
import com.idega.webface.WFUtil;
import com.idega.webface.htmlarea.HTMLArea;

/**
 * <p>
 * This is the part for the editor of article is inside the admin interface
 * </p>
 * Last modified: $Date: 2005/12/21 16:33:18 $ by $Author: laddi $
 *
 * @author Joakim,Tryggvi Larusson
 * @version $Revision: 1.12 $
 */
public class EditArticleView extends IWBaseComponent implements ManagedContentBeans, ActionListener, ValueChangeListener {
	public final static String EDIT_ARTICLE_BLOCK_ID = "edit_articles_block";
	private final static String P = "list_articles_block_"; // Id prefix
	
	public final static String EDIT_ARTICLES_BEAN_ID = "editArticlesBean";
	public final static String ref = ARTICLE_ITEM_BEAN_ID + ".";
	
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
	//private final static String FOLDER_ID = P + "folder_location";
	private final static String AUTHOR_ID = P + "author";
	private final static String SOURCE_ID = P + "source";
	private final static String COMMENT_ID = P + "comment";
	//private final static String PUBLISHED_FROM_DATE_ID = P + "published_from_date";
	//private final static String PUBLISHED_TO_DATE_ID = P + "published_to_date";
	
	private final static String USER_MESSAGE_ID = P + "user_message";
	
	private final static String SAVE_ID = P + "save";
	private final static String FOR_REVIEW_ID = P + "for_review";
	private final static String PUBLISH_ID = P + "publish";
	private final static String REWRITE_ID = P + "rewrite";
	private final static String REJECT_ID = P + "reject";
	private final static String DELETE_ID = P + "delete";
	/*private final static String CANCEL_ID = P + "cancel";
	private final static String EDIT_CATEGORIES_ID = P + "edit_categories";
	private final static String ADD_IMAGE_ID = P + "add_image";
	private final static String REMOVE_IMAGE_ID = P + "remove_image";
	private final static String ADD_ATTACHMENT_ID = P + "add_attachment";
	private final static String REMOVE_ATTACHMENT_ID = P + "remove_attachment";
	private final static String ADD_RELATED_CONTENT_ITEM_ID = P + "add_related_item";
	private final static String REMOVE_RELATED_CONTENT_ITEM_ID = P + "remove_related_item";
	private final static String RELATED_CONTENT_ITEMS_CANCEL_ID = P + "related_items_cancel";
	private final static String EDIT_HTML_ID = P + "edit_html";*/
	
	private final static String TASKBAR_ID = P + "taskbar";

	private final static String BUTTON_SELECTOR_ID = P + "button_selector";
	private final static String EDITOR_SELECTOR_ID = P + "editor_selector";
	private final static String ARTICLE_EDITOR_ID = P + "article_editor";
	private final static String CATEGORY_EDITOR_ID = P + "category_editor";
	private final static String RELATED_CONTENT_ITEMS_EDITOR_ID = P + "related_items_editor";

	/*private final static String RELATED_CONTENT_ITEMS_LIST_ID = P + "related_items_list";

	private final static String AVAILABLE_CATEGORIES_ID = P + "avaliable_categories";
	private final static String ARTICLE_CATEGORIES_ID = P + "article_categories";
	private final static String ADD_CATEGORIES_ID = P + "add_categories";
	private final static String SUB_CATEGORIES_ID = P + "sub_categories";
	private final static String CATEGORY_BACK_ID = P + "category_back";*/
	private static final String EDIT_MODE_CREATE = "create";
	private static final String EDIT_MODE_EDIT = "edit";
	

	//WebDAVCategories categoriesUI = new WebDAVCategories();

	boolean clearOnInit = false;
	private String editMode;

	public EditArticleView() {
	}

	protected void initializeComponent(FacesContext context) {
		setId(EDIT_ARTICLE_BLOCK_ID);
		if(clearOnInit){
			ArticleItemBean bean = getArticleItemBean();
			if(bean!=null){
				bean.clear();
			}
		}
		
//		WFUtil.invoke(EDIT_ARTICLES_BEAN_ID, "setArticleLinkListener", this, ActionListener.class);
		add(getEditContainer());
	}
	
	/*
	 * Creates an edit container for the article.
	 */
	public UIComponent getEditContainer() {
		
		IWContext iwc = IWContext.getInstance();
		WFResourceUtil localizer = WFResourceUtil.getResourceUtilArticle();
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
		
		//Saving the state of the articleItemBean specially because the scpoe
		//of this bean now is 'request' not 'session'
		UISaveState beanSaveState = new UISaveState();
		ValueBinding binding = WFUtil.createValueBinding("#{"+ARTICLE_ITEM_BEAN_ID+"}");
		beanSaveState.setId("articleItemBeanSaveState");
		beanSaveState.setValueBinding("value",binding);
		mainContainer.add(beanSaveState);
		
		HtmlPanelGrid p = WFPanelUtil.getPlainFormPanel(2);

		//Language dropdown
		p.getChildren().add(WFUtil.group(localizer.getTextVB("language"), WFUtil.getText(":")));
		UIComponent langDropdown = getLanguageDropdownMenu();
		p.getChildren().add(langDropdown);

		p.getChildren().add(WFUtil.group(localizer.getTextVB("headline"), WFUtil.getText(":")));
//		p.getChildren().add(WFUtil.group(WFUtil.getTextVB(bref + "language"), WFUtil.getText(":")));
		HtmlInputText headlineInput = WFUtil.getInputText(HEADLINE_ID, ref + "headline");
		headlineInput.setSize(70);
		p.getChildren().add(headlineInput);
		//HtmlSelectOneMenu localeMenu = WFUtil.getSelectOneMenu(LOCALE_ID, ref + "allLocales", ref + "pendingLocaleId");
		//localeMenu.setOnchange("document.forms[0].submit();");
		//p.getChildren().add(localeMenu);

		p.getChildren().add(WFUtil.group(localizer.getTextVB("author"), WFUtil.getText(":")));
		HtmlInputText authorInput = WFUtil.getInputText(AUTHOR_ID, ref + "author");
		authorInput.setSize(70);
		User user = iwc.getCurrentUser();
		if(user!=null){
			String userName = user.getName();
			getArticleItemBean().setAuthor(userName);
		}
		p.getChildren().add(authorInput);

		//Article body
		p.getChildren().add(WFUtil.group(localizer.getTextVB("body"), WFUtil.getText(":")));
		HTMLArea bodyArea = WFUtil.getHtmlAreaTextArea(BODY_ID, ref + "body", "500px", "400px");
		//HTMLArea bodyArea = WFUtil.getHtmlAreaTextArea(BODY_ID, ref + "body");
		
		
		/*bodyArea.addPlugin(HTMLArea.PLUGIN_TABLE_OPERATIONS);
		bodyArea.addPlugin(HTMLArea.PLUGIN_DYNAMIC_CSS, "3");
		bodyArea.addPlugin(HTMLArea.PLUGIN_CSS, "3");
		bodyArea.addPlugin(HTMLArea.PLUGIN_CONTEXT_MENU);
		bodyArea.addPlugin(HTMLArea.PLUGIN_LIST_TYPE);
		bodyArea.addPlugin(HTMLArea.PLUGIN_CHARACTER_MAP);
		*/
		bodyArea.setAllowFontSelection(false);
		//bodyArea.addPlugin("TableOperations");
		//bodyArea.addPlugin("Template");
		
		//bodyArea.addPlugin("Forms");
		//bodyArea.addPlugin("FormOperations");
		//bodyArea.addPlugin("EditTag");
		//bodyArea.addPlugin("Stylist");
		//bodyArea.addPlugin("CSS");
		//bodyArea.addPlugin("DynamicCSS");
		//bodyArea.addPlugin("FullPage");
		//bodyArea.addPlugin("NoteServer");
		//bodyArea.addPlugin("QuickTag");
		//bodyArea.addPlugin("InsertSmiley");
		//bodyArea.addPlugin("InsertWords");
		//bodyArea.addPlugin("ContextMenu");
		//bodyArea.addPlugin("LangMarks");
		//bodyArea.addPlugin("DoubleClick");
		//bodyArea.addPlugin("ListType");
		//bodyArea.addPlugin("ImageManager");
		
		p.getChildren().add(WFUtil.group(bodyArea, WFUtil.getBreak()));

		p.getChildren().add(WFUtil.group(localizer.getTextVB("teaser"), WFUtil.getText(":")));
		HtmlInputTextarea teaserArea = WFUtil.getTextArea(TEASER_ID, ref + "teaser", "500px", "60px");
		p.getChildren().add(teaserArea);
		
		p.getChildren().add(WFUtil.group(localizer.getTextVB("source"), WFUtil.getText(":")));
		HtmlInputText sourceArea = WFUtil.getInputText(SOURCE_ID, ref + "source");
		sourceArea.setSize(70);
		p.getChildren().add(sourceArea);

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
		p.getChildren().add(WFUtil.getTextVB(ref + "versionName"));
		p.getChildren().add(WFUtil.getBreak());
		p.getChildren().add(WFUtil.getBreak());
		
//		p = WFPanelUtil.getFormPanel(2);
		p.getChildren().add(WFUtil.group(localizer.getTextVB("comment"), WFUtil.getText(":")));
//		p.getChildren().add(WFUtil.group(WFUtil.getTextVB(bref + "attachments"), WFUtil.getText(":")));	
		HtmlInputTextarea commentArea = WFUtil.getTextArea(COMMENT_ID, ref + "comment", "500px", "60px");
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
		//Categories
//		WebDAVCategories categoriesUI = new WebDAVCategories();
		//ArticleItemBean articleItemBean = (ArticleItemBean) getArticleItemBean();
		//String resourcePath = articleItemBean.getArticleResourcePath();
		
		addCategoryEditor(p);
		
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
		
		//WFComponentSelector editorSelector = new WFComponentSelector();
		//editorSelector.setId(EDITOR_SELECTOR_ID);
		//editorSelector.add(mainContainer);
		//editorSelector.add(getCategoryEditContainer());
//		FileUploadForm f = new FileUploadForm(this, FILE_UPLOAD_ID, FILE_UPLOAD_CANCEL_ID);

		//editorSelector.add(getRelatedContentItemsContainer());
		//editorSelector.setSelectedId(ARTICLE_EDITOR_ID, true);
		
		//return editorSelector;
		
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
		UIComponent categoriesUi = findComponent(WebDAVCategories.CATEGORIES_BLOCK_ID);
		return categoriesUi;
	}
	
	protected void addCategoryEditor(UIComponent parent){
		WebDAVCategories categoriesUI = (WebDAVCategories) getCategoryEditor();
		if(categoriesUI==null){
			//id on the component is set implicitly
			categoriesUI=new WebDAVCategories();
			//we want to set the categories also on the parent ".article" folder:
			categoriesUI.setCategoriesOnParent(true);
			categoriesUI.setDisplaySaveButton(false);
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
			parent.getChildren().add(categoriesUI);
		}
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
		UIComponent rootParent = rootParent = event.getComponent().getParent().getParent().getParent();
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
					categoriesUI.saveCategoriesSettings(fileResourcePath, categoriesUI);
				}
			}
			clearOnInit=false;
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
			WFUtil.addMessageVB(findComponent(SAVE_ID),ArticleUtil.IW_BUNDLE_IDENTIFIER, errorKey);
		}
		catch(Exception e){
			String errorKey = ArticleStoreException.KEY_ERROR_ON_STORE;
			WFUtil.addMessageVB(findComponent(SAVE_ID),ArticleUtil.IW_BUNDLE_IDENTIFIER, errorKey);
			WFUtil.addMessage(findComponent(SAVE_ID),e.getClass().getName()+" : "+e.getMessage());
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
		WFTabbedPane tb = (WFTabbedPane) findComponent(TASKBAR_ID);
		tb.setSelectedMenuItemId(TASK_ID_MESSAGES);
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
		if(clearOnInit) {
			//WFUtil.invoke(ARTICLE_ITEM_BEAN_ID, "clear");
			ArticleItemBean bean = getArticleItemBean();
			if(bean!=null){
				bean.clear();
			}
		}
		

		
//		WFUtil.invoke(ARTICLE_ITEM_BEAN_ID, "updateLocale");
//		updateEditButtons();
		
		super.encodeBegin(context);

		IWContext iwc = IWContext.getIWContext(context);
		String resourcePath = iwc.getParameter(ContentViewer.PARAMETER_CONTENT_RESOURCE);
		String baseFolderPath = iwc.getParameter(ContentItemToolbar.PARAMETER_BASE_FOLDER_PATH);
		
		if(resourcePath!=null){
			getArticleItemBean().setResourcePath(resourcePath);
		}
		
		if(baseFolderPath!=null){
			getArticleItemBean().setBaseFolderLocation(baseFolderPath);
		}
		
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
	public void processValueChange(ValueChangeEvent arg0) throws AbortProcessingException {
		if(arg0.getOldValue()==null) {
			return;
		}
		System.out.println("Language value has changed from "+arg0.getOldValue()+" to "+arg0.getNewValue());
		
		ArticleItemBean bean = getArticleItemBean();
		//String articlePath = (String)WFUtil.invoke(ARTICLE_ITEM_BEAN_ID, "getArticlePath");
		String articlePath = bean.getResourcePath();
		//String language = (String)WFUtil.invoke(ARTICLE_ITEM_BEAN_ID, "getContentLanguage");
		bean.getContentLanguage();
		if(null==articlePath) {
			//Article has not been stored previousley, so nothing have to be done
			return;
		}
		System.out.println("processValueChange: Article path: "+articlePath);
		//boolean result = ((Boolean)WFUtil.invoke(ARTICLE_ITEM_BEAN_ID, "load",articlePath+"/"+arg0.getNewValue()+ArticleItemBean.ARTICLE_SUFFIX)).booleanValue();
		//System.out.println("loading other language "+result);
		//if(result) {
			//WFUtil.invoke(ARTICLE_ITEM_BEAN_ID, "setLanguageChange",arg0.getNewValue().toString());
			String langChange = arg0.getNewValue().toString();
			bean.setLanguageChange(langChange);
			Locale locale = new Locale(langChange);
			bean.setLocale(locale);
			System.out.println("processValueChange: changint to other language "+langChange);
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
	
}
