"use strict";

function PiApproximation() {

  var numRainDrops = 0;
  var numWithinRadius = 0;

  this.updatePi = function(err, msg) {
    numRainDrops++;
    var rainDrop = msg.body;
    if (rainDrop.x * rainDrop.x + rainDrop.y * rainDrop.y <= 1.0) {
      numWithinRadius++;
    }
    document.getElementById('num-drops').innerHTML = numRainDrops.toFixed(0);
    document.getElementById('num-within-radius').innerHTML = numWithinRadius.toFixed(0);
    document.getElementById('pi').innerHTML = (4.0 * numWithinRadius / numRainDrops).toFixed(16);
  };
}