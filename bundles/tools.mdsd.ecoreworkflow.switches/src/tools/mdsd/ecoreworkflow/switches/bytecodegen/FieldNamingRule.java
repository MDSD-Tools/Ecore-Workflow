package tools.mdsd.ecoreworkflow.switches.bytecodegen;

import org.eclipse.emf.ecore.EClass;

class FieldNamingRule {
  public String getFieldNameForCase(EClass eClass) {
    return "case_" + Math.abs(eClass.getEPackage().getNsURI().hashCode()) + eClass.getEPackage().getName() + "_" + eClass.getClassifierID();
  }
}
