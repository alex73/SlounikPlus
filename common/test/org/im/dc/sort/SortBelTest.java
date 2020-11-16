package org.im.dc.sort;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.Test;

public class SortBelTest {
	@Test
	public void testOrder() {
		SortBel s = new SortBel();

		assertEquals(1, s.compare("Ао", "а"));
		
		assertEquals(-1, s.compare("але", "алё"));
		assertEquals(0, s.compare("Але", "ал'е"));
		assertEquals(1, s.compare("Алео", "ал'е"));
		assertEquals(-1, s.compare("а/1", "ай"));
	}
}
