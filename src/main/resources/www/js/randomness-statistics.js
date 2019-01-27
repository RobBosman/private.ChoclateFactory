"use strict";

function RandomnessStatistics() {

  var numRainDrops = 0;
  var sumX = 0.0;
  var sumY = 0.0;
  var sumDeviationXY = 0.0;
  var sumSquaredDeviationX = 0.0;
  var sumSquaredDeviationY = 0.0;

  this.updateDropCounter = function(err, msg) {
    numRainDrops++;
  };

  this.updateAverageX = function(err, msg) {
    sumX += msg.body.x;
    document.getElementById('average-x').innerHTML = (sumX / numRainDrops).toFixed(16);
  };

  this.updateAverageY = function(err, msg) {
    sumY += msg.body.y;
    document.getElementById('average-y').innerHTML = (sumY / numRainDrops).toFixed(16);
  };

  this.updateCorrelation = function(err, msg) {
    var averageX = sumX / numRainDrops;
    var averageY = sumY / numRainDrops;
    var deviationX = msg.body.x - averageX;
    var deviationY = msg.body.y - averageY;
    sumDeviationXY += deviationX * deviationY;
    sumSquaredDeviationX += deviationX * deviationX;
    sumSquaredDeviationY += deviationY * deviationY;
    if (sumSquaredDeviationX * sumSquaredDeviationY != 0.0) {
      var correlationCoefficient = sumDeviationXY / Math.sqrt(sumSquaredDeviationX * sumSquaredDeviationY);
      document.getElementById('correlation-coefficient').innerHTML = correlationCoefficient.toFixed(16);
    }
  };
}
