package com.example.demo.service;

import io.kubernetes.client.ApiException;
import java.io.IOException;

/**
 * @author zhaozi on 2019/3/21
 */
public interface KubeService {

    boolean runTask(String id) throws ApiException, IOException, InterruptedException;

    String getLogger(String id) throws ApiException, IOException;

    String downloadLogger(String id);
}
