package tools.mdsd.ecoreworkflow.switches.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.eclipse.emf.ecore.EObject;
import org.junit.jupiter.api.Test;
import tools.mdsd.ecoreworkflow.switches.DynamicSwitch;
import tools.mdsd.ecoreworkflow.switches.HashDynamicSwitch;
import tools.mdsd.ecoreworkflow.switches.MSwitch.SwitchingException;
import tools.mdsd.ecoreworkflow.switches.testmodel.testscenario.E;
import tools.mdsd.ecoreworkflow.switches.testmodel.testscenario.F;
import tools.mdsd.ecoreworkflow.switches.testmodel.testscenario.TestscenarioFactory;
import tools.mdsd.ecoreworkflow.switches.testmodel.testscenario.TestscenarioPackage;
import tools.mdsd.ecoreworkflow.switches.testmodel.testscenario.TestscenarioPackage.Literals;

class DynamicSwitchTest {

  @Test
  void testGetCases() {
    DynamicSwitch<String> s = new HashDynamicSwitch<>();
    s.defaultCase(o -> "default");
    assertEquals(0, s.getCases().size(), "Only the default case must be defined");
    assertNotNull(s.getDefaultCase(), "The default case must be defined");
  }

  @Test
  void childCaseWins() {
    String result = getSwitch().dynamicCase(Literals.G, (EObject g) -> "G")
        .dynamicCase(Literals.H, (EObject h) -> "H").doSwitch(getE());

    assertEquals("G", result, "child cases are more specific");
  }

  @Test
  void firstParentWins() {
    String result = getSwitch().dynamicCase(Literals.F, (EObject f) -> "F")
        .dynamicCase(Literals.G, (EObject g) -> "G").doSwitch(getE());

    assertEquals("F", result, "first listed parent is treated more specific");
  }

  @Test
  void shorterInheritancePathsAreMoreSpecific() {
    StringBuffer callOrder = new StringBuffer();
    getSwitch().dynamicCase(Literals.G, (EObject g) -> {
      callOrder.append("G");
      return null;
    }).dynamicCase(Literals.I, (EObject i) -> {
      callOrder.append("I");
      return null;
    }).dynamicCase(Literals.H, (EObject h) -> {
      callOrder.append("H");
      return "H";
    }).defaultCase(x -> "X").doSwitch(getE());

    assertEquals("GIH", callOrder.toString(), "shorter pathes are more specific");
  }

  @Test
  void exceptionOnNoMatch() {
    DynamicSwitch<String> sw = getSwitch();
    sw.dynamicCase(Literals.G, (EObject g) -> null);
    F nonMatchingObject = TestscenarioFactory.eINSTANCE.createF();

    assertThrows(SwitchingException.class, () -> {
      sw.doSwitch(nonMatchingObject);
    }, "a SwitchingException must be thrown when none of the defined branches match and no default case is defined.");
  }

  @Test
  void defaultCaseIsCalled() {
    DynamicSwitch<String> sw = getSwitch();
    F nonMathingObject = TestscenarioFactory.eINSTANCE.createF();

    sw.dynamicCase(Literals.G, (EObject g) -> null);
    sw.defaultCase((param) -> {
      assertTrue(nonMathingObject == param,
          "default case must be passed the EObject that was put into the switch");
      return "default";
    });

    assertEquals("default", sw.doSwitch(nonMathingObject),
        "default case must be used when the defined cases don't match");
  }

  @Test
  void subsequentCasesNotCalled() {
    DynamicSwitch<String> sw = getSwitch();
    sw.dynamicCase(Literals.A, (EObject a) -> {
      fail("The A case must not be called");
      return null;
    });
    sw.dynamicCase(Literals.D, (EObject d) -> "d");
    sw.doSwitch(getE());
  }

  @Test
  void subsequentCasesCalledInCorrectOrder() {
    DynamicSwitch<String> sw = getSwitch();
    StringBuffer callOrder = new StringBuffer();

    sw.dynamicCase(Literals.A, (EObject x) -> {
      callOrder.append("A");
      return null;
    });
    sw.dynamicCase(Literals.B, (EObject x) -> {
      callOrder.append("B");
      return null;
    });
    sw.dynamicCase(Literals.C, (EObject x) -> {
      callOrder.append("C");
      return null;
    });
    sw.dynamicCase(Literals.D, (EObject x) -> {
      callOrder.append("D");
      return null;
    });
    sw.dynamicCase(Literals.E, (EObject x) -> {
      callOrder.append("E");
      return null;
    });
    sw.dynamicCase(Literals.F, (EObject x) -> {
      callOrder.append("F");
      return null;
    });
    sw.dynamicCase(Literals.G, (EObject x) -> {
      callOrder.append("G");
      return null;
    });
    sw.dynamicCase(Literals.H, (EObject x) -> {
      callOrder.append("H");
      return null;
    });
    sw.dynamicCase(Literals.I, (EObject x) -> {
      callOrder.append("I");
      return null;
    });
    sw.dynamicCase(Literals.K, (EObject x) -> {
      callOrder.append("K");
      return null;
    });
    sw.dynamicCase(Literals.L, (EObject x) -> {
      callOrder.append("L");
      return null;
    });
    sw.dynamicCase(Literals.M, (EObject x) -> {
      callOrder.append("M");
      return null;
    });
    sw.defaultCase((x) -> {
      callOrder.append("X");
      return null;
    });

    sw.doSwitch(getE());

    assertEquals("EKDCFGILBHMAX", callOrder.toString());
  }

  @Test
  void mergingLessSpecificBranchWithoutEffect() {
    DynamicSwitch<String> original = getSwitch();
    DynamicSwitch<String> merged = getSwitch();

    original.dynamicCase(Literals.D, (EObject d) -> "D");
    merged.dynamicCase(Literals.B, (EObject b) -> "B");

    assertEquals("D", original.merge(merged).doSwitch(getE()));
  }



  @Test
  void mergingSameBranchOverrides() {
    DynamicSwitch<String> original = getSwitch();
    DynamicSwitch<String> merged = getSwitch();

    original.dynamicCase(Literals.D, (EObject d) -> "1");
    merged.dynamicCase(Literals.D, (EObject d) -> "2");

    assertEquals("2", original.merge(merged).doSwitch(getE()));
  }

  @Test
  void mergingDefaultBranchOverrides() {
    DynamicSwitch<String> original = getSwitch();
    DynamicSwitch<String> merged = getSwitch();

    original.dynamicCase(Literals.D, (EObject d) -> null);
    original.defaultCase(x -> "1");
    merged.defaultCase(x -> "2");

    assertEquals("2", original.merge(merged).doSwitch(getE()));
  }

  @Test
  void mergingAffectsGetCases() {
    DynamicSwitch<String> original = getSwitch();
    DynamicSwitch<String> merged = getSwitch();

    merged.dynamicCase(Literals.A, (EObject a) -> "a");

    original.merge(merged);

    assertEquals("a", original.getCases().get(TestscenarioPackage.Literals.A).apply(getE()));
  }

  private E getE() {
    return TestscenarioFactory.eINSTANCE.createE();
  }

  private DynamicSwitch<String> getSwitch() {
    return new HashDynamicSwitch<>();
  }

}
