package com.idega.block.article.component;

import com.idega.block.article.business.ArticleConstants;
import com.idega.content.business.ContentConstants;
import com.idega.content.presentation.ContentViewer;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.Block;
import com.idega.presentation.DefaultErrorHandlingUriWindow;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.text.Heading1;
import com.idega.presentation.text.Heading3;
import com.idega.presentation.ui.GenericButton;
import com.idega.util.CoreConstants;
import com.idega.util.StringUtil;

public class ArticleDeleter extends Block {
	
	@Override
	public void main(IWContext iwc) {
		Layer container = new Layer();
		container.setStyleClass("articleDelete");
		add(container);
		Layer buttons = new Layer();
		
		String action = iwc.getParameter(ContentViewer.PARAMETER_ACTION);
		String resource = iwc.getParameter(ContentViewer.PARAMETER_CONTENT_RESOURCE);
		String fromArticleListParameter = iwc.getParameter(ContentConstants.RENDERING_COMPONENT_OF_ARTICLE_LIST);
		if (StringUtil.isEmpty(fromArticleListParameter)) {
			fromArticleListParameter = Boolean.TRUE.toString();
		}
		
		String nullValue = "null";
		if (action == null || nullValue.endsWith(action)) {
			container.add(new DefaultErrorHandlingUriWindow());
			return;
		}
		
		IWResourceBundle iwrb = getResourceBundle(iwc);
		String closeAction = "window.parent.ArticleEditorHelper.closeAllObjects();";
		if (!action.equals(ContentConstants.CONTENT_ITEM_ACTION_DELETE) || resource == null || nullValue.equals(resource)) {
			container.add(new Heading1(iwrb.getLocalizedString("undefined_action", "Sorry, can not delete article.")));
			
			container.add(buttons);
			GenericButton close = new GenericButton(iwrb.getLocalizedString("close", "Close"));
			close.setOnClick(closeAction);
			buttons.add(close);
			return;
		}
		
		if (resource.startsWith(CoreConstants.WEBDAV_SERVLET_URI)) {
			resource = resource.replaceFirst(CoreConstants.WEBDAV_SERVLET_URI, CoreConstants.EMPTY);
		}
		
		String text = iwrb.getLocalizedString("are_you_sure_you_want_delete_this_article", "Are you sure you want to delete this article?");
		container.add(new Heading3(text));
		
		container.add(buttons);
		GenericButton delete = new GenericButton(iwrb.getLocalizedString("delete", "Delete"));
		delete.setOnClick(new StringBuffer("window.parent.showLoadingMessage('").append(iwrb.getLocalizedString("deleting", "Deleting..."))
							.append("'); window.parent.ArticleEditorHelper.deleteSelectedArticle('").append(resource).append("', ").append(fromArticleListParameter)
							.append(");").toString());
		buttons.add(delete);
		
		GenericButton cancel = new GenericButton(iwrb.getLocalizedString("cancel", "Cancel"));
		cancel.setOnClick(closeAction);
		buttons.add(cancel);
	}
	
	@Override
	public String getBundleIdentifier() {
		return ArticleConstants.IW_BUNDLE_IDENTIFIER;
	}

}
