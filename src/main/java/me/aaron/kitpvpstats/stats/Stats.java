package me.aaron.kitpvpstats.stats;

import lombok.Data;
import me.aaron.kitpvpstats.KitPvPStats;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.util.UUID;

/**
 * Created by Aaron.
 * Stats Object that holds data for each player
 */
@Data
public class Stats{
    private static DecimalFormat format = new DecimalFormat("##.00");

    private UUID uuid;
    private String name;
    private int kills,deaths;
    private double damage;

    public Stats(Player p){
        this();
        uuid = p.getUniqueId();
        name = p.getName();
    }

    public Stats(){
        kills = 0;
        deaths = 0;
        damage = 0;
    }

    public void incrementKill(int i){
        kills += i;
    }

    public void incrementDeath(int i){
        deaths +=i;
    }

    public void incrementDamage(double d){
        damage += d;
    }

    public synchronized void updateDatabase(){
        KitPvPStats.getI().getDbManager().updateStats(this);
    }

    public synchronized void getFromDatabase(){
        KitPvPStats.getI().getDbManager().retrieveStats(uuid, (stats -> {
            if(stats != null){
                this.kills = stats.getKills();
                this.deaths = stats.getDeaths();
                this.damage = stats.getDamage();
            }
        }));
    }

    //Static formatting.. could be made customizable if needed
    public void sendTo(CommandSender sender){
        sender.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + ChatColor.STRIKETHROUGH + "-------------------------");
        sender.sendMessage(ChatColor.AQUA + "Stats for " + name);
        sender.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + ChatColor.STRIKETHROUGH + "-------------------------");
        sender.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "Kills: " + ChatColor.RESET + ChatColor.AQUA + kills);
        sender.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "Deaths: " + ChatColor.RESET + ChatColor.AQUA + deaths);
        sender.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "Damage Done: " + ChatColor.RESET + ChatColor.AQUA + format.format(damage));
        sender.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + ChatColor.STRIKETHROUGH + "-------------------------");
    }
}
