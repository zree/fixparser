package fix;

import java.util.Arrays;

public class FIXGroup {

    private FIXGroupElement[] elements = new FIXGroupElement[20];
    private int lastIndex = -1;

    public FIXGroup() {
        for (int i = 0; i < elements.length; i++) {
            elements[i] = new FIXGroupElement();
        }
    }

    public void reset() {
        for (int i = 0; i <= lastIndex; i++) {
            elements[i].reset();
        }
        lastIndex = -1;
    }

    public FIXGroupElement nextElement() {
        lastIndex++;
        if (lastIndex >= elements.length) {
            elements = Arrays.copyOf(elements, elements.length * 2);
            for (int i = lastIndex; i < elements.length; i++) {
                elements[i] = new FIXGroupElement();
            }
        }
        return elements[lastIndex];
    }

    public int getSize() {
        return lastIndex + 1;
    }

    private FIXGroupElement getElement(int index) {
        if (index > lastIndex) {
            return null;
        }
        return elements[index];
    }

    public FIXMessage.FIXTag getTag(int index, int tagId) {
        FIXGroupElement element = getElement(index);
        if(element==null) {
            return null;
        }
        return element.getTag(tagId);
    }

    public FIXGroup getRepeatingGroup(int index, int tagId){
        FIXGroupElement element = getElement(index);
        if(element == null) {
            return null;
        }
        return element.getRepeatingGroup(tagId);
    }
}
