/*
 * $Id: ArticleLocalizedItemBean.java,v 1.7.2.1 2006/12/05 15:52:03 gimmi Exp $
 *
 * Copyright (C) 2004 Idega. All Rights Reserved.
 *
 * This software is the proprietary information of Idega.
 * Use is subject to license terms.
 *
 */
package com.idega.block.article.bean;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.webdav.lib.WebdavResource;
import org.w3c.tidy.Configuration;
import org.w3c.tidy.Tidy;

import com.idega.content.bean.ContentItem;
import com.idega.content.bean.ContentItemBean;
import com.idega.content.bean.ContentItemCase;
import com.idega.content.bean.ContentItemField;
import com.idega.content.bean.ContentItemFieldBean;
import com.idega.data.IDOStoreException;
import com.idega.idegaweb.IWUserContext;
import com.idega.presentation.IWContext;
import com.idega.slide.business.IWSlideSession;
import com.idega.slide.util.WebdavExtendedResource;
import com.idega.slide.util.WebdavRootResource;
import com.idega.xml.XMLDocument;
import com.idega.xml.XMLElement;
import com.idega.xml.XMLException;
import com.idega.xml.XMLNamespace;
import com.idega.xml.XMLOutput;
import com.idega.xml.XMLParser;

/**
 * <p>
 * This is a JSF managed bean that manages each article xml document 
 * instance per language/locale.
 * <p>
 * Last modified: $Date: 2006/12/05 15:52:03 $ by $Author: gimmi $
 *
 * @author Anders Lindman,<a href="mailto:tryggvi@idega.com">Tryggvi Larusson</a>
 * @version $Revision: 1.7.2.1 $
 */
public class ArticleLocalizedItemBean extends ContentItemBean implements Serializable, ContentItem {
	
