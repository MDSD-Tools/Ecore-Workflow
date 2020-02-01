package tools.mdsd.ecoreworkflow;

import org.eclipse.emf.ecore.EObject;
import tools.mdsd.ecoreworkflow.switches.testmodel.testscenario.xutil.TestscenarioMSwitch;
import tools.mdsd.ecoreworkflow.switches.DynamicSwitch;
import tools.mdsd.ecoreworkflow.switches.HashDynamicSwitch;
import tools.mdsd.ecoreworkflow.switches.testmodel.testscenario.*;
import static tools.mdsd.ecoreworkflow.switches.testmodel.testscenario.TestscenarioPackage.Literals.*;
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
}
