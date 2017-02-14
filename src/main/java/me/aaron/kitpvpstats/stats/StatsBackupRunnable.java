package me.aaron.kitpvpstats.stats;

import me.aaron.kitpvpstats.KitPvPStats;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Created by Aaron.
 */
public class StatsBackupRunnable extends BukkitRunnable {
    @Override
    public void run(){
        for(Stats s : KitPvPStats.getI().getStatsCache().getCache().values()){
            s.updateDatabase();
        }
    }
}
