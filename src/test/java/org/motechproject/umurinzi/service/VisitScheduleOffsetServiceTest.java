package org.motechproject.umurinzi.service;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.motechproject.umurinzi.domain.VisitScheduleOffset;
import org.motechproject.umurinzi.domain.enums.VisitType;
import org.motechproject.umurinzi.repository.VisitScheduleOffsetDataService;
import org.motechproject.umurinzi.service.impl.VisitScheduleOffsetServiceImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class VisitScheduleOffsetServiceTest {

    @InjectMocks
    private VisitScheduleOffsetService visitScheduleOffsetService = new VisitScheduleOffsetServiceImpl();

    @Mock
    private VisitScheduleOffsetDataService visitScheduleOffsetDataService;

    @Before
    public void setUp() {
        initMocks(this);
    }

    @Test
    public void shouldGetAllAsMap() {
        List<VisitScheduleOffset> visitScheduleOffsetList = createVisitScheduleOffsetList();

        when(visitScheduleOffsetDataService.retrieveAll()).thenReturn(visitScheduleOffsetList);

        Map<VisitType, VisitScheduleOffset> map = visitScheduleOffsetService.getAllAsMap();

        for (VisitScheduleOffset offset : visitScheduleOffsetList) {
            VisitType key = offset.getVisitType();
            assertTrue(map.containsKey(key));
            checkVisitScheduleOffset(offset, map.get(key));
        }
    }

    private void checkVisitScheduleOffset(VisitScheduleOffset expected, VisitScheduleOffset result) {
        assertEquals(expected.getVisitType(), result.getVisitType());
        assertEquals(expected.getTimeOffset(), result.getTimeOffset());
        assertEquals(expected.getEarliestDateOffset(), result.getEarliestDateOffset());
        assertEquals(expected.getLatestDateOffset(), result.getLatestDateOffset());
    }

    private List<VisitScheduleOffset> createVisitScheduleOffsetList() {
        List<VisitScheduleOffset> list = new ArrayList<>();
        List<VisitType> types = Arrays.asList(VisitType.values());

        for (int i = 0; i < types.size(); i++) {
            VisitScheduleOffset offset = new VisitScheduleOffset();
            offset.setVisitType(types.get(i));
            offset.setEarliestDateOffset(i * 2 + 1);
            offset.setLatestDateOffset(i * 2 + 10);
            offset.setTimeOffset(i * 2 + 5);
            list.add(offset);
        }

        return list;
    }
}
