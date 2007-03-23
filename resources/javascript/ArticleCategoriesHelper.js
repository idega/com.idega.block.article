function setDisplayArticleCategory(element, pageKey, moduleId, savingMessage) {
	if (element == null || pageKey == null || moduleId == null) {
		return;
	}
	var category = element.name;
	if (category == null) {
		return;
	}
	showLoadingMessage(savingMessage);
	if (element.checked) {
		BuilderService.addPropertyToModule(pageKey, moduleId, "categories", category, setDisplayArticleCategoryCallback);
	}
	else {
		BuilderService.removeValueFromModuleProperty(pageKey, moduleId, "categories", category, setDisplayArticleCategoryCallback);
	}
}

function setDisplayArticleCategoryCallback(result) {
	refreshArticlePageFrame();
}

function refreshArticlePageFrame() {
	window.location.href = window.location.href;
	window.onload = closeLoadingMessage;
}