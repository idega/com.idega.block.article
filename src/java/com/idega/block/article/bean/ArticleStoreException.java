package com.idega.block.article.bean;


/**
 * <p>
 * Exception throwed when failure in storing article.
 * </p>
 * @author Joakim
 */
public class ArticleStoreException extends RuntimeException{
	
	/**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 4646957124496443145L;
	
	public final static String KP = "error_"; // Key prefix
	public final static String KEY_ERROR_HEADLINE_EMPTY = KP + "headline_empty";
	public final static String KEY_ERROR_BODY_EMPTY = KP + "body_empty";
	public final static String KEY_ERROR_PUBLISHED_FROM_DATE_EMPTY = KP + "published_from_date_empty";
	public final static String KEY_ERROR_ON_STORE = KP + "error_saving";
	
	private String errorKey;
	
	public ArticleStoreException() {
		super();
	}

	public ArticleStoreException(String s) {
		super(s);
	}
	
	public ArticleStoreException(Exception e){
		super(e);
	}
	
	/**
	 * @return Returns the errorKey.
	 */
	public String getErrorKey() {
		return errorKey;
	}
	/**
	 * @param errorKey The errorKey to set.
	 */
	public void setErrorKey(String errorKey) {
		this.errorKey = errorKey;
	}
	
}
