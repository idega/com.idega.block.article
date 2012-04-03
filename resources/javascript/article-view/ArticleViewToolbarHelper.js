var ArticleViewToolbarHelper = {};

ArticleViewToolbarHelper.initEdit = function(linkId){
	jQuery("#"+linkId)..fancybox({
		width: windowinfo.getWindowWidth() * 0.5
		,height: windowinfo.getWindowHeight() * 0.7
		,autoDimensions: false
	});
}
ArticleViewToolbarHelper.LOADING_MSG = "";
ArticleViewToolbarHelper.TOOLBAR_SELECTOR = null;
ArticleViewToolbarHelper.edit = function(input,linkSelector,urlContainerClass,fbDivSelector){
	showLoadingMessage(ArticleViewToolbarHelper.LOADING_MSG);
	var urlContainer = jQuery(input).parents(ArticleViewToolbarHelper.TOOLBAR_SELECTOR).children("."+urlContainerClass);
	var url = urlContainer.val();
	ArticleComponent = ArticleServices.getArticleEditForm(url,{
		callback : function(articleEditor){
			var fbDiv = jQuery(fbDivSelector);
			fbDiv.empty();
			IWCORE.insertRenderedComponent(component,{
				container: fbDiv,
				append: true
			});
			jQuery(linkSelector).click();
			closeAllLoadingMessages();
		}
	});
}

