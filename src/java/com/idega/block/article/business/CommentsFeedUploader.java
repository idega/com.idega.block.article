package com.idega.block.article.business;

import javax.jcr.RepositoryException;

import com.idega.content.business.ContentConstants;
import com.idega.repository.RepositoryService;
import com.idega.util.expression.ELUtil;

public class CommentsFeedUploader implements Runnable {

	private String fileBase = null;
	private String fileName = null;
	private String commentsContent = null;

	private CommentsFeedUploader() {
		super();
	}

	public CommentsFeedUploader(String fileBase, String fileName, String commentsContent) {
		this();

		this.fileBase = fileBase;
		this.fileName = fileName;
		this.commentsContent = commentsContent;
	}

	@Override
	public void run() {
		if (fileBase == null || fileName == null || commentsContent == null) {
			return;
		}
		try {
			RepositoryService repository = ELUtil.getInstance().getBean(RepositoryService.class);
			repository.uploadFileAndCreateFoldersFromStringAsRoot(fileBase, fileName, commentsContent, ContentConstants.XML_MIME_TYPE);
		} catch (RepositoryException e) {
			e.printStackTrace();
		}
	}

}