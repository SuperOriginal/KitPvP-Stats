package me.aaron.kitpvpstats.stats;

import me.aaron.kitpvpstats.KitPvPStats;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Created by Aaron.
 * Listens to Player events and increments player stats accordingly
 */
public class StatsListener implements Listener{
    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        KitPvPStats.getI().getDbManager().updateNames(e.getPlayer().getUniqueId(), e.getPlayer().getName());
        KitPvPStats.getI().getStatsCache().insertIntoCache(e.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e){
        KitPvPStats.getI().getStatsCache().removeFromCache(e.getPlayer());
    }

    @EventHandler
    public void onKick(PlayerKickEvent e){
        KitPvPStats.getI().getStatsCache().removeFromCache(e.getPlayer());
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e){
        Player killer = e.getEntity().getKiller();
        if(killer != null){
            KitPvPStats.getI().getStatsCache().getStatsForPlayer(killer.getUniqueId(), stats -> stats.incrementKill(1));
        }
        KitPvPStats.getI().getStatsCache().getStatsForPlayer(e.getEntity().getUniqueId(), stats -> stats.incrementDeath(1));
    }

    //I'm tracking all types of damage because that's easier for testing purposes than PVP
    @EventHandler
    public void onDmg(EntityDamageByEntityEvent e){
        if(e.getDamager() instanceof Player){
            KitPvPStats.getI().getStatsCache().getStatsForPlayer(e.getDamager().getUniqueId(), stats -> stats.incrementDamage(e.getDamage()));
        }
    }
}
