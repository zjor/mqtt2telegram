package com.github.zjor.services.sub;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.bson.Document;
import org.bson.types.ObjectId;

@Getter
@ToString
@AllArgsConstructor
public class Subscription {

    public static final String KEY_ID = "_id";
    public static final String KEY_USER_ID = "userId";
    public static final String KEY_TOPIC = "topic";
    public static final String KEY_CREATED_AT = "createdAt";

    private ObjectId id;
    private String userId;
    private String topic;
    private long createdAt;

    public Subscription(String userId, String topic) {
        this.id = new ObjectId();
        this.userId = userId;
        this.topic = topic;
        this.createdAt = System.currentTimeMillis();
    }

    public static Subscription of(Document doc) {
        return new Subscription(
                doc.getObjectId(KEY_ID),
                doc.getString(KEY_USER_ID),
                doc.getString(KEY_TOPIC),
                doc.getLong(KEY_CREATED_AT));
    }

    public Document asDocument() {
        return new Document()
                .append(KEY_ID, id)
                .append(KEY_USER_ID, userId)
                .append(KEY_TOPIC, topic)
                .append(KEY_CREATED_AT, createdAt);
    }
}
