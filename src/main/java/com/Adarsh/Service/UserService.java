package com.Adarsh.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import com.Adarsh.Entity.User;
import com.Adarsh.Repository.UserRepository;
import com.Adarsh.dto.EditProfileDto;
import com.Adarsh.dto.LoginDto;
import com.Adarsh.dto.RegisterDto;
import com.Adarsh.util.EmailUtil;
import com.Adarsh.util.OtpUtil;
import jakarta.mail.MessagingException;

@Service
public class UserService {

  @Autowired
  private OtpUtil otpUtil;
  @Autowired
  private EmailUtil emailUtil;
  @Autowired
  private UserRepository userRepository;
  
  
  

  public String register(RegisterDto registerDto) {
    String otp = otpUtil.generateOtp();
    try {
      emailUtil.sendOtpEmail(registerDto.getEmail(), otp);
    } catch (MessagingException e) {
      throw new RuntimeException("Unable to send otp please try again");
    }
    User user = new User();
    user.setName(registerDto.getName());
    user.setEmail(registerDto.getEmail());
    user.setPassword(registerDto.getPassword());
    user.setOtp(otp);
    user.setOtpGeneratedTime(LocalDateTime.now());
    userRepository.save(user);
    return "User registration successful";
  }

  public String verifyAccount(String email, String otp) {
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new RuntimeException("User not found with this email: " + email));
    if (user.getOtp().equals(otp) && Duration.between(user.getOtpGeneratedTime(),
        LocalDateTime.now()).getSeconds() < (1 * 60)) {
      user.setActive(true);
      userRepository.save(user);
      return "OTP verified you can login";
    }
    return "Please regenerate otp and try again";
  }

  public String regenerateOtp(String email) {
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new RuntimeException("User not found with this email: " + email));
    String otp = otpUtil.generateOtp();
    try {
      emailUtil.sendOtpEmail(email, otp);
    } catch (MessagingException e) {
      throw new RuntimeException("Unable to send otp please try again");
    }
    user.setOtp(otp);
    user.setOtpGeneratedTime(LocalDateTime.now());
    userRepository.save(user);
    return "Email sent... please verify account within 1 minute";
  }

  public String login(LoginDto loginDto) {
    User user = userRepository.findByEmail(loginDto.getEmail())
        .orElseThrow(
            () -> new RuntimeException("User not found with this email: " + loginDto.getEmail()));
    if (!loginDto.getPassword().equals(user.getPassword())) {
      return "Password is incorrect";
    } else if (!user.isActive()) {
      return "your account is not verified";
    }
    return "Login successful";
  }
  

  public String editProfile(EditProfileDto editProfileDto) {
	    User user = userRepository.findByEmail(editProfileDto.getEmail())
	        .orElseThrow(() -> new RuntimeException("User not found with this email: " + editProfileDto.getEmail()));

	    user.setName(editProfileDto.getName());
	    user.setPassword(editProfileDto.getPassword());
	    
	    userRepository.save(user);

	    return "Profile updated successfully";
	}

  
  
}


	
 
 



