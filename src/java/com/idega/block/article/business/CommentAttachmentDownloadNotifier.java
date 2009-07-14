package com.idega.block.article.business;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.idega.block.article.bean.CommentAttachmentNotifyBean;
import com.idega.business.file.FileDownloadNotificationProperties;
import com.idega.business.file.FileDownloadNotifier;
import com.idega.user.data.User;
import com.idega.util.expression.ELUtil;

@Scope(BeanDefinition.SCOPE_SINGLETON)
@Service(CommentAttachmentDownloadNotifier.BEAN_IDENTIFIER)
public class CommentAttachmentDownloadNotifier extends FileDownloadNotifier {
	
	private static final long serialVersionUID = 2622603459380497529L;

	static final String BEAN_IDENTIFIER = "commentAttachmentDownloadNotifier";
	
	private CommentsPersistenceManager defaultManager;
	
	@Override
	public String getUriToAttachment(FileDownloadNotificationProperties properties, User user) {
		if (!(properties instanceof CommentAttachmentNotifyBean)) {
			return null;
		}
		
		CommentAttachmentNotifyBean commentAttachmentProperties = (CommentAttachmentNotifyBean) properties;
		
		CommentsPersistenceManager defaultManager = getDefaultManager();
		if (defaultManager == null) {
			return null;
		}
		
		try {
			return defaultManager.getUriToAttachment(commentAttachmentProperties.getComment(), defaultManager.getCommentAttachment(properties.getFile()), user);
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	private CommentsPersistenceManager getDefaultManager() {
		if (defaultManager == null) {
			try {
				defaultManager = ELUtil.getInstance().getBean(DefaultCommentsPersistenceManager.BEAN_IDENTIFIER);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		return defaultManager;
	}

}
