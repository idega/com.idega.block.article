package com.idega.block.article.business;

import java.util.List;

import com.idega.builder.bean.AdvancedProperty;
import com.idega.util.SendMail;

public class CommentsNotificationSender implements Runnable {
	
	private List<AdvancedProperty> recipientsWithLinks = null;
	private String emailFieldFrom = null;
	private String emailFieldSubject = null;
	private String emailFieldBody = null;
	private String mailServerHost = null;
	
	public CommentsNotificationSender(List<AdvancedProperty> recipientsWithLinks, String emailFieldFrom, String emailFieldSubject, String emailFieldBody,
			String mailServerHost) {
		this.recipientsWithLinks = recipientsWithLinks;
		this.emailFieldFrom = emailFieldFrom;
		this.emailFieldSubject = emailFieldSubject;
		this.emailFieldBody = emailFieldBody;
		this.mailServerHost = mailServerHost;
	}

	public void run() {
		if (recipientsWithLinks == null) {
			return;
		}
		
		for (AdvancedProperty recipientWithLink: recipientsWithLinks) {
			try {
				String message = new StringBuilder(emailFieldBody).append(recipientWithLink.getValue()).toString();
				SendMail.send(emailFieldFrom, recipientWithLink.getId(), null, null, mailServerHost, emailFieldSubject, message);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}