package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.content.exception.BusinessException;
import com.xuecheng.content.model.entity.XcUser;
import com.xuecheng.content.model.vo.LoginVO;
import com.xuecheng.content.mapper.XcUserMapper;
import com.xuecheng.content.security.JwtUtil;
import com.xuecheng.content.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final XcUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public LoginVO login(String username, String password) {
        XcUser user = userMapper.selectOne(
                new LambdaQueryWrapper<XcUser>()
                        .eq(XcUser::getUsername, username)
        );
        if (user == null) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(403, "账号已禁用");
        }
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        String token = jwtUtil.generate(
                user.getUsername(),
                user.getId(),
                user.getRole(),
                user.getCompanyId()
        );
        return new LoginVO(
                token,
                user.getId(),
                user.getUsername(),
                user.getRealName(),
                user.getRole(),
                user.getCompanyId()
        );
    }
}
