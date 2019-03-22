package com.example.demo.controller;

import com.example.demo.service.KubeService;
import com.google.common.base.Strings;
import io.kubernetes.client.ApiException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zhaozi on 2019/3/21
 */
@RestController @RequestMapping("/log") public class LoggerController {

    @Autowired private KubeService kubeService;

    @CrossOrigin("*")
    @GetMapping("/{id}") public String log(@PathVariable(required = false) String id) throws IOException, ApiException {

        if(Strings.isNullOrEmpty(id)) {
            return "";
        }

        return kubeService.getLogger(id);
    }

    @CrossOrigin("*")
    @GetMapping("/download/{id}") public ResponseEntity<Resource> logDownload(@PathVariable String id) {
        Resource resource = new InputStreamResource(new ByteArrayInputStream(
                kubeService.downloadLogger(id).getBytes(Charset.forName("UTF-8"))));

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + id + ".log\"")
                .body(resource);
    }
}
