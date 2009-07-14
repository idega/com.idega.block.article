package com.idega.block.article.bean;

import java.util.List;

import org.directwebremoting.annotations.DataTransferObject;
import org.directwebremoting.annotations.RemoteProperty;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.idega.business.file.FileDownloadNotificationProperties;
import com.idega.dwr.business.DWRAnnotationPersistance;

@DataTransferObject
@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class CommentAttachmentNotifyBean extends FileDownloadNotificationProperties implements DWRAnnotationPersistance {

	private static final long serialVersionUID = -3903991576176166843L;

	@RemoteProperty
	private String comment;

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	@Override
	@RemoteProperty
	public String getFile() {
		return super.getFile();
	}

	@Override
	@RemoteProperty
	public String getServer() {
		return super.getServer();
	}

	@Override
	@RemoteProperty
	public String getUrl() {
		return super.getUrl();
	}

	@Override
	@RemoteProperty
	public List<String> getUsers() {
		return super.getUsers();
	}
	
	
}
