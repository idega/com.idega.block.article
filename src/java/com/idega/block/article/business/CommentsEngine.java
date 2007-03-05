package com.idega.block.article.business;


import com.idega.block.article.bean.ContentItemComment;
import com.idega.business.IBOService;

import java.util.List;
import java.rmi.RemoteException;

public interface CommentsEngine extends IBOService {
	/**
	 * @see com.idega.block.article.business.CommentsEngineBean#addComment
	 */
	public boolean addComment(String cacheKey, String user, String subject, String email, String body, String uri, boolean notify) throws RemoteException;

	/**
	 * @see com.idega.block.article.business.CommentsEngineBean#getComments
	 */
	public List<ContentItemComment> getComments(String uri) throws RemoteException;

	/**
	 * @see com.idega.block.article.business.CommentsEngineBean#getCommentsCount
	 */
	public int getCommentsCount(String uri) throws RemoteException;
	
	/**
	 * @see com.idega.block.article.business.CommentsEngineBean#setModuleProperty
	 */
	public boolean setModuleProperty(String pageKey, String moduleId, String propName, String propValue, String cacheKey) throws RemoteException;
	
	/**
	 * @see com.idega.block.article.business.CommentsEngineBean#manageArticleCache
	 */
	public boolean manageArticleCache(String methodName) throws RemoteException;
	
	/**
	 * @see com.idega.block.article.business.CommentsEngineBean#clearArticleCaches
	 */
	public boolean clearArticleCaches(String cacheKey) throws RemoteException;
	
	/**
	 * @see com.idega.block.article.business.CommentsEngineBean#getInitInfoForComments
	 */
	public List<String> getInitInfoForComments() throws RemoteException;
	
	/**
	 * @see com.idega.block.article.business.CommentsEngineBean#getUserRights
	 */
	public boolean getUserRights() throws RemoteException;
}