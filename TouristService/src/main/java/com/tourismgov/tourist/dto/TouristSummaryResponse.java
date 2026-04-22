package com.tourismgov.tourist.dto;

import com.tourismgov.tourist.enums.Status;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TouristSummaryResponse {
	private Long touristId;
	private String name;
	private Status status;
}
