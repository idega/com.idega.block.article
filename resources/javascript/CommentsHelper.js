var IS_COMMENT_PANEL_ADDED = false;

var USER = null;
var SUBJECT = null;
var BODY = null;

var LABEL_USER = null;
var LABEL_SUBJECT = null;
var LABEL_COMMENT = null;
var LABEL_POSTED = null;
var LABEL_SEND = null;
var LABEL_SENDING = null;

var LOGGED_USER = null;

var COMMENT_PANEL_ID = "comment_panel";

var EXISTS_COMMENTS_FILE = false;
var FIRST_TIME_ACTIVATION = true;

function setCommentValues(user, subject, body) {
	USER = user;
	SUBJECT = subject;
	BODY = body;
}

function addCommentPanel(id, linkToComments, lblUser, lblSubject, lblComment, lblPosted, lblSend, lblSending, loggedUser, existsFile) {
	setCommentValues("", "", "");
	LABEL_USER = lblUser;
	LABEL_SUBJECT = lblSubject;
	LABEL_COMMENT = lblComment;
	LABEL_POSTED = lblPosted;
	LABEL_SEND = lblSend;
	LABEL_SENDING = lblSending;
	LOGGED_USER = loggedUser;
	if (FIRST_TIME_ACTIVATION) {
		FIRST_TIME_ACTIVATION = false;
		EXISTS_COMMENTS_FILE = existsFile;
	}
	if (IS_COMMENT_PANEL_ADDED) {
		closeCommentsPanel();
		return;
	}
	if (id == null || linkToComments == null) {
		return;
	}
	var container = document.getElementById(id);
	if (container == null) {
		return;
	}
	container.appendChild(getCommentPane(linkToComments));
}

function getCommentPane(linkToComments) {
	var userId = "comment_user_value";
	var subjectId = "comment_subject_value";
	var bodyId = "comment_comment_value";
	
	var container = document.createElement("div");
	container.setAttribute("id", COMMENT_PANEL_ID);
	
	var table = document.createElement("table");
	table.setAttribute("class", "add_comment_table");
	var tbBody = document.createElement("tbody");
	
	// User
	tbBody.appendChild(createTableLine(LABEL_USER + ": ", userId, "text", LOGGED_USER, "comment_input_style"));

	// Subject
	tbBody.appendChild(createTableLine(LABEL_SUBJECT + ": ", subjectId, "text", "", "comment_input_style"));
	
	// Comment
	var bodyLine = document.createElement("tr");
	var bodyLabel = document.createElement("td");
	bodyLabel.appendChild(document.createTextNode(LABEL_COMMENT + ": "));
	bodyLine.appendChild(bodyLabel);
	var bodyInput = document.createElement("td");
	var bodyValue = document.createElement("textarea");
	bodyValue.setAttribute("id", bodyId);
	bodyValue.setAttribute("style", "comment_comment_style");
	bodyInput.appendChild(bodyValue);
	bodyLine.appendChild(bodyInput);
	tbBody.appendChild(bodyLine);
	
	// Send button
	var sendLine = document.createElement("tr");
	sendLine.setAttribute("cells", "2");
	var sendCell = document.createElement("td");
	var send = createInput("send_comment", "button", LABEL_SEND, "send_comment_button");
	if (typeof container.attachEvent != "undefined") {
		send.attachEvent('onclick', function(e){closeCommentPanelAndSendComment(userId, subjectId, bodyId, linkToComments);});
	} else {
		send.addEventListener('click', function(e){closeCommentPanelAndSendComment(userId, subjectId, bodyId, linkToComments);},false);
	}
	sendCell.appendChild(send);
	sendLine.appendChild(sendCell);
	tbBody.appendChild(sendLine);
	
	table.appendChild(tbBody);
	container.appendChild(table);
	
	IS_COMMENT_PANEL_ADDED = true;
	return container;
}

function closeCommentPanelAndSendComment(userId, subjectId, bodyId, linkToComments) {
	if (userId == null || subjectId == null || bodyId == null || linkToComments == null) {
		return;
	}
	var user = document.getElementById(userId);
	if (user == null) {
		return;
	}
	var subject = document.getElementById(subjectId);
	if (subject == null) {
		alert("subject");
		return;
	}
	var body = document.getElementById(bodyId);
	if (body == null) {
		return;
	}
	showLoadingMessage(LABEL_SENDING);
	closeCommentsPanel();
	setCommentValues(user.value, subject.value, body.value);
	CommentsEngine.addComment(USER, SUBJECT, BODY, linkToComments, EXISTS_COMMENTS_FILE, addCommentCallback);
}

function addCommentCallback(date) {
	if (date != null) {
		addCommentToPage(date);
		EXISTS_COMMENTS_FILE = true;
	}
	closeLoadingMessage();
}

function addCommentToPage(date) {
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
	var commentsList = document.getElementById("comments_block_list");
	if (commentsList == null) {
		commentsList = document.createElement("ol");
		commentsList.setAttribute("id", "comments_block_list");
		commentsContainer.appendChild(commentsList);
	}
	
	var commentContainer = document.createElement("li");
	commentContainer.setAttribute("id", "cmnt_" + commentIndex);
	var commentValue = document.createElement("dl");
	
	var user = document.createElement("dt");
	var userValue = document.createElement("span");
	userValue.appendChild(document.createTextNode(USER + ":"));
	user.appendChild(userValue);
	commentValue.appendChild(user);
	
	var subject = document.createElement("dd");
	var subjectValue = document.createElement("span");
	subjectValue.appendChild(document.createTextNode(SUBJECT));
	subject.appendChild(subjectValue);
	commentValue.appendChild(subject);
	
	var body = document.createElement("dd");
	var bodyValue = document.createElement("p");
	bodyValue.appendChild(document.createTextNode(BODY));
	body.appendChild(bodyValue);
	commentValue.appendChild(body);
	
	var posted = document.createElement("dd");
	var postedValue = document.createElement("span");
	postedValue.appendChild(document.createTextNode(LABEL_POSTED + ": " + date));
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
	labelCell.appendChild(document.createTextNode(cellLabel));
	line.appendChild(labelCell);
	var inputCell = document.createElement("td");
	inputCell.appendChild(createInput(inputId, inputType, inputValue, inputStyle));
	line.appendChild(inputCell);
	return line;
}
