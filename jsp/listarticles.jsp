<?xml version="1.0"?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page"
        xmlns:h="http://java.sun.com/jsf/html"
        xmlns:jsf="http://java.sun.com/jsf/core"
        xmlns:t="http://myfaces.apache.org/tomahawk"
        xmlns:ws="http://xmlns.idega.com/com.idega.workspace"
        xmlns:wf="http://xmlns.idega.com/com.idega.webface"
        xmlns:article="http://xmlns.idega.com/com.idega.block.article"
        xmlns:co="http://xmlns.idega.com/com.idega.content"  
version="1.2">
<jsp:directive.page contentType="text/html" pageEncoding="UTF-8"/>
	<jsf:view>
		<ws:page id="listarticles1" javascripturls="
				/idegaweb/bundles/com.idega.block.web2.0.bundle/resources/javascript/jquery/1.2.3/jquery-compressed.js,
				/idegaweb/bundles/com.idega.block.article.bundle/resources/javascript/article-list.js">
			<h:form styleClass="articleListContent" id="listarticlesform1">
				<wf:wfblock id="article_list_block" title="#{localizedStrings['com.idega.block.article']['list_articles']}" maximizedVertically="true">
					<t:div styleClass="articleHeadings">
						<t:div styleClass="articleDate"><h:outputText value="#{localizedStrings['com.idega.block.article']['article_date']}" /></t:div>
						<t:div styleClass="articleTitle"><h:outputText value="#{localizedStrings['com.idega.block.article']['article_title']}" /></t:div>
						<t:div styleClass="articleAuthor"><h:outputText value="#{localizedStrings['com.idega.block.article']['article_author']}" /></t:div>
					</t:div>
					<article:ArticleListViewer id="article_list" resourcePath="/files/cms/article" detailsViewerPath="/workspace/content/article/preview" showBody="false" headlineAsLink="true" showTime="false" showComments="false" />
				</wf:wfblock>
				<t:div styleClass="articleListButtons"></t:div>
			</h:form>
		</ws:page>
	</jsf:view>
</jsp:root>