	/**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = -7871069835129148485L;

	private boolean _isUpdated = false;
	
	public final static String FIELDNAME_AUTHOR = "author";
	public final static String FIELDNAME_HEADLINE = "headline";
	public final static String FIELDNAME_TEASER = "teaser";
	public final static String FIELDNAME_BODY = "body";
	public final static String FIELDNAME_SOURCE = "source";
	public final static String FIELDNAME_COMMENT = "comment";
	//Note "comment" seems to be a reserved attribute in DAV so use "article_comment" for that!!!
	public final static String FIELDNAME_ARTICLE_COMMENT = "article_comment";
	public final static String FIELDNAME_IMAGES = "images";
	public final static String FIELDNAME_FILENAME = "filename";
	public final static String FIELDNAME_FOLDER_LOCATION = "folder_location"; // ../cms/article/YYYY/MM/
	public final static String FIELDNAME_CONTENT_LANGUAGE = "content_language";
	//public final static String FIELDNAME_LANGUAGE_CHANGE = "language_change";
	
	
	private final static String[] ATTRIBUTE_ARRAY = new String[] {FIELDNAME_AUTHOR,FIELDNAME_CREATION_DATE,FIELDNAME_HEADLINE,FIELDNAME_TEASER,FIELDNAME_BODY};
	//private final static String[] ACTION_ARRAY = new String[] {"edit","delete"};

	transient XMLNamespace idegaXMLName = new XMLNamespace("http://xmlns.idega.com/block/article/xml");
	String xIdegaXMLNameSpace = "http://xmlns.idega.com/block/article/xml";
	//private String baseFolderLocation = null;
	private ArticleItemBean articleItem;
	
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
	public String getBody() { return (String)getValue(FIELDNAME_BODY); }
	public String getAuthor() { return (String)getValue(FIELDNAME_AUTHOR); }
	public String getSource() { return (String)getValue(FIELDNAME_SOURCE); }
	public String getComment() { return (String)getValue(FIELDNAME_COMMENT); }
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
		setValue(FIELDNAME_BODY, body);
//		if (null != articleIn) {
////			System.out.println("ArticleIn = "+articleIn);
//			//Use JTidy to clean up the html
//			Tidy tidy = new Tidy();
//			tidy.setXHTML(true);
//			tidy.setXmlOut(true);
//			ByteArrayInputStream bais = new ByteArrayInputStream(articleIn.getBytes());
//			ByteArrayOutputStream baos = new ByteArrayOutputStream();
//			tidy.parse(bais, baos);
//			String articleOut = baos.toString();
////			System.out.println("ArticleOut = "+articleOut);
//			setValue(FIELDNAME_BODY, articleOut);
////			setValue(FIELDNAME_BODY, articleIn);
//		}
//		else {
//			setValue(FIELDNAME_BODY, null);
//		}
	} 
	public void setAuthor(String s) { setValue(FIELDNAME_AUTHOR, s); } 
	public void setSource(String s) { setValue(FIELDNAME_SOURCE, s); }
	public void setComment(String s) { setValue(FIELDNAME_COMMENT, s); }
	public void setImages(List l) { setItemFields(FIELDNAME_IMAGES, l); }
	//public void setFilename(String l) { setValue(FIELDNAME_FILENAME, l); }
//	public void setFolderLocation(String l) { setValue(FIELDNAME_FOLDER_LOCATION, l); }
	//public void setContentLanguage(String lang) { 	
		//setValue(FIELDNAME_CONTENT_LANGUAGE, lang); 
	//	setLanguage(lang);
	//}
	
	public void setLanguage(String lang){
		super.setLanguage(lang);
		getArticleItem().setLanguage(lang);
	}
	//public void setLanguageChange(String s) { setValue(FIELDNAME_LANGUAGE_CHANGE, s); }

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
	

	/**
	 * Returns the Article Item as an XML-formatted string
	 * @return the XML string
	 * @throws IOException
	 * @throws XMLException
	 */
	public String getAsXML() throws IOException, XMLException {

		XMLParser builder = new XMLParser();
		
		XMLElement bodyElement = null;
		XMLElement teaserElement = null;
		
		prettifyBody();
		prettifyTeaser();
		
		String bodyString = getBody();
		if(bodyString != null && !bodyString.trim().equals("")){
			XMLDocument bodyDoc = builder.parse(new ByteArrayInputStream(bodyString.getBytes("UTF-8")));
			bodyElement = bodyDoc.getRootElement();
		}
		
		String teaserString = getTeaser();
		if(teaserString != null && !teaserString.trim().equals("")){
			XMLDocument bodyDoc = builder.parse(new ByteArrayInputStream(teaserString.getBytes("UTF-8")));
			teaserElement = bodyDoc.getRootElement();
		}

		
		
		XMLElement root = new XMLElement("article",getIdegaXMLNameSpace());
		XMLElement contentLanguage = new XMLElement(FIELDNAME_CONTENT_LANGUAGE,getIdegaXMLNameSpace()).setText(getContentLanguage());
		XMLElement headline = new XMLElement(FIELDNAME_HEADLINE,getIdegaXMLNameSpace()).setText(getHeadline());
		XMLElement author = new XMLElement(FIELDNAME_AUTHOR,getIdegaXMLNameSpace()).setText(getAuthor());
		XMLElement source = new XMLElement(FIELDNAME_SOURCE,getIdegaXMLNameSpace()).setText(getSource());
		XMLElement articleComment = new XMLElement("article_comment",getIdegaXMLNameSpace()).setText(getComment());

		XMLElement body = new XMLElement(FIELDNAME_BODY,getIdegaXMLNameSpace());
		if(bodyElement != null){
			body.addContent(bodyElement);
		}

		XMLElement teaser = new XMLElement(FIELDNAME_TEASER,getIdegaXMLNameSpace());
		if(teaserElement != null){
			teaser.addContent(teaserElement);
		}

		root.addContent(contentLanguage);
		root.addContent(headline);
		root.addContent(teaser);
		root.addContent(body);
		root.addContent(author);
		root.addContent(source);
		root.addContent(articleComment);
		XMLDocument doc = new XMLDocument(root);
		XMLOutput outputter = new XMLOutput();
		return outputter.outputString(doc);		
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

			try {
				IWUserContext iwuc = IWContext.getInstance();
				//IWSlideSession session = (IWSlideSession)IBOLookup.getSessionInstance(iwuc,IWSlideSession.class);
				IWSlideSession session = getIWSlideSession(iwuc);
				WebdavRootResource rootResource = session.getWebdavRootResource();
	
				//Setting the path for creating new file/creating localized version/updating existing file
				String filePath=getResourcePath();
				
				String article = getAsXML();
				
				ByteArrayInputStream utf8stream = new ByteArrayInputStream(article.getBytes("UTF-8"));
				
	//			System.out.println(article);
				
				//Conflict fix: uri for creating but path for updating
				//Note! This is a patch to what seems to be a bug in WebDav
				//Apparently in verion below works in some cases and the other in other cases.
				//Seems to be connected to creating files in folders created in same tomcat session or similar
				//not quite clear...
				
				if(rootResource.putMethod(filePath,utf8stream)){
					rootResource.proppatchMethod(filePath,ArticleItemBean.PROPERTY_CONTENT_TYPE,ArticleItemBean.ARTICLE_FILENAME_SCOPE,true);
				}
				else{
					utf8stream = new ByteArrayInputStream(article.getBytes("UTF-8"));
					String fixedURL = session.getURI(filePath);
					rootResource.putMethod(fixedURL,utf8stream);
					rootResource.proppatchMethod(fixedURL,ArticleItemBean.PROPERTY_CONTENT_TYPE,ArticleItemBean.ARTICLE_FILENAME_SCOPE,true);
				}
				
				rootResource.close();
				try {
					//load(filePath);
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
		String text = toPrettify;
		if(text!=null){
			//Use JTidy to clean up the html
			Tidy tidy = new Tidy();
			tidy.setXHTML(true);
			tidy.setXmlOut(true);
			tidy.setShowWarnings(false);
			tidy.setCharEncoding(Configuration.UTF8);
			ByteArrayInputStream bais;
			try {
				bais = new ByteArrayInputStream(text.getBytes("UTF-8"));
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				
				tidy.parse(bais, baos);
				text = baos.toString("UTF-8");
			}
			catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			
			
			text = removeAbsoluteReferences(text);
		}
		return text;
	}

	private String removeAbsoluteReferences(String text) {
		StringBuffer replaceBuffer = new StringBuffer(text);
		ArrayList patterns = new ArrayList();
		Pattern p1 = Pattern.compile("(<a[^>]+href=\")([^#][^\"]+)([^>]+>)",Pattern.CASE_INSENSITIVE);
		Pattern p2 = Pattern.compile("(<img[^>]+src=\")([^#][^\"]+)([^>]+>)",Pattern.CASE_INSENSITIVE);
		patterns.add(p1);
		patterns.add(p2);
		
		StringBuffer outString;

		Iterator patternIter = patterns.iterator();
		while (patternIter.hasNext()) {
			Pattern p = (Pattern) patternIter.next();
			Matcher m = p.matcher(replaceBuffer);
			outString = new StringBuffer();

			while (m.find()) {

				String url = m.group(2);
				if (url.startsWith("http") && url.indexOf(IWContext.getInstance().getServerName()) > 0) {
					url = url.substring(url.indexOf("//")+2);
					url = url.substring(url.indexOf("/"));

					m.appendReplacement(outString,"$1"+url+"$3");
				}
			}
			m.appendTail(outString);
			replaceBuffer=new StringBuffer(outString.toString());

		}
		text = replaceBuffer.toString();
		return text;
	}
	
	/**
	 * Loads an xml file specified by the webdav resource
	 * The beans atributes are then set according to the information in the XML file
	 */
	protected boolean load(WebdavExtendedResource webdavResource) throws IOException {
		XMLParser builder = new XMLParser();
		XMLDocument bodyDoc = null;
		try {
			
			if(webdavResource.isCollection()){
				throw new RuntimeException(webdavResource+" is folder but should be file");
			}
			WebdavResource theArticle = webdavResource;
			

			if(theArticle!=null && !theArticle.isCollection()){
				//setArticleResourcePath(theArticle.getPath());
				setResourcePath(theArticle.getPath());
				//String inStr = theArticle.getMethodDataAsString();
				//byte[] bytes = inStr.getBytes("UTF-8");
				//InputStream inStream = new ByteArrayInputStream(bytes);
				InputStream inStream = theArticle.getMethodData();
				bodyDoc = builder.parse(inStream);
			} else {
				bodyDoc = null;
			}
		} catch (XMLException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
		if(bodyDoc!=null){
			XMLElement rootElement = bodyDoc.getRootElement();
	
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
					setHeadline("");
				}
			}catch(Exception e) {		//Nullpointer could occur if field isn't used
				e.printStackTrace();
				setHeadline("");
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
		//			e.printStackTrace();
					Logger log = Logger.getLogger(this.getClass().toString());
					log.warning("Teaser of article is empty");
					setTeaser("");
				}
				}catch(Exception e) {		//Nullpointer could occur if field isn't used
					e.printStackTrace();
					setTeaser("");
				}
			try {
				XMLElement author = rootElement.getChild(FIELDNAME_AUTHOR,getIdegaXMLNameSpace());
				if(author != null){
					setAuthor(author.getText());
				} else {
					setAuthor("");
				}
			}catch(Exception e) {		//Nullpointer could occur if field isn't used
				e.printStackTrace();
				setAuthor("");
			}
	
			//Parse out the body
			try {
				XMLNamespace htmlNamespace = new XMLNamespace("http://www.w3.org/1999/xhtml");
				XMLElement bodyElement = rootElement.getChild(FIELDNAME_BODY,getIdegaXMLNameSpace());
				XMLElement htmlElement = bodyElement.getChild("html",htmlNamespace);
				XMLElement htmlBodyElement = htmlElement.getChild("body",htmlNamespace);
				
				String bodyValue = htmlBodyElement.getContentAsString();
				setBody(bodyValue);
			}catch(Exception e) {		//Nullpointer could occur if field isn't used
	//			e.printStackTrace();
				Logger log = Logger.getLogger(this.getClass().toString());
				log.warning("Body of article is empty");
				setBody("");
			}
			
			try {
				XMLElement source = rootElement.getChild(FIELDNAME_SOURCE,getIdegaXMLNameSpace());
				if(source != null){
					setSource(source.getText());
				} else {
					setSource("");
				}
			}catch(Exception e) {		//Nullpointer could occur if field isn't used
				setSource("");
			}
			try {
				XMLElement comment = rootElement.getChild(FIELDNAME_ARTICLE_COMMENT,getIdegaXMLNameSpace());
				if(comment != null){
					setComment(comment.getText());
				} else {
					setComment("");
				}
			}catch(Exception e) {		//Nullpointer could occur if field isn't used
				e.printStackTrace();
				setComment("");
			}
		} else {
			//article not found
			Logger log = Logger.getLogger(this.getClass().toString());
			log.warning("Article xml file was not found");
			setRendered(false);
			return false;
		}
		return true;
//	    setFilename();
//		setFolderLocation(bodyElement.getChild(FIELDNAME_FOLDER_LOCATION,idegans).getText());
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
	
}