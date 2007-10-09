var IS_COMMENT_PANEL_ADDED = false;
var SHOW_COMMENTS_LIST = false;
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

var COMMENTS_POSTED_LABEL = 'Posted';
var COMMENTS_MESSAGE = 'Loading comments...';
var COMMENTS_ATOM_LINK_TITLE = 'Atom Feed';
var COMMENTS_ATOMS_SERVER = '127.0.0.1';
var ADD_NOTIFICATION_TEXT = 'Do you wish to receive notifications about new comments?';
var COMMENTS_YES = 'Yes';
var COMMENTS_NO = 'No';
var COMMENTS_ENTER_EMAIL = 'Please enter your e-mail!';
var COMMENTS_SAVING_TEXT = 'Saving...';
var LINK_TO_ATOM_FEED_IMAGE = '/idegaweb/bundles/com.idega.block.article.bundle/resources/images/feed.png';
var LINK_TO_DELETE_COMMENTS_IMAGE = '/idegaweb/bundles/com.idega.block.article.bundle/resources/images/comments_delete.png';
var LINK_TO_DELETE_COMMENT_IMAGE = '/idegaweb/bundles/com.idega.block.article.bundle/resources/images/comment_delete.png';
var DELETING_MESSAGE_TEXT = 'Deleting...';
var ARE_YOU_SURE_FOR_DELETING = 'Are you sure?';
var DELETE_COMMENTS_LABEL = 'Delete comments';
var DELETE_COMMENT_LABEL = 'Delete this comment';

var HAS_COMMENT_VIEWER_VALID_RIGHTS = false;
var ADDED_LINK_TO_ATOM_IN_BODY = false;

var COMMENTS_LINK_TO_FILE = '/files';
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
	setCommentValues('', '', '', '');
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
		return false;
	}
	if (id == null || getLinkToComments() == null) {
		return false;
	}
	var container = $(id);
	if (container == null) {
		return false;
	}
	container.appendChild(getCommentPane(linkToComments, addEmail, commentsId));
}

