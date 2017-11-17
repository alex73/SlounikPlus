package org.im.dc.client.spell;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.languagetool.JLanguageTool;
import org.languagetool.language.Belarusian;

public class SpellDictionary {
    private static Set<String> words;

    public static synchronized void loadDictionary() {
        if (words != null) {
            return;
        }
        try (BufferedReader rd = new BufferedReader(
                new InputStreamReader(SpellDictionary.class.getResourceAsStream("/be2008.txt"), "UTF-8"))) {
            String s;
            List<String> ws = new ArrayList<>();
            while ((s = rd.readLine()) != null) {
                ws.add(s);
            }
            words = new HashSet<>(ws);
        } catch (Exception ex) {
            throw new RuntimeException("Error load dictionary", ex);
        }
        JLanguageTool lt=new JLanguageTool(new Belarusian());
    }

    public List<SpellError> check(String text) {
        loadDictionary();

        List<SpellError> errors = new ArrayList<>();
        String lo = text.toLowerCase();
        int wordStart = -1;
        for (int i = 0; i < lo.length(); i++) {
            char c = lo.charAt(i);
            boolean isLetter = LETTERS.indexOf(c) >= 0;
            if (isLetter) {
                if (wordStart < 0) {
                    wordStart = i;
                }
            } else {
                if (wordStart >= 0) {
                    // check this word
                    String word = text.substring(wordStart, i);
                    if (!words.contains(word)) {
                        errors.add(new SpellError(wordStart, i - wordStart, "Невядомае слова: " + word));
                    }
                    wordStart = -1;
                }
            }
        }
        return errors;
    }

    static String LETTERS = "ёйцукенгшўзх'фывапролджэячсмітьбю";
}
