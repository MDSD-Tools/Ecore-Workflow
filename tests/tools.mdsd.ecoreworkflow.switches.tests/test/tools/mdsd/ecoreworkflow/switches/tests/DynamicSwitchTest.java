package tools.mdsd.ecoreworkflow.switches.tests;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import tools.mdsd.ecoreworkflow.switches.DynamicSwitch;
import tools.mdsd.ecoreworkflow.switches.HashDynamicSwitch;

class DynamicSwitchTest {

	@Test
	void testGetCases() {
		DynamicSwitch<String> s = new HashDynamicSwitch<>();
		s.defaultCase(o -> "default");
		assertEquals(0, s.getCases().size(), "Only the default case must be defined");
		assertNotNull(s.getDefaultCase(), "The default case must be defined");
	}

}
