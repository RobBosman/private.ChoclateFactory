"use strict";

function TileWithPeanuts() {

  var SVG_NS = "http://www.w3.org/2000/svg";
  var PEANUT_TIME_TO_LIVE = 2000;

  this.drawPeanut = function(err, msg) {
    var tileSvg = document.getElementById("tile-with-peanuts-svg");
    var drop = document.createElementNS(SVG_NS, "circle");
    drop.setAttribute("r", "2%");
    drop.setAttribute("cx", 100.0 * msg.body.x + "%");
    drop.setAttribute("cy", 100.0 * msg.body.y + "%");
    tileSvg.appendChild(drop);

    setTimeout(function () {
      tileSvg.removeChild(drop);
    }, PEANUT_TIME_TO_LIVE);
  };
}