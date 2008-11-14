package com.idega.block.article.business;

import java.rmi.RemoteException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.idega.block.article.bean.ArticleFields;
import com.idega.block.article.bean.ArticleItemBean;
import com.idega.block.web2.business.Web2Business;
import com.idega.content.bean.ManagedContentBeans;
import com.idega.content.business.ContentConstants;
import com.idega.content.presentation.ContentItemToolbar;
import com.idega.core.accesscontrol.business.StandardRoles;
import com.idega.core.builder.business.BuilderService;
import com.idega.core.builder.business.BuilderServiceFactory;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.IWContext;
import com.idega.util.CoreConstants;
import com.idega.util.CoreUtil;
import com.idega.util.IWTimestamp;
import com.idega.util.StringUtil;
import com.idega.util.expression.ELUtil;
import com.idega.webface.WFUtil;

@Scope("singleton")
@Service(ArticleItemInfoFetcher.SPRING_BEAN_IDENTIFIER)
public class ArticleItemInfoFetcher {

	public static final String SPRING_BEAN_IDENTIFIER = "articleItemInfoFetcher";
	
	private static final Logger LOGGER = Logger.getLogger(ArticleItemInfoFetcher.class.getName());
	
	public ArticleFields getArticleInfo(String resourcePath) {
		if (StringUtil.isEmpty(resourcePath)) {
			return null;
		}
		
		IWContext iwc = CoreUtil.getIWContext();
		if (iwc == null) {
			return null;
		}
		
		if (!hasRights(iwc)) {
			return null;
		}
		
		ArticleItemBean articleBean = null;
		Object o = WFUtil.getBeanInstance(ManagedContentBeans.ARTICLE_ITEM_BEAN_ID);
		if (o instanceof ArticleItemBean) {
			articleBean = (ArticleItemBean) o;
		}
		else {
			articleBean = new ArticleItemBean();
			articleBean.setResourcePath(resourcePath);
			
		}
		if (articleBean == null) {
			return null;
		}
		
		boolean needToLoad = false;
		String currentResourcePath = articleBean.getResourcePath();
		if (StringUtil.isEmpty(currentResourcePath) || !resourcePath.equals(currentResourcePath)) {
			articleBean.setResourcePath(resourcePath);
			needToLoad = true;
		}
		if (needToLoad || !articleBean.isLoaded()) {
			try {
				articleBean.load();
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE, "Error getting resource: " + resourcePath, e);
				return null;
			}
		}
		
		ArticleFields info = new ArticleFields();
		
		info.setHeader(articleBean.getHeadline());
		
		IWTimestamp date = articleBean.getPublishedDate() == null ? null : new IWTimestamp(articleBean.getPublishedDate());
		info.setDate(date == null ? CoreConstants.EMPTY : date.getLocaleDateAndTime(iwc.getCurrentLocale(), DateFormat.SHORT, DateFormat.SHORT));
		
		info.setAuthor(articleBean.getAuthor());
		info.setBody(articleBean.getBody());
		info.setTeaser(articleBean.getTeaser());
		
		return info;
	}
	
	public List<String> getArticleWasNotDeletedMessage() {
		String articleWasNotDeleted = "Oops... Article was not deleted - some error occurred...";
		
		IWContext iwc = CoreUtil.getIWContext();
		if (iwc == null) {
			return null;
		}
		
		IWResourceBundle iwrb = null;
		try {
			iwrb = ArticleUtil.getBundle().getResourceBundle(iwc);
		} catch(Exception e) {
			LOGGER.log(Level.WARNING, "Error getting resource bundle!", e);
		}
		if (iwrb == null) {
			return Arrays.asList(articleWasNotDeleted);
		}
		
		List<String> resources = new ArrayList<String>();
		resources.add(iwrb.getLocalizedString("article_item.article_was_not_deleted", articleWasNotDeleted));
		
		Web2Business web2 = ELUtil.getInstance().getBean(Web2Business.SPRING_BEAN_IDENTIFIER);
		resources.add(web2.getBundleUriToHumanizedMessagesStyleSheet());
		resources.add(web2.getBundleUriToHumanizedMessagesScript());
		
		return resources;
	}
	
	public String getButtons(String resourcePath, String previousAction, boolean fromArticleItemListViewer) {
		if (StringUtil.isEmpty(resourcePath) || StringUtil.isEmpty(previousAction)) {
			return null;
		}
		if (ContentConstants.CONTENT_ITEM_ACTION_EDIT.equals(previousAction)) {
			return null;
		}
		
		IWContext iwc = CoreUtil.getIWContext();
		if (iwc == null) {
			return null;
		}
		if (!hasRights(iwc)) {
			return null;
		}

		ContentItemToolbar toolbar = new ContentItemToolbar(true);
		toolbar.setResourcePath(resourcePath);
		toolbar.setToolbarActions(ContentConstants.CONTENT_ITEM_ACTION_CREATE.equals(previousAction) ?
				new String[] {ContentConstants.CONTENT_ITEM_ACTION_DELETE, ContentConstants.CONTENT_ITEM_ACTION_EDIT} :
				new String[] {ContentConstants.CONTENT_ITEM_ACTION_CREATE});
		
		BuilderService builderService = null;
		try {
			builderService = BuilderServiceFactory.getBuilderService(iwc);
		} catch (RemoteException e) {
			LOGGER.log(Level.SEVERE, "Error getting instance of: " + BuilderService.class.getName(), e);
		}
		if (builderService == null) {
			return null;
		}
		
		iwc.setSessionAttribute(ContentConstants.RENDERING_COMPONENT_OF_ARTICLE_LIST, Boolean.valueOf(fromArticleItemListViewer));
		String buttonsHTML = builderService.getRenderedComponent(toolbar, iwc, true);
		iwc.removeSessionAttribute(ContentConstants.RENDERING_COMPONENT_OF_ARTICLE_LIST);
		
		return buttonsHTML;
	}
	
	private boolean hasRights(IWContext iwc) {
		if (!iwc.isLoggedOn()) {
			return false;
		}
		if (!iwc.hasRole(StandardRoles.ROLE_KEY_AUTHOR) || !iwc.hasRole(StandardRoles.ROLE_KEY_EDITOR)) {
			return false;
		}
		
		return true;
	}
	
}
