package com.example.demo.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Service;

/**
 * @author zhaozi on 2019/3/22
 */
@Service
public class ArtifactServiceImpl implements ArtifactService {

    private Map<String, List<Storage>> stores = new ConcurrentHashMap<>();

    @Override public void store(String id, String name, byte[] values) {
        List<Storage> storages = stores.get(id);
        if(Objects.isNull(storages)) {
            stores.put(id, new ArrayList<>());
        }
        stores.get(id).add(new Storage(name, values));
    }

    @Override public List<String> list(String id) {

        List<Storage> storages = stores.get(id);
        if(Objects.isNull(storages)) {
            return new ArrayList<>();
        }

        List<String> fileNames = new ArrayList<>();
        for (Storage storage : storages) {
            fileNames.add(storage.getName());
        }

        return fileNames;
    }

    @Override public byte[] download(String id, String fileName) {

        List<Storage> storages = stores.get(id);
        if(Objects.isNull(storages)) {
            return new byte[0];
        }

        for (Storage storage : storages) {
            if(storage.getName().equals(fileName)) {
                return storage.getData();
            }
        }
        return new byte[0];
    }

    @AllArgsConstructor
    @Data
    class Storage {
        //filename
        private String name;

        private byte[] data;
    }
}
