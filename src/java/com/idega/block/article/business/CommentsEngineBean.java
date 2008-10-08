package com.idega.block.article.business;

import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.httpclient.HttpException;
import org.apache.webdav.lib.WebdavResource;
import org.directwebremoting.ScriptBuffer;
import org.directwebremoting.WebContext;
import org.directwebremoting.WebContextFactory;
import org.directwebremoting.impl.DefaultScriptSession;

import com.idega.block.article.ArticleCacher;
import com.idega.block.article.bean.ArticleComment;
import com.idega.block.article.bean.CommentsViewerProperties;
import com.idega.block.article.component.CommentsViewer;
import com.idega.block.rss.business.RSSBusiness;
import com.idega.builder.bean.AdvancedProperty;
import com.idega.builder.business.BuilderLogicWrapper;
import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.business.IBOSessionBean;
import com.idega.content.bean.ContentItemFeedBean;
import com.idega.content.business.ContentConstants;
import com.idega.content.business.ContentUtil;
import com.idega.content.themes.helpers.business.ThemesHelper;
import com.idega.core.accesscontrol.business.NotLoggedOnException;
import com.idega.core.builder.business.BuilderService;
import com.idega.core.builder.business.BuilderServiceFactory;
import com.idega.core.cache.IWCacheManager2;
import com.idega.dwr.business.ScriptCaller;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWMainApplication;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.IWContext;
import com.idega.slide.business.IWSlideService;
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
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.WireFeedOutput;

public class CommentsEngineBean extends IBOSessionBean implements CommentsEngine {

	private static final long serialVersionUID = 7299800648381936213L;
	private static final Logger logger = Logger.getLogger(CommentsEngineBean.class.getName());
	
	private static final String COMMENTS_CACHE_NAME = "article_comments_feeds_cache";
	
	private RSSBusiness rss = null;
	private WireFeedOutput wfo = new WireFeedOutput();
	
	private String newComment = "New comment";
	private String newCommentMessage = "New comment was entered. You can read all comments at";
	private String articeComments = "Comments of Article";
	private String allArticleComments = "All comments";
	
	private List<String> parsedEmails = new ArrayList<String>();
	
	private volatile BuilderService builder = null;

	public boolean addComment(CommentsViewerProperties properties) {
		if (properties == null) {
			logger.log(Level.INFO, "Comment properties are undefined!");
			return false;
		}
		
		String uri = properties.getUri();
		String subject = properties.getSubject();
		String user = properties.getUser();
		String instanceId = properties.getInstanceId();
		String body = properties.getBody();
		String email = properties.getEmail();
		String errorMessage = "Unable to add comment: '" + subject + "' by: " + user;
		boolean notify = properties.isNotify();
		
		if (uri == null) {
			closeLoadingMessage();
			logger.log(Level.SEVERE, errorMessage);
			return false;
		}
		if (ContentConstants.EMPTY.equals(uri)) {
			closeLoadingMessage();
			logger.log(Level.SEVERE, errorMessage);
			return false;
		}
		
		IWContext iwc = CoreUtil.getIWContext();
		if (iwc == null) {
			logger.log(Level.SEVERE, errorMessage);
			return false;
		}
		
		CommentsPersistenceManager commentsManager = getCommentsManager(properties.getSpringBeanIdentifier());
		
		if (commentsManager == null) {
			uri = getFixedCommentsUri(iwc, uri, instanceId);
		}
		
		String language = ThemesHelper.getInstance().getCurrentLanguage(iwc);
		Timestamp date = IWTimestamp.getTimestampRightNow();
		Feed comments = getCommentsFeed(uri, properties.getSpringBeanIdentifier(), properties.getIdentifier(), iwc);
		if (comments == null) {
			String feedTitle = commentsManager == null ? properties.getTitle() : commentsManager.getFeedTitle(iwc, properties.getIdentifier());
			String feedSubtitle = commentsManager == null ? properties.getSubtitle() : commentsManager.getFeedSubtitle(iwc, properties.getIdentifier());
			comments = createFeed(uri, user, date, language, iwc, feedTitle, feedSubtitle, commentsManager);
		}
		if (comments == null) {
			logger.log(Level.SEVERE, errorMessage);
			return false;
		}
		
		if (!addNewEntry(comments, subject, uri, date, body, user, language, email, notify)) {
			logger.log(Level.SEVERE, errorMessage);
			return false;
		}
		
		//	Caching XML
		if (commentsManager == null) {
			putFeedToCache(comments, uri, iwc);
		}
		
		//	Clearing cache for articles (ALWAYS)
		if (commentsManager == null) {
			clearArticleCache(iwc);
		}
		
		//	Uploading changed XML for comments
		boolean finisheSuccessfully = commentsManager == null ? uploadFeed(uri, comments, iwc, true) : commentsManager.storeFeed(properties.getIdentifier(),
																																comments);
		if (!finisheSuccessfully) {
			logger.log(Level.SEVERE, errorMessage);
			return false;
		}
		
		//	Updating clients with the newest comments
		ScriptCaller scriptCaller = new ScriptCaller(WebContextFactory.get(), new ScriptBuffer("getUpdatedCommentsFromServer();"), true);
		scriptCaller.run();	//	Not thread!
		
		//	Sending notifications (if needed) about new comment
		sendNotification(comments, email, iwc);
		
		return finisheSuccessfully;
	}
	
