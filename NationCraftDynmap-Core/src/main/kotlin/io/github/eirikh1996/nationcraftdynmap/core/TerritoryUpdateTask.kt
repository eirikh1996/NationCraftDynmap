package io.github.eirikh1996.nationcraftdynmap.core

import io.github.eirikh1996.nationcraft.api.nation.Nation
import io.github.eirikh1996.nationcraft.api.nation.NationManager
import io.github.eirikh1996.nationcraft.api.settlement.Settlement
import io.github.eirikh1996.nationcraft.api.settlement.SettlementManager
import io.github.eirikh1996.nationcraft.api.territory.Territory
import io.github.eirikh1996.nationcraft.api.territory.TerritoryManager
import io.github.eirikh1996.nationcraft.api.utils.Direction
import org.dynmap.DynmapCommonAPI
import org.dynmap.markers.AreaMarker
import org.dynmap.markers.MarkerIcon
import org.dynmap.markers.MarkerSet
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.HashSet
import kotlin.collections.LinkedHashSet

object TerritoryUpdateTask : Runnable {
    val markerAPI = NationCraftDynmap.instance.dynmapPlugin.markerAPI
    val nationMarkers : MarkerSet
    val settlementMarkers : MarkerSet
    val nationMarkersByID = HashMap<String, AreaMarker>()

    val nationAreas = HashMap<String, AreaMarker>()
    val settlementAreas = HashMap<String, AreaMarker>()

    init {
        val nMark = markerAPI.getMarkerSet("nations")
        nationMarkers = if (nMark == null)
            markerAPI.createMarkerSet("nations", "Nations", markerAPI.markerIcons, false)
        else
            nMark
        val sMark = markerAPI.getMarkerSet("settlements")
        settlementMarkers = if (sMark == null)
            markerAPI.createMarkerSet("settlements", "Settlements", markerAPI.markerIcons, false)
        else
            sMark
    }
    override fun run() {
        processNationTerritories()
    }
    private val SHIFTS = arrayOf(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST)

    private fun worldTerritories(terrMgr : TerritoryManager) : Map<String, MutableSet<Territory>> {
        val worldTerrMap = HashMap<String, MutableSet<Territory>>()
        for (terr in terrMgr) {
            val worldTerr = worldTerrMap.getOrDefault(terr.world, HashSet())
            worldTerr.add(terr)
            worldTerrMap.put(terr.world, worldTerr)

        }
        return worldTerrMap
    }

    private fun processCoherentTerritories(terrColl : Collection<Territory>) : List<Set<Territory>> {
        val visited = HashSet<Territory>()
        val coherentTerritories = ArrayList<Set<Territory>>()
        for (terr in terrColl) {
            if (visited.contains(terr)) {
                continue
            }
            val queue = LinkedList<Territory>()
            queue.add(terr)
            val coherentTerritory = HashSet<Territory>()
            while (!queue.isEmpty()) {
                val poll = queue.poll()
                if (coherentTerritory.contains(poll) || visited.contains(poll))
                    continue

                coherentTerritory.add(poll)
                for (shift in SHIFTS) {
                    val node = poll.getRelative(shift)
                    if (!terrColl.contains(node))
                        continue
                    if (coherentTerritory.contains(node)) {
                        continue
                    }
                    queue.add(node)
                }


            }
            visited.addAll(coherentTerritory)
            coherentTerritories.add(coherentTerritory)
        }
        return coherentTerritories
    }

