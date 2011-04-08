package com.idega.block.article.component;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import org.springframework.beans.factory.annotation.Autowired;

import com.idega.block.article.bean.CommentsViewerProperties;
import com.idega.block.article.business.ArticleConstants;
import com.idega.block.article.business.ArticleUtil;
import com.idega.block.article.business.CommentsEngine;
import com.idega.block.article.business.CommentsPersistenceManager;
import com.idega.block.rss.business.RSSBusiness;
import com.idega.block.web2.business.JQuery;
import com.idega.block.web2.business.Web2Business;
import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.content.business.ContentConstants;
import com.idega.content.business.ContentUtil;
import com.idega.content.presentation.ContentViewer;
import com.idega.content.themes.helpers.business.ThemesHelper;
import com.idega.core.accesscontrol.business.NotLoggedOnException;
import com.idega.core.builder.business.BuilderService;
import com.idega.core.contact.data.Email;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.idegaweb.IWUserContext;
import com.idega.presentation.Block;
import com.idega.presentation.IWContext;
import com.idega.presentation.Image;
import com.idega.presentation.Layer;
import com.idega.presentation.text.Link;
import com.idega.presentation.ui.CheckBox;
import com.idega.presentation.ui.Label;
import com.idega.user.business.NoEmailFoundException;
import com.idega.user.business.UserBusiness;
import com.idega.user.data.User;
import com.idega.util.CoreConstants;
import com.idega.util.CoreUtil;
import com.idega.util.IWTimestamp;
import com.idega.util.PresentationUtil;
import com.idega.util.StringUtil;
import com.idega.util.expression.ELUtil;

public class CommentsViewer extends Block {

	private static final String FEED_IMAGE = "images/feed.png";
	private static final String DELETE_IMAGE = "images/comments_delete.png";
	private static final String DELETE_COMMENT_IMAGE = "images/comment_delete.png";

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
	private boolean addLoginbyUUIDOnRSSFeedLink = false;

	private String COMMENTS_ENGINE = "/dwr/interface/CommentsEngine.js";
	private String COMMENTS_HELPER = "javascript/ArticleCommentsHelper.js";

	private static final String SEPARATOR = "', '";

	private CommentsEngine commentsEngine = null;

	private boolean fullCommentsRights;

	@Autowired
	private JQuery jQuery;
	@Autowired
	private Web2Business web2;

	public static final String AUTO_SHOW_COMMENTS = "autoShowComments";

