package com.idega.block.article.component.article_view;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import org.springframework.beans.factory.annotation.Autowired;

import com.idega.block.article.bean.article_view.EditArticlesListDataBean;
import com.idega.block.article.business.ArticleConstants;
import com.idega.block.article.business.EditArticlesListBean;
import com.idega.block.web2.business.JQuery;
import com.idega.block.web2.business.Web2Business;
import com.idega.block.web2.business.Web2BusinessBean;
import com.idega.content.upload.presentation.UploadArea;
import com.idega.facelets.ui.FaceletComponent;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWMainApplication;
import com.idega.presentation.IWBaseComponent;
import com.idega.presentation.IWContext;
import com.idega.util.ListUtil;
import com.idega.util.PresentationUtil;
import com.idega.util.expression.ELUtil;
import com.idega.webface.WFUtil;

public class EditArticlesList extends IWBaseComponent{
	
	@Autowired
	private EditArticlesListBean editArticlesListBean;
	
	private Integer maxResult;
	private Integer startPosition;
	private String afterShow;

	@Override
	public void encodeBegin(FacesContext context) throws IOException {
		if(editArticlesListBean == null){
			ELUtil.getInstance().autowire(this);
		}
		editArticlesListBean.setComponentId(getId());
		editArticlesListBean.setMaxResult(getMaxResult());
		editArticlesListBean.setStartPosition(getStartPosition());
		
		editArticlesListBean.setTotalCount(editArticlesListBean.getArticlesCount());
		
		editArticlesListBean.setArticles(getArticles());
		editArticlesListBean.setAfterShow(getAfterShow());
		editArticlesListBean.setAddsPerPage(getAddsPerpage());
		super.encodeBegin(context);
	}
	
	@Override
	protected void initializeComponent(FacesContext context) {
		super.initializeComponent(context);
		
		IWContext iwc = IWContext.getIWContext(context);
		IWMainApplication iwma = iwc.getApplicationContext().getIWMainApplication();
		IWBundle iwb = iwma.getBundle(ArticleConstants.IW_BUNDLE_IDENTIFIER);
		
		addFiles(iwc);
		
		UIComponent facelet = getIWMainApplication(iwc)
				.createComponent(FaceletComponent.COMPONENT_TYPE);		
		if (facelet instanceof FaceletComponent) {
			((FaceletComponent) facelet).setFaceletURI(iwb.getFaceletURI("article-view/article-list.xhtml"));
		}

		add(facelet);
		
	}
	
	@SuppressWarnings("unchecked")
	public List<EditArticlesListDataBean> getArticles() {
		List<EditArticlesListDataBean> articles = editArticlesListBean.searchArticles(getMaxResult(), getStartPosition());
		if(!ListUtil.isEmpty(articles)){
			return articles;
		}
		articles = (List<EditArticlesListDataBean>) getValue("articles");
		if(articles == null){
			return Collections.emptyList();
		}
		return articles;
	}
	
	public Integer getMaxResult() {
		if(maxResult != null){
			return maxResult;
		}
		maxResult = (Integer) getValue("maxResult");
		if(maxResult == null){
			maxResult = 2;
		}
		return maxResult;
	}

	public void setMaxResult(Integer maxResult) {
		this.maxResult = maxResult;
	}

	public Integer getStartPosition() {
		if(startPosition != null){
			return startPosition;
		}
		startPosition = (Integer) getValue("startPosition");
		if(startPosition == null){
			startPosition = 0;
		}
		return startPosition;
	}

	public void setStartPosition(Integer startPosition) {
		this.startPosition = startPosition;
	}

	
	private Object getValue(String attribute){
		ValueExpression valueExpression = getValueExpression(attribute);
		if(valueExpression == null){
			return null;
		}
		Object value = valueExpression.getValue(getFacesContext().getELContext());
		return value;
	}
	
	protected void addFiles(IWContext iwc){
		
		
		IWMainApplication iwma = iwc.getApplicationContext().getIWMainApplication();
		IWBundle iwb = iwma.getBundle(ArticleConstants.IW_BUNDLE_IDENTIFIER);
		
		// Style
//		List<String> styles = new ArrayList<String>();
//		
//		PresentationUtil.addStyleSheetsToHeader(iwc, styles);
	
		// Script
		List<String> scripts = new ArrayList<String>();
		
		Web2Business web2 = WFUtil.getBeanInstance(iwc, Web2Business.SPRING_BEAN_IDENTIFIER);
		try{
				JQuery  jQuery = web2.getJQuery();
				scripts.add(jQuery.getBundleURIToJQueryUILib(Web2BusinessBean.JQUERY_UI_LATEST_VERSION,"jquery-ui.custom.min.js"));
				scripts.addAll(web2.getBundleUrisToBlueimpFileUploadBasicScriptFiles());
		}
		catch (Exception e) {
			Logger.getLogger(UploadArea.class.getName()).log(Level.WARNING, "Failed adding scripts no jQuery and it's plugins files were added");
		}
		
		scripts.add(iwb.getVirtualPathWithFileNameString("javascript/article-view/edit_article_list.js"));
		scripts.add("/dwr/interface/ArticleServices.js");
		
		PresentationUtil.addJavaScriptSourcesLinesToHeader(iwc, scripts);
	}

	public String getAfterShow() {
		return afterShow;
	}

	public void setAfterShow(String afterShow) {
		this.afterShow = afterShow;
	}
	
	public List<Integer> getAddsPerpage() {
		ArrayList<Integer> list = new ArrayList<Integer>();
		int max = getMaxResult();
		int prev = 0;
		for(int i = 0; i < 4; i++){
			int count = (i+1)*5;
			if((max > prev) && (max < count)){
				list.add(max);
			}
			list.add(count);
			prev = count;
		}
		return list;
	}
	
}