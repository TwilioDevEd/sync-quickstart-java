package com.twilio;

import static spark.Spark.get;
import static spark.Spark.staticFileLocation;
import static spark.Spark.afterAfter;

import java.util.HashMap;

import com.github.javafaker.Faker;
import com.twilio.jwt.accesstoken.AccessToken;
import com.twilio.jwt.accesstoken.SyncGrant;

import com.google.gson.Gson;

public class Webapp {

    public static void main(String[] args) {
        // Serve static files from src/main/resources/public
        staticFileLocation("/public");

        // Create a Faker instance to generate a random username for the connecting user
        Faker faker = new Faker();

        // Log all requests and responses
        afterAfter(new LoggingFilter());

        // Create an access token using our Twilio credentials
        get("/token", "application/json", (request, response) -> {
            // Generate a random username for the connecting client
            String identity = faker.firstName() + faker.lastName() + faker.zipCode();

            // Create IP messaging grant
            SyncGrant grant = new SyncGrant();
            grant.setServiceSid(System.getenv("TWILIO_SYNC_SERVICE_SID"));

            // Create access token
            AccessToken token = new AccessToken.Builder(
                    System.getenv("TWILIO_ACCOUNT_SID"),
                    System.getenv("TWILIO_API_KEY"),
                    System.getenv("TWILIO_API_SECRET")
            ).identity(identity).grant(grant).build();

            // create JSON response payload
            HashMap<String, String> json = new HashMap<String, String>();
            json.put("identity", identity);
            json.put("token", token.toJwt());

            // Render JSON response
            Gson gson = new Gson();
            return gson.toJson(json);
        });
    }
}
