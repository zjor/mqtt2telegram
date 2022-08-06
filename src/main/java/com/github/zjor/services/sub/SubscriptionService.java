package com.github.zjor.services.sub;

import com.github.zjor.services.AbstractMongoService;
import com.mongodb.MongoClient;
import org.bson.Document;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class SubscriptionService extends AbstractMongoService {

    private static final BiFunction<String, String, Document> USER_ID_AND_TOPIC_FILTER = (userId, topic)
            -> new Document(Map.of(Subscription.KEY_USER_ID, userId, Subscription.KEY_TOPIC, topic));

    private static final Function<String, Document> USER_ID_FILTER = (userId)
            -> new Document(Map.of(Subscription.KEY_USER_ID, userId));


    public SubscriptionService(MongoClient client, String dbName) {
        super(client, dbName);
    }

    @Override
    protected String getCollectionName() {
        return "subscriptions";
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

    public long count() {
        return c().countDocuments();
    }

}
