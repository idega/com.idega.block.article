

var ArticleEditHelper = {};


ArticleEditHelper.LOADING_MSG = "";
ArticleEditHelper.init = function(uriToFileBrowser,selectedGroupsParameterName,articleCategoryParameterName,selectedCategories){
	tinyMCE.init({
		// General options
		mode : "textareas",
		theme : "advanced",
		plugins : "autolink,lists,pagebreak,style,layer,table,save,advhr,advimage,advlink,emotions,iespell,insertdatetime,preview,media,searchreplace,print,contextmenu,paste,directionality,fullscreen,noneditable,visualchars,nonbreaking,xhtmlxtras,template,inlinepopups,autosave",

		theme_advanced_buttons1 : "save,newdocument,|,bold,italic,underline,strikethrough,|,justifyleft,justifycenter,justifyright,justifyfull,|,styleselect,formatselect,fontselect,fontsizeselect,cut,copy,paste,pastetext,pasteword,|,search,replace",
		theme_advanced_buttons2 :"bullist,numlist,|,outdent,indent,blockquote,|,undo,redo,|,link,unlink,anchor,image,cleanup,help,code,|,insertdate, inserttime,preview,|,forecolor,backcolor,tablecontrols,|",
		theme_advanced_buttons3 : "hr,removeformat,visualaid,|,sub,sup,|,charmap,emotions,iespell,media,advhr,|,print,|,ltr,rtl,|,fullscreen,insertlayer,moveforward,movebackward,absolute,|,styleprops,|,cite,abbr,acronym,del,ins,attribs,|,visualchars,nonbreaking,template,pagebreak,restoredraft",
		theme_advanced_toolbar_location : "top",
		theme_advanced_toolbar_align : "left",
		theme_advanced_statusbar_location : "bottom",
		width : "100%",
		height : "400px",
		file_browser_callback : 'ArticleEditHelper.openFileBrowser',
		relative_urls: false

	});
	ArticleEditHelper.URI_TO_FILE_BROWSER = uriToFileBrowser;
	ArticleEditHelper.SELECTED_GROUPS_PARAMETER_NAME = selectedGroupsParameterName;
	ArticleEditHelper.ARTICLE_CATEGORY_PARAMETER_NAME = articleCategoryParameterName;
	var categoryInputs = jQuery("input[name=" + ArticleEditHelper.ARTICLE_CATEGORY_PARAMETER_NAME +"]");
	if((selectedCategories.length < 1) && (categoryInputs.length > 0)){
		jQuery(categoryInputs[0]).attr('checked', true);
	}
	for(var i = 0; i <selectedCategories.length;i++){
		categoryInputs.filter("[value=" + selectedCategories[i] + "]").attr('checked', true);
	}
		
//	tinyMCEPopup.onInit.add(ArticleEditHelper.FileBrowserDialogue.init, ArticleEditHelper.FileBrowserDialogue);
}

ArticleEditHelper.openFileBrowser = function(field_name, url, type, win) {

    tinyMCE.activeEditor.windowManager.open({
        file : ArticleEditHelper.URI_TO_FILE_BROWSER,
        title : 'My File Browser',
        width : "300px",//document.body.offsetWidth * 0.8,  // Your dimensions may differ - toy around with them!
        height : "150px",//document.body.offsetHeight * 0.8,
        resizable : "yes",
        inline : "yes",  // This parameter only has an effect if you use the inlinepopups plugin!
        close_previous : "no"
    }, {
        window : win,
        input : field_name
    });
    return false;
 }

ArticleEditHelper.saveAricle = function(idsObject){
	showLoadingMessage(ArticleEditHelper.LOADING_MSG);
	
	// Texts
	var saveValues = {};
	saveValues.articleUri = idsObject.articleUri;
	saveValues.headline = jQuery("#"+idsObject.headlineId).val();
	saveValues.body = tinyMCE.get(idsObject.bodyId).getContent();
	saveValues.teaser = tinyMCE.get(idsObject.teaserId).getContent();
	
	
	//categories
	var collectionValues = {};
	var selectedCategories = jQuery("[name="+ ArticleEditHelper.ARTICLE_CATEGORY_PARAMETER_NAME+"]").filter(":checked");
	var articleCategories = [];
	for(var i = 0;i<selectedCategories.length;i++){
		var input = jQuery(selectedCategories[i]);
		articleCategories.push(input.val());
	}
	collectionValues.articleCategories = articleCategories;
	
	// permissions
//	var selectedGroups = jQuery("[name="+ ArticleEditHelper.SELECTED_GROUPS_PARAMETER_NAME+"]").filter(":checked");
//	var permissionGroups = [];
//	for(var i = 0;i<selectedGroups.length;i++){
//		var input = jQuery(selectedGroups[i]);
//		permissionGroups.push(input.val());
//	}
	collectionValues.permissionGroups = GroupsFilter.getSelectedGroups();
	
	ArticleServices.saveArticle(saveValues,collectionValues,{
		callback : function(reply){
			if(reply.status != "success"){
				// Actions for saviong failure
				closeAllLoadingMessages();
				humanMsg.displayMsg(reply.message);
				return;
			}
			
			// Actions for saving success
			closeAllLoadingMessages();
			humanMsg.displayMsg(reply.message);
			return;
		},
		errorHandler:function(message) {
			closeAllLoadingMessages();
			alert(message);
		}
	});
	
}
