"use strict";

function Future() {

  var callbackFunctions = [];
  var self = this;

  this.thenDo = function(callbackFunction) {
    callbackFunctions[callbackFunctions.length] = callbackFunction;
    return this;
  };

  this.completed = function() {
    for (var i = 0; i < callbackFunctions.length; i++) {
      callbackFunctions[i](self);
    }
  };
}


function CompositeFuture() {

  var composite = new Future();
  var futures = [];

  this.and = function(future) {
    futures[futures.length] = future;
    future.thenDo(this.futureCallback);
    return this;
  };

  this.thenDo = composite.thenDo;

  this.futureCallback = function(completedFuture) {
    var allCompleted = true;
    for (var i = 0; i < futures.length; i++) {
      if (futures[i] == completedFuture) {
        futures[i] = null;
      } else if (futures[i] != null) {
        allCompleted = false;
      }
    }
    if (allCompleted) {
      composite.completed();
    }
  };
}


var whenDomIsReady = new Future();
var whenEventBusIsOpen = new Future();
var whenSliderIsReady = new Future();
