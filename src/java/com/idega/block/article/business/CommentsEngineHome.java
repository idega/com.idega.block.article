package com.idega.block.article.business;


import javax.ejb.CreateException;
import com.idega.business.IBOHome;
import java.rmi.RemoteException;

public interface CommentsEngineHome extends IBOHome {
	public CommentsEngine create() throws CreateException, RemoteException;
}