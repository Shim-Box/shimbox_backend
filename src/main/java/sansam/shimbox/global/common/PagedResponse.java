package sansam.shimbox.global.common;

import lombok.RequiredArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class PagedResponse<T> {

    private final List<T> data;
    private final int page;
    private final int size;
    private final long totalElements;
    private final int totalPages;

    public static <T> BaseResponse<PagedResponse<T>> fromPage(Page<T> pageData) {
        PagedResponse<T> paged = new PagedResponse<>(
                pageData.getContent(),
                pageData.getNumber(),
                pageData.getSize(),
                pageData.getTotalElements(),
                pageData.getTotalPages()
        );
        return BaseResponse.success(paged, "요청이 성공했습니다.", HttpStatus.OK);
    }
}