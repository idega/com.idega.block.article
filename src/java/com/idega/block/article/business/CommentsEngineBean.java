package com.idega.block.article.business;

import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.directwebremoting.ScriptBuffer;
import org.directwebremoting.WebContextFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.idega.block.article.ArticleCacher;
import com.idega.block.article.bean.ArticleComment;
import com.idega.block.article.bean.CommentAttachmentNotifyBean;
import com.idega.block.article.bean.CommentEntry;
import com.idega.block.article.bean.CommentsViewerProperties;
import com.idega.block.article.component.CommentCreator;
import com.idega.block.article.data.Comment;
import com.idega.block.rss.business.RSSBusiness;
import com.idega.builder.bean.AdvancedProperty;
import com.idega.builder.business.BuilderLogicWrapper;
import com.idega.business.IBOLookup;
import com.idega.business.IBOSessionBean;
import com.idega.business.file.FileDownloadNotifier;
import com.idega.content.bean.ContentItemFeedBean;
import com.idega.content.business.ContentConstants;
import com.idega.content.business.ContentUtil;
import com.idega.content.themes.helpers.business.ThemesHelper;
import com.idega.core.builder.business.BuilderService;
import com.idega.core.cache.IWCacheManager2;
import com.idega.core.component.bean.RenderedComponent;
import com.idega.core.contact.data.Email;
import com.idega.dwr.reverse.ScriptCaller;
import com.idega.idegaweb.IWMainApplication;
import com.idega.idegaweb.IWMainApplicationSettings;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.IWContext;
import com.idega.user.business.NoEmailFoundException;
import com.idega.user.business.UserBusiness;
import com.idega.user.data.User;
import com.idega.util.CoreConstants;
import com.idega.util.CoreUtil;
import com.idega.util.IWTimestamp;
import com.idega.util.ListUtil;
import com.idega.util.StringHandler;
import com.idega.util.StringUtil;
import com.idega.util.expression.ELUtil;
import com.sun.syndication.feed.atom.Content;
import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.feed.atom.Feed;
import com.sun.syndication.feed.atom.Link;
import com.sun.syndication.feed.atom.Person;
import com.sun.syndication.feed.module.DCModule;
import com.sun.syndication.feed.module.DCModuleImpl;
import com.sun.syndication.feed.synd.SyndFeed;

public class CommentsEngineBean extends IBOSessionBean implements CommentsEngine {

	private static final long serialVersionUID = 7299800648381936213L;
	private static final Logger LOGGER = Logger.getLogger(CommentsEngineBean.class.getName());

	private static final String COMMENTS_CACHE_NAME = "article_comments_feeds_cache";

	@Autowired
	private ThemesHelper themesHelper;

	private FileDownloadNotifier downloadNotifier;

	@Autowired
	private BuilderLogicWrapper builderLogicWrapper;

	@Override
	public boolean addComment(CommentsViewerProperties properties) {
		if (properties == null) {
			LOGGER.log(Level.INFO, "Comment properties are undefined!");
			return false;
		}

		String uri = properties.getUri();
		String subject = properties.getSubject();
		String user = properties.getUser();
		String instanceId = properties.getInstanceId();
		String body = properties.getBody();
		String commentAuthorEmail = properties.getEmail();
		String errorMessage = "Unable to add comment: '" + subject + "' by: " + user;
		boolean notify = properties.isNotify();

		if (uri == null) {
			closeLoadingMessage();
			LOGGER.log(Level.SEVERE, errorMessage);
			return false;
		}
		if (ContentConstants.EMPTY.equals(uri)) {
			closeLoadingMessage();
			LOGGER.log(Level.SEVERE, errorMessage);
			return false;
		}

		IWContext iwc = CoreUtil.getIWContext();
		if (iwc == null) {
			LOGGER.log(Level.SEVERE, errorMessage);
			return false;
		}

		CommentsPersistenceManager commentsManager = getCommentsManager(properties.getSpringBeanIdentifier());

		if (commentsManager == null) {
			uri = getFixedCommentsUri(uri, instanceId, properties.getCurrentPageUri());
			properties.setUri(uri);
		}

		String language = getThemesHelper().getCurrentLanguage(iwc);
		Timestamp date = IWTimestamp.getTimestampRightNow();
		Feed comments = getCommentsFeed(properties);
		if (comments == null) {
			String feedTitle = commentsManager == null ? properties.getTitle() : commentsManager.getFeedTitle(iwc, properties.getIdentifier());
			String feedSubtitle = commentsManager == null ? properties.getSubtitle() : commentsManager.getFeedSubtitle(iwc, properties.getIdentifier());
			comments = createFeed(uri, user, date, language, iwc, feedTitle, feedSubtitle, commentsManager);
		}
		if (comments == null) {
			LOGGER.log(Level.SEVERE, errorMessage);
			return false;
		}

		String entryId = addNewEntry(comments, subject, getUriForCommentLink(properties), date, body, user, language, commentAuthorEmail, notify);
		if (entryId == null) {
			LOGGER.log(Level.SEVERE, errorMessage);
			return false;
		}
		properties.setEntryId(entryId);

		//	Caching XML
		if (commentsManager == null) {
			putFeedToCache(comments, uri);
		}

		//	Clearing cache for articles (ALWAYS)
		if (commentsManager == null) {
			clearArticleCache();
		}

		//	Uploading changed XML for comments
		boolean finishedSuccessfully = commentsManager == null ? uploadFeed(uri, comments, iwc, true) :
																commentsManager.storeFeed(properties.getIdentifier(), comments);
		if (!finishedSuccessfully) {
			LOGGER.log(Level.SEVERE, errorMessage);
			return false;
		}

		//	Adding entry to DB about new comment
		Object commentId = null;
		if (commentsManager != null) {
			commentId = commentsManager.addComment(properties);
			if (commentId == null) {
				LOGGER.warning("Unable to add entry to " + Comment.class + " by properties: " + properties);
				finishedSuccessfully = false;
			}
		}

		if (!finishedSuccessfully) {
			LOGGER.log(Level.SEVERE, errorMessage);
			return false;
		}

		//	Updating clients with the newest comments
		ScriptCaller scriptCaller = new ScriptCaller(WebContextFactory.get(), new ScriptBuffer("getUpdatedCommentsFromServer(").appendData(properties.getId())
				.appendScript(");"), true);
		executeScriptForAllPages(scriptCaller, false);

		//	Sending notifications (if needed) about new comment
		if (commentsManager == null) {
			sendNotification(comments, commentAuthorEmail, iwc, properties);
		} else {
			sendNotification(commentsManager.getPersonsToNotifyAboutComment(properties, commentId, false), commentAuthorEmail, iwc, properties);
		}

		return finishedSuccessfully;
	}

