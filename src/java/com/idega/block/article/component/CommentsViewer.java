package com.idega.block.article.component;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import com.idega.block.article.business.ArticleConstants;
import com.idega.block.article.business.CommentsEngine;
import com.idega.block.web2.business.Web2Business;
import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.business.SpringBeanLookup;
import com.idega.content.business.ContentConstants;
import com.idega.content.business.ContentUtil;
import com.idega.content.themes.helpers.business.ThemesHelper;
import com.idega.core.accesscontrol.business.NotLoggedOnException;
import com.idega.core.builder.business.BuilderService;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWUserContext;
import com.idega.presentation.Block;
import com.idega.presentation.IWContext;
import com.idega.presentation.Image;
import com.idega.presentation.Layer;
import com.idega.presentation.Script;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.CheckBox;
import com.idega.util.CoreConstants;
import com.idega.util.CoreUtil;
import com.idega.util.PresentationUtil;

public class CommentsViewer extends Block {
	
	public static final String FEED_IMAGE = "/images/feed.png";
	public static final String DELETE_IMAGE = "/images/comments_delete.png";
	private static final String COMMENTS_BLOCK_ID = "comments_block";
	private static final String SHOW_COMMENTS_PROPERTY = "showCommentsForAllUsers";
	
	private String styleClass = "content_item_comments_style";
	private String linkToComments = null;
	
	private String moduleId = null;
	
	private boolean showCommentsForAllUsers = true;
	private boolean showCommentsList = true; // If expand list on page load
	private boolean isForumPage = false;
	private boolean usedInArticleList = false;
	
	private String DWR_ENGINE = "/dwr/engine.js";
	private String COMMENTS_ENGINE = "/dwr/interface/CommentsEngine.js";
	private String COMMENTS_HELPER = "javascript/ArticleCommentsHelper.js";
	private String INIT_COMMENTS_ACTION = "initComments();";
	private String INIT_SCRIPT_LINE = "window.addEvent('domready', function() {"+INIT_COMMENTS_ACTION+"});";
	private String ENABLE_REVERSE_AJAX_ACTION = "enableReverseAjax();";
	private String ENABLE_REVERSE_AJAX_SCRIPT_LINE = "window.addEvent('domready', function() {"+ENABLE_REVERSE_AJAX_ACTION+"});";
	
	private static final String SEPARATOR = "', '";
	
