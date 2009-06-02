package com.idega.block.article.media;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.apache.webdav.lib.WebdavResource;

import com.idega.business.IBOLookup;
import com.idega.core.file.data.ICFile;
import com.idega.core.file.data.ICFileHome;
import com.idega.core.file.util.MimeTypeUtil;
import com.idega.data.IDOLookup;
import com.idega.io.DownloadWriter;
import com.idega.io.MediaWritable;
import com.idega.presentation.IWContext;
import com.idega.slide.business.IWSlideService;
import com.idega.user.data.User;
import com.idega.util.CoreConstants;
import com.idega.util.FileUtil;
import com.idega.util.ListUtil;

public class CommentAttachmentDownloader extends DownloadWriter implements MediaWritable {

	public static final String COMMENT_ID_PARAMETER = "commentId";
	public static final String COMMENT_ATTACHMENT_ID_PARAMETER = "commentAttachmentId";
	
	private String mimeType;
	
	private WebdavResource attachedFile;
	
	@Override
	public String getMimeType() {
		return mimeType;
	}

	@Override
	public void init(HttpServletRequest req, IWContext iwc) {
		String commentId = iwc.getParameter(COMMENT_ID_PARAMETER);
		String attachmentId = iwc.getParameter(COMMENT_ATTACHMENT_ID_PARAMETER);
		
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
		
		if (!markFileAsDownloaded(iwc, commentId, attachment)) {
			Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Error while marking attachment as downloaded: " + attachment.getFileUri());
			return;
		}
		
		setAsDownload(iwc, name, Long.valueOf(attachedFile.getGetContentLength()).intValue());
	}

	@Override
	public void writeTo(OutputStream out) throws IOException {
		InputStream in = attachedFile.getMethodData();
		
		FileUtil.streamToOutputStream(in, out);
		
		out.flush();
		out.close();
		in.close();
	}
	
	private boolean markFileAsDownloaded(IWContext iwc, String commentId, ICFile attachment) {
		User user = iwc.isLoggedOn() ? iwc.getCurrentUser() : null;
		if (user == null) {
			return false;
		}
		
		Collection<User> downloadedBy = attachment.getDownloadedBy();
		if (!ListUtil.isEmpty(downloadedBy) && downloadedBy.contains(user)) {
			return true;
		}
		
		try {
			attachment.addDownloadedBy(user);
			attachment.store();
			return true;
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	private boolean setResource(IWContext iwc, ICFile attachment) {
		try {
			IWSlideService slide = IBOLookup.getServiceInstance(iwc, IWSlideService.class);
			
			String uri = URLDecoder.decode(attachment.getFileUri(), CoreConstants.ENCODING_UTF8);
			attachedFile = slide.getWebdavResourceAuthenticatedAsRoot(uri);
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
