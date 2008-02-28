/*
 * $Id: ArticleLocalizedItemBean.java,v 1.28 2008/02/28 14:30:49 valdas Exp $
 *
 * Copyright (C) 2004 Idega. All Rights Reserved.
 *
 * This software is the proprietary information of Idega.
 * Use is subject to license terms.
 *
 */
package com.idega.block.article.bean;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.webdav.lib.WebdavResource;
import org.w3c.tidy.Configuration;
import org.w3c.tidy.Tidy;

import com.idega.block.article.business.ArticleConstants;
import com.idega.block.article.component.ArticleItemViewer;
import com.idega.content.bean.ContentItem;
import com.idega.content.bean.ContentItemBean;
import com.idega.content.bean.ContentItemCase;
import com.idega.content.bean.ContentItemField;
import com.idega.content.bean.ContentItemFieldBean;
import com.idega.content.business.ContentConstants;
import com.idega.content.business.categories.CategoryBean;
import com.idega.data.IDOStoreException;
import com.idega.presentation.IWContext;
import com.idega.slide.business.IWSlideSession;
import com.idega.slide.util.WebdavExtendedResource;
import com.idega.slide.util.WebdavRootResource;
import com.idega.util.CoreConstants;
import com.idega.util.CoreUtil;
import com.idega.util.StringHandler;
import com.idega.xml.XMLDocument;
import com.idega.xml.XMLElement;
import com.idega.xml.XMLException;
import com.idega.xml.XMLNamespace;
import com.idega.xml.XMLParser;

/**
 * <p>
 * This is a JSF managed bean that manages each article xml document 
 * instance per language/locale.
 * <p>
 * Last modified: $Date: 2008/02/28 14:30:49 $ by $Author: valdas $
 *
 * @author Anders Lindman,<a href="mailto:tryggvi@idega.com">Tryggvi Larusson</a>
 * @version $Revision: 1.28 $
 */
public class ArticleLocalizedItemBean extends ContentItemBean implements Serializable, ContentItem {
	
	/**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = -7871069835129148485L;

	private boolean _isUpdated = false;
	
	private static final String ROOT_ELEMENT_NAME_ARTICLE = "article";
	
	public final static String FIELDNAME_AUTHOR = "author";
	public final static String FIELDNAME_HEADLINE = "headline";
	public final static String FIELDNAME_TEASER = "teaser";
	public final static String FIELDNAME_SOURCE = "source";
	public final static String FIELDNAME_COMMENT = "comment";
	public final static String FIELDNAME_LINK_TO_COMMENT = "linkToComments";
	//Note "comment" seems to be a reserved attribute in DAV so use "article_comment" for that!!!
	public final static String FIELDNAME_ARTICLE_COMMENT = "article_comment";
	public final static String FIELDNAME_IMAGES = "images";
	public final static String FIELDNAME_FILENAME = "filename";
	public final static String FIELDNAME_FOLDER_LOCATION = "folder_location"; // ../cms/article/YYYY/MM/
	public final static String FIELDNAME_CONTENT_LANGUAGE = "content_language";
	//public final static String FIELDNAME_LANGUAGE_CHANGE = "language_change";
	
	
	private final static String[] ATTRIBUTE_ARRAY = new String[] {FIELDNAME_AUTHOR,FIELDNAME_CREATION_DATE,FIELDNAME_HEADLINE,FIELDNAME_TEASER,ContentItemBean.FIELDNAME_BODY};
	//private final static String[] ACTION_ARRAY = new String[] {"edit","delete"};

	transient XMLNamespace idegaXMLName = new XMLNamespace("http://xmlns.idega.com/block/article/xml");
	String xIdegaXMLNameSpace = "http://xmlns.idega.com/block/article/xml";
	//private String baseFolderLocation = null;
	private ArticleItemBean articleItem;
	
	private XMLNamespace atomNamespace = new XMLNamespace("http://www.w3.org/2005/Atom");
	private XMLNamespace dcNamespace = new XMLNamespace("http://purl.org/dc/elements/1.1/");
	private XMLNamespace commentNamespace = new XMLNamespace("http://wellformedweb.org/CommentAPI/");
	
	private String articleCategories = null; // This string should be set in EditArticleView, parsing submitted categories
	
	/**
	 * Default constructor.
	 */
	public ArticleLocalizedItemBean() {
		clear();
	}
	
