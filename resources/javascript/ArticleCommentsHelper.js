if (CommentsViewer == null) var CommentsViewer = {};

CommentsViewer.localizations = {
	posted: 'Posted',
	loadingComments: 'Loading comments...',
	atomLink: 'Atom Feed',
	addNotification: 'Do you wish to receive notifications about new comments?',
	yes: 'Yes',
	no: 'No',
	enterEmail: 'Please enter your e-mail!',
	saving: 'Saving...',
	deleting: 'Deleting...',
	areYouSure: 'Are you sure?',
	deleteComments: 'Delete comments',
	deleteComment: 'Delete this comment'
}

CommentsViewer.info = {
	commentsServer: '127.0.0.1',
	feedImage: '/idegaweb/bundles/com.idega.block.article.bundle/resources/images/feed.png',
	deleteImage: '/idegaweb/bundles/com.idega.block.article.bundle/resources/images/comments_delete.png',
	deleteCommentImage: '/idegaweb/bundles/com.idega.block.article.bundle/resources/images/comment_delete.png',
	hasValidRights: false
}

CommentsViewer.commentInfo = [];

CommentsViewer.setLocalization = function(localizations) {
	if (localizations == null) {
		return false;
	}
	
	CommentsViewer.localizations = localizations;
}

CommentsViewer.setStartInfo = function(startInfo) {
	if (startInfo == null) {
		return false;
	}
	
	CommentsViewer.info = startInfo;
}

var IS_COMMENT_PANEL_ADDED = false;
var SET_REVERSE_AJAX = false;
var NEED_TO_CHECK_COMMENTS_SIZE = true;
var NEED_TO_NOTIFY = false;
var CHECKED_BOX_MANUALY = false;

var USER = '';
var SUBJECT = '';
var EMAIL = '';
var BODY = '';

var LABEL_USER = 'User';
var LABEL_SUBJECT = 'Subject';
var LABEL_COMMENT = 'Comment';
var LABEL_SEND = 'Send';
var LABEL_SENDING = 'Sending...';
var LOGGED_USER = 'Anonymous';
var LABEL_EMAIL = 'E-mail';
var LABEL_COMMENT_FORM = 'Comment form';

var COMMENT_PANEL_ID = 'comment_panel';
var COMMENTS_BLOCK_LIST_ID = 'comments_block_list';

var GLOBAL_COMMENTS_MARK_ID = null;
var ENABLE_REVERSE_AJAX_TIME_OUT_ID = 0;

function addCommentStartInfo(linkToComments, commentsId, showCommentsListOnLoad, newestEntriesOnTop, springBeanIdentifier, identifier) {
	var commentsInfo = new SingleCommentInfo(linkToComments, commentsId, springBeanIdentifier, identifier, showCommentsListOnLoad, newestEntriesOnTop);
	CommentsViewer.commentInfo.push(commentsInfo);

	if (showCommentsListOnLoad) {
		getComments(linkToComments, commentsId);
	}
}

function SingleCommentInfo(linkToComments, commentsId, springBeanIdentifier, identifier, showCommentsList, newestEntriesOnTop) {
	this.linkToComments = linkToComments;
	this.commentsId = commentsId;
	this.springBeanIdentifier = springBeanIdentifier;
	this.identifier = identifier;
	
	this.showCommentsList = showCommentsList;
	this.newestEntriesOnTop = newestEntriesOnTop;
	
	this.commentsListOpened = false;
}

function setCommentValues(user, subject, email, body) {
	USER = user;
	SUBJECT = subject;
	EMAIL = email;
	BODY = body;
}

