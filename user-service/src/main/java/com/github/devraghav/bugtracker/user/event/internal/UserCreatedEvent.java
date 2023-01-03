package com.github.devraghav.bugtracker.user.event.internal;

import com.github.devraghav.bugtracker.user.dto.User;
import java.time.LocalDateTime;
import java.util.UUID;

public record UserCreatedEvent(UUID uuid, User user, LocalDateTime createdAt) {}
