package com.leyou.sms.listener;

import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.leyou.sms.config.SmsProperties;
import com.leyou.sms.utils.SmsUtils;
import com.sun.xml.internal.ws.api.model.ExceptionType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Map;

/**
 * @author VvGnaK
 * @date 2020-03-13 10:16
 */
@Component
@EnableConfigurationProperties(SmsProperties.class)
public class SmsListener {

    @Autowired
    private SmsUtils smsUtils;
    @Autowired
    private SmsProperties prop;

//    @RabbitListener(bindings = @QueueBinding(
//            value = @Queue(value = "leyou.sms.queue", durable = "true"),
//            exchange = @Exchange(value = "leyou.sms.exchange",
//                    ignoreDeclarationExceptions = "true",type = ExchangeTypes.TOPIC),
//            key = {"verifycode.sms"}))
//    public void sendSms (Map<String, String> msg)  throws Exception{
//
//        if (CollectionUtils.isEmpty(msg)) {
//            return;
//        }
//        String phone = msg.get("phone");
//        String code = msg.get("code");
//        if (StringUtils.isNoneBlank(phone) && StringUtils.isNoneBlank(code)) {
//            Boolean sendSms = smsUtils.sendSms(phone, code, prop.getSignName(), prop.getVerifyCodeTemplate());
//
//        }
//
//    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "leyou.sms.queue", durable = "true"),
            exchange = @Exchange(value = "leyou.sms.exchange",
                    ignoreDeclarationExceptions = "true"),
            key = {"sms.verify.code"}))
    public void listenSms(Map<String, String> msg) throws Exception {
        if (msg == null || msg.size() <= 0) {
            // 放弃处理
            return;
        }
        String phone = msg.get("phone");
        String code = msg.get("code");

        if (StringUtils.isBlank(phone) || StringUtils.isBlank(code)) {
            // 放弃处理
            return;
        }
        // 发送消息
        SendSmsResponse resp = this.smsUtils.sendSms(phone, code,
                prop.getSignName(),
                prop.getVerifyCodeTemplate());

    }
}