	private String getUriForCommentLink(CommentsViewerProperties properties) {
		CommentsPersistenceManager commentsManager = getCommentsManager(properties.getSpringBeanIdentifier());
		if (commentsManager == null) {
			commentsManager = getDefaultCommentsManager();
		}
		return commentsManager.getUriForCommentLink(properties);
	}

	private void clearArticleCache() {
		Map<String, String> articlesCache = getArticlesCache();
		if (articlesCache == null) {
			Logger.getLogger(CommentsEngineBean.class.getName()).log(Level.WARNING, "Aticle cache is null, can not clear it!");
		}
		else {
			articlesCache.clear();
		}
	}

	@SuppressWarnings("unchecked")
	private boolean sendNotification(Feed comments, String commentAuthorEmail, IWContext iwc, CommentsViewerProperties properties) {
		if (comments == null) {
			return false;
		}

		List<String> recipients = getDefaultCommentsManager().getEmails(comments.getEntries(), commentAuthorEmail);
		return sendNotification(recipients, commentAuthorEmail, iwc, properties);
	}

	private boolean sendNotification(List<String> recipients, String commentAuthorEmail, IWContext iwc, CommentsViewerProperties properties) {
		return sendNotification(recipients, commentAuthorEmail, iwc, properties, false);
	}

	private boolean sendNotification(List<String> recipients, String commentAuthorEmail, IWContext iwc, CommentsViewerProperties properties,
			boolean useAuthorEmailAsFrom) {
		if (ListUtil.isEmpty(recipients)) {
			return false;
		}

		recipients = new ArrayList<String>(recipients);

		if (!StringUtil.isEmpty(commentAuthorEmail) && recipients.contains(commentAuthorEmail)) {
			recipients.remove(commentAuthorEmail);
		}

		String newCommentMessage = getLocalizedString(iwc, "comments_viewer.new_comment_message", "New comment was entered. You can read all comments at");
		String newComment = getLocalizedString(iwc, "comments_viewer.new_comment", "New comment");
		if (!StringUtil.isEmpty(properties.getSubject())) {
			newComment = new StringBuilder(newComment).append(": ").append(properties.getSubject()).toString();
		}

		StringBuilder body = new StringBuilder(newCommentMessage).append(CoreConstants.COLON).append(CoreConstants.SPACE);
		CommentsPersistenceManager manager = getCommentsManager(properties.getSpringBeanIdentifier());
		if (manager == null) {
			manager = getDefaultCommentsManager();
		}
		List<AdvancedProperty> recipientsWithLinks = manager.getLinksForRecipients(recipients, properties);

		return sendNotification(recipientsWithLinks, newComment, body.toString(), useAuthorEmailAsFrom ? commentAuthorEmail : null);
	}

