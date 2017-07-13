package edu.pitt.cs.admt.cytoscape.annotations.db;

import com.google.common.base.Preconditions;
import org.hsqldb.jdbc.JDBCConnection;
import org.hsqldb.jdbc.JDBCDriver;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

/**
 * @author Nikos R. Katsipoulakis
 */
class StorageDelegate {

  public enum ExtendedAttributeType {
    CHAR ("CHAR"),
    BOOLEAN ("BOOLEAN"),
    INT ("INT"),
    FLOAT ("FLOAT"),
    STRING ("STRING");

    private final String name;

    private ExtendedAttributeType(String s) {
      name = s;
    }

    public boolean equalsName(String otherName) {
      return name.equals(otherName);
    }

    public String toString() {
      return this.name;
    }
  }

  private JDBCConnection connection = null;

  public StorageDelegate() {

  }

  public void init(String dbName) {
    Properties properties = new Properties();
    JDBCDriver driver = null;
    try {
      driver = (JDBCDriver) Class.forName("org.hsqldb.jdbcDriver").newInstance();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    } catch (InstantiationException e) {
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
    try {
      connection = (JDBCConnection) driver.getConnection("jdbc:hsqldb:mem:" + dbName +
          "_ccd_annot_db", properties);
      connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
      connection.commit();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    /**
     * Initialize schema Transaction
     */
    try {
      connection.setAutoCommit(false);
      createNodeTable();
      createEdgeTable();
      createAnnotationTable();
      createAnnoToNodeTable();
      createAnnoToEdgeTable();
      createExtAttrTable();
      createExtAttrValTable();
      connection.commit();
    } catch (SQLException e) {
      try {
        connection.rollback();
        connection.close();
      } catch (SQLException e1) {
        e1.printStackTrace();
      }
      System.err.println("failed to create database");
      e.printStackTrace();
    }
  }

  public void close() {
    try {
      connection.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  private void createNodeTable() throws SQLException {
    PreparedStatement statement = connection.prepareStatement(AnnotationSchema.CREATE_NODE_TABLE);
    statement.execute();
  }

  private void createEdgeTable() throws SQLException {
    PreparedStatement statement = connection.prepareStatement(AnnotationSchema.CREATE_EDGE_TABLE);
    statement.execute();
  }

  private void createAnnotationTable() throws SQLException {
    PreparedStatement statement = connection.prepareStatement(AnnotationSchema.CREATE_ANNOT_TABLE);
    statement.execute();
  }

  private void createAnnoToNodeTable() throws SQLException {
    PreparedStatement statement = connection.prepareStatement(AnnotationSchema
        .CREATE_ANNOT_TO_NODE_TABLE);
    statement.execute();
  }

  private void createAnnoToEdgeTable() throws SQLException {
    PreparedStatement statement = connection.prepareStatement(AnnotationSchema
        .CREATE_ANNOT_TO_EDGE_TABLE);
    statement.execute();
  }

  private void createExtAttrTable() throws SQLException {
    PreparedStatement statement = connection.prepareStatement(AnnotationSchema
        .CREATE_ANNOT_EXT_ATTR_TABLE);
    statement.execute();
  }

  private void createExtAttrValTable() throws SQLException {
    PreparedStatement statement = connection.prepareStatement(AnnotationSchema
        .CREATE_ANNOT_EXT_ATTR_VAL);
    statement.execute();
  }

  void insertNewNode(int nodeId) throws SQLException {
    connection.setAutoCommit(false);
    PreparedStatement statement = connection.prepareStatement(AnnotationSchema.INSERT_NODE);
    statement.setInt(1, nodeId);
    statement.execute();
    connection.commit();
  }

  void insertNewEdge(int edgeId, int source, int destination) throws SQLException {
    connection.setAutoCommit(false);
    PreparedStatement statement = connection.prepareStatement(AnnotationSchema.INSERT_EDGE);
    statement.setInt(1, edgeId);
    statement.setInt(2, source);
    statement.setInt(3, destination);
    statement.execute();
    connection.commit();
  }

  void insertAnnotation(int annotationId, String description) throws IllegalArgumentException,
      SQLException {
    connection.setAutoCommit(false);
    Preconditions.checkArgument(description.length() < 64);
    PreparedStatement statement = connection.prepareStatement(AnnotationSchema.INSERT_ANNOT);
    statement.setInt(1, annotationId);
    statement.setString(2, description);
    statement.execute();
    connection.commit();
  }

  void attachAnnotationToNode(int annotationId, int nodeId) throws SQLException {
    connection.setAutoCommit(false);
    PreparedStatement statement = connection.prepareStatement(AnnotationSchema
        .INSERT_ANNOT_TO_NODE);
    statement.setInt(1, annotationId);
    statement.setInt(2, nodeId);
    statement.execute();
    connection.commit();
  }

  void attachAnnotationToEdge(int annotationId, int edgeId) throws SQLException {
    connection.setAutoCommit(false);
    PreparedStatement statement = connection.prepareStatement(AnnotationSchema
        .INSERT_ANNOT_TO_EDGE);
    statement.setInt(1, annotationId);
    statement.setInt(2, edgeId);
    statement.execute();
    connection.commit();
  }

  void insertAnnotationExtendedAttribute(int extendedAttributeId, String name,
                                         ExtendedAttributeType type, String description)
      throws IllegalArgumentException, SQLException {
    Preconditions.checkArgument(extendedAttributeId >= 0);
    Preconditions.checkArgument(name != null && name.length() < 32);
    Preconditions.checkArgument(type != null);
    if (description != null)
      Preconditions.checkArgument(description.length() < 64);
    connection.setAutoCommit(false);
    PreparedStatement statement = connection.prepareStatement(AnnotationSchema
        .INSERT_ANNOT_EXT_ATTR);
    statement.setInt(1, extendedAttributeId);
    statement.setString(2, name);
    statement.setString(3, type.toString());
    statement.setString(4, description);
    statement.execute();
    connection.commit();
  }

  void insertAnnotationExtendedAttributeValue(int id, int attributeId, Object value)
      throws IllegalArgumentException, SQLException, IOException {
    Preconditions.checkArgument(id >= 0);
    Preconditions.checkArgument(attributeId >= 0);
    Preconditions.checkArgument(value != null);
    Preconditions.checkArgument(value instanceof Character || value instanceof Integer ||
        value instanceof Float || value instanceof Boolean || value instanceof String);
    connection.setAutoCommit(false);
    PreparedStatement statement = connection.prepareStatement(AnnotationSchema.INSERT_EXT_ATTR_VAL);
    statement.setInt(1, id);
    statement.setInt(2, attributeId);
    try (ByteArrayOutputStream binaryStream = new ByteArrayOutputStream()) {
      try (ObjectOutputStream outStream = new ObjectOutputStream(binaryStream)) {
        outStream.writeObject(value);
        byte[] lob = binaryStream.toByteArray();
        statement.setBytes(3, lob);
      }
    }
    statement.execute();
    connection.commit();
  }

  Object retrieveExtendedAttributeValue(int id, int attributeId) throws SQLException, IOException, ClassNotFoundException {
    final String query = "SELECT value FROM " + AnnotationSchema.ANNOT_EXT_ATTR_VAL_TABLE +
        " WHERE id = ? AND attr_id = ?";
    Object value = null;
    PreparedStatement statement = connection.prepareStatement(query);
    statement.setInt(1, id);
    statement.setInt(2, attributeId);
    ResultSet resultSet = statement.executeQuery();
    while (resultSet.next()) {
      ByteArrayInputStream byteStream = new ByteArrayInputStream(resultSet.getBytes(1));
      ObjectInputStream objectStream = new ObjectInputStream(byteStream);
      value = objectStream.readObject();
    }
    resultSet.close();
    return value;
  }
}
