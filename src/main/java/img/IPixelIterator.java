package img;

import java.util.Iterator;

/**
 * Common iterator interface used by Img subclasses to iterate over pixels
 */
interface IPixelIterator extends Iterator {
    Integer next();  // yields RGB value of a pixel
    boolean hasNonNullLeft();  // whether iterator has a non-null remaining
}
