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

package com.google.codeu.servlets;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.codeu.data.Datastore;
import com.google.codeu.data.Message;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import java.util.regex.*;
import org.apache.commons.validator.routines.UrlValidator;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.Translate.TranslateOption;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.Document.Type;
import com.google.cloud.language.v1.LanguageServiceClient;
import com.google.cloud.language.v1.Sentiment;


/** Handles fetching and saving {@link Message} instances. */
@WebServlet("/messages")
public class MessageServlet extends HttpServlet {

  private Datastore datastore;

  @Override
  public void init() {
    datastore = new Datastore();
  }

  /**
   * Responds with a JSON representation of {@link Message} data for a specific
   * user. Responds with an empty array if the user is not provided.
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

    response.setContentType("application/json");

    String user = request.getParameter("user");
    String targetLanguageCode = request.getParameter("language");

    if (user == null || user.equals("")) {
      // Request is invalid, return empty array

      response.getWriter().println("[]");
      return;
    }


    List<Message> messages = datastore.getMessages(user);

    if (targetLanguageCode != null) {
    	translateMessages(messages, targetLanguageCode);
    }

    Gson gson = new Gson();
    String json = gson.toJson(messages);

    response.getWriter().println(json);
  }

  /** Stores a new {@link Message}. */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

    UserService userService = UserServiceFactory.getUserService();
    if (!userService.isUserLoggedIn()) {
      response.sendRedirect("/index.html");
      return;
    }

    String user = userService.getCurrentUser().getEmail();
    String userText = Jsoup.clean(request.getParameter("text"), Whitelist.none());
    String recipient = request.getParameter("recipient");

    Pattern regex = Pattern.compile("(https?://\\S+\\.(png|jpg|gif|bmp|jpeg|mp4|webm|mp3|ogg|wav))");
    Matcher m = regex.matcher(userText);
    StringBuffer sb = new StringBuffer();
    UrlValidator urlValidator = new UrlValidator();
    while (m.find()) {
      String text = m.group(0);
      if (urlValidator.isValid(text) && text.matches("(https?://\\S+\\.(png|jpg|gif|bmp|jpeg))")){
        System.out.println("valid image");
        String replacement = "<img src=\"" + text + "\" />";
        m.appendReplacement(sb, replacement);
      } else if (urlValidator.isValid(text) && text.matches("(https?://\\S+\\.(mp4|webm))")){
        System.out.println("valid video");
        String replacement = "<video src=\"" + text + "\" width=\"400\" alt=\"Your browser does not support the video element.\" controls/>";
        m.appendReplacement(sb, replacement);
      } else if (urlValidator.isValid(text) && text.matches("(https?://\\S+\\.(mp3|ogg|wav))")){
        System.out.println("valid audio");
        String replacement = "<audio src=\"" + text + "\" alt=\"Your browser does not support the video element.\" controls/>";
        m.appendReplacement(sb, replacement);
      }
    }
    m.appendTail(sb);
    userText = sb.toString();

    //String regex = "(https?://\\S+\\.(png|jpg|gif|bmp|jpeg))";
    //String replacement = "<img src=\"$1\" />";
    //userText = userText.replaceAll(regex, replacement);
    
    float sentimentScore = calculateSentimentScore(userText);

    Message message = new Message(user, userText, recipient, sentimentScore);
    datastore.storeMessage(message);

    response.sendRedirect("/user-page.html?user=" + recipient);
  }


  private void translateMessages(List<Message> messages, String targetLanguageCode) {
	  Translate translate = TranslateOptions.getDefaultInstance().getService();

	  for(Message message : messages) {
	    String originalText = message.getText();

	    Translation translation =
	        translate.translate(originalText, TranslateOption.targetLanguage(targetLanguageCode));
	    String translatedText = translation.getTranslatedText();
	      
	    message.setText(translatedText);
	  }    
	}

  private float calculateSentimentScore(String text) throws IOException {
    Document doc = Document.newBuilder()
    .setContent(text).setType(Type.PLAIN_TEXT).build();

    LanguageServiceClient languageService = LanguageServiceClient.create();
    Sentiment sentiment = languageService.analyzeSentiment(doc).getDocumentSentiment();
    languageService.close();

    return sentiment.getScore();
  }

}
