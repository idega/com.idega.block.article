package com.idega.block.article.component;

import java.rmi.RemoteException;

import javax.faces.component.UIComponent;

import com.idega.block.article.bean.CommentsViewerProperties;
import com.idega.block.article.business.ArticleConstants;
import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.content.upload.presentation.FileUploadViewer;
import com.idega.core.contact.data.Email;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.Block;
import com.idega.presentation.CSSSpacer;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.Table2;
import com.idega.presentation.TableBodyRowGroup;
import com.idega.presentation.TableCell2;
import com.idega.presentation.TableRow;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.FieldSet;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.GenericButton;
import com.idega.presentation.ui.Label;
import com.idega.presentation.ui.Legend;
import com.idega.presentation.ui.RadioButton;
import com.idega.presentation.ui.TextArea;
import com.idega.presentation.ui.TextInput;
import com.idega.user.business.NoEmailFoundException;
import com.idega.user.business.UserBusiness;
import com.idega.user.data.User;
import com.idega.util.CoreConstants;
import com.idega.util.StringUtil;

public class CommentCreator extends Block {
	
	private boolean addUploader;
	private String uploadPath;
	
	private CommentsViewerProperties properties;
	
	@Override
	public void main(IWContext iwc) {
		if (properties == null) {
			return;
		}
		
		IWResourceBundle iwrb = getResourceBundle(iwc);
		
		Layer container = new Layer();
		add(container);
		container.setId(getId());
		container.setStyleClass("commentsContainer");
		
		Form form = new Form();
		container.add(form);
		
		Legend legend = new Legend(iwrb.getLocalizedString("comments_viewer.comment_form", "Comment form"));
		legend.setStyleClass("comment_legend");
		
		FieldSet fieldSet = new FieldSet(legend);
		fieldSet.setStyleClass("comment_fieldset");
		form.add(fieldSet);
		
		Layer mainContainer = new Layer();
		fieldSet.add(mainContainer);
		
		Table2 table = new Table2();
		mainContainer.add(table);
		table.setStyleClass("add_comment_table");
		
		TableBodyRowGroup tableBody = table.createBodyRowGroup();
		
		TextInput secretInput = getInput("secretCommentsInput", CoreConstants.EMPTY, "secretCommentsInputStyle");
		mainContainer.add(secretInput);
		
		//	User
		User currentUser = iwc.isLoggedOn() ? iwc.getCurrentUser() : null;
		addLine(tableBody, iwrb.getLocalizedString("comments_viewer.name", "Name"), currentUser == null ?
				iwrb.getLocalizedString("anonymous", "Anonymous") : currentUser.getName(), "comment_input_style", "comment_user_value");
		
		//	Subject
		addLine(tableBody, iwrb.getLocalizedString("comments_viewer.subject", "Subject"), properties.getSubject(), "comment_input_style", "comment_subject_value");
		
		//	Email
		addLine(tableBody, iwrb.getLocalizedString("comments_viewer.email", "Email"), getUserEmail(iwc, currentUser), "comment_input_style",
				"comment_email_value");
		
		//	Comment
		addLine(tableBody, iwrb.getLocalizedString("comments_viewer.comment_body", "Comment"), properties.getBody(), "comment_comment_style",
				"comment_comment_value", true, null);
		
		//	Files attacher
		if (isAddUploader()) {
			FileUploadViewer uploader = new FileUploadViewer();
			uploader.setAllowMultipleFiles(false);
			uploader.setAutoAddFileInput(false);
			uploader.setAutoUpload(true);
			uploader.setShowUploadedFiles(true);
			uploader.setFormId(form.getId());
			uploader.setUploadPath(uploadPath);
			addLine(tableBody, iwrb.getLocalizedString("comments_viewer.attach_file", "Attach file"), null, null, null, false, uploader);
		}
		
		//	Notify
		Layer needToNotifyContainer = new Layer();
		needToNotifyContainer.setStyleClass("commentsNotify");
		mainContainer.add(needToNotifyContainer);
		
		Layer notificationTextContainer = new Layer();
		needToNotifyContainer.add(notificationTextContainer);
		notificationTextContainer.add(
				iwrb.getLocalizedString("comments_viewer.need_send_notification", "Do you wish to receive notifications about new comments?"));
		
		needToNotifyContainer.add(new CSSSpacer());
		
		Layer sendNotificationContainer = new Layer();
		needToNotifyContainer.add(sendNotificationContainer);
		sendNotificationContainer.setStyleClass("commentsSendNotification");
		
		addNotificationButton(sendNotificationContainer, "comments_send_notifications", iwrb.getLocalizedString("yes", "Yes"), false);
		addNotificationButton(sendNotificationContainer, "comments_not_send_notifications", iwrb.getLocalizedString("no", "No"), true);
		
		//	Buttons
		Layer buttons = new Layer();
		mainContainer.add(buttons);
		buttons.setStyleAttribute("float", "right");
		
		GenericButton sendComment = new GenericButton();
		buttons.add(sendComment);
		sendComment.setId("send_comment");
		sendComment.setStyleClass("send_comment_button");
		sendComment.setContent(iwrb.getLocalizedString("comments_viewer.send", "Send"));
		sendComment.setOnClick(new StringBuilder("CommentsViewer.sendComment({linkToComments: '").append(properties.getUri())
			.append("', commentsId: '").append(properties.getId())
			.append("', instanceId: '").append(properties.getInstanceId())
			.append("', springBeanIdentifier: ").append(getJavaScriptParameter(properties.getSpringBeanIdentifier()))
			.append(", identifier: ").append(getJavaScriptParameter(properties.getIdentifier()))
			.append(", newestEntriesOnTop: ").append(properties.isNewestEntriesOnTop())
		.append("});").toString());
	}
	
