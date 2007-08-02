package com.idega.block.article.business;


import java.util.List;

import com.idega.block.article.bean.ArticleComment;
import com.idega.business.IBOSession;
import java.rmi.RemoteException;

public interface CommentsEngine extends IBOSession {
	/**
	 * @see com.idega.block.article.business.CommentsEngineBean#addComment
	 */
	public boolean addComment(String user, String subject, String email, String body, String uri, boolean notify, String id) throws RemoteException;

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
}