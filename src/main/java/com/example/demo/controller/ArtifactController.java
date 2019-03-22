package com.example.demo.controller;

import com.example.demo.service.ArtifactService;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author zhaozi on 2019/3/21
 */
@RestController
@RequestMapping("/artifacts")
public class ArtifactController {

    @Autowired
    private ArtifactService artifactService;

    @PostMapping("/{id}")
    public boolean store(@RequestParam("file") MultipartFile file, @PathVariable String id)
            throws IOException {

        artifactService.store(id, file.getOriginalFilename(), file.getBytes());
        return true;
    }

    @GetMapping("/{id}/{filename}/download")
    public ResponseEntity<Resource> download(@PathVariable String id, @PathVariable String filename) {
        byte[] bytes = artifactService.download(id, filename);
        Resource resource = new InputStreamResource(new ByteArrayInputStream(bytes));
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(resource);
    }

    @CrossOrigin("*")
    @GetMapping("/{id}/files")
    public List<String> listArtifacts(@PathVariable String id) {
        return artifactService.list(id);
    }
}
