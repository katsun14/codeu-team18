
google.charts.load('current', { packages: ['corechart'] });

google.charts.setOnLoadCallback(drawChart);

function drawChart() {
  var medal_data = new google.visualization.DataTable();

  //define columns for the medal_data DataTable instance
  medal_data.addColumn('string', 'Country');
  medal_data.addColumn('number', 'Gold');
  medal_data.addColumn('number', 'Silver');
  medal_data.addColumn('number', 'Bronze');


  //add data to book_data
  medal_data.addRows([
    ["Norway", 14, 14, 11],
    ["Germany", 14, 10, 7],
    ["Canada", 11, 8, 10],
    ["United States", 9, 8, 6],
    ["Netherlands", 8, 6, 6]
  ]);

  var medal_chart = new google.visualization.ColumnChart(document.getElementById('medal_chart'));

  var medal_chart_options = {
    chart: {
      title: "Medal Counts of Five Highest Ranking Countries in 2018 Winter Olympics"
    },
    colors: ['#D6AF36', '#D7D7D7', '#A77044']
  };


  medal_chart.draw(medal_data, medal_chart_options);
}
