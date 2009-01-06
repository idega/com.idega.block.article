function setDisplayArticleCategory(id, pageKey, moduleIds, savingMessage, cacheKey) {
	if (id == null || pageKey == null || moduleIds == null) {
		return false;
	}
	var element = document.getElementById(id);
	if (element == null) {
		return false;
	}
	
	var categoryKey = element.name;
	if (categoryKey == null) {
		return false;
	}
	
	LazyLoader.loadMultiple(['/dwr/engine.js', '/dwr/interface/BuilderService.js'], function() {
		showLoadingMessage(savingMessage);
		if (element.checked) {
			BuilderService.addPropertyToModules(pageKey, moduleIds, "categories", categoryKey, {
				callback: function(result) {
					setDisplayArticleCategoryCallback(result, cacheKey);
				}
			});
		}
		else {
			BuilderService.removeValueFromModulesProperties(pageKey, moduleIds, "categories", categoryKey, {
				callback: function(result) {
					setDisplayArticleCategoryCallback(result, cacheKey);
				}
			});
		}
	});
}

function setDisplayArticleCategoryCallback(result, cacheKey) {
	if (result) {
		BuilderService.removeBlockObjectFromCacheByCacheKey(cacheKey, {
			callback: function(result) {
				reloadPage();
			}
		});
	}
	else {
		reloadPage();
	}
}