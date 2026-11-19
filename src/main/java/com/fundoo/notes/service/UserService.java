package com.fundoo.notes.service;

import com.fundoo.notes.dto.request.LoginRequestDto;
import com.fundoo.notes.dto.request.UserRegisterRequestDto;
import com.fundoo.notes.dto.response.LoginResponseDto;
import com.fundoo.notes.dto.response.UserResponseDto;

/**
 * Business operations for user accounts.
 *
 * @author Navya Raj
 */
public interface UserService {

    UserResponseDto register(UserRegisterRequestDto requestDto);

    LoginResponseDto login(LoginRequestDto requestDto);
}