	private String getJavaScriptParameter(String value) {
		if (StringUtil.isEmpty(value)) {
			return "null";
		}
		
		return new StringBuilder(CoreConstants.QOUTE_SINGLE_MARK).append(value).append(CoreConstants.QOUTE_SINGLE_MARK).toString();
	}
	
	private void addNotificationButton(Layer container, String id, String label, boolean setChecked) {
		RadioButton radio = new RadioButton("comments_confirm_want_notifications");
		radio.setId(id);
		radio.setOnClick(setChecked ?
				"setNeedToNotify('comments_not_send_notifications', 'comments_send_notifications');" :
				"setNeedToNotify('comments_send_notifications', 'comments_not_send_notifications');"
		);
		radio.setSelected(setChecked);
		container.add(radio);
		Label sendLabel = new Label(label, radio);
		container.add(sendLabel);
	}
	
	private String getUserEmail(IWContext iwc, User currentUser) {
		if (currentUser == null) {
			return null;
		}
		
		UserBusiness userBusiness = null;
		try {
			userBusiness = IBOLookup.getServiceInstance(iwc, UserBusiness.class);
		} catch (IBOLookupException e) {
			e.printStackTrace();
		}
		if (userBusiness == null) {
			return null;
		}
		
		Email email = null;
		try {
			email = userBusiness.getUsersMainEmail(currentUser);
		} catch (RemoteException e) {
		} catch (NoEmailFoundException e) {
		}
		if (email == null) {
			return null;
		}
		
		return email.getEmailAddress();
	}
	
	private void addLine(TableBodyRowGroup tableBody, String label, String value, String styleClass, String id) {
		addLine(tableBody, label, value, styleClass, id, false, null);
	}
	
	private void addLine(TableBodyRowGroup tableBody, String label, String value, String styleClass, String id, boolean textArea, UIComponent component) {
//		var line = new Element('tr');
//		var labelCell = new Element('td');
//		labelCell.addClass('comments_table_cell');
//		labelCell.appendText(cellLabel);
//		line.appendChild(labelCell);
//		var inputCell = new Element('td');
//		inputCell.appendChild(createInput(inputId, inputType, inputValue, inputStyle));
//		line.appendChild(inputCell);
//		return line;
		
		TableRow row = tableBody.createRow();
		TableCell2 labelCell = row.createCell();
		labelCell.setStyleClass("comments_table_cell");
		labelCell.add(new Text(label));
		
		TableCell2 valueCell = row.createCell();
		valueCell.add(component == null ? textArea ? getTextArea(id, value, styleClass) : getInput(id, value, styleClass) : component);
	}
	
	private TextArea getTextArea(String id, String value, String styleClass) {
		TextArea area = new TextArea();
		area.setId(id);
		if (styleClass != null) {
			area.setStyleClass(styleClass);
		}
		if (value != null) {
			area.setContent(value);
		}
		return area;
	}
	
	private TextInput getInput(String id, String value, String styleClass) {
//		var input = new Element('input');
//		input.setProperty('id', id);
//		if (style != null) {
//			input.addClass(style);
//		}
//		if (type != null) {
//			input.setProperty('type', type);
//		}
//		input.setProperty('value', value);
//		return input;
		
		TextInput input = new TextInput();
		input.setId(id);
		if (styleClass != null) {
			input.setStyleClass(styleClass);
		}
		if (value != null) {
			input.setContent(value);
		}
		return input;
	}
	
	@Override
	public String getBundleIdentifier() {
		return ArticleConstants.IW_BUNDLE_IDENTIFIER;
	}

	public String getUploadPath() {
		return uploadPath;
	}

	public void setUploadPath(String uploadPath) {
		this.uploadPath = uploadPath;
	}

	public boolean isAddUploader() {
		return addUploader;
	}

	public void setAddUploader(boolean addUploader) {
		this.addUploader = addUploader;
	}

	public CommentsViewerProperties getProperties() {
		return properties;
	}

	public void setProperties(CommentsViewerProperties properties) {
		this.properties = properties;
	}

}
