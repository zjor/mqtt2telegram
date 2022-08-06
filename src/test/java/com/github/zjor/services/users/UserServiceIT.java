package com.github.zjor.services.users;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Optional;

@Ignore("relies on MONGO_URI env variable not available in CI/CD")
public class UserServiceIT {

    @Test
    public void shouldCreateUser() {
        System.setProperty("jdk.tls.client.protocols", "TLSv1.2");

        var uri = System.getenv("MONGO_URI");
        var mongoClient = new MongoClient(new MongoClientURI(uri));
        UserService service = new UserService(mongoClient, "test-user");

        service.ensureExists(1L, "Alice", "Brown", "alice.brown");
        var user = service.updateSecret(1L, Optional.of("s3cr3t"));
        Assert.assertEquals("s3cr3t", user.getSecret());
    }

}