	private boolean sendNotification(List<AdvancedProperty> recipients, String subject, String message, String from) {
		if (ListUtil.isEmpty(recipients) || StringUtil.isEmpty(subject) || StringUtil.isEmpty(message)) {
			return false;
		}

		try {
			IWMainApplicationSettings settings = IWMainApplication.getDefaultIWMainApplication().getSettings();
			String host = settings.getProperty(CoreConstants.PROP_SYSTEM_SMTP_MAILSERVER);
			from = StringUtil.isEmpty(from) ? settings.getProperty(CoreConstants.PROP_SYSTEM_MAIL_FROM_ADDRESS) : from;
			if (StringUtil.isEmpty(from)) {
				LOGGER.warning("Address 'from' is not defined, unable to send message: " + subject + " to: " + recipients);
				return false;
			}

			Thread sender = new Thread(new CommentsNotificationSender(recipients, from, subject, message, host));
			sender.start();
			return true;
		} catch(Exception e) {
			LOGGER.log(Level.WARNING, "Error sending message: " + subject + " to: " + recipients, e);
		}
		return false;
	}

	private String addNewEntry(Feed feed, String subject, String uri, Timestamp date, String body, String user, String language, String email, boolean notify) {
		if (feed == null) {
			return null;
		}

		List<Entry> entries = initEntries(feed.getEntries());
		Entry entry = new Entry();

		// Title
		entry.setTitle(subject);

		// Summary
		Content summary = new Content();
		summary.setType("html");
		summary.setValue(getShortBody(body));
		entry.setSummary(summary);

		// Body of comment
		Content comment = new Content();
		comment.setType("html");
		comment.setValue(body);
		List<Content> comments = new ArrayList<Content>();
		comments.add(comment);
		entry.setContents(comments);

		// Dates
		entry.setUpdated(date);
		entry.setCreated(date);
		entry.setPublished(date);
		entry.setModified(date);

		// Author & Email
		Person author = new Person();
		author.setName(user);
		if (notify) {
			author.setEmail(encodeMail(email));
		}
		List<Person> authors = new ArrayList<Person>();
		authors.add(author);
		entry.setAuthors(authors);

		// ID
		String id = getThemesHelper().getUniqueIdByNumberAndDate(ContentConstants.COMMENT_SCOPE);
		entry.setId(id);

		if (!StringUtil.isEmpty(uri)) {
			// URI
			Link link = new Link();
			link.setHref(uri);
			List<Link> links = new ArrayList<Link>();
			links.add(link);
			entry.setAlternateLinks(links);
		}

		//	DC module
		DCModule dcModule = new DCModuleImpl();
		dcModule.setCreator(user);
		dcModule.setDate(date);
		entry.setModules(Arrays.asList(dcModule));

		entries.add(entry);
		feed.setEntries(entries);

		return id;
	}

	private List<Entry> initEntries(@SuppressWarnings("rawtypes") List oldEntries) {
		if (ListUtil.isEmpty(oldEntries)) {
			return new ArrayList<Entry>();
		}

		List<Entry> entries = new ArrayList<Entry>();
		Object o = null;
		for (int i = 0; i < oldEntries.size(); i++) {
			o = oldEntries.get(i);
			if (o instanceof Entry) {
				entries.add((Entry) o);
			}
		}
		return entries;
	}

	private Feed createFeed(String uri, String user, Timestamp date, String language, IWContext iwc, String feedTitle, String feedSubtitle,
							CommentsPersistenceManager commentsManager) {
		String serverName = getThemesHelper().getFullServerName(iwc);

		String articeComments = StringUtil.isEmpty(feedTitle) ? getLocalizedString(iwc, "comments_viewer.article_comments", "Comments of Article") : feedTitle;
		String allArticleComments = StringUtil.isEmpty(feedSubtitle) ?
				getLocalizedString(iwc, "comments_viewer.all_article_comments", "All comments") :
				feedSubtitle;

		Feed comments = new Feed();
		comments.setFeedType(ContentItemFeedBean.FEED_TYPE_ATOM_1);

		// Title
		comments.setTitle(articeComments);

		// Subtitle
		Content subtitle = new Content();
		subtitle.setValue(allArticleComments);
		comments.setSubtitle(subtitle);

		// Language
		comments.setLanguage(language);

		// Dates
		comments.setModified(date);
		comments.setUpdated(date);

		// ID
		comments.setId(serverName + CoreConstants.WEBDAV_SERVLET_URI + uri);

		// Author
		Person author = new Person();
		author.setName(user);
		List<Person> authors = new ArrayList<Person>();
		authors.add(author);
		comments.setAuthors(authors);

		// Link
		Link link = new Link();
		link.setHref(serverName);
		List<Link> links = new ArrayList<Link>();
		links.add(link);
		comments.setAlternateLinks(links);

		if (commentsManager == null) {
			putFeedToCache(comments, uri);
		}

		return comments;
	}

