package org.seasar.s2jsfplugin;

import junit.framework.TestCase;

public class UtilTest extends TestCase {

	public void testEscapeHTML() {
		String result = Util.escapeHTML("aa<>&\"‚ ‚ ");
		assertEquals("aa&lt;&gt;&amp;&quot;‚ ‚ ", result);
	}

}
