package org.rsverchk.ahocorasick;

/**
 * A function used to transform a character before adding/searching, for example toLowerCase.
 *
 * @author Ruslan Sverchkov
 */
@FunctionalInterface
public interface CharConverter {

    /**
     * Transform a character before adding/searching.
     *
     * @param c character to transform
     * @return transformed character
     */
    char convert(char c);

}