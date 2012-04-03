package com.idega.block.article.component.article_view;

import javax.faces.context.FacesContext;

import com.idega.block.article.business.ArticleConstants;
import com.idega.facelets.ui.FaceletComponent;
import com.idega.idegaweb.IWBundle;
import com.idega.presentation.IWBaseComponent;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;

public class ArticleViewToolbar  extends IWBaseComponent{

	@Override
	protected void initializeComponent(FacesContext context) {
		super.initializeComponent(context);
		
		Layer layer = new Layer();
		add(layer);
		
		IWContext iwc = IWContext.getIWContext(context);
		
		IWBundle bundle = iwc.getIWMainApplication().getBundle(ArticleConstants.IW_BUNDLE_IDENTIFIER);
		FaceletComponent facelet = (FaceletComponent)iwc.getApplication().createComponent(FaceletComponent.COMPONENT_TYPE);
		facelet.setFaceletURI(bundle.getFaceletURI("article-view/article-toolbar.xhtml"));
		
		layer.add(facelet);
		
	}
}
