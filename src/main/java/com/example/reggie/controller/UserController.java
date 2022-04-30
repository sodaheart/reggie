package com.example.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.reggie.common.R;
import com.example.reggie.model.User;
import com.example.reggie.service.UserService;
import com.example.reggie.utils.SMSUtils;
import com.example.reggie.utils.ValidateCodeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private RedisTemplate redisTemplate;
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session){
        String phone = user.getPhone();
        if(phone!=null){
//            String code = ValidateCodeUtils.generateValidateCode(4).toString();
//            SMSUtils.sendMessage("瑞吉外卖","",phone,code);
            // 成本控制：4位验证码就是手机号后4位
            String code = phone.substring(7);

            // 往session写入 手机号-验证码 键值对结构
//            session.setAttribute(phone,code);

            // 往redis中缓存，时间为5分钟
            redisTemplate.opsForValue().set(phone,code,5, TimeUnit.MINUTES);
            System.out.println(code);
            return R.success("验证码发送成功");
        }
        return R.error("验证码发送失败");
    }
    @PostMapping("/login")
    public R<User> login(@RequestBody Map map,HttpSession session){
        //获取手机号 - 用户提交
        String phone = map.get("phone").toString();
        //获取验证码 - 用户提交
        String code = map.get("code").toString();

        //从Session中获取保存的验证码
//        Object codeInSession = session.getAttribute(phone);

        // 改为从redis中获取验证码
        Object codeInSession = redisTemplate.opsForValue().get(phone);

        // 进行验证码的比对（页面提交的验证码和Session中保存的验证码比对）
        if(codeInSession != null && codeInSession.equals(code)){
            // 如果能够比对成功，说明登录成功

            // 删除存在redis验证码
            redisTemplate.delete(phone);

            // 查询用户
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone,phone);
            User user = userService.getOne(queryWrapper);
            if(user == null){
                //判断当前手机号对应的用户是否为新用户，如果是新用户就自动完成注册
                user = new User();
                user.setPhone(phone);
                user.setStatus(1);
                userService.save(user);
            }
            // 将用户信息写入session中
            session.setAttribute("user",user.getId());
            return R.success(user);
        }
        return R.error("登录失败");
    }
}
