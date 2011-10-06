package com.idega.block.article.business;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.directwebremoting.WebContextFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.idega.block.article.bean.CommentsViewerProperties;
import com.idega.block.article.component.ArticleCommentAttachmentStatisticsViewer;
import com.idega.block.article.component.CommentsViewer;
import com.idega.block.article.data.Comment;
import com.idega.block.article.data.CommentHome;
import com.idega.block.article.media.CommentAttachmentDownloader;
import com.idega.builder.bean.AdvancedProperty;
import com.idega.business.file.FileDownloadNotificationProperties;
import com.idega.core.accesscontrol.business.AccessController;
import com.idega.core.accesscontrol.business.LoginBusinessBean;
import com.idega.core.accesscontrol.business.LoginSession;
import com.idega.core.business.DefaultSpringBean;
import com.idega.core.contact.data.Email;
import com.idega.core.file.data.ICFile;
import com.idega.core.file.data.ICFileHome;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWMainApplication;
import com.idega.io.MediaWritable;
import com.idega.presentation.IWContext;
import com.idega.user.business.NoEmailFoundException;
import com.idega.user.business.UserBusiness;
import com.idega.user.data.Group;
import com.idega.user.data.User;
import com.idega.util.CoreConstants;
import com.idega.util.CoreUtil;
import com.idega.util.ListUtil;
import com.idega.util.StringUtil;
import com.idega.util.URIUtil;
import com.idega.util.expression.ELUtil;
import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.feed.atom.Feed;
import com.sun.syndication.feed.atom.Person;
import com.sun.syndication.feed.module.DCModule;
import com.sun.syndication.feed.module.Module;
import com.sun.syndication.io.WireFeedOutput;

@Scope(BeanDefinition.SCOPE_SINGLETON)
@Service(DefaultCommentsPersistenceManager.BEAN_IDENTIFIER)
public class DefaultCommentsPersistenceManager extends DefaultSpringBean implements CommentsPersistenceManager {

	static final String BEAN_IDENTIFIER = "defaultCommentsPersistenceManager";

	private static final Logger LOGGER = Logger.getLogger(DefaultCommentsPersistenceManager.class.getName());

	private WireFeedOutput wfo = new WireFeedOutput();

	@Override
	public Object addComment(CommentsViewerProperties properties) {
		if (properties == null || properties.getEntryId() == null) {
			LOGGER.warning("Properties are not provided or entry ID is unknown: " + properties);
			return null;
		}

		try {
			boolean hasFullRightsForComments = hasFullRightsForComments(properties.getIdentifier());

			CommentHome commentHome = getCommentHome();
			Comment comment = commentHome.create();

			comment.setEntryId(properties.getEntryId());
			comment.setCommentHolder(String.valueOf(properties.getIdentifier()));

			boolean hasReplyToId = properties.getReplyForComment() == null ? false : properties.getReplyForComment() < 0 ? false : true;
			boolean privateComment = hasReplyToId || !hasFullRightsForComments;
			comment.setPrivateComment(privateComment);
			comment.setReplyForCommentId(properties.getReplyForComment());
			comment.setAnnouncedToPublic(hasFullRightsForComments && !privateComment);

			User author = getOldUser(getCurrentUser());
			if (author != null) {
				comment.setAuthorId(Integer.valueOf(author.getId()));
			}
			comment.store();

			addAttachment(properties, comment);

			return comment.getPrimaryKey();
		} catch(Exception e) {
			LOGGER.log(Level.WARNING, "Error creating " + Comment.class, e);
		}

		return null;
	}

	private boolean addAttachment(CommentsViewerProperties properties, Comment comment) {
		if (ListUtil.isEmpty(properties.getUploadedFiles())) {
			return true;
		}

		try {
			ICFileHome fileHome = getFileHome();
			for (String uploadedFile: properties.getUploadedFiles()) {
				if (!uploadedFile.startsWith(CoreConstants.WEBDAV_SERVLET_URI)) {
					uploadedFile = new StringBuilder(CoreConstants.WEBDAV_SERVLET_URI).append(uploadedFile).toString();
				}

				ICFile file = fileHome.create();

				file.setName(URLEncoder.encode(uploadedFile.substring(uploadedFile.lastIndexOf(CoreConstants.SLASH) + 1), CoreConstants.ENCODING_UTF8));
				file.setFileUri(URLEncoder.encode(uploadedFile, CoreConstants.ENCODING_UTF8));

				file.store();

				comment.addAttachment(file);
			}
			comment.store();
			return true;
		} catch(Exception e) {
			LOGGER.log(Level.WARNING, "Unable to add attachments: " + properties.getUploadedFiles(), e);
		}
		return false;
	}

