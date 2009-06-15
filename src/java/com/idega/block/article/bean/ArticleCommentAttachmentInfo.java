package com.idega.block.article.bean;

import java.io.Serializable;

import org.directwebremoting.annotations.DataTransferObject;
import org.directwebremoting.annotations.RemoteProperty;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.idega.dwr.business.DWRAnnotationPersistance;

@DataTransferObject
@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ArticleCommentAttachmentInfo implements Serializable, DWRAnnotationPersistance {

	private static final long serialVersionUID = 1754733914959572714L;
	
	@RemoteProperty
	private String commentId;
	@RemoteProperty
	private String attachmentId;
	@RemoteProperty
	private String identifier;
	@RemoteProperty
	private String beanIdentifier;
	
	@RemoteProperty
	private String name;
	@RemoteProperty
	private String uri;
	@RemoteProperty
	private String statisticsUri;
	
	public String getCommentId() {
		return commentId;
	}
	public void setCommentId(String commentId) {
		this.commentId = commentId;
	}
	public String getAttachmentId() {
		return attachmentId;
	}
	public void setAttachmentId(String attachmentId) {
		this.attachmentId = attachmentId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	public String getStatisticsUri() {
		return statisticsUri;
	}
	public void setStatisticsUri(String statisticsUri) {
		this.statisticsUri = statisticsUri;
	}
	public String getIdentifier() {
		return identifier;
	}
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	public String getBeanIdentifier() {
		return beanIdentifier;
	}
	public void setBeanIdentifier(String beanIdentifier) {
		this.beanIdentifier = beanIdentifier;
	}
}
