package com.epam.gym_crm.api.dto;

import com.epam.gym_crm.db.entity.User;

public record UserCreationResult(User userToPersist, String rawPassword) {
}
