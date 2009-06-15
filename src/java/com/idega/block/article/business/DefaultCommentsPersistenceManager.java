package com.idega.block.article.business;

import java.net.URLEncoder;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.idega.block.article.bean.CommentsViewerProperties;
import com.idega.block.article.component.ArticleCommentAttachmentStatisticsViewer;
import com.idega.block.article.data.Comment;
import com.idega.block.article.data.CommentHome;
import com.idega.block.article.media.CommentAttachmentDownloader;
import com.idega.core.accesscontrol.business.LoginBusinessBean;
import com.idega.core.accesscontrol.business.LoginSession;
import com.idega.core.file.data.ICFile;
import com.idega.core.file.data.ICFileHome;
import com.idega.data.IDOLookup;
import com.idega.idegaweb.IWMainApplication;
import com.idega.io.MediaWritable;
import com.idega.presentation.IWContext;
import com.idega.user.data.User;
import com.idega.util.CoreConstants;
import com.idega.util.ListUtil;
import com.idega.util.URIUtil;
import com.idega.util.expression.ELUtil;
import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.feed.atom.Feed;

@Scope(BeanDefinition.SCOPE_SINGLETON)
@Service(DefaultCommentsPersistenceManager.BEAN_IDENTIFIER)
public class DefaultCommentsPersistenceManager implements CommentsPersistenceManager {

	static final String BEAN_IDENTIFIER = "defaultCommentsPersistenceManager";
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
			
			addAttachment(properties, comment);
			
			return comment.getPrimaryKey();
		} catch(Exception e) {
			LOGGER.log(Level.WARNING, "Error creating " + Comment.class, e);
		}
		
		return null;
	}
	
	private boolean addAttachment(CommentsViewerProperties properties, Comment comment) {
		if (ListUtil.isEmpty(properties.getUploadedFiles())) {
			return true;
		}
		
		try {
			ICFileHome fileHome = (ICFileHome) IDOLookup.getHome(ICFile.class);
			for (String uploadedFile: properties.getUploadedFiles()) {
				if (!uploadedFile.startsWith(CoreConstants.WEBDAV_SERVLET_URI)) {
					uploadedFile = new StringBuilder(CoreConstants.WEBDAV_SERVLET_URI).append(uploadedFile).toString();
				}
				
				ICFile file = fileHome.create();
				
				file.setName(URLEncoder.encode(uploadedFile.substring(uploadedFile.lastIndexOf(CoreConstants.SLASH) + 1), CoreConstants.ENCODING_UTF8));
				file.setFileUri(URLEncoder.encode(uploadedFile, CoreConstants.ENCODING_UTF8));
				
				file.store();
				
				comment.addAttachment(file);
			}
			comment.store();
			return true;
		} catch(Exception e) {
			LOGGER.log(Level.WARNING, "Unable to add attachments: " + properties.getUploadedFiles(), e);
		}
		return false;
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

	public Comment getComment(Object primaryKey) {
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

	public String getCommentFilesPath(CommentsViewerProperties properties) {
		throw new UnsupportedOperationException("This method is not implemented by default manager");
	}

	public boolean useFilesUploader(CommentsViewerProperties properties) {
		throw new UnsupportedOperationException("This method is not implemented by default manager");
	}

	public String getTaskNameForAttachments() {
		throw new UnsupportedOperationException("This method is not implemented by default manager");
	}

	public ICFile getCommentAttachment(String icFileId) {
		try {
			ICFileHome fileHome = (ICFileHome) IDOLookup.getHome(ICFile.class);
			return fileHome.findByPrimaryKey(icFileId);
		} catch(Exception e) {
			LOGGER.log(Level.WARNING, "Error getting ICFile: " + icFileId, e);
		}
		return null;
	}

	public String getUriToAttachment(String commentId, ICFile attachment, User user) {
		URIUtil uri = new URIUtil(IWMainApplication.getDefaultIWMainApplication().getMediaServletURI());
		
		uri.setParameter(MediaWritable.PRM_WRITABLE_CLASS, IWMainApplication.getEncryptedClassName(CommentAttachmentDownloader.class));
		uri.setParameter(ArticleCommentAttachmentStatisticsViewer.COMMENT_ID_PARAMETER, commentId);
		uri.setParameter(ArticleCommentAttachmentStatisticsViewer.COMMENT_ATTACHMENT_ID_PARAMETER, attachment.getPrimaryKey().toString());
		
		if (user != null) {
			uri.setParameter(LoginBusinessBean.PARAM_LOGIN_BY_UNIQUE_ID, user.getUniqueId());
			uri.setParameter(LoginBusinessBean.LoginStateParameter, LoginBusinessBean.LOGIN_EVENT_LOGIN);
		}
		
		return uri.getUri();
	}
	
}