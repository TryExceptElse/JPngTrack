package img;

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.stream.StreamSupport;

import static java.lang.Math.abs;
import static java.lang.Math.max;

/**
 * Class handling pixel accessing and other methods for a passed image
 */
public abstract class Img implements ImgI{
    // create object used as a lock for multithreaded processes
    private final static Object evaluationLock = new Object();

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
        if (getHeight() != getHeight() || getWidth() != getWidth()){
            throw new IllegalArgumentException(String.format(
                    "images to compare for motion are of different size." +
                    "(%s, %s) vs (%s, %s)",
                    getWidth(), getHeight(),
                    other.getWidth(), other.getHeight()
            ));
        }
        EvaluationData data = new EvaluationData(other, xGate, yGate);
        // work in parallel w to find the best x and y match.
        int nProcessors = data.nLiveThreads =
                Runtime.getRuntime().availableProcessors();
        Thread[] threads = new Thread[nProcessors];
        for (int i = 0; i < nProcessors; i++){
            threads[i] = new Thread(() -> evaluate(data));
            threads[i].start();
        }
        // wait for lock object to be released by all threads evaluating offsets
        synchronized(evaluationLock) {
            try {
                evaluationLock.wait();
            } catch (InterruptedException ignore) {}
        }
        return data.bestOffset;
    }

    /**
     * Evaluation loop that is run by each thread evaluating
     * different offsets.
     * @param data
     */
    private void evaluate(EvaluationData data){
        int[] offset; // current x, y offset
        // while evaluation is underway...
        while (data.offsetIterator.hasNext()){
            // first get next offset to test from iterator
            synchronized (data.offsetIterator){
                // two threads should not simultaneously ask for a new
                // pair of offsets.
                offset = data.offsetIterator.next(); // x and y offset
            }
            evaluateOffset(data, offset);
        }
        // if iterator has no further position inside bound,
        // decrement number of live threads
        data.nLiveThreads --;
        if (data.nLiveThreads <= 0){
            // if there are now no threads left running,
            // unlock evaluation lock.
            synchronized (evaluationLock){
                evaluationLock.notify();
            }
        }
    }

    /**
     * Evaluates a single pair of x, y offsets to see what the
     * difference ratio is.
     * @param data: EvaluationData
     * @param offset: int[] x, y
     */
    private void evaluateOffset(EvaluationData data, int[] offset){
        assert offset.length == 2;
        float diff = diffAtOffset(
                data.img,
                offset[0],
                offset[1],
                data.leastDiff
        );
        if (diff < data.leastDiff){
            data.leastDiff = diff;
            data.bestOffset =
                    new float[] {(float) offset[0], (float) offset[1]};
        }
    }

    /**
     * Yields spiraling coordinate values
     * @return Iterable returning int[]
     */
    private Iterable<int[]> spiralCoordinates(){
        return new SpiralCoordinates(getWidth(), getHeight());
    }

    /**
     * Returns percentage similarity between two images with a
     * given offset.
     * if difference is greater than exclusionVal,
     * returns 1. (max difference)
     * This can speed up operations that only need to test if
     * difference is less than some value-to-beat.
     * @param otherImg: Img
     * @param xOffset: int
     * @param yOffset: int
     * @param exclusionVal: float
     * @return float
     */
    private float diffAtOffset(
            Img otherImg,
            int xOffset,
            int yOffset,
            float exclusionVal
        ){
        float cumulativeDiff = 0f;
        int nPixels = (getWidth() - abs(xOffset)) * (getHeight() - abs(yOffset));
        for (int[] colorPair : pixelPairs(otherImg, xOffset, yOffset)){
            cumulativeDiff += ColorUtil.compareRGB(colorPair[0], colorPair[1]);
            if (cumulativeDiff / nPixels > exclusionVal){
                return 1f;
            }
        }
        return cumulativeDiff / nPixels;
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

    private Iterable<int[]> pixelPairs(Img otherImg, int xOffset, int yOffset){
        return new PixelPairs(this, otherImg, xOffset, yOffset);
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
                return aIterator.hasNonNullLeft() && bIterator.hasNext();
            }

            public int[] next(){
                Integer aRGB;
                Integer bRGB;
                // loop until return
                if (!aIterator.hasNonNullLeft() || !bIterator.hasNext()){
                    // if there is no non-null left, raise iterator error
                    throw new NoSuchElementException();
                }
                // otherwise, iterate through iterators until a
                // non-null pair is found.
                do {
                    aRGB = aIterator.next();
                    bRGB = bIterator.next();
                } while (aRGB == null);
                return new int[] {aRGB, bRGB};
            }
        }
    }

    private class SpiralCoordinates implements Iterable<int[]>{
        private final int width, height;

        /**
         * Constructs spiral iterable.
         * @param width: int
         */
        private SpiralCoordinates(int width, int height){
            this.width = width;this.height = height;
        }

        public Iterator<int[]> iterator(){
            return new SpiralIterator(width, height);
        }

        private class SpiralIterator implements Iterator<int[]>{
            final int height;     // height of area to iterate in
            final int width;      // width of area
            int r;          // maximum radius of spiral
            int x;          // current x position
            int y;          // current y position
            int dx;         // x motion
            int dy;         // y motion

            private SpiralIterator(int width, int height){
                this.width = width;
                this.height = height;
                x = y = 0;
                dx = 0;
                dy = -1;
                // set radius. Feels like there should be a simpler way
                r = (int)Math.ceil((double)max(width, height)/2);
            }

            public boolean hasNext(){
                return x <= r && y <= r;
            }

            public int[] next(){
                // if position x,y is outside bounds:
                if (abs(x) > width / 2 || abs(y) > height / 2){
                    // change current position, depending on dx, dy
                    if (dx != 0){
                        y = x * dx;
                        dx = dx * -1;
                    } else if (dy == 1){
                        x = -y;
                        dy = -1;
                    } else {
                        x = -y + 1;
                        dy = 1;
                    }
                    // increment position
                    x += dx;
                    y += dy;
                    // check that position is within bounds
                    if (abs(x) > r || abs(y) > r){
                        throw new NoSuchElementException();
                    }
                }
                int[] pos = {x, y};
                // check if direction needs to be changed
                if ((x == y )                       // lower left, upper right corners
                        || (x < 0 && x == -y) ||    // upper left corner
                        (x > 0 && x == 1 - y)) {    // lower right, increment radius
                    int dx1 = dy * -1;   // change directions
                    dy = dx;
                    dx = dx1;
                }
                x += dx;
                y += dy;
                return pos;
            }
        }
    }

    /** Holds data used to calculate difference fraction at offset in parallel */
    private class EvaluationData {
        private float leastDiff;
        private float[] bestOffset;
        private final Iterator<int[]> offsetIterator;
        private final Img img;
        private int nLiveThreads;

        private EvaluationData(Img comparisonImg, float xGate, float yGate){
            leastDiff = 1f;
            img = comparisonImg;
            offsetIterator = new SpiralCoordinates(
                    (int)xGate * 2,
                    (int)yGate * 2
            ).iterator();
        }
    }
}