function getCommentPane(linkToComments, addEmail, commentsId) {
	var userId = 'comment_user_value';
	var subjectId = 'comment_subject_value';
	var emailId = 'comment_email_value';
	var bodyId = 'comment_comment_value';
	var secretInputId= 'secretCommentsInput';
	
	var container = new Element('div');
	container.setProperty('id', commentsId + COMMENT_PANEL_ID);
	
	var fieldset = new Element('fieldset');
	fieldset.addClass('comment_fieldset');
	container.appendChild(fieldset);
	
	var legend = new Element('legend');
	legend.addClass('comment_legend');
	legend.appendText('Comment form');
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
		
		var notificationTextContainer = new Element('div');
		notificationTextContainer.setStyle('float', 'right');
		notificationTextContainer.appendText(getAddNotificationText());
		needToNotifyContainer.appendChild(notificationTextContainer);
		
		needToNotifyContainer.appendChild(new Element('div').addClass('spacer'));

		var sendNotificationContainer = new Element('div');
		sendNotificationContainer.setStyle('float', 'right');
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
		sendNotificationLabel.appendText(getYesText());
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
		notSendNotificationLabel.appendText(getNoText());
		sendNotificationContainer.appendChild(notSendNotificationLabel);
		
		needToNotifyContainer.appendChild(sendNotificationContainer);
		mainCommentContainer.appendChild(needToNotifyContainer);
		mainCommentContainer.appendChild(new Element('div').addClass('spacer'));
	//}
	
	// Send button
	var sendButtonContainer = new Element('div');
	sendButtonContainer.setStyle('float', 'right');
	var send = createInput('send_comment', 'button', LABEL_SEND, 'send_comment_button');
	send.addEvent('click', function() {
		closeCommentPanelAndSendComment(userId, subjectId, emailId, bodyId, linkToComments, secretInputId, commentsId);
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

function closeCommentPanelAndSendComment(userId, subjectId, emailId, bodyId, linkToComments, secretInputId, commentsId) {
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
			alert(getEnterEmailText());
			return false;
		}
	}
	showLoadingMessage(LABEL_SENDING);
	closeCommentsPanel(commentsId);
	setCommentValues(user.value, subject.value, emailValue, body.value);
	CommentsEngine.addComment(USER, SUBJECT, EMAIL, BODY, linkToComments, NEED_TO_NOTIFY, commentsId);
}

function addComment(articleComment, commentsId, linkToComments) {
	var commentIndex = 0;
	var counter = $(commentsId + 'contentItemCount');
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
	
	var commentsContainer = $(commentsId + 'comments_block');
	if (commentsContainer == null) {
		return false;
	}
	var commentsList = $(commentsId + COMMENTS_BLOCK_LIST_ID);
	if (commentsList == null) {
		commentsList = new Element('ol');
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
	commentContainer.setProperty('id', 'cmnt_' + commentIndex);
	if (commentIndex == 1) {
		commentContainer.setProperty('style', 'margin: 0px;');
	}
	var commentValue = new Element('dl');
	
	if (HAS_COMMENT_VIEWER_VALID_RIGHTS) {
		var deleteImage = new Element('img');
		deleteImage.setProperty('id', commentsId + 'delete_article_comment' + articleComment.id);
		deleteImage.setProperty('src', LINK_TO_DELETE_COMMENT_IMAGE);
		deleteImage.setProperty('title', DELETE_COMMENT_LABEL);
		deleteImage.setProperty('alt', DELETE_COMMENT_LABEL);
		deleteImage.setProperty('name', DELETE_COMMENT_LABEL);
		deleteImage.addClass('deleteCommentsImage');
		deleteImage.addEvent('click', function() {
			deleteComments(commentsId, articleComment.id, linkToComments);
		});
		commentContainer.appendChild(deleteImage);
	}
	
	var user = new Element('dt');
	var userValue = new Element('p');
	userValue.addClass('comment_author_text');
	userValue.appendText(articleComment.user + ':');
	user.appendChild(userValue);
	commentValue.appendChild(user);
	
	var subject = new Element('dd');
	var subjectValue = new Element('p');
	subjectValue.appendText(articleComment.subject);
	subject.appendChild(subjectValue);
	commentValue.appendChild(subject);
	
	var body = new Element('dd');
	var bodyValue = new Element('p');
	bodyValue.appendText(articleComment.comment);
	body.appendChild(bodyValue);
	commentValue.appendChild(body);
	
	var posted = new Element('dd');
	var postedValue = new Element('p');
	postedValue.appendText(getPostedLabel() + ': ' + articleComment.posted);
	posted.appendChild(postedValue);
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
		return false;
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

function removeCommentsList(commentId) {
	var counter = $(commentId + 'contentItemCount');
	if (counter != null) {
		var children = counter.childNodes;
		if (children != null) {
			var countValue = counter.childNodes[0];
			countValue.nodeValue = 0;
		}
	}
	
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
}

function showCommentsList(commentsId) {
	var list = $(commentsId + COMMENTS_BLOCK_LIST_ID);
	if (list != null) {
		list.style.display = 'block';
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
	var counter = $(commentId + 'contentItemCount');
	if (counter == null) {
		return false;
	}
	var children = counter.childNodes;
	if (children == null) {
		return false;
	}
	counter.removeChild(counter.childNodes[0]);
	counter.appendText(count);
}

function getCommentsCount(commentId) {
	var counter = $(commentId + 'contentItemCount');
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
	showLoadingMessage(getCommentsSavingText());
	CommentsEngine.setModuleProperty(pageKey, moduleId, propName, enable, cacheKey);
}

function hideOrShowComments() {
	CommentsEngine.hideOrShowComments(hideOrShowCommentsCallback);
}

function hideOrShowCommentsCallback(needToReload) {
	if (needToReload) {
		reloadPage();
	}
	closeLoadingMessage();
	if (getHasCommentViewerValidRights()) {
		if (CHECKED_BOX_MANUALY) {
			CHECKED_BOX_MANUALY = false; // Original page, no actions to perform
		}
		else {
			var checkBox = $('manageCommentsBlockCheckBox'); // Marking as checked/unchecked
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

function addAtomButtonForComments(commentsId, linkToComments) {
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
	linkToFeed.setProperty('href', getCommentsAtomsServer() + linkToComments);
	linkToFeed.setProperty('rel', 'alternate');
	linkToFeed.setProperty('type', 'application/atom+xml');
		
	linkToFeed.appendText(' ');
		
	// Image
	var image = new Element('img');
	image.setProperty('src', getLinkToAtomFeedImage());
	image.setProperty('title', getCommentsAtomLinkTitle());
	image.setProperty('alt', getCommentsAtomLinkTitle());
	image.setProperty('name', getCommentsAtomLinkTitle());
	linkToFeed.appendChild(image);
		
	container.appendChild(linkToFeed);
	
	if (HAS_COMMENT_VIEWER_VALID_RIGHTS) {
		container.appendText(' ');
		
		var deleteImage = new Element('img');
		deleteImage.setProperty('id', commentsId + 'delete_article_comments');
		deleteImage.setProperty('src', LINK_TO_DELETE_COMMENTS_IMAGE);
		deleteImage.setProperty('title', DELETE_COMMENTS_LABEL);
		deleteImage.setProperty('alt', DELETE_COMMENTS_LABEL);
		deleteImage.setProperty('name', DELETE_COMMENTS_LABEL);
		deleteImage.addClass('deleteCommentsImage');
		
		deleteImage.addEvent('click', function() {
			deleteComments(commentsId, null, linkToComments);
		});
		
		container.appendChild(deleteImage);
	}
}

function setAddedLinkToAtomInBody(added) {
	ADDED_LINK_TO_ATOM_IN_BODY = added;
}

function deleteComments(id, commentId, linkToComments) {
	var confirmed = confirm(ARE_YOU_SURE_FOR_DELETING);
	if (!confirmed) {
		return false;
	}
	showLoadingMessage(DELETING_MESSAGE_TEXT);
	CommentsEngine.deleteComments(id, commentId, linkToComments, deleteCommentsCallback);
}

function deleteCommentsCallback(result) {
	closeLoadingMessage();
	if (result == null) {
		return false;
	}
	if (result.length == 2) {
		CommentsEngine.getCommentsForAllPages(result[1], result[0]);
	}
}

function needToShowCommentsList(commentsId) {
	var list = $(commentsId + COMMENTS_BLOCK_LIST_ID);
	if (list == null) {
		return true;
	}
	if (list.style.display == 'block') {
		return false;
	}
	return true;
}

function initComments() {
	CommentsEngine.getInitInfoForComments(getInitInfoForCommentsCallback);
}