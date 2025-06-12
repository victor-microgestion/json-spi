package com.microgestion.keycloak;

import org.keycloak.Config;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.storage.UserStorageProviderFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class JsonUserStorageProviderFactory implements
        UserStorageProviderFactory<JsonUserStorageProvider> {

    public static final String PROVIDER_ID = "json-file-userstore";
    private Map<String, String> userData = new HashMap<>();

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public void init(Config.Scope config) {
        ObjectMapper mapper = new ObjectMapper();
        try (InputStream is = getClass().getClassLoader()
                .getResourceAsStream("users.json")) {
            if (is != null) {
                userData = mapper.readValue(is,
                        new TypeReference<Map<String, String>>() {});
            }
        } catch (IOException e) {
            throw new RuntimeException("Error cargando users.json", e);
        }
    }

    @Override
    public JsonUserStorageProvider create(KeycloakSession session, ComponentModel model) {
        return new JsonUserStorageProvider(session, model, userData);
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        // No-op
    }

    @Override
    public void close() {
        // No-op
    }
}

