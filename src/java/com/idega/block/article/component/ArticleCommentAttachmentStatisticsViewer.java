package com.idega.block.article.component;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import com.idega.block.article.business.ArticleConstants;
import com.idega.block.article.business.CommentsPersistenceManager;
import com.idega.block.article.data.Comment;
import com.idega.core.contact.data.Email;
import com.idega.core.file.data.ICFile;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.Block;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.Table2;
import com.idega.presentation.TableBodyRowGroup;
import com.idega.presentation.TableHeaderRowGroup;
import com.idega.presentation.TableRow;
import com.idega.presentation.text.Break;
import com.idega.presentation.text.Heading1;
import com.idega.presentation.text.Heading3;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.GenericButton;
import com.idega.user.data.User;
import com.idega.util.CoreConstants;
import com.idega.util.ListUtil;
import com.idega.util.StringUtil;
import com.idega.util.expression.ELUtil;

public class ArticleCommentAttachmentStatisticsViewer extends Block {

	public static final String COMMENT_ID_PARAMETER = "commentId";
	public static final String COMMENT_ATTACHMENT_ID_PARAMETER = "commentAttachmentId";
	public static final String IDENTIFIER_PARAMETER = "identifier";
	public static final String BEAN_IDENTIFIER_PARAMETER = "beanIdentifier";
	
	private CommentsPersistenceManager manager;
	
	@Override
	public void main(IWContext iwc) throws Exception {
		IWResourceBundle iwrb = getResourceBundle(iwc);
	
		Layer container = new Layer();
		add(container);
		container.setStyleClass("articleCommentAttachmentStatisticsViewerStyle");
		
		if (!hasRights(iwc)) {
			container.add(new Heading1(iwrb.getLocalizedString("attachment_stats.no_rights", "Sorry, you do not have rights to view statistics")));
			return;
		}
		
		Comment comment = manager.getComment(iwc.getParameter(COMMENT_ID_PARAMETER));
		if (comment == null) {
			container.add(new Heading1(iwrb.getLocalizedString("attachment_stats.no_info", "Sorry, requested information was not found")));
			return;
		}
		
		Collection<ICFile> attachments = comment.getAllAttachments();
		if (ListUtil.isEmpty(attachments)) {
			container.add(new Heading1(iwrb.getLocalizedString("attachment_stats.no_attachments", "There are no attachments for selected comment")));
			return;
		}
		
		ICFile attachment = getAttachment(attachments, iwc.getParameter(COMMENT_ATTACHMENT_ID_PARAMETER));
		if (attachment == null) {
			container.add(new Heading1(iwrb.getLocalizedString("attachment_stats.no_info", "Sorry, requested information was not found")));
			return;
		}
		
		Collection<User> downloaders = attachment.getDownloadedBy();
		if (ListUtil.isEmpty(downloaders)) {
			container.add(new Heading1(iwrb.getLocalizedString("attachment_stats.no_downloads_yet", "File was not downloaded yet")));
			addNotifier(iwc, container, comment, attachment, null);
			return;
		}
		
		container.add(new Heading3(new StringBuilder(iwrb.getLocalizedString("attachment_stats.downloads_statistics_for", "Download statistics for")).append(": ")
				.append(URLDecoder.decode(attachment.getName(), CoreConstants.ENCODING_UTF8)).toString()));
		
		container.add(new Break());
		
		Table2 table = new Table2();
		container.add(table);
		TableHeaderRowGroup headerRows = table.createHeaderRowGroup();
		TableRow row = headerRows.createRow();
		row.createCell().add(new Text(iwrb.getLocalizedString("attachment_stats.nr", "Nr")));
		row.createCell().add(new Text(iwrb.getLocalizedString("attachment_stats.name", "Name")));
		row.createCell().add(new Text(iwrb.getLocalizedString("attachment_stats.personal_id", "Personal ID")));
		row.createCell().add(new Text(iwrb.getLocalizedString("attachment_stats.email", "E-mail")));
		
		int index = 0;
		TableBodyRowGroup bodyRows = table.createBodyRowGroup();
		for (User downloader: downloaders) {
			row = bodyRows.createRow();
			
			row.createCell().add(new Text(new StringBuilder().append(index + 1).append(CoreConstants.DOT).toString()));
			row.createCell().add(new Text(downloader.getName()));
			row.createCell().add(new Text(downloader.getPersonalID()));
			String emailAddress = getEmailAddress(iwc, downloader);
			row.createCell().add(StringUtil.isEmpty(emailAddress) ?
				new Text(CoreConstants.MINUS) :
				new Link(emailAddress, new StringBuilder("mailto:").append(emailAddress).toString()));
			
			index++;
		}
		
		addNotifier(iwc, container, comment, attachment, downloaders);
	}
	
