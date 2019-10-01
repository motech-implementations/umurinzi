package org.motechproject.umurinzi.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.IOException;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.motechproject.umurinzi.domain.Subject;
import org.motechproject.umurinzi.domain.UnscheduledVisit;
import org.motechproject.umurinzi.dto.UnscheduledVisitDto;
import org.motechproject.umurinzi.repository.SubjectDataService;
import org.motechproject.umurinzi.repository.UnscheduledVisitDataService;
import org.motechproject.umurinzi.service.impl.UnscheduledVisitServiceImpl;
import org.motechproject.umurinzi.web.domain.GridSettings;
import org.motechproject.mds.query.QueryParams;

public class UnscheduledVisitServiceTest {

    @InjectMocks
    private UnscheduledVisitService unscheduledVisitService = new UnscheduledVisitServiceImpl();

    @Mock
    private LookupService lookupService;

    @Mock
    private UnscheduledVisitDataService unscheduledVisitDataService;

    @Mock
    private SubjectDataService subjectDataService;

    @Before
    public void setUp() {
        initMocks(this);
    }

    @Test
    public void shouldGetUnscheduletVisitRecords() throws IOException {
        unscheduledVisitService.getUnscheduledVisitsRecords(new GridSettings());

        verify(lookupService).getEntities(any(UnscheduledVisitDto.class.getClass()), any(UnscheduledVisit.class.getClass()),
                any(String.class), any(String.class), any(QueryParams.class));
    }

    @Test
    public void shouldAddUnscheduledVisit() {
        String subjectId = "subjectId";
        UnscheduledVisitDto unscheduledVisitDto = new UnscheduledVisitDto();
        unscheduledVisitDto.setDate(new LocalDate(2017, 4, 15));
        unscheduledVisitDto.setPurpose("purpose");
        unscheduledVisitDto.setParticipantId(subjectId);

        Subject subject = createSubject(subjectId);
        UnscheduledVisit unscheduledVisit = createUnscheduledVisit(subject, new LocalDate(2017, 4, 15));

        when(subjectDataService.findBySubjectId(subjectId)).thenReturn(subject);
        when(unscheduledVisitDataService.create(any(UnscheduledVisit.class))).thenReturn(unscheduledVisit);

        UnscheduledVisitDto resultDto = unscheduledVisitService.addOrUpdate(unscheduledVisitDto, false);

        verify(unscheduledVisitDataService).create(any(UnscheduledVisit.class));

        assertEquals(subjectId, resultDto.getParticipantId());
        assertEquals(new LocalDate(2017, 4, 15), resultDto.getDate());
        assertEquals("purpose", resultDto.getPurpose());
    }

    @Test
    public void shouldUpdateUnscheduledVisit() {
        String subjectId = "subjectId";
        UnscheduledVisitDto unscheduledVisitDto = new UnscheduledVisitDto();
        unscheduledVisitDto.setDate(new LocalDate(2017, 4, 16));
        unscheduledVisitDto.setPurpose("purpose");
        unscheduledVisitDto.setParticipantId(subjectId);
        unscheduledVisitDto.setId("1");

        Subject subject = createSubject(subjectId);
        UnscheduledVisit unscheduledVisitInDB = createUnscheduledVisit(subject, new LocalDate(2017, 4, 15));

        when(subjectDataService.findBySubjectId(subjectId)).thenReturn(subject);
        when(unscheduledVisitDataService.create(any(UnscheduledVisit.class))).thenReturn(
                createUnscheduledVisit(subject, new LocalDate(2017, 4, 16)));
        when(unscheduledVisitDataService.findById(1L)).thenReturn(unscheduledVisitInDB);

        UnscheduledVisitDto resultDto = unscheduledVisitService.addOrUpdate(unscheduledVisitDto, false);

        verify(unscheduledVisitDataService).create(any(UnscheduledVisit.class));

        assertEquals(subjectId, resultDto.getParticipantId());
        assertEquals(new LocalDate(2017, 4, 16), resultDto.getDate());
        assertEquals("purpose", resultDto.getPurpose());
    }

    private Subject createSubject(String subjectId) {
        Subject subject = new Subject();
        subject.setSubjectId(subjectId);
        return subject;
    }

    private UnscheduledVisit createUnscheduledVisit(Subject subject, LocalDate date) {
        UnscheduledVisit unscheduledVisit = new UnscheduledVisit();
        unscheduledVisit.setId(1L);
        unscheduledVisit.setDate(date);
        unscheduledVisit.setPurpose("purpose");
        unscheduledVisit.setSubject(subject);
        return unscheduledVisit;
    }
}
