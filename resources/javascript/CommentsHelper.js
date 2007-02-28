var IS_COMMENT_PANEL_ADDED = false;
var SHOW_COMMENTS_LIST = false;
var SET_REVERSE_AJAX = false;
var NEED_TO_CHECK_COMMENTS_SIZE = true;
var NEED_TO_NOTIFY = false;

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

function setCommentValues(user, subject, email, body) {
	USER = user;
	SUBJECT = subject;
	EMAIL = email;
	BODY = body;
}

function addCommentPanel(id, linkToComments, lblUser, lblSubject, lblComment, lblPosted, lblSend, lblSending, loggedUser, lblEmail,
	lblCommentForm, addEmail) {
	enableReverseAjax();
	setCommentValues("", "", "", "");
	
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
	
	if (IS_COMMENT_PANEL_ADDED) {
		closeCommentsPanel();
		return;
	}
	if (id == null || getLinkToComments() == null) {
		return;
	}
	var container = document.getElementById(id);
	if (container == null) {
		return;
	}
	container.appendChild(getCommentPane(linkToComments, addEmail));
}

function getCommentPane(linkToComments, addEmail) {
	var userId = "comment_user_value";
	var subjectId = "comment_subject_value";
	var emailId = "comment_email_value";
	var bodyId = "comment_comment_value";
	var secretInputId= "secretCommentsInput";
	
	var container = document.createElement("div");
	container.setAttribute("id", COMMENT_PANEL_ID);
	
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
	if (addEmail) {
		tbBody.appendChild(createTableLine(LABEL_EMAIL + ":", emailId, "text", "", "comment_input_style"));
	}
	
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
	
	if (addEmail) {
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
	}
	
	// Send button
	var sendLine = document.createElement("tr");
	var sendCell = document.createElement("td");
	sendCell.setAttribute("colspan", "2");
	var send = createInput("send_comment", "button", LABEL_SEND, "send_comment_button");
	if (typeof container.attachEvent != "undefined") {
		send.attachEvent('onclick', function(e){closeCommentPanelAndSendComment(userId, subjectId, emailId, bodyId, linkToComments, secretInputId);});
	} else {
		send.addEventListener('click', function(e){closeCommentPanelAndSendComment(userId, subjectId, emailId, bodyId, linkToComments, secretInputId);},false);
	}
	sendCell.appendChild(send);
	sendLine.appendChild(sendCell);
	tbBody.appendChild(sendLine);
	
	table.appendChild(tbBody);
	fieldset.appendChild(table);
	
	IS_COMMENT_PANEL_ADDED = true;
	return container;
}

function closeCommentPanelAndSendComment(userId, subjectId, emailId, bodyId, linkToComments, secretInputId) {
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
	closeCommentsPanel();
	setCommentValues(user.value, subject.value, emailValue, body.value);
	CommentsEngine.addComment(getComponentCacheKey(), USER, SUBJECT, EMAIL, BODY, linkToComments, NEED_TO_NOTIFY, addCommentCallback);
}

function addCommentCallback(result) {
	closeLoadingMessage();
	if (result) {
		addAtomButtonForComments();
		CommentsEngine.getCommentsForAllPages(getLinkToComments(), getCommentsForAllPagesCallback);
	}
}

function getCommentsForAllPagesCallback(result) {
	closeLoadingMessage();
}

function addComment(articleComment) {
	var commentIndex = 0;
	var counter = document.getElementById("contentItemCount");
	if (counter != null) {
		var children = counter.childNodes;
		if (children != null) {
			var countValue = counter.childNodes[0];
			countValue.nodeValue++;
			commentIndex = countValue.nodeValue;
		}
	}
	
	var commentsContainer = document.getElementById("comments_block");
	if (commentsContainer == null) {
		return;
	}
	var commentsList = document.getElementById(COMMENTS_BLOCK_LIST_ID);
	if (commentsList == null) {
		commentsList = document.createElement("ol");
		commentsList.setAttribute("id", COMMENTS_BLOCK_LIST_ID);
		commentsContainer.appendChild(commentsList);
		if (SHOW_COMMENTS_LIST) {
			showCommentsList();
		}
		else {
			hideCommentsList();
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

function closeCommentsPanel() {
	var commentPanel = document.getElementById(COMMENT_PANEL_ID);
	if (commentPanel == null) {
		return;
	}
	var parentContainer = commentPanel.parentNode;
	if (parentContainer == null) {
		return;
	}
	parentContainer.removeChild(commentPanel);
	IS_COMMENT_PANEL_ADDED = false;
	NEED_TO_CHECK_COMMENTS_SIZE = false;
}

function getComments(linkToComments) {
	showLoadingMessage(getCommentsLoadingMessage());
	CommentsEngine.getComments(linkToComments, getCommentsCallback);
}

function getCommentsCallback(comments) {
	if (comments == null) {
		closeLoadingMessage();
		return;
	}
	removeCommentsList();
	for (var i = 0; i < comments.length; i++) {
		addComment(comments[i]);
	}
	closeLoadingMessage();
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

function removeCommentsList() {
	var counter = document.getElementById("contentItemCount");
	if (counter != null) {
		var children = counter.childNodes;
		if (children != null) {
			var countValue = counter.childNodes[0];
			countValue.nodeValue = 0;
		}
	}
	
	var commentsList = document.getElementById(COMMENTS_BLOCK_LIST_ID);
	if (commentsList == null) {
		return;
	}
	var parentContainer = commentsList.parentNode;
	if (parentContainer == null) {
		return;
	}
	parentContainer.removeChild(commentsList);
}

function hideCommentsList() {
	var list = document.getElementById(COMMENTS_BLOCK_LIST_ID);
	if (list != null) {
		list.style.display = "none";
	}
}

function showCommentsList() {
	var list = document.getElementById(COMMENTS_BLOCK_LIST_ID);
	if (list != null) {
		list.style.display = "block";
	}
}

function enableReverseAjax() {
	if (!SET_REVERSE_AJAX) {
		SET_REVERSE_AJAX = true;
		DWREngine.setActiveReverseAjax(true);
	}
}

function getCommentsList() {
	enableReverseAjax();
	if (SHOW_COMMENTS_LIST) {
		SHOW_COMMENTS_LIST = false;
		hideCommentsList();
	}
	else {
		SHOW_COMMENTS_LIST = true;
		getComments(getLinkToComments());
	}
}

function getCommentsSize() {
	if (NEED_TO_CHECK_COMMENTS_SIZE) {
		CommentsEngine.getCommentsCount(getLinkToComments(), getCommentsCountCallback);
	}
}

function getCommentsCountCallback(count) {
	if (count > 0) {
		addAtomButtonForComments();
	}
	setCommentsCount(count);
}

function setCommentsCount(count) {
	var counter = document.getElementById("contentItemCount");
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

function setNeedToNotify(object) {
	if ("comments_send_notifications" == object.id) {
		NEED_TO_NOTIFY = true;
	}
	else {
		NEED_TO_NOTIFY = false;
	}
}

function enableComments(enable, pageKey, moduleId, propName) {
	showLoadingMessage("Saving...");
	CommentsEngine.setModuleProperty(pageKey, moduleId, propName, enable, getComponentCacheKey(), setModulePropertyCallback);
}

function setModulePropertyCallback(result) {
	window.location.href=window.location.href;
	closeLoadingMessage();
}