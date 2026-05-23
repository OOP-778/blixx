package dev.oop778.blixx.api.parser.node;

import dev.oop778.blixx.api.Blixx;
import java.util.Iterator;

public interface BlixxNodeInternal extends BlixxNode {
    @Override
    BlixxNodeInternal copy();

    @Override
    BlixxNodeInternal getPrevious();

    @Override
    BlixxNodeInternal getNext();

    void setPrevious(BlixxNode previous);

    void setNext(BlixxNode next);

    void setBlixx(Blixx blixx);

    BlixxNodeInternal copyMe();

    BlixxNodeInternal findTreeEnd();

    Iterator<BlixxNodeInternal> iterator(boolean withSelf);
}
