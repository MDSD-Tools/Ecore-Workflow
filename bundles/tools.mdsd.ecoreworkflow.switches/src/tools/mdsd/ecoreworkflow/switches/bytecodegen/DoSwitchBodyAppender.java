package tools.mdsd.ecoreworkflow.switches.bytecodegen;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.implementation.Implementation.Context;
import net.bytebuddy.implementation.bytecode.ByteCodeAppender;
import net.bytebuddy.jar.asm.Label;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.jar.asm.Opcodes;
import net.bytebuddy.jar.asm.Type;
import tools.mdsd.ecoreworkflow.switches.SwitchingException;

/**
 * appends the body of a DynamicBytecodesSwitch's doSwitch method
 * @author Christian van Rensen
 *
 */
class DoSwitchBodyAppender implements ByteCodeAppender {

  private static final String OBJECT_N = Type.getInternalName(Object.class);
  private static final String FUNCTION_N = Type.getInternalName(Function.class);
  private static final String E_OBJECT_N = Type.getInternalName(EObject.class);
  private static final String ECLASS_N = Type.getInternalName(EClass.class);
  private static final String E_CLASSIFIER_N = Type.getInternalName(EClassifier.class);
  private static final String EPACKAGE_N = Type.getInternalName(EPackage.class);
 
  private static final Type OBJECT_TYPE = Type.getType(Object.class);
  
  private static final String FUNCTION_TDESCRIPTOR = Type.getDescriptor(Function.class);
  
  private static final String TAKES_OBJECT_RETURNS_OBJECT_MDESCRIPTOR = Type.getMethodDescriptor(OBJECT_TYPE, OBJECT_TYPE);
  private static final String RETURNS_INT_MDESCRIPTOR = Type.getMethodDescriptor(Type.INT_TYPE);
  private static final String RETURNS_ECLASS_MDESCRIPTOR = Type.getMethodDescriptor(Type.getType(EClass.class));
  private static final String RETURNS_EPACKAGE_MDESCRIPTOR = Type.getMethodDescriptor(Type.getType(EPackage.class));
  
  private Map<EClass, List<EClass>> invocationsPerClass;
  private Map<EPackage, List<EClass>> classesPerPackage;
  private FieldNamingRule namingRule;

  public DoSwitchBodyAppender(Map<EClass, List<EClass>> invocationOrders, FieldNamingRule namingRule) {
    this.namingRule = namingRule;
    this.invocationsPerClass = invocationOrders;
    this.classesPerPackage = groupByEPackage(invocationOrders);
  }

  private Map<EPackage, List<EClass>> groupByEPackage(Map<EClass, List<EClass>> invocationOrders) {
    return invocationOrders.keySet().stream().collect(Collectors.groupingBy(EClass::getEPackage));
  }

