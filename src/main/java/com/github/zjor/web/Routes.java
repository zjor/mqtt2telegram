package com.github.zjor.web;

import io.javalin.apibuilder.EndpointGroup;
import static io.javalin.apibuilder.ApiBuilder.*;

public class Routes implements EndpointGroup {

    @Override
    public void addEndpoints() {
        get("/", ctx -> ctx.html("OK"));
    }
}
