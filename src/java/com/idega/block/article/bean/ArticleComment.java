package com.idega.block.article.bean;

public class ArticleComment {
	
	private String user = null;
	private String subject = null;
	private String email = null;
	private String comment = null;
	private String posted = null;
	private String id = null;
	
	private String primaryKey;
	
	private int listNumber = 1;
	
	private boolean canBePublished;
	
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

}