	@Override
	public Feed getCommentsFeed(String processInstanceId) {
		throw new UnsupportedOperationException("This method is not implemented by default manager");
	}

	@Override
	public String getFeedSubtitle(IWContext iwc, String processInstanceId) {
		throw new UnsupportedOperationException("This method is not implemented by default manager");
	}

	@Override
	public String getFeedTitle(IWContext iwc, String processInstanceId) {
		throw new UnsupportedOperationException("This method is not implemented by default manager");
	}

	@Override
	public String getLinkToCommentsXML(String processInstanceId) {
		throw new UnsupportedOperationException("This method is not implemented by default manager");
	}

	@Override
	public User getUserAvailableToReadWriteCommentsFeed(IWContext iwc) {
		throw new UnsupportedOperationException("This method is not implemented by default manager");
	}

	@Override
	public boolean hasFullRightsForComments(String processInstanceId) {
		throw new UnsupportedOperationException("This method is not implemented by default manager");
	}

	@Override
	public boolean hasFullRightsForComments(Long processInstanceId) {
		throw new UnsupportedOperationException("This method is not implemented by default manager");
	}

	@Override
	public boolean hasRightsToViewComments(String processInstanceId) {
		throw new UnsupportedOperationException("This method is not implemented by default manager");
	}

	@Override
	public boolean hasRightsToViewComments(Long processInstanceId) {
		throw new UnsupportedOperationException("This method is not implemented by default manager");
	}

	@Override
	public boolean storeFeed(String processInstanceId, Feed comments) {
		throw new UnsupportedOperationException("This method is not implemented by default manager");
	}

	private CommentHome getCommentHome() {
		return getHomeForEntity(Comment.class);
	}

