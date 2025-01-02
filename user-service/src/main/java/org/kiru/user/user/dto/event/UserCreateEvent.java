package org.kiru.user.user.dto.event;

import java.util.List;
import lombok.Builder;
import org.kiru.user.user.dto.request.UserPurposesReq;
import org.kiru.user.user.dto.request.UserTalentsReq;
import org.springframework.web.multipart.MultipartFile;

@Builder
public record UserCreateEvent(
        Long userId,String userName,
        List<MultipartFile> images, List<UserPurposesReq> purposes, List<UserTalentsReq> talents
){
}
