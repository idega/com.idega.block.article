var IS_COMMENT_PANEL_ADDED = false;
var SHOW_COMMENTS_LIST = false;
var SET_REVERSE_AJAX = false;
var NEED_TO_CHECK_COMMENTS_SIZE = true;
var NEED_TO_NOTIFY = false;
var CHECKED_BOX_MANUALY = false;

var USER = "";
var SUBJECT = "";
var EMAIL = "";
var BODY = "";

var LABEL_USER = "User";
var LABEL_SUBJECT = "Subject";
var LABEL_COMMENT = "Comment";
var LABEL_SEND = "Send";
var LABEL_SENDING = "Sending...";
var LOGGED_USER = "Anonymous";
var LABEL_EMAIL = "E-mail";
var LABEL_COMMENT_FORM = "Comment form";

var COMMENT_PANEL_ID = "comment_panel";
var COMMENTS_BLOCK_LIST_ID = "comments_block_list";

var COMMENTS_POSTED_LABEL = "Posted";
var COMMENTS_MESSAGE = "Loading comments...";
var COMMENTS_ATOM_LINK_TITLE = "Atom Feed";
var COMMENTS_ATOMS_SERVER = "127.0.0.1";
var ADD_NOTIFICATION_TEXT = "Do You wish to receive notifications about new comments?";
var COMMENTS_YES = "Yes";
var COMMENTS_NO = "No";
var COMMENTS_ENTER_EMAIL = "Please enter Your e-mail!";
var COMMENTS_SAVING_TEXT = "Saving...";
var LINK_TO_ATOM_FEED_IMAGE = "/idegaweb/bundles/com.idega.block.article.bundle/resources/images/feed.png";
var LINK_TO_DELETE_COMMENTS_IMAGE = "/idegaweb/bundles/com.idega.block.article.bundle/resources/images/comments_delete.png";
var LINK_TO_DELETE_COMMENT_IMAGE = "/idegaweb/bundles/com.idega.block.article.bundle/resources/images/comment_delete.png";
var DELETING_MESSAGE_TEXT = "Deleting...";
var ARE_YOU_SURE_FOR_DELETING = "Are You sure?";
var DELETE_COMMENTS_LABEL = "Delete comments";
var DELETE_COMMENT_LABEL = "Delete this comment";

var HAS_COMMENT_VIEWER_VALID_RIGHTS = false;
var ADDED_LINK_TO_ATOM_IN_BODY = false;

var COMMENTS_LINK_TO_FILE = "/files";
var SHOW_COMMENTS_LIST_ON_LOAD = false;

var GLOBAL_COMMENTS_MARK_ID = null;
var ENABLE_REVERSE_AJAX_TIME_OUT_ID = 0;

/** Setters - getters  begins**/
function setPostedLabel(postedLabel) {
	COMMENTS_POSTED_LABEL = postedLabel;
}
function getPostedLabel() {
	return COMMENTS_POSTED_LABEL;
}

function setCommentsLoadingMessage(message) {
	COMMENTS_MESSAGE = message;
}
function getCommentsLoadingMessage() {
	return COMMENTS_MESSAGE;
}

function setCommentsAtomLinkTitle(title) {
	COMMENTS_ATOM_LINK_TITLE = title;
}
function getCommentsAtomLinkTitle() {
	return COMMENTS_ATOM_LINK_TITLE;
}

function setCommentsAtomsServer(atomServer) {
	COMMENTS_ATOMS_SERVER = atomServer;
}
function getCommentsAtomsServer() {
	return COMMENTS_ATOMS_SERVER;
}

function getAddNotificationText() {
	return ADD_NOTIFICATION_TEXT;
}
function setAddNotificationText(text) {
	ADD_NOTIFICATION_TEXT = text;
}

function getYesText() {
	return COMMENTS_YES;
}
function setYesText(text) {
	COMMENTS_YES = text;
}

function getNoText() {
	return COMMENTS_NO;
}
function setNoText(text) {
	COMMENTS_NO = text;
}

function getEnterEmailText() {
	return COMMENTS_ENTER_EMAIL;
}
function setEnterEmailText(text) {
	COMMENTS_ENTER_EMAIL = text;
}

