/*
 * Copyright 2019 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.codeu.data;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.SortDirection;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Provides access to the data stored in Datastore. */
public class Datastore {

  private DatastoreService datastore;
  private String messageColumn = "Message";
  private String textColumn = "text";
  private String userColumn = "user";
  private String timestampColumn = "timestamp";
  private String recipientColumn = "recipient";
  private String sentimentScoreColumn = "sentimentScore";
  private String entityInformationColumn = "entityInformation";

  public Datastore() {
    datastore = DatastoreServiceFactory.getDatastoreService();
  }

  /** Stores the Message in Datastore. */
  public void storeMessage(Message message) {
    Entity messageEntity = new Entity(messageColumn, message.getId().toString());
    messageEntity.setProperty(userColumn, message.getUser());
    messageEntity.setProperty(textColumn, message.getText());
    messageEntity.setProperty(timestampColumn, message.getTimestamp());
    messageEntity.setProperty(recipientColumn, message.getRecipient());
    messageEntity.setProperty(sentimentScoreColumn, message.getSentimentScore());

    datastore.put(messageEntity);
  }

  /** Retrieves messages for a specified query. */
  public List<Message> answerQuery(Query query) {
    List<Message> messages = new ArrayList<>();

    PreparedQuery results = datastore.prepare(query);

    for (Entity entity : results.asIterable()) {
      try {
        String idString = entity.getKey().getName();
        UUID id = UUID.fromString(idString);
        String user = (String) entity.getProperty(userColumn);
        String text = (String) entity.getProperty(textColumn);
        long timestamp = (long) entity.getProperty(timestampColumn);
        String recipient = (String) entity.getProperty(recipientColumn);
        float sentimentScore =
            entity.getProperty(sentimentScoreColumn) == null
                ? (float) 0.0
                : ((Double) entity.getProperty(sentimentScoreColumn)).floatValue();

        Message message = new Message(id, user, text, timestamp, recipient, sentimentScore);
        messages.add(message);
      } catch (Exception e) {
        System.err.println("Error reading message.");
        System.err.println(entity.toString());
        e.printStackTrace();
      }
    }

    return messages;
  }

  /**
   * Gets messages posted by all users.
   *
   * @return a list of messages posted by all users, or empty list if users have never posted a
   *     message. List is sorted by time descending.
   */
  public List<Message> getAllMessages() {

    Query query = new Query(messageColumn).addSort(timestampColumn, SortDirection.DESCENDING);

    return answerQuery(query);
  }

  /**
   * Gets messages posted by all users.
   *
   * @return a list of messages posted by all users, or empty list if users have never posted a
   *     message. List is sorted by time ascending.
   */
  public List<Message> getAllMessagesAscending() {

    Query query = new Query(messageColumn).addSort(timestampColumn, SortDirection.ASCENDING);

    return answerQuery(query);
  }

  /**
   * Gets messages posted by a specific user.
   *
   * @return a list of messages posted by the user, or empty list if user has never posted a
   *     message. List is sorted by time descending.
   */
  public List<Message> getMessages(String recipient) {

    Query query =
        new Query(messageColumn)
            .setFilter(new Query.FilterPredicate(recipientColumn, FilterOperator.EQUAL, recipient))
            .addSort(timestampColumn, SortDirection.DESCENDING);

    return answerQuery(query);
  }

  /** Returns the total number of messages for all users. */
  public int getTotalMessageCount() {
    Query query = new Query(messageColumn);
    PreparedQuery results = datastore.prepare(query);
    return results.countEntities(FetchOptions.Builder.withLimit(1000));
  }

  /** Returns the average length of all messages. */
  public int getAverageMessageLength() {
    Query query = new Query(messageColumn);
    PreparedQuery results = datastore.prepare(query);

    int sum = 0;

    for (Entity entity : results.asIterable()) {
      sum = sum + ((String) entity.getProperty(textColumn)).length();
    }

    return sum / getTotalMessageCount();
  }

  /** Returns the maximum length of all the messages. Returns -1 if there are no messages. */
  public int getMaxMessageLength() {
    Query query = new Query(messageColumn);
    PreparedQuery results = datastore.prepare(query);

    int max = -1;

    for (Entity entity : results.asIterable()) {
      max = Math.max(max, ((String) entity.getProperty(textColumn)).length());
    }

    return max;
  }

  /** Stores the User in Datastore. */
  public void storeUser(User user) {
    Entity userEntity = new Entity(userColumn, user.getEmail());
    userEntity.setProperty("email", user.getEmail());
    userEntity.setProperty("aboutMe", user.getAboutMe());
    userEntity.setProperty("name", user.getName());
    userEntity.setProperty("country", user.getCountry());
    userEntity.setProperty("language", user.getLanguage());
    datastore.put(userEntity);
  }

