package com.microgestion.keycloak;

import org.keycloak.Config;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.storage.UserStorageProviderFactory;

public class JsonUserStorageProviderFactory implements
        UserStorageProviderFactory<JsonUserStorageProvider> {

    public static final String PROVIDER_ID = "create-userstore";

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public void init(Config.Scope config) {

    }

    @Override
    public JsonUserStorageProvider create(KeycloakSession session, ComponentModel model) {
        return new JsonUserStorageProvider(session, model);
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