	@SuppressWarnings("unchecked")
	private void clearArticleCache(IWContext iwc) {
		BuilderLogicWrapper builder = ELUtil.getInstance().getBean(CoreConstants.SPRING_BEAN_NAME_BUILDER_LOGIC_WRAPPER);
		Map articlesCache = null;
		if (builder != null) {
			articlesCache = getArticlesCache(iwc);
		}
		if (articlesCache == null) {
			Logger.getLogger(CommentsEngineBean.class.getName()).log(Level.WARNING, "Aticle cache is null, can not clear it!");
		}
		else {
			articlesCache.clear();
		}
	}
	
	private boolean sendNotification(Feed comments, String email, IWContext iwc) {
		List<String> emails = getEmails(comments, email);
		newCommentMessage = getLocalizedString(iwc, "comments_viewer.new_comment_message", "New comment was entered. You can read all comments at");
		newComment = getLocalizedString(iwc, "comments_viewer.new_comment", "New comment");
		
		StringBuffer body = new StringBuffer(newCommentMessage);
		WebContext wctx = WebContextFactory.get();
		body.append(ThemesHelper.getInstance().getFullServerName(iwc)).append(wctx.getCurrentPage());
		String host = iwc.getApplicationSettings().getProperty(CoreConstants.PROP_SYSTEM_SMTP_MAILSERVER);
//		if (host == null) {
//			host = "smtp.emailsrvr.com";
//		}
		String from = iwc.getApplicationSettings().getProperty(CoreConstants.PROP_SYSTEM_MAIL_FROM_ADDRESS);
//		if (from == null) {
//			from = "comments@idega.com";
//		}
		Thread sender = new Thread(new CommentsNotificationSender(emails, from, newComment,	body.toString(), host));
		sender.start();
		return true;
	}
	
	@SuppressWarnings("unchecked")
	private List<String> getEmails(Feed comments, String email) {
		List<String> emails = new ArrayList<String>();
		if (comments == null) {
			return emails;
		}
		List<Entry> entries = comments.getEntries();
		if (entries == null) {
			return emails;
		}
		Entry entry = null;
		Object o = null;
		Object oo = null;
		List<Person> authors = null;
		Person author = null;
		String mail = null;
		parsedEmails = new ArrayList<String>();
		for (int i = 0; i < entries.size(); i++) {
			o = entries.get(i);
			if (o instanceof Entry) {
				entry = (Entry) o;
				authors = entry.getAuthors();
				if (authors != null) {
					for (int j = 0; j < authors.size(); j++) {
						oo = authors.get(j);
						if (oo instanceof Person) {
							author = (Person) oo;
							mail = author.getEmail();
							if (mail != null) {
								mail = decodeMail(mail);
								if (!mail.equals(email)) {
									if (!parsedEmails.contains(mail)) {
										parsedEmails.add(mail);
										emails.add(mail);
									}
								}
							}
						}
					}
				}
			}
		}
		return emails;
	}
	
