/*
 * $Id: ArticleListManagedBean.java,v 1.12 2007/03/19 08:43:48 valdas Exp $
 * Created on 27.1.2005
 *
 * Copyright (C) 2005 Idega Software hf. All Rights Reserved.
 *
 * This software is the proprietary information of Idega hf.
 * Use is subject to license terms.
 */
package com.idega.block.article.bean;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import javax.faces.component.html.HtmlOutputLink;
import org.apache.webdav.lib.search.CompareOperator;
import org.apache.webdav.lib.search.SearchException;
import org.apache.webdav.lib.search.SearchExpression;
import org.apache.webdav.lib.search.SearchRequest;
import org.apache.webdav.lib.search.SearchScope;
import org.apache.webdav.lib.search.expressions.CompareExpression;
import org.apache.xmlbeans.XmlException;
import com.idega.block.article.business.ArticleActionURIHandler;
import com.idega.block.article.business.ArticleUtil;
import com.idega.block.article.component.ArticleItemViewer;
import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.content.bean.ContentItemBeanComparator;
import com.idega.content.bean.ContentListViewerManagedBean;
import com.idega.content.business.ContentSearch;
import com.idega.content.presentation.ContentItemViewer;
import com.idega.core.search.business.Search;
import com.idega.core.search.business.SearchResult;
import com.idega.presentation.IWContext;
import com.idega.slide.business.IWSlideSession;
import com.idega.slide.util.IWSlideConstants;
import com.idega.util.IWTimestamp;



/**
 * 
 *  Last modified: $Date: 2007/03/19 08:43:48 $ by $Author: valdas $
 * 
 * @author <a href="mailto:gummi@idega.com">Gudmundur Agust Saemundsson</a>
 * @version $Revision: 1.12 $
 */
public class ArticleListManagedBean implements ContentListViewerManagedBean {

	private int numberOfDaysDisplayed = 0;
	private List categories = null;

	private String LOCALIZEDKEY_MORE = "itemviewer.more";

	private String detailsViewerPath = null;
	private boolean headlineAsLink=false;
	private String resourcePath;
	private int maxNumberOfDisplayed=-1;
	
	/**
	 * 
	 */
	public ArticleListManagedBean() {
		super();
	}

