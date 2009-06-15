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
	hasValidRights: false,
	replyFor: null,
	replySubject: null,
	replyMessage: null
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

function addCommentStartInfo(linkToComments, commentsId, showCommentsListOnLoad, newestEntriesOnTop, springBeanIdentifier, identifier,
	addLoginbyUUIDOnRSSFeedLink, fullCommentsRights) {
	
	var commentsInfo = new SingleCommentInfo(linkToComments, commentsId, springBeanIdentifier, identifier, showCommentsListOnLoad, newestEntriesOnTop,
		addLoginbyUUIDOnRSSFeedLink, fullCommentsRights);
	CommentsViewer.commentInfo.push(commentsInfo);

	if (showCommentsListOnLoad) {
		getComments(linkToComments, commentsId);
	}
}

function SingleCommentInfo(linkToComments, commentsId, springBeanIdentifier, identifier, showCommentsList, newestEntriesOnTop, addLoginbyUUIDOnRSSFeedLink,
	fullCommentsRights) {
	
	this.linkToComments = linkToComments;
	this.commentsId = commentsId;
	this.springBeanIdentifier = springBeanIdentifier;
	this.identifier = identifier;
	
	this.showCommentsList = showCommentsList;
	this.newestEntriesOnTop = newestEntriesOnTop;
	this.addLoginbyUUIDOnRSSFeedLink = addLoginbyUUIDOnRSSFeedLink;
	
	this.commentsListOpened = false;
	
	this.fullCommentsRights = fullCommentsRights;
}

function setCommentValues(user, subject, email, body) {
	USER = user;
	SUBJECT = subject;
	EMAIL = email;
	BODY = body;
}

function addCommentPanel(id, linkToComments, lblUser, lblSubject, lblComment, lblPosted, lblSend, lblSending, loggedUser, lblEmail,
	lblCommentForm, addEmail, commentsId, instanceId, springBeanIdentifier, identifier, newestEntriesOnTop, emailAddress) {
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
	
	jQuery('div.commentsContainer', jQuery(container)).remove();
	jQuery(container).append('<div class=\'commentsContainer\' ></div>');
	
	var info = getCommentsInfo(linkToComments);
	var properties = new CommentsViewerProperties(null, CommentsViewer.info.replyFor == null ? '' : CommentsViewer.info.replySubject, null,
		CommentsViewer.info.replyFor == null ? '' : CommentsViewer.info.replyMessage, linkToComments, commentsId, instanceId, info.springBeanIdentifier,
		info.identifier, NEED_TO_NOTIFY, info.newestEntriesOnTop, info.addLoginbyUUIDOnRSSFeedLink);
	CommentsEngine.getCommentCreator(properties, {
		callback: function(html) {
			if (html == null) {
				return false;
			}
			
			jQuery('div.commentsContainer', jQuery(container)).html(html);
			jQuery('div.commentsContainer', jQuery(container)).attr('id', commentsId + COMMENT_PANEL_ID);
			IS_COMMENT_PANEL_ADDED = true;
		}
	});
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
			humanMsg.displayMsg(CommentsViewer.localizations.enterEmail, null);
			return false;
		}
	}
	showLoadingMessage(LABEL_SENDING);
	closeCommentsPanel(commentsId);
	setCommentValues(user.value, subject.value, emailValue, body.value);
	
	var commentsInfo = getCommentsInfo(linkToComments);
	var commentProperties = new CommentsViewerProperties(USER, SUBJECT, EMAIL, BODY, linkToComments, commentsId, instanceId, springBeanIdentifier, identifier,
								NEED_TO_NOTIFY, newestEntriesOnTop, commentsInfo == null ? false : commentsInfo.addLoginbyUUIDOnRSSFeedLink);
	commentProperties.replyForComment = CommentsViewer.info.replyFor;
	GLOBAL_COMMENTS_MARK_ID = commentsId;
	CommentsEngine.addComment(commentProperties, {
		callback: function(result) {
			CommentsViewer.resetAllUploadedFiles(false);
			CommentsViewer.doExternalActions(commentProperties);
			CommentsViewer.info.replyFor = null;
			closeAllLoadingMessages();
		},
		errorHandler: function(param1, param2) {
			CommentsViewer.resetAllUploadedFiles(true);
			CommentsViewer.info.replyFor = null;
			closeAllLoadingMessages();
		}
	});
}

