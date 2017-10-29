package org.im.dc.service.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Dictionaries {
    public Map<String, Dictionary> dicts = new TreeMap<>();

    public static class Dictionary {
        public List<String> values = new ArrayList<>();
    }

    public boolean exist(String dict, String val) {
        Dictionary d = dicts.get(dict);
        if (d == null) {
            return false;
        }
        return d.values.contains(val);
    }

    public void add(String dict, String val) {
        Dictionary d = dicts.get(dict);
        if (d == null) {
            d = new Dictionary();
            dicts.put(dict, d);
        }
        d.values.add(val);
    }
}