	private String getShortBody(String body) {
		if (body == null) {
			return ContentConstants.EMPTY;
		}
		if (body.length() >= 200) {
			StringBuffer shortBody = new StringBuffer(body.substring(0, 200)).append(ContentConstants.DOT);
			shortBody.append(ContentConstants.DOT).append(ContentConstants.DOT);
			return shortBody.toString();
		}
		return body;
	}

	private RSSBusiness getRSSBusiness() {
		try {
			return IBOLookup.getServiceInstance(getIWApplicationContext(), RSSBusiness.class);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error getting: " + RSSBusiness.class, e);
		}
		return null;
	}

	/**
	 * Updates particular comments block in client's browser(s)
	 * @param uri - link to comments
	 * @param id - comments group id
	 * @return
	 */
	@Override
	public boolean getCommentsForAllPages(CommentsViewerProperties properties) {
		return executeScriptForAllPages(getScriptForCommentsList(properties));
	}

	private ScriptBuffer getScriptForCommentsList(CommentsViewerProperties properties) {
		if (properties == null) {
			LOGGER.log(Level.WARNING, "Can not create ScriptBuffer: properties unknown");
			return null;
		}

		ScriptBuffer script = new ScriptBuffer();
		script = new ScriptBuffer("getCommentsCallback(").appendData(getCommentsList(properties)).appendScript(");");
		return script;
	}

	@Override
	public List<List<ArticleComment>> getCommentsFromUris(List<CommentsViewerProperties> commentsProperties) {
		if (ListUtil.isEmpty(commentsProperties)) {
			return null;
		}

		List<List<ArticleComment>> allComments = new ArrayList<List<ArticleComment>>();
		for (CommentsViewerProperties commentProperty: commentsProperties) {
			commentProperty.setAddNulls(false);
			commentProperty.setFetchFully(true);
			allComments.add(getCommentsEntries(commentProperty));
		}
		return allComments;
	}

	@Override
	public List<ArticleComment> getComments(CommentsViewerProperties properties) {
		return getCommentsList(properties);
	}

	private List<ArticleComment> getCommentsList(CommentsViewerProperties properties) {
		if (properties == null) {
			return null;
		}

		properties.setAddNulls(true);
		properties.setFetchFully(true);
		return getCommentsEntries(properties);
	}

	private List<ArticleComment> getCommentsEntries(CommentsViewerProperties properties) {
		List<ArticleComment> fake = new ArrayList<ArticleComment>();
		if (properties.getUri() == null) {
			if (properties.isAddNulls()) {
				return null;
			}
			return fake;
		}

		List<? extends Entry> entriesToFormat = getEntriesToFormat(properties);
		if (entriesToFormat == null) {
			if (properties.isAddNulls()) {
				return null;
			}
			return fake;
		}

		List<ArticleComment> articleComments = getFormattedEntries(entriesToFormat, properties);
		return articleComments == null ? properties.isAddNulls() ? null : fake : articleComments;
	}

	@SuppressWarnings("unchecked")
	private List<? extends Entry> getEntriesToFormat(CommentsViewerProperties properties) {
		Feed comments = getCommentsFeed(properties);
		if (comments == null) {
			return null;
		}

		if (StringUtil.isEmpty(properties.getSpringBeanIdentifier())) {
			return comments.getEntries();
		} else {
			CommentsPersistenceManager manager = getCommentsManager(properties.getSpringBeanIdentifier());
			if (manager != null) {
				return manager.getEntriesToFormat(comments, properties);
			}
		}

		return null;
	}

