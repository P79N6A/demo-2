package com.example.demo.service;

import java.util.List;

/**
 * @author zhaozi on 2019/3/21
 */
public interface ArtifactService {

    void store(String id, String name, byte[] values);

    List<String> list(String id);

    byte[] download(String id, String fileName);
}
