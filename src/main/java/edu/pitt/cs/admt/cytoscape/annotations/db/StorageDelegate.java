package edu.pitt.cs.admt.cytoscape.annotations.db;

import com.google.common.base.Preconditions;
import edu.pitt.cs.admt.cytoscape.annotations.db.entity.AnnotToEntity;
import edu.pitt.cs.admt.cytoscape.annotations.db.entity.Annotation;
import edu.pitt.cs.admt.cytoscape.annotations.db.entity.AnnotationValueType;
import edu.pitt.cs.admt.cytoscape.annotations.db.entity.Edge;
import edu.pitt.cs.admt.cytoscape.annotations.db.entity.Node;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.hsqldb.jdbc.JDBCConnection;
import org.hsqldb.jdbc.JDBCDriver;

/**
 * @author Nikos R. Katsipoulakis
 */
public class StorageDelegate {

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

  public static void close(final long networkSUID) {
    DBConnectionFactory.closeConnection(networkSUID);
  }
  
  public static void init(final long networkSUID) throws SQLException {
    JDBCConnection connection = DBConnectionFactory.newConnection(networkSUID);
    dropDatabase(connection);
    createDatabase(connection);
  }
  
  private static void createDatabase(final JDBCConnection connection) throws SQLException {
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
    connection.commit();
  }

  private static void dropDatabase(final JDBCConnection connection) throws SQLException {
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
    connection.commit();
  }

  protected static void destroy(final long networkSUID) throws SQLException {
    JDBCConnection connection = DBConnectionFactory.getConnection(networkSUID);
    dropDatabase(connection);
    close(networkSUID);
  }

  static void insertNode(final long networkSUID, final int nodeId) throws SQLException {
    JDBCConnection connection = DBConnectionFactory.getConnection(networkSUID);
    if (connection == null)
      throw new IllegalArgumentException("JDBC connection with network id: " + networkSUID +
          " does not exist.");
    if (nodeId < 0) {
      throw new IllegalArgumentException("negative value provided: " + nodeId);
    }
    connection.setAutoCommit(false);
    PreparedStatement statement = connection.prepareStatement(AnnotationSchema.INSERT_NODE);
    statement.setInt(1, nodeId);
    statement.execute();
    connection.commit();
  }

  /**
   * @param nodes Not nullable
   */
  static void insertNodes(final long networkSUID, final Collection<Node> nodes)
      throws SQLException {
    JDBCConnection connection = DBConnectionFactory.getConnection(networkSUID);
    if (connection == null)
      throw new IllegalArgumentException("JDBC connection with network id: " + networkSUID +
          " does not exist.");
    if (nodes == null)
      throw new IllegalArgumentException("empty collection of nodes");
    int s = nodes.stream().filter(n -> n.getSuid() < 0).collect(Collectors.toList()).size();
    if (s > 0) {
      throw new IllegalArgumentException("collection with nodes that have negative id " +
          "given.");
    }
    connection.setAutoCommit(false);
    PreparedStatement statement = connection.prepareStatement(AnnotationSchema.INSERT_NODE);
    for (Node n : nodes) {
      statement.setInt(1, n.getSuid());
      statement.addBatch();
    }
    statement.executeBatch();
    connection.commit();
  }

  static Collection<Node> getNodes(final long networkSUID) throws SQLException {
    JDBCConnection connection = DBConnectionFactory.getConnection(networkSUID);
    if (connection == null)
      throw new IllegalArgumentException("JDBC connection with network id: " + networkSUID +
          " does not exist.");
    PreparedStatement statement = connection.prepareStatement(AnnotationSchema.SELECT_ALL_NODES);
    Collection<Node> nodes = new ArrayList<>();
    ResultSet resultSet = statement.executeQuery();
    while (resultSet.next()) {
      nodes.add(new Node(resultSet.getInt(1)));
    }
    resultSet.close();
    statement.close();
    return nodes;
  }

  static void insertEdge(final long networkSUID, final int edgeId, final int source,
                         final int destination) throws SQLException {
    JDBCConnection connection = DBConnectionFactory.getConnection(networkSUID);
    if (connection == null)
      throw new IllegalArgumentException("JDBC connection with network id: " + networkSUID +
          " does not exist.");
    connection.setAutoCommit(false);
    PreparedStatement statement = connection.prepareStatement(AnnotationSchema.INSERT_EDGE);
    statement.setInt(1, edgeId);
    statement.setInt(2, source);
    statement.setInt(3, destination);
    statement.execute();
    connection.commit();
  }

