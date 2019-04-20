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

/**
 * Adds a login or logout link to the page, depending on whether the user is
 * already logged in.
 */
function addLoginOrLogoutLinkToNavigation() {
  const navigationElement = document.getElementById('navigation');
  console.log(navigationElement.childNodes[1]);
  if (!navigationElement) {
    console.warn('Navigation element not found!');
    return;
  }

  fetch('/login-status')
    .then((response) => {
      return response.json();
    })
    .then((loginStatus) => {
      if (loginStatus.isLoggedIn) {
        navigationElement.insertBefore(createListItem(createLink(
          '/user-page.html?user=' + loginStatus.username, 'Your Page')),
          navigationElement.childNodes[2]);

        navigationElement.insertBefore(createListItem(
          createLink('/logout', 'Logout')), navigationElement.childNodes[2]);
      } else {
        navigationElement.insertBefore(createListItem(
          createLink('/login', 'Login')), navigationElement.childNodes[2]);
      }

    });
}


/**
 * Adds a basic link to the page, with id 'text' to page 'url'
 */
function addBasicLinkNavigation(url, text) {
  const navigationElement = document.getElementById('dropdown-content');
  if (!navigationElement) {
    console.warn('Navigation element not found!');
    return;
  }

  navigationElement.appendChild(createListItem(createLink(url, text)));
}

/**
 * Loads the Navigation Buttons on Each Page
 */
function loadNavBar() {
  addLoginOrLogoutLinkToNavigation();
  addBasicLinkNavigation('feed.html', 'Public Feed');
  addBasicLinkNavigation('charts.html', 'Charts');
  // addBasicLinkNavigation('map.html', 'Maps');
  addBasicLinkNavigation('user-map.html', 'Maps');
  addBasicLinkNavigation('stats.html', 'Stats Page');
  addBasicLinkNavigation('events.html', 'Events Page');
  addBasicLinkNavigation('user-settings.html', 'User Settings');
}



/**
 * Creates an li element.
 * @param {Element} childElement
 * @return {Element} li element
 */
function createListItem(childElement) {
  const listItemElement = document.createElement('li');
  listItemElement.appendChild(childElement);
  return listItemElement;
}

/**
 * Creates an anchor element.
 * @param {string} url
 * @param {string} text
 * @return {Element} Anchor element
 */
function createLink(url, text) {
  const linkElement = document.createElement('a');
  linkElement.appendChild(document.createTextNode(text));
  linkElement.href = url;
  return linkElement;
}
