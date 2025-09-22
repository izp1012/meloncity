package com.meloncity.citiz.dto;

import java.util.List;

public record PageRes<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {
}
