var IS_COMMENT_PANEL_ADDED = false;

var USER = "";
var SUBJECT = "";
var BODY = "";

var LABEL_USER = "User";
var LABEL_SUBJECT = "Subject";
var LABEL_COMMENT = "Comment";
var LABEL_POSTED = "Posted";
var LABEL_SEND = "Send";
var LABEL_SENDING = "Sending...";

var LOGGED_USER = "Anonymous";

var COMMENT_PANEL_ID = "comment_panel";

var LINK_TO_COMMENTS = "/files";

var COMMENTS_TIME_OUT = 60000; // One minute
var COMMENTS_LOADING_MESSAGE = "Loading comments...";

function setCommentValues(user, subject, body) {
	USER = user;
	SUBJECT = subject;
	BODY = body;
}

function addCommentPanel(id, linkToComments, lblUser, lblSubject, lblComment, lblPosted, lblSend, lblSending, loggedUser) {
	setCommentValues("", "", "");
	
	LABEL_USER = lblUser;
	LABEL_SUBJECT = lblSubject;
	LABEL_COMMENT = lblComment;
	LABEL_POSTED = lblPosted;
	LABEL_SEND = lblSend;
	LABEL_SENDING = lblSending;
	LOGGED_USER = loggedUser;
	LINK_TO_COMMENTS = linkToComments;
	
	if (IS_COMMENT_PANEL_ADDED) {
		closeCommentsPanel();
		return;
	}
	if (id == null || LINK_TO_COMMENTS == null) {
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
		return;
	}
	var body = document.getElementById(bodyId);
	if (body == null) {
		return;
	}
	showLoadingMessage(LABEL_SENDING);
	closeCommentsPanel();
	setCommentValues(user.value, subject.value, body.value);
	CommentsEngine.addComment(USER, SUBJECT, BODY, linkToComments, addCommentCallback);
}

function addCommentCallback(result) {
	closeLoadingMessage();
	if (result) {
		CommentsEngine.getCommentsForAllPages(LINK_TO_COMMENTS, getCommentsForAllPagesCallback);
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
	userValue.appendChild(document.createTextNode(articleComment.user + ":"));
	user.appendChild(userValue);
	commentValue.appendChild(user);
	
	var subject = document.createElement("dd");
	var subjectValue = document.createElement("span");
	subjectValue.appendChild(document.createTextNode(articleComment.subject));
	subject.appendChild(subjectValue);
	commentValue.appendChild(subject);
	
	var body = document.createElement("dd");
	var bodyValue = document.createElement("p");
	bodyValue.appendChild(document.createTextNode(articleComment.comment));
	body.appendChild(bodyValue);
	commentValue.appendChild(body);
	
	var posted = document.createElement("dd");
	var postedValue = document.createElement("span");
	postedValue.appendChild(document.createTextNode(LABEL_POSTED + ": " + articleComment.posted));
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

function getCommentsPeriodically(linkToComments) {
	var t = null;
	getComments(linkToComments);
	t = setTimeout("getCommentsPeriodically('"+linkToComments+"')", COMMENTS_TIME_OUT);
}

function getComments(linkToComments) {
	showLoadingMessage(COMMENTS_LOADING_MESSAGE);
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
	
	var commentsList = document.getElementById("comments_block_list");
	if (commentsList == null) {
		return;
	}
	var parentContainer = commentsList.parentNode;
	if (parentContainer == null) {
		return;
	}
	parentContainer.removeChild(commentsList);
}

function setPostedLabel(postedLabel) {
	LABEL_POSTED = postedLabel;
}

function setCommentsTimeOut(time) {
	COMMENTS_TIME_OUT = time;
}

function setCommentsLoadingMessage(message) {
	COMMENTS_LOADING_MESSAGE = message;
}

function showCommentsList() {
	getComments(LINK_TO_COMMENTS);
}

function setLinkToComments(linkToComments) {
	LINK_TO_COMMENTS = linkToComments;
}