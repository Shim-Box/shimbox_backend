package sansam.shimbox.auth.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sansam.shimbox.auth.domain.User;
import sansam.shimbox.auth.repository.UserRepository;
import sansam.shimbox.auth.user.dto.request.RequestUserApproveDto;
import sansam.shimbox.auth.user.dto.response.ResponseUserFindAllDto;
import sansam.shimbox.global.common.PagedResponse;
import sansam.shimbox.global.common.RequestPagingDto;
import sansam.shimbox.global.exception.CustomException;
import sansam.shimbox.global.exception.ErrorCode;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;

    public PagedResponse<ResponseUserFindAllDto> userFindAll(RequestPagingDto pagingDto) {
        Page<User> usersPage = userRepository.findAllByApprovalStatusFalse(pagingDto.toPageable());

        Page<ResponseUserFindAllDto> dtoPage = usersPage.map(user ->
                ResponseUserFindAllDto.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .name(user.getName())
                        .residence(user.getResidence())
                        .approvalStatus(user.getApprovalStatus())
                        .build()
        );

        return new PagedResponse<>(
                dtoPage.getContent(),
                dtoPage.getNumber(),
                dtoPage.getSize(),
                dtoPage.getTotalElements(),
                dtoPage.getTotalPages()
        );
    }


    @Transactional
    public List<Long> approveUser(RequestUserApproveDto dto) {
        List<Long> userIds = dto.getUserIds();
        List<User> users = userRepository.findAllById(userIds);

        if (users.size() != userIds.size()) {
            throw new CustomException(ErrorCode.USERS_NOT_FOUND);
        }

        List<Long> approved = new ArrayList<>();
        for (User user : users) {
            if (!user.getApprovalStatus()) {
                user.approve();
                approved.add(user.getId());
            }
        }

        return approved;
    }
}
