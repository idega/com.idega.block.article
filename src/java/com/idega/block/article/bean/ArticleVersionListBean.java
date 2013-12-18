/*
 * $Id: ArticleVersionListBean.java,v 1.4 2007/05/30 15:03:03 gediminas Exp $
 *
 * Copyright (C) 2004 Idega. All Rights Reserved.
 *
 * This software is the proprietary information of Idega.
 * Use is subject to license terms.
 *
 */
package com.idega.block.article.bean;

import java.io.Serializable;
import java.util.Date;

import javax.faces.component.UIColumn;
import javax.faces.component.html.HtmlCommandLink;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.event.ActionListener;
import javax.faces.model.DataModel;

import com.idega.block.article.IWBundleStarter;
import com.idega.webface.WFUtil;
import com.idega.webface.bean.WFListBean;
import com.idega.webface.model.WFDataModel;

/**
 * Bean for article version list rows.
 * <p>
 * Last modified: $Date: 2007/05/30 15:03:03 $ by $Author: gediminas $
 *
 * @author Anders Lindman
 * @version $Revision: 1.4 $
 */

public class ArticleVersionListBean implements WFListBean, Serializable {

	private static final long serialVersionUID = -4566212763951268183L;

	public final static String ARTICLE_VERSION_ID = "article_version_id";

	private WFDataModel _dataModel = null;
	private ActionListener _articleLinkListener = null;

	private int _versionId = 0;
	private String _revision = null;
	private Date _created = null;
	private String _comment = null;
	private String _author = null;
	private String _publishedBy = null;

	private String[] testColumnHeaders = {
		"version",
		"created",
		"comment",
		"author",
		"published_by"
	};

	private String[] testRevisions = {
		"1.4",
		"1.3",
		"1.2",
		"1.1",
		"1.0"
	};

	private Date[] testCreated = {
		new Date(),
		new Date(),
		new Date(),
		new Date(),
		new Date()
	};

	private String[] testComments = {
		"Modified link url",
		"Removed section about...",
		"Added icon. image",
		"Added links",
		"First version"
	};

	private String[] testAuthors = {
		"Anderson",
		"Anderson",
		"Anderson",
		"Anderson",
		"Anderson"
	};

	private String[] testPublishedBy = {
		"Sam",
		"Sam",
		"Sam",
		"Sam",
		"Sam"
	};

	/**
	 * Default constructor.
	 */
	public ArticleVersionListBean() {
		//No action...
	}

	/**
	 * Constructs a new article version list bean with the specified article link listener.
	 */
	public ArticleVersionListBean(ActionListener l) {
		setArticleLinkListener(l);
	}

	/**
	 * Constructs a new article version list bean with the specified parameters.
	 */
	public ArticleVersionListBean(int versionId, String revision, Date created, String comment, String author, String publishedBy) {
		this._versionId = versionId;
		this._revision = revision;
		this._created = created;
		this._comment = comment;
		this._author = author;
		this._publishedBy = publishedBy;
	}

	public int getVersionId() { return this._versionId; }
	public String getRevision() { return this._revision; }
	public Date getCreated() { return this._created; }
	public String getComment() { return this._comment; }
	public String getAuthor() { return this._author; }
	public String getPublishedBy() { return this._publishedBy; }

	public void setVersionId(int id) { this._versionId = id; }
	public void setRevision(String s) { this._revision = s; }
	public void setCreated(Date d) { this._created = d; }
	public void setComment(String s) { this._comment = s; }
	public void setAuthor(String s) { this._author = s; }
	public void setPublishedBy(String s) { this._publishedBy = s; }

	public ActionListener getArticleLinkListener() { return this._articleLinkListener; }
	public void setArticleLinkListener(ActionListener l) { this._articleLinkListener = l; }

	/**
	 * @see com.idega.webface.bean.WFListBean#updateDataModel()
	 */
	@Override
	public void updateDataModel(Integer start, Integer rows) {
		if (this._dataModel == null) {
			this._dataModel = new WFDataModel();
		}
		int availableRows = this.testRevisions.length;
		int nrOfRows = rows.intValue();
		if (nrOfRows == 0) {
			nrOfRows = availableRows;
		}
		int maxRow = start.intValue() + nrOfRows;
		if (maxRow > availableRows) {
			maxRow = availableRows;
		}
		for (int i = start.intValue(); i < maxRow; i++) {
			ArticleVersionListBean bean = new ArticleVersionListBean(i, this.testRevisions[i], this.testCreated[i], this.testComments[i], this.testAuthors[i], this.testPublishedBy[i]);
			this._dataModel.set(bean, i);
		}
		this._dataModel.setRowCount(availableRows);
	}

	/**
	 * @see com.idega.webface.bean.WFListBean#createColumns()
	 */
	@Override
	public UIColumn[] createColumns(String var) {
		int cols = this.testColumnHeaders.length;
		UIColumn[] columns = new UIColumn[cols];

		for (int i = 0; i < cols; i++) {
			UIColumn c = new UIColumn();
			c.setHeader(WFUtil.getTextVB(IWBundleStarter.BUNDLE_IDENTIFIER, testColumnHeaders[i]));
			columns[i] = c;
		}

		HtmlCommandLink l = WFUtil.getListLinkVB(var + ".revision");
		l.setId(ARTICLE_VERSION_ID);
		l.addActionListener(this._articleLinkListener);
		WFUtil.addParameterVB(l, "id", var + ".versionId");
		columns[0].getChildren().add(l);
		HtmlOutputText t = WFUtil.getListTextVB(var + ".created");
		columns[1].getChildren().add(t);
		t = WFUtil.getListTextVB(var + ".comment");
		columns[2].getChildren().add(t);
		t = WFUtil.getListTextVB(var + ".author");
		columns[3].getChildren().add(t);
		t = WFUtil.getListTextVB(var + ".publishedBy");
		columns[4].getChildren().add(t);

		return columns;
	}

	/**
	 * @see com.idega.webface.bean.WFListBean#getDataModel()
	 */
	@Override
	public DataModel getDataModel() {
		return this._dataModel;
	}

	/**
	 * @see com.idega.webface.bean.WFListBean#setDataModel()
	 */
	@Override
	public void setDataModel(DataModel dataModel) {
		this._dataModel = (WFDataModel) dataModel;
	}
}
