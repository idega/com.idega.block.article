<?xml version="1.0"?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page"
	xmlns:html="http://java.sun.com/jsf/html"
	xmlns:jsf="http://java.sun.com/jsf/core"
	xmlns:w="http://xmlns.idega.com/com.idega.webface"
	xmlns:a="http://xmlns.idega.com/com.idega.block.article"
version="1.2">

<jsf:view>
	<jsp:output omit-xml-declaration="false" doctype-root-element="html" doctype-public="-//W3C//DTD XHTML 1.1//EN" doctype-system="http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd"/>
	<html xmlns="http://www.w3.org/1999/xhtml">
	<body>
		<html:form>
			<a:listarticlesblock id="list_articles_block"/>
		</html:form>
	</body>
	</html>
</jsf:view>
</jsp:root>
