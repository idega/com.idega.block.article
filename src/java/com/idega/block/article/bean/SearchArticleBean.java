/*
 * $Id: SearchArticleBean.java,v 1.9 2005/03/08 14:45:42 laddi Exp $
 *
 * Copyright (C) 2004 Idega. All Rights Reserved.
 *
 * This software is the proprietary information of Idega.
 * Use is subject to license terms.
 *
 */
package com.idega.block.article.bean;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.event.ActionListener;
import org.apache.webdav.lib.search.CompareOperator;
import org.apache.webdav.lib.search.SearchException;
import org.apache.webdav.lib.search.SearchExpression;
import org.apache.webdav.lib.search.SearchRequest;
import org.apache.webdav.lib.search.SearchScope;
import org.apache.xmlbeans.XmlException;
import com.idega.block.article.business.ArticleUtil;
import com.idega.business.IBOLookup;
import com.idega.content.bean.ContentItemBeanComparator;
import com.idega.content.business.ContentSearch;
import com.idega.core.search.business.Search;
import com.idega.core.search.business.SearchQuery;
import com.idega.core.search.business.SearchResult;
import com.idega.core.search.data.AdvancedSearchQuery;
import com.idega.presentation.IWContext;
import com.idega.slide.business.IWSlideSession;
import com.idega.slide.util.IWSlideConstants;
import com.idega.util.IWTimestamp;
import com.idega.webface.bean.AbstractWFEditableListManagedBean;
import com.idega.webface.bean.WFEditableListDataBean;
import com.idega.webface.bean.WFListBean;
import com.idega.webface.model.WFDataModel;

/**
 * Bean for searching articles.   
 * <p>
 * Last modified: $Date: 2005/03/08 14:45:42 $ by $Author: laddi $
 *
 * @author Anders Lindman
 * @version $Revision: 1.9 $
 */

public class SearchArticleBean extends AbstractWFEditableListManagedBean implements WFListBean, Serializable {
	
	public final static String ARTICLE_ID = "article_id";
	
	protected String[] localizationKey = new String[] { "Headline", "Published", "Author", "Status" };

	
	private WFDataModel _dataModel = null;
	private ActionListener _articleLinkListener = null;

	private String _id = null;
	private String _headline = null;
	private String _published = null;
	private String _author = null;
	private String _status = null;
	private String _testStyle = null;
	
	private String _searchText = null;
	private String _searchAuthor = null;
	private String _searchCategoryId = null;
	private Date _searchPublishedFrom = null;
	private Date _searchPublishedTo = null;
	
	private Map _allCategories = null;
	
	private String[] testColumnHeaders = { "Headline", "Published", "Author", "Status" };				

	/**
	 * Default constructor.
	 */
	public SearchArticleBean() { _searchPublishedFrom = new Date(); _searchText = "searchtext"; }

	/**
	 * Constructs a new search article bean with the specified article link listener.
	 */
	public SearchArticleBean(ActionListener l) {
		this();
		setArticleLinkListener(l);
	}
	
	/**
	 * Constructs a new search article bean with the specified parameters. 
	 */
	public SearchArticleBean(String id, String headline, String published, String author, String status) {
		_id = id;
		_headline = headline;
		_published = published;
		_author = author;
		_status = status;
		_testStyle = "";
	}
		
	public String getId() { return _id; }
	public String getHeadline() { return _headline; }
	public String getPublished() { return _published; }
	public String getAuthor() { return _author; }
	public String getStatus() { return _status; }
	public String getTestStyle() { return _testStyle; }

	public String getSearchText() { return _searchText; }
	public String getSearchAuthor() { return _searchAuthor; }
	public String getSearchCategoryId() { return _searchCategoryId; }
	public Date getSearchPublishedFrom() { return _searchPublishedFrom; }
	public Date getSearchPublishedTo() { return _searchPublishedTo; }

	public void setId(String s) { _id = s; }
	public void setHeadline(String s) { _headline = s; }
	public void setPublished(String s) { _published = s; }
	public void setAuthor(String s) { _author = s; }
	public void setStatus(String s) { _status = s; }
	public void setTestStyle(String s) { _testStyle = s; }

	public void setSearchText(String s) { _searchText = s; }
	public void setSearchAuthor(String s) { _searchAuthor = s; }
	public void setSearchCategoryId(String s) { _searchCategoryId = s; }
	public void setSearchPublishedFrom(Date d) { _searchPublishedFrom = d; }
	public void setSearchPublishedTo(Date d) { _searchPublishedTo = d; }
	
	public ActionListener getArticleLinkListener() { return _articleLinkListener; }
	public void setArticleLinkListener(ActionListener l) { _articleLinkListener = l; }
	