function setCommentsSavingText(text) {
	COMMENTS_SAVING_TEXT = text;
}
function getCommentsSavingText() {
	return COMMENTS_SAVING_TEXT;
}

function setHasCommentViewerValidRights(rights) {
	HAS_COMMENT_VIEWER_VALID_RIGHTS = rights;
}
function getHasCommentViewerValidRights() {
	return HAS_COMMENT_VIEWER_VALID_RIGHTS;
}

function setLinkToAtomFeedImage(link) {
	LINK_TO_ATOM_FEED_IMAGE = link;
}
function getLinkToAtomFeedImage() {
	return LINK_TO_ATOM_FEED_IMAGE;
}

function setLinkToComments(link) {
	COMMENTS_LINK_TO_FILE = link;
}
function getLinkToComments() {
	return COMMENTS_LINK_TO_FILE;
}

function isShowCommentsListOnLoad() {
	return SHOW_COMMENTS_LIST_ON_LOAD;
}

function setCommentStartInfo(linkToComments, commentsId, showCommentsList) {
	COMMENTS_LINK_TO_FILE = linkToComments;
	GLOBAL_COMMENTS_MARK_ID = commentsId;
	SHOW_COMMENTS_LIST_ON_LOAD = showCommentsList;
}

function setLinkToDeleteImage(deleteImage) {
	LINK_TO_DELETE_COMMENTS_IMAGE = deleteImage;
}

function setDeletingCommentMessageText(text) {
	DELETING_MESSAGE_TEXT = text;
}

function setAreYouSureForDeletingComments(text) {
	ARE_YOU_SURE_FOR_DELETING = text;
}

function setDeleteCommentsLabel(text) {
	DELETE_COMMENTS_LABEL = text;
}

function setDeleteCommentLabel(text) {
	DELETE_COMMENT_LABEL = text;
}

function setDeleteCommentImage(link) {
	LINK_TO_DELETE_COMMENT_IMAGE = link;
}
/** Setters - getters ends**/

function setCommentValues(user, subject, email, body) {
	USER = user;
	SUBJECT = subject;
	EMAIL = email;
	BODY = body;
}

function addCommentPanel(id, linkToComments, lblUser, lblSubject, lblComment, lblPosted, lblSend, lblSending, loggedUser, lblEmail,
	lblCommentForm, addEmail, commentsId) {
	enableReverseAjax();
	setCommentValues("", "", "", "");
	refreshGlobalCommentsId(commentsId);
	
	LABEL_USER = lblUser;
	LABEL_SUBJECT = lblSubject;
	LABEL_COMMENT = lblComment;
	setPostedLabel(lblPosted);
	LABEL_SEND = lblSend;
	LABEL_SENDING = lblSending;
	LOGGED_USER = loggedUser;
	setLinkToComments(linkToComments);
	LABEL_EMAIL = lblEmail;
	LABEL_COMMENT_FORM = lblCommentForm;
	
	if (closeCommentsPanel(commentsId)) {
		return;
	}
	if (id == null || getLinkToComments() == null) {
		return;
	}
	var container = document.getElementById(id);
	if (container == null) {
		return;
	}
	container.appendChild(getCommentPane(linkToComments, addEmail, commentsId));
}

