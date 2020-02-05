package tools.mdsd.ecoreworkflow.switches.bytecodegen;

import static net.bytebuddy.matcher.ElementMatchers.named;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.dynamic.DynamicType.Builder.MethodDefinition.ReceiverTypeDefinition;
import net.bytebuddy.dynamic.DynamicType.Unloaded;
import tools.mdsd.ecoreworkflow.switches.ApplyableSwitch;
import tools.mdsd.ecoreworkflow.switches.BreadthFirstSearch;

public class ByteCodeSwitchCompiler<T> {
  private Map<EClass, Function<EObject, T>> caseDefinitions;
  private Function<EObject, T> defaultCase;
  private Set<EPackage> explicitPackages;
  private Map<EClass, List<EClass>> invocationOrders;
  private FieldNamingRule namingRule;

  public ByteCodeSwitchCompiler(Map<EClass, Function<EObject, T>> caseDefinitions,
      Function<EObject, T> defaultCase, Set<EPackage> explicitPackages) {
        this.caseDefinitions = caseDefinitions;
        this.defaultCase = defaultCase;
        this.explicitPackages = explicitPackages;
        this.invocationOrders = computeInvocationOrders();
        this.namingRule = new FieldNamingRule();
  }

  @SuppressWarnings({"rawtypes", "unchecked"}) // the instantiated type will conform because the functions we feed it are type checked
  public ApplyableSwitch<T> compileSwitch() {
    Builder<ApplyableSwitch> typeUnderConstruction = new ByteBuddy().subclass(ApplyableSwitch.class);
    
    typeUnderConstruction = withAddedCaseFields(typeUnderConstruction); // fields for storing function pointers
    typeUnderConstruction = withDoSwitchMethodImplemented(typeUnderConstruction); // hardcoded doSwitch-method stub
    
    Unloaded<ApplyableSwitch> unloadedType = typeUnderConstruction.make();
    
    Class<? extends ApplyableSwitch> loadedClass = unloadedType.load(Thread.currentThread().getContextClassLoader()).getLoaded();
    ApplyableSwitch<T> instance = null;
    
    try {
      instance = loadedClass.newInstance();
    } catch (InstantiationException | IllegalAccessException e) {
      throw new RuntimeException("error invoking the constructor of the byte-assembled class", e);
    }
    
    assignFieldValues(instance); // assign concrete function pointers to the fields 
    return instance;
  }

  private void assignFieldValues(ApplyableSwitch<T> instance) {
    caseDefinitions.forEach((eClass, lambda) -> {
      try {
        Field field = instance.getClass().getField(namingRule.getFieldNameForCase(eClass));
        field.set(instance, lambda);
      } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
        throw new RuntimeException("could not set the case function correspondingly", e);
      }
      
    });
    try {
      instance.getClass().getField("defaultCase").set(instance, defaultCase);
    } catch (IllegalArgumentException | NoSuchFieldException | SecurityException | IllegalAccessException e) {
      throw new RuntimeException("could not set the default function correspondingly", e);
    }
  }

  @SuppressWarnings("rawtypes")
  private ReceiverTypeDefinition<ApplyableSwitch> withDoSwitchMethodImplemented(Builder<ApplyableSwitch> typeUnderConstruction) {
    return typeUnderConstruction
        .method(named("doSwitch"))
          .intercept(new DoSwitchImplementation(invocationOrders, namingRule));
  }

  private <S> Builder<S> withAddedCaseFields(Builder<S> to) {
    for (EClass caseClass: invocationOrders.keySet()) { // stores a function pointer for each case
      to = to.defineField(namingRule.getFieldNameForCase(caseClass), Function.class, Visibility.PUBLIC);
    }
    to = to.defineField("defaultCase", Function.class, Visibility.PUBLIC);
    return to;
  }
  
  /**
   * defines the intended behaviour of the switch
   * @return a map that maps dynamic types to an ordered list of which static cases to call
   */
  private Map<EClass, List<EClass>> computeInvocationOrders() {
    Set<EPackage> allPackages = new HashSet<EPackage>(explicitPackages);
    Set<EClass> implicitClasses = caseDefinitions.keySet();
    Set<EClass> implicitSupertypes = caseDefinitions.keySet()
      .stream()
      .flatMap(c -> c.getEAllSuperTypes().stream())
      .collect(Collectors.toSet());
    
      Stream.concat(implicitSupertypes.stream(), implicitClasses.stream())
      .map(EClass::getEPackage)
      .forEach(allPackages::add);
    
    Map<EClass, List<EClass>> invocationOrders = new HashMap<>();
    
    for (EPackage ePackage : allPackages) {
      ePackage
        .getEClassifiers()
        .stream()
        .filter(clf -> clf instanceof EClass)
        .forEach((EClassifier classifier) -> {
          EClass eClass = (EClass) classifier;
          List<EClass> invocations = new ArrayList<>();
          new BreadthFirstSearch<EClass>().scan(eClass, (c, r) -> {
            if (caseDefinitions.containsKey(c) && r.stream().noneMatch(c::isSuperTypeOf)) {
              invocations.add(c);
            }
          }, EClass::getESuperTypes);
          
          invocationOrders.put(eClass, invocations);
        });
    }
    return invocationOrders;
  }

}
