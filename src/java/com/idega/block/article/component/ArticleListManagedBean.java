/*
 * $Id: ArticleListManagedBean.java,v 1.2 2005/02/14 15:18:48 gummi Exp $
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
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import org.apache.webdav.lib.search.CompareOperator;
import org.apache.webdav.lib.search.SearchException;
import org.apache.webdav.lib.search.SearchExpression;
import org.apache.webdav.lib.search.SearchRequest;
import org.apache.webdav.lib.search.SearchScope;
import org.apache.xmlbeans.XmlException;
import com.idega.block.article.bean.ArticleItemBean;
import com.idega.block.article.business.ArticleUtil;
import com.idega.business.IBOLookup;
import com.idega.content.bean.ContentListViewerManagedBean;
import com.idega.content.business.ContentSearch;
import com.idega.content.presentation.ContentItemViewer;
import com.idega.core.search.business.Search;
import com.idega.core.search.business.SearchQuery;
import com.idega.core.search.business.SearchResult;
import com.idega.core.search.data.AdvancedSearchQuery;
import com.idega.presentation.IWContext;
import com.idega.slide.business.IWSlideSession;
import com.idega.slide.util.IWSlideConstants;
import com.idega.util.IWTimestamp;



/**
 * 
 *  Last modified: $Date: 2005/02/14 15:18:48 $ by $Author: gummi $
 * 
 * @author <a href="mailto:gummi@idega.com">Gudmundur Agust Saemundsson</a>
 * @version $Revision: 1.2 $
 */
public class ArticleListManagedBean implements ContentListViewerManagedBean {

	private String resourcePath=null;

	private int numberOfDaysDisplayed = 30;
	
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
			return loadAllArticlesInFolder(ArticleUtil.getArticleRootPath());
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
		
		SearchQuery query = new AdvancedSearchQuery();
		
		
		
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
			Search search = searchBusiness.createSearch(getSearchRequest(scope, iwc.getCurrentLocale(), oldest));
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
	 * @throws SearchException
	 */
	public SearchRequest getSearchRequest(String scope, Locale locale, IWTimestamp oldest) throws SearchException {
		SearchRequest s = new SearchRequest();
		s.addSelection(IWSlideConstants.PROPERTY_CREATION_DATE);
		s.addScope(new SearchScope(scope));
		SearchExpression expression = null;
		
		String localeString = ""; //((locale!=null)?locale.getLanguage():"");
		
		if(oldest != null){
			expression = s.and( s.compare(CompareOperator.LIKE, IWSlideConstants.PROPERTY_DISPLAY_NAME,"%"+localeString+".article"),s.compare(CompareOperator.GTE, IWSlideConstants.PROPERTY_CREATION_DATE,oldest.getDate()));
		} else {
			expression = s.compare(CompareOperator.LIKE, IWSlideConstants.PROPERTY_DISPLAY_NAME,"%"+localeString+".article");
		}
		//add other properties
		
		s.setWhereExpression(expression);
		System.out.println("------------------------");
		System.out.println(s.asString());
		System.out.println("------------------------");
		return s;
	}

	/* (non-Javadoc)
	 * @see com.idega.content.bean.ContentListViewerManagedBean#getContentViewer()
	 */
	public ContentItemViewer getContentViewer() {
		ArticleItemView viewer = new ArticleItemView();
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
	
}