	private List<ArticleComment> getFormattedEntries(List<? extends Entry> entriesToFormat, CommentsViewerProperties properties) {
		if (ListUtil.isEmpty(entriesToFormat)) {
			return null;
		}

		Locale locale = null;
		IWContext iwc = CoreUtil.getIWContext();
		if (iwc != null) {
			locale = iwc.getCurrentLocale();
		}
		if (locale == null) {
			locale = Locale.ENGLISH;
		}

		List<ArticleComment> items = new ArrayList<ArticleComment>();
		ArticleComment comment = null;
		Content content = null;
		Person author = null;
		IWTimestamp posted = null;
		int number = 0;
		for (Entry entry: entriesToFormat) {
			comment = new ArticleComment();

			//	Number
			comment.setListNumber(number + 1);

			// ID
			comment.setId(entry.getId());

			// Author
			try {
				if (entry.getAuthors() != null) {
					author = (Person) entry.getAuthors().get(0);
					comment.setUser(author.getName());
				}
				else {
					comment.setUser(ContentConstants.EMPTY);
				}
			} catch(ClassCastException e) {
				comment.setUser(ContentConstants.EMPTY);
			} catch (IndexOutOfBoundsException e) {
				comment.setUser(ContentConstants.EMPTY);
			}

			// Subject
			comment.setSubject(StringUtil.isEmpty(entry.getTitle()) ? CoreConstants.EMPTY : entry.getTitle());

			// Content
			try {
				if (entry.getContents() != null) {
					content = (Content) entry.getContents().get(0);
					comment.setComment(content.getValue());
				}
				else {
					comment.setComment(ContentConstants.EMPTY);
				}
			} catch (ClassCastException e) {
				comment.setComment(ContentConstants.EMPTY);
			} catch (IndexOutOfBoundsException e) {
				comment.setComment(ContentConstants.EMPTY);
			}

			// Date of creation
			posted = new IWTimestamp(entry.getPublished());
			comment.setPosted(posted.getLocaleDateAndTime(locale, DateFormat.FULL, DateFormat.MEDIUM));

			if (entry instanceof CommentEntry) {
				CommentEntry commentEntry = (CommentEntry) entry;

				comment.setPublished(commentEntry.isPublished());
				comment.setCanBePublished(commentEntry.isPublishable());
				comment.setCanBeRead(commentEntry.isReadable());
				comment.setCanBeReplied(commentEntry.isReplyable());
				comment.setReaders(commentEntry.getReaders());
				comment.setAttachments(commentEntry.getAttachments());

				comment.setPrimaryKey(commentEntry.getPrimaryKey());
			}

			items.add(comment);
			number++;
		}

		Collections.sort(items, new ArticleCommentComparator(locale, DateFormat.FULL, DateFormat.MEDIUM, properties.isNewestEntriesOnTop()));

		return items;
	}

	@Override
	public CommentsPersistenceManager getCommentsManager(String springBeanIdentifier) {
		if (StringUtil.isEmpty(springBeanIdentifier)) {
			return null;
		}

		try {
			return ELUtil.getInstance().getBean(springBeanIdentifier);
		} catch(Exception e) {
			LOGGER.log(Level.SEVERE, "Error getting specified Spring bean: " + springBeanIdentifier, e);
		}

		return null;
	}

	private CommentsPersistenceManager getDefaultCommentsManager() {
		return getCommentsManager(DefaultCommentsPersistenceManager.BEAN_IDENTIFIER);
	}

	@Override
	public int getCommentsCount(String uri, String springBeanIdentifier, String identifier, IWContext iwc, boolean addLoginbyUUIDOnRSSFeedLink) {
		CommentsViewerProperties properties = new CommentsViewerProperties();
		properties.setUri(uri);
		properties.setSpringBeanIdentifier(springBeanIdentifier);
		properties.setIdentifier(identifier);
		properties.setAddLoginbyUUIDOnRSSFeedLink(addLoginbyUUIDOnRSSFeedLink);
		properties.setFetchFully(false);

		List<? extends Entry> entries = getEntriesToFormat(properties);
		return entries == null ? 0 : entries.size();
	}

	private synchronized Feed getCommentsFeed(CommentsViewerProperties properties) {
		CommentsPersistenceManager commentsManager = getCommentsManager(properties.getSpringBeanIdentifier());
		if (commentsManager != null) {
			return commentsManager.getCommentsFeed(properties.getIdentifier());
		}

		if (StringUtil.isEmpty(properties.getUri())) {
			return null;
		}

		Feed cachedFeed = getFeedFromCache(properties.getUri());
		if (cachedFeed != null) {
			return cachedFeed;
		}

		try {
			if (!getRepositoryService().getExistence(properties.getUri())) {
				return null;
			}
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, "Error while checking if file exists: " + properties.getUri(), e);
			return null;
		}

		RSSBusiness rss = getRSSBusiness();
		if (rss == null) {
			return null;
		}
		User currentUser = null;
		try {
			currentUser = getOldUser(getCurrentUser());
		} catch (RemoteException e) {
			LOGGER.log(Level.WARNING, "Error getting current user", e);
		}
		if (properties.isAddLoginbyUUIDOnRSSFeedLink() && currentUser == null) {
			LOGGER.log(Level.WARNING, "User must be looged to get comments feed!");
			return null;
		}

