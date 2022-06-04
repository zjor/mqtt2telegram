package com.github.zjor.services;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import lombok.Getter;
import org.bson.Document;

public abstract class AbstractMongoService {
    public static final String DB_NAME = "mqtt2telegram";

    @Getter
    private final MongoClient client;

    public AbstractMongoService(MongoClient client) {
        this.client = client;
    }

    protected abstract String getCollectionName();

    protected MongoDatabase d() {
        return client.getDatabase(DB_NAME);
    }

    protected MongoCollection<Document> c() {
        return d().getCollection(getCollectionName());
    }

}
