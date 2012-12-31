package com.idega.block.article.data;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.Hibernate;
import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.annotations.Index;

import com.idega.block.article.data.dao.ArticleDao;
import com.idega.util.ListUtil;
import com.idega.util.expression.ELUtil;

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
@Inheritance(strategy=InheritanceType.JOINED)
public class ArticleEntity implements Serializable {

    public static final String GET_BY_URI = "articleEntity.getByURI";
    public static final String GET_ID_BY_URI = "articleEntity.getIDByURI";

    private static final long serialVersionUID = -8125483527520853214L;

    public static final String idProp = "id";
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

    public static final String modificationDateProp = "modificationDate";
    @Index(name = "modificationDateIndex")
    @Column(name = "MODIFICATION_DATE", nullable=false)
    private Date modificationDate;

    public static final String uriProp = "uri";
    @Index(name = "uriIndex")
    @Column(name = "URI", nullable=false)
    private String uri;

    public static final String categoriesProp = "categories";
    @ManyToMany
    @JoinTable(name = "JND_ARTICLE_CATEGORY",
            joinColumns = @JoinColumn(name = "ARTICLE_FK"),
            inverseJoinColumns = @JoinColumn(name = "CATEGORY_FK"))
    private Set<CategoryEntity> categories;

    public static final String editorsProp = "editorsProp";
    @CollectionOfElements(fetch = FetchType.EAGER)
    @JoinTable(name = "article_editors_groups", joinColumns=@JoinColumn(name="ARTICLE_ID"))
    @Column(name = "EDITORS_GROUPS_IDS", nullable=false)
    private Set<Integer> editors;

    private Integer hashCode = new Random().nextInt();

    public ArticleEntity() {
    	super();
    }

    public ArticleEntity(ArticleEntity entity) {
    	this();

    	setCategories(entity.getCategories());
    	setEditors(entity.getEditors());
    	setModificationDate(entity.getModificationDate());
    	setUri(entity.getUri());
    	//TODO: test this!!!!!!!!!!!!!
    	id = entity.getId();
    }

	/**
     * Returns group ids. Users of those groups are allowed to edit article.
     * @return Groups ids
     */
    public Set<Integer> getEditors() {
    	//TODO: check if can return null and return empty set if so
		return editors;
	}

    /**
     * Sets the ids of groups which users are allowed to edit articles.
     *
     * @param editors The set of ids of groups which users are allowed to edit articles.
     */
	public void setEditors(Set<Integer> editors) {
		this.editors = editors;
	}

    public Long getId() {
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

    @Transient
    public Set<CategoryEntity> getCategories() {
    	if (Hibernate.isInitialized(categories))
    		return categories;

    	if (id == null)
    		return Collections.emptySet();

    	List<CategoryEntity> categoriesList = getArticleDao().getCategories(id);
    	if (ListUtil.isEmpty(categoriesList)) {
    		// Not empty set because add should be supported
    		categories = new HashSet<CategoryEntity>();
    	} else {
    		categories = new HashSet<CategoryEntity>(categoriesList);
    	}

    	return categories;
    }

    public void setCategories(Collection<CategoryEntity> categories) {
        this.categories = new HashSet<CategoryEntity>(categories);
    }

    public boolean addCategories(List<CategoryEntity> categories){
        if (ListUtil.isEmpty(categories))
            return Boolean.TRUE;

        if (this.categories == null) {
            this.categories = new HashSet<CategoryEntity>(categories);
            return Boolean.TRUE;
        }

        return this.categories.addAll(categories);
    }

    public boolean removeCategories(List<CategoryEntity> categories){
        if (ListUtil.isEmpty(categories))
            return Boolean.TRUE;

        Set<CategoryEntity> categoriesList = getCategories();
        if (categoriesList == null) {
            setCategories(categoriesList);
            return Boolean.TRUE;
        }

        return categoriesList.addAll(categories);
    }

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ArticleEntity))
			return false;

		ArticleEntity article = (ArticleEntity) obj;
		try {
			return getUri().equals(article.getUri()) && getId().longValue() == article.getId().longValue();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public int hashCode() {
		if (hashCode == null)
			hashCode = new Random().nextInt();

		return hashCode;
	}

	@Override
	public String toString() {
		return "ID: " + getId() + ", URI: " + getUri() + ", modified: " + getModificationDate();
	}

	private ArticleDao getArticleDao() {
		ArticleDao articleDao = ELUtil.getInstance().getBean(ArticleDao.BEAN_NAME);
		return articleDao;
	}

}