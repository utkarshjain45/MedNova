package com.mednova.patientservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@AllArgsConstructor
public class PatientResponseDTO {

    private UUID id;
    private String name;
    private String email;
    private LocalDate createdAt;
    private String address;
    private LocalDate dob;
}
