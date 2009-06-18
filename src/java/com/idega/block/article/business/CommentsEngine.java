package com.idega.block.article.business;


import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.List;

import com.idega.block.article.bean.ArticleComment;
import com.idega.block.article.bean.CommentAttachmentNotifyBean;
import com.idega.block.article.bean.CommentsViewerProperties;
import com.idega.builder.bean.AdvancedProperty;
import com.idega.business.IBOSession;
import com.idega.core.component.bean.RenderedComponent;
import com.idega.presentation.IWContext;

public interface CommentsEngine extends IBOSession {
	/**
	 * @see com.idega.block.article.business.CommentsEngineBean#addComment
	 */
	public boolean addComment(CommentsViewerProperties properties) throws RemoteException;

	/**
	 * @see com.idega.block.article.business.CommentsEngineBean#getCommentsForAllPages
	 */
	public boolean getCommentsForAllPages(CommentsViewerProperties properties) throws RemoteException;

	/**
	 * @see com.idega.block.article.business.CommentsEngineBean#getComments
	 */
	public List<ArticleComment> getComments(CommentsViewerProperties properties) throws RemoteException;

	/**
	 * @see com.idega.block.article.business.CommentsEngineBean#getCommentsCount
	 */
	public int getCommentsCount(String uri, String springBeanIdentifier, String identifier, IWContext iwc, boolean addLoginbyUUIDOnRSSFeedLink)
		throws RemoteException;

	/**
	 * @see com.idega.block.article.business.CommentsEngineBean#setModuleProperty
	 */
	public boolean setModuleProperty(String pageKey, String moduleId, String propName, String propValue, String cacheKey) throws RemoteException;

	/**
	 * @see com.idega.block.article.business.CommentsEngineBean#hideOrShowComments
	 */
	public boolean hideOrShowComments() throws RemoteException;

	/**
	 * @see com.idega.block.article.business.CommentsEngineBean#deleteComments
	 */
	public CommentsViewerProperties deleteComments(CommentsViewerProperties properties) throws RemoteException;
	
	/**
	 * @see com.idega.block.article.business.CommentsEngineBean#getFixedCommentsUri
	 */
	public String getFixedCommentsUri(IWContext iwc, String uri, String instanceId, String currentPageUri);
	
	/**
	 * @see com.idega.block.article.business.CommentsEngineBean#getCommentsFromUris
	 */
	public List<List<ArticleComment>> getCommentsFromUris(List<CommentsViewerProperties> commentsProperties);
	
	public boolean initCommentsFeed(IWContext iwc, String uri, String user, Timestamp date, String language, String feedTitle, String feedSubtitle,
			CommentsPersistenceManager commentsManager);
	
	public CommentsPersistenceManager getCommentsManager(String springBeanIdentifier);
	
	public boolean setCommentPublished(CommentsViewerProperties properties);
	
	public boolean setReadComment(CommentsViewerProperties properties);
	
	public RenderedComponent getCommentCreator(CommentsViewerProperties properties);
	
	public AdvancedProperty sendNotificationsToDownloadDocument(CommentAttachmentNotifyBean properties);
}