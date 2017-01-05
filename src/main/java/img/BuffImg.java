package img;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import javax.imageio.ImageIO;

class BuffImg extends Img {
    private BufferedImage imgSource;

    private BuffImg(BufferedImage imgSource){
        this.imgSource = imgSource;
    }

    /**
     * Makes new BuffImg from passed String path
     * @param path: String
     * @return BuffImg
     * @throws IOException: On failure to read file path
     */
    static BuffImg BuffImgFromPath(String path) throws IOException {
        return new BuffImg(ImageIO.read(new File(path)));
    }

    public int getWidth() {
        return imgSource.getWidth();
    }

    public int getHeight() {
        return imgSource.getHeight();
    }

    public Iterable<Integer> offsetPixels(int xOffset, int yOffset) {
        return new Pixels(imgSource, xOffset, yOffset);
    }

    /**
     * Class used as iterable to allow iteration over pixels of Img
     */
    private class Pixels implements Iterable<Integer>{
        BufferedImage src;
        int xOffset;
        int yOffset;

        private Pixels(BufferedImage src, int xOffset, int yOffset){
            this.src = src;
            this.xOffset = xOffset;
            this.yOffset = yOffset;
        }

        /**
         * Iterates over each pixel in src image
         * @return Iterator
         */
        public Iterator<Integer> iterator() {
            return new PixelIterator(src, xOffset, yOffset);
        }

        /**
         * Iterator for Pixels Iterable. Returns each pixel in image
         */
        private class PixelIterator implements Iterator<Integer>{
            BufferedImage src;
            int srcWidth;
            int srcHeight;
            int xOffset;
            int yOffset;
            int xIndex;
            int yIndex;

            private PixelIterator(
                    BufferedImage src,
                    int xOffset,
                    int yOffset
                ){
                this.src = src;
                this.srcHeight = src.getHeight();
                this.srcWidth = src.getWidth();
                this.xOffset = xOffset;
                this.yOffset = yOffset;
                this.xIndex = 0;
                this.yIndex = 0;
            }

            /**
             * Returns bool of whether this iterator has another pixel
             * value to yield.
             * @return boolean
             */
            public boolean hasNext() {
                return xIndex < src.getWidth() && yIndex < src.getHeight();
            }

            /**
             * yields RGB value of next pixel in img
             * @return int
             */
            public Integer next() {
                int pixelX = xOffset + xIndex;
                int pixelY = yOffset + yIndex;
                // if pixel X and Y are outside bounds of image:
                //      return null
                if (
                        (pixelX < 0 || pixelX >= srcWidth) ||
                        (pixelY < 0 || pixelY >= srcHeight)
                    ){
                    return null;
                }
                // otherwise, get rgb value of pixel at position x,y
                return src.getRGB(pixelX, pixelY);
            }
        }
    }
}
