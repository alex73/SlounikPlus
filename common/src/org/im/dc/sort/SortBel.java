package org.im.dc.sort;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

public class SortBel implements Comparator<String> {
	static final String ORDER = "0123456789абвгдеёжзійклмнопрстуўфхцчшыьэюя";
    static final Collator BE = Collator.getInstance(new Locale("be"));

	@Override
	public int compare(String o1, String o2) {
		int i = 0;
		int j = 0;
		while (true) {
			int ci, cj;
			try {
				ci = ORDER.indexOf(Character.toLowerCase(o1.charAt(i)));
			} catch (StringIndexOutOfBoundsException ex) {
				ci = -2;
			}
			try {
				cj = ORDER.indexOf(Character.toLowerCase(o2.charAt(j)));
			} catch (StringIndexOutOfBoundsException ex) {
				cj = -2;
			}
			if (ci == -1) {
				i++;
				continue;
			}
			if (cj == -1) {
				j++;
				continue;
			}
			if (ci == -2 && cj == -2) {
				return BE.compare(o1, o2);
			}

			if (ci < cj) {
				return -1;
			}
			if (ci > cj) {
				return 1;
			}
			i++;
			j++;
		}
	}
}
