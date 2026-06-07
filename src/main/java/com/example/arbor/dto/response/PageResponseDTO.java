package com.example.arbor.dto.response;

import java.util.List;

public record PageResponseDTO<T>(
        List<T> items,
        int page,
        int limit,
        long total
) {
}
