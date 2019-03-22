package com.example.demo.service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

/**
 * @author zhaozi on 2019/3/21
 */
@Service
public class JenkinsServiceImpl implements JenkinsService {

    // id, jenkinsfile
    private Map<String, String> stores = new ConcurrentHashMap<>();

    @Override public String create(String jenkinsFile) {
        String id = UUID.randomUUID().toString();
        stores.put(id, jenkinsFile);
        return id;
    }

    @Override public String get(String id) {

        return stores.get(id);
    }

}