function addCommentPanel(id, linkToComments, lblUser, lblSubject, lblComment, lblPosted, lblSend, lblSending, loggedUser, lblEmail,
	lblCommentForm, addEmail, commentsId, instanceId, springBeanIdentifier, identifier, newestEntriesOnTop) {
	enableReverseAjax();
	setCommentValues('', '', '', '');
	refreshGlobalCommentsId(commentsId);
	
	LABEL_USER = lblUser;
	LABEL_SUBJECT = lblSubject;
	LABEL_COMMENT = lblComment;
	CommentsViewer.localizations.posted = lblPosted;
	LABEL_SEND = lblSend;
	LABEL_SENDING = lblSending;
	LOGGED_USER = loggedUser;
	LABEL_EMAIL = lblEmail;
	LABEL_COMMENT_FORM = lblCommentForm;
	
	if (closeCommentsPanel(commentsId)) {
		return false;
	}
	if (id == null || linkToComments == null) {
		return false;
	}
	var container = $(id);
	if (container == null) {
		return false;
	}
	container.appendChild(getCommentPane(linkToComments, addEmail, commentsId, instanceId, springBeanIdentifier, identifier, newestEntriesOnTop));
}

function getCommentPane(linkToComments, addEmail, commentsId, instanceId, springBeanIdentifier, identifier, newestEntriesOnTop) {
	var userId = 'comment_user_value';
	var subjectId = 'comment_subject_value';
	var emailId = 'comment_email_value';
	var bodyId = 'comment_comment_value';
	var secretInputId= 'secretCommentsInput';
	
	var container = new Element('div');
	container.setProperty('id', commentsId + COMMENT_PANEL_ID);
	container.addClass('commentsContainer');
	
	var fieldset = new Element('fieldset');
	fieldset.addClass('comment_fieldset');
	container.appendChild(fieldset);
	
	var legend = new Element('legend');
	legend.addClass('comment_legend');
	legend.appendText(LABEL_COMMENT_FORM);
	fieldset.appendChild(legend);
	
	var mainCommentContainer = new Element('div');
	fieldset.appendChild(mainCommentContainer);
	
	var table = new Element('table');
	table.addClass('add_comment_table');
	var tbBody = new Element('tbody');
	
	var secretInput = new Element('input');
	secretInput.setProperty('id', secretInputId);
	secretInput.addClass('secretCommentsInputStyle');
	secretInput.setProperty('value', '');
	secretInput.setProperty('type', 'text');
	mainCommentContainer.appendChild(secretInput);
	
	table.appendChild(tbBody);
	mainCommentContainer.appendChild(table);
	
	// User
	tbBody.appendChild(createTableLine(LABEL_USER + ':', userId, 'text', LOGGED_USER, 'comment_input_style'));

	// Subject
	tbBody.appendChild(createTableLine(LABEL_SUBJECT + ':', subjectId, 'text', '', 'comment_input_style'));
	
	//Email
	//if (addEmail) {
		tbBody.appendChild(createTableLine(LABEL_EMAIL + ':', emailId, 'text', '', 'comment_input_style'));
	//}
	
	// Comment
	var bodyLine = new Element('tr');
	var bodyLabel = new Element('td');
	bodyLabel.addClass('comments_table_cell');
	bodyLabel.appendText(LABEL_COMMENT + ':');
	bodyLine.appendChild(bodyLabel);
	var bodyInput = new Element('td');
	var bodyValue = new Element('textarea');
	bodyValue.setProperty('id', bodyId);
	bodyValue.addClass('comment_comment_style');
	bodyValue.setProperty('rows', '10');
	bodyValue.setProperty('cols', '40');
	bodyInput.appendChild(bodyValue);
	bodyLine.appendChild(bodyInput);
	tbBody.appendChild(bodyLine);
	
	//if (addEmail) {
		NEED_TO_NOTIFY = false;
		
		var needToNotifyContainer = new Element('div');
		needToNotifyContainer.addClass('commentsNotify');
		
		var notificationTextContainer = new Element('div');
		notificationTextContainer.appendText(CommentsViewer.localizations.addNotification);
		needToNotifyContainer.appendChild(notificationTextContainer);
		
		needToNotifyContainer.appendChild(new Element('div').addClass('spacer'));

		var sendNotificationContainer = new Element('div');
		sendNotificationContainer.addClass('commentsSendNotification');
		var sendNotification = new Element('input');
		sendNotification.setProperty('id', 'comments_send_notifications');
		sendNotification.setProperty('type', 'radio');
		sendNotification.setProperty('name', 'comments_confirm_want_notifications');
		sendNotification.addEvent('click', function() {
			setNeedToNotify('comments_send_notifications', 'comments_not_send_notifications');
		});
		sendNotificationContainer.appendChild(sendNotification);
		
		var sendNotificationLabel = new Element('label');
		sendNotificationLabel.setProperty('for', 'comments_send_notifications');
		sendNotificationLabel.appendText(CommentsViewer.localizations.yes);
		sendNotificationContainer.appendChild(sendNotificationLabel);
		
		var notSendNotification = new Element('input');
		notSendNotification.setProperty('id', 'comments_not_send_notifications');
		notSendNotification.setProperty('type', 'radio');
		notSendNotification.setProperty('name', 'comments_confirm_want_notifications');
		if (IE) {
			notSendNotification.setProperty('defaultChecked', true);
		}
		else {
			notSendNotification.setProperty('checked', true);
		}
		notSendNotification.addEvent('click', function() {
			setNeedToNotify('comments_not_send_notifications', 'comments_send_notifications');
		});
		sendNotificationContainer.appendChild(notSendNotification);
		
		var notSendNotificationLabel = new Element('label');
		notSendNotificationLabel.setProperty('for', 'comments_not_send_notifications');
		notSendNotificationLabel.appendText(CommentsViewer.localizations.no);
		sendNotificationContainer.appendChild(notSendNotificationLabel);
		
		needToNotifyContainer.appendChild(sendNotificationContainer);
		mainCommentContainer.appendChild(needToNotifyContainer);
	//}
	
	// Send button
	var sendButtonContainer = new Element('div');
	sendButtonContainer.setStyle('float', 'right');
	var send = createInput('send_comment', 'button', LABEL_SEND, 'send_comment_button');
	send.addEvent('click', function() {
		closeCommentPanelAndSendComment(userId, subjectId, emailId, bodyId, linkToComments, secretInputId, commentsId, instanceId, springBeanIdentifier,
		identifier, newestEntriesOnTop);
	});
	sendButtonContainer.appendChild(send);
	mainCommentContainer.appendChild(sendButtonContainer);
	
	if (isSafariBrowser()) {
		var fakeInput = new Element('input');
		fakeInput.setProperty('type', 'text');
		fakeInput.setStyle('visibility', 'hidden');
		fieldset.appendChild(fakeInput);
	}
	
	IS_COMMENT_PANEL_ADDED = true;
	return container;
}

