package com.idega.block.article.business;

import javax.jcr.RepositoryException;

import org.springframework.beans.factory.annotation.Autowired;

import com.idega.content.business.ContentConstants;
import com.idega.repository.RepositoryService;
import com.idega.util.expression.ELUtil;

public class CommentsFeedUploader implements Runnable {

	@Autowired
	private RepositoryService service = null;

	private String fileBase = null;
	private String fileName = null;
	private String commentsContent = null;

	private CommentsFeedUploader() {
		super();

		ELUtil.getInstance().autowire(this);
	}

	public CommentsFeedUploader(String fileBase, String fileName, String commentsContent) {
		this();

		this.fileBase = fileBase;
		this.fileName = fileName;
		this.commentsContent = commentsContent;
	}

	@Override
	public void run() {
		if (service == null || fileBase == null || fileName == null || commentsContent == null) {
			return;
		}
		try {
			service.uploadFileAndCreateFoldersFromStringAsRoot(fileBase, fileName, commentsContent, ContentConstants.XML_MIME_TYPE);
		} catch (RepositoryException e) {
			e.printStackTrace();
		}
	}

}