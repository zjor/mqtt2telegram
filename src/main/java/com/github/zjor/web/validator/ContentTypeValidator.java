package com.github.zjor.web.validator;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.HttpCode;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class ContentTypeValidator extends RequestValidator {

    private final String contentType;
    private final Handler handler;

    public ContentTypeValidator(String contentType, Handler handler) {
        this.contentType = contentType;
        this.handler = handler;
    }

    @Override
    protected boolean validate(@NotNull Context ctx) {
        return ctx.contentType() != null && ctx.contentType().contains(contentType);
    }

    @Override
    protected void success(@NotNull Context ctx) throws Exception {
        handler.handle(ctx);
    }

    @Override
    protected void failure(@NotNull Context ctx) {
        ctx.status(HttpCode.BAD_REQUEST);
        ctx.json(Map.of(
                "success", false,
                "message", "expected Content-Type: " + contentType
        ));
    }

    public static ContentTypeValidator json(Handler handler) {
        return new ContentTypeValidator("application/json", handler);
    }
}