function getCommentPane(linkToComments, addEmail, commentsId) {
	var userId = "comment_user_value";
	var subjectId = "comment_subject_value";
	var emailId = "comment_email_value";
	var bodyId = "comment_comment_value";
	var secretInputId= "secretCommentsInput";
	
	var container = document.createElement("div");
	container.setAttribute("id", commentsId + COMMENT_PANEL_ID);
	
	var fieldset = document.createElement("fieldset");
	fieldset.setAttribute("class", "comment_fieldset");
	container.appendChild(fieldset);
	
	var legend = document.createElement("legend");
	legend.setAttribute("class", "comment_legend");
	legend.appendChild(document.createTextNode("Comment form"));
	fieldset.appendChild(legend);
	
	var table = document.createElement("table");
	table.setAttribute("class", "add_comment_table");
	var tbBody = document.createElement("tbody");
	
	var secretInput = document.createElement("input");
	secretInput.setAttribute("id", secretInputId);
	secretInput.setAttribute("class", "secretCommentsInputStyle");
	secretInput.setAttribute("value", "");
	secretInput.setAttribute("type", "text");
	fieldset.appendChild(secretInput);
	
	// User
	tbBody.appendChild(createTableLine(LABEL_USER + ":", userId, "text", LOGGED_USER, "comment_input_style"));

	// Subject
	tbBody.appendChild(createTableLine(LABEL_SUBJECT + ":", subjectId, "text", "", "comment_input_style"));
	
	//Email
	//if (addEmail) {
		tbBody.appendChild(createTableLine(LABEL_EMAIL + ":", emailId, "text", "", "comment_input_style"));
	//}
	
	// Comment
	var bodyLine = document.createElement("tr");
	var bodyLabel = document.createElement("td");
	bodyLabel.setAttribute("class", "comments_table_cell");
	bodyLabel.appendChild(document.createTextNode(LABEL_COMMENT + ":"));
	bodyLine.appendChild(bodyLabel);
	var bodyInput = document.createElement("td");
	var bodyValue = document.createElement("textarea");
	bodyValue.setAttribute("id", bodyId);
	bodyValue.setAttribute("class", "comment_comment_style");
	bodyValue.setAttribute("rows", "10");
	bodyValue.setAttribute("cols", "40");
	bodyInput.appendChild(bodyValue);
	bodyLine.appendChild(bodyInput);
	tbBody.appendChild(bodyLine);
	
	//if (addEmail) {
		NEED_TO_NOTIFY = false;
		
		var notifyLine = document.createElement("tr");
		var notifyCell = document.createElement("td");
		notifyCell.setAttribute("colspan", "2");
		
		var text = document.createElement("p");
		text.appendChild(document.createTextNode(getAddNotificationText()));
		text.appendChild(document.createElement("br"));

		var sendNotification = document.createElement("input");
		sendNotification.setAttribute("id", "comments_send_notifications");
		sendNotification.setAttribute("type", "radio");
		sendNotification.setAttribute("name", "comments_confirm_want_notifications");
		if (typeof container.attachEvent != "undefined") {
			sendNotification.attachEvent('onclick', function(e){setNeedToNotify(this);});
		} else {
			sendNotification.addEventListener('click', function(e){setNeedToNotify(this);},false);
		}
		text.appendChild(sendNotification);
		
		var sendNotificationLabel = document.createElement("label");
		sendNotificationLabel.setAttribute("for", "comments_send_notifications");
		sendNotificationLabel.appendChild(document.createTextNode(getYesText()));
		text.appendChild(sendNotificationLabel);
		
		var notSendNotification = document.createElement("input");
		notSendNotification.setAttribute("id", "comments_not_send_notifications");
		notSendNotification.setAttribute("type", "radio");
		notSendNotification.setAttribute("name", "comments_confirm_want_notifications");
		notSendNotification.setAttribute("checked", true);
		if (typeof container.attachEvent != "undefined") {
			notSendNotification.attachEvent('onclick', function(e){setNeedToNotify(this);});
		} else {
			notSendNotification.addEventListener('click', function(e){setNeedToNotify(this);},false);
		}
		text.appendChild(notSendNotification);
		
		var notSendNotificationLabel = document.createElement("label");
		notSendNotificationLabel.setAttribute("for", "comments_not_send_notifications");
		notSendNotificationLabel.appendChild(document.createTextNode(getNoText()));
		text.appendChild(notSendNotificationLabel);
		
		notifyCell.appendChild(text);
		notifyLine.appendChild(notifyCell);
		tbBody.appendChild(notifyLine);
	//}
	
	// Send button
	var sendLine = document.createElement("tr");
	var sendCell = document.createElement("td");
	sendCell.setAttribute("colspan", "2");
	var send = createInput("send_comment", "button", LABEL_SEND, "send_comment_button");
	if (typeof container.attachEvent != "undefined") {
		send.attachEvent('onclick', function(e){closeCommentPanelAndSendComment(userId, subjectId, emailId, bodyId, linkToComments, secretInputId, commentsId);});
	} else {
		send.addEventListener('click', function(e){closeCommentPanelAndSendComment(userId, subjectId, emailId, bodyId, linkToComments, secretInputId, commentsId);},false);
	}
	sendCell.appendChild(send);
	sendLine.appendChild(sendCell);
	tbBody.appendChild(sendLine);
	
	table.appendChild(tbBody);
	fieldset.appendChild(table);
	
	IS_COMMENT_PANEL_ADDED = true;
	return container;
}

