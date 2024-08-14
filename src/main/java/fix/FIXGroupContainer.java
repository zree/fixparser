package fix;

import java.util.HashMap;
import java.util.Map;

public class FIXGroupContainer {
    private final Map<Integer, FIXGroup> repeatingGroups = new HashMap<>(20);

    public void reset() {
        this.repeatingGroups.clear();
    }

    public FIXGroup getRepeatingGroup(int tagId) {
        return repeatingGroups.get(tagId);
    }

    public void addRepeatingGroup(int tagId, FIXGroup fixGroup) {
        repeatingGroups.put(tagId, fixGroup);
    }
}
