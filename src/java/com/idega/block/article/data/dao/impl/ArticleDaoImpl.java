package com.idega.block.article.data.dao.impl;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.idega.block.article.data.ArticleEntity;
import com.idega.block.article.data.dao.ArticleDao;


/**
 * Class for speeding up Articles searching
 * @author martynas
 * Last changed: 2011.05.12
 * You can report about problems to: martynas@idega.com
 * AIM: lapiukshtiss
 * Skype: lapiukshtiss
 * You can expect to find some test cases notice in the end of the file.
 */
@Repository(ArticleDao.BEAN_NAME)
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class ArticleDaoImpl extends ArticleDaoTemplateImpl<ArticleEntity> implements ArticleDao {

	@Override
	protected Class<ArticleEntity> getEntityClass() {
		return ArticleEntity.class;
	}

	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly = false)
	protected <A extends ArticleEntity> A mergeEntity(A article) {
		getLogger().info("Updating article: " + article);
		ArticleEntity a = super.merge((ArticleEntity) article);
		return (A) a;
	}

	@Override
	@Transactional(readOnly = false)
	protected <A extends ArticleEntity> A persistEntity(A article) {
		getLogger().info("Creating article: " + article);
		super.persist((ArticleEntity) article);
		return article != null && article.getId() != null ? article : null;
	}

}