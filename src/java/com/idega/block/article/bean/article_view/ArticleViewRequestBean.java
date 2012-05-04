package com.idega.block.article.bean.article_view;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.idega.block.article.bean.ArticleItemBean;
import com.idega.block.article.business.ArticleConstants;
import com.idega.block.article.component.article_view.ArticleFileBrowser;
import com.idega.block.article.data.ArticleEntity;
import com.idega.block.article.data.dao.ArticleDao;
import com.idega.builder.bean.AdvancedProperty;
import com.idega.content.presentation.ContentViewer;
import com.idega.core.accesscontrol.business.AccessController;
import com.idega.core.accesscontrol.business.NotLoggedOnException;
import com.idega.core.accesscontrol.business.StandardRoles;
import com.idega.core.builder.business.BuilderService;
import com.idega.core.builder.business.BuilderServiceFactory;
import com.idega.core.business.DefaultSpringBean;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWMainApplication;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.IWContext;
import com.idega.user.data.Group;
import com.idega.util.CoreConstants;
import com.idega.util.CoreUtil;
import com.idega.util.ListUtil;


@Service(ArticleViewRequestBean.SERVICE)
@Scope("request")
public class ArticleViewRequestBean  extends DefaultSpringBean{
	public static final String SERVICE = "articleViewRequestBean";
	
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
		String action = iwc.getParameter(ContentViewer.PARAMETER_ACTION);
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
		if(articleItemBean == null){
			return true;
		}
		return articleItemBean.isAllowedToEditByCurrentUser(iwc);
	}
	
	public ArticleItemBean getArticleItemBean() throws IOException{
		if(articleItemBean != null){
			return articleItemBean;
		}
		String uri = iwc.getParameter(ContentViewer.PARAMETER_CONTENT_RESOURCE);
		articleItemBean = articleServices.getArticleItemBean(uri);
		return articleItemBean;
		
	}
	
	public String getResourcePath() throws IOException{
		return getArticleItemBean().getResourcePath();
	}
	
	public String getArticleResourcesPath() throws IOException{
		return getResourcePath().concat("/article-files/");
	}
	public String getUriToFileBrowser() throws IOException{
		BuilderService builderService;
		try {
			builderService = BuilderServiceFactory.getBuilderService(iwc);
		} catch (RemoteException e) {
			getLogger().log(Level.WARNING, "Failed getting uri to filebrowser.", e);
			return CoreConstants.EMPTY;
		}
		List<AdvancedProperty> parameters = new ArrayList<AdvancedProperty>();
		parameters.add(new AdvancedProperty(ContentViewer.PARAMETER_CONTENT_RESOURCE, getResourcePath()));
		parameters.add(new AdvancedProperty(ArticleFileBrowser.PARAMETER_UPLOAD_PATH, getArticleResourcesPath()));
		return builderService.getUriToObject(ArticleFileBrowser.class, parameters);
	}
	
	/**
	 * @return path where article files should be uploaded by current user.
	 * @throws IOException 
	 * @throws NotLoggedOnException if user is not logged on
	 */
	public String getUploadPath() throws IOException{
		String uploadPath = iwc.getParameter(ArticleFileBrowser.PARAMETER_UPLOAD_PATH);
		if(uploadPath == null){
			uploadPath = getArticleItemBean().getResourcePath() + "/article-files/";
		}
		if(!uploadPath.startsWith(CoreConstants.SLASH)){
			uploadPath = CoreConstants.SLASH + uploadPath;
		}
		if(!uploadPath.startsWith("/content")){
			uploadPath = "/content" + uploadPath;
		}
		return uploadPath;
	}
	
	@Transactional
	public Collection<String> getEditors() throws IOException{
		Collection<String> editors = new HashSet<String>();
		
		Collection<Integer> existingEditors = null;
		
		ArticleEntity articleEntity = getArticleItemBean().getArticleEntity();
		if(articleEntity != null){
			existingEditors = articleEntity.getEditors();
		}
		if(ListUtil.isEmpty(existingEditors)){
			editors.addAll(getDefaultEditorsGroups());
		}else{
			for(Integer editor : existingEditors){
				editors.add(String.valueOf(editor));
			}
		}
		return editors;
	}
	
	private Collection<String> getDefaultEditorsGroups(){
		List <String> defaultEditorGroups = new ArrayList<String>();
		
		AccessController accessController = iwc.getAccessController();
		IWApplicationContext iwac = IWMainApplication.getDefaultIWApplicationContext();
		Collection <Group> groups = accessController.getAllGroupsForRoleKey(StandardRoles.ROLE_KEY_AUTHOR, iwac);
		if(!ListUtil.isEmpty(groups)){
			defaultEditorGroups.add(groups.iterator().next().getId());
		}
		groups = accessController.getAllGroupsForRoleKey(StandardRoles.ROLE_KEY_EDITOR, iwac);
		if(!ListUtil.isEmpty(groups)){
			defaultEditorGroups.add(groups.iterator().next().getId());
		}
		
		return defaultEditorGroups;
	}
	
	public String getArticleCategoriesJsArray(){
		Collection <String> categories;
//		Collection <CategoryEntity> categories;
		try {
//			categories = getArticleItemBean().getArticleEntity().getCategories();
			categories = getArticleItemBean().getCategories();
		} catch (Exception e) {
			getLogger().log(Level.WARNING, "failed getting article categories", e);
			return "[]";
		}
		if(ListUtil.isEmpty(categories)){
			return "[]";
		}
		StringBuilder array = new StringBuilder("[");
		for(Iterator<String> iter = categories.iterator();iter.hasNext();){
			array.append(CoreConstants.QOUTE_SINGLE_MARK).append(iter.next()).append(CoreConstants.QOUTE_SINGLE_MARK);
			if(iter.hasNext()){
				array.append(CoreConstants.COMMA);
			}
		}
//		for(Iterator<CategoryEntity> iter = categories.iterator();iter.hasNext();){
//			array.append(iter.next().getCategory()).append(CoreConstants.QOUTE_SINGLE_MARK);;
//			if(iter.hasNext()){
//				array.append(CoreConstants.COMMA);
//			}
//		}
		array.append("]");
		return array.toString();
	}
	
	
}
