package com.example.demo.controller;

import com.example.demo.service.JenkinsService;
import com.example.demo.service.KubeService;
import com.google.common.base.Strings;
import io.kubernetes.client.ApiException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zhaozi on 2019/3/21
 */
@RestController
@RequestMapping("/jenkins")
public class JenkinsController {

    @Autowired private JenkinsService jenkinsService;

    @Autowired
    private KubeService kubeService;

    @CrossOrigin("*")
    @PostMapping public String create(@RequestBody String content)
            throws InterruptedException, ApiException, IOException {

        String id = jenkinsService.create(content);

        kubeService.runTask(id);
        return id;
    }

    @CrossOrigin("*")
    @GetMapping("/{id}")
    public Resource get(@PathVariable String id) {
        String content = jenkinsService.get(id);
        if(Strings.isNullOrEmpty(content)) {
            throw new RuntimeException("not found jenkins file");
        }
        return new InputStreamResource(
                new ByteArrayInputStream(content.getBytes(Charset.forName("UTF-8"))));
    }

}
