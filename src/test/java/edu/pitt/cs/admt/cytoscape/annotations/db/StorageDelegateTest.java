package edu.pitt.cs.admt.cytoscape.annotations.db;

import edu.pitt.cs.admt.cytoscape.annotations.db.entity.AnnotToEntity;
import edu.pitt.cs.admt.cytoscape.annotations.db.entity.ExtendedAttributeType;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * @author Nikos R. Katsipoulakis
 */
public class StorageDelegateTest {

  @Test
  public void initTest() {
    StorageDelegate delegate = new StorageDelegate();
    try {
      delegate.init("init_test");
    } catch (SQLException e) {
      e.printStackTrace();
      assertTrue("unexpected SQLException thrown (1)", false);
    }
    delegate.close();
  }

  @Test
  public void insertNodeTest() {
    StorageDelegate delegate = new StorageDelegate();
    try {
      delegate.init("insert_node_test");
    } catch (SQLException e) {
      assertTrue("unexpected SQLException thrown (0)", false);
    }
    try {
      delegate.insertNewNode(1);
    } catch (SQLException e) {
      e.printStackTrace();
      assertTrue("first insertion of node " + 1 + " failed.", false);
    }
    boolean thrown = false;
    try {
      delegate.insertNewNode(1);
    } catch (SQLException e) {
      thrown = true;
    }
    assertTrue("second insertion of node " + 1 + " succeeded", thrown);
    thrown = false;
    try {
      delegate.insertNewNode(-1);
    } catch (SQLException e) {
      thrown = true;
    } catch (IllegalArgumentException e) {
      thrown = true;
    }
    assertTrue("insertion of negative node-id allowed", thrown);
    delegate.close();
  }

  @Test
  public void insertEdgeTest() {
    StorageDelegate delegate = new StorageDelegate();
    try {
      delegate.init("insert_edge_test");
    } catch (SQLException e) {
      assertTrue("unexpected SQLException thrown (1)", false);
    }
    try {
      delegate.insertNewNode(1);
      delegate.insertNewNode(2);
    } catch (SQLException e) {
      e.printStackTrace();
    }
    boolean thrown = false;
    try {
      delegate.insertNewEdge(-1, 1, 2);
    } catch (SQLException e) {
      thrown = true;
    }
    assertTrue("insertion of edge with negative id allowed", thrown);
    thrown = false;
    try {
      delegate.insertNewEdge(1, 4, 5);
    } catch (SQLException e) {
      thrown = true;
    }
    assertTrue("insertion of edge to non existing nodes is allowed", thrown);
    try {
      delegate.insertNewEdge(1, 1, 2);
    } catch (SQLException e) {
      e.printStackTrace();
      assertTrue("insertion of normal edge failed", false);
    }
    delegate.close();
  }

