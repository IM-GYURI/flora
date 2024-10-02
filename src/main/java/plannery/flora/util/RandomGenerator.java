package plannery.flora.util;

import java.util.Random;

public class RandomGenerator {

  private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
  private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
  private static final String DIGITS = "0123456789";
  private static final String SPECIAL = "!@#$%^&*";
  private static final String ALLCHARS =
      UPPERCASE + LOWERCASE + DIGITS + SPECIAL;
  private static final int PASSWORD_LENGTH = 12;

  public static String generateTemporaryPassword() {
    StringBuilder sb = new StringBuilder();

    sb.append(getRandomChar(UPPERCASE));
    sb.append(getRandomChar(LOWERCASE));
    sb.append(getRandomChar(DIGITS));
    sb.append(getRandomChar(SPECIAL));

    for (int i = 4; i < PASSWORD_LENGTH; i++) {
      sb.append(getRandomChar(ALLCHARS));
    }

    return sb.toString();
  }

  private static char getRandomChar(String chars) {
    return chars.charAt(new Random().nextInt(chars.length()));
  }
}
