package org.kiru.core.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Slice;

import java.util.List;

@Getter
@AllArgsConstructor
public class PageableResponse<T> {
    private List<T> content;
    private Boolean hasNext;

    public static <T> PageableResponse<T> of(Slice<?> slice, List<T> content) {
        return new PageableResponse<>(content, slice.hasNext());
    }

    public static <T, U> PageableResponse<U> of(PageableResponse<T> pageableResponse, List<U> content) {
        return new PageableResponse<>(content, pageableResponse.getHasNext());
    }
}
