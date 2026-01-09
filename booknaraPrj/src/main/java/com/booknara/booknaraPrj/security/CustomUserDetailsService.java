package com.booknara.booknaraPrj.security;

import com.booknara.booknaraPrj.login_signup.User;
import com.booknara.booknaraPrj.login_signup.mapper.UserMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserMapper userMapper;

    public CustomUserDetailsService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String userId)
            throws UsernameNotFoundException {

        User user = userMapper.findByUserId(userId);

        if (user == null) {
            throw new UsernameNotFoundException("사용자 없음");
        }

        return new CustomUserDetails(user);


    }
}
