package edu.pitt.cs.admt.cytoscape.annotations.db;

import com.google.common.base.Preconditions;
import edu.pitt.cs.admt.cytoscape.annotations.db.entity.*;
import org.hsqldb.jdbc.JDBCConnection;
import org.hsqldb.jdbc.JDBCDriver;

import java.io.*;
import java.sql.*;
import java.util.Collection;
import java.util.Properties;

/**
 * @author Nikos R. Katsipoulakis
 */
public class StorageDelegate {
  
  private JDBCConnection connection = null;

  public StorageDelegate() {

  }

  public void init(String dbName) throws SQLException {
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
    connection = (JDBCConnection) driver.getConnection("jdbc:hsqldb:mem:" + dbName +
        "_ccd_annot_db", properties);
    connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
    connection.commit();
    /**
     * Initialize schema Transaction
     */
    dropDatabase();
    createDatabase();
  }

  public void close() {
    try {
      connection.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }
  
  private void createDatabase() throws SQLException {
    connection.setAutoCommit(false);
    PreparedStatement statement = connection.prepareStatement(AnnotationSchema.CREATE_NODE_TABLE);
    statement.execute();
    statement = connection.prepareStatement(AnnotationSchema.CREATE_EDGE_TABLE);
    statement.execute();
    statement = connection.prepareStatement(AnnotationSchema.CREATE_ANNOT_TABLE);
    statement.execute();
    statement = connection.prepareStatement(AnnotationSchema.CREATE_ANNOT_TO_NODE_TABLE);
    statement.execute();
    statement = connection.prepareStatement(AnnotationSchema.CREATE_ANNOT_TO_EDGE_TABLE);
    statement.execute();
    statement = connection.prepareStatement(AnnotationSchema.CREATE_ANNOT_EXT_ATTR_TABLE);
    statement.execute();
    statement = connection.prepareStatement(AnnotationSchema.ALTER_ANNOT_TO_NODE_TABLE);
    statement.execute();
    statement = connection.prepareStatement(AnnotationSchema.ALTER_ANNOT_TO_EDGE_TABLE);
    statement.execute();
    connection.commit();
  }
  
  private void dropDatabase() throws SQLException {
    connection.setAutoCommit(false);
    PreparedStatement statement = connection.prepareStatement(AnnotationSchema.DROP_NODE_TABLE);
    statement.execute();
    statement = connection.prepareStatement(AnnotationSchema.DROP_EDGE_TABLE);
    statement.execute();
    statement = connection.prepareStatement(AnnotationSchema.DROP_ANNOT_TABLE);
    statement.execute();
    statement = connection.prepareStatement(AnnotationSchema.DROP_ANNOT_TO_NODE_TABLE);
    statement.execute();
    statement = connection.prepareStatement(AnnotationSchema.DROP_ANNOT_TO_EDGE_TABLE);
    statement.execute();
    statement = connection.prepareStatement(AnnotationSchema.DROP_EXT_ATTR_TABLE);
    statement.execute();
    connection.commit();
  }

  void insertNewNode(int nodeId) throws SQLException {
    Preconditions.checkArgument(nodeId >= 0);
    connection.setAutoCommit(false);
    PreparedStatement statement = connection.prepareStatement(AnnotationSchema.INSERT_NODE);
    statement.setInt(1, nodeId);
    statement.execute();
    connection.commit();
  }
  
  void insertNodes(Collection<Node> nodes) throws SQLException {
    Preconditions.checkArgument(nodes != null);
    connection.setAutoCommit(false);
    PreparedStatement statement = connection.prepareStatement(AnnotationSchema.INSERT_NODE);
    for (Node n : nodes) {
      statement.setInt(1, n.getSuid());
      statement.addBatch();
    }
    statement.executeBatch();
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
  
  void insertEdges(Collection<Edge> edges) throws SQLException {
    Preconditions.checkArgument(edges != null);
    connection.setAutoCommit(false);
    PreparedStatement statement = connection.prepareStatement(AnnotationSchema.INSERT_EDGE);
    for (Edge e : edges) {
      statement.setInt(1, e.getSuid());
      statement.setInt(2, e.getSource());
      statement.setInt(3, e.getDestination());
      statement.addBatch();
    }
    statement.executeBatch();
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
  
  void insertAnnotations(Collection<Annotation> annotations) throws SQLException {
    Preconditions.checkArgument(annotations != null);
    connection.setAutoCommit(false);
    PreparedStatement statement = connection.prepareStatement(AnnotationSchema.INSERT_ANNOT);
    for (Annotation a : annotations) {
      statement.setInt(1, a.getId());
      statement.setString(2, a.getDescription());
      statement.addBatch();
    }
    statement.executeBatch();
    connection.commit();
  }

  void attachAnnotationToNode(int annotationId, int nodeId, Integer extendedAttributeId,
                              Object value) throws SQLException, IOException {
    Preconditions.checkArgument(annotationId >= 0 && nodeId >= 0);
    if (extendedAttributeId != null)
      Preconditions.checkArgument(extendedAttributeId >= 0);
    if (value != null)
      Preconditions.checkArgument(value instanceof Character || value instanceof Integer ||
          value instanceof Float || value instanceof Boolean || value instanceof String);
    connection.setAutoCommit(false);
    PreparedStatement statement = connection.prepareStatement(AnnotationSchema
        .INSERT_ANNOT_TO_NODE);
    statement.setInt(1, annotationId);
    statement.setInt(2, nodeId);
    if (extendedAttributeId != null)
      statement.setInt(3, extendedAttributeId);
    else
      statement.setNull(3, Types.INTEGER);
    if (value != null)
      statement.setBytes(4, convertToBinary(value));
    else
      statement.setNull(4, Types.LONGVARBINARY);
    statement.execute();
    connection.commit();
  }

  void attachAnnotationToEdge(int annotationId, int edgeId, Integer extendedAttributeId,
                              Object value) throws SQLException, IOException {
    Preconditions.checkArgument(annotationId >= 0 && edgeId >= 0);
    if (extendedAttributeId != null)
      Preconditions.checkArgument(extendedAttributeId >= 0);
    if (value != null)
      Preconditions.checkArgument(value instanceof Character || value instanceof Integer ||
          value instanceof Float || value instanceof Boolean || value instanceof String);
    connection.setAutoCommit(false);
    PreparedStatement statement = connection.prepareStatement(AnnotationSchema
        .INSERT_ANNOT_TO_EDGE);
    statement.setInt(1, annotationId);
    statement.setInt(2, edgeId);
    if (extendedAttributeId != null)
      statement.setInt(3, extendedAttributeId);
    else
      statement.setNull(3, Types.INTEGER);
    if (value != null)
      statement.setBytes(4, convertToBinary(value));
    else
      statement.setNull(4, Types.LONGVARBINARY);
    statement.execute();
    connection.commit();
  }

  void insertAnnotationExtendedAttribute(int extendedAttributeId, String name,
                                         ExtendedAttributeType type)
      throws IllegalArgumentException, SQLException {
    Preconditions.checkArgument(extendedAttributeId >= 0);
    Preconditions.checkArgument(name != null && name.length() < 32);
    Preconditions.checkArgument(type != null);
    connection.setAutoCommit(false);
    PreparedStatement statement = connection.prepareStatement(AnnotationSchema
        .INSERT_ANNOT_EXT_ATTR);
    statement.setInt(1, extendedAttributeId);
    statement.setString(2, name);
    statement.setString(3, type.toString());
    statement.execute();
    connection.commit();
  }
  
  void insertAnnotationExtendedAttributes(Collection<ExtendedAttribute> attributes)
      throws SQLException {
    Preconditions.checkArgument(attributes != null);
    connection.setAutoCommit(false);
    PreparedStatement statement = connection.prepareStatement(AnnotationSchema
        .INSERT_ANNOT_EXT_ATTR);
    for (ExtendedAttribute a : attributes) {
      statement.setInt(1, a.getId());
      statement.setString(2, a.getName());
      statement.setString(3, a.getType().toString());
      statement.addBatch();
    }
    statement.executeBatch();
    connection.commit();
  }
  
  private static Object convertToObject(byte[] binaryObject)
      throws IOException, ClassNotFoundException {
    try (ByteArrayInputStream byteStream = new ByteArrayInputStream(binaryObject)) {
      try (ObjectInputStream objectStream = new ObjectInputStream(byteStream)) {
        return objectStream.readObject();
      }
    }
  }
  
  private static byte[] convertToBinary(Object value) throws IOException {
    try (ByteArrayOutputStream binaryStream = new ByteArrayOutputStream()) {
      try (ObjectOutputStream outStream = new ObjectOutputStream(binaryStream)) {
        outStream.writeObject(value);
        return binaryStream.toByteArray();
      }
    }
  }
}
