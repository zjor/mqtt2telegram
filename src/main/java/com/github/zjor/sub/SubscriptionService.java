package com.github.zjor.sub;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class SubscriptionService {

    private static final String DB_NAME = "mqtt2telegram";
    private static final String COLLECTION_NAME = "subscriptions";

    private static final BiFunction<String, String, Document> USER_ID_AND_TOPIC_FILTER = (userId, topic)
            -> new Document(Map.of(Subscription.KEY_USER_ID, userId, Subscription.KEY_TOPIC, topic));

    private static final Function<String, Document> USER_ID_FILTER = (userId)
            -> new Document(Map.of(Subscription.KEY_USER_ID, userId));

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

    public Optional<Subscription> findSubscription(String userId, String topic) {
        var filter = USER_ID_AND_TOPIC_FILTER.apply(userId, topic);
        return Optional.ofNullable(c().find(filter).first()).map(Subscription::of);
    }

    public Subscription subscribe(String userId, String topic) {
        return findSubscription(userId, topic).orElseGet(() -> {
            var sub = new Subscription(userId, topic);
            c().insertOne(sub.asDocument());
            return sub;
        });
    }

    public Optional<Subscription> unsubscribe(String userId, String topic) {
        var filter = USER_ID_AND_TOPIC_FILTER.apply(userId, topic);
        return Optional.ofNullable(c().findOneAndDelete(filter)).map(Subscription::of);
    }

    public List<Subscription> getMySubscriptions(String userId) {
        return StreamSupport.stream(c().find(USER_ID_FILTER.apply(userId)).spliterator(), false)
                .map(Subscription::of)
                .collect(Collectors.toList());
    }

    public List<Subscription> getAllSubscriptions() {
        return StreamSupport.stream(c().find().spliterator(), false)
                .map(Subscription::of)
                .collect(Collectors.toList());
    }

}