function closeCommentPanelAndSendComment(userId, subjectId, emailId, bodyId, linkToComments, secretInputId, commentsId) {
	if (userId == null || subjectId == null || bodyId == null || linkToComments == null) {
		return;
	}
	var secretInput = document.getElementById(secretInputId);
	if (secretInput != null) {
		if (secretInput.value != "") { // Spam
			return;
		}
	}
	var user = document.getElementById(userId);
	if (user == null) {
		return;
	}
	var subject = document.getElementById(subjectId);
	if (subject == null) {
		return;
	}
	var emailValue = "";
	var email = document.getElementById(emailId);
	if (email != null) {
		emailValue = email.value;
	}
	var body = document.getElementById(bodyId);
	if (body == null) {
		return;
	}
	if (NEED_TO_NOTIFY) {
		if (emailValue == "") {
			alert(getEnterEmailText());
			return;
		}
	}
	showLoadingMessage(LABEL_SENDING);
	closeCommentsPanel(commentsId);
	setCommentValues(user.value, subject.value, emailValue, body.value);
	CommentsEngine.addComment(USER, SUBJECT, EMAIL, BODY, linkToComments, NEED_TO_NOTIFY, commentsId);
}

function addComment(articleComment, commentsId, linkToComments) {
	var commentIndex = 0;
	var counter = document.getElementById(commentsId + "contentItemCount");
	if (counter != null) {
		var children = counter.childNodes;
		if (children != null) {
			if (children.length > 0) {
				var countValue = counter.childNodes[0];
				countValue.nodeValue++;
				commentIndex = countValue.nodeValue;
			}
		}
	}
	
	var commentsContainer = document.getElementById(commentsId + "comments_block");
	if (commentsContainer == null) {
		return;
	}
	var commentsList = document.getElementById(commentsId + COMMENTS_BLOCK_LIST_ID);
	if (commentsList == null) {
		commentsList = document.createElement("ol");
		commentsList.setAttribute("id", commentsId + COMMENTS_BLOCK_LIST_ID);
		commentsContainer.appendChild(commentsList);
		if (needToShowCommentsList(commentsId)) {
			showCommentsList(commentsId);
		}
		else {
			hideCommentsList(commentsId);
		}
	}
	commentsList.setAttribute("class", "commens_list_all_items");
	
	var commentContainer = document.createElement("li");
	commentContainer.setAttribute("class", "comment_list_item");
	commentContainer.setAttribute("id", "cmnt_" + commentIndex);
	if (commentIndex == 1) {
		commentContainer.setAttribute("style", "margin: 0px;");
	}
	var commentValue = document.createElement("dl");
	
	if (HAS_COMMENT_VIEWER_VALID_RIGHTS) {
		var deleteImage = document.createElement("img");
		deleteImage.setAttribute("id", commentsId + "delete_article_comment" + articleComment.id);
		deleteImage.setAttribute("src", LINK_TO_DELETE_COMMENT_IMAGE);
		deleteImage.setAttribute("title", DELETE_COMMENT_LABEL);
		deleteImage.setAttribute("alt", DELETE_COMMENT_LABEL);
		deleteImage.setAttribute("name", DELETE_COMMENT_LABEL);
		deleteImage.setAttribute("class", "deleteCommentsImage");
		if (typeof commentsContainer.attachEvent == "undefined") {
			deleteImage.addEventListener("click", function(e){deleteComments(commentsId, articleComment.id, linkToComments);}, false);
		} else {
			deleteImage.attachEvent("onclick", function(e){deleteComments(commentsId, articleComment.id, linkToComments);});
		}
		commentContainer.appendChild(deleteImage);
	}
	
	var user = document.createElement("dt");
	var userValue = document.createElement("p");
	userValue.setAttribute("class", "comment_author_text");
	userValue.appendChild(document.createTextNode(articleComment.user + ":"));
	user.appendChild(userValue);
	commentValue.appendChild(user);
	
	var subject = document.createElement("dd");
	var subjectValue = document.createElement("p");
	subjectValue.appendChild(document.createTextNode(articleComment.subject));
	subject.appendChild(subjectValue);
	commentValue.appendChild(subject);
	
	var body = document.createElement("dd");
	var bodyValue = document.createElement("p");
	bodyValue.appendChild(document.createTextNode(articleComment.comment));
	body.appendChild(bodyValue);
	commentValue.appendChild(body);
	
	var posted = document.createElement("dd");
	var postedValue = document.createElement("p");
	postedValue.appendChild(document.createTextNode(getPostedLabel() + ": " + articleComment.posted));
	posted.appendChild(postedValue);
	commentValue.appendChild(posted);
	
	commentContainer.appendChild(commentValue);
	commentsList.appendChild(commentContainer);
}

