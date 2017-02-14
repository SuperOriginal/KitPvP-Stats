package me.aaron.kitpvpstats;

import com.zaxxer.hikari.HikariConfig;
import lombok.Getter;
import me.aaron.kitpvpstats.db.DatabaseManager;
import me.aaron.kitpvpstats.stats.StatsBackupRunnable;
import me.aaron.kitpvpstats.stats.StatsCache;
import me.aaron.kitpvpstats.stats.StatsListener;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created by Aaron.
 * Main Class
 */
public class KitPvPStats extends JavaPlugin{

    //Lombok provides annotations that remove the need for boilerplate code such as getters/setters
    @Getter
    private static KitPvPStats i;

    @Getter
    private StatsCache statsCache;

    @Getter
    private DatabaseManager dbManager;


    @Override
    public void onEnable(){
        i = this;

        String dburl = "jdbc:mysql://sql9.freemysqlhosting.net/sql9158685?useSSL=false";

        //I'm using a Connection pool as it is more efficient than manual connection handling
        HikariConfig cfg = new HikariConfig();
        cfg.setJdbcUrl(dburl);

        //16 is what I used at my previous server network and is a comfortable number of connections for a task like this
        cfg.setMaximumPoolSize(16);

        //Making password public because this is just a test
        //Production would have safe storage for login info
        cfg.setUsername("sql9158685");
        cfg.setPassword("FT2uZ4SWK7");

        dbManager = new DatabaseManager(cfg);

        dbManager.initializeDB();

        statsCache = new StatsCache();

        getServer().getPluginManager().registerEvents(new StatsListener(), this);

        //Backup all stats every 10 min
        //Can be made configurable easily
        new StatsBackupRunnable().runTaskTimerAsynchronously(this, 12000,12000);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String lbl, String[] args){
        if(cmd.getName().equalsIgnoreCase("stats")){
            if(args.length == 0){
                if(!(sender instanceof Player)){
                    sender.sendMessage("You must specify a player!");
                }else{
                    getStatsCache().getStatsForPlayer(((Player)sender).getUniqueId(), stats ->{
                        if(stats != null) {
                            stats.sendTo(sender);
                        }else{
                            sender.sendMessage(ChatColor.RED + "Could not find your stats... Contact an admin.");
                        }
                    });
                }
            }else{
                getStatsCache().getStatsForPlayer(args[0], stats ->{
                    if(stats != null) {
                        stats.sendTo(sender);
                    }else{
                        sender.sendMessage(ChatColor.RED + "Could not find stats for " + ChatColor.GRAY + args[0] + ChatColor.RED + ".");
                    }
                });
            }
        }
        return true;
    }

}
