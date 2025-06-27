package com.microgestion.keycloak;

import org.keycloak.credential.CredentialInput;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.models.UserModel;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.user.UserLookupProvider;
import org.keycloak.storage.StorageId;

public class JsonUserStorageProvider implements UserStorageProvider,
        UserLookupProvider {

    private final KeycloakSession session;
    private final ComponentModel model;

    public JsonUserStorageProvider(KeycloakSession session, ComponentModel model) {
        this.session = session;
        this.model = model;
    }

    @Override
    public void close() {
        // No-op
    }

    @Override
    public UserModel getUserByUsername(RealmModel realm, String username) {
        System.out.println("************************ getUserByUsername ************************");
        UserModel user = this.createUser(session, realm, username, "12345");
        return user;
    }

    @Override
    public UserModel getUserById(RealmModel realm, String id) {
        System.out.println("************************ getUserById ************************");
        StorageId storageId = new StorageId(id);
        return getUserByUsername(realm, storageId.getExternalId());
    }

    @Override
    public UserModel getUserByEmail(RealmModel realm, String email) {
        System.out.println("************************ getUserByEmail ************************");
        return null;
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
    public UserModel createUser(KeycloakSession session, RealmModel realm, String username, String password) {
        
        // 1. Crear la instancia del usuario.
        UserModel user = session.users().addUser(realm, username);

        // 2. Establecer propiedades básicas.
        user.setEnabled(true);
        
            //2.1 Cosas que se pueden agregar:
            //* Email: (user.setEmail(), user.getEmail())
            //* Nombre: (user.setFirstName(), user.getFirstName())
            //* Apellido: (user.setLastName(), user.getLastName())
            //* Estado (Habilitado/Deshabilitado): (user.setEnabled(), user.isEnabled())

        // 3. Crear el CredentialInput usando el factory method de UserCredentialModel.
        CredentialInput passwordCredential = UserCredentialModel.password(password);

        // 4. Establecer la contraseña para el usuario usando el CredentialManager.
        user.credentialManager().updateCredential(passwordCredential);

        System.out.println("Usuario '" + username + "' creado exitosamente.");

        return user;
    }
}