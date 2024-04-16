package com.hackclub.alopb.plugintemplate

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.attribute.Attribute
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Animals
import org.bukkit.entity.Firework
import org.bukkit.entity.Player
import org.bukkit.entity.Snowball
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.event.entity.FireworkExplodeEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.inventory.ShapelessRecipe
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.util.Vector
import kotlin.math.sqrt

class Plugin_template : JavaPlugin() {
    override fun onEnable() {
        // Plugin startup logic
        getCommand("jump")?.setExecutor(JumpCommand())
        getCommand("tntbow")?.setExecutor(TntBowCommand())
        getCommand("mygui")?.setExecutor(OpenCustomInventory())

        val enderPortalFrame = ItemStack(Material.WATER, 2)
        val recipe = ShapedRecipe(NamespacedKey(this, "frame"), enderPortalFrame)
//         shape(pierwszy rząd, drugi rząd, trzeci rząd), musimy zdefiniować, czym jest X
//         spacja to jest puste pole
        recipe.shape("   ", " X ", "   ")
//         pojedynczy apostrof, bo oznacza on pojedynczy znak (Char)
        recipe.setIngredient('X', Material.COBBLESTONE_STAIRS)

        Bukkit.addRecipe(recipe)

        // KONIECZNIE DODAĆ TO, INACZEJ BĘDĄ PROBLEMY
        // rejestrujemy event śmierci moba na pluginie
        server.pluginManager.registerEvents(MobDeathEvent(), this)
        // rejestrujemy event wystrzału z łuku
        server.pluginManager.registerEvents(BowShootEvent(), this)
        server.pluginManager.registerEvents(SnowballHitEvent(), this)
        server.pluginManager.registerEvents(FireworkLauncher(), this)
        server.pluginManager.registerEvents(SetMaxHealthOnJoin(), this)
        server.pluginManager.registerEvents(InventoryClick(), this)
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }
}

class OpenCustomInventory: CommandExecutor {
    override fun onCommand(sender: CommandSender, p1: Command, p2: String, p3: Array<out String>?): Boolean {
        if (sender is Player) {
            val inventory = Bukkit.createInventory(sender, 9, Component.text("My Inventory"))

            val diamond = ItemStack(Material.DIAMOND)
            inventory.setItem(4, diamond)

            sender.openInventory(inventory)
        }

        return true
    }
}

class InventoryClick: Listener {
    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val player = event.whoClicked
        val item = event.currentItem

        player.sendMessage("Slot ${event.slot}")
        val isTopBar = event.slot in 0..8
        val isCustomInventory = event.view.title() == Component.text("My Inventory")

        if (item?.type == Material.DIAMOND && isTopBar && isCustomInventory) {
            player.sendMessage("You cannot pick this item up.")
            event.isCancelled = true
        }
    }
}

class SetMaxHealthOnJoin: Listener {
    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        player.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue = 100.0
//        player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)?.baseValue = 2.0
        player.health = 100.0
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

class TntBowCommand: CommandExecutor {
    override fun onCommand(sender: CommandSender, p1: Command, p2: String, p3: Array<out String>?): Boolean {
        if (sender is Player) {
            val newBow = ItemStack(Material.BOW)
            // pobieramy metadane itemu
            val itemMeta = newBow.itemMeta

            // ustawiamy wlasne dane modelu, ktore rozrozniaja specjalny luk od zwyklego
            itemMeta.setCustomModelData(1234567)
            itemMeta.displayName(Component.text("EXPLOSIVE BOW").decorate(TextDecoration.BOLD).color(TextColor.color(175, 0, 0)))

            newBow.setItemMeta(itemMeta)
            sender.inventory.addItem(newBow)
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
        // jesli luk jest tym specjalnym, to strzelamy sniezka
        // operator ?: wykonuje to, co po prawej stronie, jesli to,
        // co po lewej stronie jest nullem
        val bow = event.bow ?: return
        // trzeba jednak najpierw sprawdzic, czy luk ma odpowiednie metadane, bo inaczej plugin nie zadziala
        if (!bow.hasItemMeta()) return
        if (!bow.itemMeta.hasCustomModelData()) return

        val bowMeta = bow.itemMeta ?: return
        // wyciagamy informacje o tym, czy luk jest tym specjalnym, czy nie
        val bowCustomModelData = bowMeta.customModelData

        if (entity is Player && bowCustomModelData == 1234567) {
            val projectile = Snowball::class.java
            entity.launchProjectile(projectile)

            // isCancelled oznacza, że domyślne zachowanie (wystrzał strzały) ma być usunięte
            // isCancelled = true => usuwamy domyślne zachowanie, gracz nie wystrzeli strzały
            event.isCancelled = true
        }
    }
}

class SnowballHitEvent: Listener {
    @EventHandler
    fun onSnowballHit(event: ProjectileHitEvent) {
        // event.entity -> pocisk w evencie
        val projectile = event.entity

        if (projectile !is Snowball) return

        val location = projectile.location

        // TNT ma domyślna sile eksplozji 4, F na koncu, bo oznacza ona zmienna typu float
        location.createExplosion(20F)
    }
}

class FireworkLauncher: Listener {
    @EventHandler
    fun onFireworkExplode(event: FireworkExplodeEvent) {
        val firework = event.entity
        val explosionPosition = firework.location.add(Vector(0.0, -1.0, 0.0))

        val entities = explosionPosition.getNearbyLivingEntities(8.0, 8.0, 8.0)
        for (entity in entities) {
            // roznica miedzy miejscem wybuchu a miejscem danego bytu
            val blastDiff = entity.location.subtract(explosionPosition).toVector()
            val blastDirection = blastDiff.clone().normalize()
            // sqrt() importujemy z kotlin.math
            val strength = sqrt(0.5 / blastDiff.length()) * 3.5
            // szybkosc wynikajaca z sily wybuchu
            val velocity = blastDirection.multiply(strength)
            entity.velocity = entity.velocity.add(velocity)
        }
    }

    @EventHandler
    fun onFireworkDamage(event: EntityDamageByEntityEvent) {
        // anulujemy obrazenia zadawane przez fajerwerki
        event.isCancelled = true
    }
}
