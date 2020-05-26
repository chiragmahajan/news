package com.chirag.news.model.entity;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "likes")
@Data
public class Likes {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "like_id")
    private Long id;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name="id",nullable = false)
    private News news;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name="username",nullable = false)
    private Login login;

    @Column(name = "is_liked")
    private Boolean isLiked;
}
