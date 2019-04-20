const urlParams = new URLSearchParams(window.location.search);
const maxMessages = 5;
//HashMap to map supported languages and their language codes
var supportedLanguages = new Map();


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

function setPageTitle() {
  document.title = 'Events Page';
}

/**
 * Shows the message form if the user is logged in.
 */
function showMessageFormIfViewingSelf() {
  fetch('/login-status')
      .then((response) => {
        return response.json();
      })
      .then((loginStatus) => {
        if (loginStatus.isLoggedIn) {
          const messageForm = document.getElementById('events-form');

          //Specifies where to send form data once it is submitted
          messageForm.action = '/events';
          messageForm.classList.remove('hidden');
        }
      });
}

/** Fetches messages and add them to the page. */
function fetchMessages() {
  const parameterLanguage = urlParams.get('language');
  let url = '/events';

  if (parameterLanguage && 
    supportedLanguages.has(parameterLanguage)) {
    url += '&language=' + parameterLanguage;
  }

  fetch(url)
      .then((response) => {
        return response.json();
      })
      .then((events) => {
        const messagesContainer = document.getElementById('events-container');
        if (events.length == 0) {
          messagesContainer.innerHTML = '<p>There are no events posts yet.</p>';
        } else {
          messagesContainer.innerHTML = '';
        }


        var count = 0;

        events.forEach((event) => {
          const messageDiv = buildMessageDiv(event);

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
  var events = document.getElementsByClassName("events-div");
  for (var index=maxMessages; index < events.length; index++) {
    events[index].hidden = false;
  }
}



function hideMessages() {
  var events = document.getElementsByClassName("events-div");
  for (var index=maxMessages; index < events.length; index++) {
    events[index].hidden = true;
  }
}
/**
 * Builds an element that displays the message.
 * @param {Message} message
 * @return {Element}
 */
function buildMessageDiv(event) {
  const usernameDiv = document.createElement('div');
  usernameDiv.classList.add("email-header");
  usernameDiv.appendChild(document.createTextNode(event.user));

  const timeDiv = document.createElement('div');
  timeDiv.classList.add('info-header');
  timeDiv.appendChild(document.createTextNode(new Date(event.timestamp)));

  const headerDiv = document.createElement('div');
  headerDiv.classList.add('events-header');
  var messageHeader = 'From: ' + event.user +
            ' - ' + new Date(event.timestamp) + ' [Sentiment: ' + event.sentimentScore + ']';

  headerDiv.appendChild(document.createTextNode(messageHeader));

  // const bodyDiv = document.createElement('div');
  // bodyDiv.classList.add('message-body');
  // bodyDiv.appendChild(document.createTextNode(message.text));
  const bodyDiv = document.createElement('div');
  bodyDiv.classList.add('events-body');
  bodyDiv.innerHTML = event.text;

  const entityDiv = document.createElement('div');
  entityDiv.classList.add('events-entity');
  entityDiv.innerHTML = event.entityInformation;

  const messageDiv = document.createElement('div');
  messageDiv.classList.add("events-div");
  messageDiv.appendChild(headerDiv);
  messageDiv.appendChild(entityDiv);
  messageDiv.appendChild(bodyDiv);

  return messageDiv;
}


/*
* Creates links to make requests for translating messages
*/
function buildLanguageLinks() {
  const userPageUrl = '/events.html';
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
  buildLanguageLinks();
}