package com.idega.block.article.bean.article_view;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.apache.commons.httpclient.HttpStatus;
import org.directwebremoting.annotations.Param;
import org.directwebremoting.annotations.RemoteMethod;
import org.directwebremoting.annotations.RemoteProxy;
import org.directwebremoting.spring.SpringCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.idega.block.article.bean.ArticleItemBean;
import com.idega.block.article.business.ArticleConstants;
import com.idega.block.article.business.EditArticlesListBean;
import com.idega.block.article.component.article_view.ArticleEdit;
import com.idega.block.article.data.dao.ArticleDao;
import com.idega.builder.business.BuilderLogic;
import com.idega.core.business.DefaultSpringBean;
import com.idega.core.component.bean.RenderedComponent;
import com.idega.dwr.business.DWRAnnotationPersistance;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.IWContext;
import com.idega.util.CoreConstants;
import com.idega.util.CoreUtil;
import com.idega.util.IWTimestamp;
import com.idega.util.ListUtil;
import com.idega.util.StringUtil;
import com.idega.util.expression.ELUtil;

@Service(ArticleServices.SERVICE)
@Scope(BeanDefinition.SCOPE_SINGLETON)
@RemoteProxy(creator=SpringCreator.class, creatorParams={
	@Param(name="beanName", value=ArticleServices.SERVICE),
	@Param(name="javascript", value=ArticleServices.DWR_SERVICE)
}, name=ArticleServices.DWR_SERVICE)
public class ArticleServices  extends DefaultSpringBean implements DWRAnnotationPersistance {
	public static final String SERVICE = "articleServices";
	public static final String DWR_SERVICE = "ArticleServices";

	@Autowired
	private ArticleDao articleDao;

	public ArticleItemBean getArticleItemBean(String uri) throws IOException{
		ArticleItemBean article = new ArticleItemBean();
		if (StringUtil.isEmpty(uri) || (uri.equals(CoreConstants.SLASH))) {
			getLogger().warning("URI is invalid: " + uri);
			return null;
		}

		article.setResourcePath(uri);
		article.load();
		return article;
	}

	@RemoteMethod
	public RenderedComponent getArticleEditForm(String url){
		ArticleEdit articleEdit = new ArticleEdit();
		RenderedComponent editForm = BuilderLogic.getInstance().getRenderedComponent(articleEdit, null);
		return editForm;
	}

	@RemoteMethod
	public Map<String,String> saveArticle(Map<String,String> saveValues,Map<String,Collection<String>> collectionValues){
		IWContext iwc = CoreUtil.getIWContext();
		IWResourceBundle iwrb = getBundle(ArticleConstants.IW_BUNDLE_IDENTIFIER).getResourceBundle(iwc);
		Map<String,String> reply = new HashMap<String,String>();
		if(saveValues == null){
			reply.put("status", "failed");
			reply.put("message",iwrb.getLocalizedString("saving_failed", "Saving failed"));
			return reply;
		}
		try{
			String uri = saveValues.get("articleUri");
			ArticleItemBean articleItemBean;
			if(StringUtil.isEmpty(uri)){
				articleItemBean = new ArticleItemBean();
			}else{
				articleItemBean = getArticleItemBean(uri);
			}
			if(!articleItemBean.isAllowedToEditByCurrentUser(iwc)){
				reply.put("status", "failed");
				String message = iwrb.getLocalizedString("saving_failed", "Saving failed")
						+" : \n"+ iwrb.getLocalizedString("permission_denied", "Permission denied");
				reply.put("message",message);
				return reply;
			}
			articleItemBean.setHeadline(saveValues.get("headline"));
			articleItemBean.setBody(saveValues.get("body"));
			articleItemBean.setTeaser(saveValues.get("teaser"));
			articleItemBean.setAuthor(iwc.getCurrentUser().getDisplayName());
			articleItemBean.setName(saveValues.get("headline"));
			articleItemBean.setPublishedDate(IWTimestamp.getTimestampRightNow());
			articleItemBean.setLocale(iwc.getCurrentLocale());

			articleItemBean.setArticleCategories(collectionValues.get("articleCategories"));

			Collection<String> permissionGroups = collectionValues.get("permissionGroups");
			if(!ListUtil.isEmpty(permissionGroups)){
				HashSet<Integer> editors = new HashSet<Integer>(permissionGroups.size());
				for(String id : permissionGroups){
					editors.add(Integer.valueOf(id));
				}
				articleItemBean.setAllowedToEditByGroupsIds(editors);
			}
			articleItemBean.store();

//			RepositoryService repositoryService = ELUtil.getInstance().getBean(RepositoryService.class);
//			String filesPath = articleItemBean.getFilesResourcePath();
//			AccessControlList accessControlList = repositoryService.getAccessControlList(filesPath);
//			AccessControlEntry access = new JackrabbitAccessControlEntry(RepositoryConstants.SUBJECT_URI_AUTHENTICATED);
//			access.addPrivilege(new RepositoryPrivilege(Privilege.JCR_ALL));
//			AccessControlEntry[] accesses = {access};
//			accessControlList.setAces(accesses);

			reply.put("status", "success");
			reply.put("message",iwrb.getLocalizedString("article_saved", "Article saved"));
			return reply;
		} catch (Exception e) {
			getLogger().log(Level.WARNING, "Failed saving article", e);
			reply.put("status", "failed");
			reply.put("message",iwrb.getLocalizedString("saving_failed", "Saving failed"));
			return reply;
		}
	}

