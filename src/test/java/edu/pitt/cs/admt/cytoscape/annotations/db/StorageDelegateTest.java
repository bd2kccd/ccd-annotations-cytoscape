package edu.pitt.cs.admt.cytoscape.annotations.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import edu.pitt.cs.admt.cytoscape.annotations.db.entity.Annotation;
import edu.pitt.cs.admt.cytoscape.annotations.db.entity.AnnotationValueType;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Nikos R. Katsipoulakis
 */

public class StorageDelegateTest {

  @Test
  public void initTest() {
    try {
      StorageDelegate.init(50L);
    } catch (SQLException e) {
      e.printStackTrace();
      assertTrue("unexpected SQLException thrown (1)", false);
    }
    StorageDelegate.close(50L);
  }

  @Test
  public void insertNodeTest() {
    try {
      StorageDelegate.init(51L);
    } catch (SQLException e) {
      assertTrue("unexpected SQLException thrown (0)", false);
    }
    try {
      StorageDelegate.insertNode(51L, 1);
    } catch (SQLException e) {
      e.printStackTrace();
      assertTrue("first insertion of node " + 1 + " failed.", false);
    }
    boolean thrown = false;
    try {
      StorageDelegate.insertNode(51L, 1);
    } catch (SQLException e) {
      thrown = true;
    }
    assertTrue("second insertion of node " + 1 + " succeeded", thrown);
    thrown = false;
    try {
      StorageDelegate.insertNode(51L, -1);
    } catch (Exception e) {
      thrown = true;
    }
    assertTrue("insertion of negative node-id allowed", thrown);
    StorageDelegate.close(51L);
  }

  @Test
  public void insertEdgeTest() {
    try {
      StorageDelegate.init(52L);
    } catch (SQLException e) {
      assertTrue("unexpected SQLException thrown (1)", false);
    }
    try {
      StorageDelegate.insertNode(52L, 1);
      StorageDelegate.insertNode(52L, 2);
    } catch (SQLException e) {
      e.printStackTrace();
    }
    boolean thrown = false;
    try {
      StorageDelegate.insertEdge(52L, -1, 1, 2);
    } catch (SQLException e) {
      thrown = true;
    }
    assertTrue("insertion of edge with negative id allowed", thrown);
    thrown = false;
    try {
      StorageDelegate.insertEdge(52L, 1, 4, 5);
    } catch (SQLException e) {
      thrown = true;
    }
    assertTrue("insertion of edge to non existing nodes is allowed", thrown);
    try {
      StorageDelegate.insertEdge(52L, 1, 1, 2);
    } catch (SQLException e) {
      e.printStackTrace();
      assertTrue("insertion of normal edge failed", false);
    }
    StorageDelegate.close(52L);
  }

  @Test
  public void insertAnnotationTest() {
    UUID firstUUID = UUID.randomUUID();
    try {
      StorageDelegate.init(53L);
    } catch (SQLException e) {
      assertTrue("unexpected SQLException thrown (1)", false);
    }
    try {
      StorageDelegate.insertNode(53L, 1);
      StorageDelegate.insertNode(53L, 2);
      StorageDelegate.insertEdge(53L, 1, 1, 2);
    } catch (SQLException e) {
      e.printStackTrace();
      assertTrue("insertion of nodes and edges failed", false);
    }
    boolean thrown = false;
    try {
      StorageDelegate.insertAnnotation(53L, null, null, AnnotationValueType.CHAR,
          "should-fail");
    } catch (Exception e) {
      thrown = true;
    }
    assertTrue("insertion with null id", thrown);
    thrown = false;
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < 65; ++i) {
      builder.append("a");
    }
    try {
      StorageDelegate.insertAnnotation(53L, firstUUID, "some-ann", AnnotationValueType.CHAR,
          builder.toString());
    } catch (IllegalArgumentException e) {
      thrown = true;
    } catch (SQLException e1) {
      e1.printStackTrace();
    }
    assertTrue("insertion with long description", thrown);
    try {
      StorageDelegate.insertAnnotation(53L, firstUUID, "some-ann", AnnotationValueType.CHAR,
          "annotation test alpha");
    } catch (SQLException e) {
      assertTrue("insertion of normal annotation failed", false);
    }
    StorageDelegate.close(53L);
  }

  @Test
  public void getAnnotationTest() {
    UUID uuid = UUID.randomUUID();
    try {
      StorageDelegate.init(54L);
    } catch (SQLException e) {
      assertTrue("unexpected SQLException thrown (1)", false);
    }
    try {
      StorageDelegate.insertNode(54L, 1);
    } catch (SQLException e) {
      e.printStackTrace();
      assertTrue("insertion of node failed", false);
    }
    try {
      StorageDelegate.insertAnnotation(54L, uuid, "annotation", AnnotationValueType.CHAR, "a");
    } catch (SQLException e) {
      e.printStackTrace();
      assertTrue("insertion of annotation failed", false);
    }
    Optional<Annotation> annotation = Optional.empty();
    try {
      annotation = StorageDelegate.getAnnotation(54L, uuid);
    } catch (SQLException e) {
      e.printStackTrace();
      assertTrue("failed to fetch annotation", false);
    }
    assertNotEquals(annotation, Optional.empty());
    assertEquals(annotation.get().getName(), "annotation");
    StorageDelegate.close(54L);
  }
}