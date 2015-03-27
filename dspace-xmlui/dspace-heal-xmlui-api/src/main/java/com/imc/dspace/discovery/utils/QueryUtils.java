package com.imc.dspace.discovery.utils;

import java.text.Normalizer;

public class QueryUtils {

    private static String stripGreekAccents(String s) {

        StringBuilder sb = new StringBuilder(s);

        for(int i = 0; i < s.length(); i++) {
            Character c = greekLowerCase(sb.charAt(i));
            if(c != null) {
                sb.setCharAt(i, c.charValue());
            }
        }

        return sb.toString();
    }

    private static String stripAccents(String s) {
        s = Normalizer.normalize(s, Normalizer.Form.NFD);
        s = s.replaceAll("[^\\p{InCombiningDiacriticalMarks}]", "");
        return s;
    }

    private static Character greekLowerCase(Character codepoint) {
        switch(codepoint) {
      /* There are two lowercase forms of sigma:
       *   U+03C2: small final sigma (end of word)
       *   U+03C3: small sigma (otherwise)
       *
       * Standardize both to U+03C3
       */
            case '\u03C2': /* small final sigma */
                return '\u03C3'; /* small sigma */

      /* Some greek characters contain diacritics.
       * This filter removes these, converting to the lowercase base form.
       */

            case '\u0386': /* capital alpha with tonos */
            case '\u03AC': /* small alpha with tonos */
                return '\u03B1'; /* small alpha */

            case '\u0388': /* capital epsilon with tonos */
            case '\u03AD': /* small epsilon with tonos */
                return '\u03B5'; /* small epsilon */

            case '\u0389': /* capital eta with tonos */
            case '\u03AE': /* small eta with tonos */
                return '\u03B7'; /* small eta */

            case '\u038A': /* capital iota with tonos */
            case '\u03AA': /* capital iota with dialytika */
            case '\u03AF': /* small iota with tonos */
            case '\u03CA': /* small iota with dialytika */
            case '\u0390': /* small iota with dialytika and tonos */
                return '\u03B9'; /* small iota */

            case '\u038E': /* capital upsilon with tonos */
            case '\u03AB': /* capital upsilon with dialytika */
            case '\u03CD': /* small upsilon with tonos */
            case '\u03CB': /* small upsilon with dialytika */
            case '\u03B0': /* small upsilon with dialytika and tonos */
                return '\u03C5'; /* small upsilon */

            case '\u038C': /* capital omicron with tonos */
            case '\u03CC': /* small omicron with tonos */
                return '\u03BF'; /* small omicron */

            case '\u038F': /* capital omega with tonos */
            case '\u03CE': /* small omega with tonos */
                return '\u03C9'; /* small omega */

      /* The previous implementation did the conversion below.
       * Only implemented for backwards compatibility with old indexes.
       */

            case '\u03A2': /* reserved */
                return '\u03C2'; /* small final sigma */

            default:
                return Character.toLowerCase(codepoint);
        }
    }

    public static String preProcessDiscoveryQuery(String query) {
        return stripGreekAccents(query.toLowerCase());
    }

    public static void main(String[] args) {
        String s = "Η Αναζήτηση της Υπατίας";
        System.out.println(preProcessDiscoveryQuery(s));
    }
}
