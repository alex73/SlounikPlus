package org.im.dc.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Storage for some intermediate date for validation summary.
 */
public class ValidationSummaryStorage {
    // key: базавы загаловак(без націску і нумару амографа), value: спіс артыкулаў з варыянтамі(націск і нумар) - для
    // кантролю паміж сабой
    public Map<String, Object> headers = new HashMap<>();

    // спіс сапраўдных назваў артыкулаў - для кантролю спасылак
    public Set<String> realHeaders = new HashSet<>();

    // спіс спасылак у артыкулах
    public List<Object> articleLinks = new ArrayList<>();
}
