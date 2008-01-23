try {
	window.addEvent('domready', function() {
		try {
			registerArticleLinksForMoodalBox();
		} catch(e) {}
	});
} catch(e) {};

function registerArticleLinksForMoodalBox() {
	var width = 800;
	var height = 600;
	MOOdalBox.init({resizeDuration: 50, evalScripts: true, initialWidth: width, initialHeight: height, contentsWidth: width, contentsHeight: height,
					defContentsWidth: width, defContentsHeight: height});
	
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
}

function addActionAfterArticleIsSavedAndEditorClosed() {
	MOOdalBox.addEventToCloseAction(function() {
		reloadPage();
	});
}