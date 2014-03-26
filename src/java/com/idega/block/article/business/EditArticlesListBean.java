package com.idega.block.article.business;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import org.apache.poi.ss.formula.eval.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.idega.block.article.bean.article_view.EditArticleListPage;
import com.idega.block.article.bean.article_view.EditArticlesListDataBean;
import com.idega.block.article.component.article_view.ArticleEdit;
import com.idega.block.article.data.dao.ArticleDao;
import com.idega.core.builder.business.BuilderService;
import com.idega.core.builder.business.BuilderServiceFactory;
import com.idega.core.business.DefaultSpringBean;
import com.idega.presentation.IWContext;
import com.idega.util.CoreUtil;
import com.idega.util.StringUtil;

@Service(EditArticlesListBean.BEAN_NAME)
@Scope("request")
public class EditArticlesListBean extends DefaultSpringBean {
	public static final String BEAN_NAME = "editArticlesListBean";

	private List<EditArticlesListDataBean> articles = null;
	
	private String  componentId;
	private Integer maxResult;
	private Integer startPosition;
	private Integer totalCount;
	private String  searchTerm;
	private String afterShow;
	private List<Integer> addsPerPage;
	
	private IWContext iwc;
	
	@Autowired
	private ArticleDao articleDao;
	

	public String getComponentId() {
		return componentId;
	}

	public void setComponentId(String componentId) {
		this.componentId = componentId;
	}

	public Integer getMaxResult() {
		return maxResult;
	}

	public void setMaxResult(Integer maxResult) {
		this.maxResult = maxResult;
	}

	public Integer getStartPosition() {
		return startPosition;
	}

	public void setStartPosition(Integer startPosition) {
		this.startPosition = startPosition;
	}

	public Integer getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(Integer totalCount) {
		this.totalCount = totalCount;
	}

	public String getSearchTerm() {
		return searchTerm;
	}

	public void setSearchTerm(String searchTerm) {
		this.searchTerm = searchTerm;
	}
	
	public boolean isFoundProducts(){
		Integer totalCount = getTotalCount(); 
		return (totalCount != null) && (totalCount > 0);
	}
	public List<EditArticleListPage> getPages(){
		if(!isFoundProducts()){
			return Collections.emptyList();
		}
		Integer totalCount = getTotalCount(); 
		int maxResult = getMaxResult();
		return getProductListPagesPages(totalCount, maxResult,getStartPosition());
	}
	
	
	public List<EditArticleListPage> getProductListPagesPages(int totalCount, int maxResult,int startPosition){
		int maxToShow = 20;
		int showWhenMany = 15;
		
		
		int pagenumber = totalCount / maxResult;
		if((totalCount % maxResult) > 0){
			pagenumber++;
		}
		List<EditArticleListPage> pages = new ArrayList<EditArticleListPage>(pagenumber);
		
		int startPos;
		int startPage;
		int lastPage;
		
		if(pagenumber > maxToShow){
			int pagesBack = showWhenMany / 2;
			startPage = (startPosition / maxResult) - pagesBack;
			startPos = startPosition - ((pagesBack+1) * maxResult);
			if(startPage < 1){
				startPage = 1;
			}
			if(startPos < 0){
				startPos = 0;
			}
			lastPage = startPage + showWhenMany;
			if(lastPage > pagenumber){
				int difference = lastPage - pagenumber;
				lastPage = pagenumber;
				startPage = startPage - difference;
				startPos = startPos - (difference * maxResult);
				if(startPage < 1){
					startPage = 1;
				}
				if(startPos < 0){
					startPos = 0;
				}
			}
			
		}else{
			startPage = 1;
			lastPage = pagenumber;
			startPos = 0;
		}
		for(int i = startPage;i <= lastPage;i++){
			EditArticleListPage page = new EditArticleListPage();
			pages.add(page);
			page.setNumber(i);
			page.setStart(startPos);
			startPos = startPos + maxResult;
		}
		return pages;
	}	

	
	public int getActivePageIndex(){
		if(!isFoundProducts()){
			return 0;
		}
		return getStartPosition() / getMaxResult();
	}
	
	public int getFirstResultNumber(){
		if(!isFoundProducts()){
			return 0;
		}
		return getStartPosition() + 1;
	}
	
	public int getLastResultNumber(){
		if(!isFoundProducts()){
			return 0;
		}
		return getStartPosition() + getArticles().size();
	}

	public String getAfterShow() {
		return afterShow;
	}

	public void setAfterShow(String afterShow) {
		this.afterShow = afterShow;
	}

	public List<Integer> getAddsPerPage() {
		if(addsPerPage == null){
			return Collections.emptyList();
		}
		return addsPerPage;
	}

	public void setAddsPerPage(List<Integer> addsPerpage) {
		this.addsPerPage = addsPerpage;
	}

	public List<EditArticlesListDataBean> getArticles() {
		return articles;
	}

	public void setArticles(List<EditArticlesListDataBean> articles) {
		this.articles = articles;
	}
	
	public List<EditArticlesListDataBean> searchArticles(int maxResult,int startPosition) {
		if(articles != null){
			return articles;
		}
		String searchTerm = getSearchTerm();
		if(!StringUtil.isEmpty(searchTerm)){
			throw new NotImplementedException("Article search is not implemented yet!");
		}
		articles = articleDao.getAllEditArticlesListDataBeans(maxResult, startPosition, getIwc());
		
		return articles;
	}
	
	public Integer getArticlesCount() {
		if(totalCount != null){
			return totalCount;
		}
		totalCount = articleDao.countArticles();
		return totalCount;
	}

	public IWContext getIwc() {
		if(iwc == null){
			iwc = CoreUtil.getIWContext();
		}
		return iwc;
	}

	public void setIwc(IWContext iwc) {
		this.iwc = iwc;
	}
	
	public String getCreateUri(){
		try {
			BuilderService service = BuilderServiceFactory.getBuilderService(iwc);
			return service.getUriToObject(ArticleEdit.class,null);
		} catch (RemoteException e) {
			getLogger().log(Level.WARNING, "Failed getting create uri", e);
		}
		return null;
	}
	
	
}
