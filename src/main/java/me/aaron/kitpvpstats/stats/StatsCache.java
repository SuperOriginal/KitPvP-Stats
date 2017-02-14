package me.aaron.kitpvpstats.stats;

import lombok.Getter;
import me.aaron.kitpvpstats.KitPvPStats;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Created by Aaron.
 * The cache stores online player statistics locally so methods don't need to call the DB for online players
 */
public class StatsCache {
    @Getter
    private Map<UUID, Stats> cache;

    public StatsCache(){
        cache = new HashMap<>();
    }

    public void insertIntoCache(Player p){
        Stats stats = new Stats(p);
        cache.put(p.getUniqueId(), stats);
        stats.getFromDatabase();
    }

    public void removeFromCache(Player p){
        cache.get(p.getUniqueId()).updateDatabase();
        cache.remove(p.getUniqueId());
    }

    public void getStatsForPlayer(UUID uuid, Consumer<Stats> consumer){
        if(Bukkit.getServer().getPlayer(uuid) != null){
            consumer.accept(cache.get(uuid));
        }else{
             KitPvPStats.getI().getDbManager().retrieveStats(uuid,consumer);
        }
    }

    public void getStatsForPlayer(String name, Consumer<Stats> consumer){
        Player p = Bukkit.getServer().getPlayer(name);
        if(p != null){
            consumer.accept(cache.get(p.getUniqueId()));
        }else{
            KitPvPStats.getI().getDbManager().retrieveStats(name, consumer);
        }
    }
}
