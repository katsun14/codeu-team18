
google.charts.load('current', { packages: ['corechart'] });

google.charts.setOnLoadCallback(drawMedalChart);

google.charts.setOnLoadCallback(fetchMessageData);

google.charts.setOnLoadCallback(fetchSentimentData);

google.charts.setOnLoadCallback(fetchGeoData);

function drawMedalChart() {
  var medal_data = new google.visualization.DataTable();

  //define columns for the book_data DataTable instance
  medal_data.addColumn('string', 'Country');
  medal_data.addColumn('number', 'Gold');
  medal_data.addColumn('number', 'Silver');
  medal_data.addColumn('number', 'Bronze');


  //add data to medal_data
  medal_data.addRows([
    ["Norway", 14, 14, 11],
    ["Germany", 14, 10, 7],
    ["Canada", 11, 8, 10],
    ["United States", 9, 8, 6],
    ["Netherlands", 8, 6, 6]
    ]);

  var medal_chart = new google.visualization.ColumnChart(document.getElementById('medal_chart'));

  var medal_chart_options = {
    title: "Medal Counts of Five Highest Ranking Countries in 2018 Winter Olympics",
    height: 400,
    width: 1000,
    colors: ['#D6AF36', '#D7D7D7', '#A77044'],
    hAxis: {
      title: "Countries"
    },
    vAxis: {
      title: "Number of Medals"
    }
  };


  medal_chart.draw(medal_data, medal_chart_options);
}

function drawMessageChart(msgData) {
  
  var message_chart = new google.visualization.LineChart(document.getElementById('message_chart'));

  var message_chart_options = {
    title: "Message Counts Over Time",
    height: 400,
    width: 800,
    hAxis: {
      title: "Date"
    },
    vAxis: {
      title: "Number of Messages"
    }
  };

  message_chart.draw(msgData, message_chart_options);
}

function fetchMessageData() {
  fetch("/messagechart")
  .then((response) => {
    return response.json();
  })
  .then((msgJson) => {
    var msgData = new google.visualization.DataTable();
                //define columns for the DataTable instance
                msgData.addColumn('date', 'Date');
                msgData.addColumn('number', 'Message Count');


                for (i = 0; i < msgJson.length; i++) {
                  msgRow = [];
                  var timestampAsDate = new Date (msgJson[i].timestamp);
                  var totalMessages = i + 1;
                    //add the formatted values to msgRow array by using JS' push method
                    msgRow.push(timestampAsDate, totalMessages);
                    //msgRow.push(totalMessages);

                    //console.log(msgRow);
                    msgData.addRow(msgRow);

                  }
                //console.log(msgData);
                drawMessageChart(msgData);
              });
}

function drawSentimentChart(msgData) {
  
  var sentiment_chart = new google.visualization.LineChart(document.getElementById('sentiment_chart'));

  var sentiment_chart_options = {
    title: "Average Sentiment Over Time",
    height: 400,
    width: 800,
    hAxis: {
      title: "Date"
    },
    vAxis: {
      title: "Average Sentiment"
    }
  };

  sentiment_chart.draw(msgData, sentiment_chart_options);
}

function fetchSentimentData() {
  fetch("/sentimentchart")
  .then((response) => {
    return response.json();
  })
  .then((msgJson) => {
    var msgData = new google.visualization.DataTable();
                //define columns for the DataTable instance
                msgData.addColumn('date', 'Date');
                msgData.addColumn('number', 'Average Sentiment');

                var avgSentiment = 0;
                for (i = 0; i < msgJson.length; i++) {
                  msgRow = [];
                  var timestampAsDate = new Date (msgJson[i].timestamp);
                  var sentiment = msgJson[i].sentimentScore;
                  var count = i + 1;
                  avgSentiment = sentiment / count;
                    //add the formatted values to msgRow array by using JS' push method
                    msgRow.push(timestampAsDate, avgSentiment);
                    //msgRow.push(totalMessages);

                    //console.log(msgRow);
                    msgData.addRow(msgRow);

                  }
                //console.log(msgData);
                drawSentimentChart(msgData);
              });
}

function drawGeoChart(usrData) {
  
  var geo_chart = new google.visualization.GeoChart(document.getElementById('geo_chart'));

  var geo_chart_options = {
    title: "Users per Country",
    height: 400,
    width: 800,
  };

  geo_chart.draw(usrData, geo_chart_options);
}

function fetchGeoData() {
  fetch("/geochart")
  .then((response) => {
    return response.json();
  })
  .then((usrJson) => {
    var usrData = new google.visualization.DataTable();
                //define columns for the DataTable instance
                usrData.addColumn('string', 'Country of Origin');
                usrData.addColumn('number', 'Number of Users');

                var count = 1;
                var country = usrJson[0].country;
                var flag = false;
                for (i = 1; i < usrJson.length; i++) {
                  if (country != usrJson[i].country){
                    usrRow = [];
                    usrRow.push(country, count);
                    usrData.addRow(usrRow);
                    console.log(country);
                    console.log(count);
                    flag = true;
                    count = 1;
                    if (i + 1 < usrJson.length){
                      country = usrJson[i+1].country;
                    }
                  } else{
                    count = count + 1;
                    flag = false
                  }

                }
                if (flag == false){
                  usrRow = [];
                  usrRow.push(country, count);
                  usrData.addRow(usrRow);
                  console.log(country);
                  console.log(count);
                }
                //console.log(msgData);
                drawGeoChart(usrData);
              });
}
