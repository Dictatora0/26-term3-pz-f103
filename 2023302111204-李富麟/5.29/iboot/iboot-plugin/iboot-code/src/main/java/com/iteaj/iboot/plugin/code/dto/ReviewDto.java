package com.iteaj.iboot.plugin.code.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ReviewDto {

    private String entity;

    private String controller;

    private String mapperXml;

    private String service;

    private String serviceImpl;

    private String html;

    private String mapper;

    private String dto;

    private String menuSql;
}
