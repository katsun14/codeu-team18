let map;

function createMap() {
  map = new google.maps.Map(document.getElementById('map'), {
    center: {lat: 37.422, lng: -122.084},
    zoom: 16
  });

  addLandmark(map, 37.423829, -122.092154, 'Google West Campus', 
	'Google West Campus is home to YouTube and Maps.');

  addLandmark(map, 37.421903, -122.084674, 'Stan the T-Rex', 
  	'This is Stan, the T-Rex statue.');
  
  addLandmark(map, 37.420919, -122.086619, 'Permanente Creek Trail', 
  	'Permanente Creek Trail connects Google to a system of bike trails.');

}



function addLandmark(map, lat, lng, title, description) {
  const trexMarker = new google.maps.Marker({
  	position: {lat: lat, lng: lng},
  	map: map,
  	title: title
  });

  var trexInfoWindow = new google.maps.InfoWindow({
	 content: description
  });

  trexMarker.addListener('click', function() {
  	trexInfoWindow.open(map, trexMarker);
  });
}