package img;

import java.io.IOException;

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
    public Iterable<Integer> pixels(){
        return offsetPixels(0, 0);
    }
}