	public String[] getContentFieldNames(){
		return ATTRIBUTE_ARRAY;
	}
	
	public String[] getToolbarActions(){
		//return ACTION_ARRAY;
		return super.getToolbarActions();
	}
	
	public String getHeadline() { return (String)getValue(FIELDNAME_HEADLINE); }
	public String getTeaser() { return (String)getValue(FIELDNAME_TEASER); }
	public String getBody() { return (String)getValue(ContentItemBean.FIELDNAME_BODY); }
	public String getAuthor() { return (String)getValue(FIELDNAME_AUTHOR); }
	public String getSource() { return (String)getValue(FIELDNAME_SOURCE); }
	public String getComment() { return (String)getValue(FIELDNAME_COMMENT); }
	public String getLinkToComments() { return (String)getValue(FIELDNAME_LINK_TO_COMMENT); }
	public List getImages() { return getItemFields(FIELDNAME_IMAGES); }
	//public String getFilename() { return (String)getValue(FIELDNAME_FILENAME); }
	

//	public void setArticleResourcePath(String path) {
//		if(path!=null){
//			if(path.indexOf("."+ARTICLE_FILENAME_SCOPE) < 0 || !path.endsWith(ARTICLE_SUFFIX)){
//				throw new RuntimeException("["+this.getClass().getName()+"]: setArticleResourcePath("+path+") path is not valid article path!");
//			}
//		}
//		setResourcePath(path);
//	}
	
	public String getContentLanguage() {
		//String setContentLanguage = (String)getValue(FIELDNAME_CONTENT_LANGUAGE);
		//if(setContentLanguage!=null){
		//	return setContentLanguage;
		//}
		//else{
			return getLanguage();
		//}
	}
	//public String getLanguageChange() { return (String)getValue(FIELDNAME_LANGUAGE_CHANGE); }

	public void setHeadline(String s) { setValue(FIELDNAME_HEADLINE, s); }
	public void setHeadline(Object o) { setValue(FIELDNAME_HEADLINE, o.toString()); } 
	public void setTeaser(String s) { setValue(FIELDNAME_TEASER, s); } 
	public void setBody(String body) {
		setValue(ContentItemBean.FIELDNAME_BODY, body);
	} 
	public void setAuthor(String s) { setValue(FIELDNAME_AUTHOR, s); } 
	public void setSource(String s) { setValue(FIELDNAME_SOURCE, s); }
	
	public void setComment(String comment) {
		setValue(FIELDNAME_COMMENT, comment);
	}
	
	public void setLinkToComments(String linkToComments) {
		setValue(FIELDNAME_LINK_TO_COMMENT, linkToComments);
	}
	
	public void setImages(List l) { setItemFields(FIELDNAME_IMAGES, l); }
	
	public void setPublishedDate(Timestamp date) { super.setPublishedDate(date); }
	
	public Timestamp getPublishedDate() { return super.getPublishedDate(); }
	
	public void setLanguage(String lang){
		super.setLanguage(lang);
		getArticleItem().setLanguage(lang);
	}

	public boolean isUpdated() { return this._isUpdated; }
	public void setUpdated(boolean b) { this._isUpdated = b; }
	public void setUpdated(Boolean b) { this._isUpdated = b.booleanValue(); }
	
	/**
	 * Clears all all attributes for this bean. 
	 */
	public void clear() {
		super.clear();

		setHeadline(null);
		setTeaser(null);
		setBody(null);
		setAuthor(null);
		setSource(null);
		setComment(null);
		setImages(null);
		//setFilename(null);
//		setFolderLocation(null);
		this._isUpdated = false;
		//setBaseFolderLocation(null);
	}
	
	/**
	 * Adds an image to this article item.
	 */
	public void addImage(byte[] imageData, String contentType) {
		List l = getImages();
		if (l == null) {
			l = new ArrayList();
		}
		ContentItemField field = new ContentItemFieldBean();
		field.setBinaryValue(imageData);
		field.setFieldType(contentType);
		field.setOrderNo(l.size());
		l.add(field);
		setImages(l);
	}
	
