package tools.mdsd.ecoreworkflow.switches;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.eclipse.emf.codegen.ecore.genmodel.GenClass;
import org.eclipse.emf.codegen.ecore.genmodel.GenPackage;
import org.eclipse.emf.common.util.EList;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.ListExtensions;
import tools.mdsd.ecoreworkflow.mwe2lib.component.PackageLevelCodeFileGenerator;

@SuppressWarnings("all")
public class MSwitchClassGenerator implements PackageLevelCodeFileGenerator {
  private GenPackage genPackage;
  
  @Override
  public void setGenPackage(final GenPackage genPackage) {
    this.genPackage = genPackage;
  }
  
  @Override
  public Path getRelativePath() {
    Path _xblockexpression = null;
    {
      if ((this.genPackage == null)) {
        throw new IllegalStateException("genPackage was not initialized");
      }
      Path _get = Paths.get("", this.getPackageName().split("\\."));
      String _className = this.getClassName();
      String _plus = (_className + ".java");
      _xblockexpression = _get.resolve(_plus);
    }
    return _xblockexpression;
  }
  
  @Override
  public void generateCode(final OutputStream out) {
    if ((this.genPackage == null)) {
      throw new IllegalStateException("genPackage was not initialized");
    }
    final PrintWriter printWriter = new PrintWriter(out);
    printWriter.print(this.makeContent());
    printWriter.flush();
  }
  
  private String getClassName() {
    return this.genPackage.getSwitchClassName().replaceFirst("Switch$", "MSwitch");
  }
  
  private String getPackageName() {
    String _packageName = this.genPackage.getPackageName();
    return (_packageName + ".xutil");
  }
  
