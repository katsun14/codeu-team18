package com.google.codeu.servlets;

import com.google.cloud.language.v1.AnalyzeEntitySentimentRequest;
import com.google.cloud.language.v1.AnalyzeEntitySentimentResponse;
import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.Document.Type;
import com.google.cloud.language.v1.EncodingType;
import com.google.cloud.language.v1.Entity;
import com.google.cloud.language.v1.LanguageServiceClient;
import com.google.codeu.data.Datastore;
import com.google.codeu.data.Message;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.regex.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Handles fetching all messages for the events feed. */
@WebServlet("/events")
public class EventsPageServlet extends HttpServlet {

  private Datastore datastore;

  @Override
  public void init() {
    datastore = new Datastore();
  }

  /** Responds with a JSON representation of Message data for all users. */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

    response.setContentType("application/json");

    List<Message> messages = datastore.getAllMessages();

    for (Message message : messages) {
      String userText = message.getText();

      String entityInformation = calculateEntityInformation(userText);

      message.setEntityInformation(entityInformation);
    }

    Gson gson = new Gson();
    String json = gson.toJson(messages);

    response.getOutputStream().println(json);
  }

  private String calculateEntityInformation(String text) throws IOException {
    Document doc = Document.newBuilder().setContent(text).setType(Type.PLAIN_TEXT).build();

    LanguageServiceClient languageService = LanguageServiceClient.create();

    AnalyzeEntitySentimentRequest request =
        AnalyzeEntitySentimentRequest.newBuilder()
            .setDocument(doc)
            .setEncodingType(EncodingType.UTF16)
            .build();

    AnalyzeEntitySentimentResponse response = languageService.analyzeEntitySentiment(request);

    StringBuilder sb = new StringBuilder();

    for (Entity entity : response.getEntitiesList()) {
      sb.append("ENTITY: ");
      sb.append(entity.getName());
      sb.append("\n");
      sb.append("Type: ");
      sb.append(entity.getType());
      sb.append("\n");
      sb.append("Salience: ");
      sb.append(entity.getSalience());
      sb.append("\n");
      sb.append("Metadata: \n");
      for (Map.Entry<String, String> entry : entity.getMetadataMap().entrySet()) {
        sb.append("\t");
        sb.append(entry.getKey());
        sb.append(" : ");
        sb.append(entry.getValue());
        sb.append("\n");
      }
      sb.append("Sentiment Score: ");
      sb.append(entity.getSentiment().getScore());
      sb.append("\n");
      // for (EntityMention mention : entity.getMentionsList()) {
      //   sb.append("Content: ");
      //   sb.append(mention.getText().getContent());
      //   sb.append("\n");
      //   sb.append("\tContent Magnitude: ");
      //   sb.append(mention.getSentiment().getMagnitude());
      //   sb.append("\n");
      //   sb.append("\tContent Sentiment: ");
      //   sb.append(mention.getSentiment().getScore());
      //   sb.append("\n");
      // }
      // System.out.printf("ENTITY: %s\n", entity.getName());
      // System.out.printf("Type: %s\n", entity.getType());
      // System.out.printf("Salience: %.3f\n", entity.getSalience());
      // System.out.println("Metadata: \n");
      // for (Map.Entry<String, String> entry : entity.getMetadataMap().entrySet()) {
      //   System.out.printf("\t%s : %s\n", entry.getKey(), entry.getValue());
      // }
      // System.out.printf("Sentiment : %s\n", entity.getSentiment());
      // for (EntityMention mention : entity.getMentionsList()) {
      //   System.out.printf("Content: %s\n", mention.getText().getContent());
      //   System.out.printf("Magnitude: %.3f\n", mention.getSentiment().getMagnitude());
      //   System.out.printf("Sentiment score : %.3f\n", mention.getSentiment().getScore());
      // }
    }

    System.out.println(sb.toString());
    return sb.toString();
  }
}
