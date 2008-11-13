if (ArticleEditorHelper == null) var ArticleEditorHelper = {};

ArticleEditorHelper.initializeJavaScriptActionsForEditingAndCreatingArticles = function() {
	window.addEvent('resize', ArticleEditorHelper.setDimensionsForArticleEditWindow);
	
	ArticleEditorHelper.setDimensionsForArticleEditWindow();
	ArticleEditorHelper.registerArticleLinksForMoodalBox();
}

ArticleEditorHelper.setDimensionsForArticleEditWindow = function() {
	var width = Math.round(window.getWidth() * 0.8);
	var height = Math.round(window.getHeight() * 0.8);
	
	try {
		MOOdalBox.init({resizeDuration: 0, evalScripts: true, animateCaption: false, defContentsWidth: width, defContentsHeight: height});
	} catch(e) {}
}

ArticleEditorHelper.registerArticleLinksForMoodalBox = function() {
	$$('a.edit').each(
		function(element) {
			ArticleEditorHelper.checkArticleLinkAndRegisterIfItsCorrect(element);
		}
	);
	$$('a.create').each(
		function(element) {
			ArticleEditorHelper.checkArticleLinkAndRegisterIfItsCorrect(element);
		}
	);
	$$('a.delete').each(
		function(element) {
			ArticleEditorHelper.checkArticleLinkAndRegisterIfItsCorrect(element);
		}
	);
}

ArticleEditorHelper.checkArticleLinkAndRegisterIfItsCorrect = function(link) {
	if (link == null) {
		return false;
	}
	
	var relProperty = link.getProperty('rel');
	if (relProperty == null) {
		return false;
	}
	
	if (relProperty.indexOf('moodalbox') != -1) {
		MOOdalBox.register(link);
	}
}

ArticleEditorHelper.addActionAfterArticleIsSavedAndEditorClosed = function() {
	MOOdalBox.addEventToCloseAction(function() {
		reloadPage();
	});
}

ArticleEditorHelper.deleteSelectedArticle = function(resource) {
	ThemesEngine.deleteArticle(resource, {
		callback: function(result) {
			MOOdalBox.close();
			reloadPage();
		}
	});
}