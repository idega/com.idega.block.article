package com.idega.block.article.data;

import java.util.Collection;

import javax.ejb.FinderException;

import com.idega.core.file.data.ICFile;
import com.idega.data.GenericEntity;
import com.idega.data.IDOAddRelationshipException;
import com.idega.data.IDORelationshipException;
import com.idega.data.IDORemoveRelationshipException;
import com.idega.data.query.Column;
import com.idega.data.query.Criteria;
import com.idega.data.query.MatchCriteria;
import com.idega.data.query.OR;
import com.idega.data.query.SelectQuery;
import com.idega.data.query.Table;
import com.idega.user.data.User;

public class CommentBMPBean extends GenericEntity implements Comment {

	private static final long serialVersionUID = 4322088004402422054L;

	private static final String TABLE_NAME = "IC_COMMENT";
	
	private static final String COLUMN_ANNOUNCED_TO_PUBLIC = "ANNOUNCED_TO_PUBLIC";
	private static final String COLUMN_AUTHOR = "AUTHOR";
	private static final String COLUMN_COMMENT_HOLDER = "COMMENT_HOLDER";
	private static final String COLUMN_DELETED = "DELETED";
	private static final String COLUMN_PRIVATE = "PRIVATE";
	private static final String COLUMN_REPLY_FOR_COMMENT_ID = "REPLY_FOR_COMMENT";
	private static final String COLUMN_ENTRY_ID = "ENTRY_ID";
	
	private static final String COMMENT_READERS = TABLE_NAME + "_READERS";
	private static final String COMMENT_ATTACHMENTS = TABLE_NAME + "_ATTACHMENTS";
	
	@Override
	public String getEntityName() {
		return TABLE_NAME;
	}

	@Override
	public void initializeAttributes() {
		addAttribute(getIDColumnName());
		
		addAttribute(COLUMN_ANNOUNCED_TO_PUBLIC, "Announced to public", Boolean.class);
		addAttribute(COLUMN_AUTHOR, "Author", true, true, Integer.class, MANY_TO_ONE, User.class);
		addAttribute(COLUMN_COMMENT_HOLDER, "Comment holder", String.class);
		addAttribute(COLUMN_DELETED, "Deleted", Boolean.class);
		addAttribute(COLUMN_PRIVATE, "Private", Boolean.class);
		addAttribute(COLUMN_REPLY_FOR_COMMENT_ID, "Reply for comment", Integer.class);
		addAttribute(COLUMN_ENTRY_ID, "Entry ID", String.class);
		
		addManyToManyRelationShip(User.class, COMMENT_READERS);
		addManyToManyRelationShip(ICFile.class, COMMENT_ATTACHMENTS);
	}

	public boolean isAnnouncedToPublic() {
		return getBooleanColumnValue(COLUMN_ANNOUNCED_TO_PUBLIC);
	}

	public Integer getAuthorId() {
		return getIntColumnValue(COLUMN_AUTHOR);
	}

	public String getCommentHolder() {
		return getStringColumnValue(COLUMN_COMMENT_HOLDER);
	}

	public boolean isDeleted() {
		return getBooleanColumnValue(COLUMN_DELETED);
	}

	public boolean isPrivateComment() {
		return getBooleanColumnValue(COLUMN_PRIVATE);
	}

	public Integer getReplyForCommentId() {
		return getIntColumnValue(COLUMN_REPLY_FOR_COMMENT_ID);
	}

	public void setAnnouncedToPublic(Boolean announcedToPublic) {
		setColumn(COLUMN_ANNOUNCED_TO_PUBLIC, announcedToPublic);
	}

	public void setAuthorId(Integer authorId) {
		setColumn(COLUMN_AUTHOR, authorId);
	}

	public void setCommentHolder(String commentHolder) {
		setColumn(COLUMN_COMMENT_HOLDER, commentHolder);
	}

	public void setDeleted(Boolean deleted) {
		setColumn(COLUMN_DELETED, deleted);
	}

	public void setPrivateComment(Boolean privateComment) {
		setColumn(COLUMN_PRIVATE, privateComment);
	}

	public void setReplyForCommentId(Integer replyForCommentId) {
		setColumn(COLUMN_REPLY_FOR_COMMENT_ID, replyForCommentId);
	}
	
	public void removeReadBy(User reader) throws IDORemoveRelationshipException {
		this.idoRemoveFrom(reader);
	}

	public void addReadBy(User reader) throws IDOAddRelationshipException {
		this.idoAddTo(reader);
	}
	
	@SuppressWarnings("unchecked")
	public Collection<User> getReadBy() {
		try {
			return super.idoGetRelatedEntities(User.class);
		} catch (IDORelationshipException e) {
			e.printStackTrace();
		}
		return null;
	}

	public String getEntryId() {
		return getStringColumnValue(COLUMN_ENTRY_ID);
	}

	public void setEntryId(String entryId) {
		setColumn(COLUMN_ENTRY_ID, entryId);		
	}
	
	@SuppressWarnings("unchecked")
	Collection<Object> ejbFindAllCommentsForUser(User author, String commentHolder) throws FinderException {
		Table table = new Table(this);
		SelectQuery query = new SelectQuery(table);
		query.addColumn(new Column(table, getIDColumnName()));
		
		query.addCriteria(new MatchCriteria(new Column(table, COLUMN_AUTHOR), MatchCriteria.EQUALS, author.getId()));
		query.addCriteria(new MatchCriteria(new Column(table, COLUMN_COMMENT_HOLDER), MatchCriteria.EQUALS, commentHolder));
		addNotDeletedCriteria(query, table);
		
		return this.idoFindPKsByQuery(query);
	}
	
	@SuppressWarnings("unchecked")
	Collection<Object> ejbFindAllCommentsByHolder(String commentHolder) throws FinderException {
		Table table = new Table(this);
		SelectQuery query = new SelectQuery(table);
		query.addColumn(new Column(table, getIDColumnName()));
		
		query.addCriteria(new MatchCriteria(new Column(table, COLUMN_COMMENT_HOLDER), MatchCriteria.EQUALS, commentHolder));
		addNotDeletedCriteria(query, table);
		
		return this.idoFindPKsByQuery(query);
	}
	
	private void addNotDeletedCriteria(SelectQuery query, Table table) {
		Criteria isNull = new MatchCriteria(new Column(table, COLUMN_DELETED), MatchCriteria.IS, MatchCriteria.NULL);
		Criteria isFalse = new MatchCriteria(new Column(table, COLUMN_DELETED), MatchCriteria.EQUALS, false);
		query.addCriteria(new OR(isNull, isFalse));
	}

	public void addAttachment(ICFile attachment) throws IDOAddRelationshipException {
		this.idoAddTo(attachment);		
	}

	@SuppressWarnings("unchecked")
	public Collection<ICFile> getAllAttachments() {
		try {
			return super.idoGetRelatedEntities(ICFile.class);
		} catch (IDORelationshipException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void removeAtttachment(ICFile attachment) throws IDORemoveRelationshipException {
		this.idoRemoveFrom(attachment);
	}
}
