/*
 * $Id: SearchArticleBean.java,v 1.11 2005/03/10 18:26:59 eiki Exp $
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
import javax.faces.component.html.HtmlOutputLink;
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
import com.idega.core.search.business.SearchResult;
import com.idega.presentation.IWContext;
import com.idega.slide.business.IWSlideSession;
import com.idega.slide.util.IWSlideConstants;
import com.idega.util.IWTimestamp;
import com.idega.webface.WFUtil;
import com.idega.webface.bean.AbstractWFEditableListManagedBean;
import com.idega.webface.bean.WFEditableListCellWrapper;
import com.idega.webface.bean.WFEditableListDataBean;
import com.idega.webface.bean.WFListBean;
import com.idega.webface.model.WFDataModel;

/**
 * Bean for searching articles.   
 * <p>
 * Last modified: $Date: 2005/03/10 18:26:59 $ by $Author: eiki $
 *
 * @author Anders Lindman
 * @version $Revision: 1.11 $
 */

public class SearchArticleBean extends AbstractWFEditableListManagedBean implements WFListBean, Serializable {
	
	public final static String ARTICLE_ID = "article_id";
	
	protected String[] localizationKey = new String[] { "Headline", "Author", "Source", "Creation date", "Language", "Status"};

	
	private WFDataModel _dataModel = null;
	private ActionListener _articleLinkListener = null;

//	private String _id = null;
//	private String _headline = null;
//	private String _published = null;
//	private String _author = null;
//	private String _status = null;
//	private String _testStyle = null;
//	
	private String _searchText = null;
	private String _searchAuthor = null;
	private String _searchCategoryId = null;
	private Date _searchPublishedFrom = null;
	private Date _searchPublishedTo = null;
	
	private Map _allCategories = null;
	
	private boolean searching = false;
	private boolean showResults = false;
			
	/**
	 * Default constructor.
	 */
	public SearchArticleBean() { 
//		_searchPublishedFrom = new Date();
//		_searchText = "searchtext"; 
	}

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
//	public SearchArticleBean(String id, String headline, String published, String author, String status) {
//		_id = id;
//		_headline = headline;
//		_published = published;
//		_author = author;
//		_status = status;
//		_testStyle = "";
//	}
//		
//	public String getId() { return _id; }
//	public String getHeadline() { return _headline; }
//	public String getPublished() { return _published; }
//	public String getAuthor() { return _author; }
//	public String getStatus() { return _status; }
//	public String getTestStyle() { return _testStyle; }

	public String getSearchText() { return _searchText; }
	public String getSearchAuthor() { return _searchAuthor; }
	public String getSearchCategoryId() { return _searchCategoryId; }
	public Date getSearchPublishedFrom() { return _searchPublishedFrom; }
	public Date getSearchPublishedTo() { return _searchPublishedTo; }

//	public void setId(String s) { _id = s; }
//	public void setHeadline(String s) { _headline = s; }
//	public void setPublished(String s) { _published = s; }
//	public void setAuthor(String s) { _author = s; }
//	public void setStatus(String s) { _status = s; }
//	public void setTestStyle(String s) { _testStyle = s; }

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
	
	/**
	 * Generates a search result from the current bean search values. 
	 */
	public void search() {
		searching = true;
		showResults = true;
		updateDataModel(new Integer(0),new Integer(0));
		searching = false;
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
		return 6;
	}

	
	public UIComponent createCellWrapper(String var, int columnIndex){
		//Overridden because of value binding
		WFEditableListCellWrapper component = constructWFEditableListCellWrapper(var, columnIndex);
		WFUtil.setValueBinding(component,"rendered",var+".rendered");
		return component;
	}
	
