package edu.pitt.cs.admt.cytoscape.annotations.db;

import com.google.common.base.Preconditions;
import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import edu.pitt.cs.admt.cytoscape.annotations.db.entity.*;
import org.hsqldb.jdbc.JDBCConnection;
import org.hsqldb.jdbc.JDBCDriver;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Nikos R. Katsipoulakis
 */
public class StorageDelegate {
  
  private JDBCConnection connection = null;

  private final String id;

  public StorageDelegate() {
    id = UUID.randomUUID().toString();
  }

  public void init() throws SQLException {
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
    connection = (JDBCConnection) driver.getConnection("jdbc:hsqldb:mem:" + id +
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
    if (connection != null) {
      try {
        connection.close();
        connection = null;
      } catch (SQLException e) {
        e.printStackTrace();
      }
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
    connection.commit();
  }

  void insertNode(int nodeId) throws SQLException {
    if (nodeId < 0) throw new IllegalArgumentException("negative value provided: " + nodeId);
    connection.setAutoCommit(false);
    PreparedStatement statement = connection.prepareStatement(AnnotationSchema.INSERT_NODE);
    statement.setInt(1, nodeId);
    statement.execute();
    connection.commit();
  }
  
  void insertNodes(@NotNull Collection<Node> nodes) throws SQLException {
    Preconditions.checkArgument(nodes != null);
    int s = nodes.stream().filter(n -> n.getSuid() < 0).collect(Collectors.toList()).size();
    if (s > 0) throw new IllegalArgumentException("collection with nodes that have negative id " +
        "given.");
    connection.setAutoCommit(false);
    PreparedStatement statement = connection.prepareStatement(AnnotationSchema.INSERT_NODE);
    for (Node n : nodes) {
      statement.setInt(1, n.getSuid());
      statement.addBatch();
    }
    statement.executeBatch();
    connection.commit();
  }

  Collection<Node> getNodes() throws SQLException {
    PreparedStatement statement = connection.prepareStatement(AnnotationSchema.SELECT_ALL_NODES);
    Collection<Node> nodes = new ArrayList<>();
    ResultSet resultSet = statement.executeQuery();
    while (resultSet.next())
      nodes.add(new Node(resultSet.getInt(1)));
    resultSet.close();
    statement.close();
    return nodes;
  }

  void insertEdge(int edgeId, int source, int destination) throws SQLException {
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

  Collection<Edge> getEdges() throws SQLException {
    PreparedStatement statement = connection.prepareStatement(AnnotationSchema.SELECT_ALL_EDGES);
    Collection<Edge> edges = new ArrayList<>();
    ResultSet resultSet = statement.executeQuery();
    while (resultSet.next())
      edges.add(new Edge(resultSet.getInt(1), resultSet.getInt(2), resultSet.getInt(3)));
    resultSet.close();
    statement.close();
    return edges;
  }

  void insertAnnotation(@NotNull UUID annotationId, @NotNull String name,
                        @NotNull AnnotationValueType type, String description)
      throws IllegalArgumentException, SQLException {
    if (annotationId == null) throw new IllegalArgumentException("null annotationId provided");
    if (name.equals("") || name.length() == 0)
      throw new IllegalArgumentException("empty name given.");
    if (type == null) throw new IllegalArgumentException("null type given");
    if (description != null && description.length() > 64)
      throw new IllegalArgumentException("too long description provided");
    connection.setAutoCommit(false);
    PreparedStatement statement = connection.prepareStatement(AnnotationSchema.INSERT_ANNOT);
    statement.setObject(1, annotationId);
    statement.setString(2, name);
    statement.setString(3, type.toString());
    if (description != null && description.length() > 0)
      statement.setString(4, description);
    else
      statement.setNull(4, Types.VARCHAR);
    statement.execute();
    connection.commit();
  }
  
  void insertAnnotations(@NotNull Collection<Annotation> annotations) throws SQLException {
    connection.setAutoCommit(false);
    PreparedStatement statement = connection.prepareStatement(AnnotationSchema.INSERT_ANNOT);
    for (Annotation a : annotations) {
      statement.setObject(1, a.getId());
      statement.setString(2, a.getName());
      statement.setString(3, a.getType().toString());
      if (a.getDescription() != null && a.getDescription().length() > 0)
        statement.setString(4, a.getDescription());
      else
        statement.setNull(4, Types.VARCHAR);
      statement.addBatch();
    }
    statement.executeBatch();
    connection.commit();
  }

  void attachAnnotationToNode(@NotNull UUID annotationId, @Nullable UUID cytoscapeAnnotationId,
                              @NotNull int nodeId, Object value)
      throws SQLException, IOException {
    if (nodeId < 0) throw new IllegalArgumentException("negative node id");
    if (annotationId == null) throw new IllegalArgumentException("null annotation id");
    if (value != null)
      Preconditions.checkArgument(value instanceof Character || value instanceof Integer ||
          value instanceof Float || value instanceof Boolean || value instanceof String);
    connection.setAutoCommit(false);
    PreparedStatement statement = connection.prepareStatement(
        AnnotationSchema.INSERT_ANNOT_TO_NODE);
    statement.setObject(1, annotationId);
    if (cytoscapeAnnotationId == null)
      statement.setNull(2, Types.JAVA_OBJECT);
    else
      statement.setObject(2, cytoscapeAnnotationId);
    statement.setInt(3, nodeId);
    if (value != null)
      statement.setBytes(4, convertToBinary(value));
    else
      statement.setNull(4, Types.LONGVARBINARY);
    statement.execute();
    connection.commit();
  }

  void attachAnnotationToEdge(@NotNull UUID annotationId, @Nullable UUID cytoscapeAnnotationId,
                              @NotNull int edgeId, Object value)
      throws SQLException, IOException {
    if (edgeId < 0) throw new IllegalArgumentException("negative edge id");
    if (annotationId == null) throw new IllegalArgumentException("null annotation id");
    if (value != null)
      Preconditions.checkArgument(value instanceof Character || value instanceof Integer ||
          value instanceof Float || value instanceof Boolean || value instanceof String);
    connection.setAutoCommit(false);
    PreparedStatement statement = connection.prepareStatement(AnnotationSchema.INSERT_ANNOT_TO_EDGE);
    statement.setObject(1, annotationId);
    if (cytoscapeAnnotationId == null)
      statement.setNull(2, Types.JAVA_OBJECT);
    else
      statement.setObject(2, cytoscapeAnnotationId);
    statement.setInt(3, edgeId);
    if (value != null)
      statement.setBytes(4, convertToBinary(value));
    else
      statement.setNull(4, Types.LONGVARBINARY);
    statement.execute();
    connection.commit();
  }

  Collection<Annotation> getAllAnnotations() throws SQLException {
    Collection<Annotation> collection = new ArrayList<>();
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

  /**
   *
   * @param annotationId If null, all annotation values are collected. Otherwise, annotation
   *                     values for the corresponding id are collected.
   * @return
   * @throws SQLException
   * @throws IOException
   * @throws ClassNotFoundException
   */
  Collection<AnnotToEntity> getAnnotationValues(@Nullable final UUID annotationId) throws
      SQLException, IOException, ClassNotFoundException {
    Collection<AnnotToEntity> collection = new ArrayList<>();
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

  /**
   *
   * @param name if empty, all annotations to nodes are returned
   * @return
   * @throws SQLException
   * @throws IOException
   * @throws ClassNotFoundException
   */
  Collection<AnnotToEntity> selectNodesWithAnnotation(@NotNull String name)
      throws SQLException, IOException, ClassNotFoundException {
    PreparedStatement statement = connection.prepareStatement(
        AnnotationSchema.SELECT_ANNOT_TO_NODES_ON_NAME);
    if (name == null)
      statement.setNull(1, Types.VARCHAR);
    else
      statement.setString(1, name);
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
   *
   * @param name if empty, all annotations to edges are returned
   * @return
   * @throws SQLException
   * @throws IOException
   * @throws ClassNotFoundException
   */
  Collection<AnnotToEntity> selectEdgesWithAnnotation(@NotNull String name)
      throws SQLException, IOException, ClassNotFoundException {
    PreparedStatement statement = connection.prepareStatement(
        AnnotationSchema.SELECT_ANNOT_TO_EDGES_ON_NAME);
    if (name == null)
      statement.setNull(1, Types.VARCHAR);
    else
      statement.setString(1, name);
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

  Collection<AnnotToEntity> selectEntitiesWithAnnotationNameAndPredicateOrdered(
      @NotNull String name, Function<Object, Boolean> predicate,
      AnnotationValueType type, boolean desc, int limit)
      throws SQLException, IOException, ClassNotFoundException {
    List<AnnotToEntity> unlimited = (List<AnnotToEntity>)
        selectEntitiesWithAnnotationNameAndPredicateOrdered(name, predicate, type, desc);
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


  Collection<AnnotToEntity> selectEntitiesWithAnnotationNameAndPredicateOrdered(
      @NotNull String name, Function<Object, Boolean> predicate,
      AnnotationValueType type, boolean desc) throws SQLException, IOException, ClassNotFoundException {
    List<AnnotToEntity> collection = (List<AnnotToEntity>)
        selectEntitiesWithAnnotationNameAndPredicate(name, predicate);
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

  private void sortChar(List<AnnotToEntity> collection, boolean desc) {
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

  private void sortString(List<AnnotToEntity> collection, boolean desc) {
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

  private void sortInteger(List<AnnotToEntity> collection, boolean desc) {
    Comparator<AnnotToEntity> comparator = null;
    if (!desc) {
      comparator = new Comparator<AnnotToEntity>() {
        @Override
        public int compare(AnnotToEntity o1, AnnotToEntity o2) {
          Integer i1 = (Integer) o1.getValue();
          Integer i2 = (Integer) o2.getValue();
          if (i1 == i2)
            return 0;
          else if (i1 > i2)
            return 1;
          else
            return  -1;
        }
      };
    } else {
      comparator = new Comparator<AnnotToEntity>() {
        @Override
        public int compare(AnnotToEntity o1, AnnotToEntity o2) {
          Integer i1 = (Integer) o1.getValue();
          Integer i2 = (Integer) o2.getValue();
          if (i1 == i2)
            return 0;
          else if (i1 < i2)
            return 1;
          else
            return  -1;
        }
      };
    }
    Collections.sort(collection, comparator);
  }

  private void sortFloat(List<AnnotToEntity> collection, boolean desc) {
    Comparator<AnnotToEntity> comparator = null;
    if (!desc) {
      comparator = new Comparator<AnnotToEntity>() {
        @Override
        public int compare(AnnotToEntity o1, AnnotToEntity o2) {
          Float i1 = (Float) o1.getValue();
          Float i2 = (Float) o2.getValue();
          if (i1 == i2)
            return 0;
          else if (i1 > i2)
            return 1;
          else
            return  -1;
        }
      };
    } else {
      comparator = new Comparator<AnnotToEntity>() {
        @Override
        public int compare(AnnotToEntity o1, AnnotToEntity o2) {
          Float i1 = (Float) o1.getValue();
          Float i2 = (Float) o2.getValue();
          if (i1 == i2)
            return 0;
          else if (i1 < i2)
            return 1;
          else
            return  -1;
        }
      };
    }
    Collections.sort(collection, comparator);
  }

  Collection<AnnotToEntity> selectEntitiesWithAnnotationNameAndPredicate(
      @NotNull String name, Function<Object, Boolean> predicate)
      throws SQLException, IOException, ClassNotFoundException {
    Collection<AnnotToEntity> nodes = selectNodesWithAnnotation(name);
    Collection<AnnotToEntity> edges = selectEdgesWithAnnotation(name);
    Collection<AnnotToEntity> collection = new ArrayList<>();
    for (AnnotToEntity a : nodes) {
      if (predicate.apply(a.getValue()))
        collection.add(a);
    }
    for (AnnotToEntity a : edges) {
      if (predicate.apply(a.getValue()))
        collection.add(a);
    }
    return collection;
  }

  public String getId() {
    return id;
  }

}
