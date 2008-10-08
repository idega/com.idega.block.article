package com.idega.block.article.component;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import org.apache.webdav.lib.WebdavResource;

import com.idega.block.article.business.ArticleConstants;
import com.idega.block.article.business.CommentsEngine;
import com.idega.block.article.business.CommentsPersistenceManager;
import com.idega.block.web2.business.Web2Business;
import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.content.business.ContentConstants;
import com.idega.content.business.ContentUtil;
import com.idega.content.presentation.ContentViewer;
import com.idega.content.themes.helpers.business.ThemesHelper;
import com.idega.core.accesscontrol.business.NotLoggedOnException;
import com.idega.core.builder.business.BuilderService;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.idegaweb.IWUserContext;
import com.idega.presentation.Block;
import com.idega.presentation.IWContext;
import com.idega.presentation.Image;
import com.idega.presentation.Layer;
import com.idega.presentation.Script;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.CheckBox;
import com.idega.slide.business.IWSlideService;
import com.idega.user.data.User;
import com.idega.util.CoreConstants;
import com.idega.util.CoreUtil;
import com.idega.util.IWTimestamp;
import com.idega.util.PresentationUtil;
import com.idega.util.StringUtil;
import com.idega.util.expression.ELUtil;

public class CommentsViewer extends Block {
	
	public static final String FEED_IMAGE = "images/feed.png";
	public static final String DELETE_IMAGE = "images/comments_delete.png";
	private static final String COMMENTS_BLOCK_ID = "comments_block";
	private static final String SHOW_COMMENTS_PROPERTY = "showCommentsForAllUsers";
	
	private String styleClass = "content_item_comments_style";
	private String linkToComments = null;
	private String springBeanIdentifier;
	private String identifier; 
	
	private String moduleId = null;
	
	private boolean showCommentsForAllUsers = true;
	private boolean showCommentsList = true; // If expand list on page load
	private boolean isForumPage = false;
	private boolean usedInArticleList = false;
	private boolean showViewController = true;
	private boolean newestEntriesOnTop = false;
	
	private String COMMENTS_ENGINE = "/dwr/interface/CommentsEngine.js";
	private String COMMENTS_HELPER = "javascript/ArticleCommentsHelper.js";
	private String INIT_COMMENTS_ACTION = "initComments();";
	private String INIT_SCRIPT_LINE = "window.addEvent('load', function() {"+INIT_COMMENTS_ACTION+"});";
//	private String ENABLE_REVERSE_AJAX_ACTION = "enableReverseAjax();";
//	private String ENABLE_REVERSE_AJAX_SCRIPT_LINE = "window.addEvent('load', function() {"+ENABLE_REVERSE_AJAX_ACTION+"});";
	
	private static final String SEPARATOR = "', '";
	
	private CommentsEngine commentsEngine = null;
	
