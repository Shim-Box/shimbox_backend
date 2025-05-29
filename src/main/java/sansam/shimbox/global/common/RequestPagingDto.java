package sansam.shimbox.global.common;

import lombok.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestPagingDto {

    private int page = 0;
    private int size = 10;

    public Pageable toPageable() {
        return PageRequest.of(page, size);
    }
}
