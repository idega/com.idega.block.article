package com.idega.block.article.business;


import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.List;

import com.idega.block.article.bean.ArticleComment;
import com.idega.business.IBOSession;
import com.idega.presentation.IWContext;

public interface CommentsEngine extends IBOSession {
	/**
	 * @see com.idega.block.article.business.CommentsEngineBean#addComment
	 */
	public boolean addComment(String user, String subject, String email, String body, String uri, boolean notify, String id, String instanceId) throws RemoteException;

	/**
	 * @see com.idega.block.article.business.CommentsEngineBean#getCommentsForAllPages
	 */
	public boolean getCommentsForAllPages(String uri, String id) throws RemoteException;

	/**
	 * @see com.idega.block.article.business.CommentsEngineBean#getComments
	 */
	public List<ArticleComment> getComments(String uri) throws RemoteException;

	/**
	 * @see com.idega.block.article.business.CommentsEngineBean#getCommentsForCurrentPage
	 */
	public boolean getCommentsForCurrentPage(String uri, String id) throws RemoteException;

	/**
	 * @see com.idega.block.article.business.CommentsEngineBean#getCommentsCount
	 */
	public int getCommentsCount(String uri) throws RemoteException;

	/**
	 * @see com.idega.block.article.business.CommentsEngineBean#setModuleProperty
	 */
	public boolean setModuleProperty(String pageKey, String moduleId, String propName, String propValue, String cacheKey) throws RemoteException;

	/**
	 * @see com.idega.block.article.business.CommentsEngineBean#hideOrShowComments
	 */
	public boolean hideOrShowComments() throws RemoteException;

	/**
	 * @see com.idega.block.article.business.CommentsEngineBean#getInitInfoForComments
	 */
	public List<String> getInitInfoForComments() throws RemoteException;

	/**
	 * @see com.idega.block.article.business.CommentsEngineBean#getUserRights
	 */
	public boolean getUserRights() throws RemoteException;

	/**
	 * @see com.idega.block.article.business.CommentsEngineBean#deleteComments
	 */
	public List<String> deleteComments(String id, String commentId, String linkToComments) throws RemoteException;
	
	/**
	 * @see com.idega.block.article.business.CommentsEngineBean#getFixedCommentsUri
	 */
	public String getFixedCommentsUri(IWContext iwc, String uri, String instanceId);
	
	/**
	 * @see com.idega.block.article.business.CommentsEngineBean#getCommentsFromUris
	 */
	public List<List<ArticleComment>> getCommentsFromUris(List<String> uris);
	
	public boolean initCommentsFeed(IWContext iwc, String uri, String user, Timestamp date, String language);
}