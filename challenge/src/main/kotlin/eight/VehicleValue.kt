package eight

import com.fasterxml.jackson.annotation.JsonProperty

class VehicleValue {
  var desi: String? = null
  var dir: String? = null
  var oper = 0
  var veh = 0
  var tst: String? = null
  var tsi = 0
  var spd = 0f
  var hdg = 0
  var lat = 0f
  @JsonProperty("long")
  var longitude = 0f
  var acc = 0f
  var dl = 0
  var odo = 0
  var drst = 0
  var oday: String? = null
  var jrn = 0
  var line = 0
  var start: String? = null
  var loc: String? = null
  var stop: String? = null
  var route: String? = null
  var occu = 0
  var seq = 0
}