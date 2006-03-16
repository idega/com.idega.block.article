/**
 * 
 */
package com.idega.block.article;

import java.util.Map;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import com.idega.block.article.component.ArticleItemViewer;
import com.idega.core.cache.IWCacheManager2;
import com.idega.core.cache.UIComponentCacher;
import com.idega.idegaweb.IWMainApplication;


/**
 * <p>
 * TODO tryggvil Describe Type ArticleCacher
 * </p>
 *  Last modified: $Date: 2006/03/16 15:36:02 $ by $Author: tryggvil $
 * 
 * @author <a href="mailto:tryggvil@idega.com">tryggvil</a>
 * @version $Revision: 1.2 $
 */
public class ArticleCacher extends UIComponentCacher {
	
	public static final String BEAN_ID="articleCacher";
	private IWMainApplication iwma;
	
	public static ArticleCacher getInstance(IWMainApplication iwma){
		ArticleCacher instance = (ArticleCacher) iwma.getAttribute(BEAN_ID);
		if(instance==null){
			instance = new ArticleCacher();
			iwma.setAttribute(BEAN_ID,instance);
			instance.iwma=iwma;
		}
		return instance;
	}
	
	public Map getCacheMap() {
		IWCacheManager2 iwcm = IWCacheManager2.getInstance(iwma);
		
		return iwcm.getCache("article");
		
		//IWCacheManager cm = IWCacheManager.getInstance(IWMainApplication.getDefaultIWMainApplication());
		//return cm.getCacheMap();
	}

	/* (non-Javadoc)
	 * @see com.idega.core.cache.UIComponentCacher#getCacheKey(javax.faces.component.UIComponent, javax.faces.context.FacesContext)
	 */
	protected String getCacheKey(UIComponent component, FacesContext context) {
		String cacheKey = super.getCacheKey(component, context);
		
		return cacheKey;
	}

	/* (non-Javadoc)
	 * @see com.idega.core.cache.UIComponentCacher#isCacheEnbled(javax.faces.component.UIComponent, javax.faces.context.FacesContext)
	 */
	public boolean isCacheEnbled(UIComponent component, FacesContext context) {
		
		IWMainApplication iwma = IWMainApplication.getIWMainApplication(context);
		String prop = iwma.getSettings().getProperty("article.cache.enabled");
		boolean defaultEnabled = true;
		if(prop!=null){
			defaultEnabled = Boolean.valueOf(prop).booleanValue();
		}
		if(defaultEnabled){
			if(component.getId()==null){
				return false;
			}
			
			if(component instanceof ArticleItemViewer){
				ArticleItemViewer viewer = (ArticleItemViewer)component;
				return viewer.isCacheEnabled();
			}
			
			return true;
		}
		else{
			return false;
		}
	}
	
	
}