function closeCommentsPanel(commentId) {
	var commentPanel = document.getElementById(commentId + COMMENT_PANEL_ID);
	if (commentPanel == null) {
		return false;
	}
	var parentContainer = commentPanel.parentNode;
	if (parentContainer == null) {
		return false;
	}
	parentContainer.removeChild(commentPanel);
	IS_COMMENT_PANEL_ADDED = false;
	NEED_TO_CHECK_COMMENTS_SIZE = false;
	return true;
}

function getComments(linkToComments, commentsId) {
	showLoadingMessage(getCommentsLoadingMessage());
	CommentsEngine.getComments(linkToComments, {
  		callback:function(comments) { // Passing extra parameters to callback
    		getCommentsCallback(comments, commentsId, linkToComments);
  		}
	});
}

function getCommentsCallback(comments, id, linkToComments) {
	closeLoadingMessage();
	if (comments == null) {
		closeLoadingMessage();
		return;
	}
	
	if (GLOBAL_COMMENTS_MARK_ID != null) {
		if (id != GLOBAL_COMMENTS_MARK_ID) {
			id = GLOBAL_COMMENTS_MARK_ID;
		}
	}
	
	if (comments.length == 0) {
		removeAtomAndDeleteButtonsForComments(id);
	}
	else {
		addAtomButtonForComments(id, linkToComments);
	}
	removeCommentsList(id);
	for (var i = 0; i < comments.length; i++) {
		addComment(comments[i], id, linkToComments);
	}
}

function createLabel(value, id, style) {
	var textLabel = document.createElement("label");
	textLabel.setAttribute("id", id);
	textLabel.setAttribute("class", style);
	textLabel.appendChild(document.createTextNode(value));
	return textLabel;
}

function createInput(id, type, value, style) {
	var input = document.createElement("input");
	input.setAttribute("id", id);
	if (style != null) {
		input.setAttribute("class", style);
	}
	if (type != null) {
		input.setAttribute("type", type);
	}
	input.setAttribute("value", value);
	return input;
}

function createTableLine(cellLabel, inputId, inputType, inputValue, inputStyle) {
	var line = document.createElement("tr");
	var labelCell = document.createElement("td");
	labelCell.setAttribute("class", "comments_table_cell");
	labelCell.appendChild(document.createTextNode(cellLabel));
	line.appendChild(labelCell);
	var inputCell = document.createElement("td");
	inputCell.appendChild(createInput(inputId, inputType, inputValue, inputStyle));
	line.appendChild(inputCell);
	return line;
}

function removeCommentsList(commentId) {
	var counter = document.getElementById(commentId + "contentItemCount");
	if (counter != null) {
		var children = counter.childNodes;
		if (children != null) {
			var countValue = counter.childNodes[0];
			countValue.nodeValue = 0;
		}
	}
	
	var commentsList = document.getElementById(commentId + COMMENTS_BLOCK_LIST_ID);
	if (commentsList == null) {
		return;
	}
	var parentContainer = commentsList.parentNode;
	if (parentContainer == null) {
		return;
	}
	parentContainer.removeChild(commentsList);
}

