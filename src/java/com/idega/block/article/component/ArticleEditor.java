package com.idega.block.article.component;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import javax.faces.component.UIComponent;

import com.idega.block.article.business.ArticleConstants;
import com.idega.block.article.component.article_view.ArticleEdit;
import com.idega.builder.bean.AdvancedProperty;
import com.idega.content.business.ContentConstants;
import com.idega.content.presentation.ContentItemToolbar;
import com.idega.content.presentation.ContentViewer;
import com.idega.core.builder.business.BuilderService;
import com.idega.idegaweb.IWMainApplicationSettings;
import com.idega.presentation.Block;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.ui.IFrame;

public class ArticleEditor extends Block {

	@Override
	public void main(IWContext iwc) {
		Layer container = new Layer();
		add(container);

		String resourcePath = iwc.getParameter(ContentViewer.PARAMETER_CONTENT_RESOURCE);
		if (resourcePath == null) {
			return;
		}
		String action = iwc.getParameter(ContentViewer.PARAMETER_ACTION);
		if (action == null) {
			action = ContentConstants.CONTENT_ITEM_ACTION_EDIT;
		}
		String renderingFromList = Boolean.TRUE.toString();
		String renderingParameter = iwc.getParameter(ContentConstants.RENDERING_COMPONENT_OF_ARTICLE_LIST);
		if (renderingParameter != null) {
			renderingFromList = renderingParameter;
		}
		String basePath = iwc.getParameter(ContentItemToolbar.PARAMETER_BASE_FOLDER_PATH);
		String categories = iwc.getParameter(ContentItemToolbar.PARAMETER_CATEGORIES);
		String containerId = iwc.getParameter(ContentConstants.CONTENT_LIST_ITEMS_IDENTIFIER);

		BuilderService service = null;
		try {
			service = getBuilderService(iwc);
		} catch (RemoteException e) {
			e.printStackTrace();
			return;
		}

		List<AdvancedProperty> parameters = new ArrayList<AdvancedProperty>();
		parameters.add(new AdvancedProperty(ContentViewer.PARAMETER_CONTENT_RESOURCE, resourcePath));
		parameters.add(new AdvancedProperty(ContentViewer.PARAMETER_ACTION, action));
		parameters.add(new AdvancedProperty(ContentViewer.CONTENT_VIEWER_EDITOR_NEEDS_FORM, Boolean.TRUE.toString()));
		parameters.add(new AdvancedProperty(ContentConstants.RENDERING_COMPONENT_OF_ARTICLE_LIST, renderingFromList));
		if (basePath != null) {
			parameters.add(new AdvancedProperty(ContentItemToolbar.PARAMETER_BASE_FOLDER_PATH, basePath));
		}
		if (categories != null) {
			parameters.add(new AdvancedProperty(ContentItemToolbar.PARAMETER_CATEGORIES, categories));
		}
		if (containerId != null) {
			parameters.add(new AdvancedProperty(ContentConstants.CONTENT_LIST_ITEMS_IDENTIFIER, containerId));
		}

		IWMainApplicationSettings settings = iwc.getIWMainApplication().getSettings();
		Class<? extends UIComponent> articleEditorClass = settings.getBoolean(ArticleConstants.USE_ROLES_IN_ARTICLE, Boolean.FALSE) ?
				ArticleEdit.class : EditArticleView.class;
		String uri = service.getUriToObject(articleEditorClass, parameters);
		IFrame frame = new IFrame(EditArticleView.class.getSimpleName(), uri);
		frame.setStyleClass("contentItemEditorFrameStyle");
		frame.setStyleAttribute("width", "100%");
		frame.setStyleAttribute("height", "99%");

		container.add(frame);
	}

	@Override
	public String getBundleIdentifier() {
		return ArticleConstants.IW_BUNDLE_IDENTIFIER;
	}
}