  static void insertEdges(final long networkSUID, final Collection<Edge> edges)
      throws SQLException {
    if (edges == null)
      throw new IllegalArgumentException("empty collection of edges");
    JDBCConnection connection = DBConnectionFactory.getConnection(networkSUID);
    if (connection == null)
      throw new IllegalArgumentException("JDBC connection with network id: " + networkSUID +
          " does not exist.");
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

  static Collection<Edge> getEdges(final long networkSUID) throws SQLException {
    JDBCConnection connection = DBConnectionFactory.getConnection(networkSUID);
    if (connection == null)
      throw new IllegalArgumentException("JDBC connection with network id: " + networkSUID +
          " does not exist.");
    PreparedStatement statement = connection.prepareStatement(AnnotationSchema.SELECT_ALL_EDGES);
    Collection<Edge> edges = new ArrayList<>();
    ResultSet resultSet = statement.executeQuery();
    while (resultSet.next()) {
      edges.add(new Edge(resultSet.getInt(1), resultSet.getInt(2), resultSet.getInt(3)));
    }
    resultSet.close();
    statement.close();
    return edges;
  }

  /**
   * @param annotationId Not nullable
   * @param name Not nullable
   * @param type Not nullable
   */
  public static void insertAnnotation(final long networkSUID, UUID annotationId, String name,
      AnnotationValueType type, String description)
      throws IllegalArgumentException, SQLException {
    if (annotationId == null) {
      throw new IllegalArgumentException("null annotationId provided");
    }
    if (name.equals("") || name.length() == 0) {
      throw new IllegalArgumentException("empty name given.");
    }
    if (type == null) {
      throw new IllegalArgumentException("null type given");
    }
    if (description != null && description.length() > 64) {
      throw new IllegalArgumentException("too long description provided");
    }
    JDBCConnection connection = DBConnectionFactory.getConnection(networkSUID);
    if (connection == null)
      throw new IllegalArgumentException("JDBC connection with network id: " + networkSUID +
          " does not exist.");
    connection.setAutoCommit(false);
    PreparedStatement statement = connection.prepareStatement(AnnotationSchema.INSERT_ANNOT);
    statement.setObject(1, annotationId);
    statement.setString(2, name);
    statement.setString(3, type.toString());
    if (description != null && description.length() > 0) {
      statement.setString(4, description);
    } else {
      statement.setNull(4, Types.VARCHAR);
    }
    statement.execute();
    connection.commit();
  }

//  /**
//   * @param annotationID not nullable
//   * @param cytoscapeID not nullable
//   * @param entityID int
//   * @param value not nullable
//   * @param insertQuery not nullable, query to use
//   * @throws IllegalArgumentException
//   * @throws SQLException
//   */
//  private void insertAnnotToEntity(
//      final UUID annotationID,
//      final UUID cytoscapeID,
//      final int entityID,
//      final Object value,
//      final String insertQuery) throws IllegalArgumentException, SQLException {
//    if (annotationID == null) {
//      throw new IllegalArgumentException("null annotation id provided");
//    }
//    if (cytoscapeID == null) {
//      throw new IllegalArgumentException("null cytoscape id provided");
//    }
//    if (value == null) {
//      throw new IllegalArgumentException("null value provided");
//    }
//    if (insertQuery == null) {
//      throw new IllegalArgumentException("null insert query provided");
//    }
//    connection.setAutoCommit(false);
//    PreparedStatement statement = connection.prepareStatement(insertQuery);
//    statement.setObject(1, annotationID);
//    statement.setObject(2, cytoscapeID);
//    statement.setInt(3, entityID);
//    statement.setObject(4, value);
//    statement.execute();
//    connection.commit();
//  }
//
//  /**
//   * @param annotationID not nullable
//   * @param cytoscapeID not nullable
//   * @param entityID not nullable
//   * @param value not nullable
//   * @throws IllegalArgumentException
//   * @throws SQLException
//   */
//  public void insertAnnotToNode(
//      final UUID annotationID,
//      final UUID cytoscapeID,
//      final int entityID,
//      final Object value) throws IllegalArgumentException, SQLException {
//    insertAnnotToEntity(annotationID, cytoscapeID, entityID, value, AnnotationSchema.INSERT_ANNOT_TO_NODE);
//  }
//
//  /**
//   * @param annotationID not nullable
//   * @param cytoscapeID not nullable
//   * @param entityID not nullable
//   * @param value not nullable
//   * @throws IllegalArgumentException
//   * @throws SQLException
//   */
//  public void insertAnnotToEdge(
//      final UUID annotationID,
//      final UUID cytoscapeID,
//      final int entityID,
//      final Object value) throws IllegalArgumentException, SQLException {
//    insertAnnotToEntity(annotationID, cytoscapeID, entityID, value, AnnotationSchema.INSERT_ANNOT_TO_EDGE);
//  }

  /**
   * @param annotations Not nullable
   */
  static void insertAnnotations(final long networkSUID, Collection<Annotation> annotations) throws
      SQLException {
    JDBCConnection connection = DBConnectionFactory.getConnection(networkSUID);
    if (connection == null)
      throw new IllegalArgumentException("JDBC connection with network id: " + networkSUID +
          " does not exist.");
    connection.setAutoCommit(false);
    PreparedStatement statement = connection.prepareStatement(AnnotationSchema.INSERT_ANNOT);
    for (Annotation a : annotations) {
      statement.setObject(1, a.getId());
      statement.setString(2, a.getName());
      statement.setString(3, a.getType().toString());
      if (a.getDescription() != null && a.getDescription().length() > 0) {
        statement.setString(4, a.getDescription());
      } else {
        statement.setNull(4, Types.VARCHAR);
      }
      statement.addBatch();
    }
    statement.executeBatch();
    connection.commit();
  }

  /**
   * @param annotationId Not nullable
   * @param cytoscapeAnnotationId Nullable
   * @param nodeId Not nullable
   */
  public static void attachAnnotationToNode(final long networkSUID, UUID annotationId, UUID
      cytoscapeAnnotationId,
      int nodeId, Object value)
      throws SQLException, IOException {
    if (nodeId < 0) {
      throw new IllegalArgumentException("negative node id");
    }
    if (annotationId == null) {
      throw new IllegalArgumentException("null annotation id");
    }
    if (value != null) {
      Preconditions.checkArgument(value instanceof Character || value instanceof Integer ||
          value instanceof Float || value instanceof Boolean || value instanceof String);
    }
    JDBCConnection connection = DBConnectionFactory.getConnection(networkSUID);
    if (connection == null)
      throw new IllegalArgumentException("JDBC connection with network id: " + networkSUID +
          " does not exist.");
    connection.setAutoCommit(false);
    PreparedStatement statement = connection.prepareStatement(
        AnnotationSchema.INSERT_ANNOT_TO_NODE);
    statement.setObject(1, annotationId);
    if (cytoscapeAnnotationId == null) {
      statement.setNull(2, Types.JAVA_OBJECT);
    } else {
      statement.setObject(2, cytoscapeAnnotationId);
    }
    statement.setInt(3, nodeId);
    if (value != null) {
      statement.setBytes(4, convertToBinary(value));
    } else {
      statement.setNull(4, Types.LONGVARBINARY);
    }
    statement.execute();
    connection.commit();
  }

  /**
   * @param annotationId Not nullable
   * @param cytoscapeAnnotationId Nullable
   * @param edgeId Not nullable
   */
  public static void attachAnnotationToEdge(final long networkSUID, UUID annotationId, UUID
      cytoscapeAnnotationId,
      int edgeId, Object value)
      throws SQLException, IOException {
    if (edgeId < 0) {
      throw new IllegalArgumentException("negative edge id");
    }
    if (annotationId == null) {
      throw new IllegalArgumentException("null annotation id");
    }
    if (value != null) {
      Preconditions.checkArgument(value instanceof Character || value instanceof Integer ||
          value instanceof Float || value instanceof Boolean || value instanceof String);
    }
    JDBCConnection connection = DBConnectionFactory.getConnection(networkSUID);
    if (connection == null)
      throw new IllegalArgumentException("JDBC connection with network id: " + networkSUID +
          " does not exist.");
    connection.setAutoCommit(false);
    PreparedStatement statement = connection
        .prepareStatement(AnnotationSchema.INSERT_ANNOT_TO_EDGE);
    statement.setObject(1, annotationId);
    if (cytoscapeAnnotationId == null) {
      statement.setNull(2, Types.JAVA_OBJECT);
    } else {
      statement.setObject(2, cytoscapeAnnotationId);
    }
    statement.setInt(3, edgeId);
    if (value != null) {
      statement.setBytes(4, convertToBinary(value));
    } else {
      statement.setNull(4, Types.LONGVARBINARY);
    }
    statement.execute();
    connection.commit();
  }

  public static Collection<Annotation> getAllAnnotations(final long networkSUID) throws SQLException {
    Collection<Annotation> collection = new ArrayList<>();
    JDBCConnection connection = DBConnectionFactory.getConnection(networkSUID);
    if (connection == null)
      throw new IllegalArgumentException("JDBC connection with network id: " + networkSUID +
          " does not exist.");
    PreparedStatement statement = connection.prepareStatement(AnnotationSchema
        .SELECT_ALL_ANNOTATIONS);
    ResultSet rs = statement.executeQuery();
    while (rs.next()) {
      UUID uuid = (UUID) rs.getObject(1);
      String name = rs.getString(2);
      AnnotationValueType type = AnnotationValueType.parse(rs.getString(3));
      String description = rs.getString(4);
      collection.add(new Annotation(uuid, name, type, description));
    }
    rs.close();
    statement.close();
    return collection;
  }

  public static Optional<Annotation> getAnnotation(final long networkSUID, final UUID annotationId) throws
      SQLException {
    if (annotationId == null) {
      throw new IllegalArgumentException("Annotation ID cannot be null");
    }
    JDBCConnection connection = DBConnectionFactory.getConnection(networkSUID);
    if (connection == null)
      throw new IllegalArgumentException("JDBC connection with network id: " + networkSUID +
          " does not exist.");
    Optional<Annotation> annotation = Optional.empty();
    PreparedStatement statement = connection.prepareStatement(AnnotationSchema.SELECT_ANNOTATION);
    statement.setObject(1, annotationId);
    ResultSet rs = statement.executeQuery();
    while (rs.next()) {
      UUID uuid = (UUID) rs.getObject(1);
      String name = rs.getString(2);
      AnnotationValueType type = AnnotationValueType.parse(rs.getString(3));
      String description = rs.getString(4);
      annotation = Optional.of(new Annotation(uuid, name, type, description));
    }
    rs.close();
    statement.close();
    return annotation;
  }

  public static Optional<Annotation> getAnnotationByName(final long networkSUID, final String annoName) throws SQLException {
    if (annoName == null) {
      throw new IllegalArgumentException("Annotation name cannot be null");
    }
    JDBCConnection connection = DBConnectionFactory.getConnection(networkSUID);
    if (connection == null)
      throw new IllegalArgumentException("JDBC connection with network id: " + networkSUID +
        " does not exist.");
    Optional<Annotation> annotation = Optional.empty();
    PreparedStatement statement = connection.prepareStatement(AnnotationSchema.SELECT_ANNOTATION_BY_NAME);
    statement.setObject(1, annoName);
    ResultSet rs = statement.executeQuery();
    while (rs.next()) {
      UUID uuid = (UUID) rs.getObject(1);
      String name = rs.getString(2);
      AnnotationValueType type = AnnotationValueType.parse(rs.getString(3));
      String description = rs.getString(4);
      annotation = Optional.of(new Annotation(uuid, name, type, description));
    }
    rs.close();
    statement.close();
    return annotation;
  }

  /**
   * @param annotationId If null, all annotation values are collected. Otherwise, annotation
   * values for the corresponding id are collected.
   */
  static Collection<AnnotToEntity> getAnnotationValues(final long networkSUID, final UUID
      annotationId) throws
      SQLException, IOException, ClassNotFoundException {
    Collection<AnnotToEntity> collection = new ArrayList<>();
    JDBCConnection connection = DBConnectionFactory.getConnection(networkSUID);
    if (connection == null)
      throw new IllegalArgumentException("JDBC connection with network id: " + networkSUID +
          " does not exist.");
    PreparedStatement statement;
    if (annotationId == null) {
      statement = connection.prepareStatement(
          AnnotationSchema.SELECT_ALL_ANNOT_VALUES);
    } else {
      statement = connection.prepareStatement(
          AnnotationSchema.SELECT_ANNOT_VALUES_WITH_ANNOT_ID);
      statement.setObject(1, annotationId);
      statement.setObject(2, annotationId);
    }
    ResultSet rs = statement.executeQuery();
    while (rs.next()) {
      UUID uuid = (UUID) rs.getObject(1);
      UUID cyId = (UUID) rs.getObject(2);
      Integer suid = rs.getInt(3);
      Object value = convertToObject(rs.getBytes(4));
      collection.add(new AnnotToEntity(uuid, cyId, suid, value));
    }
    rs.close();
    statement.close();
    return collection;
  }

  /**
   * @param name if empty, all annotations to nodes are returned. Not nullable
   */
  public static Collection<AnnotToEntity> selectNodesWithAnnotation(final long networkSUID, String name)
      throws SQLException, IOException, ClassNotFoundException {
    JDBCConnection connection = DBConnectionFactory.getConnection(networkSUID);
    if (connection == null)
      throw new IllegalArgumentException("JDBC connection with network id: " + networkSUID +
          " does not exist.");
    PreparedStatement statement = null;
    if (name == null) {
      statement = connection.prepareStatement(AnnotationSchema.SELECT_ANNOT_TO_NODE);
    } else {
      statement = connection.prepareStatement(
          AnnotationSchema.SELECT_ANNOT_TO_NODES_ON_NAME);
      statement.setString(1, name);
    }
    ResultSet resultSet = statement.executeQuery();
    Collection<AnnotToEntity> collection = new ArrayList<>();
    while (resultSet.next()) {
      UUID uuid = (UUID) resultSet.getObject(1);
      UUID cyId = (UUID) resultSet.getObject(2);
      Integer suid = resultSet.getInt(3);
      Object value = convertToObject(resultSet.getBytes(4));
      collection.add(new AnnotToEntity(uuid, cyId, suid, value));
    }
    resultSet.close();
    statement.close();
    return collection;
  }

  /**
   * @param name if empty, all annotations to edges are returned. Not nullable
   */
  public static Collection<AnnotToEntity> selectEdgesWithAnnotation(final long networkSUID, String name)
      throws SQLException, IOException, ClassNotFoundException {
    JDBCConnection connection = DBConnectionFactory.getConnection(networkSUID);
    if (connection == null)
      throw new IllegalArgumentException("JDBC connection with network id: " + networkSUID +
          " does not exist.");
    PreparedStatement statement = null;
    if (name == null) {
      statement = connection.prepareStatement(AnnotationSchema.SELECT_ANNOT_TO_EDGES);
    } else {
      statement = connection.prepareStatement(
          AnnotationSchema.SELECT_ANNOT_TO_EDGES_ON_NAME);
      statement.setString(1, name);
    }
    ResultSet resultSet = statement.executeQuery();
    Collection<AnnotToEntity> collection = new ArrayList<>();
    while (resultSet.next()) {
      UUID uuid = (UUID) resultSet.getObject(1);
      UUID cyId = (UUID) resultSet.getObject(2);
      Integer suid = resultSet.getInt(3);
      Object value = convertToObject(resultSet.getBytes(4));
      collection.add(new AnnotToEntity(uuid, cyId, suid, value));
    }
    resultSet.close();
    statement.close();
    return collection;
  }

  /**
   * @param name Not nullable
   */
  static Collection<AnnotToEntity> selectEntitiesWithAnnotationNameAndPredicateOrdered(
      final long networkSUID,
      String name, Function<Object, Boolean> predicate,
      AnnotationValueType type, boolean desc, int limit)
      throws SQLException, IOException, ClassNotFoundException {
    List<AnnotToEntity> unlimited = (List<AnnotToEntity>)
        selectEntitiesWithAnnotationNameAndPredicateOrdered(networkSUID, name, predicate, type,
            desc);
    if (unlimited.size() <= limit) {
      return unlimited;
    } else {
      Collection<AnnotToEntity> collection = new ArrayList<>();
      for (int i = 0; i < limit; i++) {
        collection.add(unlimited.get(i));
      }
      return unlimited;
    }
  }


  /**
   * @param name Not nullable
   */
  static Collection<AnnotToEntity> selectEntitiesWithAnnotationNameAndPredicateOrdered(
      final long networkSUID,
      String name, Function<Object, Boolean> predicate,
      AnnotationValueType type, boolean desc)
      throws SQLException, IOException, ClassNotFoundException {
    List<AnnotToEntity> collection = (List<AnnotToEntity>)
        selectEntitiesWithAnnotationNameAndPredicate(networkSUID, name, predicate);
    switch (type) {
      case INT:
        sortInteger(collection, desc);
      case FLOAT:
        sortFloat(collection, desc);
      case STRING:
        sortString(collection, desc);
      case CHAR:
        sortChar(collection, desc);
    }
    return collection;
  }

  private static void sortChar(List<AnnotToEntity> collection, boolean desc) {
    Comparator<AnnotToEntity> comparator = null;
    if (!desc) {
      comparator = new Comparator<AnnotToEntity>() {
        @Override
        public int compare(AnnotToEntity o1, AnnotToEntity o2) {
          Character c1 = (Character) o1.getValue();
          Character c2 = (Character) o2.getValue();
          return Character.compare(Character.toLowerCase(c1), Character.toLowerCase(c2));
        }
      };
    } else {
      comparator = new Comparator<AnnotToEntity>() {
        @Override
        public int compare(AnnotToEntity o1, AnnotToEntity o2) {
          Character c1 = (Character) o1.getValue();
          Character c2 = (Character) o2.getValue();
          return -1 * Character.compare(Character.toLowerCase(c1), Character.toLowerCase(c2));
        }
      };
    }
    Collections.sort(collection, comparator);
  }

  private static void sortString(List<AnnotToEntity> collection, boolean desc) {
    Comparator<AnnotToEntity> comparator = null;
    if (!desc) {
      comparator = new Comparator<AnnotToEntity>() {
        @Override
        public int compare(AnnotToEntity o1, AnnotToEntity o2) {
          String s1 = (String) o1.getValue();
          String s2 = (String) o2.getValue();
          return s1.compareTo(s2);
        }
      };
    } else {
      comparator = new Comparator<AnnotToEntity>() {
        @Override
        public int compare(AnnotToEntity o1, AnnotToEntity o2) {
          String s1 = (String) o1.getValue();
          String s2 = (String) o2.getValue();
          return -1 * s1.compareTo(s2);
        }
      };
    }
    Collections.sort(collection, comparator);
  }

  private static void sortInteger(List<AnnotToEntity> collection, boolean desc) {
    Comparator<AnnotToEntity> comparator = null;
    if (!desc) {
      comparator = new Comparator<AnnotToEntity>() {
        @Override
        public int compare(AnnotToEntity o1, AnnotToEntity o2) {
          Integer i1 = (Integer) o1.getValue();
          Integer i2 = (Integer) o2.getValue();
          if (i1 == i2) {
            return 0;
          } else if (i1 > i2) {
            return 1;
          } else {
            return -1;
          }
        }
      };
    } else {
      comparator = new Comparator<AnnotToEntity>() {
        @Override
        public int compare(AnnotToEntity o1, AnnotToEntity o2) {
          Integer i1 = (Integer) o1.getValue();
          Integer i2 = (Integer) o2.getValue();
          if (i1 == i2) {
            return 0;
          } else if (i1 < i2) {
            return 1;
          } else {
            return -1;
          }
        }
      };
    }
    Collections.sort(collection, comparator);
  }

  private static void sortFloat(List<AnnotToEntity> collection, boolean desc) {
    Comparator<AnnotToEntity> comparator = null;
    if (!desc) {
      comparator = new Comparator<AnnotToEntity>() {
        @Override
        public int compare(AnnotToEntity o1, AnnotToEntity o2) {
          Float i1 = (Float) o1.getValue();
          Float i2 = (Float) o2.getValue();
          if (i1 == i2) {
            return 0;
          } else if (i1 > i2) {
            return 1;
          } else {
            return -1;
          }
        }
      };
    } else {
      comparator = new Comparator<AnnotToEntity>() {
        @Override
        public int compare(AnnotToEntity o1, AnnotToEntity o2) {
          Float i1 = (Float) o1.getValue();
          Float i2 = (Float) o2.getValue();
          if (i1 == i2) {
            return 0;
          } else if (i1 < i2) {
            return 1;
          } else {
            return -1;
          }
        }
      };
    }
    Collections.sort(collection, comparator);
  }

  /**
   * @param name Not nullable
   */
  static Collection<AnnotToEntity> selectEntitiesWithAnnotationNameAndPredicate(
      final long networkSUID,
      String name, Function<Object, Boolean> predicate)
      throws SQLException, IOException, ClassNotFoundException {
    Collection<AnnotToEntity> nodes = selectNodesWithAnnotation(networkSUID, name);
    Collection<AnnotToEntity> edges = selectEdgesWithAnnotation(networkSUID, name);
    Collection<AnnotToEntity> collection = new ArrayList<>();
    for (AnnotToEntity a : nodes) {
      if (predicate.apply(a.getValue())) {
        collection.add(a);
      }
    }
    for (AnnotToEntity a : edges) {
      if (predicate.apply(a.getValue())) {
        collection.add(a);
      }
    }
    return collection;
  }
}
