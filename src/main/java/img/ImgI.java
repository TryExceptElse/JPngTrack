package img;

interface ImgI {
    int getWidth();
    int getHeight();
    IPixelIterable pixels();
    IPixelIterable offsetPixels(int xOffset, int yOffset);
}
