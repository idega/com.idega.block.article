package com.idega.block.article.business;

import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jcr.RepositoryException;
import javax.jcr.observation.EventIterator;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.servlet.ServletException;

import org.springframework.beans.factory.annotation.Autowired;

import com.idega.block.article.component.ArticleItemViewer;
import com.idega.block.article.component.ArticleListViewer;
import com.idega.block.rss.business.RSSAbstractProducer;
import com.idega.block.rss.business.RSSBusiness;
import com.idega.block.rss.business.RSSProducer;
import com.idega.block.rss.data.RSSRequest;
import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.content.business.ContentUtil;
import com.idega.core.builder.business.BuilderService;
import com.idega.core.builder.business.BuilderServiceFactory;
import com.idega.core.builder.data.ICPage;
import com.idega.presentation.IWContext;
import com.idega.repository.RepositoryService;
import com.idega.repository.event.RepositoryEventListener;
import com.idega.util.CoreConstants;
import com.idega.util.IWTimestamp;
import com.idega.util.ListUtil;
import com.idega.util.StringUtil;
import com.idega.util.expression.ELUtil;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;

/**
 * Generates 3 types of rss files for articles.
 * 1. For all articles, /rss/article/
 * 2. For all articles of a specific category, /rss/article/category/mycategory
 * 3. For all articles on a specific page, /rss/article/page/mypageuri
 * @author justinas
 *
 */
public class ArticleRSSProducer extends RSSAbstractProducer implements RSSProducer, RepositoryEventListener {

	private static final Logger LOGGER = Logger.getLogger(ArticleRSSProducer.class.getName());

	private static final String ARTICLE = ContentUtil.getContentBaseFolderPath() + "/article/";
	private static final String ARTICLE_RSS = ARTICLE + "rss/";

	protected static final String ARTICLE_SEARCH_KEY = "*.xml*";

	public static final String RSS_FOLDER_NAME = "rss";
	public static final String RSS_FILE_NAME = "articlefeed.xml";
	public static final String PATH = CoreConstants.WEBDAV_SERVLET_URI + ARTICLE;

	private List<String> rssFileURIsCacheList = new ArrayList<String>();

	@Autowired
	private RepositoryService repository;

	private int numberOfDaysDisplayed = 0;

	public ArticleRSSProducer() {
		super();
	}