	public void main(IWContext iwc) {
		getModuleId(iwc);
		
		if (linkToComments == null) {
			if (!findLinkToComments()) {
				if (isStandAlone(iwc)) {
					CommentsEngine engine = null;
					try {
						engine = (CommentsEngine) IBOLookup.getSessionInstance(iwc, CommentsEngine.class);
					} catch (IBOLookupException e) {
						e.printStackTrace();
						return;
					}
					linkToComments = engine.getFixedCommentsUri(iwc, null, moduleId);
				}
				else {
					return;
				}
			}
		}
		
		ThemesHelper helper = ThemesHelper.getInstance();
		IWBundle bundle = getBundle(iwc);
		
		String commentsId = moduleId;
		
		Layer container = new Layer();
		container.setId(new StringBuffer(commentsId).append(COMMENTS_BLOCK_ID).toString());
		container.setStyleClass(styleClass);
		this.add(container);
		
		if (!isUsedInArticleList()) {
			if (CoreUtil.isSingleComponentRenderingProcess(iwc)) {
				container.add(PresentationUtil.getJavaScriptSourceLines(getJavaScriptSources(iwc)));
				
				List<String> actions = new ArrayList<String>();
				actions.add(INIT_COMMENTS_ACTION);
				actions.add(ENABLE_REVERSE_AJAX_ACTION);
				container.add(PresentationUtil.getJavaScriptActions(actions));
			}
			else {
				PresentationUtil.addJavaScriptSourcesLinesToHeader(iwc, getJavaScriptSources(iwc));
				PresentationUtil.addJavaScriptActionsToBody(iwc, getJavaScriptActions());
			}
		}
		
		boolean hasValidRights = ContentUtil.hasContentEditorRoles(iwc);
		
		if (!hasValidRights && !showCommentsForAllUsers) {
			return;
		}
		
		int commentsCount = getCommentsCount(iwc);
		StringBuffer linkToAtomFeedImage = new StringBuffer(bundle.getResourcesPath());
		linkToAtomFeedImage.append(FEED_IMAGE);
		
		if (!isUsedInArticleList()) {
			// JavaScript
			Script script = new Script();
			StringBuffer action = new StringBuffer("window.addEvent('domready', function(){setCommentStartInfo('");
			action.append(linkToComments).append(SEPARATOR).append(commentsId).append("', ").append(showCommentsList).append(")});");
			script.addScriptLine(action.toString());
			container.add(script);
		}
		
		// Enable comments container
		addEnableCommentsCheckboxContainer(iwc, container, hasValidRights, null);
		
		// Comments label
		Layer articleComments = new Layer();
		articleComments.setId(new StringBuffer(commentsId).append("article_comments_link_label_container").toString());
		StringBuffer comments = new StringBuffer(bundle.getLocalizedString("comments"));
		comments.append(ContentConstants.SPACE).append("(<span id='").append(commentsId);
		comments.append("contentItemCount' class='contentItemCountStyle'>").append(commentsCount).append("</span>)");
		Link commentsLabel = new Link(comments.toString(), "javascript:void(0)");
		StringBuffer getCommentsAction = new StringBuffer("getCommentsList('").append(linkToComments).append(SEPARATOR);
		getCommentsAction.append(commentsId).append("'); return false;");
		commentsLabel.setOnClick(getCommentsAction.toString());
		articleComments.add(commentsLabel);
		
		// Simple space
		addSimpleSpace(articleComments);
		
		// Link - Atom feed
		if (commentsCount > 0) {
			Image atom = new Image(linkToAtomFeedImage.toString(), bundle.getLocalizedString("atom_feed"));
			Link linkToFeed = new Link();
			linkToFeed.setId(new StringBuffer(commentsId).append("article_comments_link_to_feed").toString());
			linkToFeed.setImage(atom);
			linkToFeed.setURL(helper.getFullServerName(iwc) + CoreConstants.WEBDAV_SERVLET_URI + linkToComments);
			articleComments.add(linkToFeed);
			
			// Delete comments image
			if (hasValidRights) {
				addSimpleSpace(articleComments);
				String deleteImage = new StringBuffer(bundle.getResourcesPath()).append(DELETE_IMAGE).toString();
				Image delete = new Image(deleteImage, bundle.getLocalizedString("delete_all_comments"));
				delete.setStyleClass("deleteCommentsImage");
				delete.setId(new StringBuffer(commentsId).append("delete_article_comments").toString());
				StringBuffer deleteAction = new StringBuffer("deleteComments('").append(commentsId).append("', null, '");
				deleteAction.append(linkToComments).append("'); return false;");
				delete.setOnClick(deleteAction.toString());
				articleComments.add(delete);
			}
		}
		
		container.add(articleComments);
		
		// Add comment block
		container.add(getAddCommentBlock(iwc, commentsId));
	}
	
