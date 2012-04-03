package com.idega.block.article.bean.article_view;

import java.io.IOException;

import org.directwebremoting.annotations.Param;
import org.directwebremoting.annotations.RemoteProxy;
import org.directwebremoting.spring.SpringCreator;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.idega.block.article.bean.ArticleItemBean;


@Service(ArticleServices.SERVICE)
@Scope(BeanDefinition.SCOPE_SINGLETON)
@RemoteProxy(creator=SpringCreator.class, creatorParams={
	@Param(name="beanName", value=ArticleServices.SERVICE),
	@Param(name="javascript", value=ArticleServices.DWR_SERVICE)
}, name=ArticleServices.DWR_SERVICE)
public class ArticleServices {
	public static final String SERVICE = "articleServices";
	public static final String DWR_SERVICE = "ArticleServices";
	
	public ArticleItemBean getArticleItemBean(String uri) throws IOException{
		ArticleItemBean article = new ArticleItemBean();
		article.setResourcePath(uri);
		article.load();
		return article;
	}

}
