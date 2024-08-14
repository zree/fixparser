package fix;

import exception.InvalidFIXMessageException;
import utils.Constants;
import utils.FIXTags;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class FIXMessage extends FIXGroupContainer {
    private int lastTagIndex = -1;
    private int checkSum = 0;
    private int bodyLen = 0;

    private byte[] msgCache;
    private FIXTag[] tags = new FIXTag[256];
    private FIXGroup[] groups = new FIXGroup[20];
    private int groupIndex = -1;
    private boolean parsedRepeatingGroup = false;

    private final StringBuilder sb = new StringBuilder(1024);
    private final Map<Integer, Integer> tagIndex = new HashMap<>(256);
    private final InvalidFIXMessageException invalidFIXMessageException = new InvalidFIXMessageException();


    public FIXMessage() {
        for (int i = 0; i < tags.length; i++) {
            tags[i] = new FIXTag();
        }
        for (int i = 0; i < groups.length; i++) {
            groups[i] = new FIXGroup();
        }
    }

    public void init(byte[] msgCache) {
        reset();
        this.msgCache = msgCache;
    }

    @Override
    public void reset() {
        super.reset();
        for (int i = 0; i <= lastTagIndex; i++) {
            tags[i].reset();
        }
        for (int i = 0; i <= groupIndex; i++) {
            groups[i].reset();
        }
        this.lastTagIndex = -1;
        this.groupIndex = -1;
        this.checkSum = 0;
        this.bodyLen = 0;
        this.parsedRepeatingGroup = false;
        this.tagIndex.clear();
    }


    public void setCheckSum(int checkSum) {
        this.checkSum = checkSum;
    }

    public void setBodyLen(int bodyLen) {
        this.bodyLen = bodyLen;
    }


    public void addTag(int tagId, int offset, int length) {
        lastTagIndex++;
        if (tags.length == lastTagIndex) {
            tags = Arrays.copyOf(tags, lastTagIndex * 2);
            for (int i = lastTagIndex; i < tags.length; i++) {
                tags[i] = new FIXTag();
            }
        }
        tags[lastTagIndex].set(tagId, offset, length);
        tagIndex.put(tagId, lastTagIndex);
    }

    public FIXTag getTag(int tagId) {
        int index = tagIndex.getOrDefault(tagId, -1);
        if (index < 0) {
            throw invalidFIXMessageException.reset().append("No such tag: ").append(tagId);
        }
        return tags[index];
    }

    public FIXGroup fetchFIXGroup() {
        groupIndex++;
        if (groupIndex == groups.length) {
            groups = Arrays.copyOf(groups, groupIndex * 2);
            for (int i = groupIndex; i < groups.length; i++) {
                groups[i] = new FIXGroup();
            }
        }
        return groups[groupIndex];
    }

    private int getInt(FIXTag tag, int offset, int length) {
        int result = 0;
        byte[] msg = msgCache;
        for (int i = 0; i < length; i++) {
            byte b = msg[offset + i];
            if (b > Constants.NINE || b < Constants.ZERO) {
                throw invalidFIXMessageException.reset().append("Tag ").append(tag.getFIXId()).append(" is not numerical").done();
            }
            result = result * 10 + (b - Constants.ZERO);
        }
        return result;
    }

    public int getInt(FIXTag tag) {
        return getInt(tag, tag.getOffset(), tag.getLength());
    }

    public int getInt(int tagId) {
        return getInt(getTag(tagId));
    }

    public int getSignedInt(FIXTag tag) {
        byte b = msgCache[tag.getOffset()];
        if (b == Constants.SIGN) {
            return -getInt(tag, tag.getOffset() + 1, tag.getLength() - 1);
        }
        return getInt(tag);
    }

    public int getSignedInt(int tagId) {
        return getSignedInt(getTag(tagId));
    }

    private double getDouble(FIXTag tag, int offset, int length) {
        double result = 0;
        byte[] msg = msgCache;
        int i = 0;
        for (; i < length; i++) {
            byte b = msg[offset + i];
            if (b == '.') {
                break;
            }
            if (b > Constants.NINE || b < Constants.ZERO) {
                throw invalidFIXMessageException.reset().append("Tag ").append(tag.getFIXId()).append(" is not numerical").done();
            }
            result = result * 10 + (b - Constants.ZERO);
        }

        int divisor = 1;
        i++;
        for (; i < length; i++) {
            byte b = msg[offset + i];
            if (b > Constants.NINE || b < Constants.ZERO) {
                throw invalidFIXMessageException.reset().append("Tag ").append(tag.getFIXId()).append(" is not numerical").done();
            }
            result = result * 10 + (b - Constants.ZERO);
            divisor *= 10;
        }
        return result / divisor;
    }

    public double getDouble(FIXTag tag) {
        return getDouble(tag, tag.getOffset(), tag.getLength());
    }

    public double getDouble(int tagId) {
        return getDouble(getTag(tagId));
    }

    public double getSignedDouble(FIXTag tag) {
        byte b = msgCache[tag.getOffset()];
        if (b == Constants.SIGN) {
            return -getDouble(tag, tag.getOffset() + 1, tag.getLength() - 1);
        }
        return getDouble(tag);
    }

    public double getSignedDouble(int tagId) {
        return getSignedDouble(getTag(tagId));
    }

    public byte getByte(FIXTag tag) {
        if (tag.getLength() != 1) {
            throw invalidFIXMessageException.reset().append("Tag ").append(tag.getFIXId()).append(" is not 1 byte").done();
        }
        return msgCache[tag.getOffset()];
    }

    public byte getByte(int tagId) {
        return getByte(getTag(tagId));
    }

    public boolean getBoolean(FIXTag tag) {
        if (tag.getLength() != 1) {
            throw invalidFIXMessageException.reset().append("Tag ").append(tag.getFIXId()).append(" is not 1 byte").done();
        }
        byte b = msgCache[tag.getOffset()];
        return b == '1' || b == 'Y' || b == 'y';
    }

    public boolean getBoolean(int tagId) {
        return getBoolean(getTag(tagId));
    }

    public StringBuilder getString(FIXTag tag, StringBuilder sb) {
        sb.setLength(0);
        return appendTag(tag, sb);
    }

    public StringBuilder getString(int tagId, StringBuilder sb) {
        return getString(getTag(tagId), sb);
    }

    private StringBuilder appendTag(FIXTag tag, StringBuilder sb) {
        int offset = tag.getOffset();
        int length = tag.getLength();
        for (int i = 0; i < length; i++) {
            sb.append((char) msgCache[i + offset]);
        }
        return sb;
    }


    public void parseRepeatingGroup(Map<Integer, FIXGroupStructure> repeatingGroupStructures) {
        if (parsedRepeatingGroup || repeatingGroupStructures == null) {
            return;
        }
        for (int i = 0; i < tags.length; i++) {
            FIXTag tag = tags[i];
            if (!tag.isSet()) {
                break; // End
            }
            FIXGroupStructure structure = repeatingGroupStructures.get(tag.getFIXId());
            if (structure == null) {
                continue; // Not repeating group
            }
            FIXGroup group = fetchFIXGroup();
            addRepeatingGroup(tag.getFIXId(), group);
            i = handleRepeatingGroup(repeatingGroupStructures, structure, group, i, 1) - 1;
        }
        parsedRepeatingGroup = true;
    }

    private int handleRepeatingGroup(Map<Integer, FIXGroupStructure> repeatingGroupStructures, FIXGroupStructure structure, FIXGroup group, int index, int depth) {
        FIXTag tag = tags[index];
        int expectedGroupSize = getInt(tag);
        int header = structure.header;
        int[] validTagList = structure.tagList;
        FIXGroupElement element = null;
        int groupSize = 0;
        for (int i = index + 1; i < tags.length; i++) {
            tag = tags[i];
            tag.setDepth(depth);
            int tagId = tag.getFIXId();
            // Verify if the first element is expected tag
            if (i == index + 1 && tagId != header) {
                throw invalidFIXMessageException.reset().append("Invalid repeatingGroup with unknown header tag: ").append(tag.getFIXId()).append(" but we expect ").append(header).done();
            }
            // Start of a group element
            if (tagId == header) {
                groupSize++;
                element = group.nextElement();
            }
            // Exit repeatingGroup. Verify the group count
            if (Arrays.binarySearch(validTagList, tagId) < 0) {
                if (groupSize != expectedGroupSize) {
                    throw invalidFIXMessageException.reset().append("Invalid repeatingGroup with invalid group count, real count is ").append(groupSize).append(" but we expect ").append(expectedGroupSize).done();
                }
                tag.setDepth(depth - 1);
                return i;
            }
            // Inner repeatingGroup
            FIXGroupStructure innerStructure = repeatingGroupStructures.get(tagId);
            if (innerStructure != null) {
                FIXGroup innerGroup = fetchFIXGroup();
                element.addRepeatingGroup(tagId, innerGroup);
                i = handleRepeatingGroup(repeatingGroupStructures, innerStructure, innerGroup, i, depth + 1) - 1;
            } else {
                // Normal tags
                element.addTag(tagId, tag);
            }
        }
        return tags.length;
    }


    public void validate() {
        int bodyLenInMsg = getInt(getTag(FIXTags.BODYLEN.id()));
        if (bodyLenInMsg != bodyLen) {
            throw invalidFIXMessageException.append("Invalid FIX message with bodyLen ").append(bodyLen).append(" but expecting ").append(bodyLenInMsg).done();
        }
        int checkSumInMsg = getInt(getTag(FIXTags.CHECKSUM.id()));
        if (checkSumInMsg != checkSum) {
            throw invalidFIXMessageException.append("Invalid FIX message with checkSum ").append(checkSum).append(" but expecting ").append(checkSumInMsg).done();
        }
    }

    @Override
    public String toString() {
        sb.setLength(0);
        for (FIXTag tag : tags) {
            if (!tag.isSet()) {
                break;
            }
            int depth = tag.getDepth();
            for (int i = 0; i < depth; i++) {
                sb.append("\t");
            }
            sb.append(tag.getFIXId()).append(":");
            appendTag(tag, sb);
            sb.append("\n");
        }
        return sb.toString();
    }

    static class FIXTag {
        private int FIXId;
        private int offset;
        private int length;
        private int depth;
        private boolean isSet = false;

        public void set(int id, int offset, int len) {
            this.FIXId = id;
            this.offset = offset;
            this.length = len;
            this.depth = 0;
            isSet = true;
        }

        public int getOffset() {
            return offset;
        }

        public int getLength() {
            return length;
        }

        public int getFIXId() {
            return FIXId;
        }

        public boolean isSet() {
            return isSet;
        }

        public void reset() {
            isSet = false;
        }

        public void setDepth(int depth) {
            this.depth = depth;
        }

        public int getDepth() {
            return this.depth;
        }

    }

}
