package com.hackclub.alopb.plugintemplate

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Animals
import org.bukkit.entity.Player
import org.bukkit.entity.Snowball
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.inventory.ShapelessRecipe
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.util.Vector

class Plugin_template : JavaPlugin() {
    override fun onEnable() {
        // Plugin startup logic
        getCommand("jump")?.setExecutor(JumpCommand())

        val enderPortalFrame = ItemStack(Material.END_PORTAL_FRAME, 1)
//        val recipe = ShapedRecipe(NamespacedKey(this, "frame"), enderPortalFrame)
        // shape(pierwszy rząd, drugi rząd, trzeci rząd), musimy zdefiniować, czym jest X
        // spacja to jest puste pole
//        recipe.shape("   ", " X ", "   ")
        // pojedynczy apostrof, bo oznacza on pojedynczy znak (Char)
//        recipe.setIngredient('X', Material.COBBLESTONE_STAIRS)

        val recipe = ShapelessRecipe(NamespacedKey(this, "frame"), enderPortalFrame)
        recipe.addIngredient(Material.COBBLESTONE_STAIRS)

        Bukkit.addRecipe(recipe)

        // KONIECZNIE DODAĆ TO, INACZEJ BĘDĄ PROBLEMY
        // rejestrujemy event śmierci moba na pluginie
        server.pluginManager.registerEvents(MobDeathEvent(), this)
        // rejestrujemy event wystrzału z łuku
        server.pluginManager.registerEvents(BowShootEvent(), this)
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }
}

// tutaj będzie to, co się wykona po wpisaniu komendy
class JumpCommand: CommandExecutor {
    override fun onCommand(sender: CommandSender, p1: Command, p2: String, args: Array<out String>?): Boolean {
        if (sender is Player) {
            sender.velocity = sender.velocity.add(Vector(0.0, 5.0, 0.0))
        }

        return true
    }
}

// org.bukkit.event.Listener
class MobDeathEvent: Listener {
    // KONIECZNIE DODAĆ TO, INACZEJ BĘDĄ PROBLEMY
    @EventHandler
    fun mobDeath(event: EntityDeathEvent) {
        // kiedy mob zginie, ma się wykonać to:
        val entity = event.entity

        if (entity is Animals) {
            event.drops.add(ItemStack(Material.ENDER_PEARL, 12))
            event.drops.add(ItemStack(Material.BLAZE_ROD, 6))
        }
    }
}

class BowShootEvent: Listener {
    @EventHandler
    fun bowShoot(event: EntityShootBowEvent) {
        val entity = event.entity
        if (entity is Player) {
            val projectile = Snowball::class.java
            entity.launchProjectile(projectile)

            // isCancelled oznacza, że domyślne zachowanie (wystrzał strzały) ma być usunięte
            // isCancelled = true => usuwamy domyślne zachowanie, gracz nie wystrzeli strzały
            event.isCancelled = true
        }
    }
}
