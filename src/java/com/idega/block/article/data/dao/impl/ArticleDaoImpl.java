package com.idega.block.article.data.dao.impl;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import javax.faces.component.UIComponent;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.idega.block.article.bean.ArticleItemBean;
import com.idega.block.article.bean.article_view.EditArticlesListDataBean;
import com.idega.block.article.component.article_view.ArticleEdit;
import com.idega.block.article.data.ArticleEntity;
import com.idega.block.article.data.dao.ArticleDao;
import com.idega.builder.bean.AdvancedProperty;
import com.idega.content.presentation.ContentViewer;
import com.idega.core.builder.business.BuilderService;
import com.idega.core.builder.business.BuilderServiceFactory;
import com.idega.core.persistence.Query;
import com.idega.presentation.IWContext;


/**
 * Class for speeding up Articles searching
 * @author martynas
 * Last changed: 2011.05.12
 * You can report about problems to: martynas@idega.com
 * AIM: lapiukshtiss
 * Skype: lapiukshtiss
 * You can expect to find some test cases notice in the end of the file.
 */

@Repository(ArticleDao.BEAN_NAME)
@Scope(BeanDefinition.SCOPE_SINGLETON)
@Transactional(readOnly = false)
public class ArticleDaoImpl extends ArticleDaoTemplateImpl<ArticleEntity> implements ArticleDao {

	@Override
	protected Class<ArticleEntity> getEntityClass() {
		return ArticleEntity.class;
	}
	
	@Override
	public List<ArticleEntity> getAll(int maxResult,int startFrom){
		Query query = getQueryNamed(ArticleEntity.QUERY_GET_ALL);
		if(maxResult > 0){
			query.setMaxResults(maxResult);
		}
		if(startFrom > 0){
			query.setFirstResult(startFrom);
		}
		List<ArticleEntity> articles = query.getResultList(ArticleEntity.class);
		if(articles == null){
			return Collections.emptyList();
		}
		return articles;
	}
	
	@Override
	public int countArticles(){
		Query query = getQueryNamed(ArticleEntity.QUERY_COUNT_ALL);
		Long amount = query.getSingleResult(Long.class);
		return amount.intValue();
	}
	@Override
	public List<EditArticlesListDataBean> getAllEditArticlesListDataBeans(int maxResult,int startFrom,IWContext iwc){
		List<ArticleEntity> articleEntities = getAll(maxResult, startFrom);
		List<ArticleItemBean> articleBeans = new ArrayList<ArticleItemBean>(articleEntities.size());
		for(ArticleEntity a : articleEntities){
			ArticleItemBean article = new ArticleItemBean();
			String uri = a.getUri();
			article.setResourcePath(uri);
			try {
				article.load();
			} catch (Exception e) {
				getLogger().log(Level.WARNING, "Failed loading article by uri " + uri, e);
			}
			article.setArticleEntity(a);
			articleBeans.add(article);
		}
		List<EditArticlesListDataBean> beans = new ArrayList<EditArticlesListDataBean>(articleEntities.size());
		BuilderService service;
		try {
			service = BuilderServiceFactory.getBuilderService(iwc);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
		for(ArticleItemBean item : articleBeans){
			EditArticlesListDataBean bean = toEditArticlesListDataBean(item, new EditArticlesListDataBean(),service);
			beans.add(bean);
		}
		return beans;
	}
//	private ArticleListManagedBean getArticleListManagedBean(){
//		ArticleListManagedBean articleListManagedBean = new ArticleListManagedBean();
//		articleListManagedBean.setShowAllItems(true);
//		return articleListManagedBean;
//	}
	
	private EditArticlesListDataBean toEditArticlesListDataBean(ArticleItemBean data,EditArticlesListDataBean bean,BuilderService service){
		
		ArticleEntity articleEntity = data.getArticleEntity(false);
		if(articleEntity != null){
			bean.setId(articleEntity.getId());
		}
		bean.setTitle(data.getHeadline());
		
		String uri = data.getResourcePath();
		bean.setUriToArticle(uri);
		List<AdvancedProperty> parameters = new ArrayList<AdvancedProperty>();
		parameters.add(new AdvancedProperty(ContentViewer.PARAMETER_CONTENT_RESOURCE, uri));

		Class<? extends UIComponent> articleEditorClass = ArticleEdit.class;
		String editUri = service.getUriToObject(articleEditorClass, parameters);
		bean.setEditUri(editUri);
		return bean;
	}
	
	@Override
	public boolean deleteArticle(Long id){
		try{
			ArticleEntity data = find(ArticleEntity.class, id);
			ArticleItemBean bean = new ArticleItemBean();
			bean.setResourcePath(data.getUri());
			bean.load();
			bean.delete();
			remove(data);
		}catch (Exception e) {
			return false;
		}
		return true;
	}

}