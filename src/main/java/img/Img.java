package img;

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Class handling pixel accessing and other methods for a passed image
 */
public abstract class Img implements ImgI{
    /**
     * Factory method taking a String path and returning an Img
     * @return Img
     */
    public static Img fromPath(String path) throws IOException{
        return BuffImg.BuffImgFromPath(path);
    }

    /**
     * Gets iterator for iterating over pixels (as rgb)
     * @return Iterable
     */
    public IPixelIterable pixels(){
        return offsetPixels(0, 0);
    }

    /**
     * Gets apparent motion between this Img and another
     * @param other: Img
     * @param xGate: float
     * @param yGate: float
     * @return float[2] (x, y)
     */
    public float[] appMotion(Img other, float xGate, float yGate){
        return null;
    }

    /**
     * Checks that offset passed is smaller than size of Img.
     * If not, raises a ValueError
     * @param xOffset: int
     * @param yOffset: int
     */
    void validateOffsets(int xOffset, int yOffset){
        if (xOffset > getWidth() || yOffset > getHeight()){
            throw new IllegalArgumentException(String.format(
                    "Passed offset is greater than size of image." +
                            "Offsets: %s, %s  Image size: %s, %s",
                    xOffset, yOffset, getWidth(), getHeight()
            ));
        }
    }

    /**
     * Tool for iterating over corresponding pairs of pixels in
     * two images.
     * This class' iterator yields pairs of integers, each representing
     * the rgb values of each pixel.
     */
    private final class PixelPairs implements Iterable<int[]>{

        IPixelIterable aIterable;
        IPixelIterable bIterable;

        /**
         * Constructor for PixelPairs iterable. This takes an A image
         * and a B image, along with an offset
         * @param imgA: Img
         * @param imgB: Img
         * @param xOffset: int
         * @param yOffset: int
         */
        private PixelPairs(Img imgA, Img imgB, int xOffset, int yOffset){
            if (imgA.getWidth() != imgB.getWidth() ||
                    imgA.getHeight() != imgB.getWidth()){
                throw new IllegalArgumentException(String.format(
                        "Image A and Image B are of different sizes: " +
                                "A: (%s, %s)  B: (%s, %s)",
                        imgA.getWidth(), imgA.getHeight(),
                        imgB.getWidth(), imgB.getHeight()
                ));
            }
            aIterable = imgA.offsetPixels(xOffset, yOffset);
            bIterable = imgB.pixels();
        }

        public Iterator<int[]> iterator(){
            return new PixelPairIterator(aIterable, bIterable);
        }

        class PixelPairIterator implements Iterator<int[]>{

            IPixelIterator aIterator;
            IPixelIterator bIterator;

            private PixelPairIterator(
                    IPixelIterable aIterable,
                    IPixelIterable bIterable
            ){
                aIterator = aIterable.iterator();
                bIterator = bIterable.iterator();
            }

            public boolean hasNext(){
                return aIterator.hasNext() && bIterator.hasNonNullLeft();
            }

            public int[] next(){
                Integer aRGB;
                Integer bRGB;
                // loop until return
                if (!bIterator.hasNonNullLeft() || !aIterator.hasNext()){
                    // if there is no non-null left, raise iterator error
                    throw new NoSuchElementException();
                }
                // otherwise, iterate through iterators until a
                // non-null pair is found.
                do {
                    aRGB = aIterator.next();
                    bRGB = bIterator.next();
                } while (bRGB == null);
                return new int[] {aRGB, bRGB};
            }
        }
    }
}
