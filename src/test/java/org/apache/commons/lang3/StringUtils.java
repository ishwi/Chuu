//package org.apache.commons.lang3;
//
//public class StringUtils {
//
//    public static String abbreviate(String a, int size) {
//        return a;
//    }
//
//    public static boolean isEmpty(final CharSequence cs) {
//        return cs == null || cs.length() == 0;
//    }
//
//    public static int length(final CharSequence cs) {
//        return cs == null ? 0 : cs.length();
//    }
//
//    public static String capitalize(final String str) {
//        final int strLen = length(str);
//        if (strLen == 0) {
//            return str;
//        }
//
//        final int firstCodepoint = str.codePointAt(0);
//        final int newCodePoint = Character.toTitleCase(firstCodepoint);
//        if (firstCodepoint == newCodePoint) {
//            // already capitalized
//            return str;
//        }
//
//        final int[] newCodePoints = new int[strLen]; // cannot be longer than the char array
//        int outOffset = 0;
//        newCodePoints[outOffset++] = newCodePoint; // copy the first codepoint
//        for (int inOffset = Character.charCount(firstCodepoint); inOffset < strLen; ) {
//            final int codepoint = str.codePointAt(inOffset);
//            newCodePoints[outOffset++] = codepoint; // copy the remaining ones
//            inOffset += Character.charCount(codepoint);
//        }
//        return new String(newCodePoints, 0, outOffset);
//    }
//}
