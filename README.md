# CCD Annotations for Cytoscape
This app aims to bring CCD annotations to Cytoscape.

## Requirements
- Cytoscape 3.5 or greater
- Java 1.8
- Maven 3

## Build
`mvn clean package` (add option `-DskipTests` option to skip unit tests)

## Install
1. Build the app
1. Open Cytoscape
1. Open the App Manager via **Apps -> App Manager...**
1. Click **Install from File...**
1. Select the file **ccd-annotations-cytoscape-x.jar** from the *target* directory where you built the app
1. If you have networks open, close and re-import them  
a. You may need to restart Cytoscape for some changes to take effect
