/**
 * 
 */
package com.idega.block.article.importer;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import com.idega.core.business.DefaultSpringBean;
import com.idega.idegaweb.IWMainSlideStartedEvent;

/**
 * Imports articles and their categories to database.
 * @author martynas
 * Last changed: 2011.05.18
 * You can report about problems to: <a href="mailto:martynas@idega.com">Martynas Stakė</a>
 * AIM: lapiukshtiss
 * Skype: lapiukshtiss
 * You can expect to find some test cases notice in the end of the file.
 */
public class ArticlesImporter extends DefaultSpringBean implements ApplicationListener {

	/* (non-Javadoc)
	 * @see org.springframework.context.ApplicationListener#onApplicationEvent(org.springframework.context.ApplicationEvent)
	 */
	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		// TODO Imformuoja, kad galima naudotis /cms/file...
		// TODO Sukurti application property bus naudojamas nustayti, pažiūrėti, ar jau importuota.
		// TODO DefaultSpringBean pasinaudojant paslaugimis
		// TODO Importuotojai turi būti du: kategorijoms ir articles
		// TODO Pakomitinti
		if (event instanceof IWMainSlideStartedEvent){
			this.getApplication().getSettings().getBoolean("Is categories imported", Boolean.FALSE);
			// TODO Šioje vietoje paleisti importerį, jei neimportuota.
			
		}
		
		// TODO Šioje klasėje padarome metoodus categorijoms ir articlams importinti
	}

}
