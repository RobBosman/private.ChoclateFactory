"use strict";

var eventBus = new EventBus('/eventbus');
var tileWithRainDrops = new TileWithRainDrops();
var rainIntensity = new RainIntensity();
var randomnessStatistics = new RandomnessStatistics();
var piApproximation = new PiApproximation();

window.onload = whenDomIsReady.completed;
eventBus.onopen = whenEventBusIsOpen.completed;

whenEventBusIsOpen
    .thenDo(function() {
      eventBus.registerHandler('rain.drop.notify', tileWithRainDrops.drawRainDrop);
    })
    .thenDo(function() {
      eventBus.registerHandler('rain.drop.notify', randomnessStatistics.updateDropCounter);
      eventBus.registerHandler('rain.drop.notify', randomnessStatistics.updateAverageX);
      eventBus.registerHandler('rain.drop.notify', randomnessStatistics.updateAverageY);
      eventBus.registerHandler('rain.drop.notify', randomnessStatistics.updateCorrelation);
    })
    .thenDo(function() {
      eventBus.registerHandler('rain.drop.notify', piApproximation.updatePi);
    });

new CompositeFuture()
    .and(whenSliderIsReady)
    .and(whenEventBusIsOpen)
    .thenDo(function() {
      eventBus.registerHandler('rain.intensity.set', '', rainIntensity.updateRainIntensity);
      eventBus.send('rain.intensity.get', '', rainIntensity.updateRainIntensity);
    });