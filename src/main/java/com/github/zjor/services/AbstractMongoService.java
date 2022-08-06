package com.github.zjor.services;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import lombok.Getter;
import org.bson.Document;

public abstract class AbstractMongoService {

    @Getter
    private final MongoClient client;

    private final String dbName;

    public AbstractMongoService(MongoClient client, String dbName) {
        this.client = client;
        this.dbName = dbName;
    }

    protected abstract String getCollectionName();

    protected MongoDatabase d() {
        return client.getDatabase(dbName);
    }

    protected MongoCollection<Document> c() {
        return d().getCollection(getCollectionName());
    }

}
