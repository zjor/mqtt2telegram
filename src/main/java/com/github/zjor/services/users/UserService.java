package com.github.zjor.services.users;

import com.github.zjor.services.AbstractMongoService;
import com.github.zjor.utils.SecretGenerator;
import com.mongodb.MongoClient;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@Slf4j
public class UserService extends AbstractMongoService {

    private static final Function<Long, Document> TELEGRAM_ID_FILTER =
            id -> new Document(Map.of(User.KEY_TELEGRAM_ID, id));

    public UserService(MongoClient client, String dbName) {
        super(client, dbName);
    }

    @Override
    protected String getCollectionName() {
        return "users";
    }

    public Optional<User> findByTelegramId(long telegramId) {
        return Optional.ofNullable(c().find(TELEGRAM_ID_FILTER.apply(telegramId)).first()).map(User::of);
    }

    public User ensureExists(long telegramId, String firstName, String lastName, String username) {
        return findByTelegramId(telegramId).orElseGet(() -> {
            var user = new User(telegramId, firstName, lastName, username);
            c().insertOne(user.asDocument());
            return user;
        });
    }

    public User updateSecret(long telegramId, Optional<String> secret) {
        var newSecret = secret.orElseGet(() -> SecretGenerator.next());
        var update = new Document("$set", Map.of(User.KEY_SECRET, newSecret));
        var result = c().updateOne(TELEGRAM_ID_FILTER.apply(telegramId), update);
        if (result.getMatchedCount() != 1) {
            log.warn("Updated less or more then one record: {}", result.getMatchedCount());
        }
        return findByTelegramId(telegramId).orElseThrow(() -> new UserNotFoundException(telegramId));
    }

    public User ensureExists(org.telegram.telegrambots.meta.api.objects.User tu) {
        return ensureExists(tu.getId(), tu.getFirstName(), tu.getLastName(), tu.getUserName());
    }

    public long count() {
        return c().countDocuments();
    }

    public static class UserNotFoundException extends RuntimeException {
        public UserNotFoundException(long telegramId) {
            super("telegramId: " + telegramId);
        }
    }

}
