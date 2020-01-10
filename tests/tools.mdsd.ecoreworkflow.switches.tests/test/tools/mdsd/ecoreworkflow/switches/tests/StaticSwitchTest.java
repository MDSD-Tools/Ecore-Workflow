package tools.mdsd.ecoreworkflow.switches.tests;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import testscenario.xutil.TestscenarioMSwitch;
import tools.mdsd.ecoreworkflow.switches.testmodel.testscenario.*;

class StaticSwitchTest {

  @Test
  void childCaseWins() {
    String result =
        new TestscenarioMSwitch<String>()
        .when((G g) -> "G")
        .when((H h) -> "H")
        .doSwitch(getE());
    
    assertEquals("G", result, "child cases are more specific");
  }
  
  @Test
  void firstParentWins() {
    String result = new TestscenarioMSwitch<String>()
        .when((F f) -> "F")
        .when((G g) -> "G")
        .doSwitch(getE());
    
    assertEquals("F", result, "first listed parent is treated more specific");
  }
  
  @Test
  void shorterInheritancePathsAreMoreSpecific() {
    StringBuffer callOrder = new StringBuffer();
    new TestscenarioMSwitch<String>()
        .when((G g) -> {callOrder.append("G"); return null;})
        .when((I i) -> {callOrder.append("I"); return null;})
        .when((H h) -> {callOrder.append("H"); return "H";})
        .orElse(x -> "X")
        .doSwitch(getE());
    
    assertEquals("GIH", callOrder.toString(), "shorter pathes are more specific");
  }
  

  private E getE() {
    return TestscenarioFactory.eINSTANCE.createE();
  }

}
