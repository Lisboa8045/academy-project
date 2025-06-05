package com.academy.utils;
import com.academy.dtos.SlotDTO;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SlotUtils {

    /**
     * Splits the interval [start, end) into slots of durationMinutes.
     * Returns a list of SlotDTOs for the provider.
     */
    public static List<SlotDTO> generateSlots(
            Long providerId,
            String providerName,
            LocalDateTime start,
            LocalDateTime end,
            int durationMinutes
    ) {
        List<SlotDTO> slots = new ArrayList<>();
        LocalDateTime current = start;

        while (current.plusMinutes(durationMinutes).compareTo(end) <= 0) {
            LocalDateTime slotEnd = current.plusMinutes(durationMinutes);
            slots.add(new SlotDTO(providerId, providerName, current, slotEnd));
            current = slotEnd;
        }
        return slots;
    }

    public static List<SlotDTO> generateCompleteSlots(Long providerId, String providerName,
                                                LocalDateTime start, LocalDateTime end, int slotDurationMinutes) {
        List<SlotDTO> slots = new ArrayList<>();
        LocalDateTime slotStart = start;
        LocalDateTime slotEnd = slotStart.plusMinutes(slotDurationMinutes);

        while (!slotEnd.isAfter(end)) {  // Garante que o slot termina dentro do intervalo
            slots.add(new SlotDTO(providerId, providerName, slotStart, slotEnd));
            slotStart = slotEnd;
            slotEnd = slotStart.plusMinutes(slotDurationMinutes);
        }

        return slots;
    }
}