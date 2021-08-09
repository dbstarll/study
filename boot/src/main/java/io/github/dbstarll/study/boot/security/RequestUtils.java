package io.github.dbstarll.study.boot.security;

import javax.servlet.http.HttpServletRequest;
import java.util.regex.Pattern;

public final class RequestUtils {
  private static final Pattern internalProxies = Pattern.compile(
          "10\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}|" + "192\\.168\\.\\d{1,3}\\.\\d{1,3}|" + "169\\.254\\.\\d{1,3}\\.\\d{1,3}|"
                  + "127\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}|" + "172\\.1[6-9]{1}\\.\\d{1,3}\\.\\d{1,3}|"
                  + "172\\.2[0-9]{1}\\.\\d{1,3}\\.\\d{1,3}|" + "172\\.3[0-1]{1}\\.\\d{1,3}\\.\\d{1,3}");

  private RequestUtils() {
    // 禁止实例化
  }

  public static boolean isInternal(HttpServletRequest request) {
    return isInternal(request.getRemoteAddr());
  }

  public static boolean isInternal(String remoteAddr) {
    return internalProxies.matcher(remoteAddr).matches();
  }
}
