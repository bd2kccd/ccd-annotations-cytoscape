package edu.pitt.cs.admt.cytoscape.annotations.db;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

import static org.junit.Assert.*;

/**
 * @author Nikos R. Katsipoulakis
 */
public class StorageDelegateTest {

  @Test
  public void init() throws Exception {
    StorageDelegate delegate = new StorageDelegate();
    delegate.init("init_test");
    delegate.close();
  }

  @Test
  public void insertNodeTest() throws Exception {
    StorageDelegate delegate = new StorageDelegate();
    delegate.init("insert_node_test");
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
    }
    assertTrue("insertion of negative node-id allowed", thrown);

    delegate.close();
  }

  @Test
  public void insertEdgeTest() {
    StorageDelegate delegate = new StorageDelegate();
    delegate.init("insert_edge_test");
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
    StorageDelegate delegate = new StorageDelegate();
    delegate.init("insert_annotation_test");
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
      delegate.insertAnnotation(-1, "should-fail");
    } catch (SQLException e) {
      thrown = true;
    }
    assertTrue("insertion with negative annotation id", thrown);
    thrown = false;
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < 65; ++i)
      builder.append("a");
    try {
      delegate.insertAnnotation(1, builder.toString());
    } catch (IllegalArgumentException e) {
      thrown = true;
    } catch (SQLException e1) {
      e1.printStackTrace();
    }
    assertTrue("insertion with long description", thrown);
    try {
      delegate.insertAnnotation(1, "annotation test alpha");
    } catch (SQLException e) {
      assertTrue("insertion of normal annotation failed", false);
    }
    thrown = false;
    try {
      delegate.attachAnnotationToNode(1, 5);
    } catch (SQLException e) {
      thrown = true;
    }
    assertTrue("attachment of annotation to invalid node", thrown);
    try {
      delegate.attachAnnotationToNode(1, 1);
    } catch (SQLException e) {
      assertTrue("attachment of annotation to node failed", false);
    }
    thrown = false;
    try {
      delegate.attachAnnotationToEdge(1, 54);
    } catch (SQLException e) {
      thrown = true;
    }
    assertTrue("attachment of annotation to invalid edge", thrown);
    try {
      delegate.attachAnnotationToEdge(1, 1);
    } catch (SQLException e) {
      assertTrue("attachment of annotation to edge failed", false);
    }
    delegate.close();
  }

  @Test
  public void insertAnnotationExtendedAttributeTest() {
    StorageDelegate delegate = new StorageDelegate();
    delegate.init("insert_ann_ext_attr_test");
    boolean thrown = false;
    try {
      delegate.insertAnnotationExtendedAttribute(-1, "na",
          StorageDelegate.ExtendedAttributeType.BOOLEAN, "na");
    } catch (IllegalArgumentException e) {
      thrown = true;
    } catch (SQLException e1) {
      assertTrue("irregular SQL exception thrown (1)", false);
    }
    assertTrue("insertion of extended attr with negative value", thrown);
    thrown = false;
    try {
      delegate.insertAnnotationExtendedAttribute(1, null,
          StorageDelegate.ExtendedAttributeType.BOOLEAN, null);
    } catch (IllegalArgumentException e) {
      thrown = true;
    } catch (SQLException e1) {
      assertTrue("irregular SQL exception thrown (2)", false);
    }
    assertTrue("insertion of empty name", thrown);
    thrown = false;
    try {
      delegate.insertAnnotationExtendedAttribute(1, "ext_attr_1", null, null);
    } catch (IllegalArgumentException e) {
      thrown = true;
    } catch (SQLException e1) {
      assertTrue("irregular SQL exception thrown (3)", false);
    }
    assertTrue("insertion of empty type", thrown);
    try {
      delegate.insertAnnotationExtendedAttribute(1, "ext_attr_1",
          StorageDelegate.ExtendedAttributeType.BOOLEAN, "n/a");
    } catch (SQLException e) {
      assertTrue("irregular SQL exception thrown (4)", false);
    }
    delegate.close();
  }

  @Test
  public void insertAnnotationExtendedAttributeValueTest() {
    StorageDelegate delegate = new StorageDelegate();
    delegate.init("insert_ann_ext_attr_val_test");
    try {
      delegate.insertNewNode(1);
      delegate.insertNewNode(2);
      delegate.insertNewEdge(1, 1, 2);
      delegate.insertAnnotation(1, "annot_1");
      delegate.attachAnnotationToNode(1, 1);
      delegate.attachAnnotationToNode(1, 2);
      delegate.attachAnnotationToEdge(1, 1);
      delegate.insertAnnotationExtendedAttribute(1, "posterior prob",
          StorageDelegate.ExtendedAttributeType.FLOAT, "n/a");
    } catch (SQLException e) {
      assertTrue("irregular SQL exception thrown (1)", false);
    } catch (IllegalArgumentException e) {
      assertTrue("irregular exception thrown (1)", false);
    }
    boolean thrown = false;
    try {
      delegate.insertAnnotationExtendedAttributeValue(-1, -1, null);
    } catch (IllegalArgumentException e) {
      thrown = true;
    } catch (SQLException e) {
      assertTrue("irregular SQL exception thrown (2)", false);
    } catch (IOException e) {
      assertTrue("irregular IO exception thrown (1)", false);
    }
    assertTrue("ext-attribute value with negative annotation id inserted", thrown);
    thrown = false;
    try {
      delegate.insertAnnotationExtendedAttributeValue(1, -1, null);
    } catch (IllegalArgumentException e) {
      thrown = true;
    } catch (SQLException e) {
      assertTrue("irregular SQL exception thrown (3)", false);
    } catch (IOException e) {
      assertTrue("irregular IO exception thrown (2)", false);
    }
    assertTrue("ext-attribute value with negative attribute id inserted", thrown);
    Object value = new Double(0.9f);
    thrown = false;
    try {
      delegate.insertAnnotationExtendedAttributeValue(1, 1, value);
    } catch (IllegalArgumentException e) {
      thrown = true;
    } catch (SQLException e) {
      assertTrue("irregular SQL exception thrown (4)", false);
    } catch (IOException e) {
      assertTrue("irregular IO exception thrown (3)", false);
    }
    assertTrue("ext-attibute of illegal type was added", thrown);
    value = new Boolean(true);
    try {
      delegate.insertAnnotationExtendedAttributeValue(1, 1, value);
    } catch (SQLException e) {
      assertTrue("irregular SQL exception thrown (4)", false);
    } catch (IllegalArgumentException e) {
      assertTrue("irregular exception thrown (2)", false);
    } catch (IOException e) {
      assertTrue("irregular IO exception thrown (4)", false);
    }
    try {
      value = delegate.retrieveExtendedAttributeValue(1, 1);
    } catch (SQLException e) {
      assertTrue("irregular SQL exception thrown (5)", false);
    } catch (IOException e) {
      assertTrue("irregular IO exception thrown (5)", false);
    } catch (ClassNotFoundException e) {
      assertTrue("irregular ClassNotFound exception thrown (1)", false);
    }
    assertTrue("ext-attribute value returned is of wrong type", value instanceof Boolean);
    Boolean castedValue = (Boolean) value;
    assertTrue("ext-attribute value returned has wrong value", castedValue == true);
    delegate.close();
  }
  
}