		String pathToComments = new StringBuilder(getThemesHelper().getFullWebRoot()).append(properties.getUri()).toString();
		SyndFeed comments = properties.isAddLoginbyUUIDOnRSSFeedLink() ?
				rss.getFeedAuthenticatedByUser(pathToComments, currentUser) :
				rss.getFeed(pathToComments);
		if (comments == null) {
			return null;
		}
		Object abstractFeed = comments.createWireFeed();
		if (abstractFeed instanceof Feed) {
			Feed realFeed = (Feed) abstractFeed;
			putFeedToCache(realFeed, properties.getUri());
			return realFeed;
		}
		return null;
	}

	private void putFeedToCache(Feed comments, String uri) {
		if (comments == null || uri == null) {
			return;
		}
		IWCacheManager2 cache = IWCacheManager2.getInstance(getIWMainApplication());
		if (cache == null) {
			return;
		}
		Map<String, Feed> commentsMap = cache.getCache(COMMENTS_CACHE_NAME);
		if (commentsMap == null) {
			commentsMap = new HashMap<String, Feed>();
		}
		commentsMap.put(uri, comments);

		return;
	}

	@Override
	protected IWMainApplication getIWMainApplication() {
		IWMainApplication iwma = null;
		try {
			iwma = super.getIWMainApplication();
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, "Error getting IWMainApplication from IBOSessionBean, will use default application", e);
		}

		return iwma == null ? IWMainApplication.getDefaultIWMainApplication() : iwma;
	}

	private Feed getFeedFromCache(String uri) {
		if (uri == null) {
			return null;
		}

		IWCacheManager2 cache = IWCacheManager2.getInstance(getIWMainApplication());
		if (cache == null) {
			return null;
		}
		Map<String, Feed> comments = null;
		try {
			comments = cache.getCache(COMMENTS_CACHE_NAME);
		} catch(Exception e) {
			e.printStackTrace();
		}
		if (comments == null) {
			return null;
		}
		return comments.get(uri);
	}

	private String encodeMail(String email) {
		return CoreUtil.getEncodedValue(email);
	}

	@Override
	public boolean setModuleProperty(String pageKey, String moduleId, String propName, String propValue, String cacheKey) {
		BuilderService builder = getBuilderService();
		if (builder == null) {
			closeLoadingMessage();
			return false;
		}
		IWContext iwc = CoreUtil.getIWContext();
		if (iwc == null) {
			closeLoadingMessage();
			return false;
		}
		String[] property = new String[1];
		property[0] = propValue;
		builder.setProperty(pageKey, moduleId, propName, property, iwc.getIWMainApplication());

		decacheComponent(cacheKey);

		ScriptBuffer script = new ScriptBuffer("hideOrShowComments('").appendData(moduleId).appendScript("');");
		return executeScriptForAllPages(script);
	}

	private Map<String, String> getArticlesCache() {
		ArticleCacher cache = ArticleCacher.getInstance(getIWMainApplication());
		if (cache == null) {
			return null;
		}

		try {
			return cache.getCacheMap();
		} catch(Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private void decacheComponent(String cacheKey) {
		if (cacheKey == null) {
			return;
		}

		Map<String, String> articles = getArticlesCache();
		if (articles == null) {
			return;
		}

		try {
			articles.clear();
		} catch (UnsupportedOperationException e) {
			e.printStackTrace();
			clearAllCaches();
		}
	}

	private void clearAllCaches() {
		IWCacheManager2 cacheManager = null;
		try {
			cacheManager = IWCacheManager2.getInstance(getIWMainApplication());
		} catch(Exception e) {
			e.printStackTrace();
		}
		if (cacheManager == null) {
			return;
		}

		cacheManager.reset();
	}

	@Override
	public boolean hideOrShowComments() {
		IWContext iwc = CoreUtil.getIWContext();
		if (ContentUtil.hasContentEditorRoles(iwc)) {
			return false; // Do not need reload page
		}
		return true; // Need to reload page (disable component)
	}

	private boolean executeScriptForAllPages(ScriptBuffer script) {
		return executeScriptForAllPages(script, Boolean.FALSE);
	}

	private boolean executeScriptForAllPages(ScriptBuffer script, boolean useThreading) {
		return executeScriptForAllPages(new ScriptCaller(WebContextFactory.get(), script), useThreading);
	}

	private boolean executeScriptForAllPages(ScriptCaller scriptCaller, boolean useThreading) {
		if (useThreading) {
			Thread thread = new Thread(scriptCaller);
			thread.start();
		} else {
			scriptCaller.run();
		}

		return true;
	}

	/**
	 * Closes loading layer in client's browser
	 */
	private void closeLoadingMessage() {
		ScriptBuffer script = new ScriptBuffer("closeAllLoadingMessages();");
		executeScriptForAllPages(script, true);
	}

	@Override
	public CommentsViewerProperties deleteComments(CommentsViewerProperties properties) {
		if (properties == null) {
			return null;
		}
		IWContext iwc = CoreUtil.getIWContext();
		if (iwc == null) {
			return null;
		}

		CommentsPersistenceManager commentsManager = getCommentsManager(properties.getSpringBeanIdentifier());
		if (commentsManager == null) {
			if (!ContentUtil.hasContentEditorRoles(iwc)) {
				LOGGER.log(Level.WARNING, "Current user doesn't have enough rights to delete comment(s)!");
				return null;
			}
		} else if (!commentsManager.hasFullRightsForComments(properties.getIdentifier())) {
			LOGGER.log(Level.WARNING, "Current user doesn't have enough rights to delete comment(s)!");
			return null;
		}

		String linkToComments = properties.getUri();
		String commentId = properties.getId();

		Feed comments = getCommentsFeed(properties);
		if (comments == null) {
			return null;
		}
		if (commentId == null) {
			//	Delete all comments
			comments.setEntries(new ArrayList<Entry>());
		}
		else {
			// Delete one comment
			comments.setEntries(getUpdatedEntries(initEntries(comments.getEntries()), commentId));
		}

		if (commentsManager == null) {
			clearArticleCache();
			putFeedToCache(comments, linkToComments);
		}

		String identifier = properties.getIdentifier();
		boolean deleted = commentsManager == null ? uploadFeed(linkToComments, comments, iwc, true) : commentsManager.storeFeed(identifier, comments);
		properties.setActionSuccess(deleted);

		if (deleted) {
			invokeToReceiveComments();
		}

		return properties;
	}

	private List<Entry> getUpdatedEntries(List<Entry> entries, String commentId) {
		if (entries == null) {
			return null;
		}
		if (commentId == null) {
			return entries;
		}
		Entry e = null;
		boolean found = false;
		for (int i = 0; (i < entries.size() && !found); i++) {
			e = entries.get(i);
			if (commentId.equals(e.getId())) {
				entries.remove(e);
				found = true;
			}
		}
		return entries;
	}

	@Override
	public boolean initCommentsFeed(IWContext iwc, String uri, String user, Timestamp date, String language, String feedTitle, String feedSubtitle,
			CommentsPersistenceManager commentsManager) {
		Feed initialFeed = createFeed(uri, user, date, language, iwc, feedTitle, feedSubtitle, commentsManager);
		if (initialFeed == null) {
			return false;
		}

		return uploadFeed(uri, initialFeed, iwc, false);
	}

	private boolean uploadFeed(String uri, Feed comments, IWContext iwc, boolean useThread) {
		if (uri == null || comments == null || iwc == null) {
			return false;
		}

		String commentsContent = getDefaultCommentsManager().getFeedContent(comments);
		if (commentsContent == null) {
			return false;
		}

		String fileBase = uri;
		String fileName = uri;
		int index = uri.lastIndexOf(ContentConstants.SLASH);
		if (index != -1) {
			fileBase = uri.substring(0, index + 1);
			fileName = uri.substring(index + 1);
		}
		Thread uploader = new Thread(new CommentsFeedUploader(fileBase, fileName, commentsContent));
		if (useThread) {
			uploader.start();
		}
		else {
			uploader.run();
		}

		return true;
	}

	private String getLocalizedString(IWContext iwc, String key, String defaultValue) {
		if (iwc == null) {
			return defaultValue;
		}
		try {
			IWResourceBundle iwrb = ArticleUtil.getBundle().getResourceBundle(iwc);
			return iwrb.getLocalizedString(key, defaultValue);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return defaultValue;
	}

	@Override
	public String getFixedCommentsUri(String uri, String instanceId, String currentPageUri) {
		if (uri == null) {
			uri = String.valueOf(new Date().getTime());
		}

		BuilderService service = getBuilderService();

		//	Checking uri start: must be /files/...
		if (!uri.startsWith(new StringBuilder(CoreConstants.PATH_FILES_ROOT).append(CoreConstants.SLASH).toString())) {
			if (!uri.startsWith(CoreConstants.SLASH)) {
				uri = new StringBuffer(CoreConstants.SLASH).append(uri).toString();
			}
			uri = new StringBuffer(CoreConstants.CONTENT_PATH).append(CoreConstants.SLASH).append(ContentConstants.COMMENT_SCOPE).append(uri).toString();
		}

		//	Checking end
		if (!uri.endsWith(".xml")) {
			String fileName = new StringBuffer(instanceId).append(CoreConstants.DOT).append("xml").toString();
			if (!uri.endsWith(fileName)) {
				if (!uri.endsWith(CoreConstants.SLASH)) {
					uri = new StringBuffer(uri).append(CoreConstants.SLASH).toString();
				}

				uri = new StringBuffer(uri).append(fileName).toString();
			}
		}

		char[] leaveAsIs = {'-', '_', '/', '.', '0','1','2','3','4','5','6','7','8','9'};
		uri = StringHandler.stripNonRomanCharacters(uri, leaveAsIs);

		if (service != null && !StringUtil.isEmpty(currentPageUri)) {
			try {
				String pageKey = service.getPageKeyByURI(currentPageUri);

				String method = ":method:1:implied:void:setLinkToComments:java.lang.String:";
				if (!StringUtil.isEmpty(pageKey) && !service.isPropertySet(pageKey, instanceId, method, getIWMainApplication())) {
					service.setModuleProperty(pageKey, instanceId, method, new String[] {uri});
				}
			} catch(Exception e) {
				LOGGER.log(Level.INFO, "Unable to set URI property for CommentsViewer: " + uri, e);
			}
		}

		return uri;
	}

	public BuilderLogicWrapper getBuilderLogicWrapper() {
		if (builderLogicWrapper == null) {
			ELUtil.getInstance().autowire(this);
		}
		return builderLogicWrapper;
	}

	private BuilderService getBuilderService() {
		return getBuilderLogicWrapper().getBuilderService(IWMainApplication.getDefaultIWApplicationContext());
	}

	public void setBuilderLogicWrapper(BuilderLogicWrapper builderLogicWrapper) {
		this.builderLogicWrapper = builderLogicWrapper;
	}

	public boolean markAsRead(Object primaryKey, String beanIdentifier) {
		if (StringUtil.isEmpty(beanIdentifier)) {
			return false;
		}

		CommentsPersistenceManager manager = getCommentsManager(beanIdentifier);
		if (manager == null) {
			return false;
		}

		return manager.markCommentAsRead(primaryKey);
	}

	@Override
	public boolean setCommentPublished(CommentsViewerProperties properties) {
		if (properties == null) {
			return false;
		}

		CommentsPersistenceManager manager = getCommentsManager(properties.getSpringBeanIdentifier());
		if (manager == null) {
			return false;
		}

		if (manager.hasFullRightsForComments(properties.getIdentifier())) {
			if (manager.setCommentPublished(properties.getPrimaryKey(), properties.isMakePublic())) {
				if (properties.isMakePublic()) {
					IWContext iwc = CoreUtil.getIWContext();

					if (iwc == null) {
						LOGGER.warning("Unable to send notification about published comment: " + properties.getPrimaryKey());
					} else {
						String handlerEmail = getCurrentUserEmailAddress(iwc);
						sendNotification(manager.getPersonsToNotifyAboutComment(properties, properties.getPrimaryKey(), true), handlerEmail, iwc, properties,
								true);
					}
				}
				invokeToReceiveComments();
				return true;
			}
		}

		return false;
	}

	private String getCurrentUserEmailAddress(IWContext iwc) {
		User currentUser = iwc.isLoggedOn() ? iwc.getCurrentUser() : null;
		if (currentUser == null) {
			return null;
		}

		try {
			UserBusiness userBusiness = getServiceInstance(UserBusiness.class);
			Email email = userBusiness.getUsersMainEmail(currentUser);
			return email == null ? null : email.getEmailAddress();
		} catch(NoEmailFoundException e) {
			LOGGER.warning(currentUser + " doesn't have email!");
		} catch(Exception e) {
			LOGGER.log(Level.WARNING, "Error getting user's main email", e);
		}
		return null;
	}

	@Override
	public boolean setReadComment(CommentsViewerProperties properties) {
		if (properties == null) {
			return false;
		}

		CommentsPersistenceManager manager = getCommentsManager(properties.getSpringBeanIdentifier());
		if (manager == null) {
			return false;
		}

		if (manager.setCommentRead(properties.getPrimaryKey())) {
			invokeToReceiveComments();
			return true;
		}

		return false;
	}

	@Override
	public RenderedComponent getCommentCreator(CommentsViewerProperties properties) {
		if (properties == null) {
			return null;
		}
		IWContext iwc = CoreUtil.getIWContext();
		if (iwc == null) {
			return null;
		}

		CommentCreator commentCreator = new CommentCreator();
		commentCreator.setProperties(properties);
		CommentsPersistenceManager manager = getCommentsManager(properties.getSpringBeanIdentifier());
		if (manager != null && manager.canWriteComments(properties)) {
			commentCreator.setAutoEnableNotifications(manager.isNotificationsAutoEnabled(properties));
			if (manager.isCommentsCreationEnabled(properties)) {
				commentCreator.setAddUploader(true);
				commentCreator.setUploadPath(manager.getCommentFilesPath(properties));
			}
		}

		return getBuilderService().getRenderedComponent(commentCreator, null);
	}

	private ThemesHelper getThemesHelper() {
		if (themesHelper == null) {
			ELUtil.getInstance().autowire(this);
		}
		return themesHelper;
	}

	private FileDownloadNotifier getFileDownloadNotifier() {
		if (downloadNotifier == null) {
			downloadNotifier = ELUtil.getInstance().getBean(CommentAttachmentDownloadNotifier.BEAN_IDENTIFIER);
		}
		return downloadNotifier;
	}

	@Override
	public AdvancedProperty sendNotificationsToDownloadDocument(CommentAttachmentNotifyBean properties) {
		FileDownloadNotifier downloadNotifier = getFileDownloadNotifier();
		if (downloadNotifier == null) {
			return null;
		}

		return downloadNotifier.sendNotifications(properties);
	}

	private void invokeToReceiveComments() {
		executeScriptForAllPages(new ScriptBuffer("getAllComments();"), true);
	}
}