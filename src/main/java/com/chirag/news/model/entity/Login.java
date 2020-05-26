package com.chirag.news.model.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Data
@Table(name = "login")
public class Login {
    @Id
    private String username;

    private String password;
}
