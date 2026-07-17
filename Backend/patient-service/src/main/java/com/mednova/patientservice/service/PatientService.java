package com.mednova.patientservice.service;

import billing.BillingServiceGrpc;
import com.mednova.patientservice.dto.PatientRequestDTO;
import com.mednova.patientservice.dto.PatientResponseDTO;
import com.mednova.patientservice.exception.EmailAlreadyExistException;
import com.mednova.patientservice.exception.EmailNotExistException;
import com.mednova.patientservice.grpc.BillingServiceGrpcClient;
import com.mednova.patientservice.kafka.KafkaProducer;
import com.mednova.patientservice.model.Patient;
import com.mednova.patientservice.repository.PatientRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class PatientService {

    private final KafkaProducer kafkaProducer;
    private final PatientRepository patientRepository;
    private final BillingServiceGrpcClient billingServiceGrpcClient;

    public PatientService(
            PatientRepository patientRepository,
            BillingServiceGrpcClient billingServiceGrpcClient,
            KafkaProducer kafkaProducer) {
        this.patientRepository = patientRepository;
        this.billingServiceGrpcClient = billingServiceGrpcClient;
        this.kafkaProducer = kafkaProducer;
    }

    public List<PatientResponseDTO> getAllPatients(){
        List<Patient> patients = patientRepository.findAll();

        if (patients.isEmpty()){
            throw new RuntimeException("No Patient Exists in Database");
        }


        return patients
                .stream()
                .map(patient -> new PatientResponseDTO(
                        patient.getId(),
                        patient.getName(),
                        patient.getEmail(),
                        patient.getCreatedAt(),
                        patient.getAddress(),
                        patient.getDob()
                )).toList();
    }

    public Patient createPatient(PatientRequestDTO patientRequestDTO) {
        if (patientRepository.existsByEmail(patientRequestDTO.getEmail())){
            throw new EmailAlreadyExistException("A patient with this email already exist" + patientRequestDTO.getEmail());
        }

        Patient patient = Patient.builder()
                .name(patientRequestDTO.getName())
                .email(patientRequestDTO.getEmail())
                .address(patientRequestDTO.getAddress())
                .dob(LocalDate.parse(patientRequestDTO.getDateOfBirth()))
                .build();

        Patient newPatient = patientRepository.save(patient);
        billingServiceGrpcClient.createBillingAccount(newPatient.getId().toString(), newPatient.getName(), newPatient.getEmail());

        kafkaProducer.sendEvent(newPatient);

        return newPatient;
    }

    public Patient updatePatient(UUID id, PatientRequestDTO request){
        Patient patient = patientRepository.findById(id).orElseThrow();

        patient.setName(request.getName());
        patient.setAddress(request.getAddress());
        patient.setDob(LocalDate.parse(request.getDateOfBirth()));

       return patientRepository.save(patient);
    }

    public Patient deletePatient(String email) {

        Patient patient = patientRepository.findByEmail(email)
                .orElseThrow(() ->
                        new EmailNotExistException("User does not exist"));

        patientRepository.delete(patient);

        return patient;
    }
}
