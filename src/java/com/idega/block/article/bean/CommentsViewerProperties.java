package com.idega.block.article.bean;

public class CommentsViewerProperties {

	private String user;
	private String subject;
	private String email;
	private String body;
	private String uri;
	private String id;
	private String instanceId;
	private String springBeanIdentifier;
	private String identifier;
	private String title;
	private String subtitle;
	
	private boolean notify;
	private boolean newestEntriesOnTop;
	private boolean actionSuccess;
	private boolean addLoginbyUUIDOnRSSFeedLink;

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
	
}
