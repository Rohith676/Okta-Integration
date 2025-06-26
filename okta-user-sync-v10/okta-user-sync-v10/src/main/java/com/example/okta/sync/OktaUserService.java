package com.example.okta.sync;

import com.example.okta.sync.db.DatabaseService;
import com.okta.sdk.client.Client;
import com.okta.sdk.resource.user.User;

import java.util.UUID;

public class OktaUserService {

    private final Client client;

    // ✅ This constructor must exist!
    public OktaUserService(Client client) {
        this.client = client;
    }

    public void syncUsersToDatabase() {
        Iterable<User> users = client.listUsers();

        for (User user : users) {
            String id = UUID.randomUUID().toString();
            String login = user.getProfile().getLogin();
            String firstName = user.getProfile().getFirstName();
            String lastName = user.getProfile().getLastName();
            String email = user.getProfile().getEmail();

            DatabaseService.insertUser(id, login, firstName, lastName, email);
        }
    }
}