	@Override
	public void handleRSSRequest(RSSRequest rssRequest) throws IOException {
		String feedParentFolder = null;
		String feedFile = null;
		String category = getCategory(rssRequest.getExtraUri());
		String extraURI = rssRequest.getExtraUri();
		if (extraURI == null) {
			extraURI = CoreConstants.EMPTY;
		}
		if ((!extraURI.endsWith(CoreConstants.SLASH)) && (extraURI.length() != 0)) {
			extraURI = extraURI.concat(CoreConstants.SLASH);
		}

		List<String> categories = new ArrayList<String>();
		List<String> articles = new ArrayList<String>();
		if (category != null)
			categories.add(category);

		IWContext iwc = getIWContext(rssRequest);
		String language = iwc.getLocale().getLanguage();

		if (StringUtil.isEmpty(extraURI)) {
			feedParentFolder = ARTICLE_RSS;
			feedFile = "all_".concat(language).concat(".xml");
		} else if (category != null) {
			feedParentFolder = ARTICLE_RSS.concat("category/").concat(category).concat(CoreConstants.SLASH);
			feedFile = "feed_.".concat(language).concat(".xml");
		} else {
			//	Have page URI
			feedParentFolder = ARTICLE_RSS.concat("page/").concat(extraURI);
			feedFile = "feed_".concat(language).concat(".xml");
			categories = getCategoriesByURI(extraURI, iwc);
			if (ListUtil.isEmpty(categories)) {
				articles = getArticlesByURI(extraURI, iwc);
			}
		}

		String realURI = CoreConstants.WEBDAV_SERVLET_URI+feedParentFolder+feedFile;
		if (rssFileURIsCacheList.contains(feedFile)) {
			try {
				this.dispatch(realURI, rssRequest);
			} catch (ServletException e) {
				LOGGER.log(Level.WARNING, "Error dispatching: " + realURI, e);
			}
		} else {
			//	Generate RSS and store and the dispatch to it and add a listener to that directory
			try {
				//todo code the 3 different cases (see description)
				searchForArticles(rssRequest, feedParentFolder, feedFile, categories, articles, extraURI);
				rssFileURIsCacheList.add(feedFile);

				this.dispatch(realURI, rssRequest);
			} catch (Exception e) {
				LOGGER.log(Level.WARNING, "Error while searching or dispatching: " + realURI, e);
				throw new IOException(e.getMessage());
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void searchForArticles(RSSRequest rssRequest, String feedParentPath, String feedFileName, List<String> categories, List<String> articles,
			String extraURI) {

		IWContext iwc = getIWContext(rssRequest);
		boolean getAllArticles = false;

		if (StringUtil.isEmpty(extraURI)) {
			getAllArticles = true;
		}

		String serverName = iwc.getServerURL();
		serverName = serverName.substring(0, serverName.length()-1);

		Collection<QueryResult> results = getArticleSearchResults(PATH, categories, iwc);
		if (ListUtil.isEmpty(results)) {
			LOGGER.warning("No results found in: " + PATH + " by the categories: " + categories);
			return;
		}

		List<String> urisToArticles = new ArrayList<String>();
		for (QueryResult result: results) {
			LOGGER.info("Manage the query result: " + result);	//	TODO
//			urisToArticles.add(result.getSearchResultURI());
		}

		if (!ListUtil.isEmpty(articles)) {
			if (ListUtil.isEmpty(categories)) {
				urisToArticles = articles;
			} else {
				urisToArticles.addAll(articles);
			}
		}

		if (!ListUtil.isEmpty(articles) && ListUtil.isEmpty(categories))
			urisToArticles = articles;

		RSSBusiness rss = null;
		SyndFeed articleFeed = null;
		long time = System.currentTimeMillis();
		try {
			rss = IBOLookup.getServiceInstance(iwc, RSSBusiness.class);
		} catch (IBOLookupException e) {
			LOGGER.log(Level.WARNING, "Error getting " + RSSBusiness.class, e);
		}

		String description = CoreConstants.EMPTY;
		String title = CoreConstants.EMPTY;
		if (ListUtil.isEmpty(results) && !getAllArticles) {
			description = "No articles found. Empty feed";
		} else {
			description = "Article feed generated by IdegaWeb ePlatform, Idega Software, http://www.idega.com";

			BuilderService bservice = null;
			String pageKey = null;
			try {
				if (!StringUtil.isEmpty(extraURI)) {
					bservice = BuilderServiceFactory.getBuilderService(iwc);
					pageKey = bservice.getExistingPageKeyByURI(CoreConstants.SLASH + extraURI);
					ICPage icpage = bservice.getICPage(pageKey);
					title = icpage.getName();
				}
			} catch (Exception e) {
				LOGGER.log(Level.WARNING, "Error getting page name from: " + extraURI, e);
			}

			String lang = iwc.getCurrentLocale().getLanguage();
			SyndFeed allArticles = rss.createNewFeed(title, serverName , description, "atom_1.0", lang, new Timestamp(time));

			List<SyndEntry> allEntries = new ArrayList<SyndEntry>();
			for (int i = 0; i < urisToArticles.size(); i++) {
				String articleURL = serverName.concat(urisToArticles.get(i)).concat(CoreConstants.SLASH).concat(lang).concat(".xml");
				articleFeed = rss.getFeed(articleURL);
				if (articleFeed != null)
					allEntries.addAll(articleFeed.getEntries());
			}

			allArticles.setEntries(allEntries);
			String allArticlesContent = null;
			try {
				allArticlesContent = rss.convertFeedToAtomXMLString(allArticles);
			} catch (Exception e) {
				LOGGER.log(Level.WARNING, "Error converting to Atom from: " + allArticles, e);
			}

			try {
				getRepositoryService().uploadFileAndCreateFoldersFromStringAsRoot(feedParentPath, feedFileName, allArticlesContent,
						RSSAbstractProducer.RSS_CONTENT_TYPE);
			} catch (RepositoryException e) {
				LOGGER.log(Level.WARNING, "Error uploading to: " + feedParentPath + feedFileName + " file: " + allArticlesContent, e);
			}
		}
	}

	/**
	 * @param rssRequest
	 * @return
	 */
	protected String fixURI(RSSRequest rssRequest) {
		String uri = CoreConstants.SLASH+rssRequest.getExtraUri();
		if(!uri.endsWith(CoreConstants.SLASH)){
			uri+=CoreConstants.SLASH;
		}

		if(!uri.startsWith(CoreConstants.PATH_FILES_ROOT)){
			uri = CoreConstants.PATH_FILES_ROOT+uri;
		}
		return uri;
	}

	public Collection<QueryResult> getArticleSearchResults(String folder, List<String> categories, IWContext iwc) {
		if (folder == null) {
			return null;
		}
		if (iwc == null) {
			iwc = IWContext.getInstance();
			if (iwc == null) {
				return null;
			}
		}

		IWTimestamp oldest = null;

		if (this.numberOfDaysDisplayed > 0) {
			oldest = IWTimestamp.RightNow();
			oldest.addDays(-this.numberOfDaysDisplayed);
		}

//		String webDavUri = null;
//		try {
//			webDavUri = session.getWebdavServerURI();
//		} catch (RemoteException e) {
//			e.printStackTrace();
//		}
//		if (webDavUri != null) {
//			if(folder.startsWith(webDavUri)){
//				folder = folder.substring(webDavUri.length());
//			}
//			if(folder.startsWith(CoreConstants.SLASH)){
//				folder = folder.substring(1);
//			}
//		}
//		SearchRequest articleSearch = null;
//		try {
//			articleSearch = getSearchRequest(folder, iwc.getCurrentLocale(), oldest, categories);
//		} catch (SearchException e) {
//			e.printStackTrace();
//			return null;
//		}
//		ContentSearch searchBusiness = new ContentSearch(iwc.getIWMainApplication());
//		searchBusiness.setToUseRootAccessForSearch(true);
//		searchBusiness.setToUseDescendingOrder(true);
//		Search search = searchBusiness.createSearch(articleSearch);
//		return search.getSearchResults();

		//	TODO: implement
		return null;
	}

	public Query getSearchRequest(String scope, Locale locale, IWTimestamp oldest, List<String> categoryList) throws RepositoryException {
//		SearchRequest s = new SearchRequest();
//		s.addSelection(IWSlideConstants.PROPERTY_DISPLAY_NAME);
//		s.addSelection(IWSlideConstants.PROPERTY_CREATION_DATE);
//		s.addSelection(IWSlideConstants.PROPERTY_CATEGORY);
//		s.addScope(new SearchScope(scope));
//		SearchExpression expression = null;
//
//		String localeString = CoreConstants.EMPTY;
//		SearchExpression namePatternExpression = s.compare(CompareOperator.LIKE, IWSlideConstants.PROPERTY_DISPLAY_NAME,"%"+localeString+".article");
//		expression = namePatternExpression;
//
//		SearchExpression creationDateExpression = null;
//		if(oldest != null){
//			creationDateExpression = s.compare(CompareOperator.GTE, IWSlideConstants.PROPERTY_CREATION_DATE,oldest.getDate());
//			expression = s.and(expression,creationDateExpression);
//		}
//
//		List<CompareExpression> categoryExpressions = new ArrayList<CompareExpression>();
//		if (categoryList != null) {
//			for (Iterator<String> iter = categoryList.iterator(); iter.hasNext();) {
//				String categoryName = iter.next();
//				categoryExpressions.add(s.compare(CompareOperator.LIKE,IWSlideConstants.PROPERTY_CATEGORY,"%,"+categoryName+",%"));
//			}
//			Iterator<CompareExpression> expr = categoryExpressions.iterator();
//			if(expr.hasNext()){
//				SearchExpression categoryExpression = expr.next();
//				while (expr.hasNext()) {
//					categoryExpression = s.or(categoryExpression, expr.next());
//				}
//				expression = s.and(expression,categoryExpression);
//			}
//		}
//
//		s.setWhereExpression(expression);
//		return s;

		//	TODO: implement
		return null;
	}

	/**
	 * @return Returns the rssFileURIsCacheList.
	 */
	protected List<String> getrssFileURIsCacheList() {
		return rssFileURIsCacheList;
	}

	public String getCategory(String extraURI){
		String category = null;
		if(extraURI == null)
			return null;
		if(extraURI.length() == 0)
			return null;
		if(extraURI.startsWith("category/"))
			category = extraURI.substring("category/".length(), extraURI.length());
		else return null;
		if(category.endsWith(CoreConstants.SLASH))
			category = category.substring(0, category.length()-1);
		return category;
	}

	public String getPageURI(String extraURI){
		if(extraURI.endsWith(CoreConstants.SLASH))
			return extraURI.substring(0, extraURI.length()-1);
		else
			return extraURI;
	}

	public List<String> getCategoriesByURI(String URI, IWContext iwc){
		List<String> categories = new ArrayList<String>();
		BuilderService bservice = null;
		String property = null;
		try {
			bservice = BuilderServiceFactory.getBuilderService(iwc);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		String pageKey = bservice.getExistingPageKeyByURI(CoreConstants.SLASH+URI);
		List<String> moduleId = bservice.getModuleId(pageKey, ArticleListViewer.class.getName());
		if (moduleId != null){
			for (int i = 0; i < moduleId.size(); i++) {
				property = bservice.getProperty(pageKey, moduleId.get(i), "categories");
				if (property != null) {
					if (property.indexOf(",") != -1) {
						Collection<String> strings = ListUtil.convertCommaSeparatedStringToList(property);
						for (String string : strings) {
							categories.add(string);
						}
					}
					else {
						categories.add(property);
					}
				}
				else {
					//Article list viewer without property - displaying all pages
					categories = null;
				}
			}
		}
		return categories;
	}

	public List<String> getArticlesByURI(String URI, IWContext iwc){
		List<String> articles = new ArrayList<String>();
		BuilderService bservice = null;
		try {
			bservice = BuilderServiceFactory.getBuilderService(iwc);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		String pageKey = bservice.getExistingPageKeyByURI(CoreConstants.SLASH+URI);

		List<String> moduleId = bservice.getModuleId(pageKey, ArticleItemViewer.class.getName());

		if (moduleId != null){
			for (int i = 0; i < moduleId.size(); i++) {
				String articleURI = bservice.getProperty(pageKey, moduleId.get(i), "resourcePath");
				articleURI = articleURI.substring(0, articleURI.length());
				articles.add(CoreConstants.WEBDAV_SERVLET_URI+ articleURI);
			}
		}
		return articles;
	}

	@Override
	public void onEvent(EventIterator events) {
		// TODO Auto-generated method stub

	}

	RepositoryService getRepositoryService() {
		if (repository == null) {
			ELUtil.getInstance().autowire(this);
		}
		return repository;
	}
}