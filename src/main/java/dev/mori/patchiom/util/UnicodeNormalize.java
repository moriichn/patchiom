package dev.mori.patchiom.util;

import java.text.Normalizer;
import java.util.Map;

// no good? no, this man is definitely up to evil.
// moulberry, you fool! your naming fell victim to byte-level inspection!
public class UnicodeNormalize {

	private static final Map<Integer, String> CONFUSABLES = Map.ofEntries(
			// Cyrillic
			Map.entry((int) 'А', "A"),
			Map.entry((int) 'В', "B"),
			Map.entry((int) 'С', "C"),
			Map.entry((int) 'Е', "E"),
			Map.entry((int) 'Н', "H"),
			Map.entry((int) 'К', "K"),
			Map.entry((int) 'М', "M"),
			Map.entry((int) 'О', "O"),
			Map.entry((int) 'Р', "P"),
			Map.entry((int) 'Т', "T"),
			Map.entry((int) 'Х', "X"),
			Map.entry((int) 'У', "Y"),

			Map.entry((int) 'а', "a"),
			Map.entry((int) 'е', "e"),
			Map.entry((int) 'о', "o"),
			Map.entry((int) 'р', "p"),
			Map.entry((int) 'с', "c"),
			Map.entry((int) 'х', "x"),
			Map.entry((int) 'у', "y"),

			// Greek
			Map.entry((int) 'Α', "A"),
			Map.entry((int) 'Β', "B"),
			Map.entry((int) 'Ε', "E"),
			Map.entry((int) 'Ζ', "Z"),
			Map.entry((int) 'Η', "H"),
			Map.entry((int) 'Ι', "I"),
			Map.entry((int) 'Κ', "K"),
			Map.entry((int) 'Μ', "M"),
			Map.entry((int) 'Ν', "N"),
			Map.entry((int) 'Ο', "O"),
			Map.entry((int) 'Ρ', "P"),
			Map.entry((int) 'Τ', "T"),
			Map.entry((int) 'Χ', "X"),

			Map.entry((int) 'α', "a"),
			Map.entry((int) 'ο', "o"),
			Map.entry((int) 'ρ', "p")
	);

	public static String normalizeCharacters(String input) {
		input = Normalizer.normalize(input, Normalizer.Form.NFKC);
		StringBuilder result = new StringBuilder(input.length());
		input.codePoints().forEach(codePoint -> result.append(CONFUSABLES.getOrDefault(codePoint, new String(Character.toChars(codePoint)))));
		return result.toString();
	}


}