	protected List<String> getJavaScriptSources(IWContext iwc) {
		List<String> sources = new ArrayList<String>();
		sources.add(COMMENTS_ENGINE);
		sources.add(DWR_ENGINE);
		sources.add(getBundle(iwc).getVirtualPathWithFileNameString(COMMENTS_HELPER));
		Web2Business web2 = SpringBeanLookup.getInstance().getSpringBean(iwc, Web2Business.class);
		if (web2 != null) {
			try {
				sources.add(web2.getBundleURIToMootoolsLib());
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return sources;
	}
	
	protected List<String> getJavaScriptActions() {
		List<String> actions = new ArrayList<String>();
		actions.add(INIT_SCRIPT_LINE);
		actions.add(ENABLE_REVERSE_AJAX_SCRIPT_LINE);
		return actions;
	}
		
	private void addSimpleSpace(Layer container) {
		//	Simple space
		container.add(new Text(ContentConstants.SPACE));
	}
	
	@SuppressWarnings("unchecked")
	private boolean findLinkToComments() {
		UIComponent region = this.getParent();
		if (region == null) {
			return false;
		}
		List<UIComponent> children = region.getChildren();
		if (children == null) {
			return false;
		}
		Object o = null;
		ArticleItemViewer articleViewer = null;
		for (int i = 0; i < children.size(); i++) {
			o = children.get(i);
			if (o instanceof ArticleItemViewer) {
				articleViewer = (ArticleItemViewer) o;
				linkToComments = articleViewer.getLinkToComments();
			}
		}
		if (linkToComments == null) {
			return false;
		}
		return true;
	}
	
	protected String getThisPageKey(IWContext iwc) {
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
	
	private void addEnableCommentsCheckboxContainer(IWContext iwc, Layer container, boolean hasValidRights, String cacheKey) {
		if (!hasValidRights) {
			return;
		}
		if (isUsedInArticleList()) {
			return;
		}
		container.add(getCommentsController(iwc, cacheKey, moduleId, isShowCommentsForAllUsers(), SHOW_COMMENTS_PROPERTY));
	}
	
	protected Layer getCommentsController(IWContext iwc, String cacheKey, String moduleId, boolean enabled, String propertyName) {
		Layer commentsController = new Layer();
		if (iwc == null || propertyName == null) {
			return commentsController;
		}
		
		String pageKey = getThisPageKey(iwc);
		if (pageKey == null) {
			return commentsController;
		}
		
		CheckBox enableCheckBox = new CheckBox("enableComments");
		enableCheckBox.setId(new StringBuffer(moduleId).append("manageCommentsBlockCheckBox").toString());
		StringBuffer action = new StringBuffer("enableComments(this.checked, '");
		action.append(pageKey).append(SEPARATOR).append(moduleId).append(SEPARATOR).append(propertyName).append("', ");
		if (cacheKey == null) {
			action.append("null);");
		}
		else {
			action.append("'").append(cacheKey).append("');");
		}
		enableCheckBox.setOnClick(action.toString());
		enableCheckBox.setChecked(enabled);
		
		Text enableText = new Text(getBundle(iwc).getLocalizedString("enable_comments"));
		
		commentsController.add(enableText);
		commentsController.add(enableCheckBox);
		
		return commentsController;
	}
	
	private UIComponent getAddCommentBlock(IWContext iwc, String commentsId) {
		IWBundle bundle = getBundle(iwc);
		
		Layer addComments = new Layer();
		addComments.setId(new StringBuffer(commentsId).append("add_comment_block").toString());
		Link label = new Link(bundle.getLocalizedString("add_your_comment"), "javascript:void(0)");
		String user = bundle.getLocalizedString("name");
		String subject = bundle.getLocalizedString("subject");
		String comment = bundle.getLocalizedString("comment");
		String posted = bundle.getLocalizedString("posted");
		String send = bundle.getLocalizedString("send");
		String sending = bundle.getLocalizedString("sending");
		String loggedUser = null;
		try {
			loggedUser = iwc.getCurrentUser().getName();
		} catch (NotLoggedOnException e) {
			loggedUser = bundle.getLocalizedString("anonymous");
		}
		
		StringBuffer action = new StringBuffer("addCommentPanel('").append(addComments.getId()).append(SEPARATOR);
		action.append(linkToComments).append(SEPARATOR).append(user).append(SEPARATOR).append(subject).append(SEPARATOR);
		action.append(comment).append(SEPARATOR).append(posted).append(SEPARATOR).append(send).append(SEPARATOR);
		action.append(sending).append(SEPARATOR).append(loggedUser).append(SEPARATOR);
		action.append(bundle.getLocalizedString("email")).append(SEPARATOR);
		action.append(bundle.getLocalizedString("comment_form")).append("', ").append(isForumPage);
		action.append(", '").append(commentsId).append("', '").append(moduleId).append("'); return false;");
		label.setOnClick(action.toString());
		addComments.add(label);
		return addComments;
	}
	
	private int getCommentsCount(IWContext iwc) {
		CommentsEngine comments = null;
		try {
			comments = (CommentsEngine) IBOLookup.getServiceInstance(iwc, CommentsEngine.class);
		} catch (IBOLookupException e) {
			e.printStackTrace();
			return 0;
		}
		try {
			return comments.getCommentsCount(linkToComments);
		} catch (RemoteException e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	public Object saveState(FacesContext context) {
		Object values[] = new Object[7];
		values[0] = super.saveState(context);
		values[1] = linkToComments;
		values[2] = styleClass;
		values[3] = showCommentsList;
		values[4] = isForumPage;
		values[5] = showCommentsForAllUsers;
		values[6] = usedInArticleList;
		return values;
	}

	public void restoreState(FacesContext context, Object state) {
		Object values[] = (Object[]) state;
		super.restoreState(context, values[0]);
		linkToComments = values[1].toString();
		styleClass = values[2].toString();
		showCommentsList = (Boolean) values[3];
		isForumPage = (Boolean) values[4];
		showCommentsForAllUsers = (Boolean) values[5];
		usedInArticleList = (Boolean) values[6];
	}

	public boolean isForumPage() {
		return isForumPage;
	}

	public void setForumPage(boolean isForumPage) {
		this.isForumPage = isForumPage;
	}

	public boolean isShowCommentsForAllUsers() {
		return showCommentsForAllUsers;
	}

	public void setShowCommentsForAllUsers(boolean showCommentsForAllUsers) {
		this.showCommentsForAllUsers = showCommentsForAllUsers;
	}

	public boolean isShowCommentsList() {
		return showCommentsList;
	}

	public void setShowCommentsList(boolean showCommentsList) {
		this.showCommentsList = showCommentsList;
	}

	public String getStyleClass() {
		return styleClass;
	}

	public void setStyleClass(String styleClass) {
		this.styleClass = styleClass;
	}

	public String getLinkToComments() {
		return linkToComments;
	}

	public void setLinkToComments(String linkToComments) {
		this.linkToComments = linkToComments;
	}
	
	public String getBuilderName(IWUserContext iwuc) {
		String name = getBundle(iwuc).getComponentName(CommentsViewer.class);
		if (name == null || ArticleConstants.EMPTY.equals(name)) {
			return "CommentsViewer";
		}
		return name;
	}

	public boolean isUsedInArticleList() {
		return usedInArticleList;
	}

	public void setUsedInArticleList(boolean usedInArticleList) {
		this.usedInArticleList = usedInArticleList;
	}
	
	public String getBundleIdentifier() {
		return ArticleConstants.IW_BUNDLE_IDENTIFIER;
	}
	
	private boolean isStandAlone(IWContext iwc) {
		int id = iwc.getCurrentIBPageID();
		if (id < 0) {
			return false;
		}
		
		String pageKey = String.valueOf(id);
		BuilderService service = null;
		try {
			service = getBuilderService(iwc);
		} catch (RemoteException e) {
			e.printStackTrace();
			return false;
		}
		
		List<String> articleItems = service.getModuleId(pageKey, ArticleItemViewer.class.getName());
		if (articleItems != null && articleItems.size() > 0) {
			return false;
		}
		
		List<String> articleViewers = service.getModuleId(pageKey, ArticleListViewer.class.getName());
		if (articleViewers != null && articleViewers.size() > 0) {
			return false;
		}
		
		return true;
	}
	
	private void getModuleId(IWContext iwc) {
		BuilderService service = null;
		try {
			service = getBuilderService(iwc);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		if (service != null) {
			moduleId = service.getInstanceId(this);
			if (moduleId == null) {
					moduleId = this.getId();
			}
		}
	}

}
