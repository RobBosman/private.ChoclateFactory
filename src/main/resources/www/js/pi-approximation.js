"use strict";

function PiApproximation() {

  var numPeanuts = 0;
  var numWithinRadius = 0;

  this.updatePi = function(err, msg) {
    numPeanuts++;
    var peanut = msg.body;
    if (peanut.x * peanut.x + peanut.y * peanut.y <= 1.0) {
      numWithinRadius++;
    }
    document.getElementById('num-drops').innerHTML = numPeanuts.toFixed(0);
    document.getElementById('num-within-radius').innerHTML = numWithinRadius.toFixed(0);
    document.getElementById('pi').innerHTML = (4.0 * numWithinRadius / numPeanuts).toFixed(16);
  };
}