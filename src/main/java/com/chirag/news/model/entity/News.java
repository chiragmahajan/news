package com.chirag.news.model.entity;

import com.chirag.news.constants.Constants;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "news")
@Data
public class News {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "news_body",nullable = false)
    private String newsBody;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name="username",nullable = false)
    private Login login;

    private Boolean active;

    @Column(name = "created_at", updatable = false, columnDefinition = Constants.DEFAULT_TIMESTAMP)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Column(name="updated_at",columnDefinition = Constants.ON_UPDATE_DEFAULT_TIMESTAMP)
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

}
