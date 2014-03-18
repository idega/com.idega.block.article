package com.idega.block.article.bean.article_view;

import java.io.Serializable;

import org.directwebremoting.annotations.DataTransferObject;
import org.directwebremoting.annotations.RemoteProperty;

@DataTransferObject
public class Responce implements Serializable{
	private static final long serialVersionUID = -2878378701373656472L;

	private String status;
	
	private String message;
	
	private String nextRoute;

	@RemoteProperty
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@RemoteProperty
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	

	@RemoteProperty
	public String getNextRoute() {
		return nextRoute;
	}

	public void setNextRoute(String nextRoute) {
		this.nextRoute = nextRoute;
	}
}
