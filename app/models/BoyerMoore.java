package models;
/* Copyright (c) 2011 the authors listed at the following URL, and/or
the authors of referenced articles or incorporated external code:
http://en.literateprograms.org/Boyer-Moore_string_search_algorithm_(Java)?action=history&offset=20100924202517

Permission is hereby granted, free of charge, to any person obtaining
a copy of this software and associated documentation files (the
"Software"), to deal in the Software without restriction, including
without limitation the rights to use, copy, modify, merge, publish,
distribute, sublicense, and/or sell copies of the Software, and to
permit persons to whom the Software is furnished to do so, subject to
the following conditions:

The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

Retrieved from: http://en.literateprograms.org/Boyer-Moore_string_search_algorithm_(Java)?oldid=16950
 */

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;

public class BoyerMoore {
	public static List<Integer> match(String pattern, String text) {
		List<Integer> matches = new ArrayList<Integer>();
		int m = text.length();
		int n = pattern.length();

		Map<Character, Integer> rightMostIndexes = preprocessForBadCharacterShift(pattern);

		int alignedAt = 0;
		while (alignedAt + (n - 1) < m) {

			for (int indexInPattern = n - 1; indexInPattern >= 0; indexInPattern--) {
				int indexInText = alignedAt + indexInPattern;
				char x = text.charAt(indexInText);
				char y = pattern.charAt(indexInPattern);

				if (indexInText >= m)
					break;

				if (x != y) {

					Integer r = rightMostIndexes.get(x);

					if (r == null) {
						alignedAt = indexInText + 1;
					}

					else {
						int shift = indexInText - (alignedAt + r);
						alignedAt += shift > 0 ? shift : 1;
					}

					break;
				} else if (indexInPattern == 0) {
					matches.add(alignedAt);
					alignedAt++;
				}

			}
		}
		return matches;
	}

	private static Map<Character, Integer> preprocessForBadCharacterShift(String pattern) {
		Map<Character, Integer> map = new HashMap<Character, Integer>();
		for (int i = pattern.length() - 1; i >= 0; i--) {
			char c = pattern.charAt(i);
			if (!map.containsKey(c))
				map.put(c, i);
		}

		return map;
	}
}
