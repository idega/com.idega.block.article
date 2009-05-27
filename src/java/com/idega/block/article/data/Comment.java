package com.idega.block.article.data;

import java.io.Serializable;
import java.util.Collection;

import com.idega.data.IDOAddRelationshipException;
import com.idega.data.IDOEntity;
import com.idega.data.IDORemoveRelationshipException;
import com.idega.user.data.User;

/**
 * Entity to store information about comment
 * 
 * @author <a href="mailto:valdas@idega.com">Valdas Žemaitis</a>
 * @version $Revision: 1.1 $
 *
 * Last modified: $Date: 2009/05/27 16:14:38 $ by: $Author: valdas $
 */
public interface Comment extends IDOEntity, Serializable {

	public String getCommentHolder();
	public void setCommentHolder(String commentHolder);
	
	public boolean isPrivateComment();
	public void setPrivateComment(Boolean privateComment);
	
	public boolean isAnnouncedToPublic();
	public void setAnnouncedToPublic(Boolean announcedToPublic);
	
	public Integer getAuthorId();
	public void setAuthorId(Integer authorId);
	
	public boolean isDeleted();
	public void setDeleted(Boolean deleted);
	
	public Integer getReplyForCommentId();
	public void setReplyForCommentId(Integer replyForCommentId);

	public Collection<User> getReadBy();
	public void addReadBy(User reader) throws IDOAddRelationshipException;
	public void removeReadBy(User reader) throws IDORemoveRelationshipException;
	
	public void setEntryId(String entryId);
	public String getEntryId();
}
