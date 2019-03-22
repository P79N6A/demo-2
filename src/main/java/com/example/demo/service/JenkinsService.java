package com.example.demo.service;

/**
 * @author zhaozi on 2019/3/21
 */
public interface JenkinsService {

    String create(String jenkinsFile);

    String get(String id);
}
