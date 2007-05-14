package com.idega.block.article.business;

import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.directwebremoting.ScriptBuffer;
import org.directwebremoting.ScriptSession;
import org.directwebremoting.WebContext;
import org.directwebremoting.WebContextFactory;
import org.directwebremoting.impl.DefaultScriptSession;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import com.idega.block.article.ArticleCacher;
import com.idega.block.article.bean.ArticleComment;
import com.idega.block.article.component.CommentsViewer;
import com.idega.block.rss.business.RSSBusiness;
import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.business.IBOServiceBean;
import com.idega.content.bean.ContentItemFeedBean;
import com.idega.content.business.ContentConstants;
import com.idega.content.business.ContentUtil;
import com.idega.content.themes.helpers.ThemesHelper;
import com.idega.core.builder.business.BuilderService;
import com.idega.core.builder.business.BuilderServiceFactory;
import com.idega.core.cache.IWCacheManager2;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.IWContext;
import com.idega.slide.business.IWSlideService;
import com.idega.util.CoreConstants;
import com.idega.util.CoreUtil;
import com.sun.syndication.feed.atom.Content;
import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.feed.atom.Feed;
import com.sun.syndication.feed.atom.Link;
import com.sun.syndication.feed.atom.Person;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.WireFeedOutput;

public class CommentsEngineBean extends IBOServiceBean implements CommentsEngine {

	private static final long serialVersionUID = 7299800648381936213L;
	private static final Log log = LogFactory.getLog(CommentsEngineBean.class);
	
	private static final String COMMENTS_CACHE_NAME = "article_comments_feeds_cache";
	
	private RSSBusiness rss = getRSSBusiness();
	private WireFeedOutput wfo = new WireFeedOutput();
	private BASE64Encoder encoder = new BASE64Encoder();
	private BASE64Decoder decoder = new BASE64Decoder();
	
	private String newComment = "New comment";
	private String newCommentMessage = "New comment was entered. You can read all comments at";
	private String articeComments = "Comments of Article";
	private String allArticleComments = "All comments";
	
	private List<String> parsedEmails = new ArrayList<String>();
	
	private volatile BuilderService builder = null;

	public boolean addComment(String user, String subject, String email, String body, String uri, boolean notify, String id) {
		if (uri == null) {
			closeLoadingMessage();
			return false;
		}
		if (ContentConstants.EMPTY.equals(uri)) {
			closeLoadingMessage();
			return false;
		}
		
		IWContext iwc = CoreUtil.getIWContext();
		
		String language = ThemesHelper.getInstance().getCurrentLanguage(iwc);
		
		Timestamp date = new Timestamp(System.currentTimeMillis());
		
		Feed comments = null;
		synchronized (CommentsEngineBean.class) {
			comments = getCommentsFeed(uri, iwc);
			if (comments == null) {
				comments = createFeed(uri, user, subject, body, date, language, iwc);
			}
			if (comments == null) {
				closeLoadingMessage();
				return false;
			}
			
			if (!addNewEntry(comments, subject, uri, date, body, user, language, email, notify)) {
				closeLoadingMessage();
				return false;
			}
			
			putFeedToCache(comments, uri, iwc);
			
			if (iwc == null) {
				return false;
			}
			
			sendNotification(comments, email, iwc);
			if (!uploadFeed(uri, comments, iwc)) {
				closeLoadingMessage();
			}
			
			return getCommentsForAllPages(uri, id);
		}
	}
	
