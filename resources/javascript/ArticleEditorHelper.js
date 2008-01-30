try {
	window.addEvent('domready', function() {
		try {
			registerArticleLinksForMoodalBox();
		} catch(e) {}
	});
} catch(e) {};

function registerArticleLinksForMoodalBox() {
	$$('a.edit').each(
		function(element) {
			if (element.getProperty('rel') == 'moodalbox') {
				MOOdalBox.register(element);
			}
		}
	);
	$$('a.create').each(
		function(element) {
			if (element.getProperty('rel') == 'moodalbox') {
				MOOdalBox.register(element);
			}
		}
	);
	$$('a.delete').each(
		function(element) {
			if (element.getProperty('rel') == 'moodalbox') {
				MOOdalBox.register(element);
			}
		}
	);
}

function addActionAfterArticleIsSavedAndEditorClosed() {
	MOOdalBox.addEventToCloseAction(function() {
		reloadPage();
	});
}

function deleteSelectedArticle(resource) {
	ThemesEngine.deleteArticle(resource, {
		callback: function(result) {
			MOOdalBox.close();
			reloadPage();
		}
	});
}