function closeCommentPanelAndSendComment(userId, subjectId, emailId, bodyId, linkToComments, secretInputId, commentsId, instanceId, springBeanIdentifier,
										identifier, newestEntriesOnTop) {
	if (userId == null || subjectId == null || bodyId == null || linkToComments == null) {
		return false;
	}
	var secretInput = $(secretInputId);
	if (secretInput != null) {
		if (secretInput.value != '') { // Spam
			return false;
		}
	}
	var user = $(userId);
	if (user == null) {
		return false;
	}
	var subject = $(subjectId);
	if (subject == null) {
		return false;
	}
	var emailValue = '';
	var email = $(emailId);
	if (email != null) {
		emailValue = email.value;
	}
	var body = $(bodyId);
	if (body == null) {
		return false;
	}
	if (NEED_TO_NOTIFY) {
		if (emailValue == '') {
			alert(CommentsViewer.localizations.enterEmail);
			return false;
		}
	}
	showLoadingMessage(LABEL_SENDING);
	closeCommentsPanel(commentsId);
	setCommentValues(user.value, subject.value, emailValue, body.value);
	var commentProperties = new CommentsViewerProperties(USER, SUBJECT, EMAIL, BODY, linkToComments, commentsId, instanceId, springBeanIdentifier, identifier,
								NEED_TO_NOTIFY, newestEntriesOnTop);
	GLOBAL_COMMENTS_MARK_ID = commentsId;
	CommentsEngine.addComment(commentProperties, {
		callback: function(result) {
			closeAllLoadingMessages();
		}
	});
}

