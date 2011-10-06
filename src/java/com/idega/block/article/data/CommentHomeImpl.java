package com.idega.block.article.data;

import java.util.Collection;

import javax.ejb.CreateException;
import javax.ejb.FinderException;

import com.idega.data.IDOEntity;
import com.idega.data.IDOFactory;
import com.idega.user.data.User;

public class CommentHomeImpl extends IDOFactory implements CommentHome {

	private static final long serialVersionUID = 5046333623082268465L;

	@Override
	protected Class<Comment> getEntityInterfaceClass() {
		return Comment.class;
	}

	public Comment create() throws CreateException {
		return (Comment) super.createIDO();
	}

	public Comment findByPrimaryKey(Object primaryKey) throws FinderException {
		return (Comment) super.findByPrimaryKeyIDO(primaryKey);
	}

	public Collection<Comment> findAllCommentsForUser(User author, String commentHolder) throws FinderException {
		IDOEntity entity = this.idoCheckOutPooledEntity();
		Collection<Object> ids = ((CommentBMPBean) entity).ejbFindAllCommentsForUser(author, commentHolder);
		this.idoCheckInPooledEntity(entity);
		return getEntityCollectionForPrimaryKeys(ids);
	}

	public Collection<Comment> findAllCommentsByHolder(String commentHolder) throws FinderException {
		IDOEntity entity = this.idoCheckOutPooledEntity();
		Collection<Object> ids = ((CommentBMPBean) entity).ejbFindAllCommentsByHolder(commentHolder);
		this.idoCheckInPooledEntity(entity);
		return this.getEntityCollectionForPrimaryKeys(ids);
	}

}