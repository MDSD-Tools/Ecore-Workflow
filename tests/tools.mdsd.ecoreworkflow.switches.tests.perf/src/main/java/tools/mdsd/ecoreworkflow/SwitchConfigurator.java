package tools.mdsd.ecoreworkflow;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.ComposedSwitch;
import org.eclipse.emf.ecore.util.Switch;
import tools.mdsd.ecoreworkflow.switches.testmodel.testscenario.xutil.TestscenarioMSwitch;
import tools.mdsd.ecoreworkflow.switches.testmodel.testscenario2.Y;
import tools.mdsd.ecoreworkflow.switches.testmodel.testscenario2.Z;
import tools.mdsd.ecoreworkflow.switches.testmodel.testscenario2.util.Testscenario2Switch;
import tools.mdsd.ecoreworkflow.switches.DynamicSwitch;
import tools.mdsd.ecoreworkflow.switches.HashDynamicSwitch;
import tools.mdsd.ecoreworkflow.switches.BytecodeDynamicSwitch;
import tools.mdsd.ecoreworkflow.switches.testmodel.testscenario.*;
import static tools.mdsd.ecoreworkflow.switches.testmodel.testscenario.TestscenarioPackage.Literals.*;
import static tools.mdsd.ecoreworkflow.switches.testmodel.testscenario2.Testscenario2Package.Literals.*;
import java.util.Arrays;
import tools.mdsd.ecoreworkflow.switches.testmodel.testscenario.util.TestscenarioSwitch;

public class SwitchConfigurator {
  TestscenarioMSwitch<String> buildMSwitch() {
    return new TestscenarioMSwitch<String>()
        .when((L l)-> "l")
        .when((B b)-> "b")
        .when((C c)-> "c")
        .when((H h)-> "h")
        .orElse((EObject object)-> "*");
  }
  
  TestscenarioSwitch<String> buildClassicSwitch() {
    return new TestscenarioSwitch<String>() {
      public String caseL(tools.mdsd.ecoreworkflow.switches.testmodel.testscenario.L object) {
        return "l";
      };
      public String caseB(tools.mdsd.ecoreworkflow.switches.testmodel.testscenario.B object) {
        return "b";
      };
      public String caseC(tools.mdsd.ecoreworkflow.switches.testmodel.testscenario.C object) {
        return "c";
      };
      public String caseH(tools.mdsd.ecoreworkflow.switches.testmodel.testscenario.H object) {
        return "h";
      };
      public String defaultCase(org.eclipse.emf.ecore.EObject object) {
        return "*";
      };
    };
  }
  
  DynamicSwitch<String> buildDynamicSwitch() {
    return new HashDynamicSwitch<String>()
        .dynamicCase(L, (EObject l)-> "l")
        .dynamicCase(B, (EObject b)-> "b")
        .dynamicCase(C, (EObject c)-> "c")
        .dynamicCase(H, (EObject h)-> "h")
        .defaultCase((EObject object)-> "*");
  }

  ComposedSwitch<String> buildComposedSwitch() {
    Switch<String> switch1 = buildClassicSwitch();
    Switch<String> switch2 = buildClassicSwitch2();
    return new ComposedSwitch<String>(Arrays.asList(switch1, switch2));
  }
  
  DynamicSwitch<String> buildComposedDynamicSwitch() {
    return buildDynamicSwitch()
        .dynamicCase(Y, (EObject y)-> "y")
        .dynamicCase(Z, (EObject z)-> "z");
  }

  private Testscenario2Switch<String> buildClassicSwitch2() {
    return new Testscenario2Switch<String>() {
      @Override
      public String caseY(Y object) {
        return "y";
      }
      
      @Override
      public String caseZ(Z object) {
        return "z";
      }
    };
  }
  
  public DynamicSwitch<String> buildDynamicBytecodeSwitch() {
    BytecodeDynamicSwitch<String> sw = new BytecodeDynamicSwitch<String>();
    sw
        .dynamicCase(L, (EObject l)-> "l")
        .dynamicCase(B, (EObject b)-> "b")
        .dynamicCase(C, (EObject c)-> "c")
        .dynamicCase(H, (EObject h)-> "h")
        .defaultCase((EObject object)-> "*")
        .dynamicCase(Y, (EObject y)-> "y")
        .dynamicCase(Z, (EObject z)-> "z");
    return sw.precompile();
  }
  
  public DynamicSwitch<String> buildComposedDynamicBytecodeSwitch() {
    BytecodeDynamicSwitch<String> sw = new BytecodeDynamicSwitch<String>();
    sw
        .dynamicCase(L, (EObject l)-> "l")
        .dynamicCase(B, (EObject b)-> "b")
        .dynamicCase(C, (EObject c)-> "c")
        .dynamicCase(H, (EObject h)-> "h")
        .defaultCase((EObject object)-> "*")
        .dynamicCase(Y, (EObject y)-> "y")
        .dynamicCase(Z, (EObject z)-> "z");
    return sw.precompile();
  }
}
