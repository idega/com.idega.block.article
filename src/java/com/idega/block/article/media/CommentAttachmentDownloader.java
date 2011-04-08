package com.idega.block.article.media;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import com.idega.block.article.component.ArticleCommentAttachmentStatisticsViewer;
import com.idega.core.file.data.ICFile;
import com.idega.core.file.data.ICFileHome;
import com.idega.core.file.util.MimeTypeUtil;
import com.idega.data.IDOLookup;
import com.idega.io.DownloadWriter;
import com.idega.io.MediaWritable;
import com.idega.presentation.IWContext;
import com.idega.repository.bean.RepositoryItem;
import com.idega.util.CoreConstants;
import com.idega.util.FileUtil;

public class CommentAttachmentDownloader extends DownloadWriter implements MediaWritable {

	private String mimeType;

	private RepositoryItem attachedFile;

	@Override
	public String getMimeType() {
		return mimeType;
	}

	@Override
	public void init(HttpServletRequest req, IWContext iwc) {
		String attachmentId = iwc.getParameter(ArticleCommentAttachmentStatisticsViewer.COMMENT_ATTACHMENT_ID_PARAMETER);

		ICFile attachment = getAttachment(attachmentId);
		if (attachment == null) {
			return;
		}

		String name = null;
		try {
			name = URLDecoder.decode(attachment.getName(), CoreConstants.ENCODING_UTF8);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			name = attachment.getName();
		}
		resolveMimeType(name);
		if (!setResource(iwc, attachment)) {
			Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Error reading attachment: " + attachment.getFileUri());
			return;
		}

		if (attachedFile == null || !attachedFile.exists()) {
			return;
		}

		setAsDownload(iwc, name, Long.valueOf(attachedFile.getLength()).intValue(), attachment.getHash() == null ? attachment.getId() :
			attachment.getHash());
	}

	@Override
	public void writeTo(OutputStream out) throws IOException {
		InputStream in = attachedFile.getInputStream();

		FileUtil.streamToOutputStream(in, out);

		out.flush();
		out.close();
		in.close();
	}

	private boolean setResource(IWContext iwc, ICFile attachment) {
		try {

			String uri = URLDecoder.decode(attachment.getFileUri(), CoreConstants.ENCODING_UTF8);
			attachedFile = getRepositoryService().getRepositoryItemAsRootUser(uri);
			if (attachedFile == null || !attachedFile.exists()) {
				Logger.getLogger(getClass().getName()).log(Level.WARNING, "Error getting file by uri: " + uri);
				return false;
			}
			return true;
		} catch(Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	private void resolveMimeType(String name) {
		mimeType = MimeTypeUtil.resolveMimeTypeFromFileName(name);
	}

	private ICFile getAttachment(String attachmentId) {
		if (attachmentId == null) {
			return null;
		}

		try {
			ICFileHome fileHome = (ICFileHome) IDOLookup.getHome(ICFile.class);
			return fileHome.findByPrimaryKey(attachmentId);
		} catch(Exception e) {
			e.printStackTrace();
		}

		return null;
	}

}