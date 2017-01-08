package img;

import static java.lang.Math.abs;

/**
 * Performs color utility operations
 */
final class ColorUtil {
    /**
     * Returns percentage similarity between two pixel RGB values
     * @param aRGB: int
     * @param bRGB: int
     * @return float from 0 to 1 inclusive.
     */
    static float compareRGB(int aRGB, int bRGB){
        // this will get called a lot, trying to make this as
        // simple as possible.
        int diffSum;
        diffSum = abs(r(aRGB) - r(bRGB));
        diffSum += abs(g(aRGB) - g(bRGB));
        diffSum += abs(b(aRGB) - b(bRGB));
        return (float)diffSum / 765; // diffSum/(255*3) == average diff
    }

    static int r(int color){
        return (color >> 16) & 0xFF;
    }

    static int g(int color){
        return (color >> 8) & 0xFF;
    }

    static int b(int color){
        return color & 0xFF;
    }
}