	@RemoteMethod
	public ArticleSearchResponce getArticles(Integer maxResult,Integer startPosition){
		int max = maxResult == null ? -1 : maxResult;
		int start = startPosition == null ? -1 : startPosition;
		ArticleSearchResponce responce = new ArticleSearchResponce();
		EditArticlesListBean editArticlesListBean = ELUtil.getInstance().getBean(EditArticlesListBean.BEAN_NAME);
		IWContext iwc = editArticlesListBean.getIwc();
		IWResourceBundle iwrb = getIwrb(iwc);
		try{
			List<EditArticlesListDataBean> articles = editArticlesListBean.searchArticles(max, start);
			responce.setStatus(HttpStatus.getStatusText(HttpStatus.SC_OK));
			responce.setArticles(articles);
			Integer totalCount = editArticlesListBean.getArticlesCount();
			List<EditArticleListPage> pages = editArticlesListBean.getProductListPagesPages(totalCount, max,start);
			responce.setPages(pages);
			responce.setTotalCount(totalCount);
		}catch (Exception e) {
			responce.setStatus(HttpStatus.getStatusText(HttpStatus.SC_INTERNAL_SERVER_ERROR));
			responce.setMessage(iwrb.getLocalizedString("failed_getting_articles", "failed getting articles"));
			getLogger().log(Level.WARNING, "Failed getting articles: ", e);
		}
		return responce;
	}


	protected IWResourceBundle getIwrb(IWContext iwc) {
		IWResourceBundle iwrb = iwc.getIWMainApplication().getBundle(ArticleConstants.IW_BUNDLE_IDENTIFIER).getResourceBundle(iwc);
		return iwrb;
	}

	@RemoteMethod
	public Response deleteArticle(Long id){
		Response responce = new Response();
		IWContext iwc = CoreUtil.getIWContext();
		IWResourceBundle iwrb = getIwrb(iwc);
		try{
			boolean exists = articleDao.deleteArticle(id);
			responce.setStatus(HttpStatus.getStatusText(HttpStatus.SC_OK));
			if(!exists){
				responce.setMessage(iwrb.getLocalizedString("article_does_not_exist", "Article does not exist"));
			}

		}catch (Exception e) {
			responce.setStatus(HttpStatus.getStatusText(HttpStatus.SC_INTERNAL_SERVER_ERROR));
			responce.setMessage(iwrb.getLocalizedString("failed_getting_articles", "failed getting articles"));
			getLogger().log(Level.WARNING, "Failed getting articles: ", e);
		}
		return responce;
	}

}
