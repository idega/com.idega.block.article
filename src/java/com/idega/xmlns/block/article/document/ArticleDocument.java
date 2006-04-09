/*
 * An XML document type.
 * Localname: article
 * Namespace: http://xmlns.idega.com/block/article/document
 * Java type: com.idega.xmlns.block.article.document.ArticleDocument
 *
 * Automatically generated - do not modify.
 */
package com.idega.xmlns.block.article.document;


/**
 * A document containing one article(@http://xmlns.idega.com/block/article/document) element.
 *
 * This is a complex type.
 */
public interface ArticleDocument extends org.apache.xmlbeans.XmlObject
{
    public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)schema.system.sD476AFB62C28EC9C6E8B3794CEE51FAD.TypeSystemHolder.typeSystem.resolveHandle("articlede4fdoctype");
    
    /**
     * Gets the "article" element
     */
    com.idega.xmlns.block.article.document.ArticleDocument.Article getArticle();
    
    /**
     * Sets the "article" element
     */
    void setArticle(com.idega.xmlns.block.article.document.ArticleDocument.Article article);
    
    /**
     * Appends and returns a new empty "article" element
     */
    com.idega.xmlns.block.article.document.ArticleDocument.Article addNewArticle();
    
    /**
     * An XML article(@http://xmlns.idega.com/block/article/document).
     *
     * This is a complex type.
     */
    public interface Article extends org.apache.xmlbeans.XmlObject
    {
        public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)schema.system.sD476AFB62C28EC9C6E8B3794CEE51FAD.TypeSystemHolder.typeSystem.resolveHandle("articleb9b1elemtype");
        
        /**
         * Gets the "headline" element
         */
        java.lang.String getHeadline();
        
        /**
         * Gets (as xml) the "headline" element
         */
        org.apache.xmlbeans.XmlString xgetHeadline();
        
        /**
         * Sets the "headline" element
         */
        void setHeadline(java.lang.String headline);
        
        /**
         * Sets (as xml) the "headline" element
         */
        void xsetHeadline(org.apache.xmlbeans.XmlString headline);
        
        /**
         * Gets the "teaser" element
         */
        java.lang.String getTeaser();
        
        /**
         * Gets (as xml) the "teaser" element
         */
        org.apache.xmlbeans.XmlString xgetTeaser();
        
        /**
         * Sets the "teaser" element
         */
        void setTeaser(java.lang.String teaser);
        
        /**
         * Sets (as xml) the "teaser" element
         */
        void xsetTeaser(org.apache.xmlbeans.XmlString teaser);
        
        /**
         * Gets the "body" element
         */
        java.lang.String getBody();
        
        /**
         * Gets (as xml) the "body" element
         */
        org.apache.xmlbeans.XmlString xgetBody();
        
        /**
         * Sets the "body" element
         */
        void setBody(java.lang.String body);
        
        /**
         * Sets (as xml) the "body" element
         */
        void xsetBody(org.apache.xmlbeans.XmlString body);
        
        /**
         * Gets the "author" element
         */
        java.lang.String getAuthor();
        
        /**
         * Gets (as xml) the "author" element
         */
        org.apache.xmlbeans.XmlString xgetAuthor();
        
        /**
         * Sets the "author" element
         */
        void setAuthor(java.lang.String author);
        
        /**
         * Sets (as xml) the "author" element
         */
        void xsetAuthor(org.apache.xmlbeans.XmlString author);
        
        /**
         * Gets the "source" element
         */
        java.lang.String getSource();
        
        /**
         * Gets (as xml) the "source" element
         */
        org.apache.xmlbeans.XmlString xgetSource();
        
        /**
         * Sets the "source" element
         */
        void setSource(java.lang.String source);
        
        /**
         * Sets (as xml) the "source" element
         */
        void xsetSource(org.apache.xmlbeans.XmlString source);
        
        /**
         * Gets the "comment" element
         */
        java.lang.String getComment();
        
        /**
         * Gets (as xml) the "comment" element
         */
        org.apache.xmlbeans.XmlString xgetComment();
        
        /**
         * Sets the "comment" element
         */
        void setComment(java.lang.String comment);
        
        /**
         * Sets (as xml) the "comment" element
         */
        void xsetComment(org.apache.xmlbeans.XmlString comment);
        
        /**
         * Gets the "image" element
         */
        java.lang.String getImage();
        
        /**
         * Gets (as xml) the "image" element
         */
        org.apache.xmlbeans.XmlString xgetImage();
        
        /**
         * Sets the "image" element
         */
        void setImage(java.lang.String image);
        
        /**
         * Sets (as xml) the "image" element
         */
        void xsetImage(org.apache.xmlbeans.XmlString image);
        
        /**
         * Gets the "attachment" element
         */
        java.lang.String getAttachment();
        
        /**
         * Gets (as xml) the "attachment" element
         */
        org.apache.xmlbeans.XmlString xgetAttachment();
        
        /**
         * Sets the "attachment" element
         */
        void setAttachment(java.lang.String attachment);
        
        /**
         * Sets (as xml) the "attachment" element
         */
        void xsetAttachment(org.apache.xmlbeans.XmlString attachment);
        
        /**
         * Gets the "related_items" element
         */
        java.lang.String getRelatedItems();
        
        /**
         * Gets (as xml) the "related_items" element
         */
        org.apache.xmlbeans.XmlString xgetRelatedItems();
        
        /**
         * Sets the "related_items" element
         */
        void setRelatedItems(java.lang.String relatedItems);
        
        /**
         * Sets (as xml) the "related_items" element
         */
        void xsetRelatedItems(org.apache.xmlbeans.XmlString relatedItems);
        
        /**
         * A factory class with static methods for creating instances
         * of this type.
         */
        
        public static final class Factory
        {
            public static com.idega.xmlns.block.article.document.ArticleDocument.Article newInstance() {
              return (com.idega.xmlns.block.article.document.ArticleDocument.Article) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
            
            public static com.idega.xmlns.block.article.document.ArticleDocument.Article newInstance(org.apache.xmlbeans.XmlOptions options) {
              return (com.idega.xmlns.block.article.document.ArticleDocument.Article) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
            
            private Factory() {
          		//No action...
            } // No instance of this class allowed
        }
    }
    
    /**
     * A factory class with static methods for creating instances
     * of this type.
     */
    
    public static final class Factory
    {
        public static com.idega.xmlns.block.article.document.ArticleDocument newInstance() {
          return (com.idega.xmlns.block.article.document.ArticleDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
        
        public static com.idega.xmlns.block.article.document.ArticleDocument newInstance(org.apache.xmlbeans.XmlOptions options) {
          return (com.idega.xmlns.block.article.document.ArticleDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
        
        public static com.idega.xmlns.block.article.document.ArticleDocument parse(java.lang.String s) throws org.apache.xmlbeans.XmlException {
          return (com.idega.xmlns.block.article.document.ArticleDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( s, type, null ); }
        
        public static com.idega.xmlns.block.article.document.ArticleDocument parse(java.lang.String s, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (com.idega.xmlns.block.article.document.ArticleDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( s, type, options ); }
        
        public static com.idega.xmlns.block.article.document.ArticleDocument parse(java.io.File f) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (com.idega.xmlns.block.article.document.ArticleDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( f, type, null ); }
        
        public static com.idega.xmlns.block.article.document.ArticleDocument parse(java.io.File f, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (com.idega.xmlns.block.article.document.ArticleDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( f, type, options ); }
        
        public static com.idega.xmlns.block.article.document.ArticleDocument parse(java.net.URL u) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (com.idega.xmlns.block.article.document.ArticleDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, null ); }
        
        public static com.idega.xmlns.block.article.document.ArticleDocument parse(java.net.URL u, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (com.idega.xmlns.block.article.document.ArticleDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, options ); }
        
        public static com.idega.xmlns.block.article.document.ArticleDocument parse(java.io.InputStream is) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (com.idega.xmlns.block.article.document.ArticleDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, null ); }
        
        public static com.idega.xmlns.block.article.document.ArticleDocument parse(java.io.InputStream is, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (com.idega.xmlns.block.article.document.ArticleDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, options ); }
        
        public static com.idega.xmlns.block.article.document.ArticleDocument parse(java.io.Reader r) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (com.idega.xmlns.block.article.document.ArticleDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, null ); }
        
        public static com.idega.xmlns.block.article.document.ArticleDocument parse(java.io.Reader r, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (com.idega.xmlns.block.article.document.ArticleDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, options ); }
        
        public static com.idega.xmlns.block.article.document.ArticleDocument parse(org.w3c.dom.Node node) throws org.apache.xmlbeans.XmlException {
          return (com.idega.xmlns.block.article.document.ArticleDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, null ); }
        
        public static com.idega.xmlns.block.article.document.ArticleDocument parse(org.w3c.dom.Node node, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (com.idega.xmlns.block.article.document.ArticleDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, options ); }
        
        public static com.idega.xmlns.block.article.document.ArticleDocument parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (com.idega.xmlns.block.article.document.ArticleDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, null ); }
        
        public static com.idega.xmlns.block.article.document.ArticleDocument parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (com.idega.xmlns.block.article.document.ArticleDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, options ); }
        
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, null ); }
        
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, options ); }
        
        private Factory() {
      		//No action...
        } // No instance of this class allowed
    }
}
