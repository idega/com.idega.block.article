package com.idega.block.article.data;

import java.util.Collection;

import javax.ejb.CreateException;
import javax.ejb.FinderException;

import com.idega.data.IDOHome;
import com.idega.user.data.User;

public interface CommentHome extends IDOHome {

	public Comment create() throws CreateException;
	
	public Comment findByPrimaryKey(Object primaryKey) throws FinderException;
	
	public Collection<Comment> findAllCommentsForUser(User author, String commentHolder) throws FinderException;
	
	public Collection<Comment> findAllCommentsByHolder(String commentHolder) throws FinderException;
}
