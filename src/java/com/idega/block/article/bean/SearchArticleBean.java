/*
 * $Id: SearchArticleBean.java,v 1.21 2007/09/24 15:03:44 valdas Exp $
 *
 * Copyright (C) 2004 Idega. All Rights Reserved.
 *
 * This software is the proprietary information of Idega. Use is subject to
 * license terms.
 *
 */
package com.idega.block.article.bean;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlOutputLink;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.event.ActionListener;
import javax.jcr.RepositoryException;
import javax.jcr.query.QueryResult;
import javax.mail.search.SearchException;

import org.apache.xmlbeans.XmlException;

import com.idega.block.article.business.ArticleUtil;
import com.idega.content.bean.ContentItemBeanComparator;
import com.idega.content.business.ContentSearch;
import com.idega.content.business.categories.CategoryBean;
import com.idega.content.data.ContentCategory;
import com.idega.presentation.IWContext;
import com.idega.webface.WFUtil;
import com.idega.webface.bean.AbstractWFEditableListManagedBean;
import com.idega.webface.bean.WFEditableListCellWrapper;
import com.idega.webface.bean.WFEditableListDataBean;
import com.idega.webface.bean.WFListBean;

/**
 * Bean for searching articles.
 * <p>
 * Last modified: $Date: 2007/09/24 15:03:44 $ by $Author: valdas $
 *
 * @author Anders Lindman
 * @version $Revision: 1.21 $
 */
public class SearchArticleBean extends AbstractWFEditableListManagedBean implements WFListBean, Serializable {

	/**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 3257288011107481141L;

	public final static String ARTICLE_ID = "article_id";

	protected String[] localizationKey = new String[] { "Headline", "Author", "Source", "Creation date", "Language",
			"Status" };

	private ActionListener _articleLinkListener = null;

	private String _searchText = null;

	private String _searchAuthor = null;

	private String _searchCategory = null;

	private Date _searchPublishedFrom = null;

	private Date _searchPublishedTo = null;

	private Map<String, String> _allCategories = null;

	private boolean searching = false;

	private boolean showResults = false;

	/**
	 * Default constructor.
	 */
	public SearchArticleBean() {
		// No action...
	}

	/**
	 * Constructs a new search article bean with the specified article link
	 * listener.
	 */
	public SearchArticleBean(ActionListener l) {
		this();
		setArticleLinkListener(l);
	}

	public String getSearchText() {
		return this._searchText;
	}

	public String getSearchAuthor() {
		return this._searchAuthor;
	}

	public String getSearchCategory() {
		return this._searchCategory;
	}

	public Date getSearchPublishedFrom() {
		return this._searchPublishedFrom;
	}

	public Date getSearchPublishedTo() {
		return this._searchPublishedTo;
	}

	public void setSearchText(String s) {
		this._searchText = s;
	}

	public void setSearchAuthor(String s) {
		this._searchAuthor = s;
	}

	public void setSearchCategoryId(String s) {
		this._searchCategory = s;
	}

	public void setSearchPublishedFrom(Date d) {
		this._searchPublishedFrom = d;
	}

	public void setSearchPublishedTo(Date d) {
		this._searchPublishedTo = d;
	}

	public ActionListener getArticleLinkListener() {
		return this._articleLinkListener;
	}

	public void setArticleLinkListener(ActionListener l) {
		this._articleLinkListener = l;
	}

	/**
	 * Returns all categories available for articles.
	 */
	public Map<String, String> getCategories() {
		if (this._allCategories == null) {
			final CategoryBean catBean = CategoryBean.getInstance();
			Collection<ContentCategory> cats = catBean.getCategories();
			if (cats != null && !cats.isEmpty()) {
				this._allCategories = new LinkedHashMap<String, String>();
				this._allCategories.put("-1", ArticleUtil.getBundle().getLocalizedString("all_categories", "All categories"));
				for (ContentCategory cat : cats) {
					String category = catBean.getCategoryName(cat.getId());
					this._allCategories.put(cat.getId(), category);
				}
			}
		}
		if (this._allCategories == null) {
			return new LinkedHashMap<String, String>();
		}
		return this._allCategories;
	}