	/* (non-Javadoc)
	 * @see com.idega.content.bean.ContentListViewerManagedBean#getContentItems()
	 */
	public List<ArticleItemBean> getContentItems() {
		try {
			List<ArticleItemBean> l = loadAllArticlesInFolder(ArticleUtil.getArticleBaseFolderPath());
			ContentItemBeanComparator c = new ContentItemBeanComparator();
			c.setReverseOrder(true);
			Collections.sort(l,c);
			return l;
		}
		catch (XmlException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return new ArrayList<ArticleItemBean>();
	}
	
	
	
	/**
	 * Loads all xml files in the given folder
	 * @param folder
	 * @return List containing ArticleItemBean
	 * @throws XmlException
	 * @throws IOException
	 */
	public List<ArticleItemBean> loadAllArticlesInFolder(String folder) throws XmlException, IOException{
		List<ArticleItemBean> list = new ArrayList<ArticleItemBean>();		
			
		IWContext iwc = IWContext.getInstance();		
		
		/*IWTimestamp oldest = null;
		
		if(this.numberOfDaysDisplayed > 0){
			oldest = IWTimestamp.RightNow();
			oldest.addDays(-this.numberOfDaysDisplayed);
		}*/
		
		
		/*try {
			String scope = folder;
			IWSlideSession session = (IWSlideSession)IBOLookup.getSessionInstance(iwc,IWSlideSession.class);
			if(scope != null){
				if(scope.startsWith(session.getWebdavServerURI())){
					scope = scope.substring(session.getWebdavServerURI().length());
				}
				if(scope.startsWith("/")){
					scope = scope.substring(1);
				}
			}
			ContentSearch searchBusiness = new ContentSearch(iwc.getIWMainApplication());
			Locale requestedLocale = iwc.getCurrentLocale();
			searchBusiness.setToUseDescendingOrder(true);
			Search search = searchBusiness.createSearch(getSearchRequest(scope, requestedLocale, oldest,this.categories));
			Collection results = search.getSearchResults();*/
			Collection results = getArticleSearcResults(folder, this.categories, iwc);
			int count=0;
			if (results == null) {
				return list;
			}
			ArticleItemBean article = null;
			for (Iterator iter = results.iterator(); iter.hasNext();) {
				SearchResult result = (SearchResult) iter.next();
				try {
					System.out.println("ArticleListManagedBean: Attempting to load "+result.getSearchResultURI());
					article = new ArticleItemBean();
					article.setResourcePath(result.getSearchResultURI());
					article.load();
					if (!article.isDummyArticle()) {
						if(article.getAvilableInRequestedLanguage()){
							int maxNumber = getMaxNumberOfDisplayed();
							if(maxNumber==-1 || count<maxNumber){
								list.add(article);
								count++;
								if (count == maxNumber) {
									break;
								}
							}
						}
					}
				}catch(Exception e) {
					e.printStackTrace();
				}
			}
		/*}
		catch (SearchException e1) {
			e1.printStackTrace();
		}*/
			
		return list;
	}

	/**
	 * @param folder
	 * @param localeString
	 * @param oldest
	 * @param categoryList
	 * @throws SearchException
	 */
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

	/* (non-Javadoc)
	 * @see com.idega.content.bean.ContentListViewerManagedBean#getContentViewer()
	 */
	public ContentItemViewer getContentViewer() {
		ArticleItemViewer viewer = new ArticleItemViewer();
		
		if(this.detailsViewerPath != null){
			viewer.setDetailsViewerPath(this.detailsViewerPath);
			HtmlOutputLink moreLink = viewer.getEmptyMoreLink();
			moreLink.getChildren().add(ArticleUtil.getBundle().getLocalizedText(this.LOCALIZEDKEY_MORE));
			viewer.setDetailsCommand(moreLink);
			viewer.setHeadlineAsLink(getHeadlineAsLink());
		}
		
		return viewer;
	}
	
	/* (non-Javadoc)
	 * @see com.idega.content.bean.ContentListViewerManagedBean#getAttachmentViewers()
	 */
	public List getAttachmentViewers() {
		return null;
	}

	/* (non-Javadoc)
	 * @see com.idega.content.bean.ContentListViewerManagedBean#setDetailsViewerPath(java.lang.String)
	 */
	public void setDetailsViewerPath(String path) {
		this.detailsViewerPath = path;
	}
	
	/**
	 * @return Returns the categories.
	 */
	public List getCategories() {
		return this.categories;
	}
	/**
	 * @param categories The categories to set.
	 */
	public void setCategories(List categories) {
		this.categories = categories;
	}

	/* (non-Javadoc)
	 * @see com.idega.content.bean.ContentListViewerManagedBean#getIWActionURIHandlerIdentifier()
	 */
	public String getIWActionURIHandlerIdentifier() {
		return ArticleActionURIHandler.HANDLER_IDENTIFIER;
	}

	/**
	 * <p>
	 * TODO tryggvil describe method setHeadlineAsLink
	 * </p>
	 * @param headlineAsLink
	 */
	public void setHeadlineAsLink(boolean headlineAsLink) {
		this.headlineAsLink=headlineAsLink;
	}
	
	public boolean getHeadlineAsLink(){
		return this.headlineAsLink;
	}

	/* (non-Javadoc)
	 * @see com.idega.content.bean.ContentListViewerManagedBean#setResourcePath(java.lang.String)
	 */
	public void setBaseFolderPath(String path) {
		this.resourcePath=path;
	}

	/* (non-Javadoc)
	 * @see com.idega.content.bean.ContentListViewerManagedBean#getResourcePath()
	 */
	public String getBaseFolderPath() {
		return this.resourcePath;
	}

	/* (non-Javadoc)
	 * @see com.idega.content.bean.ContentListViewerManagedBean#setMaxNumberOfDisplayed(int)
	 */
	public void setMaxNumberOfDisplayed(int maxItems) {
		this.maxNumberOfDisplayed=maxItems;
	}

	/* (non-Javadoc)
	 * @see com.idega.content.bean.ContentListViewerManagedBean#getMaxNumberOfDisplayed()
	 */
	public int getMaxNumberOfDisplayed() {
		return this.maxNumberOfDisplayed;
	}
	
	public Collection getArticleSearcResults(String folder, List categories, IWContext iwc) {
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
	
}