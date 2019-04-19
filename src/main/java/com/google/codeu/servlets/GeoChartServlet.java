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
import com.google.codeu.data.User;
import com.google.gson.Gson;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.regex.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Handles fetching and saving {@link User} instances. */
@WebServlet("/geochart")
public class GeoChartServlet extends HttpServlet {

  private Datastore datastore;

  @Override
  public void init() {
    datastore = new Datastore();
  }

  /** Responds with a JSON representation of {@link User} data. */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

    response.setContentType("application/json");
    // The following line should match however you manipulated getMessages() in Step 1
    List<User> usrList = datastore.getAllUsersCountry();
    Gson gson = new Gson();
    String json = gson.toJson(usrList);
    response.getWriter().println(json);
    // String user = request.getParameter("user");
    // String targetLanguageCode = request.getParameter("language");

    // if (user == null || user.equals("")) {
    //   // Request is invalid, return empty array

    //   response.getWriter().println("[]");
    //   return;
    // }

    // List<Message> messages = datastore.getMessages(user);

    // if (targetLanguageCode != null) {
    //   translateMessages(messages, targetLanguageCode);
    // }

    // Gson gson = new Gson();
    // String json = gson.toJson(messages);

    // response.getWriter().println(json);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

    UserService userService = UserServiceFactory.getUserService();
    if (!userService.isUserLoggedIn()) {
      response.sendRedirect("/index.html");
      return;
    }
    String currentUserEmail = userService.getCurrentUser().getEmail();

    String searchUser = request.getParameter("search-user");
    User user = datastore.getUserByName(searchUser);
    String redirect = "/user-page.html?user=" + currentUserEmail;

    if (user == null) {
      // getServletContext().getRequestDispatcher("/user-page.html?user=" +
      // currentUserEmail).forward(request, response);
      response.setContentType("text/html;charset=UTF-8");
      PrintWriter out = response.getWriter();
      out.println("<html><head>");
      out.println("<script type=\'text/javascript\'>");
      out.println(
          "alert(\'There is no user with this name. Please try again.\'); window.location.href = \""
              + redirect
              + "\"; </script>");
      out.println("</head><body></body></html>");
      // response.sendRedirect("/user-page.html?user=" + currentUserEmail);
      return;
    }

    response.sendRedirect("/user-page.html?user=" + user.getEmail());
  }
}
