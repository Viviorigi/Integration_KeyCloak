package com.duong.profile.mapper;

import com.duong.profile.dto.request.RegistrationRequest;
import com.duong.profile.dto.response.ProfileResponse;
import com.duong.profile.entity.Profile;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProfileMapper {
    Profile toProfile(RegistrationRequest request);
    ProfileResponse toProfileResponse(Profile profile);
}
