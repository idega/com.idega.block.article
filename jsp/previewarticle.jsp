<?xml version="1.0"?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page"
        xmlns:h="http://java.sun.com/jsf/html"
        xmlns:jsf="http://java.sun.com/jsf/core"
        xmlns:ws="http://xmlns.idega.com/com.idega.workspace"
        xmlns:wf="http://xmlns.idega.com/com.idega.webface"
        xmlns:article="http://xmlns.idega.com/com.idega.block.article"
        xmlns:co="http://xmlns.idega.com/com.idega.content"
version="1.2">
<jsp:directive.page contentType="text/html" pageEncoding="UTF-8"/>
	<jsf:view>
		<ws:page stylesheeturls="/idegaweb/bundles/com.idega.content.bundle/resources/style/content.css,
								/idegaweb/bundles/com.idega.block.article.bundle/resources/style/article.css">
			<h:form>
				<wf:wfblock id="article_item_block" title="#{localizedStrings['com.idega.block.article']['preview_article']}">
					<article:ArticleItemViewer id="article_item" renderDetailsCommand="false" showRequestedItem="true" showTeaser="false" />
				</wf:wfblock>
			</h:form>
		</ws:page>
	</jsf:view>
</jsp:root>