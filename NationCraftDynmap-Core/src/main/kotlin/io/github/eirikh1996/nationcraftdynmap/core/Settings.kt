package io.github.eirikh1996.nationcraftdynmap.core

object Settings {
    var SettlementTownCenterMarkerSiege = ""
    var SettlementTownCenterMarkerCapital = ""
    var SettlementTownCenterMarkerDefault = ""
    var SettlementColorFill = 0xff0000
    var SettlementColorLine = 0xff0000
    var SettlementOpacityFill = 0.3
    var SettlementOpacityLine = 1.0
    var NationsColorFill = 0xff0000
    var NationsColorLine = 0xff0000
    var NationsOpacityFill = 0.3
    var NationsOpacityLine = 1.0
    var NationCustomSettings = HashMap<String, CustomRegionData>()
    var infowindow = ""


    data class CustomRegionData constructor(val colorFill : Int, val colorLine : Int, val opacityFill : Double, val opacityLine : Double, val weightLine : Int) {

        override fun equals(other: Any?): Boolean {
            if (other !is CustomRegionData)
                return false
            return colorFill == other.colorFill &&
                    colorLine == other.colorLine &&
                    opacityFill == other.opacityFill &&
                    opacityLine == other.opacityLine &&
                    weightLine == other.weightLine
        }

        override fun hashCode(): Int {
            return colorFill.hashCode() + colorLine.hashCode() + opacityFill.hashCode() + opacityLine.hashCode() + weightLine.hashCode()
        }
    }
}