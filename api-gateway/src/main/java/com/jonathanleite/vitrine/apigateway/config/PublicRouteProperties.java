package com.jonathanleite.vitrine.apigateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "security.public-routes")
public class PublicRouteProperties {

    private List<String> exact = new ArrayList<>();
    private List<String> prefixes = new ArrayList<>();

    public List<String> getExact() {
        return exact;
    }

    public void setExact(List<String> exact) {
        this.exact = exact;
    }

    public List<String> getPrefixes() {
        return prefixes;
    }

    public void setPrefixes(List<String> prefixes) {
        this.prefixes = prefixes;
    }
}
