package com.github.zjor.web;

import io.javalin.core.security.RouteRole;

public enum Role implements RouteRole {
    ANYONE, AUTHENTICATED
}
