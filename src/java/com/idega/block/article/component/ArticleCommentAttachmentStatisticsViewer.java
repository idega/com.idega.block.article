package com.idega.block.article.component;

import java.util.Collection;
import java.util.Iterator;

import com.idega.block.article.business.ArticleConstants;
import com.idega.block.article.business.CommentsPersistenceManager;
import com.idega.block.article.data.Comment;
import com.idega.core.file.data.ICFile;
import com.idega.presentation.IWContext;
import com.idega.presentation.file.FileDownloadStatisticsViewer;
import com.idega.user.data.User;
import com.idega.util.CoreConstants;
import com.idega.util.ListUtil;
import com.idega.util.StringUtil;
import com.idega.util.expression.ELUtil;

public class ArticleCommentAttachmentStatisticsViewer extends FileDownloadStatisticsViewer {

	public static final String COMMENT_ID_PARAMETER = "commentId";
	public static final String COMMENT_ATTACHMENT_ID_PARAMETER = "commentAttachmentId";
	public static final String IDENTIFIER_PARAMETER = "identifier";
	public static final String BEAN_IDENTIFIER_PARAMETER = "beanIdentifier";
	
	private Comment comment;
	
	private CommentsPersistenceManager manager;
	
	@Override
	public String getNotifierAction(IWContext iwc, ICFile attachment, Collection<User> usersToInform) {
		Comment comment = getComment(iwc);
		if (comment == null) {
			return CoreConstants.EMPTY;
		}
		
		String managerIdentifier = iwc.getParameter(BEAN_IDENTIFIER_PARAMETER);
		
		StringBuilder action = new StringBuilder("CommentsViewer.sendNotificationsToDownloadDocument({comment: '").append(comment.getPrimaryKey().toString())
		.append("', file: '").append(attachment.getId()).append("', users: [");
		for (Iterator<User> usersIter = usersToInform.iterator(); usersIter.hasNext();) {
			action.append(CoreConstants.QOUTE_SINGLE_MARK).append(usersIter.next().getId()).append(CoreConstants.QOUTE_SINGLE_MARK);
			
			if (usersIter.hasNext()) {
				action.append(CoreConstants.COMMA);
			}
		}
		action.append("], commentsManagerIdentifier: ").append(StringUtil.isEmpty(managerIdentifier) ? "null" : "'"+managerIdentifier+"'").append("});");
		
		return action.toString();
	}
	
	@Override
	protected ICFile getFile(IWContext iwc) {
		ICFile attachment = super.getFile(iwc);
		if (attachment != null) {
			return attachment;
		}
		
		Comment comment = getComment(iwc);
		if (comment == null) {
			return null;
		}
		
		Collection<ICFile> attachments = comment.getAllAttachments();
		if (ListUtil.isEmpty(attachments)) {
			return null;
		}
		
		attachment = getAttachment(attachments, iwc.getParameter(COMMENT_ATTACHMENT_ID_PARAMETER));
		if (attachment != null) {
			setFile(attachment);
		}
		
		return attachment;
	}
	
	private Comment getComment(IWContext iwc) {
		if (comment == null) {
			CommentsPersistenceManager manager = getManager(iwc);
			comment = manager == null ? null : manager.getComment(iwc.getParameter(COMMENT_ID_PARAMETER));
		}
		return comment;
	}
		
	private ICFile getAttachment(Collection<ICFile> allAttachments, String id) {
		for (ICFile attachment: allAttachments) {
			if (id.equals(attachment.getPrimaryKey().toString())) {
				return attachment;
			}
		}
		return null;
	}
	
	private CommentsPersistenceManager getManager(IWContext iwc) {
		if (manager == null) {
			try {
				manager = ELUtil.getInstance().getBean(iwc.getParameter(BEAN_IDENTIFIER_PARAMETER));
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		return manager;
	}
	
	@Override
	public boolean hasRights(IWContext iwc) {
		CommentsPersistenceManager manager = getManager(iwc);
		return manager == null ? false : manager.hasFullRightsForComments(iwc.getParameter(IDENTIFIER_PARAMETER));
	}
	
	@Override
	public String getBundleIdentifier() {
		return ArticleConstants.IW_BUNDLE_IDENTIFIER;
	}

	@Override
	public Collection<User> getPotentialDownloaders(IWContext iwc) {
		Comment comment = getComment(iwc);
		if (comment == null) {
			return null;
		}
		
		Collection<User> commentReaders = comment.getReadBy();
		
		setFileHolderIdentifier(comment.getCommentHolder());
		Collection<User> potentialReaders = super.getPotentialDownloaders(iwc);
		if (ListUtil.isEmpty(potentialReaders)) {
			return commentReaders;
		}
		
		for (User commentReader: commentReaders) {
			if (!potentialReaders.contains(commentReader)) {
				potentialReaders.add(commentReader);
			}
		}
		
		return potentialReaders;
	}
}
