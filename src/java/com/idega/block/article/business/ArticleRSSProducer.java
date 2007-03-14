package com.idega.block.article.business;

import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.webdav.lib.search.CompareOperator;
import org.apache.webdav.lib.search.SearchException;
import org.apache.webdav.lib.search.SearchExpression;
import org.apache.webdav.lib.search.SearchRequest;
import org.apache.webdav.lib.search.SearchScope;
import org.apache.webdav.lib.search.expressions.CompareExpression;
import org.w3.x2001.xmlSchema.LengthDocument;

import com.idega.block.article.component.ArticleItemViewer;
import com.idega.block.article.component.ArticleListViewer;
import com.idega.block.rss.business.RSSAbstractProducer;
import com.idega.block.rss.business.RSSBusiness;
import com.idega.block.rss.business.RSSProducer;
import com.idega.block.rss.data.RSSRequest;
import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.content.business.ContentConstants;
import com.idega.content.business.ContentItemRssProducer;
import com.idega.content.business.ContentSearch;
import com.idega.content.business.ContentUtil;
import com.idega.core.builder.business.BuilderService;
import com.idega.core.builder.business.BuilderServiceFactory;
import com.idega.core.builder.data.ICPage;
import com.idega.core.search.business.Search;
import com.idega.core.search.business.SearchResult;
import com.idega.presentation.IWContext;
import com.idega.presentation.Page;
import com.idega.slide.business.IWContentEvent;
import com.idega.slide.business.IWSlideChangeListener;
import com.idega.slide.business.IWSlideService;
import com.idega.slide.business.IWSlideSession;
import com.idega.slide.util.IWSlideConstants;
import com.idega.util.IWTimestamp;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndLink;

/**
 * Generates 3 types of rss files for articles.
 * 1. For all articles, /rss/article/
 * 2. For all articles of a specific category, /rss/article/category/mycategory
 * 3. For all articles on a specific page, /rss/article/page/mypageuri
 * @author justinas
 *
 */
public class ArticleRSSProducer extends RSSAbstractProducer implements RSSProducer, IWSlideChangeListener{	

	protected static final String ARTICLE_SEARCH_KEY = "*.xml*";
	public static final String RSS_FOLDER_NAME = "rss";
	public static final String RSS_FILE_NAME = "articlefeed.xml";

	public static final String PATH = ContentConstants.CONTENT + ContentUtil.getContentBaseFolderPath() + "/article/";//"/files/cms/article";
	private List rssFileURIsCacheList = new ArrayList();
	private static Log log = LogFactory.getLog(ContentItemRssProducer.class);
	
	private int numberOfDaysDisplayed = 0;
	
	public ArticleRSSProducer() {
		super();
	}

