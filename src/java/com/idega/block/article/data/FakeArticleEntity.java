package com.idega.block.article.data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Index;
/**
 * Temporary solution. Fake class created for {@link ArticleEntity} inheritance
 * and class property to work if there is no other subclasses.
 * @author alex
 *
 */
@Entity
@Table(name = "ART_FAKE_ARTICLE")
public class FakeArticleEntity extends ArticleEntity {

	private static final long serialVersionUID = 7088150135350537241L;

	public static final String fake = "fake";

	@Index(name = "post_type_index")
	@Column(name = "SOCIAL_POST_TYPE")
	private String fakeProperty;

	public String getFakeProperty() {
		return fakeProperty;
	}

	public void setFakeProperty(String fakeProperty) {
		this.fakeProperty = fakeProperty;
	}
}