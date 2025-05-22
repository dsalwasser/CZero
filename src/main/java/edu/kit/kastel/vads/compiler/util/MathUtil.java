package edu.kit.kastel.vads.compiler.util;

public final class MathUtil {

  public static boolean isPowerOfTwo(int x) {
    return x > 0 && (x & (x - 1)) == 0;
  }

  public static int getPowerOfTwoExponent(int x) {
    return Integer.numberOfTrailingZeros(x);
  }
}
