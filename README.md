# CCD Annotations for Cytoscape

This app aims to bring CCD annotations to Cytoscape.

## Requirements

- Cytoscape 3.5 or greater
- Java 1.8
- Maven 3

## Build

`mvn clean package` (add option `-DskipTests` to skip unit tests)

## Install

### Cytoscape Apps Directory (Recommended)
1. Build the app
1. Locate the jar file **ccd-annotations-cytoscape-0.5.0.jar** in the `target/` directory
1. Locate your Cytscape installation directory
1. Copy the jar file to the `apps/` subdirectory of your Cytoscape installation
1. If Cytoscape is open, you must restart it for the changes to take effect
### Cytoscape App Manager
1. Build the app
1. Open Cytoscape
1. Open the App Manager via **Apps -> App Manager...**
1. Click **Install from File...**
1. Locate the build directory and select the jar file **ccd-annotations-cytoscape-0.5.0.jar** from the `target/` subdirectory
1. Restart Cytoscape for the changes to take effect

## CCD Annotations Guide

### Table of Contents

- [Project Goal](#project-goal)
- [Features](#features)
  - [CCD Annotations Panel](#ccd-annotations-panel)
  - [Creation](#creation)
  - [Search](#search)
  - [Union and Intersection](#union-and-intersection)
  - [Relayouting](#relayouting)
  - [Import and Export](#import-and-export)
- [Technical Details](#technical-details)
  - [Database](#database)
  - [Cyjs File Format](#cyjs-file-format)
  - [Cyjs Cytoscape Annotation Generation](#cyjs-cytoscape-annotation-generation)
- [Internal Overview](#internal-overview)
  - [Annotation Semantic View](#annotation-semantic-view)



### Project Goal

The goal of this plugin is to extend Cytoscape's annotating capabilities in order to connect annotations with network components and provide annotation
searching and filtering. To this end, CCD-annotation-plugin follows a dual level
view on annotations: the __Visualization__ and the __Semantic__ view.

The __Visualization__ view is responsible for rendering an annotation on a network
and is mostly handled by Cytoscape's visualization engine.
The __Semantic__ view is responsible for annotation, search, filter actions, in
agreement with Cytoscape's OSGi specifications.

The aforementioned views appear in the storage engine of the plugin and on import/export functionality of Cytoscape.

### Features

#### CCD Annotations Panel

The CCD Annotations app provides a panel in Cytoscape's left Control Panel. This panel contains most of the functionality of the app, including creation, searching, and union/intersection.

#### Creation

Creating an annotation is done in the *Create* tab of the CCD Annotations panel. Users can select from a list of pre-existing annotations in which they provide the value or create a new annotation. Once an annotation is chosen and a value is provided, select a set of nodes and/or edges from the network and click the *Create* button. At least one network component is required.

When a new annotation is created, it is positioned relative to the annotated graph components using their average location. Annotations can be moved, but will be re-positioned if *Automatic CCD Annotation Relayout* is enabled.

#### Search

Searching for annotations is done in the *Search* tab of the CCD Annotations panel. Users can search via annotation name and description, then choose to further filter the results list by value. Clicking on a search result will select the annotated graph components.

Additionally, the search scope can be narrowed from all of the nodes and edges of a graph to a sub-graph of selected components.

#### Union and Intersection

When searching for annotations on a subset of nodes and/or edges, users can choose whether they want the search results to return the *union* (default) of annotations across the set of select components or the *intersection*. 

#### Relayouting

Newly created annotations are placed relative to the components which they are annotating. When a network component is moved, associated annotations are re-layouted to maintain a relative position to the component. This is called *Automatic Annotation Relayouting*. This can be enabled or disabled via the *Layout* menu item. When disabled, users can still choose to have their annotations relayouted by clicking *Layout -> Relayout CCD Annotations*.

#### Import and Export

The CCD Annotations app supports importing and exporting of annotations using Cytoscape's built-in Cytoscape JSON (cyjs) file format. When a user exports a network using this file format, annotations will be included. Another user can then import this network from the cyjs file and view the CCD annotations. Users without this app can still view CCD annotations, but will lose out on the relative layouting and other features provided by the app. 

For more information regarding our additions to the cyjs file format, see the [Cyjs File Format](#cyjs-file-format) section of our [Technical Details](#technical-details).

### Technical Details

#### Database

The plugin makes use of [HSQLDB](http://hsqldb.org/) (version 2.4) to accommodate main-memory
storage of network components and CCD annotations. All data are stored in main-memory for
improved performance, and for each loaded Network, a different database is created. By the time a network session is stopped, HSQLDB drops the main memory database. Therefore, it is the user's
 responsibility to make changes persistent by exporting a CCD-annotated network to a .cyjs file.

**CCD-Database Schema**

The database schema consists of the following tables that represent Strong entities in the schema
 (additional details can be found in `edu.pitt.cs.admt.cytoscape.annotations.db.AnnotationSchema`):

* __Node__ (suid INTEGER)
* __Edge__ (suid INTEGER, source INTEGER, destination INTEGER)
* __Annotation__ (id UUID, name VARCHAR(32), type ENUM(bool, char, int, float, string), description
  VARCHAR(64))

Each network strong entity (i.e., Node, Edge, Annotation) has an identifying attribute: for nodes
 and edges it is the *suid* field (as defined in the cyjs input file), and for annotations is the
  *id*. For annotations' *id* a UUID data type is selected to preserve the uniqueness of
  annotations generated by multiple collaborators.

In addition, there exist two relation tables between Strong entities:

* __ANNOT_TO_NODE__ (a_id UUID, cy_a_id UUID, suid INTEGER, value LONGVARBINARY)
* __ANNOT_TO_EDGE__ (a_id UUID, cy_a_id UUID, suid INTEGER, value LONGVARBINARY)

The *a_id* field is a foreign key to the `Annotation.id` field and the `cy_a_id` is the UUID
generated by Cytoscape (used for visualization purposes). The `suid` is a foreign key to the
corresponding entity's id (either `Node` or `Edge`) and `value` holds a value for that particular
 annotation (it is optional).

#### Cyjs File Format

The CCD Annotations app adds two attributes to the cyjs file format. These additions allow the app to support annotation information and annotation-to-component associations, which is not provided by Cytoscape by default.

The **__CCD_Annotations** attribute in the **data** attribute holds a list of all CCD Annotations that have been created on the network. Each CCD Annotation has four fields:

- **uuid** - a unique identifier
- **name** - the name (or class) of the annotation
- **type** - the type accepted by this annotation when supplying a value
- **description** - a description of the annotation

This list supplies the set of annotation names available when creating a new annotation instance. Newly added annotations are appended to this list upon export of the network. Similarly, the cyjs file format include an **__Annotations** attribute that lists every Cytoscape annotation found on the network.

A **__CCD_Annotation_Set** attribute can be found for every node and edge of the network. The **elements** list holds the set of all nodes and edges. The purpose of the **__CCD_Annotation_Set** is to provide the mapping(s) between instances of CCD Annotations and their visual representation on the network – the built-in Cytoscape text annotations are used to visually show the annotation. Each item in the **__CCD_Annotation_Set** includes three fields:

- **a_id** - the CCD Annotation ID (found in **__CCD_Annotations**)
- **cy_id** - the Cytoscape annotation ID (found in **__Annotations**)
- **value** - the value this annotation instance supplies

Multiple components can share the same **a_id** and **cy_id** fields, which means that there is one visual annotation representing a CCD Annotation that is associated with both components. This occurs when creating a new annotation on two selected components. In this case, the value will also be the same.

#### Cyjs Cytoscape Annotation Generation

For applications that wish to utilize CCD Annotations by creating cyjs files, our app can automatically generate Cytoscape annotations. This is done by creating CCD Annotations, adding them to the **__CCD_Annotations** and **__CCD_Annotations_Set** attributes, and either setting the **cy_id** field to some desired UUID or leaving it *blank*. It is important to note that, if the **cy_id** field is set, then it must *not* exist in the **__Annotations** list for our app to generate a Cytoscape annotation for you. If the ID already exists, then the existing Cytoscape annotation will be used.

When the app reads the **__CCD_Annotations_Set** list(s), it will generate Cytoscape annotations in the following manner:

1. All annotations with the same **a_id** value and a *blank* **cy_id** will be mapped to the same Cytoscape annotation; i.e., one visual annotation will be created per **a_id**
2. All annotations with the same **a_id** value and the same **cy_id** value will be mapped to the same Cytoscape annotation; i.e., one visual annotation will be created per **a_id** and **cy_id** pair
3. All annotations with a unique **cy_id** value, regardless of **a_id**, will be mapped to a unique Cytoscape annotation; i.e., one visual annotation will be created per unique **cy_id**

In this way, flexibility in Cytoscape annotation generation is provided to users who wish to utilize CCD Annotations via cyjs files.

### Internal Overview

#### Annotation Semantic View

The __CCD Annotations for Cytoscape__ plugin (__ccd_annot__) is designed to support two types of
CCD annotations:
 1. __holistic__ : An annotation that refers to the whole network.
 1. __specific__ : An annotation that exists by referencing one or more network components (i.e., nodes, edges).
 __Specific__ annotations can have values associated with each one of their associated
 network components, and the value types supported are: `boolean`, `char`, `int`,
 `float`, and `string`.

Both __holistic__ and __specific__ CCD annotations are declared through an *annotation class*
that contains the following fields:
* `UUID` __id__: unique identifier
* `string` __name__: a 32 character name
* `string` __type__: a description for the value type, which can take a value of
`bool, char, int, float, string` (optional for __holistic__ annotations)
* `string` __description__

By the time an __annotation class__ is registered, multiple __annotation instances__
can be instantiated for different network components. An annotation __instance__ requires some
additional information:
* `int` __suid__: unique identifier of a network component (i.e., node or edge id)
* `UUID` __a_id__: the __id__ of the __annotation class__ it is an instance of
* `UUID` __cy_a_id__: a Cytoscape generated `UUID` for visualization purposes
* `binary` __value__: the value for this particular instance

### 