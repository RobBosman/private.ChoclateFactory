"use strict";

function PeanutSpeed() {

  var ID = Math.random().toFixed(10);
  var peanutSpeedSlider;
  var isSuppressingUpdates = true;

  this.initializeSlider = function() {
    peanutSpeedSlider = document.getElementById('peanut-speed');
    noUiSlider.create(peanutSpeedSlider, {
        start: 0,
        orientation: 'vertical',
        direction: 'rtl',
        range: { 'min': 0, 'max': 100 },
        pips: { mode: 'positions', values: [0, 25, 50, 75, 100], density: 4
      }
    });

    peanutSpeedSlider.noUiSlider.on('update', function(values, handle) {
      if (!isSuppressingUpdates) {
        eventBus.publish('peanut.speed.set',
          { 'value': values[handle] / 100.0 },
          { 'id': ID });
      }
    });

    isSuppressingUpdates = false;
    whenSliderIsReady.completed();
  };

  this.updatePeanutSpeed = function(err, msg) {
    var intensityPercentage = 100.0 * msg.body.value;
    document.getElementById('peanut-speed-percentage').innerHTML = intensityPercentage.toFixed(0) + "%";
    if (msg.headers == null || msg.headers.id != ID) {
      isSuppressingUpdates = true;
      peanutSpeedSlider.noUiSlider.set(intensityPercentage);
      isSuppressingUpdates = false;
    }
  };

  whenDomIsReady.thenDo(this.initializeSlider);
}