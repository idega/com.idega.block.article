/*
 * $Id: ArticleListManagedBean.java,v 1.1 2005/02/07 10:59:53 gummi Exp $
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
import java.util.List;
import org.apache.webdav.lib.WebdavResource;
import org.apache.xmlbeans.XmlException;
import com.idega.block.article.bean.ArticleItemBean;
import com.idega.business.IBOLookup;
import com.idega.content.bean.ContentListViewerManagedBean;
import com.idega.content.business.ContentUtil;
import com.idega.content.presentation.ContentItemViewer;
import com.idega.idegaweb.IWUserContext;
import com.idega.presentation.IWContext;
import com.idega.slide.business.IWSlideSession;



/**
 * 
 *  Last modified: $Date: 2005/02/07 10:59:53 $ by $Author: gummi $
 * 
 * @author <a href="mailto:gummi@idega.com">Gudmundur Agust Saemundsson</a>
 * @version $Revision: 1.1 $
 */
public class ArticleListManagedBean implements ContentListViewerManagedBean {

	private String resourcePath=null;
	
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
			return loadAllArticlesInFolder(ContentUtil.ARTICLE_PATH);
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
		if(file!=null){
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
		}
		
		return list;
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