    private fun processNationTerritories() {
        for (nation in NationManager.getInstance()) {
            val worldTerrMap = worldTerritories(nation.territoryManager)

            for (w in worldTerrMap.keys) {
                val worldTerr = worldTerrMap.get(w)!!
                val coherentTerritories = processCoherentTerritories(worldTerr)
                var i = 1
                for (coherentTerritory in coherentTerritories) {
                    val corners = drawTerritoryLines(w, coherentTerritory)
                    val xCorners = DoubleArray(corners.size)
                    val zCorners = DoubleArray(corners.size)
                    for (index in 0..(corners.size - 1)) {
                        val corner = corners[index]
                        xCorners[index] = corner[0].toDouble() * 16.0
                        zCorners[index] = corner[1].toDouble() * 16.0
                    }
                    val id = nation.name.toLowerCase() + "_" + w + "_" + i
                    var amark : AreaMarker? = null
                    if (!nationMarkersByID.containsKey(id)) {
                        amark = nationMarkers.findAreaMarker(id)
                        if (amark != null) {
                            amark.setCornerLocations(xCorners, zCorners)
                        }
                    }
                    if (amark == null) {
                        amark = nationMarkers.createAreaMarker(id, nation.name, true, w, xCorners, zCorners, true)
                    }
                    else {
                        amark = nationMarkersByID.get(id)!!
                        amark.setCornerLocations(xCorners, zCorners)
                    }
                    if (amark != null && !nationMarkersByID.containsKey(id)) {
                        nationMarkersByID.put(id, amark)
                    }
                    if (amark == null)
                        continue
                    amark.description = formatNationInfo(nation)
                    val fillColor : Int
                    val lineColor : Int
                    val fillOpacity : Double
                    val lineOpacity : Double
                    if (Settings.NationCustomSettings.containsKey(nation.name.toLowerCase())) {
                        val custom = Settings.NationCustomSettings.get(nation.name.toLowerCase())!!
                        fillColor = custom.colorFill
                        lineColor = custom.colorLine
                        fillOpacity = custom.opacityFill
                        lineOpacity = custom.opacityLine
                    } else {
                        fillColor = Settings.NationsColorFill
                        lineColor = Settings.NationsColorLine
                        fillOpacity = Settings.NationsOpacityFill
                        lineOpacity = Settings.NationsOpacityLine
                    }
                    amark.setFillStyle(fillOpacity, fillColor)
                    amark.setLineStyle(1,lineOpacity, lineColor)
                    i++
                }

            }

        }
    }

    private fun processSettlementTerritories() {
        for (settlement in SettlementManager.getInstance()) {
            val worldTerrMap = worldTerritories(settlement.territory)
            for (w in worldTerrMap.keys) {
                val worldTerr = worldTerrMap.get(w)!!
                val coherentTerritories = processCoherentTerritories(worldTerr)
                var i = 1
                for (coherentTerritory in coherentTerritories) {
                    val corners = drawTerritoryLines(w, coherentTerritory)
                    val xCorners = DoubleArray(corners.size)
                    val zCorners = DoubleArray(corners.size)
                    for (index in 0..(corners.size - 1)) {
                        val corner = corners[index]
                        xCorners[index] = corner[0].toDouble() * 16.0
                        zCorners[index] = corner[1].toDouble() * 16.0
                    }
                    val id = settlement.name.toLowerCase() + "_" + w + "_" + i
                    var amark : AreaMarker? = null
                    if (!nationMarkersByID.containsKey(id)) {
                        amark = nationMarkers.findAreaMarker(id)
                        if (amark != null) {
                            amark.setCornerLocations(xCorners, zCorners)
                        }
                    }
                    if (amark == null) {
                        amark = nationMarkers.createAreaMarker(id, settlement.name, true, w, xCorners, zCorners, true)
                    }
                    else {
                        amark = nationMarkersByID.get(id)!!
                        amark.setCornerLocations(xCorners, zCorners)
                    }
                    if (amark != null && !nationMarkersByID.containsKey(id)) {
                        nationMarkersByID.put(id, amark)
                    }
                    if (amark == null)
                        continue
                    amark.description = formatSettlementInfo(settlement)
                    i++
                }
                val townCenter = settlement.townCenter
                val tcX = ((townCenter.x * 16) - 7).toDouble()
                val tcZ = ((townCenter.x * 16) - 7).toDouble()
                val tcLabel = "Town center of " + settlement.name
                val tcMarker = if (settlement.isUnderSiege) {
                    Settings.SettlementTownCenterMarkerSiege
                } else if (settlement.equals(settlement.nation.capital)) {
                    Settings.SettlementTownCenterMarkerCapital
                } else {
                    Settings.SettlementTownCenterMarkerDefault
                }
                val icon = markerAPI.getMarkerIcon(tcMarker)
                settlementMarkers.createMarker(tcLabel, tcLabel, w, tcX, 63.0, tcZ, icon, true)
            }

        }
    }