function hideCommentsList(commentsId) {
	var list = document.getElementById(commentsId + COMMENTS_BLOCK_LIST_ID);
	if (list != null) {
		list.style.display = "none";
	}
}

function showCommentsList(commentsId) {
	var list = document.getElementById(commentsId + COMMENTS_BLOCK_LIST_ID);
	if (list != null) {
		list.style.display = "block";
	}
}

function enableReverseAjax(isSafari) {
	if (isSafari) {
		window.clearTimeout(ENABLE_REVERSE_AJAX_TIME_OUT_ID);
	}
	if (!SET_REVERSE_AJAX) {
		SET_REVERSE_AJAX = true;
		DWREngine.setActiveReverseAjax(true);
	}
}

function getCommentsList(linkToComments, commentsId) {
	enableReverseAjax();
	refreshGlobalCommentsId(commentsId);
	if (needToShowCommentsList(commentsId)) {
		getComments(linkToComments, commentsId);
	}
	else {
		hideCommentsList(commentsId);
	}
}

function refreshGlobalCommentsId(commentsId) {
	if (GLOBAL_COMMENTS_MARK_ID != commentsId) {
		GLOBAL_COMMENTS_MARK_ID = commentsId;
	}
}

function setCommentsCount(count, commentId) {
	var counter = document.getElementById(commentId + "contentItemCount");
	if (counter == null) {
		return;
	}
	var children = counter.childNodes;
	if (children == null) {
		return;
	}
	counter.removeChild(counter.childNodes[0]);
	counter.appendChild(document.createTextNode(count));
}

function getCommentsCount(commentId) {
	var counter = document.getElementById(commentId + "contentItemCount");
	if (counter == null) {
		return 0;
	}
	var children = counter.childNodes;
	if (children == null) {
		return 0;
	}
	var count = children[0].nodeValue;
	if (count == null) {
		return 0;
	}
	return count;
}

function setNeedToNotify(object) {
	if ("comments_send_notifications" == object.id) {
		NEED_TO_NOTIFY = true;
	}
	else {
		NEED_TO_NOTIFY = false;
	}
}

function enableComments(enable, pageKey, moduleId, propName, cacheKey) {
	CHECKED_BOX_MANUALY = true;
	showLoadingMessage(getCommentsSavingText());
	CommentsEngine.setModuleProperty(pageKey, moduleId, propName, enable, cacheKey);
}

function hideOrShowComments() {
	CommentsEngine.hideOrShowComments(hideOrShowCommentsCallback);
}

function hideOrShowCommentsCallback(needToReload) {
	if (needToReload) {
		window.location.href = window.location.href;
	}
	closeLoadingMessage();
	if (getHasCommentViewerValidRights()) {
		if (CHECKED_BOX_MANUALY) {
			CHECKED_BOX_MANUALY = false; // Original page, no actions to perform
		}
		else {
			var checkBox = document.getElementById("manageCommentsBlockCheckBox"); // Marking as checked/unchecked
			if (checkBox != null) {
				checkBox.checked = !checkBox.checked;
			}
		}
	}
}

function getInitInfoForCommentsCallback(list) {
	if (list != null) {
		if (list.length == 16) {
			setPostedLabel(list[0]);
			setCommentsLoadingMessage(list[1]);
			setCommentsAtomLinkTitle(list[2]);
			setCommentsAtomsServer(list[3]);
			setAddNotificationText(list[4]);
			setYesText(list[5]);
			setNoText(list[6]);
			setEnterEmailText(list[7]);
			setCommentsSavingText(list[8]);
			setLinkToAtomFeedImage(list[9]);
			setLinkToDeleteImage(list[10]);
			setDeletingCommentMessageText(list[11]);
			setAreYouSureForDeletingComments(list[12]);
			setDeleteCommentsLabel(list[13]);
			setDeleteCommentLabel(list[14]);
			setDeleteCommentImage(list[15]);
		}
	}
	
	if (isShowCommentsListOnLoad()) {
		SHOW_COMMENTS_LIST = true;
		//getComments(getLinkToComments());
	}
	
	CommentsEngine.getUserRights(getUserRightsCallback);
}

