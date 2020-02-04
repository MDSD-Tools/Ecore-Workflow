package tools.mdsd.ecoreworkflow.switches;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.dynamic.DynamicType.Unloaded;
import net.bytebuddy.dynamic.scaffold.InstrumentedType;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.Implementation.Context;
import net.bytebuddy.implementation.SuperMethodCall;
import net.bytebuddy.implementation.bytecode.ByteCodeAppender;
import net.bytebuddy.implementation.bytecode.StackManipulation;
import net.bytebuddy.implementation.bytecode.constant.IntegerConstant;
import net.bytebuddy.jar.asm.Label;
import net.bytebuddy.jar.asm.MethodVisitor;
import static net.bytebuddy.matcher.ElementMatchers.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcorePackage;
import com.google.common.collect.Streams;
import org.eclipse.emf.ecore.EClassifier;
import net.bytebuddy.jar.asm.Opcodes;
import net.bytebuddy.jar.asm.Type;
import tools.mdsd.ecoreworkflow.switches.MSwitch.SwitchingException;

public class BytecodeDynamicSwitch<T> extends AbstractInspectableDynamicSwitch<T> implements DynamicSwitch<T>, ApplyableSwitch<T>, InspectableSwitch<T> {
  
  private ApplyableSwitch<T> compiledSwitch;
  private Set<EPackage> explicitPackages = new HashSet<>();
  
  public BytecodeDynamicSwitch<T> precompile() {
    if (compiledSwitch == null) {
      compiledSwitch = compileSwitch();
    }
    return this;
  }
  
  class LargeSwitchLogicAppender implements ByteCodeAppender {

    private Map<EClass, List<EClass>> invocationsPerClass;
    private Map<EPackage, List<EClass>> classesPerPackage;

    public LargeSwitchLogicAppender(Map<EClass, List<EClass>> invocationOrders) {
      invocationsPerClass = invocationOrders;
      classesPerPackage = new HashMap<>();
      invocationOrders.entrySet().stream().forEach(entry -> {
        EClass eClass = entry.getKey();
        EPackage ePackage = eClass.getEPackage();
        if (!classesPerPackage.containsKey(ePackage)) {
          classesPerPackage.put(ePackage, new ArrayList<>());
        }
        classesPerPackage.get(ePackage).add(eClass);
      });
    }

    @Override
    public Size apply(MethodVisitor mv, Context ctx, MethodDescription md) {
      // Stack: |
      mv.visitVarInsn(Opcodes.ALOAD, 1); // [eObj]
      // Stack: | eObj |
      mv.visitInsn(Opcodes.DUP);
      // Stack: | eObj | eObj
      mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, Type.getInternalName(EObject.class), "eClass", Type.getMethodDescriptor(Type.getType(EClass.class)), true);
      // Stack: | eObj | eClass
      mv.visitInsn(Opcodes.DUP);
      // Stack: | eObj | eClass | eClass
      mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, Type.getInternalName(EClassifier.class), "getEPackage", Type.getMethodDescriptor(Type.getType(EPackage.class)), true);
      // Stack: | eObj | eClass | ePackage
      
      Label packageJumpLabel;
      Label returnResult = new Label();
      Label notFound = new Label();
      for (Entry<EPackage, List<EClass>> packageEntry: classesPerPackage.entrySet()) {
        Class<?> candidatePackageInterface = packageEntry.getKey().getClass().getInterfaces()[0];
        
        // Stack: | eObj | eClass | ePackage
        mv.visitInsn(Opcodes.DUP);
        // Stack: | eObj | eClass | ePackage | ePackage
        mv.visitFieldInsn(Opcodes.GETSTATIC, Type.getInternalName(candidatePackageInterface), "eINSTANCE", Type.getDescriptor(candidatePackageInterface));
        
        // Stack: | eObj | eClass | ePackage | ePackage | candidatePackage
        packageJumpLabel = new Label();
        mv.visitJumpInsn(Opcodes.IF_ACMPNE, packageJumpLabel);
        // Stack: | eObj | eClass | ePackage
        // INSIDE THE CORRECT PACKAGE
        
        mv.visitInsn(Opcodes.POP);
        // Stack: | eObj | eClass
        mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, Type.getInternalName(EClassifier.class), "getClassifierID", Type.getMethodDescriptor(Type.INT_TYPE), true);
        // Stack: | eObj | classifierID
        List<EClass> classEntries = packageEntry.getValue();
        int minId = 1, maxId = 0;
        for (EClass eClass : classEntries) {
          int clf = eClass.getClassifierID();
          if (minId > clf) minId = clf;
          if (maxId < clf) maxId = clf;
        }
        Label[] labels = new Label[maxId - minId + 1];
        for (int j = 0; j <= maxId - minId; j++) {
          labels[j] = new Label();
        }
        