	@Override
	public void main(IWContext iwc) {
		ELUtil.getInstance().autowire(this);

		getCommentsEngine(iwc);
		if (commentsEngine == null) {
			return;
		}

		boolean hasValidRights = isCommentsViewerVisible(iwc);
		if (!hasValidRights) {
			return;
		}

		if (iwc.isParameterSet(CommentsViewer.AUTO_SHOW_COMMENTS)) {
			setShowCommentsList(iwc.getParameter(CommentsViewer.AUTO_SHOW_COMMENTS).equals(Boolean.TRUE.toString()));
		}

		PresentationUtil.addStyleSheetsToHeader(iwc, Arrays.asList(
				ArticleUtil.getBundle().getVirtualPathWithFileNameString("style/article.css"),
				getWeb2().getBundleUriToHumanizedMessagesStyleSheet()
		));

		resolveModuleId(iwc);

		if (linkToComments == null) {
			if (StringUtil.isEmpty(springBeanIdentifier)) {
				if (!findLinkToComments(iwc.getParameter(ContentViewer.PARAMETER_CONTENT_RESOURCE),
						iwc.getParameter(ContentConstants.CONTENT_ITEM_VIEWER_IDENTIFIER_PARAMETER))) {
					if (isStandAlone(iwc)) {
						linkToComments = commentsEngine.getFixedCommentsUri(null, moduleId, iwc.getRequestURI());
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

		IWBundle bundle = getBundle(iwc);
		IWResourceBundle iwrb = bundle.getResourceBundle(iwc);

		String commentsId = moduleId;

		Layer container = new Layer();
		container.setId(new StringBuffer(commentsId).append(COMMENTS_BLOCK_ID).toString());
		container.setStyleClass(styleClass);
		add(container);

		if (isUsedInArticleList()) {
			showCommentsList = false;
		}

		List<String> jsFiles = getJavaScriptSources(iwc);
		jsFiles.add(getJQuery().getBundleURIToJQueryLib());
		jsFiles.add(getWeb2().getBundleUriToHumanizedMessagesScript());

		int commentsCount = getCommentsCount(iwc);

		boolean contentEditor = ContentUtil.hasContentEditorRoles(iwc);
		if (contentEditor) {
			// Enable comments container
			addEnableCommentsCheckboxContainer(iwc, container);
		}

		// Comments label
		Layer comments = new Layer();
		comments.setId(new StringBuffer(commentsId).append("article_comments_link_label_container").toString());
		container.add(comments);

		Link link = new Link(new StringBuffer(iwrb.getLocalizedString("comments_viewer.comments", "Comments")).append("(").append(commentsCount).append(")")
				.toString(), "javascript:void(0)");
		link.setId(commentsId + "CommentsLabelWithCount");
		link.setOnClick(new StringBuilder("getCommentsList('").append(linkToComments).append(SEPARATOR).append(commentsId).append("');").toString());
		link.setStyleClass("view_comments_link");
		comments.add(link);

		RSSBusiness rss = null;
		if (isAddLoginbyUUIDOnRSSFeedLink()) {
			try {
				rss = IBOLookup.getServiceInstance(iwc, RSSBusiness.class);
			} catch (IBOLookupException e) {
				e.printStackTrace();
			}
			if (rss == null) {
				return;
			}
		}

		boolean addAtomLink = true;
		CommentsPersistenceManager manager = getCommentsEngine(iwc).getCommentsManager(springBeanIdentifier);
		if (manager != null) {
			fullCommentsRights = manager.hasFullRightsForComments(identifier);
			addAtomLink = fullCommentsRights;

			if (fullCommentsRights) {
				PresentationUtil.addStyleSheetToHeader(iwc, getWeb2().getBundleURIToFancyBoxStyleFile());
				jsFiles.addAll(getWeb2().getBundleURIsToFancyBoxScriptFiles());
			}
		}

		if (CoreUtil.isSingleComponentRenderingProcess(iwc)) {
			container.add(PresentationUtil.getJavaScriptSourceLines(jsFiles));
		}
		else {
			PresentationUtil.addJavaScriptSourcesLinesToHeader(iwc, jsFiles);
		}

		// Link - Atom feed
		if (addAtomLink) {
			Link linkToFeed = new Link(CoreConstants.SPACE);
			linkToFeed.setTitle(iwrb.getLocalizedString("comments_viewer.atom_feed", "Atom feed"));
			linkToFeed.setStyleClass("articleCommentsAtomFeedLinkStyle");
			makeCommentsFeedIfNotExists(iwc);
			String uri = new StringBuilder().append(CoreConstants.WEBDAV_SERVLET_URI)
				.append(isAddLoginbyUUIDOnRSSFeedLink() ? rss.getLinkToFeedWithUUIDParameters(linkToComments, getUser(iwc)) : linkToComments).toString();
			linkToFeed.setURL(uri);
			comments.add(linkToFeed);
		}

		// Delete comments image
		if (contentEditor || fullCommentsRights) {
			Image delete = new Image(bundle.getVirtualPathWithFileNameString(DELETE_IMAGE),
					iwrb.getLocalizedString("comments_viewer.delete_all_comments", "Delete all comments"));
			delete.setStyleClass("deleteCommentsImage");
			delete.setId(new StringBuffer(commentsId).append("delete_article_comments").toString());
			StringBuffer deleteAction = new StringBuffer("deleteComments('").append(commentsId).append("', null, '");
			deleteAction.append(linkToComments).append("', ").append(isNewestEntriesOnTop()).append(");");
			delete.setOnClick(deleteAction.toString());
			comments.add(delete);
		}

		// Add comment block
		CommentsViewerProperties properties = new CommentsViewerProperties();
		properties.setIdentifier(identifier);
		boolean canWriteComments = manager == null || manager.canWriteComments(properties);
		if (canWriteComments) {
			container.add(getAddCommentBlock(iwc, commentsId));
		}

		addInitInfo(iwc, container);
	}

	private User getUser(IWContext iwc) {
		User currentUser = iwc.isLoggedOn() ? iwc.getCurrentUser() : null;
		if (currentUser == null) {
			return null;
		}

		if (StringUtil.isEmpty(getSpringBeanIdentifier())) {
			return currentUser;
		}

		return commentsEngine.getCommentsManager(getSpringBeanIdentifier()).getUserAvailableToReadWriteCommentsFeed(iwc);
	}

	private void addInitInfo(IWContext iwc, Layer container) {
		IWBundle bundle = getBundle(iwc);
		IWResourceBundle iwrb = bundle.getResourceBundle(iwc);

		StringBuilder localization = new StringBuilder("CommentsViewer.setLocalization({posted: '")
							.append(iwrb.getLocalizedString("comments_viewer.posted", "Posted")).append("', ")
							.append("loadingComments: '")
							.append(iwrb.getLocalizedString("comments_viewer.loading_comments", "Loading comments..."))
							.append("', atomLink: '")
							.append(iwrb.getLocalizedString("comments_viewer.atom_feed", "Atom Feed"))
							.append("', addNotification: '")
							.append(iwrb.getLocalizedString("comments_viewer.need_send_notification", "Do you wish to receive notifications about new comments?"))
							.append("', yes: '")
							.append(iwrb.getLocalizedString("yes", "Yes"))
							.append("', no: '")
							.append(iwrb.getLocalizedString("no", "No"))
							.append("', enterEmail: '")
							.append(iwrb.getLocalizedString("comments_viewer.enter_email_text", "Please enter your e-mail!"))
							.append("', saving: '")
							.append(iwrb.getLocalizedString("comments_viewer.saving", "Saving..."))
							.append("', deleting: '")
							.append(iwrb.getLocalizedString("comments_viewer.deleting", "Deleting..."))
							.append("', areYouSure: '")
							.append(iwrb.getLocalizedString("are_you_sure", "Are you sure?"))
							.append("', deleteComments: '")
							.append(iwrb.getLocalizedString("comments_viewer.delete_all_comments", "Delete comments"))
							.append("', deleteComment: '")
							.append(iwrb.getLocalizedString("comments_viewer.delete_comment", "Delete this comment"))
							.append("', publishComment: '")
							.append(iwrb.getLocalizedString("comments_viewer.publish_comment", "Publish comment"))
							.append("', unPublishComment: '")
							.append(iwrb.getLocalizedString("comments_viewer.un_publish_comment", "Un-publish comment"))
							.append("', commentWasPublished: '")
							.append(iwrb.getLocalizedString("comments_viewer.comment_was_published", "Comment was successfully published"))
							.append("', commentWasUnPublished: '")
							.append(iwrb.getLocalizedString("comments_viewer.comment_was_un_published", "Comment was successfully un-published"))
							.append("', readComment: '")
							.append(iwrb.getLocalizedString("comments_viewer.mark_as_read_comment", "Mark as read"))
							.append("', commentWasRead: '")
							.append(iwrb.getLocalizedString("comments_viewer.comment_was_marked_as_read", "Comment was successfully marked as read"))
							.append("', reply: '")
							.append(iwrb.getLocalizedString("comments_viewer.reply_to_comment", "Reply to comment"))
							.append("', replyFor: '")
							.append(iwrb.getLocalizedString("comments_viewer.reply_for", "Reply for"))
							.append("', replyForMessage: '")
							.append(iwrb.getLocalizedString("comments_viewer.reply_for_message", "Reply for message"))
							.append("', commentRedBy: '")
							.append(iwrb.getLocalizedString("comments_viewer.comment_red_by", "Comment was red by"))
							.append("', commentAttachments: '")
							.append(iwrb.getLocalizedString("comments_viewer.comment_attachments", "Attachments"))
							.append("', commentAttachmentDownloadInfo: '")
							.append(iwrb.getLocalizedString("comments_viewer.comment_attachment_download_info", "Download statistics"))
		.append("'});");
		PresentationUtil.addJavaScriptActionToBody(iwc, localization.toString());

		StringBuilder info = new StringBuilder("CommentsViewer.setStartInfo({commentsServer: '")
							.append(getThemesHelper().getFullServerName(iwc) + CoreConstants.WEBDAV_SERVLET_URI)
							.append("', feedImage: '")
							.append(bundle.getVirtualPathWithFileNameString(CommentsViewer.FEED_IMAGE))
							.append("', deleteImage: '")
							.append(bundle.getVirtualPathWithFileNameString(CommentsViewer.DELETE_IMAGE))
							.append("', deleteCommentImage: '")
							.append(bundle.getVirtualPathWithFileNameString(CommentsViewer.DELETE_COMMENT_IMAGE))
							.append("', hasValidRights: ").append(ContentUtil.hasContentEditorRoles(iwc) || fullCommentsRights)
		.append("});");
		PresentationUtil.addJavaScriptActionToBody(iwc, info.toString());

		String springBean = getSpringBeanIdentifier() == null ? CoreConstants.EMPTY : getSpringBeanIdentifier();
		String identifier = getIdentifier() == null ? CoreConstants.EMPTY : getIdentifier();
		StringBuilder action = new StringBuilder("addCommentStartInfo('").append(linkToComments).append(SEPARATOR).append(moduleId).append("', ")
		.append(showCommentsList).append(", ").append(isNewestEntriesOnTop()).append(", '").append(springBean).append(SEPARATOR).append(identifier)
		.append("', ").append(isAddLoginbyUUIDOnRSSFeedLink()).append(", ").append(fullCommentsRights).append(");");
		if (!CoreUtil.isSingleComponentRenderingProcess(iwc)) {
			action = new StringBuilder("window.addEvent('load', function() {").append(action.toString()).append("});");
		}
		container.add(PresentationUtil.getJavaScriptAction(action.toString()));
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
		boolean makeEmptyComments = false;
		try {
			makeEmptyComments = !getRepositoryService().getExistence(linkToComments);
		} catch (Exception e) {
			e.printStackTrace();
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
		commentsEngine.initCommentsFeed(iwc, linkToComments, user, IWTimestamp.getTimestampRightNow(), getThemesHelper().getCurrentLanguage(iwc),
				feedTitle, feedSubtitle, commentsManager);
	}

	private ThemesHelper getThemesHelper() {
		return ELUtil.getInstance().getBean(ThemesHelper.class);
	}

	protected List<String> getJavaScriptSources(IWContext iwc) {
		List<String> sources = new ArrayList<String>();
		sources.add(COMMENTS_ENGINE);
		sources.add(CoreConstants.DWR_ENGINE_SCRIPT);
		sources.add(getBundle(iwc).getVirtualPathWithFileNameString(COMMENTS_HELPER));
		try {
			sources.add(getWeb2().getBundleURIToMootoolsLib());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sources;
	}

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

	private void addEnableCommentsCheckboxContainer(IWContext iwc, Layer container) {
		if (!showViewController || isUsedInArticleList()) {
			return;
		}

		container.add(getCommentsController(iwc, null, moduleId, isShowCommentsForAllUsers(), SHOW_COMMENTS_PROPERTY));
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

		Layer layer = new Layer();
		layer.setStyleClass("commentsControllerInputs");

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

		Label label = new Label(getBundle(iwc).getLocalizedString("enable_comments"), enableCheckBox);

		layer.add(enableCheckBox);
		layer.add(label);
		commentsController.add(layer);

		return commentsController;
	}

	private UIComponent getAddCommentBlock(IWContext iwc, String commentsId) {
		IWResourceBundle iwrb = getResourceBundle(iwc);

		Layer addComments = new Layer();
		addComments.setId(new StringBuffer(commentsId).append("add_comment_block").toString());
		Link label = new Link(iwrb.getLocalizedString("comments_viewer.add_your_comment", "Add your comment"), "javascript:void(0)");
		label.setStyleClass("addCommentFormLinkInCommentsViewer");
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
			.append(identifier).append("'").toString()).append(", ").append(newestEntriesOnTop).append(", ").append(getUsersEmail(iwc))
			.append(");");
		label.setOnClick(action.toString());
		addComments.add(label);
		return addComments;
	}

	private String getUsersEmail(IWContext iwc) {
		String emailAddress = "null";

		User currentUser = null;
		try {
			currentUser = iwc.getCurrentUser();
		} catch(NotLoggedOnException e) {}
		if (currentUser == null) {
			return emailAddress;
		}

		UserBusiness userBusiness = null;
		try {
			userBusiness = IBOLookup.getServiceInstance(iwc, UserBusiness.class);
		} catch (IBOLookupException e) {
			e.printStackTrace();
		}
		if (userBusiness == null) {
			return emailAddress;
		}

		Email email = null;
		try {
			email = userBusiness.getUsersMainEmail(currentUser);
		} catch (RemoteException e) {
		} catch (NoEmailFoundException e) {
		}
		if (email == null) {
			return emailAddress;
		}
		emailAddress = email.getEmailAddress();
		if (StringUtil.isEmpty(emailAddress)) {
			return "null";
		}

		return new StringBuilder("'").append(emailAddress).append("'").toString();
	}

	private CommentsEngine getCommentsEngine(IWApplicationContext iwac) {
		if (commentsEngine == null) {
			try {
				commentsEngine = IBOLookup.getServiceInstance(iwac, CommentsEngine.class);
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
			return comments.getCommentsCount(linkToComments, getSpringBeanIdentifier(), getIdentifier(), iwc, isAddLoginbyUUIDOnRSSFeedLink());
		} catch (RemoteException e) {
			e.printStackTrace();
			return 0;
		}
	}

	@Override
	public Object saveState(FacesContext context) {
		Object values[] = new Object[12];
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
		values[11] = addLoginbyUUIDOnRSSFeedLink;
		return values;
	}

	@Override
	public void restoreState(FacesContext context, Object state) {
		Object values[] = (Object[]) state;
		super.restoreState(context, values[0]);
		linkToComments = values[1] == null ? null : values[1].toString();
		styleClass = values[2] == null ? null : values[2].toString();
		showCommentsList = (Boolean) values[3];
		isForumPage = (Boolean) values[4];
		showCommentsForAllUsers = (Boolean) values[5];
		usedInArticleList = (Boolean) values[6];
		springBeanIdentifier = values[7] == null ? null : values[7].toString();
		identifier = values[8] == null ? null : values[8].toString();
		showViewController = (Boolean) values[9];
		newestEntriesOnTop = (Boolean) values[10];
		addLoginbyUUIDOnRSSFeedLink = (Boolean) values[11];
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

	private void resolveModuleId(IWContext iwc) {
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

	public boolean isAddLoginbyUUIDOnRSSFeedLink() {
		return addLoginbyUUIDOnRSSFeedLink;
	}

	public void setAddLoginbyUUIDOnRSSFeedLink(boolean addLoginbyUUIDOnRSSFeedLink) {
		this.addLoginbyUUIDOnRSSFeedLink = addLoginbyUUIDOnRSSFeedLink;
	}

	public JQuery getJQuery() {
		if (jQuery == null) {
			ELUtil.getInstance().autowire(this);
		}
		return jQuery;
	}

	public void setJQuery(JQuery query) {
		jQuery = query;
	}

	public Web2Business getWeb2() {
		if (web2 == null) {
			ELUtil.getInstance().autowire(this);
		}
		return web2;
	}

	public void setWeb2(Web2Business web2) {
		this.web2 = web2;
	}

}
