package com.kkk.op.support.bean;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.RateLimiter;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * IP限流，使用 Guava Cache & RateLimiter
 *
 * @author KaiKoo
 */
@Slf4j
public class IPControlInterceptor implements HandlerInterceptor {

  /** 限流开关 */
  private boolean controlSwitch;

  public IPControlInterceptor(boolean controlSwitch) {
    this.controlSwitch = controlSwitch;
  }

  private static final LoadingCache<String, RateLimiter> CACHE =
      CacheBuilder.newBuilder()
          .expireAfterAccess(10L, TimeUnit.SECONDS)
          // 并发级别是指可以同时写缓存的线程数，设置为cpu核心数
          .concurrencyLevel(Runtime.getRuntime().availableProcessors())
          // 设置为软引用，在内存不足时回收缓存
          .softValues()
          // 需要设置一个合适的初始容量，因为扩容消耗很大
          .initialCapacity(1 << 10)
          // 需要设置最大容量，软引用对象数量不能太多，对性能有影响
          .maximumSize(1 << 14)
          // 使用Supplier初始化RateLimiter，限制每秒访问一次
          .build(CacheLoader.from(() -> RateLimiter.create(1D)));

  // 对写请求做IP限流
  private static final List<? extends HttpMethod> METHODS =
      ImmutableList.of(HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE, HttpMethod.PATCH);

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {
    // 判断限流开关是否打开
    if (!controlSwitch) {
      return true;
    }
    // 判断请求方式
    var method = request.getMethod();
    if (!METHODS.contains(HttpMethod.valueOf(method))) {
      return true;
    }
    log.info("IP Control cache size:{}", CACHE.size());
    var ip = getRealIp(request);
    // 判断是否流量超出
    if (CACHE.get(ip).tryAcquire()) {
      return true;
    }
    log.warn("block {}({}:{}) by IPControl!", ip, method, request.getRequestURI());
    // 返回错误信息
    response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value()); // 429
    response.setHeader("Content-Type", "text/plain;charset=UTF-8");
    try (var out = response.getWriter()) {
      out.write("请您歇会再试！");
      out.flush();
    }
    return false;
  }

  private static String getRealIp(HttpServletRequest request) {
    var ip = request.getHeader("x-forwarded-for");
    if (isIPValid(ip)) {
      // 多次反向代理后会有多个ip值，第一个ip才是真实ip
      ip = ip.split(",")[0];
    }
    if (isIPValid(ip)) {
      return ip;
    }
    ip = request.getHeader("Proxy-Client-IP");
    if (isIPValid(ip)) {
      return ip;
    }
    ip = request.getHeader("WL-Proxy-Client-IP");
    if (isIPValid(ip)) {
      return ip;
    }
    ip = request.getHeader("HTTP_CLIENT_IP");
    if (isIPValid(ip)) {
      return ip;
    }
    ip = request.getHeader("HTTP_X_FORWARDED_FOR");
    if (isIPValid(ip)) {
      return ip;
    }
    ip = request.getHeader("X-Real-IP");
    if (isIPValid(ip)) {
      return ip;
    }
    ip = request.getRemoteAddr();
    return ip.equals("0:0:0:0:0:0:0:1") ? "127.0.0.1" : ip;
  }

  // 判断IP是否合法，仅简单判断
  private static boolean isIPValid(String ip) {
    return !(ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip));
  }
}