        mv.visitTableSwitchInsn(minId, maxId, notFound, labels);
        
        
        for (int j = 0; j <= maxId - minId; j++) {
          int classifierId = minId + j;
          mv.visitLabel(labels[j]);
          mv.visitFrame(Opcodes.F_NEW, 2, new String[]{ctx.getInstrumentedType().getInternalName(), Type.getInternalName(EObject.class)}, 1, new String[]{Type.getInternalName(EObject.class)});
          // Stack: | eObj |
          Optional<EClass> dynamicType = classEntries.stream().filter(c -> c.getClassifierID() == classifierId).findAny();
          if (dynamicType.isPresent()) {
            // the classifier is associated with a class we know.
            // now try to get each of the functions on the stack and call them one by one
            for (EClass caseDefinedOn : invocationsPerClass.getOrDefault(dynamicType.get(), new LinkedList<>())) {
              // Stack: | eObj 
              mv.visitInsn(Opcodes.DUP);
              // Stack: | eObj | eObj
              mv.visitVarInsn(Opcodes.ALOAD, 0);
              // Stack: | eObj | eObj | this
              mv.visitFieldInsn(Opcodes.GETFIELD, ctx.getInstrumentedType().getInternalName(), getCaseName(caseDefinedOn), Type.getDescriptor(Function.class));
              // Stack: | eObj | eObj | fptr
              mv.visitInsn(Opcodes.SWAP);
              // Stack: | eObj | fptr | eObj
              mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, Type.getInternalName(Function.class), "apply", Type.getMethodDescriptor(Type.getType(Object.class), Type.getType(Object.class)), true);
              // Stack: | eObj | result
              mv.visitInsn(Opcodes.DUP);
              // Stack: | eObj | result | result
              mv.visitJumpInsn(Opcodes.IFNONNULL, returnResult);
              // Stack: | eObj | result
              mv.visitInsn(Opcodes.POP);
              // Stack: | eObj
            }
          }
          mv.visitJumpInsn(Opcodes.GOTO, notFound);
          
        }
        
        //mv.visitJumpInsn(Opcodes.GOTO, notFound);
        // END INSIDE THE CORRECT PACKAGE
        
        mv.visitLabel(packageJumpLabel); // <-- incoming jump
        mv.visitFrame(Opcodes.F_NEW, 2, new String[]{ctx.getInstrumentedType().getInternalName(), Type.getInternalName(EObject.class)}, 3, new String[]{Type.getInternalName(EObject.class), Type.getInternalName(EClass.class), Type.getInternalName(EPackage.class)});
        // Stack: | eObj | eClass | ePackage
      }
      // Stack: | eObj | eClass | ePackage
      mv.visitInsn(Opcodes.POP2);
      
      mv.visitLabel(notFound);
      // Stack: | eObj
      mv.visitFrame(Opcodes.F_NEW, 2, new String[]{ctx.getInstrumentedType().getInternalName(), Type.getInternalName(EObject.class)}, 1, new String[]{Type.getInternalName(EObject.class)});
      
      mv.visitVarInsn(Opcodes.ALOAD, 0);
      //  Stack: | eObj | this
      mv.visitFieldInsn(Opcodes.GETFIELD, ctx.getInstrumentedType().getInternalName(), "defaultCase", Type.getDescriptor(Function.class));
      // Stack: | eObj | defaultCase
      mv.visitInsn(Opcodes.DUP);
      // Stack: | eObj | defaultCase | defaultCase
      Label noDefaultCase = new Label();
      mv.visitJumpInsn(Opcodes.IFNULL, noDefaultCase);
      // Stack: | eObj | defaultCase
      mv.visitInsn(Opcodes.SWAP);
      // Stack: | defaultCase | eObj
      mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, Type.getInternalName(Function.class), "apply", Type.getMethodDescriptor(Type.getType(Object.class), Type.getType(Object.class)), true);
      // Stack: | result
      mv.visitInsn(Opcodes.ARETURN);
      
      mv.visitLabel(noDefaultCase);
      mv.visitFrame(Opcodes.F_NEW, 2, new String[]{ctx.getInstrumentedType().getInternalName(), Type.getInternalName(EObject.class)}, 2, new String[]{Type.getInternalName(EObject.class), Type.getInternalName(Function.class)});
      // Stack: | eObject | null
      mv.visitInsn(Opcodes.POP2);
      // Stack: |
      mv.visitTypeInsn(Opcodes.NEW, Type.getInternalName(SwitchingException.class));
      mv.visitInsn(Opcodes.DUP);
      mv.visitLdcInsn("no default case is defined and you have fallen through all cases");
      try {
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, Type.getInternalName(SwitchingException.class), "<init>", Type.getConstructorDescriptor(SwitchingException.class.getConstructor(String.class)), false);
      } catch (NoSuchMethodException | SecurityException e) {
        throw new RuntimeException("could not find SwitchingException's constructor", e);
      }
      mv.visitInsn(Opcodes.ATHROW);
      
      mv.visitLabel(returnResult);
      mv.visitFrame(Opcodes.F_NEW, 2, new String[]{ctx.getInstrumentedType().getInternalName(), Type.getInternalName(EObject.class)}, 2, new String[]{Type.getInternalName(EObject.class), Type.getInternalName(Object.class)});
      mv.visitInsn(Opcodes.ARETURN);
      return new Size(5, md.getStackSize());
    }
    
  }
  
  class LargeSwitchLogicImplementation implements Implementation {
    
    private Map<EClass, List<EClass>> invocationOrders;

    public LargeSwitchLogicImplementation(Map<EClass, List<EClass>> invocationOrders) {
      this.invocationOrders = invocationOrders;
    }

    @Override
    public InstrumentedType prepare(InstrumentedType instrType) {
      return instrType;
    }

    @Override
    public ByteCodeAppender appender(Target target) {
      return new LargeSwitchLogicAppender(invocationOrders);
    }
    
  }
  
  @SuppressWarnings("unchecked")
  private ApplyableSwitch<T> compileSwitch() {
    Map<EClass, List<EClass>> invocationOrders = computeInvocationOrders();
    
   //invocationOrders.forEach((cl, invs)->System.out.println(cl.getName() + ":" + invs.stream().map(EClass::getName).collect(Collectors.joining(",")))); 
    
    Builder<ApplyableSwitch> typeUnderConstruction = new ByteBuddy()
        .subclass(ApplyableSwitch.class, ConstructorStrategy.Default.DEFAULT_CONSTRUCTOR);
    
    for (EClass caseClass: invocationOrders.keySet()) { // stores a function pointer for each case
      typeUnderConstruction = typeUnderConstruction.defineField(getCaseName(caseClass), Function.class, Visibility.PUBLIC);
    }
    typeUnderConstruction = typeUnderConstruction.defineField("defaultCase", Function.class, Visibility.PUBLIC);
    
    Unloaded<ApplyableSwitch> made = typeUnderConstruction
        .method(named("doSwitch"))
          .intercept(new LargeSwitchLogicImplementation(invocationOrders))
        .make();
    
//    try {
//      made.saveIn(new File("c:/temp"));
//    } catch (IOException e1) {
//      // TODO Auto-generated catch block
//      e1.printStackTrace();
//    }
    
    Class<? extends ApplyableSwitch> dynamicType = made
        .load(Thread.currentThread().getContextClassLoader())
        .getLoaded();
    try {
      ApplyableSwitch instance = dynamicType.newInstance();
      caseDefinitions.forEach((eClass, lambda) -> {
        try {
          Field field = instance.getClass().getField(getCaseName(eClass));
          field.set(instance, lambda);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
          throw new RuntimeException("could not set the case function correspondingly", e);
        }
        
      });
      try {
        instance.getClass().getField("defaultCase").set(instance, defaultCase);
      } catch (IllegalArgumentException | NoSuchFieldException | SecurityException e) {
        throw new RuntimeException("could not set the default function correspondingly", e);
      }
      
      return instance;
    } catch (InstantiationException | IllegalAccessException e) {
      throw new RuntimeException("error invoking the constructor of the byte-assembled class", e);
    }
  }
  
  private String getCaseName(EClass eClass) {
    return "case_" + Math.abs(eClass.getEPackage().getNsURI().hashCode()) + eClass.getEPackage().getName() + "_" + eClass.getClassifierID();
  }

  protected Map<EClass, List<EClass>> computeInvocationOrders() {
    Set<EPackage> allPackages = new HashSet<EPackage>(explicitPackages);
    Set<EClass> implicitClasses = caseDefinitions.keySet();
    Set<EClass> implicitSupertypes = caseDefinitions.keySet()
      .stream()
      .flatMap(c -> c.getEAllSuperTypes().stream())
      .collect(Collectors.toSet());
    
      Streams.concat(implicitSupertypes.stream(), implicitClasses.stream())
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

  @Override
  protected boolean canDafineCases() {
    return compiledSwitch == null;
  }

  @Override
  public T doSwitch(EObject object) {
    precompile();
    return compiledSwitch.doSwitch(object);
  }
}


