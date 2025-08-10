package com.bci.mapper;

import com.bci.entity.Phone;
import com.bci.entity.User;
import com.bci.model.PhoneRequestDTO;
import com.bci.model.PhoneResponseDTO;
import com.bci.model.UserResponseDTO;
import com.bci.model.UserSignUpRequestDTO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserMapper {

    public User toEntity(UserSignUpRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword());
        return user;
    }

    public Phone phoneRequestDTOToEntity(PhoneRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        Phone phone = new Phone();
        phone.setNumber(dto.getNumber());
        phone.setCitycode(dto.getCitycode());
        phone.setCountrycode(dto.getCountrycode());
        return phone;
    }

    public List<Phone> phoneRequestDTOsToEntities(List<PhoneRequestDTO> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            return List.of();
        }

        return dtos.stream()
                .map(this::phoneRequestDTOToEntity)
                .filter(phone -> phone != null)
                .collect(Collectors.toList());
    }

    public PhoneResponseDTO phoneEntityToDTO(Phone entity) {
        if (entity == null) {
            return null;
        }

        return new PhoneResponseDTO(
                entity.getNumber(),
                entity.getCitycode(),
                entity.getCountrycode()
        );
    }

    public UserResponseDTO toDTO(User entity) {
        if (entity == null) {
            return null;
        }

        List<PhoneResponseDTO> phoneDTOs = entity.getPhones() != null ?
                entity.getPhones().stream()
                        .map(this::phoneEntityToDTO)
                        .filter(phone -> phone != null)
                        .collect(Collectors.toList()) :
                List.of();

        return new UserResponseDTO(
                entity.getId(),
                entity.getName(),
                entity.getEmail(),
                phoneDTOs,
                entity.getCreated(),
                entity.getLastLogin(),
                entity.getToken(),
                entity.getIsActive()
        );
    }
}