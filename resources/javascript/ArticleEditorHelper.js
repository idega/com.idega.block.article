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
}

ArticleEditorHelper.deleteSelectedArticle = function(resource, fromArticleItemListViewer) {
	ThemesEngine.deleteArticle(resource, {
		callback: function(result) {
			LazyLoader.loadMultiple(['/dwr/engine.js', '/dwr/interface/ArticleItemInfoFetcher.js'], function() {
				if (result) {
					var parents = ArticleEditorHelper.getArticleContainer(null, resource);
					var info = {header: '', body: '', teaser: '', date: '', author: ''};
					ArticleEditorHelper.updateArticleFields(info, parents);
					
					ArticleEditorHelper.addToolbarButtons(resource, 'delete', parents, fromArticleItemListViewer, function() {MOOdalBox.close()});
				}
				else {
					ArticleItemInfoFetcher.getArticleWasNotDeletedMessage({
						callback: function(resources) {
							if (resources == null || resources.length == 0) {
								reloadPage();
								return false;
							}
							
							var message = resources[0];
							if (resources.length == 1) {
								alert(message);
								return false;
							}
		
							var stuffToLoad = new Array();
							for (var i = 1; i < resources.length; i++) {
								stuffToLoad.push(resources[i]);
							}
							LazyLoader.loadMultiple(stuffToLoad, function() {
								humanMsg.displayMsg(message);
							});
						}
					});
				}
			});
		}
	});
}

ArticleEditorHelper.reloadArticle = function(resourcePath, styleClass, mode, fromArticleItemListViewer) {
	if (resourcePath == null || resourcePath == '') {
		return false;
	}
	
	LazyLoader.loadMultiple(['/dwr/engine.js', '/dwr/interface/ArticleItemInfoFetcher.js'], function() {
		ArticleItemInfoFetcher.getArticleInfo(resourcePath, {
			callback: function(info) {
				var parents = ArticleEditorHelper.getArticleContainer(styleClass, resourcePath);
				
				ArticleEditorHelper.updateArticleFields(info, parents);

				ArticleEditorHelper.addToolbarButtons(resourcePath, mode, parents, fromArticleItemListViewer, null);
			}
		});
	});
}

ArticleEditorHelper.getArticleContainer = function(styleClass, resourcePath) {
	var search = 'input';
	if (styleClass != null) {
		search += '.' + styleClass;
	}
	return jQuery(search + '[type=\'hidden\'][value=\''+ resourcePath +'\']').parent();
}

ArticleEditorHelper.updateArticleFields = function(info, parents) {
	if (info == null) {
		MOOdalBox.addEventToCloseAction(function() {
			reloadPage();
		});
		return false;
	}
	
	if (parents == null || parents.length == 0) {
		MOOdalBox.addEventToCloseAction(function() {
			reloadPage();
		});
		return false;
	}
	var container = null;
	for (var i = 0; i < parents.length; i++) {
		var container = jQuery(parents[i]);

		ArticleEditorHelper.setArticleFieldValue('title', info.header, container);
		ArticleEditorHelper.setArticleFieldValue('date', info.date, container);
		ArticleEditorHelper.setArticleFieldValue('author', info.author, container);
		ArticleEditorHelper.setArticleFieldValue('teaser', info.teaser, container);
		ArticleEditorHelper.setArticleFieldValue('body', info.body, container);
	}
}

ArticleEditorHelper.addToolbarButtons = function(resourcePath, previousAction, parents, fromArticleItemListViewer, callback) {
	if (previousAction == null || previousAction == 'edit' || parents == null || parents.length == 0) {
		if (callback) {
			callback();
		}
		
		return false;
	}
	
	ArticleItemInfoFetcher.getButtons(resourcePath, previousAction, fromArticleItemListViewer, {
		callback: function(buttons) {
			if (buttons != null) {
				for (var i = 0; i < parents.length; i++) {
					jQuery('div[class*=\'content_item_toolbar\']', jQuery(parents[i])).html(buttons);
				}
				ArticleEditorHelper.registerArticleLinksForMoodalBox();
			}
			
			if (callback) {
				callback();
			}
		}
	});
}

ArticleEditorHelper.setArticleFieldValue = function(identifier, value, container) {
	jQuery('div[class*=\'' + identifier + '\']', container).html(value == null ? '' : value);
}