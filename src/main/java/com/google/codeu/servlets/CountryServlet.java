package com.google.codeu.servlets;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.Translate.TranslateOption;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import com.google.codeu.data.Datastore;
import com.google.codeu.data.User;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Handles fetching and saving user's country. */
@WebServlet("/country")
public class CountryServlet extends HttpServlet {

  private Datastore datastore;

  @Override
  public void init() {
    datastore = new Datastore();
  }

  /** Responds with the "country" section for a particular user. */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

    response.setContentType("text/html");

    String user = request.getParameter("user");
    String targetLanguageCode = request.getParameter("language");

    if (user == null || user.equals("")) {
      // Request is invalid, return empty response
      return;
    }

    User userData = datastore.getUser(user);

    if (userData == null || userData.getCountry() == null) {
      return;
    }

    String text = userData.getCountry();

    if (targetLanguageCode != null) {
      text = translateText(text, targetLanguageCode);
    }

    response.getWriter().println(text);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

    UserService userService = UserServiceFactory.getUserService();
    if (!userService.isUserLoggedIn()) {
      response.sendRedirect("/index.html");
      return;
    }

    String userEmail = userService.getCurrentUser().getEmail();

    User user = datastore.getUser(userEmail);
    if (user == null) user = new User(userEmail, "", "", "", "");

    user.setCountry(request.getParameter("country"));
    datastore.storeUser(user);

    response.sendRedirect("/user-page.html?user=" + userEmail);
  }

  public String translateText(String text, String targetLanguageCode) {
    Translate translate = TranslateOptions.getDefaultInstance().getService();

    Translation translation =
        translate.translate(text, TranslateOption.targetLanguage(targetLanguageCode));
    String translatedText = translation.getTranslatedText();

    if (translatedText != null) {
      text = translatedText;
    }

    return text;
  }
}
