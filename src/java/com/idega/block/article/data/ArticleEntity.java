package com.idega.block.article.data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.hibernate.annotations.Index;

import com.idega.util.ListUtil;

/**
 * IC_ARTICLE table entity
 * @author martynas
 *
 */
@Entity
@Table(name = "IC_ARTICLE")
@NamedQueries(
	{
		@NamedQuery(name = ArticleEntity.GET_BY_URI, query = "from ArticleEntity s where s.uri = :"+ArticleEntity.uriProp),
		@NamedQuery(name = ArticleEntity.GET_ID_BY_URI, query = "select id from ArticleEntity s where s.uri = :"+ArticleEntity.uriProp)
	}
)
public class ArticleEntity implements Serializable {

	public static final String GET_BY_URI = "articleEntity.getByURI";
	public static final String GET_ID_BY_URI = "articleEntity.getIDByURI";

	private static final long serialVersionUID = -8125483527520853214L;

	public static final String idProp = "id";
	@Id @GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	public static final String modificationDateProp = "modificationDate";
	@Index(name = "modificationDateIndex")
	@Column(name="MODIFICATION_DATE", nullable=false)
	private Date modificationDate;

	public static final String uriProp = "uri";
	@Index(name = "uriIndex")
	@Column(name="URI", nullable=false)
	private String uri;

	public static final String categoriesProp = "categories";
	@ManyToMany
	@JoinTable(name = "JND_ARTICLE_CATEGORY",
			joinColumns = @JoinColumn(name = "ARTICLE_FK"),
			inverseJoinColumns = @JoinColumn(name = "CATEGORY_FK"))
	private List<CategoryEntity> categories;

	public ArticleEntity() { }

	public Long getId(){
		return id;
	}

	public Date getModificationDate() {
		return modificationDate;
	}

	public void setModificationDate(Date modificationDate) {
		this.modificationDate = modificationDate;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public List<CategoryEntity> getCategories() {
		return categories;
	}

	public void setCategories(List<CategoryEntity> categories) {
		this.categories = categories;
	}

	public boolean addCategories(List<CategoryEntity> categories){
		if (ListUtil.isEmpty(categories)) {
			return Boolean.TRUE;
		}
		return this.categories.addAll(categories);
	}

	public boolean removeCategories(List<CategoryEntity> categories){
		if (ListUtil.isEmpty(categories)) {
			return Boolean.TRUE;
		}
		return this.categories.removeAll(categories);
	}

	@Override
	public String toString(){
		return this.id + " " + this.uri;
	}
}
