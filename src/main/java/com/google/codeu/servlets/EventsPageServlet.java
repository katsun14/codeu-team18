package com.google.codeu.servlets;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.cloud.language.v1.AnalyzeEntitySentimentRequest;
import com.google.cloud.language.v1.AnalyzeEntitySentimentResponse;
import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.Document.Type;
import com.google.cloud.language.v1.EncodingType;
import com.google.cloud.language.v1.Entity;
import com.google.cloud.language.v1.LanguageServiceClient;
import com.google.cloud.language.v1.Sentiment;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.Translate.TranslateOption;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import com.google.codeu.data.Datastore;
import com.google.codeu.data.Event;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.List;
import java.util.regex.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.validator.routines.UrlValidator;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

/** Handles fetching all events for the events feed. */
@WebServlet("/events")
public class EventsPageServlet extends HttpServlet {

  private Datastore datastore;

  @Override
  public void init() {
    datastore = new Datastore();
  }

  /** Responds with a JSON representation of Event data for all users. */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

    response.setContentType("application/json");

    String targetLanguageCode = request.getParameter("language");

    List<Event> events = datastore.getAllEvents();

    // for (Event event : events) {
    //   String userText = event.getText();

    //   String entityInformation = calculateEntityInformation(userText);

    //   event.setEntityInformation(entityInformation);
    // }

    if (targetLanguageCode != null) {
      translateEvents(events, targetLanguageCode);
    }

    Gson gson = new Gson();
    String json = gson.toJson(events);

    response.getWriter().println(json);
  }

  /** Stores a new {@link Event}. */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

    UserService userService = UserServiceFactory.getUserService();
    if (!userService.isUserLoggedIn()) {
      response.sendRedirect("/index.html");
      return;
    }

    String user = userService.getCurrentUser().getEmail();
    String userText = Jsoup.clean(request.getParameter("text"), Whitelist.none());

    Pattern regex =
        Pattern.compile("(https?://\\S+\\.(png|jpg|gif|bmp|jpeg|mp4|webm|mp3|ogg|wav))");
    Matcher m = regex.matcher(userText);
    StringBuffer sb = new StringBuffer();
    UrlValidator urlValidator = new UrlValidator();
    while (m.find()) {
      String text = m.group(0);
      if (urlValidator.isValid(text) && text.matches("(https?://\\S+\\.(png|jpg|gif|bmp|jpeg))")) {
        System.out.println("valid image");
        String replacement = "<img src=\"" + text + "\" />";
        m.appendReplacement(sb, replacement);
      } else if (urlValidator.isValid(text) && text.matches("(https?://\\S+\\.(mp4|webm))")) {
        System.out.println("valid video");
        String replacement =
            "<video src=\""
                + text
                + "\" width=\"400\" alt=\"Your browser does not support the video element.\" controls/>";
        m.appendReplacement(sb, replacement);
      } else if (urlValidator.isValid(text) && text.matches("(https?://\\S+\\.(mp3|ogg|wav))")) {
        System.out.println("valid audio");
        String replacement =
            "<audio src=\""
                + text
                + "\" alt=\"Your browser does not support the video element.\" controls/>";
        m.appendReplacement(sb, replacement);
      }
    }
    m.appendTail(sb);
    userText = sb.toString();

    // String regex = "(https?://\\S+\\.(png|jpg|gif|bmp|jpeg))";
    // String replacement = "<img src=\"$1\" />";
    // userText = userText.replaceAll(regex, replacement);

    float sentimentScore = calculateSentimentScore(userText);
    String entityInformation = calculateEntityInformation(userText);

    Event event = new Event(user, userText, sentimentScore, entityInformation);
    datastore.storeEvent(event);

    response.sendRedirect("/events.html");
  }

  private void translateEvents(List<Event> events, String targetLanguageCode) {
    Translate translate = TranslateOptions.getDefaultInstance().getService();

    for (Event event : events) {
      String originalText = event.getText();

      Translation translation =
          translate.translate(originalText, TranslateOption.targetLanguage(targetLanguageCode));
      String translatedText = translation.getTranslatedText();

      event.setText(translatedText);
    }
  }

  private float calculateSentimentScore(String text) throws IOException {
    Document doc = Document.newBuilder().setContent(text).setType(Type.PLAIN_TEXT).build();

    LanguageServiceClient languageService = LanguageServiceClient.create();
    Sentiment sentiment = languageService.analyzeSentiment(doc).getDocumentSentiment();
    languageService.close();

    return sentiment.getScore();
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
    sb.append("Event Tags: ");

    for (Entity entity : response.getEntitiesList()) {
      if (entity.getType().getNumber() == 4) { // Event type is 4
        sb.append(entity.getName());
        sb.append(". ");
      }
    }

    return sb.toString();
  }
}
