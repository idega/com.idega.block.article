package com.idega.block.article.business;

import java.text.DateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

import com.idega.block.article.bean.ArticleComment;
import com.idega.util.StringUtil;

public class ArticleCommentComparator implements Comparator<ArticleComment> {

	private DateFormat dateFormatter;
	
	private Locale locale;
	
	private int dateStyle = -1;
	private int timeStyle = -1;
	
	private boolean newestEntriesOnTop = true;
	
	public ArticleCommentComparator(Locale locale, int dateStyle, int timeStyle, boolean newestEntriesOnTop) {
		this.locale = locale;
		this.dateStyle = dateStyle;
		this.timeStyle = timeStyle;
		this.newestEntriesOnTop = newestEntriesOnTop;
	}
	
	public int compare(ArticleComment comment1, ArticleComment comment2) {
		if (dateStyle < 0 || timeStyle < 0 || locale == null) {
			return 0;
		}
		
		String timestampValue1 = comment1.getPosted();
		String timestampValue2 = comment2.getPosted();
		if (StringUtil.isEmpty(timestampValue1) || StringUtil.isEmpty(timestampValue2)) {
			return 0;
		}
		
		if (dateFormatter == null) {
			dateFormatter = DateFormat.getDateTimeInstance(dateStyle, timeStyle, locale);
		}
		
		Date time1 = null;
		Date time2 = null;
		try {
			time1 = dateFormatter.parse(timestampValue1);
			time2 = dateFormatter.parse(timestampValue2);
		} catch(Exception e) {
			e.printStackTrace();
		}
		if (time1 == null || time2 == null) {
			return 0;
		}

		return newestEntriesOnTop ? -time1.compareTo(time2) : time1.compareTo(time2);
	}

}
