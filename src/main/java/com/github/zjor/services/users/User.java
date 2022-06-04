package com.github.zjor.services.users;

import com.github.zjor.utils.SecretGenerator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.bson.Document;
import org.bson.types.ObjectId;

@Getter
@ToString
@AllArgsConstructor
public class User {

    public static final String KEY_ID = "_id";
    public static final String KEY_TELEGRAM_ID = "telegramId";
    public static final String KEY_FIRST_NAME = "firstName";
    public static final String KEY_LAST_NAME = "lastName";
    public static final String KEY_USERNAME = "username";
    public static final String KEY_SECRET = "secret";
    public static final String KEY_CREATED_AT = "createdAt";

    private ObjectId id;
    private long telegramId;
    private String firstName;
    private String lastName;
    private String username;
    private String secret;
    private long createdAt;

    public User(long telegramId, String firstName, String lastName, String username) {
        this.id = new ObjectId();
        this.telegramId = telegramId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.secret = SecretGenerator.next();
        this.createdAt = System.currentTimeMillis();
    }

    public static User of(Document doc) {
        return new User(
                doc.getObjectId(KEY_ID),
                doc.getLong(KEY_TELEGRAM_ID),
                doc.getString(KEY_FIRST_NAME),
                doc.getString(KEY_LAST_NAME),
                doc.getString(KEY_USERNAME),
                doc.getString(KEY_SECRET),
                doc.getLong(KEY_CREATED_AT));
    }

    public Document asDocument() {
        return new Document()
                .append(KEY_ID, id)
                .append(KEY_TELEGRAM_ID, telegramId)
                .append(KEY_FIRST_NAME, firstName)
                .append(KEY_LAST_NAME, lastName)
                .append(KEY_USERNAME, username)
                .append(KEY_SECRET, secret)
                .append(KEY_CREATED_AT, createdAt);
    }

}
