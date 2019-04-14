// Fetch messages and add them to the page.
function fetchMessages() {
  const url = '/events';
  fetch(url).then((response) => {
    return response.json();
  }).then((messages) => {
    const messageContainer = document.getElementById('message-container');
    if (messages.length == 0) {
      messageContainer.innerHTML = '<p>There are no posts yet.</p>';
    }
    else {
      messageContainer.innerHTML = '';
    }
    messages.forEach((message) => {
      const messageDiv = buildMessageDiv(message);
      messageContainer.appendChild(messageDiv);
    });
  });
}

function buildMessageDiv(message) {
  const usernameDiv = document.createElement('div');
  usernameDiv.classList.add("email-header");
  usernameDiv.appendChild(document.createTextNode(message.user));

  const timeDiv = document.createElement('div');
  timeDiv.classList.add('info-header');
  timeDiv.appendChild(document.createTextNode(new Date(message.timestamp)));

  const headerDiv = document.createElement('div');
  headerDiv.classList.add('message-header');
  var messageHeader = 'From: ' + message.user +
            ' - ' + new Date(message.timestamp) + ' [' + message.sentimentScore + ']';

  headerDiv.appendChild(document.createTextNode(messageHeader));

  // const bodyDiv = document.createElement('div');
  // bodyDiv.classList.add('message-body');
  // bodyDiv.appendChild(document.createTextNode(message.text));
  const bodyDiv = document.createElement('div');
  bodyDiv.classList.add('message-body');
  bodyDiv.innerHTML = message.text;

  const entityDiv = document.createElement('div');
  entityDiv.classList.add('message-entity');
  entityDiv.innerHTML = message.entityInformation;

  const messageDiv = document.createElement('div');
  messageDiv.classList.add("message-div");
  messageDiv.appendChild(headerDiv);
  messageDiv.appendChild(entityDiv);
  messageDiv.appendChild(bodyDiv);

  return messageDiv;
}

// Fetch data and populate the UI of the page.
function buildUI() {
  fetchMessages();
}