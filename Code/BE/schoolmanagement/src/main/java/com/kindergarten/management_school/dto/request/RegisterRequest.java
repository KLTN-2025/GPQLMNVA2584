package com.kindergarten.management_school.dto.request;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {
    private String username;
    private String password;
    private String fullName;
    private String email;
    private String phone;
    private String gender;
    private String occupation;
    private String relationship;
    private String emergencyContact;
    private String additionalPhone;
}