CommentsViewer.doExternalActions = function(properties) {
	if (properties.identifier != null && typeof CasesBPMAssets != 'undefined') {
		CasesBPMAssets.reloadDocumentsGrid();
	}
}

function CommentsViewerProperties(user, subject, email, body, uri, id, instanceId, springBeanIdentifier, identifier, notify, newestEntriesOnTop,
									addLoginbyUUIDOnRSSFeedLink) {
	this.user = user || null;
	this.subject = subject || null;
	this.email = email || null;
	this.body = body || null;
	this.uri = uri || null;
	this.id = id || null;
	this.instanceId = instanceId || null;
	this.springBeanIdentifier = springBeanIdentifier || null;
	this.identifier = identifier || null;
	
	this.currentPageUri = window.location.pathname;
	
	this.notify = notify || false;
	this.newestEntriesOnTop = newestEntriesOnTop || false;
	this.addLoginbyUUIDOnRSSFeedLink = addLoginbyUUIDOnRSSFeedLink || false;
	
	this.uploadedFiles = CommentsViewer.getAllUploadedFiles();
}

CommentsViewer.getAllUploadedFiles = function() {
	return typeof FileUploadHelper == 'undefined' ? null : FileUploadHelper.allUploadedFiles;
}

CommentsViewer.resetAllUploadedFiles = function(deleteFiles) {
	if (typeof FileUploadHelper != 'undefined') {
		if (deleteFiles) {
			FileUploadHelper.removeAllUploadedFiles();
		} else {
			FileUploadHelper.allUploadedFiles = [];
		}
	}
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

function addComment(index, articleComment, commentsId, linkToComments, newestEntriesOnTop, forceToOpen) {
	var commentsContainer = $(commentsId + 'comments_block');
	if (commentsContainer == null) {
		return false;
	}
	var commentsList = $(commentsId + COMMENTS_BLOCK_LIST_ID);
	if (commentsList == null) {
		commentsList = new Element('ul');
		commentsList.setProperty('id', commentsId + COMMENTS_BLOCK_LIST_ID);
		commentsContainer.appendChild(commentsList);
		if (forceToOpen || needToShowCommentsList(commentsId)) {
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
	
	var commentInfo = getCommentsInfo(linkToComments);
	var hasFullCommentsRights = commentInfo.fullCommentsRights;
	
	if (CommentsViewer.info.hasValidRights || hasFullCommentsRights) {
		if (CommentsViewer.info.hasValidRights) {
			var deleteImage = new Element('img');
			deleteImage.setProperty('id', commentsId + 'delete_article_comment' + articleComment.id);
			deleteImage.setProperty('src', CommentsViewer.info.deleteCommentImage);
			deleteImage.setProperty('title', CommentsViewer.localizations.deleteComment);
			deleteImage.setProperty('alt', CommentsViewer.localizations.deleteComment);
			deleteImage.setProperty('name', CommentsViewer.localizations.deleteComment);
			deleteImage.addClass('deleteCommentsImage');
			deleteImage.addEvent('click', function() {
				deleteComments(commentsId, articleComment.id, linkToComments, commentInfo.newestEntriesOnTop);
			});
			commentContainer.appendChild(deleteImage);
		}
		
		if (hasFullCommentsRights) {
			if (articleComment.canBePublished) {
				var publishImage = new Element('img');
				publishImage.setProperty('src', '/idegaweb/bundles/com.idega.block.article.bundle/resources/images/publish.png');
				publishImage.setProperty('title', CommentsViewer.localizations.publishComment);
				publishImage.addClass('publishCommentImage');
				publishImage.addEvent('click', function() {
					var info = commentInfo;
					var properties = new CommentsViewerProperties(null, null, null, null, null, articleComment.id, null, info.springBeanIdentifier,
						info.identifier, true, info.newestEntriesOnTop, info.addLoginbyUUIDOnRSSFeedLink);
					properties.primaryKey = articleComment.primaryKey;
					CommentsEngine.setCommentPublished(properties, {
						callback: function(result) {
							if (result) {
								humanMsg.displayMsg(CommentsViewer.localizations.commentWasPublished, null);
								jQuery('img.replyCommentsImage', jQuery(publishImage).parent()).remove();
								publishImage.remove();
							}
						}
					});
				});
				commentContainer.appendChild(publishImage);
			}
		}
	}
	
	if (articleComment.canBeRead) {
		var readImage = new Element('img');
		readImage.setProperty('src', '/idegaweb/bundles/com.idega.block.article.bundle/resources/images/read.png');
		readImage.setProperty('title', CommentsViewer.localizations.readComment);
		readImage.addClass('readCommentsImage');
		readImage.addEvent('click', function() {
			var info = commentInfo;
			var properties = new CommentsViewerProperties(null, null, null, null, null, articleComment.id, null, info.springBeanIdentifier,
				info.identifier, true, info.newestEntriesOnTop, info.addLoginbyUUIDOnRSSFeedLink);
			properties.primaryKey = articleComment.primaryKey;
			CommentsEngine.setReadComment(properties, {
				callback: function(result) {
					if (result) {
						humanMsg.displayMsg(CommentsViewer.localizations.commentWasRead, null);
						readImage.remove();
						commentContainer.removeClass('commentCanBeRead');
					}
				}
			});
		});
		commentContainer.appendChild(readImage);
		commentContainer.addClass('commentCanBeRead');
	}
	
	if (articleComment.canBeReplied) {
		var replyImage = new Element('img');
		replyImage.setProperty('src', '/idegaweb/bundles/com.idega.block.article.bundle/resources/images/reply.png');
		replyImage.setProperty('title', CommentsViewer.localizations.reply);
		replyImage.addClass('replyCommentsImage');
		replyImage.addEvent('click', function() {
			CommentsViewer.info.replyFor = articleComment.primaryKey;
			CommentsViewer.info.replySubject = CommentsViewer.localizations.replyFor + ': ' + articleComment.subject;
			CommentsViewer.info.replyMessage = CommentsViewer.localizations.replyForMessage + ': "' + articleComment.comment + '"\n';
			jQuery('a.addCommentFormLinkInCommentsViewer').trigger('click');
		});
		commentContainer.appendChild(replyImage);
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
	
	if (articleComment.attachments != null) {
		var attachmentsContainer = new Element('div');
		attachmentsContainer.addClass('contentItemAttachments');
		attachmentsContainer.appendText(CommentsViewer.localizations.commentAttachments + ':');
		commentValue.appendChild(attachmentsContainer);
		for (var attachmentIndex = 0; attachmentIndex < articleComment.attachments.length; attachmentIndex++) {
			var attachmentInfo = articleComment.attachments[attachmentIndex];
			
			var attachmentInfoContainer = new Element('div');
			attachmentInfoContainer.addClass('commentItemAttachmentInfo');
			attachmentsContainer.appendChild(attachmentInfoContainer);
			
			var html = (attachmentIndex + 1) + '. ' + '<a href=\''+attachmentInfo.uri+'\'>' + attachmentInfo.name + '</a>';
			if (hasFullCommentsRights) {
				var link = "<a class='commentItemAttachmentDownloadInfo' title='" + CommentsViewer.localizations.commentAttachmentDownloadInfo + "' "+
					"href='" + attachmentInfo.statisticsUri + "'>" +
					"<img src='/idegaweb/bundles/com.idega.block.article.bundle/resources/images/comment_info.png'></img></a>";
				html = html + link;
			}
			jQuery(attachmentInfoContainer).html(html);
			CommentsViewer.initializeAttachmentStatisticsLink(jQuery(attachmentInfoContainer));
		}
	}
	
	if (articleComment.readers != null) {
		var readers = new Element('div');
		readers.addClass('commentItemReaders');
		readers.appendText(CommentsViewer.localizations.commentRedBy + ':');
		commentValue.appendChild(readers);
		for (var readerIndex = 0; readerIndex < articleComment.readers.length; readerIndex++) {
			var readerInfo = articleComment.readers[readerIndex];
			
			var readerInfoContainer = new Element('div');
			readerInfoContainer.addClass('commentItemReaderInfo');
			readers.appendChild(readerInfoContainer);
			if (readerInfo.value == null) {
				readerInfoContainer.appendText((readerIndex + 1) + '. ' + readerInfo.id);
			} else {
				jQuery(readerInfoContainer).html((readerIndex + 1) + '. ' + '<a href=\'mailto:'+readerInfo.value+'\'>'+readerInfo.id+'</a>');
			}
		}
	}
	
	var posted = new Element('div');
	posted.addClass('commentItemCreated');
	posted.appendText(articleComment.posted);
	commentValue.appendChild(posted);
	
	commentContainer.appendChild(commentValue);
	commentsList.appendChild(commentContainer);
}

CommentsViewer.initializeAttachmentStatisticsLink = function(container) {
	jQuery('a.commentItemAttachmentDownloadInfo', jQuery(container)).each(function() {
		var link = jQuery(this);
		
		if (!link.hasClass('commentItemAttachmentDownloadInfoLinkInitialized')) {
			link.fancybox({
				frameWidth:		400,
				frameHeight:	300,
				hideOnContentClick: false
			});
			link.addClass('commentItemAttachmentDownloadInfoLinkInitialized');
		}
	});
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
		properties.push(new CommentsViewerProperties(null, null, null, null, CommentsViewer.commentInfo[i].linkToComments,
			CommentsViewer.commentInfo[i].commentsId, null, CommentsViewer.commentInfo[i].springBeanIdentifier, CommentsViewer.commentInfo[i].identifier, true,
			CommentsViewer.commentInfo[i].newestEntriesOnTop, CommentsViewer.commentInfo[i].addLoginbyUUIDOnRSSFeedLink));
	}
	
	showLoadingMessage(CommentsViewer.localizations.loadingComments);
	CommentsEngine.getCommentsFromUris(properties, {
		callback: function(allComments) {
			closeAllLoadingMessages();
			
			if (allComments != null) {
				if (allComments.length == CommentsViewer.commentInfo.length) {
					for (var i = 0; i < allComments.length; i++) {
						getCommentsCallback(allComments[i], CommentsViewer.commentInfo[i].commentsId, CommentsViewer.commentInfo[i].linkToComments,
											CommentsViewer.commentInfo[i].newestEntriesOnTop, false);
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
															commentsInfo.identifier, true, newestEntriesOnTop, commentsInfo.addLoginbyUUIDOnRSSFeedLink), {
  		callback:function(comments) {
    		getCommentsCallback(comments, commentsId, linkToComments, newestEntriesOnTop, true);
    		
    		enableReverseAjax();
  		}
	});
}

function getCommentsCallback(comments, id, linkToComments, newestEntriesOnTop, forceToOpen) {
	closeAllLoadingMessages();
	if (comments == null) {
		removeCommentsList(id, 0);
		closeAllLoadingMessages();
		return false;
	}
	
	removeCommentsList(id, comments.length);
	for (var i = 0; i < comments.length; i++) {
		addComment(i, comments[i], id, linkToComments, newestEntriesOnTop, forceToOpen);
	}
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
		dwr.engine.setActiveReverseAjax(true);
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
									commentInfo.identifier, true, newestEntriesOnTop, commentInfo.addLoginbyUUIDOnRSSFeedLink), {
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

CommentsViewer.sendComment = function(properties) {
	var userId = 'comment_user_value';
	var subjectId = 'comment_subject_value';
	var emailId = 'comment_email_value';
	var bodyId = 'comment_comment_value';
	var secretInputId= 'secretCommentsInput';
	
	closeCommentPanelAndSendComment(userId, subjectId, emailId, bodyId, properties.linkToComments, secretInputId, properties.commentsId, properties.instanceId,
		properties.springBeanIdentifier, properties.identifier, properties.newestEntriesOnTop);
}

CommentsViewer.sendNotificationsToDownloadDocument = function(properties) {
	if (properties == null) {
		return;
	}
	
	properties.url = window.location.href;
	CommentsEngine.sendNotificationsToDownloadDocument(properties, {
		callback: function(result) {
			if (result == null) {
				return false;
			}
			
			humanMsg.displayMsg(result.value, null);
		}, errorHandler: function(o1, o2) {
		}
	});
}