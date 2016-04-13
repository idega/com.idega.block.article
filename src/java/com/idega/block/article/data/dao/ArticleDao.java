package com.idega.block.article.data.dao;

import java.util.List;

import com.idega.block.article.bean.article_view.EditArticlesListDataBean;
import com.idega.block.article.data.ArticleEntity;
import com.idega.presentation.IWContext;

/**
 * Class description goes here.
 * You can report about problems to: <a href="mailto:martynas@idega.com">Martynas Stakė</a>
 * You can expect to find some test cases notice in the end of the file.
 *
 *
 * @version 1.0.0 2011.08.24
 * @author martynas
 */
public interface ArticleDao extends ArticleDaoTemplate<ArticleEntity> {

	public static final String BEAN_NAME = "articleDAO";
	
	public ArticleEntity getById(Long id);
	
	public List<ArticleEntity> getAll(int maxResult,int startFrom);
	
	public int countArticles();
	
	public List<EditArticlesListDataBean> getAllEditArticlesListDataBeans(int maxResult,int startFrom,IWContext iwc);
	
	public boolean deleteArticle(Long id);

}