package com.duong.profile.service;

import com.duong.profile.dto.identity.Credential;
import com.duong.profile.dto.identity.TokenExchangeParam;
import com.duong.profile.dto.identity.UserCreationParam;
import com.duong.profile.dto.request.RegistrationRequest;
import com.duong.profile.dto.response.ProfileResponse;
import com.duong.profile.exception.AppException;
import com.duong.profile.exception.ErrorCode;
import com.duong.profile.exception.ErrorNormalizer;
import com.duong.profile.mapper.ProfileMapper;
import com.duong.profile.repository.IdentityClient;
import com.duong.profile.repository.ProfileRepository;
import feign.FeignException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProfileService {
    ProfileRepository profileRepository;
    ProfileMapper profileMapper;
    IdentityClient identityClient;
    ErrorNormalizer errorNormalizer;

    @Value("${idp.client-id}")
    @NonFinal
    String idpClientId;

    @Value("${idp.client-secret}")
    @NonFinal
    String idpClientSecret;

    public List<ProfileResponse> getAllProfiles(){
        var profiles = profileRepository.findAll();
        return profiles.stream().map(profileMapper::toProfileResponse).toList();
    }

    public ProfileResponse getMyProfile(){
        var authentication =SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();
        var profile = profileRepository.findByUserId(userId).orElseThrow(()->
            new AppException(ErrorCode.USER_NOT_EXISTED)
        );
        return profileMapper.toProfileResponse(profile);
    }

    public ProfileResponse register(RegistrationRequest request){

        try{
        var token = identityClient.exchangeToken(TokenExchangeParam.builder()
                        .grant_type("client_credentials")
                        .client_id(idpClientId)
                        .client_secret(idpClientSecret)
                        .scope("openid")
                .build());

        log.info("Token info: {}", token);

        var creationResponse = identityClient.createUser(
                "Bearer " + token.getAccessToken()
                , UserCreationParam.builder()
                        .username(request.getUsername())
                        .firstName(request.getFirstName())
                        .lastName(request.getLastName())
                        .email(request.getEmail())
                        .enabled(true)
                        .emailVerified(false)
                        .credentials(List.of(Credential.builder()
                                        .type("password")
                                        .temporary(false)
                                        .value(request.getPassword())
                                .build()))
                .build());


        String userId = extractUserId(creationResponse);

        log.info("UserId {}",userId);

        var profile = profileMapper.toProfile(request);
        profile.setUserId(userId);

        profile = profileRepository.save(profile);

        return profileMapper.toProfileResponse(profile);
        } catch(FeignException e){
            throw  errorNormalizer.handleKeyCloakException(e);
        }
    }

    private String  extractUserId(ResponseEntity<?> response){
      String location =  response.getHeaders().get("Location").getFirst();
      String[] split = location.split("/");
      return split[split.length - 1];
    }
}
