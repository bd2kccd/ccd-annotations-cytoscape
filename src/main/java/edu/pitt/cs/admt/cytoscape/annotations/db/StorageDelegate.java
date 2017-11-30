package edu.pitt.cs.admt.cytoscape.annotations.db;

import com.google.common.base.Preconditions;
import edu.pitt.cs.admt.cytoscape.annotations.db.entity.*;
import org.hsqldb.jdbc.JDBCConnection;
import org.hsqldb.jdbc.JDBCDriver;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.*;
import java.sql.*;
import java.util.*;

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

  Optional<Collection<Node>> getNodes() throws SQLException {
    PreparedStatement statement = connection.prepareStatement("SELECT * FROM " +
        AnnotationSchema.NODE_TABLE);
    Collection<Node> nodes = new ArrayList<>();
    ResultSet resultSet = statement.executeQuery();
    while (resultSet.next())
      nodes.add(new Node(resultSet.getInt(1)));
    return nodes.size() > 0 ? Optional.of(nodes) : Optional.empty();
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

  void insertAnnotation(UUID annotationId, String description) throws IllegalArgumentException,
      SQLException {
    if (annotationId == null) throw new IllegalArgumentException("null annotationId provided");
    connection.setAutoCommit(false);
    Preconditions.checkArgument(description.length() < 64);
    PreparedStatement statement = connection.prepareStatement(AnnotationSchema.INSERT_ANNOT);
    statement.setObject(1, annotationId);
    statement.setString(2, description);
    statement.execute();
    connection.commit();
  }
  
  void insertAnnotations(Collection<Annotation> annotations) throws SQLException {
    Preconditions.checkArgument(annotations != null);
    connection.setAutoCommit(false);
    PreparedStatement statement = connection.prepareStatement(AnnotationSchema.INSERT_ANNOT);
    for (Annotation a : annotations) {
      statement.setObject(1, a.getId());
      statement.setString(2, a.getDescription());
      statement.addBatch();
    }
    statement.executeBatch();
    connection.commit();
  }

  void attachAnnotationToNode(UUID annotationId, int nodeId, Integer extendedAttributeId,
                              Object value) throws SQLException, IOException {
    Preconditions.checkArgument(nodeId >= 0);
    if (extendedAttributeId != null)
      Preconditions.checkArgument(extendedAttributeId >= 0);
    if (value != null)
      Preconditions.checkArgument(value instanceof Character || value instanceof Integer ||
          value instanceof Float || value instanceof Boolean || value instanceof String);
    connection.setAutoCommit(false);
    PreparedStatement statement = connection.prepareStatement(AnnotationSchema
        .INSERT_ANNOT_TO_NODE);
    statement.setObject(1, annotationId);
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

  void attachAnnotationToEdge(UUID annotationId, int edgeId, Integer extendedAttributeId,
                              Object value) throws SQLException, IOException {
    Preconditions.checkArgument(edgeId >= 0);
    if (extendedAttributeId != null)
      Preconditions.checkArgument(extendedAttributeId >= 0);
    if (value != null)
      Preconditions.checkArgument(value instanceof Character || value instanceof Integer ||
          value instanceof Float || value instanceof Boolean || value instanceof String);
    connection.setAutoCommit(false);
    PreparedStatement statement = connection.prepareStatement(AnnotationSchema
        .INSERT_ANNOT_TO_EDGE);
    statement.setObject(1, annotationId);
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

  Optional<Collection<ExtendedAttribute>> getExtendedAttributes() throws SQLException {
    PreparedStatement statement = connection.prepareStatement("SELECT * FROM " +
        AnnotationSchema.ANNOT_EXT_ATTR_TABLE);
    ResultSet resultSet = statement.executeQuery();
    Collection<ExtendedAttribute> attributes = new ArrayList<>();
    while (resultSet.next()) {
      int id = resultSet.getInt(1);
      String name = resultSet.getString(2);
      String typeName = resultSet.getString(3);
      if (ExtendedAttributeType.CHAR.equalsName(typeName))
        attributes.add(new ExtendedAttribute(id, name, ExtendedAttributeType.CHAR));
      else if (ExtendedAttributeType.BOOLEAN.equalsName(typeName))
        attributes.add(new ExtendedAttribute(id, name, ExtendedAttributeType.BOOLEAN));
      else if (ExtendedAttributeType.FLOAT.equalsName(typeName))
        attributes.add(new ExtendedAttribute(id, name, ExtendedAttributeType.FLOAT));
      else if (ExtendedAttributeType.INT.equalsName(typeName))
        attributes.add(new ExtendedAttribute(id, name, ExtendedAttributeType.INT));
      else if (ExtendedAttributeType.STRING.equalsName(typeName))
        attributes.add(new ExtendedAttribute(id, name, ExtendedAttributeType.STRING));
    }
    return attributes.size() > 0 ? Optional.of(attributes) : Optional.empty();
  }

  Collection<ExtendedAttribute> getAllAnnotationToExtendedAttributes() throws SQLException {
    PreparedStatement statement = connection.prepareStatement(AnnotationSchema
        .SELECT_ALL_EXT_ATTRS);
    List<ExtendedAttribute> attributes = new ArrayList<>();
    ResultSet rs = statement.executeQuery();
    while (rs.next()) {
      ExtendedAttributeType type = null;
      String serialType = rs.getString(3);
      if (serialType.equals("BOOLEAN"))
        type = ExtendedAttributeType.BOOLEAN;
      else if (serialType.equals("INT"))
        type = ExtendedAttributeType.INT;
      else if (serialType.equals("FLOAT"))
        type = ExtendedAttributeType.FLOAT;
      else if (serialType.equals("CHAR"))
        type = ExtendedAttributeType.CHAR;
      else if (serialType.equals("STRING"))
        type = ExtendedAttributeType.STRING;
      ExtendedAttribute attribute = new ExtendedAttribute(rs.getInt(1), rs.getString(2), type);
    }
    return attributes;
  }
  
  Collection<AnnotToEntity> getAllExtendedAttributeValues() throws SQLException, IOException,
      ClassNotFoundException {
    List<AnnotToEntity> collection = new ArrayList<>();
    PreparedStatement statement = connection.prepareStatement(AnnotationSchema
        .SELECT_ALL_EXT_ATTRS_VALUES);
    ResultSet rs = statement.executeQuery();
    while (rs.next()) {
      UUID uuid = (UUID) rs.getObject(1);
      Object value = convertToObject(rs.getBytes(4));
      AnnotToEntity entity = new AnnotToEntity(uuid, rs.getInt(2), rs.getInt(3), value);
      collection.add(entity);
    }
    statement.close();
    return collection;
  }
  
  Collection<AnnotToEntity> getExtendedAttributeValues(final UUID annotationId) throws SQLException,
      IOException, ClassNotFoundException {
    if (annotationId == null)
      throw new IllegalArgumentException("null UUID provided.");
    List<AnnotToEntity> collection = new ArrayList<>();
    PreparedStatement statement = connection.prepareStatement(AnnotationSchema
        .SELECT_EXT_ATTR_VALUES_WITH_ANNOT_ID);
    statement.setObject(1, annotationId);
    statement.setObject(2, annotationId);
    ResultSet rs = statement.executeQuery();
    while (rs.next()) {
      UUID uuid = (UUID) rs.getObject(1);
      Object value = convertToObject(rs.getBytes(4));
      AnnotToEntity entity = new AnnotToEntity(uuid, rs.getInt(2), rs.getInt(3), value);
      collection.add(entity);
    }
    statement.close();
    return collection;
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

  Optional<Collection<Node>> selectNodesWithExtendedAttribute(String name) throws SQLException {
    String query = "SELECT DISTINCT suid FROM " + AnnotationSchema.ANNOT_TO_NODE_TABLE +
        " JOIN " + AnnotationSchema.ANNOT_EXT_ATTR_TABLE + " ON " +
        AnnotationSchema.ANNOT_TO_NODE_TABLE + ".ext_attr_id = " +
        AnnotationSchema.ANNOT_EXT_ATTR_TABLE + ".id WHERE " +
        AnnotationSchema.ANNOT_EXT_ATTR_TABLE + ".name = ?";
    if (name == null || name.length() == 0)
      return Optional.empty();
    PreparedStatement statement = connection.prepareStatement(query);
    statement.setString(1, name);
    ResultSet resultSet = statement.executeQuery();
    Collection<Node> nodes = new ArrayList<>();
    while (resultSet.next())
      nodes.add(new Node(resultSet.getInt(1)));
    return nodes.size() > 0 ? Optional.of(nodes) : Optional.empty();
  }

  Optional<Collection<Edge>> selectEdgesWithExtendedAttribute(String name) throws SQLException {
    String query = "SELECT DISTINCT e.suid, e.source, e.destination FROM " +
        AnnotationSchema.EDGE_TABLE + " as e, " +
        AnnotationSchema.ANNOT_TO_EDGE_TABLE + " as a_e, " +
        AnnotationSchema.ANNOT_EXT_ATTR_TABLE + " as a_ext_attr " +
        "WHERE e.suid = a_e.suid AND a_ext_attr.id = a_e.ext_attr_id AND a_ext_attr" +
        ".name = ?";
    if (name == null || name.length() == 0)
      return Optional.empty();
    PreparedStatement statement = connection.prepareStatement(query);
    statement.setString(1, name);
    ResultSet resultSet = statement.executeQuery();
    Collection<Edge> edges = new ArrayList<>();
    while (resultSet.next())
      edges.add(new Edge(resultSet.getInt(1), resultSet.getInt(2), resultSet.getInt(3)));
    return edges.size() > 0 ? Optional.of(edges) : Optional.empty();
  }
}
