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

public class ArticleDeleter extends Block {
	
	public void main(IWContext iwc) {
		Layer container = new Layer();
		add(container);
		Layer buttons = new Layer();
		
		String action = iwc.getParameter(ContentViewer.PARAMETER_ACTION);
		String resource = iwc.getParameter(ContentViewer.PARAMETER_CONTENT_RESOURCE);
		
		String nullValue = "null";
		if (action == null || nullValue.endsWith(action)) {
			container.add(new DefaultErrorHandlingUriWindow());
			return;
		}
		
		IWResourceBundle iwrb = getResourceBundle(iwc);
		String closeAction = "MOOdalBox.close();";
		if (!action.equals(ContentConstants.CONTENT_ITEM_ACTION_DELETE) || resource == null || nullValue.equals(resource)) {
			container.add(new Heading1(iwrb.getLocalizedString("undefined_action", "Sorry, can not delete article.")));
			
			container.add(buttons);
			GenericButton close = new GenericButton(iwrb.getLocalizedString("close", "Close"));
			close.setOnClick(closeAction);
			buttons.add(close);
			return;
		}
		
		String text = iwrb.getLocalizedString("are_you_sure_you_want_delete_this_article", "Are you sure you want delete this article?");
		container.add(new Heading3(text));
		
		container.add(buttons);
		GenericButton delete = new GenericButton(iwrb.getLocalizedString("delete", "Delete"));
		delete.setOnClick(new StringBuffer("deleteSelectedArticle('").append(resource).append("');").toString());
		buttons.add(delete);
		
		GenericButton cancel = new GenericButton(iwrb.getLocalizedString("cancel", "Cancel"));
		cancel.setOnClick(closeAction);
		buttons.add(cancel);
	}
	
	public String getBundleIdentifier() {
		return ArticleConstants.IW_BUNDLE_IDENTIFIER;
	}

}
