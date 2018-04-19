package edu.pitt.cs.admt.cytoscape.annotations.db;

import org.hsqldb.jdbc.JDBCConnection;
import org.hsqldb.jdbc.JDBCDriver;

import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Nikos R. Katsipoulakis (nick.katsip@gmail.com)
 */
public enum DBConnectionFactory {
  INSTANCE;
  
  private static final ConcurrentHashMap<Long, JDBCConnection> index = new ConcurrentHashMap<>();
  
  public static JDBCConnection newConnection(final long networkSUID) {
    Properties properties = new Properties();
    JDBCDriver driver = null;
    JDBCConnection connection = null;
    try {
      driver = (JDBCDriver) Class.forName("org.hsqldb.jdbcDriver").newInstance();
    } catch (IllegalAccessException | InstantiationException e) {
      e.printStackTrace();
      return null;
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
      return null;
    }
    try {
      connection = (JDBCConnection) driver.getConnection("jdbc:hsqldb:mem:" +
          networkSUID + "_ccd_annot_db", properties);
    } catch (SQLException e) {
      e.printStackTrace();
      return null;
    }
    JDBCConnection returnValue = index.putIfAbsent(networkSUID, connection);
    if (returnValue != null)
      throw new IllegalArgumentException("already existing network id (" + networkSUID + ") used " +
          "for new JDBC connection");
    return connection;
  }
  
  public static JDBCConnection getConnection(final long networkSUID) {
    return index.get(networkSUID);
  }

  public static boolean hasConnection(final long networkSUID) {
    return index.contains(networkSUID);
  }
  
  public static void closeConnection(final long networkSUID) {
    JDBCConnection connection = index.remove(networkSUID);
    if (connection != null) {
      try {
        connection.close();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
  }
  
  public static void clear() {
    index.clear();
  }
}
