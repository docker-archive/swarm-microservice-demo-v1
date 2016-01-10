package worker;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;
import java.sql.*;
import org.json.JSONObject;
import java.util.Map;

class Worker {
  // TODO:  current just reads FROM_REDIS_HOST, and returns only that.
  // Should read FROM_REDIS_HOST and TO_REDIS_HOST and return an array
  // of "redisNN" hostnames that main program can loop through sequentially
  // reading all the votes.
  public static String getRedisQueueHostname() throws Exception {
    String fromRedisHost = "";
    boolean bFoundFromRedisHost = false;
    boolean bFoundToRedisHost = false;
    int nfromRedisHost;
    Map<String, String> env = System.getenv();
    for (String envName : env.keySet()) {
       //System.out.format("%s=%s%n", envName, env.get(envName));
       if (envName.equals("FROM_REDIS_HOST")) {
	 //System.out.format("FROM_REDIS_HOST = '%s'\n", env.get("FROM_REDIS_HOST"));
	 bFoundFromRedisHost = true;
         nfromRedisHost = Integer.parseInt(env.get("FROM_REDIS_HOST"));
	 fromRedisHost = String.format("redis%02d", nfromRedisHost);
       }
       if (envName.equals("TO_REDIS_HOST")) {
	 //System.out.format("TO_REDIS_HOST = '%s'\n", env.get("TO_REDIS_HOST"));
	 bFoundToRedisHost = true;
       }
    }
    if (!bFoundFromRedisHost) {
      throw new Exception("Abort:  no FROM_REDIS_HOST environment variable");
    }
    if (!bFoundToRedisHost) {
      throw new Exception("Abort:  no TO_REDIS_HOST environment variable");
    }
    System.err.println("Warning:  Ignoring TO_REDIS_HOST for now, reading only from " + fromRedisHost);
    return fromRedisHost;
  }
  public static void main(String[] args) {

    try {
      String fromRedisHost = getRedisQueueHostname();
      Jedis redis = connectToRedis(fromRedisHost);
      //Connection dbConn = connectToDB("voteapps_db_1");

      System.err.println("Watching vote queue");

      while (true) {
        String voteJSON = redis.blpop(0, "votes").get(1);
        JSONObject voteData = new JSONObject(voteJSON);
        String voterID = voteData.getString("voter_id");
        String vote = voteData.getString("vote");

        System.err.printf("Processing vote for '%s' by '%s'\n", vote, voterID);
        //updateVote(dbConn, voterID, vote);
      }
//    } catch (SQLException e) {
//      e.printStackTrace();
//      System.exit(1);
//    }
      } catch (Exception e) {
        e.printStackTrace();
	System.exit(1);
      }
  }

  static void updateVote(Connection dbConn, String voterID, String vote) throws SQLException {
    PreparedStatement insert = dbConn.prepareStatement(
      "INSERT INTO votes (id, vote) VALUES (?, ?)");
    insert.setString(1, voterID);
    insert.setString(2, vote);

    try {
      insert.executeUpdate();
    } catch (SQLException e) {
      PreparedStatement update = dbConn.prepareStatement(
        "UPDATE votes SET vote = ? WHERE id = ?");
      update.setString(1, vote);
      update.setString(2, voterID);
      update.executeUpdate();
    }
  }

  static Jedis connectToRedis(String host) {
    Jedis conn = new Jedis(host);

    while (true) {
      try {
        conn.keys("*");
        break;
      } catch (JedisConnectionException e) {
        System.err.println("Failed to connect to redis - retrying");
        sleep(1000);
      }
    }

    System.err.println("Connected to redis");
    return conn;
  }

  static Connection connectToDB(String host) throws SQLException {
    Connection conn = null;

    try {

      Class.forName("org.postgresql.Driver");
      String url = "jdbc:postgresql://" + host + "/postgres";

      while (conn == null) {
        try {
          conn = DriverManager.getConnection(url, "postgres", "");
        } catch (SQLException e) {
          System.err.println("Failed to connect to db - retrying");
          sleep(1000);
        }
      }

      PreparedStatement st = conn.prepareStatement(
        "CREATE TABLE IF NOT EXISTS votes (id VARCHAR(255) NOT NULL UNIQUE, vote VARCHAR(255) NOT NULL)");
      st.executeUpdate();

    } catch (ClassNotFoundException e) {
      e.printStackTrace();
      System.exit(1);
    }

    return conn;
  }

  static void sleep(long duration) {
    try {
      Thread.sleep(duration);
    } catch (InterruptedException e) {
      System.exit(1);
    }
  }
}
