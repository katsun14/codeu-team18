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

// Get ?user=XYZ parameter value
const urlParams = new URLSearchParams(window.location.search);
const parameterUsername = urlParams.get('user');
const maxMessages = 5;
//HashMap to map supported languages and their language codes
var supportedLanguages = new Map();

// URL must include ?user=XYZ parameter. If not, redirect to homepage.
if (!parameterUsername) {
  window.location.replace('/');
}


/*
* Set supported languages in hashmap
*/
function fillMap() {
  supportedLanguages.set('en', 'English');
  supportedLanguages.set('zh', 'Chinese');
  supportedLanguages.set('hi', 'Hindi');
  supportedLanguages.set('es', 'Spanish');
  supportedLanguages.set('ar', 'Arabic');
}


/** Sets the page title based on the URL parameter username. */
function setPageTitle() {
  document.getElementById('page-title').innerText = parameterUsername;
  document.title = parameterUsername + ' - User Page';
}

/**
 * Shows the message form if the user is logged in and viewing their own page.
 */
function showMessageFormIfViewingSelf() {
  fetch('/login-status')
      .then((response) => {
        return response.json();
      })
      .then((loginStatus) => {
        if (loginStatus.isLoggedIn) {
          const messageForm = document.getElementById('message-form');

          //Specifies where to send form data once it is submitted
          messageForm.action = '/messages?recipient=' + parameterUsername;
          messageForm.classList.remove('hidden');
        }
      });
      document.getElementById('about-me-form').classList.remove('hidden');
}

/** Fetches messages and add them to the page. */
function fetchMessages() {
  const parameterLanguage = urlParams.get('language');
  let url = '/messages?user=' + parameterUsername;

  if (parameterLanguage && 
    supportedLanguages.has(parameterLanguage)) {
    url += '&language=' + parameterLanguage;
  }

  fetch(url)
      .then((response) => {
        return response.json();
      })
      .then((messages) => {
        const messagesContainer = document.getElementById('message-container');
        if (messages.length == 0) {
          messagesContainer.innerHTML = '<p>This user has no posts yet.</p>';
        } else {
          messagesContainer.innerHTML = '';
        }


        var count = 0;

        messages.forEach((message) => {
          const messageDiv = buildMessageDiv(message);

          if (count !== maxMessages) {
          	count++;
          } else {
          	messageDiv.hidden = true;
          }

          messagesContainer.appendChild(messageDiv);
        });

      });
}



function showAll() {
	var messages = document.getElementsByClassName("message-div");
	for (var index=maxMessages; index < messages.length; index++) {
		messages[index].hidden = false;
	}
}



function hideMessages() {
	var messages = document.getElementsByClassName("message-div");
	for (var index=maxMessages; index < messages.length; index++) {
		messages[index].hidden = true;
	}
}
/**
 * Builds an element that displays the message.
 * @param {Message} message
 * @return {Element}
 */
function buildMessageDiv(message) {
  const headerDiv = document.createElement('div');
  headerDiv.classList.add('message-header');

  var messageHeader = 'From: ' + message.user +
  					' To: ' + message.recipient +
  					' - ' + new Date(message.timestamp) + ' [' + message.sentimentScore + ']';

  headerDiv.appendChild(document.createTextNode(messageHeader));

  const bodyDiv = document.createElement('div');
  bodyDiv.classList.add('message-body');
  bodyDiv.innerHTML = message.text;

  const messageDiv = document.createElement('div');
  messageDiv.classList.add('message-div');
  messageDiv.appendChild(headerDiv);
  messageDiv.appendChild(bodyDiv);

  return messageDiv;
}

function fetchAboutMe(){
  const url = '/about?user=' + parameterUsername;
  fetch(url).then((response) => {
    return response.text();
  }).then((aboutMe) => {
    const aboutMeContainer = document.getElementById('about-me-container');
    if(aboutMe == ''){
      aboutMe = 'This user has not entered any information yet.';
    }
    
    aboutMeContainer.innerHTML = aboutMe;

  });
}


/*
* Creates links to make requests for translating messages
*/
function buildLanguageLinks() {
  const userPageUrl = '/user-page.html?user=' + parameterUsername;
  const languagesListElement  = document.getElementById('languages');

  //Iterate through hash map
  supportedLanguages.forEach(function(value, key) {
    languagesListElement.appendChild(createListItem(createLink(
       userPageUrl + '&language=' + key, value)));
  });

}



/** Fetches data and populates the UI of the page. */
function buildUI() {
  setPageTitle();
  showMessageFormIfViewingSelf();
  fillMap();
  fetchMessages();
  fetchAboutMe();
  buildLanguageLinks();
}