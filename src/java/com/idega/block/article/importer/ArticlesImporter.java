/**
 *
 */
package com.idega.block.article.importer;

import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.idega.block.article.data.dao.CategoryDao;
import com.idega.content.business.categories.CategoriesEngine;
import com.idega.content.data.ContentCategory;
import com.idega.core.business.DefaultSpringBean;
import com.idega.core.localisation.business.ICLocaleBusiness;
import com.idega.idegaweb.IWMainSlideStartedEvent;
import com.idega.util.ListUtil;

/**
 * Imports articles and their categories to database.
 * @author martynas
 * Last changed: 2011.05.18
 * You can report about problems to: <a href="mailto:martynas@idega.com">Martynas Stakė</a>
 * AIM: lapiukshtiss
 * Skype: lapiukshtiss
 * You can expect to find some test cases notice in the end of the file.
 */

@Service
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class ArticlesImporter extends DefaultSpringBean implements ApplicationListener {

	@Autowired
	private CategoriesEngine categoryEngine;

	@Autowired
	private CategoryDao categoryDao;

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
			// TODO Šioje vietoje paleisti importerį, jei neimportuota.
			if(!this.getApplication().getSettings().getBoolean("is_categories_imported", Boolean.FALSE)){
				this.importCategories();
				this.getApplication().getSettings().setProperty("is_categories_imported", Boolean.FALSE.toString());
			}
		}

		// TODO Šioje klasėje padarome metoodus categorijoms ir articlams importinti
	}

	public void importCategories(){
		List<Locale> localeList = ICLocaleBusiness.getListOfLocalesJAVA();
		for(Locale locale : localeList){
			List<ContentCategory> categoryList = this.categoryEngine.getCategoriesByLocale(locale.toString());
			if(ListUtil.isEmpty(categoryList)){
				continue;
			}
			for(ContentCategory category : categoryList){
				categoryDao.addCategory(category.getId());
			}
		}
	}

}