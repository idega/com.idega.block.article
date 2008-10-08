package com.idega.block.article.business;

import com.idega.presentation.IWContext;
import com.sun.syndication.feed.atom.Feed;

public interface CommentsPersistenceManager {

	public boolean hasRightsToViewComments(String processInstanceId);
	
	public boolean hasRightsToViewComments(Long processInstanceId);
	
	public String getLinkToCommentsXML(String processInstanceId);
	
	public String getFeedTitle(IWContext iwc, String processInstanceId);
	
	public String getFeedSubtitle(IWContext iwc, String processInstanceId);
	
	public boolean storeFeed(String processInstanceId, Feed comments);
	
	public Feed getCommentsFeed(IWContext iwc, String processInstanceId);
}
