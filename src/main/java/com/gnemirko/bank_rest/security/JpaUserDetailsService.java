package com.gnemirko.bank_rest.security;

import com.gnemirko.bank_rest.entity.User;
import com.gnemirko.bank_rest.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JpaUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByName(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getName())
                .password(user.getPasswordHash())
                .authorities(new SimpleGrantedAuthority(user.getRole().name()))
                .disabled(!user.isEnabled())
                .build();
    }
}