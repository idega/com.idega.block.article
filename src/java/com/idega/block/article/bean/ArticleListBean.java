/*
 * $Id: ArticleListBean.java,v 1.14 2005/02/03 11:30:54 joakim Exp $
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
import java.util.List;
import javax.faces.component.UIColumn;
import javax.faces.component.html.HtmlCommandLink;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.event.ActionListener;
import javax.faces.model.DataModel;
import org.apache.webdav.lib.WebdavResource;
import org.apache.xmlbeans.XmlException;
import com.idega.business.IBOLookup;
import com.idega.content.business.ContentUtil;
import com.idega.idegaweb.IWUserContext;
import com.idega.presentation.IWContext;
import com.idega.slide.business.IWSlideSession;
import com.idega.webface.WFPage;
import com.idega.webface.WFUtil;
import com.idega.webface.bean.WFListBean;
import com.idega.webface.model.WFDataModel;

/**
 * Bean for article list rows.   
 * <p>
 * Last modified: $Date: 2005/02/03 11:30:54 $ by $Author: joakim $
 *
 * @author Anders Lindman
 * @version $Revision: 1.14 $
 */

public class ArticleListBean implements WFListBean, Serializable {
	
	public final static String ARTICLE_ID = "article_id";
	
	private WFDataModel _dataModel = null;
	private ActionListener _articleLinkListener = null;

	private String _id = null;
	private String _headline = null;
	private String _published = null;
	private String _author = null;
	private String _status = null;
	private String _testStyle = null;

	private String[] testColumnHeaders = { 
			WFPage.CONTENT_BUNDLE + ".headline", 
			WFPage.CONTENT_BUNDLE + ".published",
			WFPage.CONTENT_BUNDLE + ".author",
			WFPage.CONTENT_BUNDLE + ".status" 
	};				

	/**
	 * Default constructor.
	 */
	public ArticleListBean() {}

	/**
	 * Constructs a new article list bean with the specified article link listener.
	 */
	public ArticleListBean(ActionListener l) {
		setArticleLinkListener(l);
	}
	
	/**
	 * Constructs a new article list bean with the specified parameters. 
	 */
	public ArticleListBean(String id, String headline, String published, String author, String status) {
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

	public void setId(String s) { _id = s; }
	public void setHeadline(String s) { _headline = s; }
	public void setPublished(String s) { _published = s; }
	public void setAuthor(String s) { _author = s; }
	public void setStatus(String s) { _status = s; }
	public void setTestStyle(String s) { _testStyle = s; }
	
	public ActionListener getArticleLinkListener() { return _articleLinkListener; }
	public void setArticleLinkListener(ActionListener l) { _articleLinkListener = l; }
	
	/**
	 * @see com.idega.webface.bean.WFListBean#updateDataModel() 
	 */
	public void updateDataModel(Integer start, Integer rows) {
		if (_dataModel == null) {
			_dataModel = new WFDataModel();
		}
		int availableRows = 0;

		ArticleItemBean[] articleItemBean;
		try {
			articleItemBean = (ArticleItemBean[])loadAllArticlesInFolder(ContentUtil.ARTICLE_PATH).toArray(new ArticleItemBean[0]);
			availableRows = articleItemBean.length;
			
			int nrOfRows = rows.intValue();
			if (nrOfRows == 0) {
				nrOfRows = availableRows;
			}

			int maxRow = Math.min(start.intValue() + nrOfRows,availableRows);
			
			for (int i = start.intValue(); i < maxRow; i++) {
				ArticleListBean a = new ArticleListBean(articleItemBean[i].getFolderLocation()+"/"+articleItemBean[i].getHeadline()+ArticleItemBean.ARTICLE_SUFFIX, articleItemBean[i].getHeadline(), articleItemBean[i].getItemType(), articleItemBean[i].getAuthor(), articleItemBean[i].getStatus());
				_dataModel.set(a, i);
			}
		}
		catch (XmlException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		_dataModel.setRowCount(availableRows);
	}

	/**
	 * Loads all xml files in the given folder
	 * @param folder
	 * @return List containing ArticleItemBean
	 * @throws XmlException
	 * @throws IOException
	 */
	public static List loadAllArticlesInFolder(String folder) throws XmlException, IOException{
		List list = new ArrayList();
		
//		File[] articleFile = folder.listFiles();
		
		IWUserContext iwuc = IWContext.getInstance();
//		IWApplicationContext iwac = iwuc.getApplicationContext();
		
		IWSlideSession session = (IWSlideSession)IBOLookup.getSessionInstance(iwuc,IWSlideSession.class);
//		IWSlideService service = (IWSlideService)IBOLookup.getServiceInstance(iwac,IWSlideService.class);
		
		WebdavResource folderResource = session.getWebdavResource(folder);
		
		String[] file = folderResource.list();

		//TODO(JJ) need to only get the article files. Right now it gets all folders and other filetypes
		//This code will probably never be used, so not wasting any time on it.
		for(int i=0;i<file.length;i++){
			try {
				System.out.println("Attempting to load "+file[i].toString());
				ArticleItemBean article = new ArticleItemBean();
//				article.load(folder+"/"+file[i]);
				//TODO this is a patch since getWebdavResource(folder) seems to return the whole path now
				article.load(file[i].substring(12));
				list.add(article);
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		return list;
	}
	
	/**
	 * @see com.idega.webface.bean.WFListBean#createColumns() 
	 */
	public UIColumn[] createColumns(String var) {
		int cols = testColumnHeaders.length;
		UIColumn[] columns = new UIColumn[cols];

		for (int i = 0; i < cols; i++) {
			UIColumn c = new UIColumn();
			c.setHeader(WFUtil.getTextVB(testColumnHeaders[i]));
			columns[i] = c;
		}
		
		String styleAttr =  var + ".testStyle";
		HtmlCommandLink l = WFUtil.getListLinkVB(var + ".headline");
		l.setId(ARTICLE_ID);
		WFUtil.setValueBinding(l, "style", styleAttr);
		l.addActionListener(_articleLinkListener);
		WFUtil.addParameterVB(l, "id", var + ".id");
		columns[0].getChildren().add(l);
		HtmlOutputText t = WFUtil.getListTextVB(var + ".published");
		WFUtil.setValueBinding(t, "style", styleAttr);
		columns[1].getChildren().add(t);
		t = WFUtil.getListTextVB(var + ".author");
		WFUtil.setValueBinding(t, "style", styleAttr);
		columns[2].getChildren().add(t);
		t = WFUtil.getListTextVB(var + ".status");
		WFUtil.setValueBinding(t, "style", styleAttr);
		columns[3].getChildren().add(t);		
		
		return columns;
	}
	
	/**
	 * @see com.idega.webface.bean.WFListBean#getDataModel() 
	 */
	public DataModel getDataModel() {
		return _dataModel;
	}
	
	/**
	 * @see com.idega.webface.bean.WFListBean#setDataModel() 
	 */
	public void setDataModel(DataModel dataModel) {
		_dataModel = (WFDataModel) dataModel;
	}
}
