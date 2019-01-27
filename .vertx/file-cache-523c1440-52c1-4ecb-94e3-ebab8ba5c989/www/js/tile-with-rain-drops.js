"use strict";

function TileWithRainDrops() {

  var SVG_NS = "http://www.w3.org/2000/svg";
  var RAIN_DROP_TIME_TO_LIVE = 2000;

  this.drawRainDrop = function(err, msg) {
    var tileSvg = document.getElementById("tile-with-rain-drops-svg");
    var drop = document.createElementNS(SVG_NS, "circle");
    drop.setAttribute("r", "2%");
    drop.setAttribute("cx", 100.0 * msg.body.x + "%");
    drop.setAttribute("cy", 100.0 * msg.body.y + "%");
    tileSvg.appendChild(drop);

    setTimeout(function () {
      tileSvg.removeChild(drop);
    }, RAIN_DROP_TIME_TO_LIVE);
  };
}