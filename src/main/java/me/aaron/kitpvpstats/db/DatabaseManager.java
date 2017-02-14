package me.aaron.kitpvpstats.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.SneakyThrows;
import me.aaron.kitpvpstats.KitPvPStats;
import me.aaron.kitpvpstats.stats.Stats;
import me.aaron.kitpvpstats.util.UUIDUtils;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Created by Aaron.
 * Contains all methods that interact directly with MYSQL
 */
public class DatabaseManager {

    private HikariDataSource db;

    public DatabaseManager(HikariConfig cfg){
        db = new HikariDataSource(cfg);
    }

    @SneakyThrows
    private Connection getDBConnection(){
        return db.getConnection();
    }


    //Create tables in DB if they do not exist
    @SneakyThrows
    public void initializeDB(){
        try(Connection connection = getDBConnection()){
            String namesSQL =
                    "CREATE TABLE IF NOT EXISTS `names` (" +
                            "  `uuid` BINARY(16) NOT NULL," +
                            "  `ign` VARCHAR(16) NOT NULL," +
                            "  PRIMARY KEY (`uuid`));";

            String statsSQL =
                    "CREATE TABLE IF NOT EXISTS `stats` (" +
                            "  `uuid` BINARY(16) NOT NULL," +
                            "  `kills` INTEGER NOT NULL DEFAULT 0," +
                            "  `deaths` INTEGER NOT NULL DEFAULT 0," +
                            "  `damage` DOUBLE PRECISION NOT NULL DEFAULT 0," +
                            "  PRIMARY KEY (`uuid`));";


            Statement st = connection.createStatement();
            st.execute(namesSQL);
            st.execute(statsSQL);

            st.close();
        }
    }

    //Wrapping all MySQL database interaction in ASync runnables to ensure the main thread isn't bogged down.

    //Update UUID and IGN reference. Values should only change if a player changed their name
    public void updateNames(UUID uuid, String name){
        new BukkitRunnable(){
            @Override
            @SneakyThrows
            public void run(){
                String query = "INSERT INTO names (`uuid`, `ign`) VALUES (?,?) ON DUPLICATE KEY UPDATE ign = VALUES(ign);";
                try(Connection conn = KitPvPStats.getI().getDbManager().getDBConnection()){
                    PreparedStatement statement = conn.prepareStatement(query);
                    statement.setBytes(1, UUIDUtils.toBytes(uuid));
                    statement.setString(2, name);

                    statement.executeUpdate();
                    statement.close();
                }
            }
        }.runTaskAsynchronously(KitPvPStats.getI());
    }

    //Update stats in the DB
    public void updateStats(Stats stats){
        new BukkitRunnable(){
            @Override
            @SneakyThrows
            public void run(){
                String query = "INSERT INTO stats (`uuid`,`kills`,`deaths`,`damage`) VALUES (?,?,?,?) ON DUPLICATE KEY " +
                        "UPDATE `kills` = VALUES(kills), deaths = VALUES(deaths), damage = VALUES(damage)";

                try(Connection conn = KitPvPStats.getI().getDbManager().getDBConnection()){
                    PreparedStatement statement = conn.prepareStatement(query);
                    statement.setBytes(1, UUIDUtils.toBytes(stats.getUuid()));
                    statement.setInt(2, stats.getKills());
                    statement.setInt(3, stats.getDeaths());
                    statement.setDouble(4, stats.getDamage());

                    statement.executeUpdate();
                    statement.close();
                }
            }
        }.runTaskAsynchronously(KitPvPStats.getI());
    }

    //For all retrieve methods I used a Consumer<Stats> instead of simply returning the Stats object.
    //Consumers allow functions to be called when the DB returns data instead of forcing a thread to wait for a response.

    //Retrieve stats based on UUID
    public void retrieveStats(UUID uuid, Consumer<Stats> statsConsumer){
        new BukkitRunnable(){
            @Override
            @SneakyThrows
            public void run(){
                Stats stats = new Stats();
                String query = "SELECT names.ign, stats.* FROM names JOIN stats ON names.uuid = stats.uuid WHERE names.uuid = ?";

                try(Connection conn = KitPvPStats.getI().getDbManager().getDBConnection()){
                    PreparedStatement statement = conn.prepareStatement(query);
                    statement.setBytes(1, UUIDUtils.toBytes(uuid));

                    ResultSet set = statement.executeQuery();
                    boolean found = false;
                    while(set.next()){
                        found = true;
                        stats.setName(set.getString(1));
                        stats.setUuid(uuid);
                        stats.setKills(set.getInt(3));
                        stats.setDeaths(set.getInt(4));
                        stats.setDamage(set.getDouble(5));
                        statsConsumer.accept(stats);
                    }

                    if(!found) statsConsumer.accept(null);
                    set.close();
                    statement.close();
                }
            }
        }.runTaskAsynchronously(KitPvPStats.getI());
    }

    //Retrieve stats based on IGN
    public void retrieveStats(String ign, Consumer<Stats> statsConsumer){
        new BukkitRunnable(){
            Stats stats = new Stats();
            @Override
            @SneakyThrows
            public void run(){

                String query = "SELECT stats.* FROM names JOIN stats ON names.uuid = stats.uuid WHERE names.ign = ?";

                try(Connection conn = KitPvPStats.getI().getDbManager().getDBConnection()){
                    PreparedStatement statement = conn.prepareStatement(query);
                    statement.setString(1, ign);
                    ResultSet set = statement.executeQuery();
                    boolean found = false;
                    while(set.next()){
                        found = true;
                        stats.setName(ign);
                        stats.setUuid(UUIDUtils.fromBytes(set.getBytes(1)));
                        stats.setKills(set.getInt(2));
                        stats.setDeaths(set.getInt(3));
                        stats.setDamage(set.getDouble(4));
                        statsConsumer.accept(stats);
                    }
                    if(!found)
                        statsConsumer.accept(null);
                    set.close();
                    statement.close();
                }
            }
        }.runTaskAsynchronously(KitPvPStats.getI());
    }
}
