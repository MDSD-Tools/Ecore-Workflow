# Advanced Ecore Switching

## Introduction and concepts
### EMF 
[EMF](https://www.eclipse.org/modeling/emf/) is a modeling framework. It allows code generation from Ecore models.
What does switching mean? :pencil:

How do the different approaches work? :pencil:

## Performance Evaluation
[JMH](https://openjdk.java.net/projects/code-tools/jmh/) was used to measure the most important performance indicator, the throughput, ie the speed at which objects can be processed by a readily instantiated and configured switch.

### Single Package Szenario
In the single package szenario, the classic EMF switch remains fastest. However, the MSwitch is only slightly slower. The HashDynamicSwitch achieves only about half the throughput. Therefore, if a switch is used extensively, it makes sense to use the funtionally equivalent ByteCodeSwitch instead.
![](https://i.imgur.com/vkP17Wa.png)


### Multi Package Szenario
In the multi package szenario, the ComposableSwitch that EMF offers only works when the package hierarchies are disjunct. But even then, it is slower than the HashDynamicSwitch and the BytecodeSwitch that perform as fast as in the single package szenario (That both achieve a slightly smaller throughput than in the single package benchmark ist probably due to the slightly larger test sample).
![](https://i.imgur.com/KyTMpnc.png)

### Limitations of the performance analysis
This measuring approach does not take into account the time it takes to configure the switch and also neglegts memory footprint. In case of creating numerous different switch instances, DynamicSwitch might take up significant amounts of memory because of its internal cache. BytecodeDynamicSwitch will take time to initialize, because everytime a new class has to be assembled and loaded into the VM. From a theoretical point of view, MSwitch should not take long to initialize, but stores one field per ecore class in the model while the classic EMF switch instances don't have any fields.


## Build
1. Clone the repository:
   ``git clone https://github.com/.../Ecore-Workflow``
   `cd Ecore-Workflow`
2. Run maven:
   `mvn clean verify`
    (Pitfall: On windows the `JAVAHOME` variable must be set). Required dependencies will be downloaded automatically and an eclipse update site will be created in the subfolder `releng/tools.mdsd.ecoreworkflow.updatesite/target/repository`. If you want to temporarily host an update site for testing, you can start a local http server:
    ```
        cd releng/tools.mdsd.ecoreworkflow.updatesite/target/repository
        python -m http.server 8080
    ```

## Usage

The code generation is accessible as a [MWE2 component](https://www.eclipse.org/Xtext/documentation/306_mwe2.html) and can be combined with standard Ecore code generation using an adecuate MWE2 workflow that combines these two steps.
There are several possibilities to invoke such a workflow: (a) as part of a maven tycho build, (b) directly from an Eclipse instance or \(c\) using the experimental Eclipse builder that is included in Ecore-Workflow.

### (a) as part of a maven tycho build
Following steps conceptually describe how to incorporate advanced switching capability into an ecore model build with maven tycho. Instead of following the steps, you can clone the [example repo](https://github.com/christianthechristian/example_mswitch_maven_project).
1. **Create a maven project** with the usual structure used in MDSD projects:
    ````
    |- .mvn
    |    |- extensions.xml
    |- bundles
    |- releng
    |    |- pom.xml
    |    |- <projectname>.targetplatform
    |    |    |- .project
    |    |    |- tp.target
    |- pom.xml
    ````
    Especially note:
    1. In `extensions.xml` add `org.eclipse.tycho.extras.tycho-pomless` and `org.palladiosimulator.tycho-tp-refresh-maven-plugin` as extensions.
    2. In the `tp.target` target definition the repositories that will be used to satisfy elipse plugins' dependencies are specified. You have to add the repository that resulted from the build. You can use `https://updatesite.mdsd.tools/ecore-workflow/releases/0.1.0/` for the currently deployed oficial version of the ecore workflow or you can use your locally hosted update site by adding a section as follows:
        ```
        <location includeAllPlatforms="false" includeConfigurePhase="false" includeMode="planner" includeSource="true" type="InstallableUnit">
		  <repository location="http://localhost:8080/"/>
	      <unit id="tools.mdsd.ecoreworkflow.mwe2lib" version="0.1.0.202003220103"/>
		  <unit id="tools.mdsd.ecoreworkflow.switches" version="0.1.0.202003220103"/>
	    </location>
        ```
        Note that tycho does not seem to support local `file://` URLs as repository locations.
    3. Use the following parent in the POM:
       ```
       <parent>
         <groupId>tools.mdsd</groupId>
         <artifactId>eclipse-parent-updatesite</artifactId>
         <version>0.4.2</version>
       </parent>
       ```
2. **Create your eclipse modeling project** in the bundles subdirectory. You can use eclipse or create/copy the corresponding files manually. The `bundles` folder should typically look like this:
    ````
    |- ...
    |- bundles
    |   |- <projectname>.bundle
    |   |   |- META-INF
    |   |   |   |- MANIFEST.MF
    |   |   |- model
    |   |   |   |- <modelname>.genmodel
    |   |   |   |- <modelname>.ecore
    |   |   |- .classpath
    |   |   |- .project
    |   |   |- build.properties
    |   |   |- plugin.xml
    ````
    Add some classes to `<modelname>.ecore`.
3. In order to enable switch generation some **additional configuration** has to be done:
    1. Add `org.eclipse.emf.mwe2.launch`, `org.eclipse.emf.mwe2.lib`, `org.eclipse.emf.mwe2.launch`, `tools.mdsd.ecoreworkflow.mwe2lib` and `tools.mdsd.ecoreworkflow.switches` to the **dependencies** in the `Require-Bundle` section in the manifest file. They are required to launch mwe2 workflows and provide the additional code generation functionalities.
    2. In order to **configure the build workflow**, create a folder `workflow` and add files `generate.mwe2` and `clean.mwe2`. The maven parent we configured before executes them as part of the build process. Beside the usual `EcoreGenerator` component,  add an `AdditionalTemplateGenerator` step to `generate.mwe2` that will create the advenced switch classes:
        ```
        component = AdditionalTemplateGenerator {
            genModel = "platform:/resoure/<projectname>.bundle/model/<modelname>.genmodel"
            destPath = "platform:/resource/<projectname>.bundle/src/"
            packageLevelGenerator = "tools.mdsd.ecoreworkflow.switches.MSwitchClassGenerator"
        }
        ```
3. Now the tycho project can be built with maven:
    ```
    mvn clean verify
    ```
    The ecore model will be translated into Java, including the `MSwitch` class, compiled, and packaged into OSGI-compatible jar-files.
### (b) directly from an Eclipse instance with XText
1. Install a fresh copy of Eclipse (Eclipse Modeling Tools 2019-09).
2. Install the EMF-Eclipse Modeling SDK, Xtext Complete SDK, and the ByteBuddy plugin (from eclipse orbit)
3. Install both the MWE Lib and the Switches feature from the Ecore-Workflow update site (or a locally hosted variant).
4. Create an Ecore project (with the usual structure):
    ````
    - src
    - src-gen
    - META-INF
    -   |- MANIFEST.MF
    - model
        |- somemodel.mwe2
    ````
4. Add a workflow in `model/workflow.mwe2` with `EcoreGenerator` and `AdditionalTemplateGenerator`.
5. Make sure that the current target platform that eclipse uses includes the features from the Ecore-Workflow update site.
6. Add the plugins `tools.mdsd.ecoreworkflow.switches` and `tools.mdsd.ecoreworkflow.mwe2lib` to the requirements in `MANIFEST.MF`.
7. Right click -> Run as MWE Workflow.
   That will build the model into the `src-gen` folder.
### \(c\) using the experimental Eclipse builder that is included in Ecore-Workflow
1. Open this project in Eclipse (= Java 2019-09, with the modeling tools and ByteBuddy also installed).
2. Right click some eclipse Project -> Run as -> Eclipse Application
    * The target platform: must include ByteBuddy, Ecore, etc.
    * As plugins, use all workspace and application plugins.
3. In the now running eclipse application, create a project as in case (b).
4. Open `.project` with an xml-based editor and add the following element as `<buildSpec>`'s first child:
   ````
    <buildCommand>
      <name>tools.mdsd.ecoreworkflow.builder.Builder</name> 
        <arguments>
          <dictionary>
      	    <key>tools.mdsd.ecoreworkflow.builder.workflowdefinition</key>
      	    <value><projectname>/model/workflow.mwe2</value>
          </dictionary>
      </arguments>
    </buildCommand>
   ````
   Now, whenever you save changes to the model, the workflow in `model/workflow.mwe2` will be run automatically, resulting in a smooth IDE-adecuate user experience. As the MWE builder is only considered experimental, this variant is not guaranteed to work stably.


## Development

Switch generation is part of the Ecore-Workflow project.
As an eclipse tycho project, it consists of several components that each are also valid eclipse plugins.

### Project Components

These components play a part in switching:


| Plugin | affected by switching | Purpose / Changes made |
| -------- | -------- | -------- |
| tools.mdsd.ecoreworkflow.switches | added     | contains classes required for switching at runtime as well as the template needed for code generation at compile time |
| tools.mdsd.ecoreworkflow.mwe2lib | changed | added a mwe2 component for invoking template generation |
| tools.mdsd.ecoreworkflow.switches.feature | added | a feature allowing to install the tools.mdsd.ecoreworkflow.switches plugin via an update-site
| tools.mdsd.ecoreworkflow.targetplatform | changed | added byte buddy to the required plugins
| tools.mdsd.ecoreworkflow.switches.tests | added | unit tests for the switching, relies on below testmodels
| tools.mdsd.ecoreworkflow.switches.testmodel | added | ecore testmodel that must pass the build and is base for the unit tests
| tools.mdsd.ecoreworkflow.switches.testmodel2 | added | just another test model that inherits from testmodel
| tools.mdsd.ecoreworkflow.switches.testmodel3 | added | just another test model, that does not inherit from testmodel (so that its switch can be combined with the testmodel for benchmarking EMF's ComposedSwitch)
| tools.mdsd.ecoreworkflow.switches.tests.perf | added | contains benchmarks, uses JMH, warning: not a tycho project, and therefore does not use referenced tycho projects' transitive dependencies.

### Running Tests

Tests are run with `mvn clean verify`.

### Running Benchmarks

Benchmarking takes precious build time and are only run when explicitly called with:
```
    mvn clean verify -Dbenchmarking=true
```
