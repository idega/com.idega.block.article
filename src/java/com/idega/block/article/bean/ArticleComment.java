package com.idega.block.article.bean;

import java.util.List;

import com.idega.builder.bean.AdvancedProperty;

public class ArticleComment {
	
	private String user = null;
	private String subject = null;
	private String email = null;
	private String comment = null;
	private String posted = null;
	private String id = null;
	
	private String primaryKey;
	
	private int listNumber = 1;
	
	private boolean published;
	private boolean canBePublished;
	private boolean canBeRead;
	private boolean canBeReplied;
	
	private List<AdvancedProperty> readers;
	private List<ArticleCommentAttachmentInfo> attachments;
	
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public String getPosted() {
		return posted;
	}
	public void setPosted(String posted) {
		this.posted = posted;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public int getListNumber() {
		return listNumber;
	}
	public void setListNumber(int listNumber) {
		this.listNumber = listNumber;
	}
	public boolean isCanBePublished() {
		return canBePublished;
	}
	public void setCanBePublished(boolean canBePublished) {
		this.canBePublished = canBePublished;
	}
	public String getPrimaryKey() {
		return primaryKey;
	}
	public void setPrimaryKey(String primaryKey) {
		this.primaryKey = primaryKey;
	}
	public boolean isCanBeRead() {
		return canBeRead;
	}
	public void setCanBeRead(boolean canBeRead) {
		this.canBeRead = canBeRead;
	}
	public boolean isCanBeReplied() {
		return canBeReplied;
	}
	public void setCanBeReplied(boolean canBeReplied) {
		this.canBeReplied = canBeReplied;
	}
	public List<AdvancedProperty> getReaders() {
		return readers;
	}
	public void setReaders(List<AdvancedProperty> readers) {
		this.readers = readers;
	}
	public List<ArticleCommentAttachmentInfo> getAttachments() {
		return attachments;
	}
	public void setAttachments(List<ArticleCommentAttachmentInfo> attachments) {
		this.attachments = attachments;
	}
	public boolean isPublished() {
		return published;
	}
	public void setPublished(boolean published) {
		this.published = published;
	}

}
