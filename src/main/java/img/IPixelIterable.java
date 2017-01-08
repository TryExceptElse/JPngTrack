package img;

/**
 * Iterable returning pixel RGB values
 */
interface IPixelIterable extends Iterable{
    IPixelIterator iterator();
}