function CommentsViewerProperties(user, subject, email, body, uri, id, instanceId, springBeanIdentifier, identifier, notify, newestEntriesOnTop) {
	this.user = user || null;
	this.subject = subject || null;
	this.email = email || null;
	this.body = body || null;
	this.uri = uri || null;
	this.id = id || null;
	this.instanceId = instanceId || null;
	this.springBeanIdentifier = springBeanIdentifier || null;
	this.identifier = identifier || null;
	
	this.notify = notify || true;
	this.newestEntriesOnTop = newestEntriesOnTop || false;
}

function changeCommentsCount(linkId, change, finalCount) {
	var link = $(linkId + 'CommentsLabelWithCount');
	if (link == null) {
		return false;
	}
	
	var text = link.getText();
	if (text == null) {
		return false;
	}
	
	var textParts = text.split('(');
	if (textParts == null && textParts.length < 2) {
		return false;
	}	

	var countPart = textParts[1];
	var count = countPart.substring(0, countPart.length - 1);
	if (count == null || count == '') {
		return false;
	}
	
	if (finalCount == null && change != null) {
		var counter = count.toInt();
		counter += change;
	}
	else {
		counter = finalCount;
	}
	
	text = textParts[0] + '(' + counter + ')';
	link.setText(text);
}

function addComment(index, articleComment, commentsId, linkToComments) {
	var commentsContainer = $(commentsId + 'comments_block');
	if (commentsContainer == null) {
		return false;
	}
	var commentsList = $(commentsId + COMMENTS_BLOCK_LIST_ID);
	if (commentsList == null) {
		commentsList = new Element('ul');
		commentsList.setProperty('id', commentsId + COMMENTS_BLOCK_LIST_ID);
		commentsContainer.appendChild(commentsList);
		if (needToShowCommentsList(commentsId)) {
			showCommentsList(commentsId);
		}
		else {
			hideCommentsList(commentsId);
		}
	}
	commentsList.addClass('commens_list_all_items');
	
	var commentContainer = new Element('li');
	commentContainer.addClass('comment_list_item');
	if (index % 2 == 0) {
		commentContainer.addClass('even');
	}
	else {
		commentContainer.addClass('odd');
	}
	commentContainer.setProperty('id', 'cmnt_' + index);
	if (index == 0) {
		commentContainer.setProperty('style', 'margin: 0px;');
	}
	
	var commentValue = new Element('div');
	commentValue.addClass('commentItem');
	
	if (CommentsViewer.info.hasValidRights) {
		var deleteImage = new Element('img');
		deleteImage.setProperty('id', commentsId + 'delete_article_comment' + articleComment.id);
		deleteImage.setProperty('src', CommentsViewer.info.deleteCommentImage);
		deleteImage.setProperty('title', CommentsViewer.localizations.deleteComment);
		deleteImage.setProperty('alt', CommentsViewer.localizations.deleteComment);
		deleteImage.setProperty('name', CommentsViewer.localizations.deleteComment);
		deleteImage.addClass('deleteCommentsImage');
		deleteImage.addEvent('click', function() {
			deleteComments(commentsId, articleComment.id, linkToComments, getCommentsInfo(linkToComments).newestEntriesOnTop);
		});
		commentContainer.appendChild(deleteImage);
	}
	
	var number = new Element('div');
	number.addClass('commentItemNumber');
	number.appendText(articleComment.listNumber + '.');
	commentValue.appendChild(number);
	
	var subject = new Element('div');
	subject.addClass('commentItemSubject');
	subject.appendText(articleComment.subject);
	commentValue.appendChild(subject);
	
	var body = new Element('div');
	body.addClass('commentItemComment');
	body.appendText(articleComment.comment);
	commentValue.appendChild(body);
	
	var user = new Element('div');
	user.addClass('commentItemAuthor');
	user.appendText(articleComment.user);
	commentValue.appendChild(user);
	
	var posted = new Element('div');
	posted.addClass('commentItemCreated');
	posted.appendText(articleComment.posted);
	commentValue.appendChild(posted);
	
	commentContainer.appendChild(commentValue);
	commentsList.appendChild(commentContainer);
}

