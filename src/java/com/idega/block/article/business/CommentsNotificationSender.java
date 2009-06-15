package com.idega.block.article.business;

import java.util.List;

import com.idega.util.SendMail;

public class CommentsNotificationSender implements Runnable {
	
	private List<String> emails = null;
	private String emailFieldFrom = null;
	private String emailFieldSubject = null;
	private String emailFieldBody = null;
	private String mailServerHost = null;
	
	public CommentsNotificationSender(List<String> emails, String emailFieldFrom, String emailFieldSubject, String emailFieldBody,
			String mailServerHost) {
		this.emails = emails;
		this.emailFieldFrom = emailFieldFrom;
		this.emailFieldSubject = emailFieldSubject;
		this.emailFieldBody = emailFieldBody;
		this.mailServerHost = mailServerHost;
	}

	public void run() {
		if (emails == null) {
			return;
		}
		
		for (String email: emails) {
			try {
				SendMail.send(emailFieldFrom, email, null, null, mailServerHost, emailFieldSubject, emailFieldBody);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}