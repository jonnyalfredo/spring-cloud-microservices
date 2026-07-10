package com.jonathanleite.vitrine.apigateway.security;

import com.jonathanleite.vitrine.apigateway.config.PublicRouteProperties;
import org.springframework.stereotype.Component;

@Component
public class PublicRouteMatcher {

    private final PublicRouteProperties publicRouteProperties;

    public PublicRouteMatcher(PublicRouteProperties publicRouteProperties) {
        this.publicRouteProperties = publicRouteProperties;
    }

    public boolean isPublic(String path) {
        return publicRouteProperties.getExact().contains(path)
                || publicRouteProperties.getPrefixes().stream().anyMatch(path::startsWith);
    }
}
