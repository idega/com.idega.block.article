<?xml version="1.0"?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page"
        xmlns:h="http://java.sun.com/jsf/html"
        xmlns:jsf="http://java.sun.com/jsf/core"
        xmlns:ws="http://xmlns.idega.com/com.idega.workspace"
        xmlns:wf="http://xmlns.idega.com/com.idega.webface"
        xmlns:article="http://xmlns.idega.com/com.idega.block.article"
        xmlns:co="http://xmlns.idega.com/com.idega.content"
version="1.2">

	<jsf:view>
		<ws:page>
			<h:form>
				<wf:wfblock id="article_list_block" title="Article List">
					<co:ContentItemListViewer id="article_list" beanIdentifier="articleItemListBean" resourcePath="/files/cms/article" detailsViewerPath="/workspace/content/article/preview" />
				</wf:wfblock>
			</h:form>
		</ws:page>
	</jsf:view>
</jsp:root>