  /** Returns the User owned by the email address, or null if no matching User was found. */
  public User getUser(String email) {

    Query query =
        new Query(userColumn)
            .setFilter(new Query.FilterPredicate("email", FilterOperator.EQUAL, email));
    PreparedQuery results = datastore.prepare(query);
    Entity userEntity = results.asSingleEntity();
    if (userEntity == null) {
      return null;
    }

    String aboutMe = (String) userEntity.getProperty("aboutMe");
    String name = (String) userEntity.getProperty("name");
    String country = (String) userEntity.getProperty("country");
    String language = (String) userEntity.getProperty("language");
    User user = new User(email, aboutMe, name, country, language);

    return user;
  }

  public User getUserByName(String name) {

    Query query =
        new Query(userColumn)
            .setFilter(new Query.FilterPredicate("name", FilterOperator.EQUAL, name));
    PreparedQuery results = datastore.prepare(query);
    Entity userEntity = results.asSingleEntity();
    if (userEntity == null) {
      return null;
    }

    String aboutMe = (String) userEntity.getProperty("aboutMe");
    String email = (String) userEntity.getProperty("email");
    String country = (String) userEntity.getProperty("country");
    String language = (String) userEntity.getProperty("language");
    User user = new User(email, aboutMe, name, country, language);

    return user;
  }

  /** Retrieves messages for a specified query. */
  public List<User> answerUserQuery(Query query) {
    List<User> users = new ArrayList<>();

    PreparedQuery results = datastore.prepare(query);

    for (Entity entity : results.asIterable()) {
      try {
        String email = (String) entity.getProperty("email");
        String aboutMe = (String) entity.getProperty("aboutMe");
        String name =
            entity.getProperty("name") == null
                ? "Unknown User"
                : ((String) entity.getProperty("name"));
        String country =
            entity.getProperty("country") == null
                ? "Unknown Country"
                : ((String) entity.getProperty("country"));
        String language =
            entity.getProperty("language") == null
                ? "en"
                : ((String) entity.getProperty("language"));

        User user = new User(email, aboutMe, name, country, language);
        users.add(user);
      } catch (Exception e) {
        System.err.println("Error reading user.");
        System.err.println(entity.toString());
        e.printStackTrace();
      }
    }

    return users;
  }

  public List<User> getAllUsersCountry() {

    Query query = new Query(userColumn).addSort("country", SortDirection.ASCENDING);

    return answerUserQuery(query);
  }

  public List<UserMarker> getMarkers() {
    List<UserMarker> markers = new ArrayList<>();

    Query query = new Query("UserMarker");
    PreparedQuery results = datastore.prepare(query);

    for (Entity entity : results.asIterable()) {
      try {
        double lat = (double) entity.getProperty("lat");
        double lng = (double) entity.getProperty("lng");
        String content = (String) entity.getProperty("content");

        UserMarker marker = new UserMarker(lat, lng, content);
        markers.add(marker);
      } catch (Exception e) {
        System.err.println("Error reading marker.");
        System.err.println(entity.toString());
        e.printStackTrace();
      }
    }
    return markers;
  }

  public void storeMarker(UserMarker marker) {
    Entity markerEntity = new Entity("UserMarker");
    markerEntity.setProperty("lat", marker.getLat());
    markerEntity.setProperty("lng", marker.getLng());
    markerEntity.setProperty("content", marker.getContent());
    datastore.put(markerEntity);
  }

  /** Stores the Event in Datastore. */
  public void storeEvent(Event event) {
    Entity eventEntity = new Entity("Event", event.getId().toString());
    eventEntity.setProperty(userColumn, event.getUser());
    eventEntity.setProperty(textColumn, event.getText());
    eventEntity.setProperty(timestampColumn, event.getTimestamp());
    eventEntity.setProperty(sentimentScoreColumn, event.getSentimentScore());
    eventEntity.setProperty(entityInformationColumn, event.getEntityInformation());

    datastore.put(eventEntity);
  }

  /** Retrieves events for a specified query. */
  public List<Event> answerEventQuery(Query query) {
    List<Event> events = new ArrayList<>();

    PreparedQuery results = datastore.prepare(query);

    for (Entity entity : results.asIterable()) {
      try {
        String idString = entity.getKey().getName();
        UUID id = UUID.fromString(idString);
        String user = (String) entity.getProperty(userColumn);
        String text = (String) entity.getProperty(textColumn);
        long timestamp = (long) entity.getProperty(timestampColumn);
        float sentimentScore =
            entity.getProperty(sentimentScoreColumn) == null
                ? (float) 0.0
                : ((Double) entity.getProperty(sentimentScoreColumn)).floatValue();
        String entityInformation = (String) entity.getProperty(entityInformationColumn);

        Event event = new Event(id, user, text, timestamp, sentimentScore, entityInformation);
        events.add(event);
      } catch (Exception e) {
        System.err.println("Error reading event.");
        System.err.println(entity.toString());
        e.printStackTrace();
      }
    }

    return events;
  }

  /**
   * Gets events posted by all users.
   *
   * @return a list of events posted by all users, or empty list if users have never posted a event.
   *     List is sorted by time descending.
   */
  public List<Event> getAllEvents() {

    Query query = new Query("Event").addSort(timestampColumn, SortDirection.DESCENDING);

    return answerEventQuery(query);
  }
}