    private fun formatSettlementInfo(s: Settlement): String {
        var v = "<div class=\"regioninfo\">" + Settings.infowindow + "</div>"
        v = v.replace("%regionname%", s.name)
        val players = HashSet<String>()
        for (p in s.players.keys) {
            players.add(p.name)
        }
        v = v.replace("%players%", "Players: " + players.joinToString())
        return v
    }

    private fun drawTerritoryLines(w : String, coherentTerritory: Collection<Territory>) : List<IntArray>{
        var minX = Int.MAX_VALUE
        var minZ = Int.MAX_VALUE
        for (terr in coherentTerritory) {
            if (terr.x < minX) {
                minX = terr.x
                minZ = terr.z
            } else if (terr.x == minX && terr.z < minZ) {
                minZ = terr.z
            }
        }
        val corners = ArrayList<IntArray>()
        var dir = Dir.POSITIVE_X
        var currX = minX
        var currZ = minZ
        val initX = minX
        val initZ = minZ
        corners.add(intArrayOf(initX, initZ))
        while ((currX != initX) || (currZ != initZ) || (dir != Dir.NEGATIVE_Z)) {
            when (dir) {
                Dir.NEGATIVE_Z -> {
                    if (!coherentTerritory.contains(Territory(w, currX, currZ - 1))) {//Right turn
                        corners.add(intArrayOf(currX, currZ))
                        dir = Dir.POSITIVE_X
                    } else if (!coherentTerritory.contains(Territory(w, currX - 1, currZ - 1))) { //straight
                        currZ--
                    } else {
                        corners.add(intArrayOf(currX, currZ))
                        dir = Dir.NEGATIVE_X
                        currX--
                        currZ--
                    }
                }
                Dir.POSITIVE_Z -> {
                    if (!coherentTerritory.contains(Territory(w, currX, currZ + 1))) {
                        corners.add(intArrayOf(currX + 1, currZ + 1))
                        dir = Dir.NEGATIVE_X
                    } else if (!coherentTerritory.contains(Territory(w, currX + 1, currZ + 1))) {
                        currZ++
                    } else {
                        dir = Dir.POSITIVE_X
                        corners.add(intArrayOf(currX + 1, currZ + 1))
                        currX++
                        currZ++
                    }
                }
                Dir.NEGATIVE_X -> {
                    if (!coherentTerritory.contains(Territory(w, currX - 1, currZ))) {//Right turn
                        corners.add(intArrayOf(currX, currZ + 1))
                        dir = Dir.NEGATIVE_Z
                    } else if (!coherentTerritory.contains(Territory(w, currX - 1, currZ + 1))) { //straight
                        currX--
                    } else {
                        corners.add(intArrayOf(currX, currZ + 1))
                        dir = Dir.POSITIVE_Z
                        currX--
                        currZ++
                    }
                }
                Dir.POSITIVE_X -> {
                    if (!coherentTerritory.contains(Territory(w, currX + 1, currZ))) {//Right turn
                        corners.add(intArrayOf(currX + 1, currZ))
                        dir = Dir.POSITIVE_Z
                    } else if (!coherentTerritory.contains(Territory(w, currX + 1, currZ - 1))) { //straight
                        currX++
                    }
                    else {
                        corners.add(intArrayOf(currX + 1, currZ))
                        dir = Dir.NEGATIVE_Z
                        currX++
                        currZ--
                    }

                }
            }
        }
        return corners
    }

    private fun formatNationInfo(n : Nation) : String {
        var v = "<div class=\"regioninfo\">" + Settings.infowindow + "</div>"
        v = v.replace("%regionname%", n.name)
        val players = HashSet<String>()
        for (p in n.players.keys) {
            players.add(p.name)
        }
        v = v.replace("%players%", "Players: " + players.joinToString())
        return v
    }
}