	/**
	 * Generates a search result from the current bean search values.
	 */
	public void search() {
		this.searching = true;
		this.showResults = true;
		updateDataModel(new Integer(0), new Integer(0));
		this.searching = false;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.idega.webface.bean.AbstractWFEditableListManagedBean#getData()
	 */
	@Override
	public WFEditableListDataBean[] getData() {
		List beans = getContentItems();
		if (beans != null) {
			return (ArticleSearchResultBean[]) beans.toArray(new ArticleSearchResultBean[beans.size()]);
		}
		return new ArticleSearchResultBean[0];
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.idega.webface.bean.AbstractWFEditableListManagedBean#getNumberOfColumns()
	 */
	@Override
	public int getNumberOfColumns() {
		return 6;
	}

	@Override
	public UIComponent createCellWrapper(String var, int columnIndex) {
		// Overridden because of value binding
		WFEditableListCellWrapper component = constructWFEditableListCellWrapper(var, columnIndex);
		WFUtil.setValueBinding(component, "rendered", var + ".rendered");
		return component;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.idega.webface.bean.AbstractWFEditableListManagedBean#getUIComponent(java.lang.String,
	 *      int)
	 */
	@Override
	public UIComponent getUIComponent(String var, int columnIndex) {
		HtmlOutputLink namePreviewLink = new HtmlOutputLink();
		namePreviewLink.setId("columnIndex" + columnIndex + "_preview");
		namePreviewLink.setValueBinding("value", WFUtil.createValueBinding("#{" + var + ".previewIWActionURI}"));
		namePreviewLink.setStyleClass("wf_listtext");
		HtmlOutputText textOnLink = new HtmlOutputText();
		textOnLink.setId("columnIndex" + columnIndex + "_text");
		WFUtil.setValueBindingToArray(textOnLink, "value", var + ".values", columnIndex);
		namePreviewLink.getChildren().add(textOnLink);
		return namePreviewLink;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.idega.webface.bean.AbstractWFEditableListManagedBean#getHeader(int)
	 */
	@Override
	public UIComponent getHeader(int columnIndex) {
		return ArticleUtil.getBundle().getLocalizedText(this.localizationKey[columnIndex]);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.idega.content.bean.ContentListViewerManagedBean#getContentItems()
	 */
	public List getContentItems() {
		if (this.searching) {
			try {
				List l = listArticles();
				ContentItemBeanComparator c = new ContentItemBeanComparator();
				c.setReverseOrder(true);
				Collections.sort(l, c);
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

	public List<ArticleItemBean> listArticles() throws XmlException, IOException {
		List<ArticleItemBean> list = new ArrayList<ArticleItemBean>();
		IWContext iwc = IWContext.getInstance();
//		try {
			String scope = ArticleUtil.getArticleBaseFolderPath();
			if (scope != null) {
//				if (scope.startsWith(session.getWebdavServerURI())) {
//					scope = scope.substring(session.getWebdavServerURI().length());
//				}
//				if (scope.startsWith("/")) {
//					scope = scope.substring(1);
//				}
			}
			ContentSearch searchBusiness = new ContentSearch(iwc.getIWMainApplication());
//			Search search = searchBusiness.createSearch(getSearchRequest(scope, iwc.getCurrentLocale()));
//			Collection results = search.getSearchResults();
//			if (results != null) {
//				for (Iterator iter = results.iterator(); iter.hasNext();) {
//					SearchResult result = (SearchResult) iter.next();
//					try {
//						System.out.println("Attempting to load " + result.getSearchResultURI());
//						ArticleItemBean article = new ArticleSearchResultBean();
//						article.load(result.getSearchResultURI());
//						list.add(article);
//					}
//					catch (Exception e) {
//						e.printStackTrace();
//					}
//				}
//			}
//		}
//		catch (SearchException e1) {
//			e1.printStackTrace();
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
	public QueryResult getSearchRequest(String scope, Locale locale) throws RepositoryException {
//		SearchRequest s = new SearchRequest();
//		s.addSelection(IWSlideConstants.PROPERTY_CREATION_DATE);
//		s.addSelection(IWSlideConstants.PROPERTY_CATEGORY);
//		s.addScope(new SearchScope(scope));
//		SearchExpression whereExpression = null;
//		// TODO create search input for language
//		SearchExpression namePatternExpression = s.compare(CompareOperator.LIKE,
//				IWSlideConstants.PROPERTY_DISPLAY_NAME, "%.article");
//		// todo search by the content type
//		whereExpression = namePatternExpression;
//		SearchExpression creationDateFromExpression = null;
//		if (getSearchPublishedFrom() != null) {
//			Date from = getSearchPublishedFrom();
//			IWTimestamp stamp = new IWTimestamp(from);
//			// the date's time is at 24:00 so anything from that day will not be
//			// found. So be back up a day to 24:00
//			stamp.addDays(-1);
//			from = stamp.getDate();
//			creationDateFromExpression = s.compare(CompareOperator.GTE, IWSlideConstants.PROPERTY_CREATION_DATE, from);
//			whereExpression = s.and(whereExpression, creationDateFromExpression);
//		}
//		SearchExpression creationDateToExpression = null;
//		if (getSearchPublishedTo() != null) {
//			creationDateToExpression = s.compare(CompareOperator.LTE, IWSlideConstants.PROPERTY_CREATION_DATE,
//					getSearchPublishedTo());
//			whereExpression = s.and(whereExpression, creationDateToExpression);
//		}
//		if (!("-1").equals(getSearchCategory())) {
//			SearchExpression categoryExpression = s.compare(CompareOperator.LIKE, IWSlideConstants.PROPERTY_CATEGORY,
//					"," + getSearchCategory() + ",");
//			whereExpression = s.and(whereExpression, categoryExpression);
//		}
//		String author = getSearchAuthor();
//		if (author != null && !"".equals(author)) {
//			SearchExpression authorExpression = s.compare(CompareOperator.LIKE,
//					IWSlideConstants.PROPERTY_CREATOR_DISPLAY_NAME, "%" + author + "%");
//			whereExpression = s.and(whereExpression, authorExpression);
//		}
//		SearchExpression containsExpression = null;
//		if (getSearchText() != null && !"".equals(getSearchText())) {
//			containsExpression = s.contains(getSearchText());
//			if (whereExpression != null) {
//				whereExpression = s.and(whereExpression, containsExpression);
//			}
//			else {
//				whereExpression = containsExpression;
//			}
//		}
//		s.setWhereExpression(whereExpression);
//		System.out.println("------------------------");
//		System.out.println(s.asString());
//		System.out.println("------------------------");
//		return s;

		//	TODO
		return null;
	}

	public boolean getShowSearchResults() {
		return this.showResults;
	}
}