	public void handleRSSRequest(RSSRequest rssRequest) throws IOException {
		String feedParentFolder = null;
		String feedFile = null;
		String category = getCategory(rssRequest.getExtraUri());
		String extraURI = rssRequest.getExtraUri();
		if(extraURI == null){
			extraURI = "";
		}
			if((!extraURI.endsWith("/")) && (extraURI.length() != 0)){
				extraURI = extraURI.concat("/");
			}
		
		List<String> categories = new ArrayList<String>();
		List<String> articles = new ArrayList<String>();
		if (category != null)
			categories.add(category);
		IWContext iwc = getIWContext(rssRequest);
		if(extraURI.length() ==0){
			feedParentFolder = "/files/cms/article/rss/";
			feedFile = "all_"+iwc.getLocale().toString()+".xml";			
		}
		else if(category != null){		
			feedParentFolder = "/files/cms/article/rss/category/"+category+"/";
			feedFile = "feed_"+iwc.getLocale().toString()+".xml";			
		}
		else{	//have page URI
			feedParentFolder = "/files/cms/article/rss/page/"+extraURI;
			feedFile = "feed_"+iwc.getLocale().toString()+".xml";
			categories = getCategoriesByURI(extraURI, iwc);
			if (categories != null)
				articles = getArticlesByURI(extraURI, iwc);
		}
		String realURI = "/content"+feedParentFolder+feedFile;		
		
		if(rssFileURIsCacheList.contains(feedFile)){
			try {
				this.dispatch(realURI, rssRequest);
			} catch (ServletException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else{
			//generate rss and store and the dispatch to it
			//and add a listener to that directory
			try {
				//todo code the 3 different cases (see description)
				searchForArticles(rssRequest,feedParentFolder,feedFile, categories, articles, extraURI);
				rssFileURIsCacheList.add(feedFile);
					
				this.dispatch(realURI, rssRequest);
			} catch (Exception e) {
				throw new IOException(e.getMessage());
			}
		}
	}
	public void searchForArticles(RSSRequest rssRequest, String feedParentPath, String feedFileName, List categories, List<String> articles, String extraURI) {
		IWContext iwc = getIWContext(rssRequest);
		//TODO fix serverName
		String serverName = iwc.getServerURL();
		serverName = serverName.substring(0, serverName.length()-1);
		
		Collection results = getArticleSearchResults(PATH, categories, iwc);
		if (results == null) {
			log.error("ContentSearch.doSimpleDASLSearch returned results Collection, which is null: " + results);
			return;
		}
		Iterator it = results.iterator();
		List <String> urisToArticles = new ArrayList<String>();
		String uri = null;
		Object o = null;
		while (it.hasNext()) {
			o = it.next();
			if (o instanceof SearchResult) {
				uri = ((SearchResult) o).getSearchResultURI();
				urisToArticles.add(uri);
			}
		}
		if(!articles.isEmpty()){
			if(categories.isEmpty()){
				urisToArticles = articles;
			}
			else{
				urisToArticles.addAll(articles);
			}
		}
		
		
		if(!articles.isEmpty() && categories.isEmpty())			
			urisToArticles = articles;
		
		RSSBusiness rss = null;
		SyndFeed articleFeed = null;
		Date now = new Date();
		long time = now.getTime();
		try {
			rss = (RSSBusiness) IBOLookup.getServiceInstance(iwc,RSSBusiness.class);			
		} catch (IBOLookupException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String title = null;
		BuilderService bservice = null;
		String pageKey = null;
		try {
			if(!extraURI.equals("")){
				bservice = BuilderServiceFactory.getBuilderService(iwc);
				pageKey = bservice.getExistingPageKeyByURI("/"+extraURI);
				ICPage icpage = bservice.getICPage(pageKey);
				title = icpage.getName();
			}			
		} catch (RemoteException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String description = "";
		if (uri == null)
			description = "No articles found. Empty feed";
		else
			description = "File feed generated by IdegaWeb ePlatform, <a href'http://www.idega.com'/>. This feed lists the latest documents from the folder: "+uri;
		SyndFeed allArticles = rss.createNewFeed(title, serverName , description, "atom_1.0", iwc.getCurrentLocale().toString(), new Timestamp(time));
			
		List<SyndEntry> allEntries = new ArrayList<SyndEntry>();
		for (int i = 0; i < urisToArticles.size(); i++) {
			String lang = iwc.getCurrentLocale().toString();
	
			String articleURL = serverName + urisToArticles.get(i)+"/"+lang+".xml";
			articleFeed = rss.getFeed(articleURL);
			allEntries.addAll(articleFeed.getEntries());
		}
		
		allArticles.setEntries(allEntries);
		String allArticlesContent = null;
		try {
			allArticlesContent = rss.convertFeedToAtomXMLString(allArticles);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
		IWSlideService service = this.getIWSlideService(rssRequest);
		
			service.uploadFileAndCreateFoldersFromStringAsRoot(feedParentPath, feedFileName, allArticlesContent, this.RSS_CONTENT_TYPE, true);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public void onSlideChange(IWContentEvent contentEvent) {
//			On a file change this code checks if an rss file already exists and if so updates it (overwrites) with a new folder list
			String URI = contentEvent.getContentEvent().getUri();
			//only do it for articles (whenever something changes in the articles folder)
			if(URI.indexOf("/cms/article/")>-1){
				//TODO dont remove cache on COmments change, just check the URI for commentesrss.
				getrssFileURIsCacheList().clear();
			}
	}	
	
	/**
	 * @param rssRequest
	 * @return
	 */
	protected String fixURI(RSSRequest rssRequest) {
		String uri = "/"+rssRequest.getExtraUri();
		if(!uri.endsWith("/")){
			uri+="/";
		}
		
		if(!uri.startsWith("/files")){
			uri = "/files"+uri;
		}
		return uri;
	}

	public Collection getArticleSearchResults(String folder, List categories, IWContext iwc) {
		if (folder == null) {
			return null;
		}
		if (iwc == null) {
			return null;
		}
		IWTimestamp oldest = null;
		
		if (this.numberOfDaysDisplayed > 0) {
			oldest = IWTimestamp.RightNow();
			oldest.addDays(-this.numberOfDaysDisplayed);
		}
		
		IWSlideSession session = null;
		try {
			session = (IWSlideSession) IBOLookup.getSessionInstance(iwc,IWSlideSession.class);
		} catch (IBOLookupException e) {
			e.printStackTrace();
			return null;
		}
		String webDavUri = null;
		try {
			webDavUri = session.getWebdavServerURI();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		if (webDavUri != null) {
			if(folder.startsWith(webDavUri)){
				folder = folder.substring(webDavUri.length());
			}
			if(folder.startsWith("/")){
				folder = folder.substring(1);
			}
		}
		SearchRequest articleSearch = null;
		try {
			articleSearch = getSearchRequest(folder, iwc.getCurrentLocale(), oldest, categories);
		} catch (SearchException e) {
			e.printStackTrace();
			return null;
		}
		ContentSearch searchBusiness = new ContentSearch(iwc.getIWMainApplication());
		searchBusiness.setToUseRootAccessForSearch(true);
		
		searchBusiness.setToUseDescendingOrder(true);
		Search search = searchBusiness.createSearch(articleSearch);
		return search.getSearchResults();
	}
	public SearchRequest getSearchRequest(String scope, Locale locale, IWTimestamp oldest, List categoryList) throws SearchException {
		SearchRequest s = new SearchRequest();
		s.addSelection(IWSlideConstants.PROPERTY_DISPLAY_NAME);
		s.addSelection(IWSlideConstants.PROPERTY_CREATION_DATE);
		s.addSelection(IWSlideConstants.PROPERTY_CATEGORY);
		s.addScope(new SearchScope(scope));
		SearchExpression expression = null;
		
		
		String localeString = "";
		SearchExpression namePatternExpression = s.compare(CompareOperator.LIKE, IWSlideConstants.PROPERTY_DISPLAY_NAME,"%"+localeString+".article");
		expression = namePatternExpression;
		
		SearchExpression creationDateExpression = null;		
		if(oldest != null){
			creationDateExpression = s.compare(CompareOperator.GTE, IWSlideConstants.PROPERTY_CREATION_DATE,oldest.getDate());
			expression = s.and(expression,creationDateExpression);
		}
		
		List<CompareExpression> categoryExpressions = new ArrayList<CompareExpression>();
		if(categoryList != null){
			for (Iterator iter = categoryList.iterator(); iter.hasNext();) {
				String categoryName = (String) iter.next();
				categoryExpressions.add(s.compare(CompareOperator.LIKE,IWSlideConstants.PROPERTY_CATEGORY,"%,"+categoryName+",%"));
			}
			Iterator expr = categoryExpressions.iterator();
			if(expr.hasNext()){
				SearchExpression categoryExpression = (SearchExpression)expr.next();
				while(expr.hasNext()){
					categoryExpression = s.or(categoryExpression,(SearchExpression)expr.next());
				}
				expression = s.and(expression,categoryExpression);
			}
		}
		s.setWhereExpression(expression);
		return s;
	}	
	

	/**
	 * @return Returns the rssFileURIsCacheList.
	 */
	protected List getrssFileURIsCacheList() {
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
		if(category.endsWith("/"))
			category = category.substring(0, category.length()-1);
		return category;
	}

	public String getPageURI(String extraURI){
//		String pageURI = nuell;
		if(extraURI.endsWith("/"))
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String pageKey = bservice.getExistingPageKeyByURI("/"+URI);		
		List<String> moduleId = bservice.getModuleId(pageKey, ArticleListViewer.class.getName());
		if (moduleId != null){
			for (int i = 0; i < moduleId.size(); i++) {
				property = bservice.getProperty(pageKey, moduleId.get(i), "categories");
				if (property != null){
					categories.add(property);	
				}
				else{
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String pageKey = bservice.getExistingPageKeyByURI("/"+URI);
		
		List<String> moduleId = bservice.getModuleId(pageKey, ArticleItemViewer.class.getName());

		if (moduleId != null){
			for (int i = 0; i < moduleId.size(); i++) {
				String articleURI = bservice.getProperty(pageKey, moduleId.get(i), "resourcePath");
				articleURI = articleURI.substring(0, articleURI.length());
				articles.add("/content"+ articleURI);
			}
		}		
		return articles;
	}
}