	private void addNotifier(IWContext iwc, Layer container, Comment comment, ICFile attachment, Collection<User> downloaders) {
		IWResourceBundle iwrb = getResourceBundle(iwc);
		
		Layer notificationContainer = new Layer();
		container.add(notificationContainer);
		
		Collection<User> readers = comment.getReadBy();
		if (ListUtil.isEmpty(readers)) {
			notificationContainer.setStyleClass("articleCommentAttachmentNotificationContainerCommentWasNotRed");
			notificationContainer.add(new Heading3(iwrb.getLocalizedString("attachment_stats.there_are_no_readers_for_comment", "Comment was not red yet")));
			return;
		}
		
		notificationContainer.setStyleClass("articleCommentAttachmentNotificationContainer");
		
		Collection<User> usersToInform = null;
		if (ListUtil.isEmpty(downloaders)) {
			usersToInform = readers;
		} else {
			usersToInform = new ArrayList<User>();
			for (User reader: readers) {
				if (!downloaders.contains(reader)) {
					usersToInform.add(reader);
				}
			}
		}
		if (ListUtil.isEmpty(usersToInform)) {
			return;
		}
		
		GenericButton sendNotification = new GenericButton("sendNotifications",
				iwrb.getLocalizedString("attachment_stats.send_remind", "Remind readers to download this document"));
		notificationContainer.add(sendNotification);
		StringBuilder action = new StringBuilder("CommentsViewer.sendNotificationsToDownloadDocument({comment: '").append(comment.getPrimaryKey().toString())
		.append("', file: '").append(attachment.getId()).append("', users: [");
		for (Iterator<User> usersIter = usersToInform.iterator(); usersIter.hasNext();) {
			action.append(CoreConstants.QOUTE_SINGLE_MARK).append(usersIter.next().getId()).append(CoreConstants.QOUTE_SINGLE_MARK);
			
			if (usersIter.hasNext()) {
				action.append(CoreConstants.COMMA);
			}
		}
		action.append("]});");
		notificationContainer.setOnClick(action.toString());
	}
	
	@SuppressWarnings("unchecked")
	private String getEmailAddress(IWContext iwc, User user) {
		Collection<Email> mails = user.getEmails();
		if (ListUtil.isEmpty(mails)) {
			return null;
		}
		return mails.iterator().next().getEmailAddress();
	}
	
	private ICFile getAttachment(Collection<ICFile> allAttachments, String id) {
		for (ICFile attachment: allAttachments) {
			if (id.equals(attachment.getPrimaryKey().toString())) {
				return attachment;
			}
		}
		return null;
	}
	
	private boolean hasRights(IWContext iwc) {
		try {
			manager = ELUtil.getInstance().getBean(iwc.getParameter(BEAN_IDENTIFIER_PARAMETER));
		} catch(Exception e) {
			e.printStackTrace();
		}
		return manager == null ? false : manager.hasFullRightsForComments(iwc.getParameter(IDENTIFIER_PARAMETER));
	}
	
	@Override
	public String getBundleIdentifier() {
		return ArticleConstants.IW_BUNDLE_IDENTIFIER;
	}
}
