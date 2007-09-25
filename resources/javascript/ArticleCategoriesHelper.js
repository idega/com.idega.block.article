function setDisplayArticleCategory(id, pageKey, moduleIds, savingMessage) {
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
	showLoadingMessage(savingMessage);
	if (element.checked) {
		BuilderService.addPropertyToModules(pageKey, moduleIds, "categories", categoryKey, setDisplayArticleCategoryCallback);
	}
	else {
		BuilderService.removeValueFromModuleProperty(pageKey, moduleIds, "categories", categoryKey, setDisplayArticleCategoryCallback);
	}
}

function setDisplayArticleCategoryCallback(result) {
	reloadPage();
}