	/* (non-Javadoc)
	 * @see com.idega.webface.bean.AbstractWFEditableListManagedBean#getUIComponent(java.lang.String, int)
	 */
	public UIComponent getUIComponent(String var, int columnIndex) {

		HtmlOutputLink namePreviewLink = new HtmlOutputLink();
		
		namePreviewLink.setId(columnIndex+"_preview");
		namePreviewLink.setValueBinding("value", WFUtil.createValueBinding("#{"+ var + ".previewIWActionURI}"));
		namePreviewLink.setStyleClass("wf_listtext");

		HtmlOutputText textOnLink = new HtmlOutputText();	
		textOnLink.setId(columnIndex+"_text");
		WFUtil.setValueBindingToArray(textOnLink,"value",var+".values",columnIndex);
		namePreviewLink.getChildren().add(textOnLink);
		
		return namePreviewLink;
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
		if(searching){
			try {
				List l = listArticles();
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
		}
		
		return new ArrayList();
	}
	
	
	public List listArticles() throws XmlException, IOException{
		List list = new ArrayList();		
		
		IWContext iwc = IWContext.getInstance();
	
		try {
			String scope = ArticleUtil.getArticleRootPath();
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
			Search search = searchBusiness.createSearch(getSearchRequest(scope, iwc.getCurrentLocale()));
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
	public SearchRequest getSearchRequest(String scope, Locale locale) throws SearchException {
		SearchRequest s = new SearchRequest();
		s.addSelection(IWSlideConstants.PROPERTY_CREATION_DATE);
		s.addSelection(IWSlideConstants.PROPERTY_CATEGORY);
		s.addScope(new SearchScope(scope));
		SearchExpression whereExpression = null;
		
		
		//String localeString = ""; //((locale!=null)?locale.getLanguage():"");
		//TODO create search input for language
		SearchExpression namePatternExpression = s.compare(CompareOperator.LIKE, IWSlideConstants.PROPERTY_DISPLAY_NAME,"%.article");
		//todo search by the content type
		//SearchExpression contentTypeExpression = s.compare(CompareOperator.LIKE, ArticleItemBean.PROPERTY_CONTENT_TYPE, ArticleItemBean.ARTICLE_FILENAME_SCOPE);
		whereExpression = namePatternExpression;
		//whereExpression = contentTypeExpression;
		
		SearchExpression creationDateFromExpression = null;		
		if(getSearchPublishedFrom() != null){
			Date from = getSearchPublishedFrom();
			
			IWTimestamp stamp = new IWTimestamp(from);
			//the date's time is at 24:00 so anything from that day will not be found. So be back up a day to 24:00
			stamp.addDays(-1);
			
			from = stamp.getDate();
			creationDateFromExpression = s.compare(CompareOperator.GTE, IWSlideConstants.PROPERTY_CREATION_DATE,from);
			whereExpression = s.and(whereExpression,creationDateFromExpression);
		}
		
		SearchExpression creationDateToExpression = null;		
		if(getSearchPublishedTo() != null){
			creationDateToExpression = s.compare(CompareOperator.LTE, IWSlideConstants.PROPERTY_CREATION_DATE,getSearchPublishedTo());
			whereExpression = s.and(whereExpression,creationDateToExpression);
		}
		
//		List categoryExpressions = new ArrayList();
		if(!getSearchCategoryId().equals("-1")){
			SearchExpression categoryExpression = s.compare(CompareOperator.LIKE,IWSlideConstants.PROPERTY_CATEGORY,"%"+getSearchCategoryId()+"%");
			whereExpression = s.and(whereExpression,categoryExpression);
		}
		
		String author = getSearchAuthor();
		if(author!=null && !"".equals(author)){
			SearchExpression authorExpression = s.compare(CompareOperator.LIKE,IWSlideConstants.PROPERTY_CREATOR_DISPLAY_NAME,"%"+author+"%");
			whereExpression = s.and(whereExpression,authorExpression);
		}
		
		
//			}
//			whereExpression = s.and(whereExpression,categoryExpression);
//			for (Iterator iter = categoryList.iterator(); iter.hasNext();) {
//				String categoryName = (String) iter.next();
//				categoryExpressions.add(s.compare(CompareOperator.LIKE,IWSlideConstants.PROPERTY_CATEGORY,"%"+categoryName+"%"));
//			}
//			Iterator expr = categoryExpressions.iterator();
//			if(expr.hasNext()){
//				SearchExpression categoryExpression = (SearchExpression)expr.next();
//				while(expr.hasNext()){
//					categoryExpression = s.or(categoryExpression,(SearchExpression)expr.next());
//				}
//				whereExpression = s.and(whereExpression,categoryExpression);
//			}
//		}
		
		SearchExpression containsExpression = null;		
		if(getSearchText() != null && !"".equals(getSearchText())){
			containsExpression = s.contains(getSearchText());
			if(whereExpression!=null){
				whereExpression = s.and(whereExpression,containsExpression);
			}else{
				whereExpression = containsExpression;
			}
		}
				
		s.setWhereExpression(whereExpression);
	
		System.out.println("------------------------");
		System.out.println(s.asString());
		System.out.println("------------------------");
		return s;
	}
	
	
	public boolean getShowSearchResults(){
		return showResults;
	}
}
