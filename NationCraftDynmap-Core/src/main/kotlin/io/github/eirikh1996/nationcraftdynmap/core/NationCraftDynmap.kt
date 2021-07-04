package io.github.eirikh1996.nationcraftdynmap.core

import org.dynmap.DynmapCommonAPI
import org.dynmap.markers.MarkerAPI

interface NationCraftDynmap {
    val dynmapPlugin : DynmapCommonAPI
    fun log(s : String)
    companion object {
        lateinit var instance : NationCraftDynmap

    }
}