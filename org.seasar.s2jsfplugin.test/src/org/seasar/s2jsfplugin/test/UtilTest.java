package org.seasar.s2jsfplugin.test;

import junit.framework.TestCase;

import org.seasar.s2jsfplugin.Util;

public class UtilTest extends TestCase {

	public void testEscapeHTML() {
		String result = Util.escapeHTML("aa<>&\"‚ ‚ ");
		assertEquals("aa&lt;&gt;&amp;&quot;‚ ‚ ", result);
	}

}
