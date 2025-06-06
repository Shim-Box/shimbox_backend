package sansam.shimbox.global.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestPagingDto {

    @Schema(description = "몇번째 페이지인지", example = "1")
    private int page = 1;

    @Schema(description = "페이지당 몇개의 페이지를 가져올지", example = "10")
    private int size = 10;

    public Pageable toPageable() {
        return PageRequest.of(page - 1, size);
    }
}
