package com.github.zjor.sub;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SubscriptionService {

    private static final String DB_NAME = "mqtt2telegram";
    private static final String COLLECTION_NAME = "subscriptions";

    private final MongoClient client;

    public SubscriptionService(MongoClient client) {
        this.client = client;
    }

    private MongoDatabase d() {
        return client.getDatabase(DB_NAME);
    }

    private MongoCollection<Document> c() {
        return d().getCollection(COLLECTION_NAME);
    }

    public Subscription subscribe(String userId, String topic) {
        var sub = new Subscription(userId, topic);
        c().insertOne(sub.asDocument());
        return sub;
    }

    public Optional<Subscription> unsubscribe(String userId, String topic) {
        var filter = new Document(Map.of("userId", userId, "topic", topic));
        return Optional.ofNullable(c().findOneAndDelete(filter)).map(Subscription::of);
    }

    public List<Subscription> getMySubscriptions(String userId) {
        return null;
    }

    public List<Subscription> getAllSubscriptions() {
        return null;
    }


}
