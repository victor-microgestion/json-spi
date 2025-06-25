package com.microgestion.keycloak;

import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.credential.CredentialModel;
import org.keycloak.credential.UserCredentialManager;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.models.UserModel;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.user.UserLookupProvider;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.adapter.AbstractUserAdapter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class JsonUserStorageProvider implements UserStorageProvider,
        UserLookupProvider, CredentialInputValidator {

    private final KeycloakSession session;
    private final ComponentModel model;
    private final Map<String, String> userData;
    private final Map<String, UserModel> loadedUsers = new ConcurrentHashMap<>();

    public JsonUserStorageProvider(KeycloakSession session, ComponentModel model,
            Map<String, String> userData) {
        this.session = session;
        this.model = model;
        this.userData = userData;
    }

    @Override
    public void close() {
        // No-op
    }

    @Override
    public UserModel getUserByUsername(RealmModel realm, String username) {
        if (!userData.containsKey(username))
            return null;
        return loadedUsers.computeIfAbsent(username, u -> createAdapter(realm, u));
    }

    @Override
    public UserModel getUserById(RealmModel realm, String id) {
        StorageId storageId = new StorageId(id);
        return getUserByUsername(realm, storageId.getExternalId());
    }

    @Override
    public UserModel getUserByEmail(RealmModel realm, String email) {
        return null;
    }

    private UserModel createAdapter(RealmModel realm, String username) {
        return new AbstractUserAdapter(session, realm, model) {
            @Override
            public String getUsername() {
                return username;
            }

            @Override
            public boolean isEnabled() {
                return true;
            }

            @Override
            public UserCredentialManager credentialManager() {
                return new UserCredentialManager(session, realm, this);
            }
        };
    }

    @Override
    public boolean supportsCredentialType(String credentialType) {
        return CredentialModel.PASSWORD.equals(credentialType);
    }

    @Override
    public boolean isConfiguredFor(RealmModel realm, UserModel user, String credentialType) {
        return supportsCredentialType(credentialType)
                && userData.containsKey(user.getUsername());
    }

    @Override
    public boolean isValid(RealmModel realm, UserModel user, CredentialInput input) {
        if (!supportsCredentialType(input.getType()))
            return false;
        String provided = input.getChallengeResponse();
        String stored = userData.get(user.getUsername());
        //session.users().addUser(realm, stored);
        this.createUser(session, realm, user.getUsername(), stored);
        return stored != null && stored.equals(provided);
    }

    /**
     * Crea un nuevo usuario con nombre y contraseña en un realm específico.
     *
     * @param session La sesión actual de Keycloak.
     * @param realm El realm donde se creará el usuario.
     * @param username El nombre de usuario deseado.
     * @param password La contraseña en texto plano para el nuevo usuario.
     * @return El UserModel del usuario creado.
     */
    public void createUser(KeycloakSession session, RealmModel realm, String username, String password) {

        // 1. Crear la instancia del usuario.
        UserModel user = session.users().addUser(realm, username);

        // 2. Establecer propiedades básicas.
        user.setEnabled(true);

        // 3. Crear el CredentialInput usando el factory method de UserCredentialModel.
        CredentialInput passwordCredential = UserCredentialModel.password(password);

        // 4. Establecer la contraseña para el usuario usando el CredentialManager.
        user.credentialManager().updateCredential(passwordCredential);

        System.out.println("Usuario '" + username + "' creado exitosamente.");
    }
}