package org.acme;

import java.util.List;

import org.fuin.kcawrapper.Client;
import org.fuin.kcawrapper.Group;
import org.fuin.kcawrapper.Realm;
import org.fuin.kcawrapper.Role;
import org.fuin.kcawrapper.User;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.ClientScopeRepresentation;

/**
 * Creates a realm with all necessary setup.
 */
public class KeycloakSetup {

    public static final String REALM = "quarkus-demo";

    public static final String KEYCLOAK_URL = "http://localhost:8180/auth";

    public static final String REDIRECT_URL = "http://localhost*";

    public static final String CLIENT_ID = "product-app";

    public static final String CLIENT_SECRET = "very-secret";

    public static final String ALICE_NAME = "alice";

    public static final String ALICE_PW = "alice";

    public static final String BOB_NAME = "bob";

    public static final String BOB_PW = "bob";

    private ClientScopeRepresentation findByName(List<ClientScopeRepresentation> scopes, String name) {
        for (ClientScopeRepresentation scope : scopes) {
            if (name.contentEquals(scope.getName())) {
                return scope;
            }
        }
        return null;
    }

    public void makeMicroprofileJwtScopeDefault(Client client) {

        String scopeName = "microprofile-jwt";

        ClientScopeRepresentation mpJwtScope = findByName(client.getResource().getDefaultClientScopes(), scopeName);
        if (mpJwtScope == null) {
            mpJwtScope = findByName(client.getResource().getOptionalClientScopes(), scopeName);
            if (mpJwtScope == null) {
                throw new IllegalStateException("Wasn't able to find '" + scopeName + "' scope");
            }
            client.getResource().removeOptionalClientScope(mpJwtScope.getId());
            client.getResource().addDefaultClientScope(mpJwtScope.getId());
        }

    }

    /**
     * Creates the realm and configures it with some test data in case it does not already exist.
     */
    public void execute() {

        try (final Keycloak keycloak = KeycloakBuilder.builder().serverUrl(KEYCLOAK_URL).realm("master").username("admin").password("admin")
                .clientId("admin-cli").build()) {

            // Create realm
            Realm realm = Realm.findOrCreate(keycloak, REALM, true);

            // Create the client
            boolean standardFlow = true;
            boolean directAccessGrants = true;
            Client client = Client.findOrCreateOpenIdConnectWithSecret(realm, CLIENT_ID, CLIENT_SECRET, REDIRECT_URL, standardFlow,
                    directAccessGrants);

            makeMicroprofileJwtScopeDefault(client);

            // Create roles
            Role roleAdmin = Role.findOrCreate(realm, "admin", "Administrator");
            Role roleUser = Role.findOrCreate(realm, "user", "Normal user");

            // Create groups and add roles
            Group groupAdmin = Group.findOrCreate(realm, "admin");
            groupAdmin.addRealmRoles(roleAdmin.getName(), roleUser.getName());
            Group groupUser = Group.findOrCreate(realm, "user");
            groupUser.addRealmRoles(roleUser.getName());

            // Create users and join groups

            // Bob is admin and user
            User bob = User.findOrCreate(realm, BOB_NAME, BOB_PW, true);
            bob.joinGroups(groupAdmin, groupUser);

            // Alice is only user (no admin)
            User alice = User.findOrCreate(realm, ALICE_NAME, ALICE_PW, true);
            alice.joinGroups(groupUser);

        }

    }

    public static void main(String[] args) {

        System.out.println("BEGIN Creating Keycloak setup...");

        new KeycloakSetup().execute();

        System.out.println("END Creating Keycloak setup...");

    }

}
