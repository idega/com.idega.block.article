package com.idega.block.article.component;

import java.rmi.RemoteException;
import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import com.idega.block.article.business.ArticleUtil;
import com.idega.block.article.business.CommentsEngine;
import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.content.business.ContentConstants;
import com.idega.content.business.ContentUtil;
import com.idega.content.themes.helpers.ThemesHelper;
import com.idega.core.accesscontrol.business.NotLoggedOnException;
import com.idega.core.builder.business.BuilderService;
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
	private static final String COMMENTS_BLOCK_ID = "comments_block";
	
	private String styleClass = "content_item_comments_style";
	private String linkToComments = null;
	private boolean showCommentsForAllUsers = false;
	private boolean showCommentsList = false; // If expand list on page load
	private boolean isForumPage = false;
	
	public void main(IWContext iwc) {
		if (linkToComments == null) {
			if (!findLinkToComments()) {
				return;
			}
		}
		
		// JavaScript for DWR
		Page page = PresentationObjectUtil.getParentPage(this);
		if (page == null) {
			return;
		}
		page.addJavascriptURL("/dwr/interface/CommentsEngine.js"); // We need this script in order to work DWR "PUSH"
		page.addJavascriptURL("/dwr/engine.js");
		page.addJavascriptURL(ArticleUtil.getBundle().getResourcesPath() + "/javascript/CommentsHelper.js");
		
		boolean hasValidRights = ContentUtil.hasContentEditorRoles(iwc);
		
		if (!hasValidRights && !showCommentsForAllUsers) {
			return;
		}
		
		WFDivision container = new WFDivision();
		container.setId(COMMENTS_BLOCK_ID);
		container.setStyleClass(styleClass);
		this.add(container);
		
		int commentsCount = getCommentsCount(iwc);
		StringBuffer linkToAtomFeedImage = new StringBuffer(ArticleUtil.getBundle().getResourcesPath());
		linkToAtomFeedImage.append(FEED_IMAGE);
		
		// JavaScript
		Script script = new Script();
		StringBuffer action = new StringBuffer("setCommentStartInfo('");
		action.append(linkToComments).append("', ").append(showCommentsList).append(");");
		script.addScriptLine(action.toString());
		container.add(script);
		
		// Enable comments container
		addEnableCommentsCheckboxContainer(iwc, container, hasValidRights);
		
		// Comments label
		WFDivision articleComments = new WFDivision();
		articleComments.setId("article_comments_link_label_container");
		StringBuffer comments = new StringBuffer(ArticleUtil.getBundle().getLocalizedString("comments"));
		comments.append(ContentConstants.SPACE).append("(<span id='contentItemCount' class='contentItemCountStyle'>");
		comments.append(commentsCount).append("</span>)");
		Link commentsLabel = new Link(comments.toString(), "#showCommentsList");
		commentsLabel.setOnClick("getCommentsList()");
		articleComments.add(commentsLabel);
		
		// Simple space
		Text emptyText = new Text(ContentConstants.SPACE);
		articleComments.add(emptyText);
		
		// Link - Atom feed
		if (commentsCount > 0) {
			Image atom = new Image(linkToAtomFeedImage.toString(), ArticleUtil.getBundle().getLocalizedString("atom_feed"));
			Link linkToFeed = new Link();
			linkToFeed.setId("article_comments_link_to_feed");
			linkToFeed.setImage(atom);
			linkToFeed.setURL(ThemesHelper.getInstance().getFullServerName(iwc) + ContentConstants.CONTENT + linkToComments);
			articleComments.add(linkToFeed);
		}
		container.add(articleComments);
		
		// Add comment block
		container.add(getAddCommentBlock(iwc));
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
		BuilderService service = null;
		try {
			service = this.getBuilderService(iwc);
		} catch (RemoteException e) {
			e.printStackTrace();
			return;
		}
		String pageKey = getThisPageKey(iwc);
		if (pageKey == null) {
			return;
		}
		List<String> moduleIds = service.getModuleId(pageKey, CommentsViewer.class.getName());
		if (moduleIds == null) {
			return;
		}
		if (moduleIds.size() == 0) {
			return;
		}
		WFDivision showCommentsContainer = new WFDivision();
		CheckBox enableCheckBox = new CheckBox("enableComments");
		enableCheckBox.setId("manageCommentsBlockCheckBox");
		enableCheckBox.setOnClick("enableComments(this.checked, '"+pageKey+"', '"+moduleIds.get(0)+"', 'showCommentsForAllUsers');");
		enableCheckBox.setChecked(isShowCommentsForAllUsers());
		Text enableText = new Text(ArticleUtil.getBundle().getLocalizedString("enable_comments"));
		showCommentsContainer.add(enableText);
		showCommentsContainer.add(enableCheckBox);
		container.add(showCommentsContainer);
	}
	
	private UIComponent getAddCommentBlock(IWContext iwc) {
		WFDivision addComments = new WFDivision();
		addComments.setId("add_comment_block");
		Link label = new Link(ArticleUtil.getBundle().getLocalizedString("add_your_comment"), "#" + addComments.getId());
		String user = ArticleUtil.getBundle().getLocalizedString("name");
		String subject = ArticleUtil.getBundle().getLocalizedString("subject");
		String comment = ArticleUtil.getBundle().getLocalizedString("comment");
		String posted = ArticleUtil.getBundle().getLocalizedString("posted");
		String send = ArticleUtil.getBundle().getLocalizedString("send");
		String sending = ArticleUtil.getBundle().getLocalizedString("sending");
		String separator = "', '";
		String loggedUser = null;
		try {
			loggedUser = iwc.getCurrentUser().getName();
		} catch (NotLoggedOnException e) {
			loggedUser = ArticleUtil.getBundle().getLocalizedString("anonymous");
		}
		StringBuffer action = new StringBuffer("addCommentPanel('").append(addComments.getId()).append(separator);
		action.append(linkToComments).append(separator).append(user).append(separator).append(subject).append(separator);
		action.append(comment).append(separator).append(posted).append(separator).append(send).append(separator);
		action.append(sending).append(separator).append(loggedUser).append(separator);
		action.append(ArticleUtil.getBundle().getLocalizedString("email")).append(separator);
		action.append(ArticleUtil.getBundle().getLocalizedString("comment_form")).append("',").append(isForumPage).append(")");
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
		Object values[] = new Object[6];
		values[0] = super.saveState(context);
		values[1] = linkToComments;
		values[2] = styleClass;
		values[3] = showCommentsList;
		values[4] = isForumPage;
		values[5] = showCommentsForAllUsers;
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
}
