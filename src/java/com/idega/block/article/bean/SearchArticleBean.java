/*
 * $Id: SearchArticleBean.java,v 1.19 2006/04/09 12:32:00 laddi Exp $
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
import com.idega.content.business.CategoryBean;
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

/**
 * Bean for searching articles.   
 * <p>
 * Last modified: $Date: 2006/04/09 12:32:00 $ by $Author: laddi $
 *
 * @author Anders Lindman
 * @version $Revision: 1.19 $
 */

public class SearchArticleBean extends AbstractWFEditableListManagedBean implements WFListBean, Serializable {
	
	/**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 3257288011107481141L;

	public final static String ARTICLE_ID = "article_id";
	
	protected String[] localizationKey = new String[] { "Headline", "Author", "Source", "Creation date", "Language", "Status"};

	private ActionListener _articleLinkListener = null;


	private String _searchText = null;
	private String _searchAuthor = null;
	private String _searchCategory = null;
	private Date _searchPublishedFrom = null;
	private Date _searchPublishedTo = null;
	
	private Map _allCategories = null;
	
	private boolean searching = false;
	private boolean showResults = false;
			
	/**
	 * Default constructor.
	 */
	public SearchArticleBean() { 
		//No action...
	}

	/**
	 * Constructs a new search article bean with the specified article link listener.
	 */
	public SearchArticleBean(ActionListener l) {
		this();
		setArticleLinkListener(l);
	}
	

	public String getSearchText() { return this._searchText; }
	public String getSearchAuthor() { return this._searchAuthor; }
	public String getSearchCategory() { return this._searchCategory; }
	public Date getSearchPublishedFrom() { return this._searchPublishedFrom; }
	public Date getSearchPublishedTo() { return this._searchPublishedTo; }

	public void setSearchText(String s) { this._searchText = s; }
	public void setSearchAuthor(String s) { this._searchAuthor = s; }
	public void setSearchCategoryId(String s) { this._searchCategory = s; }
	public void setSearchPublishedFrom(Date d) { this._searchPublishedFrom = d; }
	public void setSearchPublishedTo(Date d) { this._searchPublishedTo = d; }
	
	public ActionListener getArticleLinkListener() { return this._articleLinkListener; }
	public void setArticleLinkListener(ActionListener l) { this._articleLinkListener = l; }
	
	/**
	 * Returns all categories available for articles.
	 */
	public Map getCategories() {
		if (this._allCategories == null) {
			Collection cats = CategoryBean.getInstance().getCategories();
			if(cats!=null && !cats.isEmpty()){
				this._allCategories = new LinkedHashMap();
				this._allCategories.put(ArticleUtil.getBundle().getLocalizedText("All categories"), "-1");
				
				Iterator cat = cats.iterator();
				while (cat.hasNext()) {
					String category = (String) cat.next();
					
					this._allCategories.put(ArticleUtil.getBundle().getLocalizedText(category),category);
					
				}
			}	
		}
		
		if(this._allCategories==null){
			return new LinkedHashMap();
		}
		return this._allCategories;
	}
	
	/**
	 * Generates a search result from the current bean search values. 
	 */
	public void search() {
		this.searching = true;
		this.showResults = true;
		updateDataModel(new Integer(0),new Integer(0));
		this.searching = false;
	}

	/* (non-Javadoc)
	 * @see com.idega.webface.bean.AbstractWFEditableListManagedBean#getData()
	 */
	public WFEditableListDataBean[] getData() {
		List beans = getContentItems();
		if(beans!=null){
			return (ArticleSearchResultBean[])beans.toArray(new ArticleSearchResultBean[beans.size()]);
		}
		return new ArticleSearchResultBean[0];
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
		
		namePreviewLink.setId("columnIndex"+columnIndex+"_preview");
		namePreviewLink.setValueBinding("value", WFUtil.createValueBinding("#{"+ var + ".previewIWActionURI}"));
		namePreviewLink.setStyleClass("wf_listtext");

		HtmlOutputText textOnLink = new HtmlOutputText();	
		textOnLink.setId("columnIndex"+columnIndex+"_text");
		WFUtil.setValueBindingToArray(textOnLink,"value",var+".values",columnIndex);
		namePreviewLink.getChildren().add(textOnLink);
		
		return namePreviewLink;
	}

	/* (non-Javadoc)
	 * @see com.idega.webface.bean.AbstractWFEditableListManagedBean#getHeader(int)
	 */
	public UIComponent getHeader(int columnIndex) {
		return ArticleUtil.getBundle().getLocalizedText(this.localizationKey[columnIndex]);
	}
	
	
	/* (non-Javadoc)
	 * @see com.idega.content.bean.ContentListViewerManagedBean#getContentItems()
	 */
	public List getContentItems() {
		if(this.searching){
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
			String scope = ArticleUtil.getArticleBaseFolderPath();
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
		
		
		//TODO create search input for language
		SearchExpression namePatternExpression = s.compare(CompareOperator.LIKE, IWSlideConstants.PROPERTY_DISPLAY_NAME,"%.article");
		//todo search by the content type
		whereExpression = namePatternExpression;
		
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
		
		if(!("-1").equals(getSearchCategory())){
			SearchExpression categoryExpression = s.compare(CompareOperator.LIKE,IWSlideConstants.PROPERTY_CATEGORY,","+getSearchCategory()+",");
			whereExpression = s.and(whereExpression,categoryExpression);
		}
		
		String author = getSearchAuthor();
		if(author!=null && !"".equals(author)){
			SearchExpression authorExpression = s.compare(CompareOperator.LIKE,IWSlideConstants.PROPERTY_CREATOR_DISPLAY_NAME,"%"+author+"%");
			whereExpression = s.and(whereExpression,authorExpression);
		}
		
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
		return this.showResults;
	}
}
