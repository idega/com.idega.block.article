package com.idega.block.article.business;

import java.util.List;

import com.idega.block.article.bean.CommentsViewerProperties;
import com.idega.block.article.data.Comment;
import com.idega.core.file.data.ICFile;
import com.idega.presentation.IWContext;
import com.idega.user.data.User;
import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.feed.atom.Feed;

public interface CommentsPersistenceManager {

	public boolean hasRightsToViewComments(String identifier);
	
	public boolean hasRightsToViewComments(Long identifier);
	
	public boolean hasFullRightsForComments(String identifier);
	
	public boolean hasFullRightsForComments(Long identifier);
	
	public String getLinkToCommentsXML(String identifier);
	
	public String getFeedTitle(IWContext iwc, String identifier);
	
	public String getFeedSubtitle(IWContext iwc, String identifier);
	
	public boolean storeFeed(String identifier, Feed comments);
	
	public Feed getCommentsFeed(IWContext iwc, String identifier);
	
	public User getUserAvailableToReadWriteCommentsFeed(IWContext iwc);
	
	public Object addComment(CommentsViewerProperties properties);
	
	public boolean markCommentAsRead(Object primaryKey);
	
	public List<? extends Entry> getEntriesToFormat(Feed comments, CommentsViewerProperties properties);
	
	public boolean setCommentPublished(Object primaryKey);
	
	public boolean setCommentRead(Object primaryKey);
	
	public String getCommentFilesPath(CommentsViewerProperties properties);
	
	public Comment getComment(Object primaryKey);
	
	public boolean useFilesUploader(CommentsViewerProperties properties);
	
	public String getTaskNameForAttachments();
	
	public ICFile getCommentAttachment(String icFileId);
}
