package com.idega.block.article.component.article_view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.context.FacesContext;

import com.idega.block.article.business.ArticleConstants;
import com.idega.block.web2.business.JQuery;
import com.idega.block.web2.business.Web2Business;
import com.idega.facelets.ui.FaceletComponent;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWMainApplication;
import com.idega.presentation.IWBaseComponent;
import com.idega.presentation.IWContext;
import com.idega.util.CoreConstants;
import com.idega.util.PresentationUtil;
import com.idega.webface.WFUtil;


public class ArticleEdit extends IWBaseComponent{


	@Override
	protected void initializeComponent(FacesContext context) {
		super.initializeComponent(context);

		IWContext iwc = IWContext.getIWContext(context);

		IWBundle bundle = iwc.getIWMainApplication().getBundle(ArticleConstants.IW_BUNDLE_IDENTIFIER);
		FaceletComponent facelet = (FaceletComponent)iwc.getApplication().createComponent(FaceletComponent.COMPONENT_TYPE);
		facelet.setFaceletURI(bundle.getFaceletURI("article-view/article-edit.xhtml"));

		add(facelet);

		addFiles(iwc);
	}

	private void addFiles(IWContext iwc){
		List<String> scripts = new ArrayList<String>();
		List<String> styles = new ArrayList<String>();

		scripts.add(CoreConstants.DWR_ENGINE_SCRIPT);
		scripts.add(CoreConstants.DWR_UTIL_SCRIPT);

		Web2Business web2 = WFUtil.getBeanInstance(iwc, Web2Business.SPRING_BEAN_IDENTIFIER);
		if (web2 != null) {
			JQuery  jQuery = web2.getJQuery();
			scripts.add(jQuery.getBundleURIToJQueryLib());

			scripts.addAll(web2.getBundleURIsToFancyBoxScriptFiles());
			styles.add(web2.getBundleURIToFancyBoxStyleFile());

			scripts.add(web2.getBundleUriToHumanizedMessagesScript());
			styles.add(web2.getBundleUriToHumanizedMessagesStyleSheet());

			List<String> tinyMceFiles = Arrays.asList("tiny_mce.js");
			scripts.addAll(web2.getBundleUrisToTinyMceScriptFiles("3.5b3", tinyMceFiles));
		} else {
			Logger.getLogger(ArticleEdit.class.getName()).log(Level.WARNING, "Failed getting Web2Business no jQuery and it's plugins files were added");
		}

		IWMainApplication iwma = iwc.getApplicationContext().getIWMainApplication();
		IWBundle iwb = iwma.getBundle(ArticleConstants.IW_BUNDLE_IDENTIFIER);
		scripts.add(iwb.getVirtualPathWithFileNameString("javascript/article-view/ArticleEditHelper.js"));
		styles.add(iwb.getVirtualPathWithFileNameString("style/article-view/article-view.css"));
		scripts.add("/dwr/interface/ArticleServices.js");

		iwb = iwma.getBundle("com.idega.core");
		scripts.add(iwb.getVirtualPathWithFileNameString("iw_core.js"));
		styles.add(iwb.getVirtualPathWithFileNameString("style/iw_core.css"));
		PresentationUtil.addJavaScriptSourcesLinesToHeader(iwc, scripts);
		PresentationUtil.addStyleSheetsToHeader(iwc, styles);
	}



}
