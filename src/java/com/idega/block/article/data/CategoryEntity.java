package com.idega.block.article.data;

import java.io.Serializable;
import java.util.Random;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.hibernate.annotations.Index;

@Entity
@Table(name = "ART_CATEGORY")

@NamedQueries(
    {
        @NamedQuery(
        		name = CategoryEntity.GET_BY_NAMES,
        		query = "from CategoryEntity s where s.category in (:"+CategoryEntity.categoryProp+")"),
        @NamedQuery(
        		name = CategoryEntity.GET_ALL,
        		query = "from CategoryEntity s"),
        @NamedQuery(
        		name = CategoryEntity.GET_BY_NAME,
        		query = "from CategoryEntity s where s.category = :" + CategoryEntity.categoryProp)
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

    private final int hashCode;

    public CategoryEntity() {
        super();

        hashCode = new Random().nextInt();
    }

    public Long getId(){
        return id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    public String toString() {
        return getId() + " " + getCategory();
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof CategoryEntity))
            return false;

        CategoryEntity category = (CategoryEntity) object;
        try {
            return getCategory().equals(category.getCategory()) && getId().longValue() == category.getId().longValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}