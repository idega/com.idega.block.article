/*
 * An XML document type.
 * Localname: article
 * Namespace: http://xmlns.idega.com/block/article/document
 * Java type: com.idega.xmlns.block.article.document.ArticleDocument
 *
 * Automatically generated - do not modify.
 */
package com.idega.xmlns.block.article.document.impl;
/**
 * A document containing one article(@http://xmlns.idega.com/block/article/document) element.
 *
 * This is a complex type.
 */
public class ArticleDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements com.idega.xmlns.block.article.document.ArticleDocument
{

	private static final long serialVersionUID = -93158179085399290L;


	public ArticleDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }

    private static final javax.xml.namespace.QName ARTICLE$0 =
        new javax.xml.namespace.QName("http://xmlns.idega.com/block/article/document", "article");


    /**
     * Gets the "article" element
     */
    @Override
	public com.idega.xmlns.block.article.document.ArticleDocument.Article getArticle()
    {
        synchronized (monitor())
        {
            check_orphaned();
            com.idega.xmlns.block.article.document.ArticleDocument.Article target = null;
            target = (com.idega.xmlns.block.article.document.ArticleDocument.Article)get_store().find_element_user(ARTICLE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }

    /**
     * Sets the "article" element
     */
    @Override
	public void setArticle(com.idega.xmlns.block.article.document.ArticleDocument.Article article)
    {
        synchronized (monitor())
        {
            check_orphaned();
            com.idega.xmlns.block.article.document.ArticleDocument.Article target = null;
            target = (com.idega.xmlns.block.article.document.ArticleDocument.Article)get_store().find_element_user(ARTICLE$0, 0);
            if (target == null)
            {
                target = (com.idega.xmlns.block.article.document.ArticleDocument.Article)get_store().add_element_user(ARTICLE$0);
            }
            target.set(article);
        }
    }

    /**
     * Appends and returns a new empty "article" element
     */
    @Override
	public com.idega.xmlns.block.article.document.ArticleDocument.Article addNewArticle()
    {
        synchronized (monitor())
        {
            check_orphaned();
            com.idega.xmlns.block.article.document.ArticleDocument.Article target = null;
            target = (com.idega.xmlns.block.article.document.ArticleDocument.Article)get_store().add_element_user(ARTICLE$0);
            return target;
        }
    }
    /**
     * An XML article(@http://xmlns.idega.com/block/article/document).
     *
     * This is a complex type.
     */
    public static class ArticleImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements com.idega.xmlns.block.article.document.ArticleDocument.Article
    {

		private static final long serialVersionUID = -2386940032025715997L;


		public ArticleImpl(org.apache.xmlbeans.SchemaType sType)
        {
            super(sType);
        }

        private static final javax.xml.namespace.QName HEADLINE$0 =
            new javax.xml.namespace.QName("http://xmlns.idega.com/block/article/document", "headline");
        private static final javax.xml.namespace.QName TEASER$2 =
            new javax.xml.namespace.QName("http://xmlns.idega.com/block/article/document", "teaser");
        private static final javax.xml.namespace.QName BODY$4 =
            new javax.xml.namespace.QName("http://xmlns.idega.com/block/article/document", "body");
        private static final javax.xml.namespace.QName AUTHOR$6 =
            new javax.xml.namespace.QName("http://xmlns.idega.com/block/article/document", "author");
        private static final javax.xml.namespace.QName SOURCE$8 =
            new javax.xml.namespace.QName("http://xmlns.idega.com/block/article/document", "source");
        private static final javax.xml.namespace.QName COMMENT$10 =
            new javax.xml.namespace.QName("http://xmlns.idega.com/block/article/document", "comment");
        private static final javax.xml.namespace.QName IMAGE$12 =
            new javax.xml.namespace.QName("http://xmlns.idega.com/block/article/document", "image");
        private static final javax.xml.namespace.QName ATTACHMENT$14 =
            new javax.xml.namespace.QName("http://xmlns.idega.com/block/article/document", "attachment");
        private static final javax.xml.namespace.QName RELATEDITEMS$16 =
            new javax.xml.namespace.QName("http://xmlns.idega.com/block/article/document", "related_items");


        /**
         * Gets the "headline" element
         */
        @Override
		public java.lang.String getHeadline()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.SimpleValue target = null;
                target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(HEADLINE$0, 0);
                if (target == null)
                {
                    return null;
                }
                return target.getStringValue();
            }
        }

        /**
         * Gets (as xml) the "headline" element
         */
        @Override
		public org.apache.xmlbeans.XmlString xgetHeadline()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.XmlString target = null;
                target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(HEADLINE$0, 0);
                return target;
            }
        }

        /**
         * Sets the "headline" element
         */
        @Override
		public void setHeadline(java.lang.String headline)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.SimpleValue target = null;
                target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(HEADLINE$0, 0);
                if (target == null)
                {
                    target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(HEADLINE$0);
                }
                target.setStringValue(headline);
            }
        }

        /**
         * Sets (as xml) the "headline" element
         */
        @Override
		public void xsetHeadline(org.apache.xmlbeans.XmlString headline)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.XmlString target = null;
                target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(HEADLINE$0, 0);
                if (target == null)
                {
                    target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(HEADLINE$0);
                }
                target.set(headline);
            }
        }

        /**
         * Gets the "teaser" element
         */
        @Override
		public java.lang.String getTeaser()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.SimpleValue target = null;
                target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(TEASER$2, 0);
                if (target == null)
                {
                    return null;
                }
                return target.getStringValue();
            }
        }

        /**
         * Gets (as xml) the "teaser" element
         */
        @Override
		public org.apache.xmlbeans.XmlString xgetTeaser()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.XmlString target = null;
                target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(TEASER$2, 0);
                return target;
            }
        }

        /**
         * Sets the "teaser" element
         */
        @Override
		public void setTeaser(java.lang.String teaser)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.SimpleValue target = null;
                target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(TEASER$2, 0);
                if (target == null)
                {
                    target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(TEASER$2);
                }
                target.setStringValue(teaser);
            }
        }

        /**
         * Sets (as xml) the "teaser" element
         */
        @Override
		public void xsetTeaser(org.apache.xmlbeans.XmlString teaser)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.XmlString target = null;
                target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(TEASER$2, 0);
                if (target == null)
                {
                    target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(TEASER$2);
                }
                target.set(teaser);
            }
        }

        /**
         * Gets the "body" element
         */
        @Override
		public java.lang.String getBody()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.SimpleValue target = null;
                target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(BODY$4, 0);
                if (target == null)
                {
                    return null;
                }
                return target.getStringValue();
            }
        }

        /**
         * Gets (as xml) the "body" element
         */
        @Override
		public org.apache.xmlbeans.XmlString xgetBody()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.XmlString target = null;
                target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(BODY$4, 0);
                return target;
            }
        }

        /**
         * Sets the "body" element
         */
        @Override
		public void setBody(java.lang.String body)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.SimpleValue target = null;
                target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(BODY$4, 0);
                if (target == null)
                {
                    target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(BODY$4);
                }
                target.setStringValue(body);
            }
        }

        /**
         * Sets (as xml) the "body" element
         */
        @Override
		public void xsetBody(org.apache.xmlbeans.XmlString body)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.XmlString target = null;
                target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(BODY$4, 0);
                if (target == null)
                {
                    target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(BODY$4);
                }
                target.set(body);
            }
        }

        /**
         * Gets the "author" element
         */
        @Override
		public java.lang.String getAuthor()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.SimpleValue target = null;
                target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(AUTHOR$6, 0);
                if (target == null)
                {
                    return null;
                }
                return target.getStringValue();
            }
        }

        /**
         * Gets (as xml) the "author" element
         */
        @Override
		public org.apache.xmlbeans.XmlString xgetAuthor()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.XmlString target = null;
                target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(AUTHOR$6, 0);
                return target;
            }
        }

        /**
         * Sets the "author" element
         */
        @Override
		public void setAuthor(java.lang.String author)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.SimpleValue target = null;
                target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(AUTHOR$6, 0);
                if (target == null)
                {
                    target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(AUTHOR$6);
                }
                target.setStringValue(author);
            }
        }

        /**
         * Sets (as xml) the "author" element
         */
        @Override
		public void xsetAuthor(org.apache.xmlbeans.XmlString author)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.XmlString target = null;
                target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(AUTHOR$6, 0);
                if (target == null)
                {
                    target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(AUTHOR$6);
                }
                target.set(author);
            }
        }

        /**
         * Gets the "source" element
         */
        @Override
		public java.lang.String getSource()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.SimpleValue target = null;
                target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(SOURCE$8, 0);
                if (target == null)
                {
                    return null;
                }
                return target.getStringValue();
            }
        }

        /**
         * Gets (as xml) the "source" element
         */
        @Override
		public org.apache.xmlbeans.XmlString xgetSource()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.XmlString target = null;
                target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(SOURCE$8, 0);
                return target;
            }
        }

        /**
         * Sets the "source" element
         */
        @Override
		public void setSource(java.lang.String source)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.SimpleValue target = null;
                target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(SOURCE$8, 0);
                if (target == null)
                {
                    target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(SOURCE$8);
                }
                target.setStringValue(source);
            }
        }

        /**
         * Sets (as xml) the "source" element
         */
        @Override
		public void xsetSource(org.apache.xmlbeans.XmlString source)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.XmlString target = null;
                target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(SOURCE$8, 0);
                if (target == null)
                {
                    target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(SOURCE$8);
                }
                target.set(source);
            }
        }

        /**
         * Gets the "comment" element
         */
        @Override
		public java.lang.String getComment()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.SimpleValue target = null;
                target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(COMMENT$10, 0);
                if (target == null)
                {
                    return null;
                }
                return target.getStringValue();
            }
        }

        /**
         * Gets (as xml) the "comment" element
         */
        @Override
		public org.apache.xmlbeans.XmlString xgetComment()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.XmlString target = null;
                target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(COMMENT$10, 0);
                return target;
            }
        }

        /**
         * Sets the "comment" element
         */
        @Override
		public void setComment(java.lang.String comment)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.SimpleValue target = null;
                target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(COMMENT$10, 0);
                if (target == null)
                {
                    target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(COMMENT$10);
                }
                target.setStringValue(comment);
            }
        }

        /**
         * Sets (as xml) the "comment" element
         */
        @Override
		public void xsetComment(org.apache.xmlbeans.XmlString comment)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.XmlString target = null;
                target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(COMMENT$10, 0);
                if (target == null)
                {
                    target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(COMMENT$10);
                }
                target.set(comment);
            }
        }

        /**
         * Gets the "image" element
         */
        @Override
		public java.lang.String getImage()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.SimpleValue target = null;
                target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(IMAGE$12, 0);
                if (target == null)
                {
                    return null;
                }
                return target.getStringValue();
            }
        }

        /**
         * Gets (as xml) the "image" element
         */
        @Override
		public org.apache.xmlbeans.XmlString xgetImage()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.XmlString target = null;
                target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(IMAGE$12, 0);
                return target;
            }
        }

        /**
         * Sets the "image" element
         */
        @Override
		public void setImage(java.lang.String image)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.SimpleValue target = null;
                target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(IMAGE$12, 0);
                if (target == null)
                {
                    target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(IMAGE$12);
                }
                target.setStringValue(image);
            }
        }

        /**
         * Sets (as xml) the "image" element
         */
        @Override
		public void xsetImage(org.apache.xmlbeans.XmlString image)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.XmlString target = null;
                target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(IMAGE$12, 0);
                if (target == null)
                {
                    target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(IMAGE$12);
                }
                target.set(image);
            }
        }

        /**
         * Gets the "attachment" element
         */
        @Override
		public java.lang.String getAttachment()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.SimpleValue target = null;
                target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(ATTACHMENT$14, 0);
                if (target == null)
                {
                    return null;
                }
                return target.getStringValue();
            }
        }

        /**
         * Gets (as xml) the "attachment" element
         */
        @Override
		public org.apache.xmlbeans.XmlString xgetAttachment()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.XmlString target = null;
                target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(ATTACHMENT$14, 0);
                return target;
            }
        }

        /**
         * Sets the "attachment" element
         */
        @Override
		public void setAttachment(java.lang.String attachment)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.SimpleValue target = null;
                target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(ATTACHMENT$14, 0);
                if (target == null)
                {
                    target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(ATTACHMENT$14);
                }
                target.setStringValue(attachment);
            }
        }

        /**
         * Sets (as xml) the "attachment" element
         */
        @Override
		public void xsetAttachment(org.apache.xmlbeans.XmlString attachment)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.XmlString target = null;
                target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(ATTACHMENT$14, 0);
                if (target == null)
                {
                    target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(ATTACHMENT$14);
                }
                target.set(attachment);
            }
        }

        /**
         * Gets the "related_items" element
         */
        @Override
		public java.lang.String getRelatedItems()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.SimpleValue target = null;
                target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(RELATEDITEMS$16, 0);
                if (target == null)
                {
                    return null;
                }
                return target.getStringValue();
            }
        }

        /**
         * Gets (as xml) the "related_items" element
         */
        @Override
		public org.apache.xmlbeans.XmlString xgetRelatedItems()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.XmlString target = null;
                target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(RELATEDITEMS$16, 0);
                return target;
            }
        }

        /**
         * Sets the "related_items" element
         */
        @Override
		public void setRelatedItems(java.lang.String relatedItems)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.SimpleValue target = null;
                target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(RELATEDITEMS$16, 0);
                if (target == null)
                {
                    target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(RELATEDITEMS$16);
                }
                target.setStringValue(relatedItems);
            }
        }

        /**
         * Sets (as xml) the "related_items" element
         */
        @Override
		public void xsetRelatedItems(org.apache.xmlbeans.XmlString relatedItems)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.XmlString target = null;
                target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(RELATEDITEMS$16, 0);
                if (target == null)
                {
                    target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(RELATEDITEMS$16);
                }
                target.set(relatedItems);
            }
        }
    }
}
