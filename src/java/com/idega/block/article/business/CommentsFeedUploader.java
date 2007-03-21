package com.idega.block.article.business;

import java.rmi.RemoteException;

import com.idega.content.business.ContentConstants;
import com.idega.slide.business.IWSlideService;

public class CommentsFeedUploader implements Runnable {
	
	private IWSlideService service = null;
	private String fileBase = null;
	private String fileName = null;
	private String commentsContent = null;
	
	
	public CommentsFeedUploader(IWSlideService service, String fileBase, String fileName, String commentsContent) {
		this.service = service;
		this.fileBase = fileBase;
		this.fileName = fileName;
		this.commentsContent = commentsContent;
	}

	public void run() {
		if (service == null || fileBase == null || fileName == null || commentsContent == null) {
			return;
		}
		try {
			service.uploadFileAndCreateFoldersFromStringAsRoot(fileBase, fileName, commentsContent, ContentConstants.XML_MIME_TYPE, true);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

}