	@Override
	public Comment getComment(Object primaryKey) {
		if (primaryKey == null) {
			return null;
		}
		try {
			return getCommentHome().findByPrimaryKey(primaryKey);
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, "Nothing found by: " + primaryKey, e);
		}
		return null;
	}

	@Override
	public boolean markCommentAsRead(Object primaryKey) {
		Comment comment = getComment(primaryKey);
		if (comment == null) {
			return false;
		}

		User reader = getOldUser(getCurrentUser());
		if (reader == null) {
			return false;
		}

		try {
			comment.addReadBy(reader);
			comment.store();
		} catch(Exception e) {
			LOGGER.log(Level.WARNING, "Error marking comment ('"+primaryKey+"') as read by reader: " + reader, e);
			return false;
		}

		return true;
	}

	@Override
	public List<? extends Entry> getEntriesToFormat(Feed comments, CommentsViewerProperties properties) {
		throw new UnsupportedOperationException("This method is not implemented by default manager");
	}

	@Override
	public boolean setCommentPublished(Object primaryKey, boolean makePublic) {
		Comment comment = getComment(primaryKey);
		if (comment == null) {
			return false;
		}

		comment.setAnnouncedToPublic(makePublic);
		comment.store();

		return true;
	}

	@Override
	public boolean setCommentRead(Object primaryKey) {
		Comment comment = getComment(primaryKey);
		if (comment == null) {
			return false;
		}

		User currentUser = getOldUser(getCurrentUser());
		if (currentUser == null) {
			return false;
		}

		try {
			comment.addReadBy(currentUser);
			comment.store();
		} catch(Exception e) {
			LOGGER.log(Level.WARNING, "Error adding user " + currentUser + " as have red comment: " + primaryKey, e);
		}

		return true;
	}

	@Override
	public String getCommentFilesPath(CommentsViewerProperties properties) {
		throw new UnsupportedOperationException("This method is not implemented by default manager");
	}

	@Override
	public boolean isCommentsCreationEnabled(CommentsViewerProperties properties) {
		return true;
	}

	@Override
	public String getTaskNameForAttachments() {
		LOGGER.info("This method is not implemented by default manager");
		return null;
	}

	private ICFileHome getFileHome() {
		return getHomeForEntity(ICFile.class);
	}

	@Override
	public ICFile getCommentAttachment(String icFileId) {
		try {
			ICFileHome fileHome = getFileHome();
			return fileHome.findByPrimaryKey(icFileId);
		} catch(Exception e) {
			LOGGER.log(Level.WARNING, "Error getting ICFile: " + icFileId, e);
		}
		return null;
	}

	@Override
	public String getUriToAttachment(String commentId, ICFile attachment, User user) {
		URIUtil uri = new URIUtil(IWMainApplication.getDefaultIWMainApplication().getMediaServletURI());

		uri.setParameter(MediaWritable.PRM_WRITABLE_CLASS, IWMainApplication.getEncryptedClassName(CommentAttachmentDownloader.class));
		uri.setParameter(ArticleCommentAttachmentStatisticsViewer.COMMENT_ID_PARAMETER, commentId);
		uri.setParameter(ArticleCommentAttachmentStatisticsViewer.COMMENT_ATTACHMENT_ID_PARAMETER, attachment.getPrimaryKey().toString());

		if (user != null) {
			uri.setParameter(LoginBusinessBean.PARAM_LOGIN_BY_UNIQUE_ID, user.getUniqueId());
			uri.setParameter(LoginBusinessBean.LoginStateParameter, LoginBusinessBean.LOGIN_EVENT_LOGIN);
		}

		return uri.getUri();
	}

	@Override
	public boolean isNotificationsAutoEnabled(CommentsViewerProperties properties) {
		return false;
	}

	@Override
	public List<String> getPersonsToNotifyAboutComment(CommentsViewerProperties properties, Object commentId, boolean justPublished) {
		throw new UnsupportedOperationException("This method is not implemented by default manager");
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<String> getEmails(List<? extends Entry> entries, String commentAuthorEmail) {
		if (entries == null) {
			return null;
		}

		if (commentAuthorEmail == null) {
			commentAuthorEmail = CoreConstants.EMPTY;
		}

		List<String> emails = new ArrayList<String>();
		List<Person> authors = null;
		String email = null;
		for (Entry entry: entries) {
			authors = entry.getAuthors();
			if (authors != null) {
				for (Person author: authors) {
					email = author.getEmail();
					if (email != null) {
						email = CoreUtil.getDecodedValue(email);
						if (!email.equals(commentAuthorEmail) && !emails.contains(email)) {
							emails.add(email);
						}
					}
				}
			}
		}

		return emails;
	}

	@SuppressWarnings("unchecked")
	protected List<String> getAllFeedSubscribers(String processInstanceId, Integer authorId) {
		Feed comments = getCommentsFeed(processInstanceId);
		if (comments == null) {
			return null;
		}

		return getEmails(comments.getEntries(), getUserMail(authorId));
	}

	protected String getUserMail(Integer userId) {
		if (userId == null) {
			return null;
		}

		UserBusiness userBusiness = getUserBusiness();
		if (userBusiness == null) {
			return null;
		}

		User user = null;
		try {
			user = userBusiness.getUser(userId);
		} catch(Exception e) {
			LOGGER.log(Level.WARNING, "Error getting user by id: " + userId, e);
		}
		if (user == null) {
			return null;
		}

		Email email = getEmail(user);
		return email == null ? null : email.getEmailAddress();
	}

	private Email getEmail(User user) {
		try {
			return getUserBusiness().getUsersMainEmail(user);
		} catch(NoEmailFoundException e) {
		} catch(Exception e) {
			LOGGER.log(Level.WARNING, "Error getting main email for user: " + user, e);
		}
		return null;
	}

	protected List<String> getEmails(Collection<User> users) {
		if (ListUtil.isEmpty(users)) {
			return null;
		}

		UserBusiness userBusiness = getUserBusiness();
		if (userBusiness == null) {
			return null;
		}

		List<String> emails = new ArrayList<String>(users.size());
		for (User user: users) {
			Email email = getEmail(user);
			String emailAddress = email == null ? null : email.getEmailAddress();
			if (!StringUtil.isEmpty(emailAddress) && !emails.contains(emailAddress)) {
				emails.add(emailAddress);
			}
		}

		return emails;
	}

	protected List<User> getUsersHavingHandlerRole() {
		String roleKey = getHandlerRoleKey();
		if (StringUtil.isEmpty(roleKey)) {
			return null;
		}

		IWApplicationContext iwac = IWMainApplication.getDefaultIWApplicationContext();
		AccessController accessControler = IWMainApplication.getDefaultIWMainApplication().getAccessController();
		Collection<Group> groupsWithRole = accessControler.getAllGroupsForRoleKeyLegacy(roleKey, iwac);
		if (ListUtil.isEmpty(groupsWithRole)) {
			return null;
		}

		UserBusiness userBusiness = getUserBusiness();
		if (userBusiness == null) {
			return null;
		}

		List<User> users = new ArrayList<User>();
		for (Group group: groupsWithRole) {
			if (group instanceof User) {
				User user = (User) group;
				if (!users.contains(user)) {
					users.add(user);
				}
			} else {
				Collection<User> usersInGroup = null;
				try {
					usersInGroup = userBusiness.getUsersInGroup(group);
				} catch(Exception e) {
					LOGGER.log(Level.WARNING, "Error getting users in group: " + group, e);
				}
				if (!ListUtil.isEmpty(usersInGroup)) {
					for (User user: usersInGroup) {
						if (!users.contains(user)) {
							users.add(user);
						}
					}
				}
			}
		}

		return users;
	}

	protected List<User> getCaseHandlers(String identifier) {
		throw new UnsupportedOperationException("This method is not implemented by default manager");
	}

	protected UserBusiness getUserBusiness() {
		return getServiceInstance(UserBusiness.class);
	}

	@Override
	public String getHandlerRoleKey() {
		throw new UnsupportedOperationException("This method is not implemented by default manager");
	}

	@Override
	public boolean canWriteComments(CommentsViewerProperties properties) {
		return true;
	}

	@Override
	public List<AdvancedProperty> getLinksForRecipients(List<String> recipients, CommentsViewerProperties properties) {
		List<AdvancedProperty> links = new ArrayList<AdvancedProperty>(recipients.size());
		for (String recipient: recipients) {
			links.add(new AdvancedProperty(recipient, getLinkForRecipient(recipient, properties)));
		}
		return links;
	}

	protected String getLinkForRecipient(String recipient, CommentsViewerProperties properties) {
		return properties.getCommentsPageUrl();
	}

	@Override
	public Map<String, String> getUriToDocument(FileDownloadNotificationProperties properties, String identifier, List<User> users) {
		if (properties == null || ListUtil.isEmpty(users)) {
			return null;
		}

		Map<String, String> uris = new HashMap<String, String>(users.size());
		for (User user: users) {
			uris.put(user.getId(), properties.getUrl());
		}
		return uris;
	}

	@Override
	public String getUriForCommentLink(CommentsViewerProperties properties) {
		try {
			URIUtil uri = new URIUtil(WebContextFactory.get().getCurrentPage());
			uri.setParameter(CommentsViewer.AUTO_SHOW_COMMENTS, Boolean.TRUE.toString());
			return uri.getUri();
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, "Error getting link for comment", e);
		}
		return null;
	}

	@Override
	@SuppressWarnings("unchecked")
	public String getFeedContent(Feed feed) {
		if (feed == null) {
			LOGGER.warning("Feed is unknown!");
			return null;
		}

		Date updated = new Date(System.currentTimeMillis());
		feed.setUpdated(updated);
		List<Module> modules = feed.getModules();
		if (!ListUtil.isEmpty(modules)) {
			for (Module module: modules) {
				if (module instanceof DCModule) {
					((DCModule) module).setDate(updated);
				}
			}
		}

		try {
			return wfo.outputString(feed);
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, "Error while outputing feed to string: " + feed, e);
			return null;
		}
	}

	@Override
	protected com.idega.user.data.bean.User getCurrentUser() {
		try {
			LoginSession loginSession = ELUtil.getInstance().getBean(LoginSession.class);
			return loginSession.isLoggedIn() ? loginSession.getUser() : null;
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, "Error getting logged in user", e);
		}
		return null;
	}

	@Override
	public boolean hasRightsToWriteComments(Long identifier) {
		com.idega.user.data.bean.User currentUser = getCurrentUser();
		return currentUser != null;
	}
}