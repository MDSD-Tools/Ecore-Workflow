# Advanced Ecore Switching
*The Ecore advanced switching project is about bringing composability, dynamic case definitions and cross-package-capabilities to EMF switches. Benchmarking was used to verify that this is possible with only a reasonable loss in throughput.*
## Introduction and concepts
Switches are an important feature for performing calculations upon EMF model instances. Let us start with a brief introduction on what EMF code generation means and how switches can be used.
### EMF 
[EMF](https://www.eclipse.org/modeling/emf/) is a modeling framework. It allows code generation from Ecore models.
Assume the following simple ecore model:

![](https://i.imgur.com/doM4ay5m.png)

EMF allows us to create a GenModel based upon it (wich also includes details about the generation process, such as included classes, package names, and the destination folder). Based upon the genmodel, java code can automatically be generated. The generated code has the following structure:

````
    interface StrassenFahrzeug {}
    interface PKW extends StrassenFahrzeug {}
    interface LKW extends StrassenFahrzeug {}
    
    class StrassenFahrzeugImpl implements StrassenFahrzeug {…}
    class PKWImpl implements PKW {…}
    class LKWImpl implements LKW {…}
    
    class TransportmittelPackage {…}
    class TransportmittelSwitch {…}
````
For each class in the model, a corresponding interface is created. Apart from that, the generated classes include implementation classes for the model, a package class for reflectively inspecting the packages class hierarchy, a factory and a switch class.


### Switching
Switches in EMF allow to call different methods based on an object's dynamic type, enabling polymorphic behaviour. They are called switches, because - similar to a switch-case-control structure - they transfer control flow to one of several case branches, depending on an input value. In contrast to a usual switch-case control structure, with EMF switches, the branch that is taken does not depend on the entire object value, but on only on its dynamic type.
A case definition and switch invocation in EMF looks like this:

```
float maxSpeed = new TransportationSwitch<Float>() {
        public Float caseLKW(LKW lkw) { 
            return 80.0f;
        }
        public Float casePKW(PKW pkw) {
            return 120.0f;
        }
        public Float defaultCase(EObject object) {
            return FLOAT.POSITIVE_INFINITY;
        }
    }.doSwitch(anEObject);
```

#### Purpose
This switching functionality is similar to pattern matching and serves a similar purpose as multimethods: It enables the creation of type-specific behaviour, but without incorporating this behaviour into the types' classes themselves. The model and its types do not need to know about the different switches that might exist.
For example, in the code example above, a type-specific speed limit was implemented without changes to the model.

#### Semantics
Sometimes it is not obvious, which case should be called when many cases match. EMFs switching semantics therefore were reverse-engineered from the original switch class's [template](https://github.com/eclipse/emf/blob/d45610f/plugins/org.eclipse.emf.codegen.ecore/templates/model/SwitchClass.javajet#L179) and from the [implementation](https://github.com/eclipse/emf/blob/d45610fdd4c22493ce69705b3c569d279deb5617/plugins/org.eclipse.emf.codegen.ecore/src/org/eclipse/emf/codegen/ecore/genmodel/impl/GenClassImpl.java#L487) of `GenClass#getSwitchGenClasses()` used in the template.
It boils down to the following rules:
* An object matches a case, if it's type is a subtype of the type used in the case definition.
* The default case matches all objects.
* If an object's type matches several of the cases, the most specific one is invoked first. If a case returns `null`, less specific cases are invoked, until one returns a non-null value (which is then returned as result of the switch invocation) or all matching cases have been tried (in which case `null` is returned as result of the switch invocation).
* A case defined on type A is more specific than a case defined on type B for an object of dynamic type X if the longest inheritance path from X to A is shorter than the longest inheritance path from X to B OR if these longest paths are of the same lenght and A is found before B in a breadth-first-search starting at X that only considers paths of that specific length. (This implies that in the multi-inheritance setting, `extends A, B` can have a different effect than `extends B,A`.)

Basically, to calculate the order in which the different cases are tried when supplying an object of dynamic type `dynamicType`, EMF uses an algorithm that is equivalent to the following pseudocode:
```
function calculateInvocationSequence(caseDefinitions, dynamicType) {
    invocations = [];
    queue = [eClass];
    while (!queue.empty()) {
        next = queue.pop();
        if (caseDefinitions[next] && queue.none(e -> next.isSupertypeOf(e)) ) {
            invocations.append(caseDefinitions[next]);
        }
        queue.appendAll(next.getSupertypes());
    }
    return invocations;
}
```

As an example, take the following complex class hierarchy:

![](https://i.imgur.com/XwbL5bT.png)

There are two base classes, A and B, from which several subclasses inherit in different ways.
Furthermore, a switch is assumed that defines cases for the classes A and B, but not for any of their subclasses.
The result is that the two cases are invoked in different orders for different subclasses:


| Dynamic Type | Order of case Invocation | Reason                                                                                                                |
|:------------ | ------------------------ | --------------------------------------------------------------------------------------------------------------------- |
| C            | caseA, caseB             | A occurs before B in the list of C's supertypes.                                                                      |
| D            | caseB, caseA             | B occurs before A in the list of D's supertypes.                                                                      |
| E            | caseB, caseA             | The longest Inheritance path from E to B is shorter (length 1) than from  E to A (length 2).                          |
| F            | caseA, caseB             | The longest Inheritance pathes from F to A and from F to B are of the same length (2) and Y inherits from A before B. |


## Aim of this project
While EMF's switches can be very useful for implementing model-type-depending behaviour outside of the model, which for example is used excessively in the [Palladio Software](https://www.palladio-simulator.com/home/), some additional functionality is wished for in some use cases.

### Composability for switches
The way that one defines cases for EMF's built-in switches is by sublclassing them: One writes a class that inherits from the generated switch class and overrides the caseXYZ-methods of the cases one wishes to define. This approach contradicts the *Composition over Subclassing* design pattern and imposes limitations to the reusability of such defined switching rules: While a defined switch can also be subclassed and extended with more special cases or modified with partially overridden behaviour, it is not possible in a non-verbose way to reuse the behaviour of two or more switches at once. Extending two switch classes would require multi-inheritance in Java. We want an option to merge an existing switch, effectively copying all case definitions from one switch to another, possibly overriding already existent definitions.

### New API
This also means that our new switching mechanisms will not use subclassing for defining cases anymore, but an easy-to-read builder API:
```
float maxSpeed = new TransportationMSwitch<Float>()
    .when((LKW lkw) -> 80.0f)
    .when((PKW pkw) -> 120.0f)
    .orElse((EObject object) -> Float.POSITIVE_INFINITY)
    .merge(someOtherSwitch)
    .doSwitch(anEObject);
```

### Consistent behaviour with EMF switches
As many already existend EMF switches will likely be converted to new switches, translating EMF case definitions into new switches' definitions must yield semantically equivalent results, event in presence of complex multi-inheritance hierarchies.
With one exception: For the sake of better error tracement, when an object is not handled by any of the defined cases and no default case is defined, new switches throw an exception instead of returning `null`, so make sure to always define a default case when porting existing switches.

### Cross-package switching
Another downside of the EMF approach is to be mitigated: Because the EMF approach relies on per-package code generation at compile time, case definition is always limited to the types of one package at a time.
While EMF does offer its `ComposedSwitch` to circumvent this issue, that switch is slow at runtime and only works with packages that have a disjunct inheritance hierarchy.
We want an approach that permits case definitions across package boundaries.

### Case definition at runtime
During the requirement engineering phase of the lab, it turned out that users might also want the ability to define switch behaviour with case types only known at runtime, allowing API calls such as:
```
new ExampleSwitch()
    .dynamicCase(someEObj.eClass(), someAction)
```

### Maintain high throughput
While the beforementioned requirements for additional functionality naturally come with a performance penalty, this performance loss must not be unbearable in comparison to the classic approach. The measure that matters, is switching throughput, the speed with which objects can be dispatched with a readily configured and loaded switch. It can be measured with benchmarks.

## Switching aproaches
Within the scope of this lab, a total of three approaches was implemented and evaluated:
EMFs code generation approach was extended to allow mergeable switches (which we call MSwitches) in the package-specific setting.
To also cover cross-package switching functionality and dynamic case definitions at runtime, we implemented two further approaches, a naive reflection-based one (the HashDynamicSwitch) and an optimized one that is functionally equal, but optimized for a higher throughput (the BytecodeDynamicSwitch).
These three approaches then were compared to EMFs classic approach and also to each other.

### Classic EMF aproach

The classic EMF approach works as follows:
1. At code generation time of a package, the package's class hierarchy is analyzed. A package-specific switch class is generated that contains a method stub for each type that can be overridden in order to define a case. It also includes a concrete `doSwitch(EObject)`-method that calls these methods in the appropriate order according to its dynamic type. Conceptually, the generated switch class for the above example looks like this:
    ```
    class TransportmittelSwitch<T> {
    
        T casePKW(PKW obj) {return null;}
        T caseLKW(LKW obj) {return null;}
        T caseStrassenFahrzeug(StrassenFahrzeug obj) {return null;}
        
        T defaultCase(EObject obj) {return null;}
        
        public T doSwitch(EObject obj) {
            if (obj.getClass().equals(PKW.class)) {
                return casePKW((PKW) obj) || caseStrassenFahrzeug((StrassenFahrzeug) obj) || defaultCase(obj);
            } else if (obj.getClass().equals(LKW.class)) {
                return caseLKW() || caseStrassenFahrzeug() || defaultCase();
            } else if (...){
                ...
            }
            ...
        }
    }
    ```
2. A switch user inherits from the generated class overriding some of the case methods.
3. The child class is instantiated at runtime and the doSwitch method is called for each object to be switched and delegates to the caseXYZ()-methods.
### MSwitch aproach
Preserving the idea of the classic approach, but enhancing it with mergeability, MSwitches also use code generation, but swap the case method stubs for pointers to functional interfaces.
This means that, as subclassing is no longer required, case definition can happen at runtime, which makes ist easy to implement trait-like merging capabilities. A generated MSwitch class conceptually looks like this:
```
   class TransportmittelMSwitch<T> {
    
        Function<PKW,T> casePKW = null;
        Function<LKW,T> caseLKW = null;
        Function<StrassenFahrzeug,T>  caseStrassenFahrzeug = null;
        
        Function<EObject,T> defaultCase = null;
        
        public T doSwitch(EObject obj) {
            if (obj.getClass().equals(PKW.class)) {
                T result = null;
                if (casePKW != null) {
                    result = casePKW.apply((PKW) obj);
                }
                if (result == null && caseStrassenFahrzeug != null) {
                    result = caseStrassenFahrzeug.apply((StrassenFahrzeug) obj);
                }
                ...
                if (result == null && defaultCase != null ){...}
                return result;
            } else if (...){
                ...
            }
            ...
        }
        
        public void merge(TransmportmittelMSwitch<T> other) {
            if (other.casePKW != null) {
                this.casePKW = other.casePKW;
            }
            if (other.caseLKW != null) {
                this.caseLKW = other.caseLKW;
            }
            ...
        }
    }
```
The overall process only changes slightly:
1. Package specific switch code is generated at compile time.
2. The class is instantiated and cases are configured by assigning the function pointers at runtime.
3. At switching time, doSwitch is called and delegates to the function pointers.
### HashDynamicSwitch aproach
In order to meet with the reqirement of cross package switching, it is unpractical to use code generation at compile time, because the packages across which to switch are be compiled individually, but affect the combined switching behaviour in a closely linked manner (if one package contains subclasses of the other).
Therefore, evaluation of the defined cases relative specifity is performed at runtime when the dynamic type of the object to be switched is present.
As this calculation is costly, the invocation sequence for each dynamic type is cached in a hashmap.
Switching logic is roughly as follows:
```
T doSwitch(EObject obj) {
    EClass dynamicType = obj.eClass();
    if (!cache.containsKey(dynamicType)) {
        cache.put(dynamicType, calculateInvocationSequence(caseDefinitions, dynamicType))
    }
    Iterable<Function<EObject, T>> invocationSequence = cache.get(dynamicType);
    T result = null;
    for (Function invokedCase : invokationSequence) {
        result ||= invokedCase.apply(obj);
    }
    return result;
}
```
The overall process now is fundamentally different:
1. The switch class is always the same and no model specific code must be generated at compile time.
2. The switch class is instantiated at runtime and cases are configured.
3. At switching time the class hierarchy is analyzed as necessary and delegation to the correct function pointer is done.
### BytecodeDynamicSwitch aproach
A problem with above approach is that the high cost of the  `calculateInvocationSequence()`-method (which has to traverse the dynamic type's inheritance hierarchy), is costly and, being executed at switching time, bottlenecks the throughput.

One wishes for the efficient `doSwitch` implementation from the package specific approaches. This wish can indeed be granted by generating the `doSwitch` method's body at runtime - using bytecode generation:
1. At runtime, case definitions are configured. By now, the involved class hierarchy is known completely.
2. Bytecode generation is used to dynamically create a switch subclass with an efficiend `doSwitch`-method, which is immediately loaded and instantiated.
3. Objects that are fed into the switch can be directly processed by the fast `doSwitch`-method that delegates to the adequate function pointers.

## Performance Evaluation
[JMH](https://openjdk.java.net/projects/code-tools/jmh/) was used to measure the most important performance indicator, the throughput, ie the speed at which objects can be processed by a readily instantiated and configured switch.

### Single Package Szenario
In the single package szenario, the classic EMF switch remains fastest. However, the MSwitch is only slightly slower. The HashDynamicSwitch achieves only about half the throughput. Therefore, if a switch is used extensively, it makes sense to use the funtionally equivalent ByteCodeSwitch instead.
![](https://i.imgur.com/vkP17Wa.png)


### Multi Package Szenario
In the multi package szenario, the ComposableSwitch that EMF offers only works when the package hierarchies are distinct. But even then, it is slower than the HashDynamicSwitch and the BytecodeSwitch that perform as fast as in the single package szenario (That both achieve a slightly smaller throughput than in the single package benchmark ist probably due to the slightly larger test sample).
![](https://i.imgur.com/KyTMpnc.png)

### Limitations of the performance analysis method
This measuring approach does not take into account the time it takes to configure the switch and also neglegts memory footprint. In case of creating numerous different switch instances, DynamicSwitch might take up significant amounts of memory because of its internal cache. BytecodeDynamicSwitch will take time to initialize, because everytime a new class has to be assembled and loaded into the VM. From a theoretical point of view, MSwitch should not take long to initialize, but stores one field per ecore class in the model while the classic EMF switch instances don't have any fields.

### Conclusion: Usage Guideline
If package-specific switching is sufficient, use the MSwitch in order to enable reuse of your switches later on, at no significant performance cost.
If cross-package switching is needed, it makesuse a HashDynamicSwitch for a small number of objects to be switched with a particular switch and us

## Build
As described above, our advanced switching approaches are a promising substitute to Ecore's builtin switches in practice. 
In order to reproduce the benchmarking results or use the new switches for your own project, these are the technical steps required to build the project:
1. Clone the repository:
   ````
       git clone https://github.com/.../Ecore-Workflow
       cd Ecore-Workflow
   ````
2. Run maven:
   ```
       mvn clean verify
   ```
    (Pitfall: On windows the `JAVAHOME` variable must be set). Required dependencies will be downloaded automatically and an eclipse update site will be created in the subfolder `releng/tools.mdsd.ecoreworkflow.updatesite/target/repository`.
3. If you want to temporarily host an update site for testing, you can start a local http server:
    ```
        cd releng/tools.mdsd.ecoreworkflow.updatesite/target/repository
        python -m http.server 8080
    ```

## Usage

The code generation is accessible as a [MWE2 component](https://www.eclipse.org/Xtext/documentation/306_mwe2.html) and can be combined with standard Ecore code generation using an adecuate MWE2 workflow that combines these two steps.
There are several possibilities to invoke such a workflow:
(a) as part of a maven tycho build,
(b) directly from an Eclipse instance or
\(c\) using the experimental Eclipse builder that is included in Ecore-Workflow.

### (a) as part of a maven tycho build
Following steps conceptually describe how to incorporate advanced switching capability into an ecore model build with maven tycho. (Instead of following the steps one by one, you can clone the [example repo](https://github.com/christianthechristian/example_mswitch_maven_project).)
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
    2. In the `tp.target` target definition the repositories that will be used to satisfy elipse plugins' dependencies are specified. You have to add the repository that resulted from the build. You can use [`https://updatesite.mdsd.tools/ecore-workflow/releases/0.1.0/`](https://updatesite.mdsd.tools/ecore-workflow/releases/0.1.0/) for the currently deployed oficial version of the ecore workflow or you can use your locally hosted update site by adding a section such as follows:
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
7. Right click the workflow -> Run as MWE Workflow.
   That will build the model into the `src-gen` folder.
### \(c\) using the experimental Eclipse builder that is included in Ecore-Workflow
1. Open this project in Eclipse (= Java IDE 2019-09, with the modeling tools and ByteBuddy also installed).
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

Details on the implementation can be found in the code which should be self-explanatory.

### Running Tests

Tests are run with `mvn clean verify`.

### Running Benchmarks

Benchmarking takes precious build time and therefore benchmarks are only enabled in a specific maven profile. You can enable this profile with this additional command line parameter:
```
    mvn clean verify -Dbenchmarking=true
```