  @Test
  public void insertAnnotationTest() {
    UUID firstUUID = UUID.randomUUID();
    StorageDelegate delegate = new StorageDelegate();
    try {
      delegate.init("insert_annotation_test");
    } catch (SQLException e) {
      assertTrue("unexpected SQLException thrown (1)", false);
    }
    try {
      delegate.insertNewNode(1);
      delegate.insertNewNode(2);
      delegate.insertNewEdge(1, 1, 2);
    } catch (SQLException e) {
      e.printStackTrace();
      assertTrue("insertion of nodes and edges failed", false);
    }
    boolean thrown = false;
    try {
      delegate.insertAnnotation(null, "should-fail");
    } catch (SQLException e) {
      thrown = true;
    } catch (IllegalArgumentException e) {
      thrown = true;
    }
    assertTrue("insertion with null id", thrown);
    thrown = false;
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < 65; ++i)
      builder.append("a");
    try {
      delegate.insertAnnotation(firstUUID, builder.toString());
    } catch (IllegalArgumentException e) {
      thrown = true;
    } catch (SQLException e1) {
      e1.printStackTrace();
    }
    assertTrue("insertion with long description", thrown);
    try {
      delegate.insertAnnotation(firstUUID, "annotation test alpha");
    } catch (SQLException e) {
      assertTrue("insertion of normal annotation failed", false);
    }
    thrown = false;
    try {
      delegate.attachAnnotationToNode(firstUUID, 5, null, null);
    } catch (SQLException e) {
      thrown = true;
    } catch (IOException e) {
      assertTrue("unexpected IOException thrown (1)", false);
    }
    assertTrue("attachment of annotation to invalid node", thrown);
    try {
      delegate.attachAnnotationToNode(firstUUID, 1, null, null);
    } catch (SQLException e) {
      e.printStackTrace();
      assertTrue("attachment of annotation to node failed", false);
    } catch (IOException e) {
      assertTrue("unexpected IOException thrown (2)", false);
    }
    thrown = false;
    try {
      delegate.attachAnnotationToEdge(firstUUID, 54, null, null);
    } catch (SQLException e) {
      thrown = true;
    } catch (IOException e) {
      assertTrue("unexpected IOException thrown (3)", false);
    }
    assertTrue("attachment of annotation to invalid edge", thrown);
    try {
      delegate.attachAnnotationToEdge(firstUUID, 1, null, null);
    } catch (SQLException e) {
      assertTrue("attachment of annotation to edge failed", false);
    } catch (IOException e) {
      assertTrue("unexpected IOException thrown (4)", false);
    }
    delegate.close();
  }

  @Test
  public void insertAnnotationExtendedAttributeTest() {
    StorageDelegate delegate = new StorageDelegate();
    UUID firstUUID = UUID.randomUUID();
    try {
      delegate.init("insert_ann_ext_attr_test");
    } catch (SQLException e) {
      assertTrue("unexpected SQLException thrown (1)", false);
    }
    boolean thrown = false;
    try {
      delegate.insertAnnotationExtendedAttribute(-1, "na",
          ExtendedAttributeType.BOOLEAN);
    } catch (IllegalArgumentException e) {
      thrown = true;
    } catch (SQLException e1) {
      assertTrue("irregular SQL exception thrown (1)", false);
    }
    assertTrue("insertion of extended attr with negative value", thrown);
    thrown = false;
    try {
      delegate.insertAnnotationExtendedAttribute(1, null,
          ExtendedAttributeType.BOOLEAN);
    } catch (IllegalArgumentException e) {
      thrown = true;
    } catch (SQLException e1) {
      assertTrue("irregular SQL exception thrown (2)", false);
    }
    assertTrue("insertion of empty name", thrown);
    thrown = false;
    try {
      delegate.insertAnnotationExtendedAttribute(1, "ext_attr_1", null);
    } catch (IllegalArgumentException e) {
      thrown = true;
    } catch (SQLException e1) {
      assertTrue("irregular SQL exception thrown (3)", false);
    }
    assertTrue("insertion of empty type", thrown);
    try {
      delegate.insertAnnotationExtendedAttribute(1, "ext_attr_1",
          ExtendedAttributeType.BOOLEAN);
    } catch (SQLException e) {
      assertTrue("irregular SQL exception thrown (4)", false);
    }
    try {
      delegate.insertAnnotation(firstUUID, "first annot");
    } catch (SQLException e) {
      assertTrue("failed to add annotation", false);
    }
    try {
      delegate.insertNewNode(1);
    } catch (SQLException e) {
      assertTrue("failed to add node", false);
    }
    try {
      delegate.attachAnnotationToNode(firstUUID, 1, 1, new Float(0.1234F));
    } catch (SQLException e) {
      e.printStackTrace();
      assertTrue("SQL exception when attaching annotation to node", false);
    } catch (IOException e) {
      e.printStackTrace();
      assertTrue("IO exception when attaching annotation to node", false);
    }
    Collection<AnnotToEntity> attributes = null;
    try {
      attributes = delegate.getExtendedAttributeValues(firstUUID);
    } catch (SQLException e) {
      e.printStackTrace();
      assertTrue("SQL exception when retrieving values to nodes", false);
    } catch (IOException e) {
      e.printStackTrace();
      assertTrue("IO exception when retrieving values to nodes", false);
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
      assertTrue("Class Not Found exception when retrieving values to nodes", false);
    }
    for (AnnotToEntity a : attributes) {
      Float f = (Float) a.getValue();
      assertTrue("unexpected value extracted", f == 0.1234F);
      break;
    }
    delegate.close();
  }
  
}