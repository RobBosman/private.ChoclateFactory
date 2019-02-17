"use strict";

function RandomnessStatistics() {

  var numPeanuts = 0;
  var sumX = 0.0;
  var sumY = 0.0;
  var sumDeviationXY = 0.0;
  var sumSquaredDeviationX = 0.0;
  var sumSquaredDeviationY = 0.0;

  this.updateDropCounter = function(err, msg) {
    numPeanuts++;
  };

  this.updateAverageX = function(err, msg) {
    sumX += msg.body.x;
    document.getElementById('average-x').innerHTML = (sumX / numPeanuts).toFixed(16);
  };

  this.updateAverageY = function(err, msg) {
    sumY += msg.body.y;
    document.getElementById('average-y').innerHTML = (sumY / numPeanuts).toFixed(16);
  };

  this.updateCorrelation = function(err, msg) {
    var averageX = sumX / numPeanuts;
    var averageY = sumY / numPeanuts;
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
