package com.idega.block.article.business;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.idega.block.article.bean.CommentsViewerProperties;
import com.idega.block.article.data.Comment;
import com.idega.block.article.data.CommentHome;
import com.idega.core.accesscontrol.business.LoginSession;
import com.idega.data.IDOLookup;
import com.idega.presentation.IWContext;
import com.idega.user.data.User;
import com.idega.util.expression.ELUtil;
import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.feed.atom.Feed;

public class DefaultCommentsPersistenceManager implements CommentsPersistenceManager {

	private static final Logger LOGGER = Logger.getLogger(DefaultCommentsPersistenceManager.class.getName());
	
	public Object addComment(CommentsViewerProperties properties) {
		if (properties == null || properties.getEntryId() == null) {
			return null;
		}
		
		try {
			boolean hasFullRightsForComments = hasFullRightsForComments(properties.getIdentifier());
			
			CommentHome commentHome = (CommentHome) IDOLookup.getHome(Comment.class);
			Comment comment = commentHome.create();
			
			comment.setEntryId(properties.getEntryId());
			comment.setCommentHolder(String.valueOf(properties.getIdentifier()));
			
			boolean hasReplyToId = properties.getReplyForComment() == null ? false : properties.getReplyForComment() < 0 ? false : true;
			boolean privateComment = hasReplyToId || !hasFullRightsForComments;
			comment.setPrivateComment(privateComment);
			comment.setReplyForCommentId(properties.getReplyForComment());
			comment.setAnnouncedToPublic(hasFullRightsForComments && !privateComment);
			
			User author = getLoggedInUser();
			if (author != null) {
				comment.setAuthorId(Integer.valueOf(author.getId()));
			}
			
			comment.store();
			
			return comment.getPrimaryKey();
		} catch(Exception e) {
			LOGGER.log(Level.WARNING, "Error creating " + Comment.class, e);
		}
		
		return null;
	}

	public Feed getCommentsFeed(IWContext iwc, String processInstanceId) {
		throw new UnsupportedOperationException("This method is not implemented by default manager");
	}

	public String getFeedSubtitle(IWContext iwc, String processInstanceId) {
		throw new UnsupportedOperationException("This method is not implemented by default manager");
	}

	public String getFeedTitle(IWContext iwc, String processInstanceId) {
		throw new UnsupportedOperationException("This method is not implemented by default manager");
	}

	public String getLinkToCommentsXML(String processInstanceId) {
		throw new UnsupportedOperationException("This method is not implemented by default manager");
	}

	public User getUserAvailableToReadWriteCommentsFeed(IWContext iwc) {
		throw new UnsupportedOperationException("This method is not implemented by default manager");
	}

	public boolean hasFullRightsForComments(String processInstanceId) {
		throw new UnsupportedOperationException("This method is not implemented by default manager");
	}

	public boolean hasFullRightsForComments(Long processInstanceId) {
		throw new UnsupportedOperationException("This method is not implemented by default manager");
	}

	public boolean hasRightsToViewComments(String processInstanceId) {
		throw new UnsupportedOperationException("This method is not implemented by default manager");
	}

	public boolean hasRightsToViewComments(Long processInstanceId) {
		throw new UnsupportedOperationException("This method is not implemented by default manager");
	}

	public boolean storeFeed(String processInstanceId, Feed comments) {
		throw new UnsupportedOperationException("This method is not implemented by default manager");
	}

	protected Comment getComment(Object primaryKey) {
		if (primaryKey == null) {
			return null;
		}
		try {
			return ((CommentHome) IDOLookup.getHome(Comment.class)).findByPrimaryKey(primaryKey);
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, "Nothing found by: " + primaryKey, e);
		}
		return null;
	}
	
	public boolean markCommentAsRead(Object primaryKey) {
		Comment comment = getComment(primaryKey);
		if (comment == null) {
			return false;
		}
		
		User reader = getLoggedInUser();
		if (reader == null) {
			return false;
		}
		
		try {
			comment.addReadBy(reader);
			comment.store();
		} catch(Exception e) {
			LOGGER.log(Level.WARNING, "Error marking comment ('"+primaryKey+"') as read by reader: " + reader, e);
			return false;
		}
		
		return true;
	}

	public List<? extends Entry> getEntriesToFormat(Feed comments, CommentsViewerProperties properties) {
		throw new UnsupportedOperationException("This method is not implemented by default manager");
	}

	protected User getLoggedInUser() {
		try {
			LoginSession loginSession = ELUtil.getInstance().getBean(LoginSession.class);
			return loginSession.isLoggedIn() ? loginSession.getUser() : null;
		} catch(Exception e) {
			LOGGER.log(Level.WARNING, "Error getting logged in user", e);
		}
		return null;
	}

	public boolean setCommentPublished(Object primaryKey) {
		Comment comment = getComment(primaryKey);
		if (comment == null) {
			return false;
		}
		
		comment.setAnnouncedToPublic(Boolean.TRUE);
		comment.store();
		
		return true;
	}

	public boolean setCommentRead(Object primaryKey) {
		Comment comment = getComment(primaryKey);
		if (comment == null) {
			return false;
		}
		
		User currentUser = getLoggedInUser();
		if (currentUser == null) {
			return false;
		}
		
		try {
			comment.addReadBy(currentUser);
			comment.store();
		} catch(Exception e) {
			LOGGER.log(Level.WARNING, "Error adding user " + currentUser + " as have red comment: " + primaryKey, e);
		}
		
		return true;
	}
	
}
