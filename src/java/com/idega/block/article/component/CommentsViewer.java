package com.idega.block.article.component;

import java.rmi.RemoteException;
import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import com.idega.block.article.business.ArticleConstants;
import com.idega.block.article.business.ArticleUtil;
import com.idega.block.article.business.CommentsEngine;
import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.content.business.ContentConstants;
import com.idega.content.business.ContentUtil;
import com.idega.content.themes.helpers.ThemesHelper;
import com.idega.core.accesscontrol.business.NotLoggedOnException;
import com.idega.idegaweb.IWUserContext;
import com.idega.presentation.Block;
import com.idega.presentation.IWContext;
import com.idega.presentation.Image;
import com.idega.presentation.Page;
import com.idega.presentation.PresentationObjectUtil;
import com.idega.presentation.Script;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.CheckBox;
import com.idega.webface.WFDivision;

public class CommentsViewer extends Block {
	
	public static final String FEED_IMAGE = "/images/feed.png";
	public static final String DELETE_IMAGE = "/images/delete.png";
	private static final String COMMENTS_BLOCK_ID = "comments_block";
	private static final String SHOW_COMMENTS_PROPERTY = "showCommentsForAllUsers";
	
	private String styleClass = "content_item_comments_style";
	private String linkToComments = null;
	private boolean showCommentsForAllUsers = false;
	private boolean showCommentsList = false; // If expand list on page load
	private boolean isForumPage = false;
	private boolean usedInArticleList = false;
	
	protected static final String DWR_ENGINE = "/dwr/engine.js";
	protected static final String COMMENTS_ENGINE = "/dwr/interface/CommentsEngine.js";
	protected static final String COMMENTS_HELPER = "/javascript/ArticleCommentsHelper.js";
	protected static final String INIT_SCRIPT_LINE = "addEvent(window, 'load', initComments);";
	
	private static final String ARTICLE_COMMENT_SCOPE = "article_comment";
	private static final String SEPARATOR = "', '";
	
	public void main(IWContext iwc) {
		if (linkToComments == null) {
			if (!findLinkToComments()) {
				return;
			}
		}
		
		String commentsId = new StringBuffer("unique")
										.append(ThemesHelper.getInstance().getUniqueIdByNumberAndDate(ARTICLE_COMMENT_SCOPE)).toString();
		
		WFDivision container = new WFDivision();
		container.setId(COMMENTS_BLOCK_ID);
		container.setStyleClass(styleClass);
		this.add(container);
		
		if (!isUsedInArticleList()) {
			Page page = PresentationObjectUtil.getParentPage(this);
			if (page == null) {
				return;
			}
			page.addJavascriptURL(COMMENTS_ENGINE);
			page.addJavascriptURL(DWR_ENGINE);
			page.addJavascriptURL(ArticleUtil.getBundle().getResourcesPath() + COMMENTS_HELPER);
			
			Script onLoadScript = new Script();
			onLoadScript.addScriptLine(INIT_SCRIPT_LINE);
			container.add(onLoadScript);
		}
		
		boolean hasValidRights = ContentUtil.hasContentEditorRoles(iwc);
		
		if (!hasValidRights && !showCommentsForAllUsers) {
			return;
		}
		
		int commentsCount = getCommentsCount(iwc);
		StringBuffer linkToAtomFeedImage = new StringBuffer(ArticleUtil.getBundle().getResourcesPath());
		linkToAtomFeedImage.append(FEED_IMAGE);
		
		if (!isUsedInArticleList()) {
			// JavaScript
			Script script = new Script();
			StringBuffer action = new StringBuffer("setCommentStartInfo('");
			action.append(linkToComments).append(SEPARATOR).append(commentsId).append("', ").append(showCommentsList).append(");");
			script.addScriptLine(action.toString());
			container.add(script);
		}
		
		// Enable comments container
		addEnableCommentsCheckboxContainer(iwc, container, hasValidRights);
		
		// Comments label
		WFDivision articleComments = new WFDivision();
		articleComments.setId(new StringBuffer(commentsId).append("article_comments_link_label_container").toString());
		StringBuffer comments = new StringBuffer(ArticleUtil.getBundle().getLocalizedString("comments"));
		comments.append(ContentConstants.SPACE).append("(<span id='").append(commentsId);
		comments.append("contentItemCount' class='contentItemCountStyle'>").append(commentsCount).append("</span>)");
		Link commentsLabel = new Link(comments.toString(), "#showCommentsList");
		StringBuffer getCommentsAction = new StringBuffer("getCommentsList('").append(linkToComments).append(SEPARATOR);
		getCommentsAction.append(commentsId).append("')");
		commentsLabel.setOnClick(getCommentsAction.toString());
		articleComments.add(commentsLabel);
		
		// Simple space
		addSimpleSpace(articleComments);
		
		// Link - Atom feed
		if (commentsCount > 0) {
			Image atom = new Image(linkToAtomFeedImage.toString(), ArticleUtil.getBundle().getLocalizedString("atom_feed"));
			Link linkToFeed = new Link();
			linkToFeed.setId(new StringBuffer(commentsId).append("article_comments_link_to_feed").toString());
			linkToFeed.setImage(atom);
			linkToFeed.setURL(ThemesHelper.getInstance().getFullServerName(iwc) + ContentConstants.CONTENT + linkToComments);
			articleComments.add(linkToFeed);
			
			// Delete comments image
			if (hasValidRights) {
				addSimpleSpace(articleComments);
				String deleteImage = new StringBuffer(ArticleUtil.getBundle().getResourcesPath()).append(DELETE_IMAGE).toString();
				Image delete = new Image(deleteImage, ArticleUtil.getBundle().getLocalizedString("delete_all_comments"));
				delete.setStyleClass("deleteCommentsImage");
				delete.setId(new StringBuffer(commentsId).append("delete_article_comments").toString());
				StringBuffer deleteAction = new StringBuffer("deleteComments('").append(commentsId).append("', null, '");
				deleteAction.append(linkToComments).append("');");
				delete.setOnClick(deleteAction.toString());
				articleComments.add(delete);
			}
		}
		
		container.add(articleComments);
		
		// Add comment block
		container.add(getAddCommentBlock(iwc, commentsId));
	}
		
