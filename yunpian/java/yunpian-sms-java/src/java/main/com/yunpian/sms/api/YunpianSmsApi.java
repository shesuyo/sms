package com.yunpian.sms.api;


import com.yunpian.sms.api.exception.ApiException;
import com.yunpian.sms.api.result.GetUserInfoResult;
import com.yunpian.sms.api.result.SendSmsResult;
import com.yunpian.sms.api.util.JsonUtil;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 云片短信API
 * Created by jiacheo on 15/5/18.
 */
public class YunpianSmsApi {

  // 查账户信息的http地址
  private static String URI_GET_USER_INFO = ApiConfig.getUserInfoApi();

  //通用发送接口的http地址
  private static String URI_SEND_SMS = ApiConfig.getSendSmsApi();

  // 模板发送接口的http地址
  private static String URI_TPL_SEND_SMS = ApiConfig.getTplSendSmsApi();

  //编码格式。发送编码格式统一用UTF-8
  private static String ENCODING = ApiConfig.getEncoding();

  private static String API_KEY = ApiConfig.getApiKey();

  //thread safe
  private static final CloseableHttpClient httpClient = HttpClients.createDefault();

  /**
   * 取账户信息
   *
   * @return json格式字符串
   * @throws java.io.IOException
   */
  public static GetUserInfoResult getUserInfo() throws ApiException {
    Map<String, String> params = new HashMap<String, String>();
    params.put("apikey", API_KEY);
    try {
      String json = post(URI_GET_USER_INFO, params);
      return JsonUtil.fromJson(json, GetUserInfoResult.class);
    } catch (Exception e) {
      throw new ApiException("Invoke Api Failed", e);
    }
  }

  /**
   * 通用接口发短信
   *
   * @param text   　短信内容
   * @param mobile 　接受的手机号
   * @return json格式字符串
   * @throws IOException
   */
  public static SendSmsResult sendSms(String text, String mobile) throws ApiException {
    Map<String, String> params = new HashMap<String, String>();
    params.put("apikey", API_KEY);
    params.put("text", text);
    params.put("mobile", mobile);
    try {
      String post = post(URI_SEND_SMS, params);
      return JsonUtil.fromJson(post, SendSmsResult.class);
    } catch (Exception e) {
      throw new ApiException("Invoke Api Failed", e);
    }
  }

  /**
   * 通过模板发送短信(不推荐)
   *
   * @param tpl_id    　模板id
   * @param tpl_value 　模板变量值
   * @param mobile    　接受的手机号
   * @return json格式字符串
   * @throws IOException
   */
  public static SendSmsResult tplSendSms(long tpl_id, String tpl_value, String mobile) throws ApiException {
    Map<String, String> params = new HashMap<String, String>();
    params.put("apikey", API_KEY);
    params.put("tpl_id", String.valueOf(tpl_id));
    params.put("tpl_value", tpl_value);
    params.put("mobile", mobile);
    try {
      String post = post(URI_TPL_SEND_SMS, params);
      return JsonUtil.fromJson(post, SendSmsResult.class);
    } catch (Exception e) {
      throw new ApiException("Invoke Api Failed", e);
    }
  }

  /**
   * 基于HttpClient 4.3的通用POST方法
   *
   * @param url       提交的URL
   * @param paramsMap 提交<参数，值>Map
   * @return 提交响应
   */
  public static String post(String url, Map<String, String> paramsMap) throws Exception {
    //reuse httpclient to keepalive to the server
    //keepalive in https will save time on tcp handshaking.
    CloseableHttpClient client = httpClient;
    String responseText = "";
    HttpPost method = new HttpPost(url);
    CloseableHttpResponse response = null;
    try {
      if (paramsMap != null) {
        List<NameValuePair> paramList = new ArrayList<NameValuePair>();
        for (Map.Entry<String, String> param : paramsMap.entrySet()) {
          NameValuePair pair = new BasicNameValuePair(param.getKey(), param.getValue());
          paramList.add(pair);
        }
        method.setEntity(new UrlEncodedFormEntity(paramList, ENCODING));
      }
      response = client.execute(method);
      HttpEntity entity = response.getEntity();
      if (entity != null) {
        responseText = EntityUtils.toString(entity);
      }
    } finally {
      //must close the response or will lead to next request hang.
      if (response != null) {
        response.close();
      }
    }
    return responseText;
  }
}
