package tools.mdsd.ecoreworkflow.switches.bytecodegen;

import java.util.List;
import java.util.Map;
import org.eclipse.emf.ecore.EClass;
import net.bytebuddy.dynamic.scaffold.InstrumentedType;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.bytecode.ByteCodeAppender;

class DoSwitchImplementation implements Implementation {
  
  private Map<EClass, List<EClass>> invocationOrders;
  private FieldNamingRule namingRule;

  public DoSwitchImplementation(Map<EClass, List<EClass>> invocationOrders, FieldNamingRule namingRule) {
    this.invocationOrders = invocationOrders;
    this.namingRule = namingRule;
  }

  @Override
  public InstrumentedType prepare(InstrumentedType instrType) {
    return instrType;
  }

  @Override
  public ByteCodeAppender appender(Target target) {
    return new DoSwitchBodyAppender(invocationOrders, namingRule);
  }
  
}