  @Override
  public Size apply(MethodVisitor mv, Context ctx, MethodDescription md) {
    // Stack: |
    mv.visitVarInsn(Opcodes.ALOAD, 1); // [eObj]
    // Stack: | eObj |
    mv.visitInsn(Opcodes.DUP);
    // Stack: | eObj | eObj
    mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, E_OBJECT_N, "eClass", RETURNS_ECLASS_MDESCRIPTOR, true);
    // Stack: | eObj | eClass
    mv.visitInsn(Opcodes.DUP);
    // Stack: | eObj | eClass | eClass
    mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, E_CLASSIFIER_N, "getEPackage", RETURNS_EPACKAGE_MDESCRIPTOR, true);
    // Stack: | eObj | eClass | ePackage
    
    Label packageJumpLabel;
    Label returnResult = new Label();
    Label notFound = new Label();
    String thisTypeName = ctx.getInstrumentedType().getInternalName();
    for (Entry<EPackage, List<EClass>> packageEntry: classesPerPackage.entrySet()) {
      assert 1 == packageEntry.getKey().getClass().getInterfaces().length; // EClass implementations should only implement the interface that defines their type
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
      mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, E_CLASSIFIER_N, "getClassifierID", RETURNS_INT_MDESCRIPTOR, true);
      // Stack: | eObj | classifierID
      List<EClass> possibleDynamicTypes = packageEntry.getValue();
      appendTableSwitch(mv, returnResult, notFound, thisTypeName, possibleDynamicTypes);
      
      //mv.visitJumpInsn(Opcodes.GOTO, notFound);
      // END INSIDE THE CORRECT PACKAGE
      
      mv.visitLabel(packageJumpLabel); // <-- incoming jump
      mv.visitFrame(Opcodes.F_NEW, 2, new String[]{thisTypeName, E_OBJECT_N}, 3, new String[]{E_OBJECT_N, ECLASS_N, EPACKAGE_N});
      // Stack: | eObj | eClass | ePackage
    }
    // Stack: | eObj | eClass | ePackage
    mv.visitInsn(Opcodes.POP2);
    
    mv.visitLabel(notFound);
    // Stack: | eObj
    mv.visitFrame(Opcodes.F_NEW, 2, new String[]{thisTypeName, E_OBJECT_N}, 1, new String[]{E_OBJECT_N});
    
    mv.visitVarInsn(Opcodes.ALOAD, 0);
    //  Stack: | eObj | this
    mv.visitFieldInsn(Opcodes.GETFIELD, thisTypeName, "defaultCase", FUNCTION_TDESCRIPTOR);
    // Stack: | eObj | defaultCase
    mv.visitInsn(Opcodes.DUP);
    // Stack: | eObj | defaultCase | defaultCase
    Label noDefaultCase = new Label();
    mv.visitJumpInsn(Opcodes.IFNULL, noDefaultCase);
    // Stack: | eObj | defaultCase
    mv.visitInsn(Opcodes.SWAP);
    // Stack: | defaultCase | eObj
    mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, FUNCTION_N, "apply", TAKES_OBJECT_RETURNS_OBJECT_MDESCRIPTOR, true);
    // Stack: | result
    mv.visitInsn(Opcodes.ARETURN);
    
    mv.visitLabel(noDefaultCase);
    mv.visitFrame(Opcodes.F_NEW, 2, new String[]{thisTypeName, E_OBJECT_N}, 2, new String[]{E_OBJECT_N, FUNCTION_N});
    // Stack: | eObject | null
    mv.visitInsn(Opcodes.POP2);
    // Stack: |
    mv.visitTypeInsn(Opcodes.NEW, Type.getInternalName(SwitchingException.class));
    mv.visitInsn(Opcodes.DUP);
    mv.visitLdcInsn("no default case is defined and you have fallen through all cases");
    String exceptionConstructor;
    try {
      exceptionConstructor = Type.getConstructorDescriptor(SwitchingException.class.getConstructor(String.class));
    } catch (NoSuchMethodException | SecurityException e) {
      throw new RuntimeException("could not find SwitchingException's constructor", e);
    }
    mv.visitMethodInsn(Opcodes.INVOKESPECIAL, Type.getInternalName(SwitchingException.class), "<init>", exceptionConstructor, false);
    mv.visitInsn(Opcodes.ATHROW);
    
    mv.visitLabel(returnResult);
    mv.visitFrame(Opcodes.F_NEW, 2, new String[]{thisTypeName, E_OBJECT_N}, 2, new String[]{E_OBJECT_N, OBJECT_N});
    mv.visitInsn(Opcodes.ARETURN);
    return new Size(5, md.getStackSize());
  }

  private void appendTableSwitch(MethodVisitor mv, Label successLabel, Label notFoundLabel,
      String thisTypeName, List<EClass> possibleDynamicTypes) {
    
    // each class has a classifierID unique for the package
    // therefore we will do a tableswitch instruction with the classifierID and have to provide a label for every possible int value
    
    //first: determine the ranges:
    int minId = 1, maxId = 0;
    for (EClass eClass : possibleDynamicTypes) {
      int classifierID = eClass.getClassifierID();
      if (minId > classifierID) minId = classifierID;
      if (maxId < classifierID) maxId = classifierID;
    }
    
    // create a label for every classifierID in the range
    Label[] labels = new Label[maxId - minId + 1];
    for (int j = 0; j <= maxId - minId; j++) {
      labels[j] = new Label();
    }
    
    mv.visitTableSwitchInsn(minId, maxId, notFoundLabel, labels);
    
    for (int j = 0; j <= maxId - minId; j++) {
      int classifierId = minId + j;
      mv.visitLabel(labels[j]);
      mv.visitFrame(Opcodes.F_NEW, 2, new String[]{thisTypeName, E_OBJECT_N}, 1, new String[]{E_OBJECT_N});
      // Stack: | eObj |
      Optional<EClass> dynamicType = possibleDynamicTypes.stream().filter(c -> c.getClassifierID() == classifierId).findAny();
      if (dynamicType.isPresent()) {
        // the classifier is associated with a class we know.
        // now try to get each of the functions on the stack and call them one by one
        // Stack: | eObj 
        appendCaseInvocationsChain(mv, thisTypeName, successLabel, dynamicType.get());
        // Stack: | eObj
      }
      mv.visitJumpInsn(Opcodes.GOTO, notFoundLabel);
      // Stack: | eObj
    }
  }

  private void appendCaseInvocationsChain(MethodVisitor mv, String thisTypeName, Label successLabel, EClass dynamicType) {
    for (EClass caseDefinedOn : invocationsPerClass.getOrDefault(dynamicType, new LinkedList<>())) {
      // Stack: | eObj 
      mv.visitInsn(Opcodes.DUP);
      // Stack: | eObj | eObj
      mv.visitVarInsn(Opcodes.ALOAD, 0);
      // Stack: | eObj | eObj | this
      mv.visitFieldInsn(Opcodes.GETFIELD, thisTypeName, namingRule.getFieldNameForCase(caseDefinedOn), FUNCTION_TDESCRIPTOR);
      // Stack: | eObj | eObj | fptr
      mv.visitInsn(Opcodes.SWAP);
      // Stack: | eObj | fptr | eObj
      mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, FUNCTION_N, "apply", TAKES_OBJECT_RETURNS_OBJECT_MDESCRIPTOR, true);
      // Stack: | eObj | result
      mv.visitInsn(Opcodes.DUP);
      // Stack: | eObj | result | result
      mv.visitJumpInsn(Opcodes.IFNONNULL, successLabel);
      // Stack: | eObj | result
      mv.visitInsn(Opcodes.POP);
      // Stack: | eObj
    }
  }
    
}
