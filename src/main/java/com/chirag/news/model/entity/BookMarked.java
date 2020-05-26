package com.chirag.news.model.entity;

import lombok.Data;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "bookmarks")
@Data
public class BookMarked {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bookmark_id")
    private Long id;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name="id",nullable = false)
    private News news;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name="username",nullable = false)
    private Login login;

    @Column(name = "is_bookmarked")
    private Boolean isBookmarked;
}
