package com.github.zjor.web;


import io.javalin.security.RouteRole;

public enum Role implements RouteRole {
    ANYONE, AUTHENTICATED
}
