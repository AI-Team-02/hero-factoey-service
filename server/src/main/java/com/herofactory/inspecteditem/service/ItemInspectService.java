package com.herofactory.inspecteditem.service;

import com.herofactory.inspecteditem.dto.AutoInspectionResult;
import com.herofactory.inspecteditem.dto.InspectedItemDto;
import com.herofactory.shop.dto.ItemDto;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@RequiredArgsConstructor
@Slf4j
@Service
public class ItemInspectService {

    private final AutoInspectService autoInspectService;

    public InspectedItemDto inspectAndGetIfValid(ItemDto itemDto) {
        log.debug("Starting inspection for itemDto: [name={}, description={}, categoryName={}]",
                itemDto.getName(),
                itemDto.getDescription(),
                itemDto.getCategoryName());
        AutoInspectionResult inspectionResult = autoInspectService.inspect(itemDto);
        log.debug("Inspection result: status=[{}], tags={}",
                inspectionResult.getStatus(),
                Arrays.toString(inspectionResult.getTags())
        );
        if (inspectionResult.getStatus().equals("BAD")) {
            return null;
        }

        return InspectedItemDto.generate(
                itemDto,
                Arrays.asList(inspectionResult.getTags())
        );
    }
}