	/**
	 * Removes the image with the specified image number from this article item.
	 */
	public void removeImage(Integer imageNumber) {
		int imageNo = imageNumber.intValue();
		try {
			List l = getImages();
			l.remove(imageNo);
			for (int i = 0; i < l.size(); i++) {
				ContentItemField field = (ContentItemField) l.get(i);
				field.setOrderNo(i);
			}
		} catch (Exception e) {
			//No action...
		}
	}
	
	/**
	 * <p>
	 * Try to publish the article if it's
	 * </p>
	 */
	protected void tryPublish(){
		//TODO: Implement publishing here:
		setPublished();
	}
	
	protected void setPublished(){
		setStatus(ContentItemCase.STATUS_PUBLISHED);
	}
	
	private boolean closeInputStream(InputStream stream) {
		if (stream == null) {
			return true;
		}
		
		try {
			stream.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * Returns the Article Item as an XML-formatted string
	 * @return the XML string
	 * @throws IOException
	 * @throws XMLException
	 */
	public String getAsXML() throws IOException, XMLException {
		
		//	Remove absolute references for example
		prettifyBody();
		prettifyTeaser();
		
		String body = getBody();
		if (body == null) {
			body = ArticleConstants.EMPTY;
		}
		
		String teaser = getTeaser();
		if (teaser == null) {
			teaser = ArticleConstants.EMPTY;
		}
		
		XMLParser builder = new XMLParser();
		
		XMLElement rootElement = null;
		XMLElement bodyElement = null;
		XMLElement teaserElement = null;
		XMLNamespace jTidyNamespace = new XMLNamespace("http://www.w3.org/1999/xhtml");
		
		InputStream stream = null;
		if (body != null && !ContentConstants.EMPTY.equals(body.trim())) {
			try {
				stream = StringHandler.getStreamFromString(body);
				XMLDocument bodyDoc = builder.parse(stream);
				rootElement = bodyDoc.getRootElement();
				if (rootElement != null) {
					bodyElement = rootElement.getChild("body", jTidyNamespace);
					if (bodyElement != null) {
						body = bodyElement.getContentAsString();
					}
				}
			} catch(Exception e) {
				e.printStackTrace();
			} finally {
				closeInputStream(stream);
			}
		}
		
		if (teaser != null && !ContentConstants.EMPTY.equals(teaser.trim())) {
			try {
				stream = StringHandler.getStreamFromString(teaser);
				XMLDocument bodyDoc = builder.parse(stream);
				rootElement = bodyDoc.getRootElement();
				if (rootElement != null) {
					teaserElement = rootElement.getChild("body", jTidyNamespace);
					if (teaserElement != null) {
						teaser = teaserElement.getContentAsString();
					}
				}
			} catch(Exception e) {
				e.printStackTrace();
			} finally {
				closeInputStream(stream);
			}
		}
		
		IWContext iwc = CoreUtil.getIWContext();
		return getFeedEntryAsXML(iwc, getHeadline(), null, getHeadline(), teaser, body, getAuthor(), getCategories(), getSource(), getComment(),
				ArticleItemViewer.class.getName(), getLinkToComments());
	}

	/**
	 * @deprecated use getAsXML() instead. We have dropped XMLBeans, since it insisted on adding CData
	 * @return
	 */
/*	public String getAsXMLFromXMLBeans() {
		ArticleDocument articleDoc = ArticleDocument.Factory.newInstance();
	    
	    ArticleDocument.Article article =  articleDoc.addNewArticle();
	    
		article.setHeadline(getHeadline());
		article.setBody(getBody());
		article.setTeaser(getTeaser());
		article.setAuthor(getAuthor());
		article.setSource(getSource());
		article.setComment(getComment());
		
		return articleDoc.toString();
	}
*/
	/**
	 * Stores this article file.
	 */
	public void store() throws IDOStoreException{
		InputStream stream = null;
		try {
			IWContext iwc = CoreUtil.getIWContext();
			IWSlideSession session = getIWSlideSession(iwc);
			WebdavRootResource rootResource = session.getWebdavRootResource();
	
			//	Setting the path for creating new file/creating localized version/updating existing file
			String filePath = getResourcePath();
			
			String article = getAsXML();
			if (article == null) {
				return;
			}
			
			boolean success = false;
			try {
				stream = StringHandler.getStreamFromString(article);
			
				//Conflict fix: uri for creating but path for updating
				//Note! This is a patch to what seems to be a bug in WebDav
				//Apparently in verion below works in some cases and the other in other cases.
				//Seems to be connected to creating files in folders created in same tomcat session or similar
				//not quite clear...
			
				success = rootResource.putMethod(filePath, stream);
			} catch(Exception e) {
				e.printStackTrace();
			} finally {
				closeInputStream(stream);
			}
			if (success) {
				rootResource.proppatchMethod(filePath, ArticleItemBean.PROPERTY_CONTENT_TYPE,CoreConstants.ARTICLE_FILENAME_SCOPE,true);
			}
			else {
				try {
					stream = StringHandler.getStreamFromString(article);
					String fixedURL = session.getURI(filePath);
					rootResource.putMethod(fixedURL, stream);
					rootResource.proppatchMethod(fixedURL, ArticleItemBean.PROPERTY_CONTENT_TYPE,CoreConstants.ARTICLE_FILENAME_SCOPE,true);
				} catch(Exception e) {
					e.printStackTrace();
				} finally {
					closeInputStream(stream);
				}
			}
			
			rootResource.close();
			try {
				ArticleLocalizedItemBean newBean = new ArticleLocalizedItemBean();
				newBean.setArticleItem(this.getArticleItem());
				newBean.setResourcePath(filePath);
				newBean.load();
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		catch (IOException e1) {
			throw new RuntimeException(e1);
		}
		catch (XMLException e) {
			throw new RuntimeException(e);
		}
	
		if (getRequestedStatus() != null) {
			setStatus(getRequestedStatus());
			setRequestedStatus(null);
		}
	}
    
	/**
	 * 
	 */
	protected void prettifyBody() {
		String s = prettify(getBody());
		if (s != null) {
			setBody(s);
		}
	}

	protected void prettifyTeaser() {
		String s = prettify(getTeaser());
		if (s != null) {
			setTeaser(s);
		}
	}
	
	protected String prettify(String toPrettify) {
		if (toPrettify == null) {
			return null;
		}
		
		String text = toPrettify;
		//	Use JTidy to clean up the HTML
		Tidy tidy = new Tidy();
		tidy.setXHTML(true);
		tidy.setXmlOut(true);
		tidy.setShowWarnings(false);
		tidy.setCharEncoding(Configuration.UTF8);
		InputStream stream = null;
		ByteArrayOutputStream baos = null;
		try {
			stream = StringHandler.getStreamFromString(text);
			baos = new ByteArrayOutputStream();
			
			tidy.parse(stream, baos);
			text = baos.toString(CoreConstants.ENCODING_UTF8);
		}
		catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeInputStream(stream);
			closeOutputStream(baos);
		}
	
		text = removeAbsoluteReferences(text);
		return text;
	}

	private boolean closeOutputStream(OutputStream outputStream) {
		if (outputStream == null) {
			return false;
		}
		
		try {
			outputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	private String removeAbsoluteReferences(String text) {
		return StringHandler.removeAbsoluteReferencies(text);
	}
	
	/**
	 * Loads an xml file specified by the webdav resource
	 * The beans atributes are then set according to the information in the XML file
	 */
	protected boolean load(WebdavExtendedResource webdavResource) throws IOException {
		XMLParser builder = new XMLParser();
		XMLDocument bodyDoc = null;
		InputStream stream = null;
		try {
			if (webdavResource.isCollection()) {
				throw new RuntimeException(webdavResource+" is folder but should be file");
			}
			WebdavResource theArticle = webdavResource;
			if (theArticle != null && !theArticle.isCollection()) {
				setResourcePath(theArticle.getPath());
				stream = theArticle.getMethodData();
				bodyDoc = builder.parse(stream);
			} else {
				bodyDoc = null;
			}
		} catch (XMLException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		} finally {
			closeInputStream(stream);
		}
		
		if (bodyDoc == null) {
			//	Article not found
			Logger log = Logger.getLogger(this.getClass().toString());
			log.warning("Article xml file was not found");
			setRendered(false);
			return false;
		}
		XMLElement rootElement = bodyDoc.getRootElement();
		
		boolean isOldArticleXMLFile = true;
		if (!ROOT_ELEMENT_NAME_ARTICLE.equals(rootElement.getName())) {
			isOldArticleXMLFile = false;
		}
		
		if (!isOldArticleXMLFile) {
			return loadArticleFromFeed(rootElement);
		}
	
		try {
			XMLElement language  = rootElement.getChild(FIELDNAME_CONTENT_LANGUAGE,getIdegaXMLNameSpace());
			if(language != null){
				setLanguage(language.getText());
			} else {
				setLanguage(null);
			}
		}catch(Exception e) {	
			setLanguage(null);
		}
		try {
			XMLElement headline = rootElement.getChild(FIELDNAME_HEADLINE,getIdegaXMLNameSpace());
			if(headline != null){
				setHeadline(headline.getText());
			} else {
				setHeadline(CoreConstants.EMPTY);
			}
		}catch(Exception e) {		//Nullpointer could occur if field isn't used
			e.printStackTrace();
			setHeadline(CoreConstants.EMPTY);
		}
		try {
			//Parse out the teaser
			try {
				XMLNamespace htmlNamespace = new XMLNamespace("http://www.w3.org/1999/xhtml");
				XMLElement bodyElement = rootElement.getChild(FIELDNAME_TEASER,getIdegaXMLNameSpace());
				XMLElement htmlElement = bodyElement.getChild("html",htmlNamespace);
				if (htmlElement == null) {
					setTeaser(bodyElement.getText());
				} else {
					XMLElement htmlBodyElement = htmlElement.getChild("body",htmlNamespace);
					
					String bodyValue = htmlBodyElement.getContentAsString();
					setTeaser(bodyValue);
				}
			}catch(Exception e) {		//Nullpointer could occur if field isn't used
				Logger log = Logger.getLogger(this.getClass().toString());
				log.warning("Teaser of article is empty");
				setTeaser(CoreConstants.EMPTY);
			}
			}catch(Exception e) {		//Nullpointer could occur if field isn't used
				e.printStackTrace();
				setTeaser(CoreConstants.EMPTY);
			}
		try {
			XMLElement author = rootElement.getChild(FIELDNAME_AUTHOR,getIdegaXMLNameSpace());
			if(author != null){
				setAuthor(author.getText());
			} else {
				setAuthor(CoreConstants.EMPTY);
			}
		}catch(Exception e) {		//Nullpointer could occur if field isn't used
			e.printStackTrace();
			setAuthor(CoreConstants.EMPTY);
		}

		//Parse out the body
		try {
			XMLNamespace htmlNamespace = new XMLNamespace("http://www.w3.org/1999/xhtml");
			XMLElement bodyElement = rootElement.getChild(ContentItemBean.FIELDNAME_BODY,getIdegaXMLNameSpace());
			XMLElement htmlElement = bodyElement.getChild("html",htmlNamespace);
			XMLElement htmlBodyElement = htmlElement.getChild("body",htmlNamespace);
			
			String bodyValue = htmlBodyElement.getContentAsString();
			setBody(bodyValue);
		}catch(Exception e) {		//Nullpointer could occur if field isn't used
			Logger log = Logger.getLogger(this.getClass().toString());
			log.warning("Body of article is empty");
			setBody(CoreConstants.EMPTY);
		}
		
		try {
			XMLElement source = rootElement.getChild(FIELDNAME_SOURCE,getIdegaXMLNameSpace());
			if(source != null){
				setSource(source.getText());
			} else {
				setSource(CoreConstants.EMPTY);
			}
		}catch(Exception e) {		//Nullpointer could occur if field isn't used
			setSource(CoreConstants.EMPTY);
		}
		try {
			XMLElement comment = rootElement.getChild(FIELDNAME_ARTICLE_COMMENT,getIdegaXMLNameSpace());
			if(comment != null){
				setComment(comment.getText());
			} else {
				setComment(CoreConstants.EMPTY);
			}
		}catch(Exception e) {		//Nullpointer could occur if field isn't used
			e.printStackTrace();
			setComment(CoreConstants.EMPTY);
		}
		return true;
	}
	
	private boolean loadArticleFromFeed(XMLElement root) {
		if (root == null) {
			return false;
		}
		
		XMLElement entry =  root.getChild("entry", atomNamespace);
		if (entry == null) {
			return false;
		}
		
		XMLElement headline = entry.getChild("title", atomNamespace);
		if (headline != null) {
			if (headline.getValue() != null) {
				setHeadline(headline.getValue());
			}
		}
		
		XMLElement summary = entry.getChild("summary", atomNamespace);
		if (summary != null) {
			if (summary.getValue() != null) {
				setTeaser(summary.getValue());
			}
		}
		
		XMLElement content = entry.getChild("content", atomNamespace);
		if (content != null) {
			if (content.getValue() != null) {
				setBody(content.getValue());
			}
		}
		
		XMLElement author = entry.getChild("author", atomNamespace);
		if (author != null) {
			XMLElement authorName = author.getChild("name", atomNamespace);
			if (authorName != null) {
				setAuthor(authorName.getValue() == null ? CoreConstants.EMPTY : authorName.getValue());
			}
		}
		
		XMLElement creator = entry.getChild("creator", dcNamespace);
		if (creator != null) {
			String creatorId = creator.getValue();
			if (creatorId != null) {
				int convertedCreatorId = -1;
				try {
					convertedCreatorId = Integer.valueOf(creatorId);
				} catch(Exception e) {}
				setCreatedByUserId(convertedCreatorId);
			}
		}
		
		XMLElement language = entry.getChild("language", dcNamespace);
		if (language != null) {
			if (language.getValue() != null) {
				setLanguage(language.getValue());
			}
		}
		
		XMLElement published = entry.getChild("published", atomNamespace);
		if (published != null) {
			if (published.getValue() != null) {
				setPublishedDate(getParsedDateFromFeed(published.getValue()));
			}
		}
		
		XMLElement updated = entry.getChild("updated", atomNamespace);
		if (updated != null) {
			if (updated.getValue() != null) {
				setLastModifiedDate(getParsedDateFromFeed(updated.getValue()));
			}
		}
		
		XMLElement source = entry.getChild("source", dcNamespace);
		if (source != null) {
			if (source.getValue() != null) {
				setSource(source.getValue());
			}
		}
		
		XMLElement comment = entry.getChild("comment", commentNamespace);
		if (comment != null) {
			if (comment.getValue() != null) {
				setComment(comment.getValue());
			}
		}
		
		XMLElement linkToComments = entry.getChild("commentRss", commentNamespace);
		if (linkToComments != null) {
			if (linkToComments.getValue() != null) {
				setLinkToComments(linkToComments.getValue());
			}
		}
		
		return true;
	}
	
/*
 * The old XMLBean way of loading data
	public void loadOld(File file) throws XmlException, IOException{
		ArticleDocument articleDoc;
		
		articleDoc = ArticleDocument.Factory.parse(file);
		
	    ArticleDocument.Article article =  articleDoc.getArticle();
	    setHeadline(article.getHeadline());
	    setBody(article.getBody());
	    setTeaser(article.getTeaser());
	    setAuthor(article.getAuthor());
	    setSource(article.getSource());
	    setComment(article.getComment());
	}
*/
	/* (non-Javadoc)
	 * @see com.idega.content.bean.ContentItem#getContentItemPrefix()
	 */
	public String getContentItemPrefix() {
		return "article_";
	}
	
	
	ArticleItemBean getArticleItem(){
		return this.articleItem;
	}
	
	
	void setArticleItem(ArticleItemBean item){
		this.articleItem=item;
	}
	
	
	protected XMLNamespace getIdegaXMLNameSpace(){
		if(this.idegaXMLName==null){
			this.idegaXMLName = new XMLNamespace(this.xIdegaXMLNameSpace);
		}
		return this.idegaXMLName;
	}

	protected String getArticleCategories() {
		return articleCategories;
	}

	protected void setArticleCategories(String articleCategories) {
		this.articleCategories = articleCategories;
	}
	
	private List<String> getCategories() {
		if (articleCategories == null) {
			return null;
		}
		String[] entries = articleCategories.split(CategoryBean.CATEGORY_DELIMETER);
		if (entries == null) {
			return null;
		}
		if (entries.length == 0) {
			return null;
		}
		List<String> categories = new ArrayList<String>();
		for (int i = 0; i < entries.length; i++) {
			if (!entries[i].equals(ArticleConstants.EMPTY)) {
				categories.add(entries[i]);
			}
		}
		return categories;
	}
	
	@Override
	public boolean isSetPublishedDateByDefault() {
		return super.isSetPublishedDateByDefault();
	}

	@Override
	public void setSetPublishedDateByDefault(boolean setPublishedDateByDefault) {
		super.setSetPublishedDateByDefault(setPublishedDateByDefault);
	}

}