  private String makeContent() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("package ");
    String _packageName = this.getPackageName();
    _builder.append(_packageName);
    _builder.append(";");
    _builder.newLineIfNotEmpty();
    _builder.newLine();
    _builder.append("import java.util.function.Function;");
    _builder.newLine();
    _builder.append("import java.util.HashMap;");
    _builder.newLine();
    _builder.append("import java.util.Map;");
    _builder.newLine();
    _builder.newLine();
    _builder.append("import org.eclipse.emf.ecore.EClass;");
    _builder.newLine();
    _builder.append("import org.eclipse.emf.ecore.EPackage;");
    _builder.newLine();
    _builder.append("import org.eclipse.emf.ecore.EObject;");
    _builder.newLine();
    _builder.newLine();
    _builder.append("import tools.mdsd.ecoreworkflow.switches.MSwitch;");
    _builder.newLine();
    _builder.newLine();
    _builder.append("// auto-generated class, do not edit");
    _builder.newLine();
    _builder.newLine();
    _builder.append("public class ");
    String _className = this.getClassName();
    _builder.append(_className);
    _builder.append("<T> extends MSwitch<T> {");
    _builder.newLineIfNotEmpty();
    _builder.append("\t");
    _builder.append("private static ");
    String _importedPackageInterfaceName = this.genPackage.getImportedPackageInterfaceName();
    _builder.append(_importedPackageInterfaceName, "\t");
    _builder.append(" MODEL_PACKAGE = ");
    String _importedPackageInterfaceName_1 = this.genPackage.getImportedPackageInterfaceName();
    _builder.append(_importedPackageInterfaceName_1, "\t");
    _builder.append(".eINSTANCE;");
    _builder.newLineIfNotEmpty();
    {
      EList<GenClass> _genClasses = this.genPackage.getGenClasses();
      for(final GenClass c : _genClasses) {
        _builder.append("\t");
        _builder.append("private Function<");
        String _importedInterfaceName = c.getImportedInterfaceName();
        _builder.append(_importedInterfaceName, "\t");
        _builder.append(",T> ");
        String _caseName = this.getCaseName(c);
        _builder.append(_caseName, "\t");
        _builder.append(";");
        _builder.newLineIfNotEmpty();
      }
    }
    _builder.newLine();
    _builder.append("\t");
    _builder.append("public boolean isSwitchFor(EPackage ePackage) {");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("return ePackage == MODEL_PACKAGE;");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("\t");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("protected T doSwitch(int classifierID, EObject eObject) throws MSwitch.SwitchingException {");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("T result;");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("switch(classifierID) {");
    _builder.newLine();
    {
      EList<GenClass> _genClasses_1 = this.genPackage.getGenClasses();
      for(final GenClass c_1 : _genClasses_1) {
        _builder.append("\t\t\t");
        _builder.append("case ");
        String _importedPackageInterfaceName_2 = this.genPackage.getImportedPackageInterfaceName();
        _builder.append(_importedPackageInterfaceName_2, "\t\t\t");
        _builder.append(".");
        String _classifierID = this.genPackage.getClassifierID(c_1);
        _builder.append(_classifierID, "\t\t\t");
        _builder.append(": {");
        _builder.newLineIfNotEmpty();
        _builder.append("\t\t\t");
        _builder.append("\t");
        String _importedInterfaceName_1 = c_1.getImportedInterfaceName();
        _builder.append(_importedInterfaceName_1, "\t\t\t\t");
        _builder.append(" casted = (");
        String _importedInterfaceName_2 = c_1.getImportedInterfaceName();
        _builder.append(_importedInterfaceName_2, "\t\t\t\t");
        _builder.append(") eObject;");
        _builder.newLineIfNotEmpty();
        _builder.append("\t\t\t");
        _builder.append("\t");
        _builder.append("if (");
        String _caseName_1 = this.getCaseName(c_1);
        _builder.append(_caseName_1, "\t\t\t\t");
        _builder.append(" != null) {");
        _builder.newLineIfNotEmpty();
        _builder.append("\t\t\t");
        _builder.append("\t\t");
        _builder.append("result = ");
        String _caseName_2 = this.getCaseName(c_1);
        _builder.append(_caseName_2, "\t\t\t\t\t");
        _builder.append(".apply(casted);");
        _builder.newLineIfNotEmpty();
        _builder.append("\t\t\t");
        _builder.append("\t\t");
        _builder.append("if (result != null) return result;");
        _builder.newLine();
        _builder.append("\t\t\t");
        _builder.append("\t");
        _builder.append("}");
        _builder.newLine();
        {
          List<GenClass> _switchGenClasses = c_1.getSwitchGenClasses();
          for(final GenClass alternative : _switchGenClasses) {
            _builder.append("\t\t\t");
            _builder.append("\t");
            _builder.append("if (");
            String _caseName_3 = this.getCaseName(alternative);
            _builder.append(_caseName_3, "\t\t\t\t");
            _builder.append(" != null) {");
            _builder.newLineIfNotEmpty();
            _builder.append("\t\t\t");
            _builder.append("\t");
            _builder.append("\t");
            _builder.append("result = ");
            String _caseName_4 = this.getCaseName(alternative);
            _builder.append(_caseName_4, "\t\t\t\t\t");
            _builder.append(".apply(casted);");
            _builder.newLineIfNotEmpty();
            _builder.append("\t\t\t");
            _builder.append("\t");
            _builder.append("\t");
            _builder.append("if (result != null) return result;");
            _builder.newLine();
            _builder.append("\t\t\t");
            _builder.append("\t");
            _builder.append("}");
            _builder.newLine();
          }
        }
        _builder.append("\t\t\t");
        _builder.append("\t");
        _builder.append("break;");
        _builder.newLine();
        _builder.append("\t\t\t");
        _builder.append("}");
        _builder.newLine();
      }
    }
    _builder.append("\t\t\t");
    _builder.append("default:");
    _builder.newLine();
    _builder.append("\t\t\t\t");
    _builder.append("throw new Error(\"type \" + eObject.eClass() + \" was not considered by the mswitch code generator\");");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("return applyDefaultCase(eObject);");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("\t");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("public ");
    String _className_1 = this.getClassName();
    _builder.append(_className_1, "\t");
    _builder.append("<T> merge(");
    String _className_2 = this.getClassName();
    _builder.append(_className_2, "\t");
    _builder.append("<T> other) {");
    _builder.newLineIfNotEmpty();
    {
      final Function1<GenClass, String> _function = (GenClass it) -> {
        return this.getCaseName(it);
      };
      List<String> _map = ListExtensions.<GenClass, String>map(this.genPackage.getGenClasses(), _function);
      for(final String field : _map) {
        _builder.append("\t\t");
        _builder.append("if (other.");
        _builder.append(field, "\t\t");
        _builder.append(" != null) this.");
        _builder.append(field, "\t\t");
        _builder.append(" = other.");
        _builder.append(field, "\t\t");
        _builder.append(";");
        _builder.newLineIfNotEmpty();
      }
    }
    _builder.append("\t\t");
    _builder.append("return this;");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("} ");
    _builder.newLine();
    _builder.append("\t");
    _builder.newLine();
    {
      EList<GenClass> _genClasses_2 = this.genPackage.getGenClasses();
      for(final GenClass c_2 : _genClasses_2) {
        _builder.append("\t");
        _builder.append("public interface ");
        String _interfaceName = this.getInterfaceName(c_2);
        _builder.append(_interfaceName, "\t");
        _builder.append("<T> extends Function<");
        String _importedInterfaceName_3 = c_2.getImportedInterfaceName();
        _builder.append(_importedInterfaceName_3, "\t");
        _builder.append(",T> {}");
        _builder.newLineIfNotEmpty();
      }
    }
    _builder.append("\t");
    _builder.newLine();
    {
      EList<GenClass> _genClasses_3 = this.genPackage.getGenClasses();
      for(final GenClass c_3 : _genClasses_3) {
        _builder.append("\t");
        _builder.append("public ");
        String _className_3 = this.getClassName();
        _builder.append(_className_3, "\t");
        _builder.append("<T> when(");
        String _interfaceName_1 = this.getInterfaceName(c_3);
        _builder.append(_interfaceName_1, "\t");
        _builder.append("<T> then) {");
        _builder.newLineIfNotEmpty();
        _builder.append("\t");
        _builder.append("\t");
        _builder.append("this.");
        String _caseName_5 = this.getCaseName(c_3);
        _builder.append(_caseName_5, "\t\t");
        _builder.append(" = then;");
        _builder.newLineIfNotEmpty();
        _builder.append("\t");
        _builder.append("\t");
        _builder.append("return this;");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("}");
        _builder.newLine();
      }
    }
    _builder.append("\t");
    _builder.append("public ");
    String _className_4 = this.getClassName();
    _builder.append(_className_4, "\t");
    _builder.append("<T> orElse(Function<EObject, T> defaultCase) {");
    _builder.newLineIfNotEmpty();
    _builder.append("\t\t");
    _builder.append("this.defaultCase = defaultCase;");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("return this;");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("\t");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("@Override");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("public Map<EClass, Function<EObject, T>> getCases() {");
    _builder.newLine();
    _builder.append("\t  ");
    _builder.append("Map<EClass, Function<EObject, T>> definedCases = new HashMap<>();");
    _builder.newLine();
    _builder.append("\t  ");
    _builder.newLine();
    {
      EList<GenClass> _genClasses_4 = this.genPackage.getGenClasses();
      for(final GenClass c_4 : _genClasses_4) {
        _builder.append("\t  ");
        _builder.append("if (this.");
        String _caseName_6 = this.getCaseName(c_4);
        _builder.append(_caseName_6, "\t  ");
        _builder.append(" != null) {");
        _builder.newLineIfNotEmpty();
        _builder.append("\t  ");
        _builder.append("\t");
        _builder.append("definedCases.put(");
        String _importedPackageInterfaceName_3 = this.genPackage.getImportedPackageInterfaceName();
        _builder.append(_importedPackageInterfaceName_3, "\t  \t");
        _builder.append(".Literals.");
        String _classifierID_1 = this.genPackage.getClassifierID(c_4);
        _builder.append(_classifierID_1, "\t  \t");
        _builder.append(", this.");
        String _caseName_7 = this.getCaseName(c_4);
        _builder.append(_caseName_7, "\t  \t");
        _builder.append(".compose(o -> (");
        String _importedInterfaceName_4 = c_4.getImportedInterfaceName();
        _builder.append(_importedInterfaceName_4, "\t  \t");
        _builder.append(") o));");
        _builder.newLineIfNotEmpty();
        _builder.append("\t  ");
        _builder.append("}");
        _builder.newLine();
      }
    }
    _builder.append("\t  ");
    _builder.newLine();
    _builder.append("\t  ");
    _builder.append("return definedCases;");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    return _builder.toString();
  }
  
  private String getCaseName(final GenClass c) {
    String _classUniqueName = this.genPackage.getClassUniqueName(c);
    return ("case" + _classUniqueName);
  }
  
  private String getInterfaceName(final GenClass c) {
    String _classUniqueName = this.genPackage.getClassUniqueName(c);
    return ("When" + _classUniqueName);
  }
}
