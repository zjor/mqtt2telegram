package com.github.zjor.services.users;

import com.github.zjor.services.AbstractMongoService;
import com.mongodb.MongoClient;
import org.bson.Document;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class UserService extends AbstractMongoService {

    private static final Function<Long, Document> TELEGRAM_ID_FILTER =
            id -> new Document(Map.of(User.KEY_TELEGRAM_ID, id));

    public UserService(MongoClient client) {
        super(client);
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

    public User ensureExists(org.telegram.telegrambots.meta.api.objects.User tu) {
        return ensureExists(tu.getId(), tu.getFirstName(), tu.getLastName(), tu.getUserName());
    }

}
