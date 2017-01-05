package img;

public interface ImgI {
    int getWidth();
    int getHeight();
    Iterable<Integer> pixels();
    Iterable<Integer> offsetPixels(int xOffset, int yOffset);
}
