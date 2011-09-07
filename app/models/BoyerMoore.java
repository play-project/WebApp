package models;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;

public class BoyerMoore {
	public static List<Integer> match(String pattern, String text) {
		List<Integer> matches = new ArrayList<Integer>();
		int pLength = pattern.length();
		int tLength = text.length();

		Map<Character, Integer> rightMostIndexes = preprocessForBadCharacterShift(pattern);

		int alignedAt = 0;
		while (alignedAt + (pLength - 1) < tLength) {
			for (int indexInPattern = pLength - 1; indexInPattern >= 0; indexInPattern--) {
				int indexInText = alignedAt + indexInPattern;
				char x = text.charAt(indexInText);
				char y = pattern.charAt(indexInPattern);

				if (indexInText >= tLength) {
					break;
				}

				if (x != y) {
					Integer r = rightMostIndexes.get(x);

					if (r == null) {
						alignedAt = indexInText + 1;
					} else {
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

	private static Map<Character, Integer> preprocessForBadCharacterShift(String search) {
		Map<Character, Integer> map = new HashMap<Character, Integer>();
		for (int i = search.length() - 1; i >= 0; i--) {
			char c = search.charAt(i);
			if (!map.containsKey(c)) {
				map.put(c, i);
			}
		}

		return map;
	}
}
