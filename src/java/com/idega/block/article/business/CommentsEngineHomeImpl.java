package com.idega.block.article.business;


import javax.ejb.CreateException;
import com.idega.business.IBOHomeImpl;

public class CommentsEngineHomeImpl extends IBOHomeImpl implements CommentsEngineHome {

	private static final long serialVersionUID = -4510081484490343119L;

	public Class getBeanInterfaceClass() {
		return CommentsEngine.class;
	}

	public CommentsEngine create() throws CreateException {
		return (CommentsEngine) super.createIBO();
	}
}