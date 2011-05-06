package com.idega.block.article.data;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.hibernate.annotations.Index;

@Entity
@Table(name = "IC_CATEGORY")
@NamedQueries(
	{ 
		@NamedQuery(name = CategoryEntity.GET_BY_NAMES, query = "from CategoryEntity s where s.category in (:"+CategoryEntity.categoryProp+")"),
		@NamedQuery(name = CategoryEntity.GET_ALL, query = "from CategoryEntity s"),
		@NamedQuery(name = CategoryEntity.GET_BY_NAME, query = "from CategoryEntity s where s.category = :" + CategoryEntity.categoryProp)
	}
)
public class CategoryEntity implements Serializable {

	public static final String GET_BY_NAMES = "categoryEntity.getByNames";
	public static final String GET_BY_NAME = "categoryEntity.getByName";
	public static final String GET_ALL = "categoryEntity.getAll";
	
	private static final long serialVersionUID = -439847510923445260L;
	
	public static final String idProp = "id";
	@Id @GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	public static final String categoryProp = "category";
	@Index(name = "categoryIndex")
	@Column(name="CATEGORY", nullable=false)
	private String category;
	
	public static final String articlesProp = "articles";
	@ManyToMany(mappedBy = "categories")
	private List<ArticleEntity> articles;
	
	public CategoryEntity() { }

	public Long getId(){
		return id;
	}
	
	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public List<ArticleEntity> getArticles() {
		return articles;
	}

	public void setArticles(List<ArticleEntity> articles) {
		this.articles = articles;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}
}
