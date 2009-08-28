package com.idega.block.article.bean;

import java.util.List;

public class CommentsViewerProperties {

	private String user;
	private String subject;
	private String email;
	private String body;
	private String uri;
	private String commentsPageUrl;
	private String id;
	private String instanceId;
	private String springBeanIdentifier;
	private String identifier;
	private String title;
	private String subtitle;
	
	private String currentPageUri;
	
	private boolean notify;
	private boolean newestEntriesOnTop;
	private boolean actionSuccess;
	private boolean addLoginbyUUIDOnRSSFeedLink;
	private boolean addNulls;
	private boolean fetchFully;
	
	private boolean privateComment;
	private boolean announcedToPublic;
	
	private Integer replyForComment;
	
	private String entryId;
	private String primaryKey;
	
	private boolean makePublic;
	
	private List<String> uploadedFiles;
	
	public boolean isPrivateComment() {
		return privateComment;
	}

	public void setPrivateComment(boolean privateComment) {
		this.privateComment = privateComment;
	}

	public boolean isAnnouncedToPublic() {
		return announcedToPublic;
	}

	public void setAnnouncedToPublic(boolean announcedToPublic) {
		this.announcedToPublic = announcedToPublic;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	public String getSpringBeanIdentifier() {
		return springBeanIdentifier;
	}

	public void setSpringBeanIdentifier(String springBeanIdentifier) {
		this.springBeanIdentifier = springBeanIdentifier;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public boolean isNotify() {
		return notify;
	}

	public void setNotify(boolean notify) {
		this.notify = notify;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSubtitle() {
		return subtitle;
	}

	public void setSubtitle(String subtitle) {
		this.subtitle = subtitle;
	}

	public boolean isNewestEntriesOnTop() {
		return newestEntriesOnTop;
	}

	public void setNewestEntriesOnTop(boolean newestEntriesOnTop) {
		this.newestEntriesOnTop = newestEntriesOnTop;
	}

	public boolean isActionSuccess() {
		return actionSuccess;
	}

	public void setActionSuccess(boolean actionSuccess) {
		this.actionSuccess = actionSuccess;
	}

	public boolean isAddLoginbyUUIDOnRSSFeedLink() {
		return addLoginbyUUIDOnRSSFeedLink;
	}

	public void setAddLoginbyUUIDOnRSSFeedLink(boolean addLoginbyUUIDOnRSSFeedLink) {
		this.addLoginbyUUIDOnRSSFeedLink = addLoginbyUUIDOnRSSFeedLink;
	}

	public String getCurrentPageUri() {
		return currentPageUri;
	}

	public void setCurrentPageUri(String currentPageUri) {
		this.currentPageUri = currentPageUri;
	}

	public Integer getReplyForComment() {
		return replyForComment;
	}

	public void setReplyForComment(Integer replyForComment) {
		this.replyForComment = replyForComment;
	}

	public boolean isAddNulls() {
		return addNulls;
	}

	public void setAddNulls(boolean addNulls) {
		this.addNulls = addNulls;
	}

	public String getEntryId() {
		return entryId;
	}

	public void setEntryId(String entryId) {
		this.entryId = entryId;
	}

	public String getPrimaryKey() {
		return primaryKey;
	}

	public void setPrimaryKey(String primaryKey) {
		this.primaryKey = primaryKey;
	}

	public List<String> getUploadedFiles() {
		return uploadedFiles;
	}

	public void setUploadedFiles(List<String> uploadedFiles) {
		this.uploadedFiles = uploadedFiles;
	}

	public boolean isFetchFully() {
		return fetchFully;
	}

	public void setFetchFully(boolean fetchFully) {
		this.fetchFully = fetchFully;
	}

	public String getCommentsPageUrl() {
		return commentsPageUrl;
	}

	public void setCommentsPageUrl(String commentsPageUrl) {
		this.commentsPageUrl = commentsPageUrl;
	}

	public boolean isMakePublic() {
		return makePublic;
	}

	public void setMakePublic(boolean makePublic) {
		this.makePublic = makePublic;
	}
	
	
}