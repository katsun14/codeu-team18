let editMarker;
let map;

function createOlymMap() {
  fetch('/olym').then(function (response) {
    return response.json();
  }).then((olym) => {

    const map = new google.maps.Map(document.getElementById('olym-map'), {
      center: { lat: 33.770015, lng: -39.379235 },
      zoom: 1
    });

    olym.forEach((city) => {
      new google.maps.Marker({
        position: { lat: city.lat, lng: city.lng },
        map: map
      });
    });
  });
}

function createMap() {

  map = new google.maps.Map(document.getElementById('map'), {
    center: { lat: 38.5949, lng: -94.8923 },
    zoom: 4
  });

  map.addListener('click', (event) => {
    createMarkerForEdit(event.latLng.lat(), event.latLng.lng());
  });

  fetchMarkers();
}

function fetchMarkers() {
  fetch('/user-markers').then((response) => {
    return response.json();
  }).then((markers) => {
    markers.forEach((marker) => {
      createMarkerForDisplay(marker.lat, marker.lng, marker.content)
    });
  });
}

function createMarkerForDisplay(lat, lng, content) {

  const marker = new google.maps.Marker({
    position: { lat: lat, lng: lng },
    map: map
  });

  var infoWindow = new google.maps.InfoWindow({
    content: content
  });
  marker.addListener('click', () => {
    infoWindow.open(map, marker);
  });
}

function postMarker(lat, lng, content) {
  const params = new URLSearchParams();
  params.append('lat', lat);
  params.append('lng', lng);
  params.append('content', content);

  fetch('/user-markers', {
    method: 'POST',
    body: params
  });
}

function createMarkerForEdit(lat, lng) {

  if (editMarker) {
    editMarker.setMap(null);
  }

  editMarker = new google.maps.Marker({
    position: { lat: lat, lng: lng },
    map: map
  });

  const infoWindow = new google.maps.InfoWindow({
    content: buildInfoWindowInput(lat, lng)
  });

  google.maps.event.addListener(infoWindow, 'closeclick', () => {
    editMarker.setMap(null);
  });

  infoWindow.open(map, editMarker);
}

function buildInfoWindowInput(lat, lng) {
  const textBox = document.createElement('textarea');
  const button = document.createElement('button');
  button.appendChild(document.createTextNode('Submit'));

  button.onclick = () => {
    postMarker(lat, lng, textBox.value);
    createMarkerForDisplay(lat, lng, textBox.value);
    editMarker.setMap(null);
  };

  const containerDiv = document.createElement('div');
  containerDiv.appendChild(textBox);
  containerDiv.appendChild(document.createElement('br'));
  containerDiv.appendChild(button);

  return containerDiv;
}