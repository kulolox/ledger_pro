package com.example.back_end.dto.request;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record BatchDeleteRequest(@NotEmpty List<Long> ids) {}