	private boolean addNewEntry(Feed feed, String subject, String uri, Timestamp date, String body, String user, String language,
			String email, boolean notify) {
		if (feed == null) {
			return false;
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
		entry.setId(ThemesHelper.getInstance().getUniqueIdByNumberAndDate(ContentConstants.COMMENT_SCOPE));
		
		// URI
		Link link = new Link();
		link.setHref(uri);
		List<Link> links = new ArrayList<Link>();
		links.add(link);
		entry.setAlternateLinks(links);
		
		entries.add(entry);
		feed.setEntries(entries);
		return true;
	}
	
	@SuppressWarnings("unchecked")
	private List<Entry> initEntries(List oldEntries) {
		if (oldEntries == null) {
			return new ArrayList<Entry>();
		}
		if (oldEntries.size() == 0) {
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
		String serverName = ThemesHelper.getInstance().getFullServerName(iwc);
		
		articeComments = StringUtil.isEmpty(feedTitle) ? getLocalizedString(iwc, "comments_viewer.article_comments", "Comments of Article") : feedTitle;
		allArticleComments = StringUtil.isEmpty(feedSubtitle) ? getLocalizedString(iwc, "comments_viewer.all_article_comments", "All comments"): feedSubtitle;
		
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
			putFeedToCache(comments, uri, iwc);
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
		if (rss == null) {
			try {
				rss = (RSSBusiness) IBOLookup.getServiceInstance(IWMainApplication.getDefaultIWApplicationContext(), RSSBusiness.class);
			} catch (IBOLookupException e) {
				e.printStackTrace();
				return null;
			}
		}
		return rss;
	}
	
	/**
	 * Updates particular comments block in client's browser(s)
	 * @param uri - link to comments
	 * @param id - comments group id
	 * @return
	 */
	public boolean getCommentsForAllPages(CommentsViewerProperties properties) {
		return executeScriptForAllPages(getScriptForCommentsList(properties));
	}
	
	private ScriptBuffer getScriptForCommentsList(CommentsViewerProperties properties) {
		if (properties == null) {
			logger.log(Level.WARNING, "Can not create ScriptBuffer: properties unknown");
			return null;
		}
		
		String id = properties.getInstanceId();
		String uri = properties.getUri();
		String identifier = properties.getIdentifier();
		boolean newestEntriesOnTop = properties.isNewestEntriesOnTop();
		String springBeanIdentifier = properties.getSpringBeanIdentifier();
		
		ScriptBuffer script = new ScriptBuffer();
		script = new ScriptBuffer("getCommentsCallback(").appendData(getCommentsList(uri, springBeanIdentifier, identifier, true, newestEntriesOnTop))
					.appendScript(", ").appendData(id).appendScript(", ").appendData(uri).appendScript(");");
		return script;
	}
	
	@SuppressWarnings("unchecked")
	private Collection getAllCurrentPageSessions() {
		WebContext wctx = WebContextFactory.get();
		if (wctx == null) {
			return null;
		}
		Collection pages = wctx.getScriptSessionsByPage(wctx.getCurrentPage());
		if (pages == null) {
			return null;
		}

		return pages;
	}
	
	public List<List<ArticleComment>> getCommentsFromUris(List<AdvancedProperty> commentsProperties) {
		if (ListUtil.isEmpty(commentsProperties)) {
			return null;
		}
		
		List<List<ArticleComment>> allComments = new ArrayList<List<ArticleComment>>();
		for (AdvancedProperty commentProperty: commentsProperties) {
			allComments.add(getCommentsList(commentProperty.getId(), null, null, false, Boolean.valueOf(commentProperty.getValue())));
		}
		return allComments;
	}
	
	public List<ArticleComment> getComments(CommentsViewerProperties properties) {
		return getCommentsList(properties);
	}
	
	private List<ArticleComment> getCommentsList(CommentsViewerProperties properties) {
		if (properties == null) {
			return null;
		}
		
		return getCommentsList(properties.getUri(), properties.getSpringBeanIdentifier(), properties.getIdentifier(), true, properties.isNewestEntriesOnTop());
	}
	
	@SuppressWarnings("unchecked")
	private List<ArticleComment> getCommentsList(String uri, String springBeanIdentifier, String identifier, boolean addNulls, boolean newestEntriesOnTop) {
		List<ArticleComment> fake = new ArrayList<ArticleComment>();
		if (uri == null) {
			if (addNulls) {
				return null;
			}
			return fake;
		}
		
		Feed comments = getCommentsFeed(uri, springBeanIdentifier, identifier, CoreUtil.getIWContext());
		if (comments == null) {
			if (addNulls) {
				return null;
			}
			return fake;
		}
		
		List entries = comments.getEntries();
		if (ListUtil.isEmpty(entries)) {
			if (addNulls) {
				return null;
			}
			return fake;
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
		Object o = null;
		Entry entry = null;
		Content content = null;
		Person author = null;
		IWTimestamp posted = null;
		for (int i = 0; i < entries.size(); i++) {
			o = entries.get(i);
			if (o instanceof Entry) {
				comment = new ArticleComment();
				entry = (Entry) o;
				
				//	Number
				comment.setListNumber(i + 1);
				
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
				comment.setSubject(entry.getTitle());
				
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
				items.add(comment);
			}
		}
		
		Collections.sort(items, new ArticleCommentComparator(locale, DateFormat.FULL, DateFormat.MEDIUM, newestEntriesOnTop));
		
		return items;
	}
	
	public CommentsPersistenceManager getCommentsManager(String springBeanIdentifier) {
		if (StringUtil.isEmpty(springBeanIdentifier)) {
			return null;
		}
		
		try {
			return ELUtil.getInstance().getBean(springBeanIdentifier);
		} catch(Exception e) {
			logger.log(Level.SEVERE, "Error getting specified Spring bean: " + springBeanIdentifier, e);
		}
		
		return null;
	}
	
	public int getCommentsCount(String uri, String springBeanIdentifier, String identifier, IWContext iwc) {
		Feed comments = getCommentsFeed(uri, springBeanIdentifier, identifier, iwc);
		if (comments == null) {
			return 0;
		}
		if (comments.getEntries() == null) {
			return 0;
		}
		return comments.getEntries().size();
	}
	
	private synchronized Feed getCommentsFeed(String uri, String springBeanIdentifier, String identifier, IWContext iwc) {
		CommentsPersistenceManager commentsManager = getCommentsManager(springBeanIdentifier);
		if (commentsManager != null) {
			return commentsManager.getCommentsFeed(iwc, identifier);
		}
		
		Feed cachedFeed = getFeedFromCache(uri, iwc);
		if (cachedFeed != null) {
			return cachedFeed;
		}
		
		if (StringUtil.isEmpty(uri)) {
			return null;
		}
		IWSlideService slide = getSlideService();
		if (slide == null) {
			return null;
		}
		WebdavResource commentsResource = null;
		try {
			commentsResource = slide.getWebdavResourceAuthenticatedAsRoot(uri);
		} catch (HttpException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (commentsResource == null || !commentsResource.getExistence()) {
			return null;
		}
		
		RSSBusiness rss = getRSSBusiness();
		if (rss == null) {
			return null;
		}
		User currentUser = null;
		try {
			currentUser = iwc == null ? null : iwc.getCurrentUser();
		} catch(NotLoggedOnException e) {}
		
		SyndFeed comments = rss.getFeedAuthenticatedByUser(new StringBuilder(ThemesHelper.getInstance().getFullWebRoot()).append(uri).toString(), currentUser);
		if (comments == null) {
			return null;
		}
		Object abstractFeed = comments.createWireFeed();
		if (abstractFeed instanceof Feed) {
			Feed realFeed = (Feed) abstractFeed;
			putFeedToCache(realFeed, uri, iwc);
			return realFeed;
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	private void putFeedToCache(Feed comments, String uri, IWContext iwc) {
		if (comments == null || uri == null) {
			return;
		}
		if (iwc == null) {
			iwc = CoreUtil.getIWContext();
			if (iwc == null) {
				return;
			}
		}
		IWCacheManager2 cache = IWCacheManager2.getInstance(iwc.getIWMainApplication());
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
	
	@SuppressWarnings("unchecked")
	private Feed getFeedFromCache(String uri, IWContext iwc) {
		if (uri == null) {
			return null;
		}
		if (iwc == null) {
			iwc = CoreUtil.getIWContext();
			if (iwc == null) {
				return null;
			}
		}
		IWCacheManager2 cache = IWCacheManager2.getInstance(iwc.getIWMainApplication());
		if (cache == null) {
			return null;
		}
		Map comments = null;
		try {
			comments = cache.getCache(COMMENTS_CACHE_NAME);
		} catch(Exception e) {
			e.printStackTrace();
		}
		if (comments == null) {
			return null;
		}
		Object o = comments.get(uri);
		if (o == null) {
			return null;
		}
		try {
			return (Feed) o;
		} catch (ClassCastException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private String encodeMail(String email) {
		return CoreUtil.getEncodedValue(email);
	}
	
	private String decodeMail(String email) {
		return CoreUtil.getDecodedValue(email);
	}
	
	private BuilderService getBuilderService() {
		if (builder == null) {
			try {
				builder = BuilderServiceFactory.getBuilderService(IWMainApplication.getDefaultIWApplicationContext());
			} catch (RemoteException e) {
				e.printStackTrace();
				return null;
			}
		}
		return builder;
	}
	
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
		
		decacheComponent(cacheKey, iwc);
		
		ScriptBuffer script = new ScriptBuffer("hideOrShowComments('").appendData(moduleId).appendScript("');");
		return executeScriptForAllPages(script);
	}
	
	@SuppressWarnings("unchecked")
	private Map getArticlesCache(IWContext iwc) {
		if (iwc == null) {
			return null;
		}
		ArticleCacher cache = ArticleCacher.getInstance(iwc.getIWMainApplication());
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
	
	@SuppressWarnings("unchecked")
	private void decacheComponent(String cacheKey, IWContext iwc) {
		if (cacheKey == null || iwc == null) {
			return;
		}
		
		Map articles = getArticlesCache(iwc);
		if (articles == null) {
			return;
		}
		
		try {
			articles.clear();
		} catch (UnsupportedOperationException e) {
			e.printStackTrace();
			clearAllCaches(iwc);
		}
	}
	
	private void clearAllCaches(IWContext iwc) {
		IWCacheManager2 cacheManager = null;
		try {
			cacheManager = IWCacheManager2.getInstance(iwc.getIWMainApplication());
		} catch(Exception e) {
			e.printStackTrace();
		}
		if (cacheManager == null) {
			return;
		}
		
		cacheManager.reset();
	}
	
	public boolean hideOrShowComments() {
		IWContext iwc = CoreUtil.getIWContext();
		if (ContentUtil.hasContentEditorRoles(iwc)) {
			return false; // Do not need reload page
		}
		return true; // Need to reload page (disable component)
	}
	
	@SuppressWarnings("unchecked")
	private boolean executeScriptForAllPages(ScriptBuffer script) {
		Collection allPages = getAllCurrentPageSessions();
		if (allPages == null) {
			return false;
		}
		Object o = null;
		DefaultScriptSession session = null;
		for (Iterator it = allPages.iterator(); it.hasNext(); ) {
			o = it.next();
			if (o instanceof DefaultScriptSession) {
	            session = (DefaultScriptSession) o;
	            session.addScript(script);
	        }
		}
		return true;
	}
	
	/**
	 * Closes loading layer in client's browser
	 */
	private void closeLoadingMessage() {
		ScriptBuffer script = new ScriptBuffer("closeAllLoadingMessages();");
		executeScriptForAllPages(script);
	}
	
	public List<String> getInitInfoForComments() {
		List<String> info = new ArrayList<String>();
		IWContext iwc = CoreUtil.getIWContext();
		if (iwc == null) {
			return info;
		}
		IWResourceBundle iwrb = null;
		try {
			iwrb = ArticleUtil.getBundle().getResourceBundle(iwc);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (iwrb == null) {
			return info;
		}
		
		try {
			IWBundle bundle = ArticleUtil.getBundle();
			info.add(iwrb.getLocalizedString("comments_viewer.posted", "Posted"));								// 0
			info.add(iwrb.getLocalizedString("comments_viewer.loading_comments", "Loading comments..."));		// 1
			info.add(iwrb.getLocalizedString("comments_viewer.atom_feed", "Atom Feed"));						// 2
			info.add(ThemesHelper.getInstance().getFullServerName(iwc) + CoreConstants.WEBDAV_SERVLET_URI);		// 3
			info.add(iwrb.getLocalizedString("comments_viewer.need_send_notification", "Do you wish to receive notifications about new comments?"));	// 4
			info.add(iwrb.getLocalizedString("yes", "Yes"));													// 5
			info.add(iwrb.getLocalizedString("no", "No"));														// 6
			info.add(iwrb.getLocalizedString("comments_viewer.enter_email_text", "Please enter your e-mail!"));	// 7
			info.add(iwrb.getLocalizedString("comments_viewer.saving", "Saving..."));							// 8
			info.add(bundle.getVirtualPathWithFileNameString(CommentsViewer.FEED_IMAGE));						// 9
			info.add(bundle.getVirtualPathWithFileNameString(CommentsViewer.DELETE_IMAGE));						// 10
			info.add(iwrb.getLocalizedString("comments_viewer.deleting", "Deleting..."));						// 11
			info.add(iwrb.getLocalizedString("are_you_sure", "Are you sure?"));									// 12
			info.add(iwrb.getLocalizedString("comments_viewer.delete_all_comments", "Delete comments"));		// 13
			info.add(iwrb.getLocalizedString("comments_viewer.delete_comment", "Delete this comment"));			// 14
			info.add(bundle.getVirtualPathWithFileNameString("images/comment_delete.png"));						// 15
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return info;
	}
	
	public boolean getUserRights() {
		IWContext iwc = CoreUtil.getIWContext();
		if (iwc == null) {
			return false;
		}
		return ContentUtil.hasContentEditorRoles(iwc);
	}
	
	public CommentsViewerProperties deleteComments(CommentsViewerProperties properties) {
		if (properties == null) {
			return null;
		}
		
		String linkToComments = properties.getUri();
		String commentId = properties.getId();
		IWContext iwc = CoreUtil.getIWContext();
		Feed comments = getCommentsFeed(linkToComments, properties.getSpringBeanIdentifier(), properties.getIdentifier(), iwc);
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
		
		CommentsPersistenceManager commentsManager = getCommentsManager(properties.getSpringBeanIdentifier());
		if (commentsManager == null) {
			clearArticleCache(iwc);
			
			putFeedToCache(comments, linkToComments, iwc);
		}
		
		String identifier = properties.getIdentifier();
		boolean deleted = commentsManager == null ? uploadFeed(linkToComments, comments, iwc, true) : commentsManager.storeFeed(identifier, comments);
		properties.setActionSuccess(deleted);
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
		
		IWSlideService service = getSlideService();
		if (service == null) {
			return false;
		}
		
		String commentsContent = null;
		try {
			commentsContent = wfo.outputString(comments);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			return false;
		} catch (FeedException e) {
			e.printStackTrace();
			return false;
		}
		
		String fileBase = uri;
		String fileName = uri;
		int index = uri.lastIndexOf(ContentConstants.SLASH);
		if (index != -1) {
			fileBase = uri.substring(0, index + 1);
			fileName = uri.substring(index + 1);
		}
		Thread uploader = new Thread(new CommentsFeedUploader(service, fileBase, fileName, commentsContent));
		if (useThread) {
			uploader.start();
		}
		else {
			uploader.run();
		}
		
		return true;
	}
	
	private IWSlideService getSlideService() {
		try {
			return (IWSlideService) IBOLookup.getServiceInstance(IWMainApplication.getDefaultIWApplicationContext(), IWSlideService.class);
		} catch (IBOLookupException e) {
			e.printStackTrace();
		}
		return null;
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
	
	public String getFixedCommentsUri(IWContext iwc, String uri, String instanceId) {
		if (uri == null) {
			uri = String.valueOf(new Date().getTime());
		}
		
		BuilderService service = getBuilderService();
		if (!uri.startsWith(CoreConstants.CONTENT_PATH)) {
			if (!uri.startsWith(CoreConstants.SLASH)) {
				uri = new StringBuffer(CoreConstants.SLASH).append(uri).toString();
			}
			
			uri = new StringBuffer(CoreConstants.CONTENT_PATH).append(CoreConstants.SLASH).append(ContentConstants.COMMENT_SCOPE).append(uri).toString();
			
			String fileName = new StringBuffer(instanceId).append(CoreConstants.DOT).append("xml").toString();
			if (!uri.endsWith(fileName)) {
				if (!uri.endsWith(CoreConstants.SLASH)) {
					uri = new StringBuffer(uri).append(CoreConstants.SLASH).toString();
				}
				
				uri = new StringBuffer(uri).append(fileName).toString();
			}
			
			char[] leaveAsIs = {'-', '_', '/', '.', '0','1','2','3','4','5','6','7','8','9'};
			uri = StringHandler.stripNonRomanCharacters(uri, leaveAsIs);
			
			if (service != null) {
				int pageId = iwc.getCurrentIBPageID();
				if (pageId > -1) {
					String pageKey = String.valueOf(pageId);
					service.setModuleProperty(pageKey, instanceId, ":method:1:implied:void:setLinkToComments:java.lang.String:", new String[] {uri});
				}
			}
		}
		
		return uri;
	}
	
}