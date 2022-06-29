package com.github.zjor.web.validator;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;

public abstract class RequestValidator implements Handler {

    protected abstract boolean validate(@NotNull Context ctx);

    protected abstract void success(@NotNull Context ctx) throws Exception;

    protected abstract void failure(@NotNull Context ctx) throws Exception;

    @Override
    public void handle(@NotNull Context ctx) throws Exception {
        if (validate(ctx)) {
            success(ctx);
        } else {
            failure(ctx);
        }
    }

}
