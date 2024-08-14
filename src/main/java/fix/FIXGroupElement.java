package fix;

import java.util.HashMap;
import java.util.Map;

public class FIXGroupElement extends FIXGroupContainer {
    private final Map<Integer, FIXMessage.FIXTag> tags = new HashMap<>(256);

    @Override
    public void reset() {
        super.reset();
        this.tags.clear();
    }

    public void addTag(int tagId, FIXMessage.FIXTag tag) {
        tags.put(tagId, tag);
    }

    public FIXMessage.FIXTag getTag(int tagId) {
        return tags.get(tagId);
    }
}