function closeCommentsPanel(commentId) {
	var commentPanel = $(commentId + COMMENT_PANEL_ID);
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

function getCommentsInfo(linkToComments) {
	if (linkToComments == null) {
		return null;
	}
	
	if (CommentsViewer.commentInfo == null || CommentsViewer.commentInfo.length == 0) {
		return null;
	}
	
	for (var i = 0; i < CommentsViewer.commentInfo.length; i++) {
		if (CommentsViewer.commentInfo[i].linkToComments == linkToComments) {
			return CommentsViewer.commentInfo[i];
		}
	}
	
	return null;
}

function getCommentsInfoById(id) {
	if (id == null) {
		return null;
	}
	
	if (CommentsViewer.commentInfo == null || CommentsViewer.commentInfo.length == 0) {
		return null;
	}
	
	for (var i = 0; i < CommentsViewer.commentInfo.length; i++) {
		if (CommentsViewer.commentInfo[i].commentsId == id) {
			return CommentsViewer.commentInfo[i];
		}
	}
	
	return null;
}

function getAllComments() {
	if (CommentsViewer.commentInfo == null) {
		return false;
	}
	
	var properties = new Array();
	for (var i = 0; i < CommentsViewer.commentInfo.length; i++) {
		properties.push({id: CommentsViewer.commentInfo[i].linkToComments, value: CommentsViewer.commentInfo[i].newestEntriesOnTop});
	}
	
	showLoadingMessage(CommentsViewer.localizations.loadingComments);
	CommentsEngine.getCommentsFromUris(properties, {
		callback: function(allComments) {
			closeAllLoadingMessages();
			
			if (allComments != null) {
				if (allComments.length == CommentsViewer.commentInfo.length) {
					for (var i = 0; i < allComments.length; i++) {
						getCommentsCallback(allComments[i], CommentsViewer.commentInfo[i].commentsId, CommentsViewer.commentInfo[i].linkToComments,
											CommentsViewer.commentInfo[i].newestEntriesOnTop);
					}
				}
			}
		}
	});
}

function getComments(linkToComments, commentsId) {
	var commentsInfo = getCommentsInfo(linkToComments);
	if (commentsInfo == null) {
		return false;
	}
	
	showLoadingMessage(CommentsViewer.localizations.loadingComments);
	var newestEntriesOnTop = commentsInfo.newestEntriesOnTop;
	CommentsEngine.getComments(new CommentsViewerProperties(null, null, null, null, linkToComments, commentsId, null, commentsInfo.springBeanIdentifier,
															commentsInfo.identifier, true, newestEntriesOnTop), {
  		callback:function(comments) {
    		getCommentsCallback(comments, commentsId, linkToComments, newestEntriesOnTop);
    		
    		enableReverseAjax();
  		}
	});
}

function getCommentsCallback(comments, id, linkToComments, newestEntriesOnTop) {
	closeAllLoadingMessages();
	if (comments == null) {
		removeCommentsList(id, 0);
		closeAllLoadingMessages();
		return false;
	}
	
	removeCommentsList(id, comments.length);
	for (var i = 0; i < comments.length; i++) {
		addComment(i, comments[i], id, linkToComments, newestEntriesOnTop);
	}
}

function createLabel(value, id, style) {
	var textLabel = new Element('label');
	textLabel.setProperty('id', id);
	textLabel.addClass(style);
	textLabel.appendText(value);
	return textLabel;
}

function createInput(id, type, value, style) {
	var input = new Element('input');
	input.setProperty('id', id);
	if (style != null) {
		input.addClass(style);
	}
	if (type != null) {
		input.setProperty('type', type);
	}
	input.setProperty('value', value);
	return input;
}

function createTableLine(cellLabel, inputId, inputType, inputValue, inputStyle) {
	var line = new Element('tr');
	var labelCell = new Element('td');
	labelCell.addClass('comments_table_cell');
	labelCell.appendText(cellLabel);
	line.appendChild(labelCell);
	var inputCell = new Element('td');
	inputCell.appendChild(createInput(inputId, inputType, inputValue, inputStyle));
	line.appendChild(inputCell);
	return line;
}

function removeCommentsList(commentId, totalComments) {
	changeCommentsCount(commentId, null, totalComments);
	
	var commentsList = $(commentId + COMMENTS_BLOCK_LIST_ID);
	if (commentsList == null) {
		return false;
	}
	var parentContainer = commentsList.parentNode;
	if (parentContainer == null) {
		return false;
	}
	parentContainer.removeChild(commentsList);
}

function hideCommentsList(commentsId) {
	var list = $(commentsId + COMMENTS_BLOCK_LIST_ID);
	if (list != null) {
		list.style.display = 'none';
	}
	
	var commentInfo = getCommentsInfoById(commentsId);
	if (commentInfo != null) {
		commentInfo.commentsListOpened = false;
	}
}

function showCommentsList(commentsId) {
	var list = $(commentsId + COMMENTS_BLOCK_LIST_ID);
	if (list != null) {
		list.style.display = 'block';
	}
	
	var commentInfo = getCommentsInfoById(commentsId);
	if (commentInfo != null) {
		commentInfo.commentsListOpened = true;
	}
}

function enableReverseAjax() {
	if (!SET_REVERSE_AJAX) {
		if (isSafariBrowser()) {
			ENABLE_REVERSE_AJAX_TIME_OUT_ID = window.setTimeout(setReverseAjaxForComments, 2000);
		}
		else {
			setReverseAjaxForComments();
		}
	}
}

function setReverseAjaxForComments() {
	if (!SET_REVERSE_AJAX) {
		SET_REVERSE_AJAX = true;
		DWREngine.setActiveReverseAjax(true);
	}
	if (ENABLE_REVERSE_AJAX_TIME_OUT_ID != 0) {
		window.clearTimeout(ENABLE_REVERSE_AJAX_TIME_OUT_ID);
	}
}

function getCommentsList(linkToComments, commentsId) {
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
	changeCommentsCount(commentId, null, count);
}

function setNeedToNotify(id, otherId) {
	if ('comments_send_notifications' == id) {
		NEED_TO_NOTIFY = true;
	}
	else {
		NEED_TO_NOTIFY = false;
	}
	if (IE) {
		var element = $(id);
		element.setProperty('checked', !element.getProperty('checked'));
		var otherElement = $(otherId);
		otherElement.setProperty('checked', !otherElement.getProperty('checked'));
	}
}

function enableComments(enable, pageKey, moduleId, propName, cacheKey) {
	CHECKED_BOX_MANUALY = true;
	showLoadingMessage(CommentsViewer.localizations.saving);
	CommentsEngine.setModuleProperty(pageKey, moduleId, propName, enable, cacheKey);
}

function hideOrShowComments(id) {
	CommentsEngine.hideOrShowComments({
		callback: function(result) {
			hideOrShowCommentsCallback(result, id);
		}
	});
}

function hideOrShowCommentsCallback(needToReload, id) {
	if (needToReload) {
		reloadPage();
	}
	closeAllLoadingMessages();
	if (CommentsViewer.info.hasValidRights) {
		if (CHECKED_BOX_MANUALY) {
			CHECKED_BOX_MANUALY = false; // Original page, no actions to perform
		}
		else {
			var checkBox = $(id + 'manageCommentsBlockCheckBox'); // Marking as checked/unchecked
			if (checkBox != null) {
				checkBox.checked = !checkBox.checked;
			}
		}
	}
}

function removeElementFromParent(element) {
	if (element == null) {
		return false;
	}
	var elementParent = element.parentNode;
	if (elementParent != null) {
		elementParent.removeChild(element);
	}
}

function removeAtomAndDeleteButtonsForComments(commentsId) {
	if (commentsId == null) {
		return false;
	}
	var atomLinkId = commentsId + 'article_comments_link_to_feed';
	removeElementFromParent($(atomLinkId));
	
	var deleteImageId = commentsId + 'delete_article_comments';
	removeElementFromParent($(deleteImageId));
}

function addAtomButtonForComments(commentsId, linkToComments, newestEntriesOnTop) {
	if (commentsId == null || linkToComments == null) {
		return false;
	}
	var atomLinkId = commentsId + 'article_comments_link_to_feed';
	var atomLink = $(atomLinkId);
	if (atomLink != null) {
		return false;
	}
	
	// Container
	var container = $(commentsId + 'article_comments_link_label_container');
	if (container == null) {
		return false;
	}
	
	// Link
	var linkToFeed = new Element('a');
	linkToFeed.setProperty('id', atomLinkId);
	linkToFeed.setProperty('href', CommentsViewer.info.commentsServer + linkToComments);
	linkToFeed.setProperty('rel', 'alternate');
	linkToFeed.setProperty('type', 'application/atom+xml');
		
	linkToFeed.appendText(' ');
		
	// Image
	var image = new Element('img');
	image.setProperty('src', CommentsViewer.info.feedImage);
	image.setProperty('title', CommentsViewer.localizations.atomLink);
	image.setProperty('alt', CommentsViewer.localizations.atomLink);
	image.setProperty('name', CommentsViewer.localizations.atomLink);
	linkToFeed.appendChild(image);
		
	container.appendChild(linkToFeed);
	
	if (CommentsViewer.info.hasValidRights) {
		container.appendText(' ');
		
		var deleteImage = new Element('img');
		deleteImage.setProperty('id', commentsId + 'delete_article_comments');
		deleteImage.setProperty('src', CommentsViewer.info.deleteImage);
		deleteImage.setProperty('title', CommentsViewer.localizations.deleteComments);
		deleteImage.setProperty('alt', CommentsViewer.localizations.deleteComments);
		deleteImage.setProperty('name', CommentsViewer.localizations.deleteComments);
		deleteImage.addClass('deleteCommentsImage');
		
		deleteImage.addEvent('click', function() {
			deleteComments(commentsId, null, linkToComments, newestEntriesOnTop);
		});
		
		container.appendChild(deleteImage);
	}
}

function deleteComments(id, commentId, linkToComments, newestEntriesOnTop) {
	var confirmed = confirm(CommentsViewer.localizations.areYouSure);
	if (!confirmed) {
		return false;
	}
	
	var commentInfo = getCommentsInfo(linkToComments);
	if (commentInfo == null) {
		return false;
	}
	
	showLoadingMessage(CommentsViewer.localizations.deleting);
	CommentsEngine.deleteComments(new CommentsViewerProperties(null, null, null, null, linkToComments, commentId, id, commentInfo.springBeanIdentifier,
									commentInfo.identifier, true, newestEntriesOnTop), {
		callback: function(properties) {
			closeAllLoadingMessages();
			if (properties == null) {
				return false;
			}
			if (properties.actionSuccess) {
				if (commentId == null) {
					changeCommentsCount(id, null, 0);
					removeElementFromParent($(id + 'comments_block_list'));
				}
				
				CommentsEngine.getCommentsForAllPages(properties);
			}
		}
	});
}

function needToShowCommentsList(commentsId) {
	var workingWithCurrentComments = (GLOBAL_COMMENTS_MARK_ID != null && GLOBAL_COMMENTS_MARK_ID == commentsId);
	var list = $(commentsId + COMMENTS_BLOCK_LIST_ID);
	if (list == null) {
		return true;
	}
	if (list.style.display == 'block') {
		return false;
	}
	
	if (!workingWithCurrentComments) {
		var commentInfo = getCommentsInfoById(commentsId);
		if (commentInfo == null) {
			return false;
		}
		if (commentInfo.commentsListOpened) {
			return true;
		}
		return false;
	}
	
	return true;
}

function getUpdatedCommentsFromServer(commentsId) {
	GLOBAL_COMMENTS_MARK_ID = commentsId;
	getAllComments();
}