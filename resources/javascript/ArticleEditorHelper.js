if (ArticleEditorHelper == null) var ArticleEditorHelper = {};

ArticleEditorHelper.needReload = false;

ArticleEditorHelper.initializeJavaScriptActionsForEditingAndCreatingArticles = function() {
	ArticleEditorHelper.registerArticleActions();
}

ArticleEditorHelper.registerArticleActions = function() {
	var editButtons = jQuery('a.edit');
	jQuery.each(editButtons, function() {
		ArticleEditorHelper.checkArticleLinkAndRegisterIfItsCorrect(this, 0.8);
	});
	
	var createButtons = jQuery('a.create');
	jQuery.each(createButtons, function() {
		ArticleEditorHelper.checkArticleLinkAndRegisterIfItsCorrect(this, 0.8);
	});

	var deleteButtons = jQuery('a.delete');
	jQuery.each(deleteButtons, function() {
		ArticleEditorHelper.checkArticleLinkAndRegisterIfItsCorrect(this, 0.35);
	});
}

ArticleEditorHelper.checkArticleLinkAndRegisterIfItsCorrect = function(link, windowResizeIndex) {
	if (link == null || link.length == 0) {
		return false;
	}
	
	if (jQuery(link).hasClass('articleEditorInitializedForLink')) {
		return false;
	}
	
	jQuery(link).addClass('articleEditorInitializedForLink');
	jQuery(link).fancybox({
		autoScale: false,
		autoDimensions: false,
		hideOnOverlayClick: false,
		width: windowinfo.getWindowWidth() * windowResizeIndex,
		height: windowinfo.getWindowHeight() * windowResizeIndex,
		onCloseCallback: function() {
			ArticleEditorHelper.addActionAfterArticleIsSavedAndEditorClosed();
		}
	});
}

ArticleEditorHelper.addActionAfterArticleIsSavedAndEditorClosed = function() {
	if (ArticleEditorHelper.needReload) {
		reloadPage();
	}
}

ArticleEditorHelper.closeAllObjects = function() {
	closeAllLoadingMessages();
	
	jQuery.fancybox.close();
}

ArticleEditorHelper.deleteSelectedArticle = function(resource, fromArticleList) {
	LucidEngine.deleteArticle(resource, {
		callback: function(result) {
			LazyLoader.loadMultiple(['/dwr/engine.js', '/dwr/interface/ArticleItemInfoFetcher.js'], function() {
				if (result) {
					var parents = ArticleEditorHelper.getArticleContainer(null, resource);
					if (fromArticleList) {
						var container = null;
						while ((parents != null && parents.length > 0) && container == null) {
							var uuids = jQuery('input.contentLisItemsIdentifierStyleClass[type=\'hidden\']', parents);
							
							if (uuids != null && uuids.length > 0) {
								container = jQuery(uuids[0]).parent();
							}
							else {
								parents = parents.parent();
							}
						}
						
						if (container == null) {
							ArticleEditorHelper.needReload = true;
							ArticleEditorHelper.closeAllObjects();
							return false;
						}
						ArticleEditorHelper.reloadArticlesList(jQuery(container).attr('id'), function() { ArticleEditorHelper.closeAllObjects(); });
					}
					else {
						var info = {header: '', body: '', teaser: '', date: '', author: ''};
						ArticleEditorHelper.updateArticleFields(info, parents);
						
						ArticleEditorHelper.addToolbarButtons(resource, 'delete', parents, false, function() { ArticleEditorHelper.closeAllObjects(); });
					}
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

ArticleEditorHelper.reloadArticlesList = function(id, callback) {
	if (id == null || id == '') {
		ArticleEditorHelper.needReload = true;
		return false;
	}
	
	var parentContainer = jQuery('#' + id);
	if (parentContainer == null || parentContainer.length == 0) {
		ArticleEditorHelper.needReload = true;
		return false;
	}
	
	var uuids = jQuery('input.contentLisItemsIdentifierStyleClass[type=\'hidden\']', parentContainer);
	if (uuids == null || uuids.length == 0) {
		ArticleEditorHelper.needReload = true;
		return false;
	}
	
	IWCORE.renderComponent(jQuery(uuids[0]).attr('value'), parentContainer[0], function() {
		ArticleEditorHelper.registerArticleActions();
		if (callback) {
			callback();
		}
	}, null);
	
	return false;
}

ArticleEditorHelper.reloadArticle = function(resourcePath, styleClass, mode) {
	if (resourcePath == null || resourcePath == '') {
		return false;
	}
	
	LazyLoader.loadMultiple(['/dwr/engine.js', '/dwr/interface/ArticleItemInfoFetcher.js'], function() {
		ArticleItemInfoFetcher.getArticleInfo(resourcePath, {
			callback: function(info) {
				var parents = ArticleEditorHelper.getArticleContainer(styleClass, resourcePath);
				
				ArticleEditorHelper.updateArticleFields(info, parents);

				ArticleEditorHelper.addToolbarButtons(resourcePath, mode, parents, false, null);
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
		ArticleEditorHelper.needReload = true;
		return false;
	}
	
	if (parents == null || parents.length == 0) {
		ArticleEditorHelper.needReload = true;
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
				ArticleEditorHelper.registerArticleActions();
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