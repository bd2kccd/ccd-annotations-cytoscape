# CCD Annotations for Cytoscape
This app aims to bring CCD annotations to Cytoscape.

## Requirements
- Cytoscape 3.5 or greater
- Java 1.8
- Maven 3

## Build
`mvn clean package` (add option `-DskipTests` to skip unit tests)

## Install
### Cytoscape Apps Directory (Preferred)
1. Build the app
1. Locate the jar file **ccd-annotations-cytoscape-0.2.0.jar** in the `target/` directory
1. Locate your Cytscape installation directory
1. Copy the jar file to the `apps/` subdirectory of your Cytoscape installation
1. If Cytoscape is open, you must restart it for the changes to take effect
### Cytoscape App Manager
1. Build the app
1. Open Cytoscape
1. Open the App Manager via **Apps -> App Manager...**
1. Click **Install from File...**
1. Locate the build directory and select the jar file **ccd-annotations-cytoscape-0.2.0.jar** from the `target/` subdirectory
1. Restart Cytoscape for the changes to take effect
