package tools.mdsd.ecoreworkflow.switches.tests;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import testscenario.xutil.TestscenarioMSwitch;
import tools.mdsd.ecoreworkflow.switches.MSwitch.SwitchingException;
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
  
  @Test
  void exceptionOnNoMatch() {
    TestscenarioMSwitch<String> sw = new TestscenarioMSwitch<String>();
    sw.when((G g) -> null);
    F nonMatchingObject = TestscenarioFactory.eINSTANCE.createF();
    
    assertThrows(SwitchingException.class, ()->{sw.doSwitch(nonMatchingObject);}, "a SwitchingException must be thrown when none of the defined branches match and no default case is defined.");
  }
  
  @Test
  void defaultCaseIsCalled() {
    TestscenarioMSwitch<String> sw = new TestscenarioMSwitch<String>();
    F nonMathingObject = TestscenarioFactory.eINSTANCE.createF();
    
    sw.when((G g) -> null);
    sw.orElse((param) -> {
      assertTrue(nonMathingObject == param, "default case must be passed the EObject that was put into the switch");
      return "default";
    });
    
    assertEquals("default", sw.doSwitch(nonMathingObject), "default case must be used when the defined cases don't match");
  }
  
  @Test
  void subsequentCasesNotCalled() {
    TestscenarioMSwitch<Object> sw = new TestscenarioMSwitch<>();
    sw.when((A a) -> {fail("The A case must not be called"); return null;});
    sw.when((D d) -> new Object());
    sw.doSwitch(getE());
  }
  
  @Test
  void subsequentCasesCalledInCorrectOrder() {
    TestscenarioMSwitch<Object> sw = new TestscenarioMSwitch<>();
    StringBuffer callOrder = new StringBuffer();
    
    sw.when((A x)->{callOrder.append("A"); return null;});
    sw.when((B x)->{callOrder.append("B"); return null;});
    sw.when((C x)->{callOrder.append("C"); return null;});
    sw.when((D x)->{callOrder.append("D"); return null;});
    sw.when((E x)->{callOrder.append("E"); return null;});
    sw.when((F x)->{callOrder.append("F"); return null;});
    sw.when((G x)->{callOrder.append("G"); return null;});
    sw.when((H x)->{callOrder.append("H"); return null;});
    sw.when((I x)->{callOrder.append("I"); return null;});
    sw.when((K x)->{callOrder.append("K"); return null;});
    sw.when((L x)->{callOrder.append("L"); return null;});
    sw.when((M x)->{callOrder.append("M"); return null;});
    sw.orElse((x) ->{callOrder.append("X"); return null;});
    
    sw.doSwitch(getE());
    
    assertEquals("EKDCFGILBHMAX", callOrder.toString());
  }
  
  @Test
  void mergingLessSpecificBranchWithoutEffect() {
    TestscenarioMSwitch<Object> original = new TestscenarioMSwitch<>();
    TestscenarioMSwitch<Object> merged = new TestscenarioMSwitch<>();
    
    original.when((D d)->"D");
    merged.when((B b) -> "B");
    
    assertEquals("D", original.merge(merged).doSwitch(getE()));
  }
  
  @Test
  void mergingSameBranchOverrides() {
    TestscenarioMSwitch<Object> original = new TestscenarioMSwitch<>();
    TestscenarioMSwitch<Object> merged = new TestscenarioMSwitch<>();
    
    original.when((D d)->"1");
    merged.when((D d) -> "2");
    
    assertEquals("2", original.merge(merged).doSwitch(getE()));
  }
  
  @Test
  void mergingDefaultBranchOverrides() {
    TestscenarioMSwitch<Object> original = new TestscenarioMSwitch<>();
    TestscenarioMSwitch<Object> merged = new TestscenarioMSwitch<>();
    
    original.when((D d) -> null);
    original.orElse(x -> "1");
    merged.orElse(x -> "2");
    
    assertEquals("2", original.merge(merged).doSwitch(getE()));
  }
  
  @Test
  void mergingAffectsGetCases() {
    TestscenarioMSwitch<Object> original = new TestscenarioMSwitch<>();
    TestscenarioMSwitch<Object> merged = new TestscenarioMSwitch<>();
    
    merged.when((A a) -> "a");
    
    original.merge(merged);
    
    assertEquals("a", original.getCases().get(TestscenarioPackage.Literals.A).apply(getE()));
  }
  
  private E getE() {
    return TestscenarioFactory.eINSTANCE.createE();
  }

}
