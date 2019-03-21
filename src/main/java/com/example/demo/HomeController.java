package com.example.demo;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zhaozi on 2019/3/20
 */
@RestController
@RequestMapping("/")
public class HomeController {

    @GetMapping
    public Resource get() throws FileNotFoundException {
        Path path = Paths.get("/root/jenkins/jenkinsFile");

        return new InputStreamResource(new FileInputStream(path.toFile()));
    }
}