	private void addSimpleSpace(WFDivision container) {
		//	Simple space
		container.add(new Text(ContentConstants.SPACE));
	}
	
	private boolean findLinkToComments() {
		UIComponent region = this.getParent();
		if (region == null) {
			return false;
		}
		List children = region.getChildren();
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
	
	private void addEnableCommentsCheckboxContainer(IWContext iwc, WFDivision container, boolean hasValidRights) {
		if (!hasValidRights) {
			return;
		}
		String pageKey = getThisPageKey(iwc);
		if (pageKey == null) {
			return;
		}
		String moduleId = this.getClientId(iwc);
		if (moduleId == null) {
			return;
		}
		WFDivision showCommentsContainer = new WFDivision();
		
		CheckBox enableCheckBox = new CheckBox("enableComments");
		enableCheckBox.setId("manageCommentsBlockCheckBox");
		StringBuffer action = new StringBuffer("enableComments(this.checked, '");
		action.append(pageKey).append(SEPARATOR).append(moduleId).append(SEPARATOR).append(SHOW_COMMENTS_PROPERTY);
		action.append("');");
		enableCheckBox.setOnClick(action.toString());
		enableCheckBox.setChecked(isShowCommentsForAllUsers());
		
		Text enableText = new Text(ArticleUtil.getBundle().getLocalizedString("enable_comments"));
		
		showCommentsContainer.add(enableText);
		showCommentsContainer.add(enableCheckBox);
		container.add(showCommentsContainer);
	}
	
	private UIComponent getAddCommentBlock(IWContext iwc, String commentsId) {
		WFDivision addComments = new WFDivision();
		addComments.setId("add_comment_block");
		Link label = new Link(ArticleUtil.getBundle().getLocalizedString("add_your_comment"), "#" + addComments.getId());
		String user = ArticleUtil.getBundle().getLocalizedString("name");
		String subject = ArticleUtil.getBundle().getLocalizedString("subject");
		String comment = ArticleUtil.getBundle().getLocalizedString("comment");
		String posted = ArticleUtil.getBundle().getLocalizedString("posted");
		String send = ArticleUtil.getBundle().getLocalizedString("send");
		String sending = ArticleUtil.getBundle().getLocalizedString("sending");
		String loggedUser = null;
		try {
			loggedUser = iwc.getCurrentUser().getName();
		} catch (NotLoggedOnException e) {
			loggedUser = ArticleUtil.getBundle().getLocalizedString("anonymous");
		}
		StringBuffer action = new StringBuffer("addCommentPanel('").append(addComments.getId()).append(SEPARATOR);
		action.append(linkToComments).append(SEPARATOR).append(user).append(SEPARATOR).append(subject).append(SEPARATOR);
		action.append(comment).append(SEPARATOR).append(posted).append(SEPARATOR).append(send).append(SEPARATOR);
		action.append(sending).append(SEPARATOR).append(loggedUser).append(SEPARATOR);
		action.append(ArticleUtil.getBundle().getLocalizedString("email")).append(SEPARATOR);
		action.append(ArticleUtil.getBundle().getLocalizedString("comment_form")).append("', ").append(isForumPage);
		action.append(", '").append(commentsId).append("');");
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
		String name = ArticleUtil.getBundle().getComponentName(CommentsViewer.class);
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

}