function getUserRightsCallback(rights) {
	setHasCommentViewerValidRights(rights);
	if (isSafariBrowser()) {
		ENABLE_REVERSE_AJAX_TIME_OUT_ID = window.setTimeout("enableReverseAjax(true)", 2000);
	}
	else {
		enableReverseAjax(false);
	}
}

function removeElementFromParent(element) {
	if (element == null) {
		return;
	}
	var elementParent = element.parentNode;
	if (elementParent != null) {
		elementParent.removeChild(element);
	}
}

function removeAtomAndDeleteButtonsForComments(commentsId) {
	if (commentsId == null) {
		return;
	}
	var atomLinkId = commentsId + "article_comments_link_to_feed";
	removeElementFromParent(document.getElementById(atomLinkId));
	
	var deleteImageId = commentsId + "delete_article_comments";
	removeElementFromParent(document.getElementById(deleteImageId));
}

function addAtomButtonForComments(commentsId, linkToComments) {
	if (commentsId == null || linkToComments == null) {
		return;
	}
	var atomLinkId = commentsId + "article_comments_link_to_feed";
	var atomLink = document.getElementById(atomLinkId);
	if (atomLink != null) {
		return;
	}
	
	// Container
	var container = document.getElementById(commentsId + "article_comments_link_label_container");
	if (container == null) {
		return;
	}
	
	// Link
	var linkToFeed = document.createElement("a");
	linkToFeed.setAttribute("id", atomLinkId);
	linkToFeed.setAttribute("href", getCommentsAtomsServer() + linkToComments);
	linkToFeed.setAttribute("rel", "alternate");
	linkToFeed.setAttribute("type", "application/atom+xml");
		
	linkToFeed.appendChild(document.createTextNode(" "));
		
	// Image
	var image = document.createElement("img");
	image.setAttribute("src", getLinkToAtomFeedImage());
	image.setAttribute("title", getCommentsAtomLinkTitle());
	image.setAttribute("alt", getCommentsAtomLinkTitle());
	image.setAttribute("name", getCommentsAtomLinkTitle());
	linkToFeed.appendChild(image);
		
	container.appendChild(linkToFeed);
	
	if (HAS_COMMENT_VIEWER_VALID_RIGHTS) {
		container.appendChild(document.createTextNode(" "));
		
		var deleteImage = document.createElement("img");
		deleteImage.setAttribute("id", commentsId + "delete_article_comments");
		deleteImage.setAttribute("src", LINK_TO_DELETE_COMMENTS_IMAGE);
		deleteImage.setAttribute("title", DELETE_COMMENTS_LABEL);
		deleteImage.setAttribute("alt", DELETE_COMMENTS_LABEL);
		deleteImage.setAttribute("name", DELETE_COMMENTS_LABEL);
		deleteImage.setAttribute("class", "deleteCommentsImage");
		if (typeof container.attachEvent == "undefined") {
			deleteImage.addEventListener("click", function(e){deleteComments(commentsId, null, linkToComments);}, false);
		} else {
			deleteImage.attachEvent("onclick", function(e){deleteComments(commentsId, null, linkToComments);});
		}
		
		container.appendChild(deleteImage);
	}
}

function setAddedLinkToAtomInBody(added) {
	ADDED_LINK_TO_ATOM_IN_BODY = added;
}

function deleteComments(id, commentId, linkToComments) {
	var confirmed = confirm(ARE_YOU_SURE_FOR_DELETING);
	if (!confirmed) {
		return;
	}
	showLoadingMessage(DELETING_MESSAGE_TEXT);
	CommentsEngine.deleteComments(id, commentId, linkToComments, deleteCommentsCallback);
}

function deleteCommentsCallback(result) {
	closeLoadingMessage();
	if (result == null) {
		return;
	}
	if (result.length == 2) {
		CommentsEngine.getCommentsForAllPages(result[1], result[0]);
	}
}

function needToShowCommentsList(commentsId) {
	var list = document.getElementById(commentsId + COMMENTS_BLOCK_LIST_ID);
	if (list == null) {
		return true;
	}
	if (list.style.display == "block") {
		return false;
	}
	return true;
}