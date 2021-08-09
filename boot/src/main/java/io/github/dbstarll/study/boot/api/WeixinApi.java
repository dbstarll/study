package io.github.dbstarll.study.boot.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.dbstarll.utils.http.client.request.RelativeUriResolver;
import io.github.dbstarll.utils.json.jackson.JsonApiClient;
import io.github.dbstarll.utils.net.api.ApiException;
import io.github.dbstarll.utils.net.api.ApiParameterException;
import io.github.dbstarll.utils.net.api.ApiResponseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang3.Validate.notBlank;
import static org.apache.commons.lang3.Validate.notNull;

public class WeixinApi extends JsonApiClient {
  private final Map<String, String> secrets;

  /**
   * 构造WeixinApi.
   *
   * @param httpClient   httpClient
   * @param objectMapper objectMapper
   * @param secrets      secrets
   */
  public WeixinApi(HttpClient httpClient, ObjectMapper objectMapper, Map<String, String> secrets) {
    super(httpClient, objectMapper);
    this.secrets = new HashMap<>(notNull(secrets, "secrets is null"));
    setUriResolver(new RelativeUriResolver("https://api.weixin.qq.com"));
  }

  /**
   * 登录凭证校验.
   *
   * @param appId 小程序 appId
   * @param code  登录时获取的 code
   * @return 登录凭证
   * @throws IOException  in case of a problem or the connection was aborted
   * @throws ApiException in case of an api error
   */
  public ObjectNode session(String appId, String code) throws IOException, ApiException {
    final HttpUriRequest request;
    try {
      request = post("/sns/jscode2session").addParameter("appid", notBlank(appId, "appId not set"))
              .addParameter("secret", notBlank(secrets.get(appId), "secret not found"))
              .addParameter("js_code", notBlank(code, "code not set")).addParameter("grant_type", "authorization_code")
              .build();
    } catch (RuntimeException e) {
      throw new ApiParameterException(e);
    }
    final ObjectNode res = execute(request, ObjectNode.class);
    final int errcode = res.path("errcode").asInt(0);
    if (errcode != 0) {
      throw new ApiResponseException(errcode, res.path("errmsg").asText());
    }
    return res;
  }
}
