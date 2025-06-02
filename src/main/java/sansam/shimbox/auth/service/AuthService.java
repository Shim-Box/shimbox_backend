package sansam.shimbox.auth.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import sansam.shimbox.auth.domain.User;
import sansam.shimbox.auth.dto.request.RequestTokenReissueDto;
import sansam.shimbox.auth.dto.request.RequestUserLoginDto;
import sansam.shimbox.auth.dto.request.RequestUserSaveDto;
import sansam.shimbox.auth.dto.response.TokenDto;
import sansam.shimbox.auth.dto.response.ResponseUserSaveDto;
import sansam.shimbox.auth.enums.*;
import sansam.shimbox.auth.repository.UserRepository;
import sansam.shimbox.global.exception.CustomException;
import sansam.shimbox.global.exception.ErrorCode;
import sansam.shimbox.global.redis.RedisService;
import sansam.shimbox.global.security.JwtUtil;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JwtUtil jwtUtil;
    private final RedisService redisService;

    @Transactional
    public ResponseUserSaveDto save(RequestUserSaveDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new CustomException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
        User user = User.builder()
                .email(dto.getEmail())
                .password(bCryptPasswordEncoder.encode(dto.getPassword()))
                .name(dto.getName())
                .identificationNumber(dto.getIdentificationNumber())
                .phoneNumber(dto.getPhoneNumber())
                .residence(dto.getResidence())
                .height(dto.getHeight())
                .weight(dto.getWeight())
                .licenseImage(dto.getLicenseImage())
                .career(Career.from(dto.getCareer()))
                .averageWorking(AverageWorking.from(dto.getAverageWorking()))
                .averageDelivery(AverageDelivery.from(dto.getAverageDelivery()))
                .bloodPressure(BloodPressure.from(dto.getBloodPressure()))
                .approvalStatus(false)
                .isDeleted(false)
                .role(Role.USER)
                .build();

        userRepository.save(user);

        return new ResponseUserSaveDto(user.getId(), user.getEmail(), user.getRole().name());
    }

    public TokenDto login(RequestUserLoginDto dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (!bCryptPasswordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_CREDENTIALS);
        }

        String accessToken = jwtUtil.createAccessToken(user.getEmail(), user.getRole().name());
        String refreshToken = jwtUtil.createRefreshToken(user.getEmail(), user.getRole().name());

        redisService.deleteRefreshToken(user.getId());
        redisService.saveRefreshToken(user.getId(), refreshToken, jwtUtil.getRefreshTokenExpirationMillis());

        return new TokenDto(accessToken, refreshToken);
    }

    public TokenDto reissueAccessToken(RequestTokenReissueDto dto) {
        if (jwtUtil.isExpired(dto.getRefreshToken())) {
            throw new CustomException(ErrorCode.TOKEN_EXPIRED);
        }

        String storedToken = redisService.getRefreshToken(dto.getUserId());
        if (storedToken == null || !storedToken.equals(dto.getRefreshToken())) {
            throw new CustomException(ErrorCode.TOKEN_INVALID);
        }

        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        String newAccessToken = jwtUtil.createAccessToken(user.getEmail(), user.getRole().name());
        return new TokenDto(newAccessToken, dto.getRefreshToken());
    }
}
