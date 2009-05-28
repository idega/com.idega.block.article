package com.idega.block.article.bean;

import java.util.ArrayList;
import java.util.List;

import com.idega.builder.bean.AdvancedProperty;
import com.sun.syndication.feed.atom.Entry;

public class CommentEntry extends Entry {

	private static final long serialVersionUID = 2053444469354043339L;

	private String primaryKey;
	
	private boolean publishable;
	private boolean readable;
	private boolean replyable;
	
	private List<AdvancedProperty> readers;
	
	public CommentEntry(Entry entry) {
		this.setAlternateLinks(entry.getAlternateLinks());
		this.setAuthors(entry.getAuthors());
		this.setCategories(entry.getCategories());
		this.setContents(entry.getContents());
		this.setContributors(entry.getContributors());
		this.setCreated(entry.getCreated());
		this.setForeignMarkup(entry.getForeignMarkup());
		this.setId(entry.getId());
		this.setIssued(entry.getIssued());
		this.setModified(entry.getModified());
		this.setModules(entry.getModules());
		this.setOtherLinks(entry.getOtherLinks());
		this.setPublished(entry.getPublished());
		this.setRights(entry.getRights());
		this.setSource(entry.getSource());
		this.setSummary(entry.getSummary());
		this.setTitle(entry.getTitle());
		this.setTitleEx(entry.getTitleEx());
		this.setUpdated(entry.getUpdated());
		this.setXmlBase(entry.getXmlBase());
	}
	
	public String getPrimaryKey() {
		return primaryKey;
	}

	public void setPrimaryKey(String primaryKey) {
		this.primaryKey = primaryKey;
	}

	public boolean isPublishable() {
		return publishable;
	}

	public void setPublishable(boolean publishable) {
		this.publishable = publishable;
	}

	public boolean isReadable() {
		return readable;
	}

	public void setReadable(boolean readable) {
		this.readable = readable;
	}

	public boolean isReplyable() {
		return replyable;
	}

	public void setReplyable(boolean replyable) {
		this.replyable = replyable;
	}

	public List<AdvancedProperty> getReaders() {
		return readers;
	}

	public void setReaders(List<AdvancedProperty> readers) {
		this.readers = readers;
	}

	public void addReader(AdvancedProperty readerInfo) {
		if (readers == null) {
			readers = new ArrayList<AdvancedProperty>();
		}
		readers.add(readerInfo);
	}
}
