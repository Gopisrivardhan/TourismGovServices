package com.tourismgov.tourist.dto;

import java.time.LocalDate;
import java.util.List;

import com.tourismgov.tourist.enums.Gender;
import com.tourismgov.tourist.enums.Status;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TouristResponse {
    private Long touristId;
    private String name;
    private LocalDate dob;
    private Gender gender;
    private String address;
    private String contactInfo;
    private Status status;
    private List<TouristDocumentResponse> documents;
}
