package com.ng_doanh.hr_management_system.common.security;

import com.ng_doanh.hr_management_system.auth.entity.User;
import com.ng_doanh.hr_management_system.auth.repository.UserRepository;
import com.ng_doanh.hr_management_system.common.enums.ResponseCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(ResponseCode.BAD_CREDENTIALS.getMessage()));

        return new CustomUserDetails(user);
    }
}
