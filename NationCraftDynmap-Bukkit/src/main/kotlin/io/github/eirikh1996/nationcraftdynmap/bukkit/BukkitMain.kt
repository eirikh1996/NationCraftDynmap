package io.github.eirikh1996.nationcraftdynmap.bukkit

import io.github.eirikh1996.nationcraft.bukkit.NationCraft
import io.github.eirikh1996.nationcraftdynmap.core.NationCraftDynmap
import io.github.eirikh1996.nationcraftdynmap.core.NationCraftDynmap.Companion.instance
import io.github.eirikh1996.nationcraftdynmap.core.Settings
import io.github.eirikh1996.nationcraftdynmap.core.TerritoryUpdateTask
import org.bukkit.plugin.java.JavaPlugin
import org.dynmap.DynmapCommonAPI
import org.dynmap.markers.MarkerAPI

class BukkitMain : JavaPlugin(), NationCraftDynmap {
    private lateinit var dmap : DynmapCommonAPI
    private val DEFAULT_INFOWINDOW = "<div class=\"infowindow\"><span style=\"font-size:120%;\">%regionname%</span><br />Flags<br /><span style=\"font-weight:bold;\">%flags%</span></div>"
    override val dynmapPlugin: DynmapCommonAPI
        get() = dmap

    override fun log(s: String) {
        logger.info(s)
    }

    lateinit var nationCraftPlugin: NationCraft

    override fun onEnable() {
        val dmap = server.pluginManager.getPlugin("dynmap")
        if (dmap !is DynmapCommonAPI || !dmap.isEnabled) {
            logger.severe("Required dependency dynmap was not found")
            server.pluginManager.disablePlugin(this)
            return
        }
        this.dmap = dmap
        val ncraft = server.pluginManager.getPlugin("NationCraft")
        if (ncraft !is NationCraft || !ncraft.isEnabled) {
            logger.severe("Required dependency NationCraft was not found")
            server.pluginManager.disablePlugin(this)
            return
        }
        saveDefaultConfig()
        nationCraftPlugin = ncraft
        server.scheduler.runTaskTimerAsynchronously(this, TerritoryUpdateTask, 0, config.getLong("update.period", 200))
        Settings.infowindow = config.getString("infowindow.format", DEFAULT_INFOWINDOW)!!
        Settings.SettlementTownCenterMarkerDefault = config.getString("settlements.townCenterMarkers.default", "redflag")!!
        Settings.SettlementTownCenterMarkerCapital = config.getString("settlements.townCenterMarkers.capital", "greenflag")!!
        Settings.SettlementTownCenterMarkerSiege = config.getString("settlements.townCenterMarkers.siege", "fire")!!
        Settings.NationsColorFill = config.getInt("nations.color.fill", 0xff0000)
        Settings.NationsColorLine = config.getInt("nations.color.line", 0xff0000)
        Settings.NationsOpacityFill = config.getDouble("nations.opacity.line", 0.3)
        Settings.NationsOpacityLine = config.getDouble("nations.opacity.line", 1.0)
        Settings.SettlementColorFill = config.getInt("settlements.color.fill", 0xfaf73c)
        Settings.SettlementColorLine = config.getInt("settlements.color.line", 0xfaf73c)
        Settings.SettlementOpacityFill = config.getDouble("settlements.opacity.line", 0.3)
        Settings.SettlementOpacityLine = config.getDouble("settlements.opacity.line", 1.0)
        if (!Settings.NationCustomSettings.isEmpty()) {
            Settings.NationCustomSettings.clear()
        }
        val nationCustom = config.getConfigurationSection("nations.custom")
        if (nationCustom != null) {
            for (key in nationCustom.getValues(false).keys) {
                val colorData = nationCustom.getConfigurationSection(key + ".color")
                val opacityData = nationCustom.getConfigurationSection(key + ".opacity")
                Settings.NationCustomSettings.put(key.toLowerCase(), Settings.CustomRegionData(colorData!!.getInt("fill"), colorData!!.getInt("line"), opacityData!!.getDouble("fill"), opacityData!!.getDouble("line"), nationCustom.getInt(key + ".weight", 1)))
            }
        }


    }

    override fun onLoad() {
        instance = this
    }


}