package com.leyou.user.service;

import com.leyou.common.utils.NumberUtils;
import com.leyou.user.mapper.UserMapper;
import com.leyou.user.pojo.User;
import com.leyou.user.utils.CodecUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author VvGnaK
 * @date 2020-03-11 17:16
 */
@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private AmqpTemplate amqpTemplate;

    @Autowired
    private StringRedisTemplate redisTemplate;

    static final String KEY_PREFIX = "user:code:phone:";

    static final Logger logger = LoggerFactory.getLogger(UserService.class);

    /**
     * 校验用户名和手机号
     * @param data
     * @param type
     * @return
     */
    public Boolean checkUserData(String data, Integer type) {

        User record = new User();
        if (type == 1) {
            record.setUsername(data);
        } else if (type == 2) {
            record.setPhone(data);
        } else {
            return null;
        }

        return userMapper.selectCount(record) == 0;
    }

    /**
     * 发送短信验证码
     * @param phone
     */
    public void sendVerifyCode(String phone) {

        if (StringUtils.isBlank(phone)) {
            return;
        }
        //生成验证码
        String code = NumberUtils.generateCode(6);

        Map<String,String> msg = new HashMap<>();
        msg.put("phone",phone);
        msg.put("code",code);
        amqpTemplate.convertAndSend("leyou.sms.exchange","verifycode.sms",msg);

        redisTemplate.opsForValue().set(KEY_PREFIX + phone,code,5, TimeUnit.MINUTES);
//        String ss = redisTemplate.opsForValue().get(KEY_PREFIX + phone);
//        System.out.println(ss);
    }




//    public Boolean sendVerifyCode(String phone) {
//        // 生成验证码
//        String code = NumberUtils.generateCode(6);
//        try {
//            // 发送短信
//            Map<String, String> msg = new HashMap<>();
//            msg.put("phone", phone);
//            msg.put("code", code);
//            this.amqpTemplate.convertAndSend("leyou.sms.exchange", "sms.verify.code", msg);
//            // 将code存入redis
//            this.redisTemplate.opsForValue().set(KEY_PREFIX + phone, code, 5, TimeUnit.MINUTES);
//            return true;
//        } catch (Exception e) {
//            logger.error("发送短信失败。phone：{}， code：{}", phone, code);
//            return false;
//        }
//    }

    /**
     * 用户注册
     * @param user
     * @param code
     * @return
     */
    public void register(User user,String code) {
        //校验验证码

        String cacheCode = this.redisTemplate.opsForValue().get(KEY_PREFIX + user.getPhone());
//        System.out.println(user.getPhone());
//        System.out.println(cacheCode);
        if (!StringUtils.equals(code,cacheCode)) {
            return ;
        }

        //获取盐
        String salt = CodecUtils.generateSalt();
        user.setPassword(CodecUtils.md5Hex(user.getPassword(),salt));
        //新增用户
        user.setId(null);
        user.setSalt(salt);
        user.setCreated(new Date());
        userMapper.insertSelective(user);
        System.out.println(user.toString());
        this.redisTemplate.delete(KEY_PREFIX + user.getPhone());
    }

    /**
     * 根据用户名密码查询用户
     * @param username
     * @param password
     * @return
     */
    public User query(String username, String password) {

        User record = new User();
        record.setUsername(username);
        User user = userMapper.selectOne(record);
        if (user == null) {
            return null;
        }
        if (!user.getPassword().equals(CodecUtils.md5Hex(password,user.getSalt()))) {
            return null;
        }
        return user;

    }
}