	private boolean sendNotification(Feed comments, String email, IWContext iwc) {
		List<String> emails = getEmails(comments, email);
		newCommentMessage = getLocalizedString(iwc, "new_comment_message", "New comment was entered. You can read all comments at");
		newComment = getLocalizedString(iwc, "new_comment", "New comment");
		
		StringBuffer body = new StringBuffer(newCommentMessage);
		WebContext wctx = WebContextFactory.get();
		body.append(ThemesHelper.getInstance().getFullServerName(iwc)).append(wctx.getCurrentPage());
		String host = iwc.getApplicationSettings().getProperty(CoreConstants.PROP_SYSTEM_SMTP_MAILSERVER);
//		if (host == null) {
//			host = "mail.simnet.is";
//		}
		String from = iwc.getApplicationSettings().getProperty(CoreConstants.PROP_SYSTEM_MAIL_FROM_ADDRESS);
//		if (from == null) {
//			from = "testing@formbuilder.idega.is";
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
	
	private Feed createFeed(String uri, String user, String subject, String body, Timestamp date, String language,
			IWContext iwc) {

		String serverName = ThemesHelper.getInstance().getFullServerName(iwc);
		
		articeComments = getLocalizedString(iwc, "article_comments", "Comments of Article");
		allArticleComments = getLocalizedString(iwc, "all_article_comments", "All comments");
		
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
		comments.setId(serverName + ContentConstants.CONTENT + uri);
		
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
		putFeedToCache(comments, uri, iwc);
		
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
			synchronized (CommentsEngineBean.class) {
				if (rss == null) {
					try {
						rss = (RSSBusiness) IBOLookup.getServiceInstance(IWContext.getInstance(), RSSBusiness.class);
					} catch (IBOLookupException e) {
						log.error(e);
					}
				}
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
	public boolean getCommentsForAllPages(String uri, String id) {
		return executeScriptForAllPages(getScriptForCommentsList(uri, id));
	}
	
	private ScriptBuffer getScriptForCommentsList(String uri, String id) {
		ScriptBuffer script = new ScriptBuffer();
		script = new ScriptBuffer("getCommentsCallback(").appendData(getCommentsList(uri)).appendScript(", ").appendData(id);
		script.appendScript(", ").appendData(uri).appendScript(");");
		return script;
	}
	
	private Collection getAllCurrentPageSessions() {
		WebContext wctx = WebContextFactory.get();
		if (wctx == null) {
			return null;
		}
		Collection pages = wctx.getScriptSessionsByPage(wctx.getCurrentPage());
		if (pages == null) {
			return null;
		}
		log.info("Found JavaScript sessions on same page ('"+wctx.getCurrentPage()+"'): " + pages.size());

		return pages;
	}
	
	public List<ArticleComment> getComments(String uri) {
		return getCommentsList(uri);
	}
	
	private List<ArticleComment> getCommentsList(String uri) {
		if (uri == null) {
			return null;
		}
		Feed comments = getCommentsFeed(uri, null);
		if (comments == null) {
			return null;
		}
		List entries = comments.getEntries();
		if (entries == null) {
			return null;
		}
		List<ArticleComment> items = new ArrayList<ArticleComment>();
		ArticleComment comment = null;
		Object o = null;
		Entry entry = null;
		Content content = null;
		Person author = null;
		for (int i = 0; i < entries.size(); i++) {
			o = entries.get(i);
			if (o instanceof Entry) {
				comment = new ArticleComment();
				entry = (Entry) o;
				
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
				comment.setPosted(entry.getPublished().toString());
				items.add(comment);
			}
		}
		return items;
	}
	
	public boolean getCommentsForCurrentPage(String uri, String id) {
		WebContext wctx = WebContextFactory.get();
		if (wctx == null) {
			return false;
		}
		ScriptSession ss = wctx.getScriptSession();
		if (ss == null) {
			return false;
		}
		ss.addScript(getScriptForCommentsList(uri, id));
		return true;
	}
	
	public int getCommentsCount(String uri) {
		Feed comments = getCommentsFeed(uri, null);
		if (comments == null) {
			return 0;
		}
		if (comments.getEntries() == null) {
			return 0;
		}
		return comments.getEntries().size();
	}
	
	private Feed getCommentsFeed(String uri, IWContext iwc) {
		Feed cachedFeed = getFeedFromCache(uri, iwc);
		if (cachedFeed != null) {
			return cachedFeed;
		}
		
		if (uri == null) {
			return null;
		}
		ThemesHelper helper = ThemesHelper.getInstance();
		if (!helper.existFileInSlide(uri)) {
			return null;
		}
		if (rss == null) {
			return null;
		}
		SyndFeed comments = rss.getFeed(helper.getFullWebRoot() + uri);
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
	}
	
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
		Map comments = cache.getCache(COMMENTS_CACHE_NAME);
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
			log.error(e);
			return null;
		}
	}
	
	private String encodeMail(String email) {
		if (email == null) {
			return email;
		}
		return encoder.encode(email.getBytes());
	}
	
	private String decodeMail(String email) {
		if (email == null) {
			return email;
		}
		try {
			return new String(decoder.decodeBuffer(email));
		} catch (IOException e) {
			log.error(e);
			return null;
		}
	}
	
	private BuilderService getBuilderService() {
		if (builder == null) {
			synchronized (CommentsEngineBean.class) {
				if (builder == null) {
					try {
						builder = BuilderServiceFactory.getBuilderService(getIWApplicationContext());
					} catch (RemoteException e) {
						log.error(e);
					}
				}
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
		
		ScriptBuffer script = new ScriptBuffer("hideOrShowComments();");
		return executeScriptForAllPages(script);
	}
	
	private void decacheComponent(String cacheKey, IWContext iwc) {
		if (cacheKey == null || iwc == null) {
			return;
		}
		ArticleCacher cache = ArticleCacher.getInstance(iwc.getIWMainApplication());
		if (cache == null) {
			return;
		}
		Map articles = cache.getCacheMap();
		if (articles == null) {
			return;
		}
		
		List<String> keys = new ArrayList<String>();
		keys.add(cacheKey);
		String[] keyParts = cacheKey.split("edit");
		if (keyParts != null) {
			if (keyParts.length == 2) {
				keys.add(new StringBuffer(keyParts[0]).append("view").append(keyParts[1]).toString());
			}
		}
		for (int i = 0; i < keys.size(); i++) {
			if (articles.get(keys.get(i)) != null) {
				articles.remove(keys.get(i));
			}
		}
	}
	
	public boolean hideOrShowComments() {
		IWContext iwc = CoreUtil.getIWContext();
		if (ContentUtil.hasContentEditorRoles(iwc)) {
			return false; // Do not need reload page
		}
		return true; // Need to reload page (disable component)
	}
	
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
		ScriptBuffer script = new ScriptBuffer("closeLoadingMessage();");
		executeScriptForAllPages(script);
	}
	
	public List<String> getInitInfoForComments() {
		List<String> info = new ArrayList<String>();
		IWContext iwc = CoreUtil.getIWContext();
		if (iwc == null) {
			return info;
		}
		IWResourceBundle resourceBundle = null;
		try {
			resourceBundle = ArticleUtil.getBundle().getResourceBundle(iwc);
		} catch (Exception e) {
			log.error(e);
		}
		if (resourceBundle == null) {
			return info;
		}
		
		try {		
			String resourcePath = ArticleUtil.getBundle().getResourcesPath();
			info.add(resourceBundle.getLocalizedString("posted", "Posted"));								// 0
			info.add(resourceBundle.getLocalizedString("loading_comments", "Loading comments..."));			// 1
			info.add(resourceBundle.getLocalizedString("atom_feed", "Atom Feed"));							// 2
			info.add(ThemesHelper.getInstance().getFullServerName(iwc) + ContentConstants.CONTENT);			// 3
			info.add(resourceBundle.getLocalizedString("need_send_notification", "Do You wish to receive notifications about new comments?"));	// 4
			info.add(resourceBundle.getLocalizedString("yes", "Yes"));										// 5
			info.add(resourceBundle.getLocalizedString("no", "No"));										// 6
			info.add(resourceBundle.getLocalizedString("enter_email_text", "Please enter Your e-mail!"));	// 7
			info.add(resourceBundle.getLocalizedString("saving", "Saving..."));								// 8
			info.add(new StringBuffer(resourcePath).append(CommentsViewer.FEED_IMAGE).toString());			// 9
			info.add(new StringBuffer(resourcePath).append(CommentsViewer.DELETE_IMAGE).toString());		// 10
			info.add(resourceBundle.getLocalizedString("deleting", "Deleting..."));							// 11
			info.add(resourceBundle.getLocalizedString("are_you_sure", "Are You sure?"));					// 12
			info.add(resourceBundle.getLocalizedString("delete_all_comments", "Delete comments"));			// 13
			info.add(resourceBundle.getLocalizedString("delete_comment", "Delete this comment"));			// 14
			info.add(new StringBuffer(resourcePath).append("/images/comment_delete.png").toString());		// 15
		} catch (Exception e) {
			log.error(e);
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
	
	public List<String> deleteComments(String id, String commentId, String linkToComments) {
		if (id == null || linkToComments == null) {
			return null;
		}
		IWContext iwc = CoreUtil.getIWContext();
		Feed comments = null;
		synchronized(CommentsEngineBean.class) {
			comments = getCommentsFeed(linkToComments, iwc);
			if (comments == null) {
				return null;
			}
			if (commentId == null) { //	Delete all comments
				comments.setEntries(new ArrayList<Entry>());
			}
			else { // Delete one comment
				comments.setEntries(getUpdatedEntries(initEntries(comments.getEntries()), commentId));
			}
			putFeedToCache(comments, linkToComments, iwc);
		}
		if (!uploadFeed(linkToComments, comments, iwc)) {
			return null;
		}
		List<String> params = new ArrayList<String>();
		params.add(id);
		params.add(linkToComments);
		return params;
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
	
	private boolean uploadFeed(String uri, Feed comments, IWContext iwc) {
		if (uri == null || comments == null || iwc == null) {
			return false;
		}
		
		String commentsContent = null;
		try {
			commentsContent = wfo.outputString(comments);
		} catch (IllegalArgumentException e) {
			log.error(e);
			return false;
		} catch (FeedException e) {
			log.error(e);
			return false;
		}
		
		String fileBase = uri;
		String fileName = uri;
		int index = uri.lastIndexOf(ContentConstants.SLASH);
		if (index != -1) {
			fileBase = uri.substring(0, index);
			fileName = uri.substring(index);
		}
		IWSlideService service = ThemesHelper.getInstance().getSlideService(iwc);
		Thread uploader = new Thread(new CommentsFeedUploader(service, fileBase, fileName, commentsContent));
		uploader.start();
		
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
			log.error(e);
		}
		return defaultValue;
	}
	
}