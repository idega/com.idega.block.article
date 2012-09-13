/*
 * $Id: IWBundleStarter.java,v 1.27 2009/05/15 07:23:50 valdas Exp $
 * Created on 2.11.2004
 *
 * Copyright (C) 2004 Idega Software hf. All Rights Reserved.
 *
 * This software is the proprietary information of Idega hf.
 * Use is subject to license terms.
 */
package com.idega.block.article;

import java.rmi.RemoteException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.idega.block.article.business.ArticleActionURIHandler;
import com.idega.block.article.business.ArticleRSSProducer;
import com.idega.block.article.data.ArticleEntity;
import com.idega.block.article.data.dao.ArticleDao;
import com.idega.block.rss.business.RSSProducerRegistry;
import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.content.view.ContentViewManager;
import com.idega.core.uri.IWActionURIManager;
import com.idega.core.view.DefaultViewNode;
import com.idega.core.view.KeyboardShortcut;
import com.idega.core.view.ViewNode;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWBundleStartable;
import com.idega.slide.business.IWSlideService;
import com.idega.util.ListUtil;
import com.idega.util.expression.ELUtil;


/**
 *
 *  Last modified: $Date: 2009/05/15 07:23:50 $ by $Author: valdas $
 *
 * @author <a href="mailto:tryggvil@idega.com">Tryggvi Larusson</a>
 * @version $Revision: 1.27 $
 */
public class IWBundleStarter implements IWBundleStartable {

	public static final String BUNDLE_IDENTIFIER = "com.idega.block.article";

	@Autowired
	private ArticleDao articleDAO;

	private ArticleDao getArticleDao() {
		if (articleDAO == null)
			ELUtil.getInstance().autowire(this);
		return articleDAO;
	}

	@Override
	public void start(IWBundle starterBundle) {
		addArticleIWActionURIHandler();
		addArticleViews(starterBundle);
		addRSSProducers(starterBundle);

		doSetArticleClass();
	}

	private void doSetArticleClass() {
		try {
			List<ArticleEntity> articles = getArticleDao().getResultListByInlineQuery("from " + ArticleEntity.class.getName() + " a where a." +
				ArticleEntity.classPropertyName + " is null", ArticleEntity.class);
			if (ListUtil.isEmpty(articles))
				return;

			for (ArticleEntity article: articles) {
				article.setTheClass(ArticleEntity.class.getSimpleName());
				getArticleDao().merge(article);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void addArticleIWActionURIHandler() {
		IWActionURIManager manager = IWActionURIManager.getInstance();
		//so it is called before contenthandler
		manager.registerHandler(0, new ArticleActionURIHandler());

	}

	@Override
	public void stop(IWBundle starterBundle) {
		//No action...
	}

	public void addArticleViews(IWBundle bundle){
		ContentViewManager cViewManager = ContentViewManager.getInstance(bundle.getApplication());
		ViewNode contentNode = cViewManager.getContentNode();

		DefaultViewNode articleNode = new DefaultViewNode("article",contentNode);
		articleNode.setJspUri(bundle.getJSPURI("listarticles.jsp"));
		articleNode.setKeyboardShortcut(new KeyboardShortcut("a"));
		articleNode.setName("#{localizedStrings['com.idega.block.article']['article']}");

		DefaultViewNode createNewArticleNode = new DefaultViewNode("create",articleNode);
		String jspUri = bundle.getJSPURI("createarticle.jsp");
		createNewArticleNode.setJspUri(jspUri);
		createNewArticleNode.setVisibleInMenus(false);
		createNewArticleNode.setName("#{localizedStrings['com.idega.block.article']['create_article']}");

		DefaultViewNode editNewArticleNode = new DefaultViewNode("edit",articleNode);
		editNewArticleNode.setJspUri(bundle.getJSPURI("editarticle.jsp"));
		editNewArticleNode.setVisibleInMenus(false);
		editNewArticleNode.setName("#{localizedStrings['com.idega.block.article']['edit']}");

		DefaultViewNode deleteArticleNode = new DefaultViewNode("delete",articleNode);
		deleteArticleNode.setJspUri(bundle.getJSPURI("deletearticle.jsp"));
		deleteArticleNode.setVisibleInMenus(false);
		deleteArticleNode.setName("#{localizedStrings['com.idega.block.article']['delete']}");

		DefaultViewNode listArticlesNode = new DefaultViewNode("list",articleNode);
		listArticlesNode.setJspUri(bundle.getJSPURI("listarticles.jsp"));
		listArticlesNode.setVisibleInMenus(false);
		listArticlesNode.setName("#{localizedStrings['com.idega.block.article']['list_articles']}");


		DefaultViewNode previewArticlesNode = new DefaultViewNode("preview",articleNode);
		previewArticlesNode.setJspUri(bundle.getJSPURI("previewarticle.jsp"));
		previewArticlesNode.setVisibleInMenus(false);
		previewArticlesNode.setName("#{localizedStrings['com.idega.block.article']['preview']}");

		/*DefaultViewNode searchArticlesNode = new DefaultViewNode("search",articleNode);
		searchArticlesNode.setJspUri(bundle.getJSPURI("searcharticle.jsp"));
		searchArticlesNode.setName("#{localizedStrings['com.idega.block.article']['search_articles']}");
		//searchArticlesNode.setVisibleInMenus(false);
		*/
	}
	private void addRSSProducers(IWBundle starterBundle) {
		RSSProducerRegistry registry = RSSProducerRegistry.getInstance();

		//ContentRSSProducer, also a IWSlideChangeListener

		ArticleRSSProducer articleProducer = new ArticleRSSProducer();
		registry.addRSSProducer("article", articleProducer);

		 IWApplicationContext iwac = starterBundle.getApplication().getIWApplicationContext();
	        try {
	            IWSlideService service = IBOLookup.getServiceInstance(iwac,IWSlideService.class);
	            service.addIWSlideChangeListeners(articleProducer);
	        } catch (IBOLookupException e) {
	            e.printStackTrace();
	        } catch (RemoteException e) {
	            e.printStackTrace();
	        }
	}
}
