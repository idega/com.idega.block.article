<?xml version="1.0"?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page"
        xmlns:html="http://java.sun.com/jsf/html"
        xmlns:jsf="http://java.sun.com/jsf/core"
        xmlns:ws="http://xmlns.idega.com/com.idega.workspace"
        xmlns:article="http://xmlns.idega.com/com.idega.block.article"
version="1.2">
<jsp:directive.page contentType="text/html" pageEncoding="UTF-8"/>
<jsf:view>
        <ws:page id="deletearticle1">
                <html:form id="deletearticleform1">
                        <article:articleAdminBlock mode="delete"/>
                </html:form>
        </ws:page>
</jsf:view>
</jsp:root>