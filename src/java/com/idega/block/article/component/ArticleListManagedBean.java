/*
 * $Id: ArticleListManagedBean.java,v 1.8 2005/04/10 22:16:23 eiki Exp $
 * Created on 27.1.2005
 *
 * Copyright (C) 2005 Idega Software hf. All Rights Reserved.
 *
 * This software is the proprietary information of Idega hf.
 * Use is subject to license terms.
 */
package com.idega.block.article.component;

import java.io.IOException;
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
import org.apache.xmlbeans.XmlException;
import com.idega.block.article.bean.ArticleItemBean;
import com.idega.block.article.business.ArticleActionURIHandler;
import com.idega.block.article.business.ArticleUtil;
import com.idega.business.IBOLookup;
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
 *  Last modified: $Date: 2005/04/10 22:16:23 $ by $Author: eiki $
 * 
 * @author <a href="mailto:gummi@idega.com">Gudmundur Agust Saemundsson</a>
 * @version $Revision: 1.8 $
 */
public class ArticleListManagedBean implements ContentListViewerManagedBean {

	private String resourcePath=null;

	private int numberOfDaysDisplayed = 30;
	private List categories = null;

	private String LOCALIZEDKEY_MORE = "itemviewer.more";

	private String detailsViewerPath = null;
	
	/**
	 * 
	 */
	public ArticleListManagedBean() {
		super();
	}

	/* (non-Javadoc)
	 * @see com.idega.content.bean.ContentListViewerManagedBean#getContentItems()
	 */
	public List getContentItems() {
		try {
			List l = loadAllArticlesInFolder(ArticleUtil.getArticleRootPath());
			ContentItemBeanComparator c = new ContentItemBeanComparator();
			c.setReverseOrder(true);
			Collections.sort(l,c);
			return l;
		}
		catch (XmlException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new ArrayList();
	}
	
	
	
	/**
	 * Loads all xml files in the given folder
	 * @param folder
	 * @return List containing ArticleItemBean
	 * @throws XmlException
	 * @throws IOException
	 */
	public List loadAllArticlesInFolder(String folder) throws XmlException, IOException{
		List list = new ArrayList();		
			
		IWContext iwc = IWContext.getInstance();		
		
		IWTimestamp oldest = null;
		
		if(numberOfDaysDisplayed > 0){
			oldest = IWTimestamp.RightNow();
			oldest.addDays(-numberOfDaysDisplayed);
		}
		
		
		try {
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
			Search search = searchBusiness.createSearch(getSearchRequest(scope, iwc.getCurrentLocale(), oldest,categories));
			Collection results = search.getSearchResults();
			
			if(results!=null){				
				for (Iterator iter = results.iterator(); iter.hasNext();) {
					SearchResult result = (SearchResult) iter.next();
					try {
						System.out.println("Attempting to load "+result.getSearchResultURI());
						ArticleItemBean article = new ArticleItemBean();
						article.load(result.getSearchResultURI());
						list.add(article);
					}catch(Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		catch (SearchException e1) {
			e1.printStackTrace();
		}
		

	
//		IWSlideSession session = (IWSlideSession)IBOLookup.getSessionInstance(iwc,IWSlideSession.class);
//		
//		WebdavResource folderResource = session.getWebdavResource(folder);
//		
//		String[] file = folderResource.list();
//
//
//		//TODO(JJ) need to only get the article files. Right now it gets all folders and other filetypes
//		//This code will probably never be used, so not wasting any time on it.
//		if(file!=null){
//			for(int i=0;i<file.length;i++){
//				try {
//					System.out.println("Attempting to load "+file[i].toString());
//					ArticleItemBean article = new ArticleItemBean();
//	//				article.load(folder+"/"+file[i]);
//					//TODO this is a patch since getWebdavResource(folder) seems to return the whole path now
//					article.load(file[i].substring(12));
//					list.add(article);
//				}catch(Exception e) {
//					e.printStackTrace();
//				}
//			}
//		}
		
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
		s.addSelection(IWSlideConstants.PROPERTY_CREATION_DATE);
		s.addSelection(IWSlideConstants.PROPERTY_CATEGORY);
		s.addScope(new SearchScope(scope));
		SearchExpression expression = null;
		
		
		String localeString = ""; //((locale!=null)?locale.getLanguage():"");
		SearchExpression namePatternExpression = s.compare(CompareOperator.LIKE, IWSlideConstants.PROPERTY_DISPLAY_NAME,"%"+localeString+".article");;
		expression = namePatternExpression;
		
		SearchExpression creationDateExpression = null;		
		if(oldest != null){
			creationDateExpression = s.compare(CompareOperator.GTE, IWSlideConstants.PROPERTY_CREATION_DATE,oldest.getDate());
			expression = s.and(expression,creationDateExpression);
		}
		
		List categoryExpressions = new ArrayList();
		if(categoryList != null){
			for (Iterator iter = categoryList.iterator(); iter.hasNext();) {
				String categoryName = (String) iter.next();
				categoryExpressions.add(s.compare(CompareOperator.LIKE,IWSlideConstants.PROPERTY_CATEGORY,","+categoryName+","));
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
//		System.out.println("------------------------");
//		System.out.println(s.asString());
//		System.out.println("------------------------");
		return s;
	}

	/* (non-Javadoc)
	 * @see com.idega.content.bean.ContentListViewerManagedBean#getContentViewer()
	 */
	public ContentItemViewer getContentViewer() {
		ArticleItemViewer viewer = new ArticleItemViewer();
		
		
		
		if(detailsViewerPath != null){
			HtmlOutputLink moreLink = new HtmlOutputLink();
			IWContext iwc = IWContext.getInstance();
			
			String appContext = iwc.getIWMainApplication().getApplicationContextURI();
			if (appContext.endsWith("/")){
				appContext = appContext.substring(0, appContext.lastIndexOf("/"));			
			}
			moreLink.setValue(appContext+detailsViewerPath);
			
			moreLink.getChildren().add(ArticleUtil.getBundle().getLocalizedText(LOCALIZEDKEY_MORE));
			viewer.setDetailsCommand(moreLink);
//			viewer.setRenderBody(false);
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
	 * @see com.idega.content.bean.ContentListViewerManagedBean#setResourcePath(java.lang.String)
	 */
	public void setResourcePath(String path) {
		resourcePath=path;
	}

	/* (non-Javadoc)
	 * @see com.idega.content.bean.ContentListViewerManagedBean#setDetailsViewerPath(java.lang.String)
	 */
	public void setDetailsViewerPath(String path) {
		detailsViewerPath = path;
	}
	
	/**
	 * @return Returns the categories.
	 */
	public List getCategories() {
		return categories;
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
		return (new ArticleActionURIHandler()).getHandlerIdentifier();
	}
}
