package com.blog.utils;

import com.aliyun.captcha20230305.Client;
import com.aliyun.captcha20230305.models.VerifyIntelligentCaptchaRequest;
import com.aliyun.captcha20230305.models.VerifyIntelligentCaptchaResponse;
import com.aliyun.teaopenapi.models.Config;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 验证码工具类
 */
@Data
@AllArgsConstructor
public class AliyunAcsClient {
    private String accessKeyId;
    private String accessKeySecret;
    private String regionId;
    private String sceneId;

    private Client createClient() throws Exception {
        Config config = new Config()
                .setAccessKeyId(accessKeyId)
                .setAccessKeySecret(accessKeySecret);
        config.setEndpoint("captcha.cn-shanghai.aliyuncs.com");
        return new Client(config);
    }

    /**
     * 校验验证码凭证
     * @param captchaVerifyParam 前端传来的验证参数（JSON 字符串或 Base64 串）
     * @return true 验证通过，false 验证不通过
     */
    public boolean verifyCaptcha(String captchaVerifyParam) {
        try {
            Client client = this.createClient();
            VerifyIntelligentCaptchaRequest request = new VerifyIntelligentCaptchaRequest()
                    .setCaptchaVerifyParam(captchaVerifyParam)
                    .setSceneId(sceneId); // 设置场景ID
            VerifyIntelligentCaptchaResponse response = client.verifyIntelligentCaptcha(request);
            return response.getBody().getResult().getVerifyResult();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}

