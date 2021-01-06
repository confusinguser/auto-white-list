package com.confusinguser.autowhitelist.utils;

import com.confusinguser.autowhitelist.AutoWhiteList;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseManager {

    MongoClient mongoClient;
    MongoDatabase smpData;
    MongoCollection<Document> userData;

    public DatabaseManager() {
        MongoClientURI uri = new MongoClientURI(AutoWhiteList.instance.getConfigStorage().getConfig().getString("Mongo_database_uri"));
        mongoClient = new MongoClient(uri);
        smpData = mongoClient.getDatabase("smp-data");
        userData = smpData.getCollection("user-data");
    }

    public void addToWhitelist(List<String> uuids) {
        List<Document> documents = new ArrayList<>();
        for (String uuid : uuids) {
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("uuid", uuid);
            documents.add(new Document(dataMap));
        }
        userData.insertMany(documents);
    }
}