	/**
	 * Returns all categories available for articles.
	 */
	public Map getCategories() {
		if (_allCategories == null) {
			_allCategories = new LinkedHashMap();
			_allCategories.put("All categories", "" + new Integer(-1));
			_allCategories.put("Public news", "" + new Integer(1));
			_allCategories.put("Business news", "" + new Integer(2));
			_allCategories.put("Company info", "" + new Integer(3));
			_allCategories.put("General info", "" + new Integer(4));
			_allCategories.put("IT stuff", "" + new Integer(5));
			_allCategories.put("Press releases", "" + new Integer(6));
			_allCategories.put("Internal info", "" + new Integer(7));
		}
		return _allCategories;
	}

//	/**
//	 * @see com.idega.webface.bean.WFListBean#getDataModel() 
//	 */
//	public DataModel getDataModel() {
//		return _dataModel;
//	}
//	
//	/**
//	 * @see com.idega.webface.bean.WFListBean#setDataModel() 
//	 */
//	public void setDataModel(DataModel dataModel) {
//		_dataModel = (WFDataModel) dataModel;
//	}
//	
//	/**
//	 * @see com.idega.webface.bean.WFListBean#updateDataModel() 
//	 */
//	public void updateDataModel(Integer start, Integer rows) {
//		if (_dataModel == null) {
//			_dataModel = new WFDataModel();
//		}
//		ArticleItemBean[] articleItemBean;
//		try {
//			articleItemBean = (ArticleItemBean[]) ArticleListBean.loadAllArticlesInFolder(ArticleUtil.getArticleRootPath()).toArray(new ArticleItemBean[0]);
//			int availableRows = articleItemBean.length;
//
//			int nrOfRows = rows.intValue();
//			if (nrOfRows == 0) {
//				nrOfRows = availableRows;
//			}
//			int maxRow = Math.min(start.intValue() + nrOfRows,availableRows);
//			for (int i = start.intValue(); i < maxRow; i++) {
//				String id = articleItemBean[i].getFolderLocation()+"/"+articleItemBean[i].getHeadline()+ArticleItemBean.ARTICLE_SUFFIX;
//				ArticleListBean bean = new ArticleListBean(id, articleItemBean[i].getHeadline(), "", articleItemBean[i].getAuthor(), articleItemBean[i].getStatus());
//				_dataModel.set(bean, i);
//			}
//			_dataModel.setRowCount(availableRows);
//		}
//		catch (XmlException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
	

	
	/**
	 * Generates a search result from the current bean search values. 
	 */
	public void search() {
//		ArticleListBean a = new ArticleListBean("100", "Search result", "...", "...", "...");
//		_dataModel.set(a, _dataModel.getRowCount());
//		_dataModel.setRowCount(_dataModel.getRowCount() + 1);		
	}

	/* (non-Javadoc)
	 * @see com.idega.webface.bean.AbstractWFEditableListManagedBean#getData()
	 */
	public WFEditableListDataBean[] getData() {
		List beans = getContentItems();
		if(beans!=null){
			return (ArticleSearchResultBean[])beans.toArray(new ArticleSearchResultBean[beans.size()]);
		} else {
			return new ArticleSearchResultBean[0];
		}
	}

	/* (non-Javadoc)
	 * @see com.idega.webface.bean.AbstractWFEditableListManagedBean#getNumberOfColumns()
	 */
	public int getNumberOfColumns() {
		return 4;
	}

	/* (non-Javadoc)
	 * @see com.idega.webface.bean.AbstractWFEditableListManagedBean#getUIComponent(java.lang.String, int)
	 */
	public UIComponent getUIComponent(String var, int columnIndex) {
		HtmlOutputText t = new HtmlOutputText();
		t.setStyleClass("wf_listtext");
		return t;
	}

	/* (non-Javadoc)
	 * @see com.idega.webface.bean.AbstractWFEditableListManagedBean#getHeader(int)
	 */
	public UIComponent getHeader(int columnIndex) {
		return ArticleUtil.getBundle().getLocalizedText(localizationKey[columnIndex]);
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
		
		SearchQuery query = new AdvancedSearchQuery();
		
		List categories = null;
		
		IWTimestamp oldest = null;
		int numberOfDaysDisplayed = -1;
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
						ArticleItemBean article = new ArticleSearchResultBean();
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
				categoryExpressions.add(s.compare(CompareOperator.LIKE,IWSlideConstants.PROPERTY_CATEGORY,"%"+categoryName+"%"));
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
		System.out.println("------------------------");
		System.out.println(s.asString());
		System.out.println("------------------------");
		return s;
	}
}
