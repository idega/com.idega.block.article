package com.idega.block.article.bean.article_view;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.logging.Level;

import org.directwebremoting.annotations.Param;
import org.directwebremoting.annotations.RemoteMethod;
import org.directwebremoting.annotations.RemoteProxy;
import org.directwebremoting.spring.SpringCreator;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.idega.block.article.bean.ArticleItemBean;
import com.idega.block.article.business.ArticleConstants;
import com.idega.block.article.component.article_view.ArticleEdit;
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

@Service(ArticleServices.SERVICE)
@Scope(BeanDefinition.SCOPE_SINGLETON)
@RemoteProxy(creator=SpringCreator.class, creatorParams={
	@Param(name="beanName", value=ArticleServices.SERVICE),
	@Param(name="javascript", value=ArticleServices.DWR_SERVICE)
}, name=ArticleServices.DWR_SERVICE)
public class ArticleServices  extends DefaultSpringBean implements DWRAnnotationPersistance {
	public static final String SERVICE = "articleServices";
	public static final String DWR_SERVICE = "ArticleServices";

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
		String uri = saveValues.get("articleUri");
		ArticleItemBean articleItemBean;
		if(StringUtil.isEmpty(uri)){
			articleItemBean = new ArticleItemBean();
		}else{
			try {
				articleItemBean = getArticleItemBean(uri);
			} catch (IOException e) {
				getLogger().log(Level.WARNING, "Failed saving article", e);
				reply.put("status", "failed");
				reply.put("message",iwrb.getLocalizedString("saving_failed", "Saving failed"));
				return reply;
			}
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


		reply.put("status", "success");
		reply.put("message",iwrb.getLocalizedString("article_saved", "Article saved"));
		return reply;
	}


}
