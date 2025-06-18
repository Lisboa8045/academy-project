package com.academy.utils;
import com.academy.dtos.SlotDTO;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SlotUtils {

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