package position.avro

import com.fasterxml.jackson.annotation.JsonProperty

class VehiclePosition {
  @JsonProperty("VP")
  var VP: VehicleValues? = null

  inner class VehicleValues {
    @JsonProperty("desi")
    var desi: String? = null
    @JsonProperty("dir")
    var dir: String? = null
    @JsonProperty("oper")
    var oper = 0
    @JsonProperty("veh")
    var veh = 0
    @JsonProperty("tst")
    var tst: String? = null
    @JsonProperty("tsi")
    var tsi: Long = 0
    @JsonProperty("spd")
    var spd: Double? = null
    @JsonProperty("hdg")
    var hdg = 0
    @JsonProperty("lat")
    var lat: Double? = null
    @JsonProperty("long")
    var longitude: Double? = null
    @JsonProperty("acc")
    var acc: Double? = null
    @JsonProperty("dl")
    var dl = 0
    @JsonProperty("odo")
    var odo = 0
    @JsonProperty("drst")
    var drst = 0
    @JsonProperty("oday")
    var oday: String? = null
    @JsonProperty("jrn")
    var jrn = 0
    @JsonProperty("line")
    var line = 0
    @JsonProperty("start")
    var start: String? = null
    @JsonProperty("loc")
    var loc: String? = null
    @JsonProperty("stop")
    var stop: String? = null
    @JsonProperty("route")
    var route: String? = null
    @JsonProperty("occu")
    var occu = 0
    @JsonProperty("seq")
    var seq = 0
  }
}
