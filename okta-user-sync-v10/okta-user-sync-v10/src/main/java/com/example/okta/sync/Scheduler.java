package com.example.okta.sync;

import com.okta.sdk.client.Client;
import com.okta.sdk.client.Clients;
import com.example.okta.sync.db.DatabaseService;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Scheduler {

    public static void main(String[] args) {
        try {
            // 🔧 Load Okta credentials from okta.properties
            Properties props = new Properties();
            InputStream input = Scheduler.class.getClassLoader().getResourceAsStream("okta.properties");
            if (input == null) {
                throw new IllegalArgumentException("❌ Could not find okta.properties in resources folder");
            }
            props.load(input);

            String token = props.getProperty("okta.client.token");
            String orgUrl = props.getProperty("okta.client.orgUrl");

            // 🏗 Build Okta client for user sync
            Client client = Clients.builder()
                    .setOrgUrl(orgUrl)
                    .setClientCredentials(() -> token)
                    .build();

            OktaUserService userService = new OktaUserService(client);
            OktaEventService eventService = new OktaEventService(token, orgUrl);

            // 🕒 Schedule sync task every 2 minutes
            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

            scheduler.scheduleAtFixedRate(() -> {
                try {
                    System.out.println("\n🔁 Sync started at: " + LocalDateTime.now());

                    // 1. Sync Users
                    userService.syncUsersToDatabase();

                    // 2. Sync Events using last sync timestamp
                    eventService.fetchAndStoreEvents();

                    // 3. Update last sync timestamp
                    DatabaseService.updateLastSyncTime(LocalDateTime.now());

                } catch (Exception e) {
                    System.err.println("❌ Sync failed!");
                    e.printStackTrace();
                    // You can add alert/email logic here if needed
                }
            }, 0, 2, TimeUnit.MINUTES);

        } catch (Exception e) {
            System.err.println("❌ Failed to initialize scheduler:");
            e.printStackTrace();
        }
    }
}
