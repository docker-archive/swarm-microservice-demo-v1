package worker;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;
import java.sql.*;
import org.json.JSONObject;
import java.util.Map;

class Worker {
  public static String[] getRedisQueueHostnames() throws Exception {
    boolean bFoundFromRedisHost = false;
    boolean bFoundToRedisHost = false;
    int nfromRedisHost = 0, ntoRedisHost = 0;
    Map<String, String> env = System.getenv();
    for (String envName : env.keySet()) {
       if (envName.equals("FROM_REDIS_HOST")) {
	 bFoundFromRedisHost = true;
         nfromRedisHost = Integer.parseInt(env.get("FROM_REDIS_HOST"));
       }
       if (envName.equals("TO_REDIS_HOST")) {
         ntoRedisHost = Integer.parseInt(env.get("TO_REDIS_HOST"));
	 bFoundToRedisHost = true;
       }
    }
    if (!bFoundFromRedisHost) {
      throw new Exception("Abort:  no FROM_REDIS_HOST environment variable");
    }
    if (!bFoundToRedisHost) {
      throw new Exception("Abort:  no TO_REDIS_HOST environment variable");
    }
    if (nfromRedisHost > ntoRedisHost) {
      throw new Exception("Abort:  no FROM_REDIS_HOST must be <= TO_REDIS_HOST");
    }
    String[] retArr = new String[ntoRedisHost-nfromRedisHost+1];
    for (int i = nfromRedisHost; i <= ntoRedisHost; i++) {
      String redisHost = String.format("redis%02d", i);
      retArr[i-nfromRedisHost] = redisHost;
    }
    return retArr;
  }
  public static void main(String[] args) {

    try {
      String[] redisHosts = getRedisQueueHostnames();
      System.err.printf("%d redis hosts\n", redisHosts.length);
      for (int i = 0; i < redisHosts.length; i++) {
	System.err.printf("  redisHosts[%d] = '%s'\n", i, redisHosts[i]);
      }

      Jedis[] redisArr = new Jedis[redisHosts.length];
      for (int i = 0; i < redisHosts.length; i++) {
        redisArr[i] = connectToRedis(redisHosts[i]);
      }

      Connection dbConn = connectToDB("store");

      System.err.println("Watching vote queue");

      while (true) {
	for (int i = 0; i < redisArr.length; i++) {
	  Jedis redis = redisArr[i];
          String voteJSON = redis.blpop(0, "votes").get(1);
          JSONObject voteData = new JSONObject(voteJSON);
          String voterID = voteData.getString("voter_id");
          String vote = voteData.getString("vote");
	  long epochMillis = voteData.getLong("ts");
          System.err.printf("Processing vote for '%s' by '%s' from '%d':  ", vote, voterID, epochMillis);
          updateVote(dbConn, voterID, vote, epochMillis);
	}
      }
    } catch (SQLException e) {
      e.printStackTrace();
      System.exit(1);
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  static void updateVote(Connection dbConn, String voterID, String vote, long epochMillis) throws SQLException {
    Timestamp ts = new Timestamp(epochMillis);

    PreparedStatement insert = dbConn.prepareStatement(
      "INSERT INTO votes (id, vote, ts) VALUES (?, ?, ?)");
    insert.setString(1, voterID);
    insert.setString(2, vote);
    insert.setTimestamp(3, ts);

    try {
      insert.executeUpdate();
      System.err.printf("successful insert for '%s'\n", voterID);
    } catch (SQLException e) {
      PreparedStatement update = dbConn.prepareStatement(
        "UPDATE votes SET vote = ? WHERE id = ? AND ts < ?");
      update.setString(1, vote);
      update.setString(2, voterID);
      update.setTimestamp(3, ts);
      int rowsAffected = update.executeUpdate();
      System.err.printf("%d rows updated for '%s'\n", rowsAffected, voterID);
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
    String password = "pg8675309";

    try {

      Class.forName("org.postgresql.Driver");
      String url = "jdbc:postgresql://" + host + "/postgres";

      while (conn == null) {
        try {
	  //Properties props = new Properties();
          //props.setProperty("user","postgres");
          conn = DriverManager.getConnection(url, "postgres", password);
        } catch (SQLException e) {
          System.err.println("Failed to connect to db - retrying");
          sleep(1000);
        }
      }

      PreparedStatement st = conn.prepareStatement(
        "CREATE TABLE IF NOT EXISTS votes (id VARCHAR(255) NOT NULL UNIQUE, vote VARCHAR(255) NOT NULL, ts TIMESTAMP DEFAULT NOW())");
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