	@Override
	public void main(IWContext iwc) {
		getCommentsEngine(iwc);
		if (commentsEngine == null) {
			return;
		}
		
		boolean hasValidRights = isCommentsViewerVisible(iwc);
		if (!hasValidRights) {
			return;
		}
		
		getModuleId(iwc);
		
		if (linkToComments == null) {
			if (StringUtil.isEmpty(springBeanIdentifier)) {
				if (!findLinkToComments(iwc.getParameter(ContentViewer.PARAMETER_CONTENT_RESOURCE),
						iwc.getParameter(ContentConstants.CONTENT_ITEM_VIEWER_IDENTIFIER_PARAMETER))) {
					if (isStandAlone(iwc)) {
						linkToComments = commentsEngine.getFixedCommentsUri(iwc, null, moduleId);
					}
					else {
						return;
					}
				}
			}
			else {
				CommentsPersistenceManager commentsManager = commentsEngine.getCommentsManager(springBeanIdentifier);
				linkToComments = commentsManager == null ? null : commentsManager.getLinkToCommentsXML(identifier);
			}
		}
		if (StringUtil.isEmpty(linkToComments)) {
			return;
		}
		
		ThemesHelper helper = ThemesHelper.getInstance();
		IWBundle bundle = getBundle(iwc);
		IWResourceBundle iwrb = bundle.getResourceBundle(iwc);
		
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
//				actions.add(ENABLE_REVERSE_AJAX_ACTION);
				container.add(PresentationUtil.getJavaScriptActions(actions));
			}
			else {
				PresentationUtil.addJavaScriptSourcesLinesToHeader(iwc, getJavaScriptSources(iwc));
				PresentationUtil.addJavaScriptActionsToBody(iwc, getJavaScriptActions());
			}
		}
		
		int commentsCount = getCommentsCount(iwc);
		String linkToAtomFeedImage = bundle.getVirtualPathWithFileNameString(FEED_IMAGE);
		
		if (!isUsedInArticleList()) {
			String springBean = getSpringBeanIdentifier() == null ? CoreConstants.EMPTY : getSpringBeanIdentifier();
			String identifier = getIdentifier() == null ? CoreConstants.EMPTY : getIdentifier();
			
			// JavaScript
			Script script = new Script();
			StringBuffer action = new StringBuffer("window.addEvent('domready', function(){setCommentStartInfo('");
			action.append(linkToComments).append(SEPARATOR).append(commentsId).append("', ").append(showCommentsList).append(", ").append(isNewestEntriesOnTop())
			.append(", '").append(springBean).append(SEPARATOR).append(identifier).append("')});");
			script.addScriptLine(action.toString());
			container.add(script);
		}
		
		// Enable comments container
		addEnableCommentsCheckboxContainer(iwc, container, hasValidRights, null);
		
		// Comments label
		Layer articleComments = new Layer();
		articleComments.setId(new StringBuffer(commentsId).append("article_comments_link_label_container").toString());
		
		Link link = new Link(new StringBuffer(iwrb.getLocalizedString("comments_viewer.comments", "Comments")).append("(").append(commentsCount).append(")")
				.toString(), "javascript:void(0)");
		link.setId(commentsId + "CommentsLabelWithCount");
		StringBuffer getCommentsAction = new StringBuffer("getCommentsList('").append(linkToComments).append(SEPARATOR);
		getCommentsAction.append(commentsId).append("'); return false;");
		link.setOnClick(getCommentsAction.toString());
		articleComments.add(link);
		
		// Link - Atom feed
		Image atom = new Image(linkToAtomFeedImage, iwrb.getLocalizedString("comments_viewer.atom_feed", "Atom feed"));
		Link linkToFeed = new Link();
		linkToFeed.setStyleClass("articleCommentsAtomFeedLinkStyle");
		linkToFeed.setId(new StringBuffer(commentsId).append("article_comments_link_to_feed").toString());
		linkToFeed.setImage(atom);
		makeCommentsFeedIfNotExists(iwc);
		linkToFeed.setURL(helper.getFullServerName(iwc) + CoreConstants.WEBDAV_SERVLET_URI + linkToComments);
		articleComments.add(linkToFeed);
		
		// Delete comments image
		if (iwc.isSuperAdmin()) {
			addSimpleSpace(articleComments);
			Image delete = new Image(bundle.getVirtualPathWithFileNameString(DELETE_IMAGE),
					iwrb.getLocalizedString("comments_viewer.delete_all_comments", "Delete all comments"));
			delete.setStyleClass("deleteCommentsImage");
			delete.setId(new StringBuffer(commentsId).append("delete_article_comments").toString());
			StringBuffer deleteAction = new StringBuffer("deleteComments('").append(commentsId).append("', null, '");
			deleteAction.append(linkToComments).append("', ").append(isNewestEntriesOnTop()).append("); return false;");
			delete.setOnClick(deleteAction.toString());
			articleComments.add(delete);
		}
		
		container.add(articleComments);
		
		// Add comment block
		container.add(getAddCommentBlock(iwc, commentsId));
	}
	
	private boolean isCommentsViewerVisible(IWContext iwc) {
		if (StringUtil.isEmpty(springBeanIdentifier)) {
			boolean hasValidRights = ContentUtil.hasContentEditorRoles(iwc);
			if (!hasValidRights && !showCommentsForAllUsers) {
				return false;
			}
			
			return true;
		}
		
		CommentsPersistenceManager commentsManager = commentsEngine.getCommentsManager(getSpringBeanIdentifier());
		return commentsManager == null ? false : commentsManager.hasRightsToViewComments(identifier);
	}
	
	private void makeCommentsFeedIfNotExists(IWContext iwc) {
		IWSlideService slide = null;
		try {
			slide = (IWSlideService) IBOLookup.getServiceInstance(iwc, IWSlideService.class);
		} catch (IBOLookupException e) {
			e.printStackTrace();
		}
		if (slide == null) {
			return;
		}
		
		boolean makeEmptyComments = false;
		WebdavResource commentsFeed = null;
		try {
			commentsFeed = slide.getWebdavResourceAuthenticatedAsRoot(CoreConstants.WEBDAV_SERVLET_URI + linkToComments);
		} catch (Exception e) {
			makeEmptyComments = true;
		}
		if (commentsFeed == null) {
			makeEmptyComments = true;
		}
		if (!makeEmptyComments) {
			makeEmptyComments = !commentsFeed.getExistence();
		}
		
		if (!makeEmptyComments) {
			return;
		}
		
		CommentsEngine commentsEngine = getCommentsEngine(iwc);
		if (commentsEngine == null) {
			return;
		}
		
		String user = getResourceBundle(iwc).getLocalizedString("comments_viewer.anonymous", "Anonymous");
		User currentUser = null;
		try {
			currentUser = iwc.getCurrentUser();
		} catch(Exception e) {}
		if (currentUser != null) {
			user = currentUser.getName();
		}
		
		String feedTitle = null;
		String feedSubtitle = null;
		CommentsPersistenceManager commentsManager = commentsEngine.getCommentsManager(getSpringBeanIdentifier());
		if (commentsManager != null) {
			feedTitle = commentsManager.getFeedTitle(iwc, identifier);
			feedSubtitle = commentsManager.getFeedSubtitle(iwc, identifier);
		}
		if (StringUtil.isEmpty(feedTitle)) {
			IWResourceBundle iwrb = getResourceBundle(iwc);
			feedTitle = iwrb.getLocalizedString("comments_viewer.article_comments", "Comments of Article");
			feedSubtitle = iwrb.getLocalizedString("comments_viewer.all_article_comments", "All comments");
		}
		commentsEngine.initCommentsFeed(iwc, linkToComments, user, IWTimestamp.getTimestampRightNow(), ThemesHelper.getInstance().getCurrentLanguage(iwc),
				feedTitle, feedSubtitle, commentsManager);
	}
	
	protected List<String> getJavaScriptSources(IWContext iwc) {
		List<String> sources = new ArrayList<String>();
		sources.add(COMMENTS_ENGINE);
		sources.add(CoreConstants.DWR_ENGINE_SCRIPT);
		sources.add(getBundle(iwc).getVirtualPathWithFileNameString(COMMENTS_HELPER));
		Web2Business web2 = ELUtil.getInstance().getBean(Web2Business.class);
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
//		actions.add(ENABLE_REVERSE_AJAX_SCRIPT_LINE);
		return actions;
	}
		
	private void addSimpleSpace(Layer container) {
		//	Simple space
		container.add(new Text(ContentConstants.SPACE));
	}
	
	@SuppressWarnings("unchecked")
	private boolean findLinkToComments(String resourcePathFromRequest, String viewerIdentifier) {
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
				
				UIComponent nextItem = null;	//	CommentsViewer is next to ArticleItemViewer
				if (i + 1 < children.size()) {
					nextItem = children.get(i + 1);
				}
				if (nextItem instanceof CommentsViewer && nextItem.equals(this)) {
					if (canInitComments(articleViewer, resourcePathFromRequest, viewerIdentifier)) {
						linkToComments = articleViewer.getLinkToComments();
					}
				}
			}
		}
		if (linkToComments == null) {
			return false;
		}
		return true;
	}
	
	private boolean canInitComments(ArticleItemViewer articleViewer, String resourcePathFromRequest, String viewerIdentifier) {
		if (!articleViewer.isCanInitAnyField()) {
			return false;
		}
		
		if (viewerIdentifier != null && !viewerIdentifier.equals(articleViewer.getArticleItemViewerFilter())) {
			return false;
		}
		
		return resourcePathFromRequest == null ? true : resourcePathFromRequest.equals(articleViewer.getResourcePath());
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
		if (!showViewController || !hasValidRights || isUsedInArticleList()) {
			return;
		}
		
		container.add(getCommentsController(iwc, cacheKey, moduleId, isShowCommentsForAllUsers(), SHOW_COMMENTS_PROPERTY));
	}
	
	protected Layer getCommentsController(IWContext iwc, String cacheKey, String moduleId, boolean enabled, String propertyName) {
		Layer commentsController = new Layer();
		if (iwc == null || propertyName == null) {
			return commentsController;
		}
		commentsController.setStyleClass("commentsController");
		
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
		IWResourceBundle iwrb = getResourceBundle(iwc);
		
		Layer addComments = new Layer();
		addComments.setId(new StringBuffer(commentsId).append("add_comment_block").toString());
		Link label = new Link(iwrb.getLocalizedString("comments_viewer.add_your_comment", "Add your comment"), "javascript:void(0)");
		String user = iwrb.getLocalizedString("comments_viewer.name", "Name");
		String subject = iwrb.getLocalizedString("comments_viewer.subject", "Subject");
		String comment = iwrb.getLocalizedString("comments_viewer.comment_body", "Comment");
		String posted = iwrb.getLocalizedString("comments_viewer.posted", "Posted");
		String send = iwrb.getLocalizedString("comments_viewer.send", "Send");
		String sending = iwrb.getLocalizedString("comments_viewer.sending", "Sending...");
		String loggedUser = null;
		try {
			loggedUser = iwc.getCurrentUser().getName();
		} catch (NotLoggedOnException e) {
			loggedUser = iwrb.getLocalizedString("anonymous", "Anonymous");
		}
		
		StringBuilder action = new StringBuilder("addCommentPanel('").append(addComments.getId()).append(SEPARATOR).append(linkToComments).append(SEPARATOR)
			.append(user).append(SEPARATOR).append(subject).append(SEPARATOR).append(comment).append(SEPARATOR).append(posted).append(SEPARATOR).append(send)
			.append(SEPARATOR).append(sending).append(SEPARATOR).append(loggedUser).append(SEPARATOR)
			.append(iwrb.getLocalizedString("comments_viewer.email", "Email")).append(SEPARATOR)
			.append(iwrb.getLocalizedString("comments_viewer.comment_form", "Comment form")).append("', ").append(isForumPage).append(", '").append(commentsId)
			.append(SEPARATOR).append(moduleId).append("', ").append(StringUtil.isEmpty(springBeanIdentifier) ? "null" : new StringBuilder("'")
			.append(springBeanIdentifier).append("'").toString()).append(", ").append(StringUtil.isEmpty(identifier) ? "null" : new StringBuilder("'")
			.append(identifier).append("', ").append(newestEntriesOnTop).toString()).append("); return false;");
		label.setOnClick(action.toString());
		addComments.add(label);
		return addComments;
	}
	
	private CommentsEngine getCommentsEngine(IWApplicationContext iwac) {
		if (commentsEngine == null) {
			try {
				commentsEngine = (CommentsEngine) IBOLookup.getServiceInstance(iwac, CommentsEngine.class);
			} catch (IBOLookupException e) {
				e.printStackTrace();
			}
		}
		return commentsEngine;
	}
	
	private int getCommentsCount(IWContext iwc) {
		CommentsEngine comments = getCommentsEngine(iwc);
		if (comments == null) {
			return 0;
		}
		
		try {
			return comments.getCommentsCount(linkToComments, getSpringBeanIdentifier(), getIdentifier(), iwc);
		} catch (RemoteException e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	@Override
	public Object saveState(FacesContext context) {
		Object values[] = new Object[11];
		values[0] = super.saveState(context);
		values[1] = linkToComments;
		values[2] = styleClass;
		values[3] = showCommentsList;
		values[4] = isForumPage;
		values[5] = showCommentsForAllUsers;
		values[6] = usedInArticleList;
		values[7] = springBeanIdentifier;
		values[8] = identifier;
		values[9] = showViewController;
		values[10] = newestEntriesOnTop;
		return values;
	}

	@Override
	public void restoreState(FacesContext context, Object state) {
		Object values[] = (Object[]) state;
		super.restoreState(context, values[0]);
		linkToComments = values[1].toString();
		styleClass = values[2].toString();
		showCommentsList = (Boolean) values[3];
		isForumPage = (Boolean) values[4];
		showCommentsForAllUsers = (Boolean) values[5];
		usedInArticleList = (Boolean) values[6];
		springBeanIdentifier = values[7] == null ? null : values[7].toString();
		identifier = values[8] == null ? null : values[8].toString();
		showViewController = (Boolean) values[9];
		newestEntriesOnTop = (Boolean) values[10];
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

	@Override
	public String getStyleClass() {
		return styleClass;
	}

	@Override
	public void setStyleClass(String styleClass) {
		this.styleClass = styleClass;
	}

	public String getLinkToComments() {
		return linkToComments;
	}

	public void setLinkToComments(String linkToComments) {
		this.linkToComments = linkToComments;
	}
	
	@Override
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
	
	@Override
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

	public String getSpringBeanIdentifier() {
		return springBeanIdentifier;
	}

	public void setSpringBeanIdentifier(String springBeanIdentifier) {
		this.springBeanIdentifier = springBeanIdentifier;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public boolean isShowViewController() {
		return showViewController;
	}

	public void setShowViewController(boolean showViewController) {
		this.showViewController = showViewController;
	}

	public boolean isNewestEntriesOnTop() {
		return newestEntriesOnTop;
	}

	public void setNewestEntriesOnTop(boolean newestEntriesOnTop) {
		this.newestEntriesOnTop = newestEntriesOnTop;
	}

}
