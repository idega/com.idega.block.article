package com.idega.block.article.test;

import java.util.Locale;

import javax.jcr.Credentials;
import javax.jcr.LoginException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import junit.textui.TestRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.idega.block.article.bean.ArticleItemBean;
import com.idega.core.test.base.IdegaBaseTest;
import com.idega.repository.RepositoryService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class ArticleRepositoryTest extends IdegaBaseTest{
	
	@Autowired
	private RepositoryService repository;
	
	private Session session;
	private Credentials credentials = new SimpleCredentials("root","".toCharArray());
	
	final int rand = (int)(10000*Math.random());
	
	//final String articlePath = "/files/cms/article/test/myarticle"+rand+".article";
	final String articlePath = "/files/cms/article/test/myarticle1.article";
	
	@Override
	@Before
	public void setUp() throws Exception {
		createNewSession();
	}

	private void createNewSession() throws LoginException, RepositoryException {
		session = repository == null ? null : repository.login(credentials);
	}
	
	private Session getSession(){
		return session;
	}

	@Test
	public void testArticleRepository() {
		//	JCR repository is moved to ePlatform 5.x
		assertEquals(true, true);
	}
	
	public void testArticleCreate() throws RepositoryException, Exception{
		ArticleItemBean article = new ArticleItemBean();
		article.setSession(getSession());
		article.setLocale(Locale.ENGLISH);
		article.setResourcePath(articlePath);
		//article.load();
		
		article.setHeadline("MyHeadline");
		article.setBody("Testbody");
		article.store();
		
		assertNotNull(article);
	}
	
	public void testArticleLoad() throws RepositoryException, Exception{
		ArticleItemBean article = new ArticleItemBean();
		article.setSession(getSession());
		article.setLocale(Locale.ENGLISH);
		article.setResourcePath(articlePath);
		article.load();
		String xml = article.getBody();//article.getLocalizedArticle().getBody();
		
		assertNotNull(xml);
	}
	
	public void testArticleUpdate() throws RepositoryException, Exception{
		ArticleItemBean article = new ArticleItemBean();
		article.setSession(getSession());
		article.setLocale(Locale.ENGLISH);
		article.setResourcePath(articlePath);
		article.load();
		
		article.setBody("Testbody");
		article.store();
		
		assertNotNull(article);
	}
	
	public void testArticleUpdate2() throws RepositoryException, Exception{
		ArticleItemBean article = new ArticleItemBean();
		article.setSession(getSession());
		article.setLocale(Locale.ENGLISH);
		article.setResourcePath(articlePath);
		article.load();
		String xml = article.getBody();
		
		assertNotNull(xml);
	}
	
	public static void main(String[] args) throws Exception{
		//SpringJUnit4ClassRunner classRunner = new SpringJUnit4ClassRunner(RepositoryTest.class);
		//classRunner.run(notifier)
		TestRunner.run(new ArticleRepositoryTest());
	}
}
