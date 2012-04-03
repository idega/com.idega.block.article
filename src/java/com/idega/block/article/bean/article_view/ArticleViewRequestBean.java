package com.idega.block.article.bean.article_view;

import java.io.IOException;

import com.idega.idegaweb.IWResourceBundle;

import com.idega.util.StringUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.idega.block.article.bean.ArticleItemBean;
import com.idega.block.article.business.ArticleConstants;
import com.idega.block.article.data.dao.ArticleDao;
import com.idega.core.business.DefaultSpringBean;
import com.idega.presentation.IWContext;
import com.idega.util.CoreUtil;


@Service(ArticleViewRequestBean.SERVICE)
@Scope("request")
public class ArticleViewRequestBean  extends DefaultSpringBean{
	public static final String SERVICE = "articleViewRequestBean";
	
	// Parameters
	// Action
	public static final String PARAMETER_ACTION = SERVICE + "_action";
	public static final String VALUE_DELETE = "delete";
	
	// Article id
	public static final String ARTICLE_URI = SERVICE + "_article-id";
	
	
	private ArticleItemBean articleItemBean = null;
	
	@Autowired
	ArticleDao articleDAO;
	
	@Autowired
	ArticleServices articleServices;
	
	
	
	//request scope, iwc can be saved
	private IWContext iwc = null;
	private IWResourceBundle iwrb = null;
	
	
	public ArticleViewRequestBean(){
		iwc = CoreUtil.getIWContext();
		iwrb = getBundle(ArticleConstants.IW_BUNDLE_IDENTIFIER).getResourceBundle(iwc);
	}
	
	public boolean isAllowedToPerformAction(){
		String action = iwc.getParameter(PARAMETER_ACTION);
		return isAllowedToPerformAction(action);
	}
	
	public boolean isAllowedToPerformAction(String action){
		if(!iwc.isLoggedOn()){
			return false;
		}
		ArticleItemBean articleItemBean;
		try {
			articleItemBean = getArticleItemBean();
			
		} catch (IOException e) {
			return false;
		}
		
		return articleItemBean.setAllowedToEditByCurrentUser(iwc.getCurrentUser());
	}
	
	public ArticleItemBean getArticleItemBean() throws IOException{
		if(articleItemBean != null){
			return articleItemBean;
		}
		String uri = iwc.getParameter(ARTICLE_URI);
		if(StringUtil.isEmpty(uri)){
			return null;
		}
		return articleServices.getArticleItemBean(uri);
		
	}
	
}
