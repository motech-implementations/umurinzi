package org.motechproject.umurinzi.service;

import static org.mockito.MockitoAnnotations.initMocks;

import org.junit.Before;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.motechproject.umurinzi.repository.UnscheduledVisitDataService;
import org.motechproject.umurinzi.repository.VisitDataService;
import org.motechproject.umurinzi.service.impl.ReportServiceImpl;

public class ReportServiceTest {

    @InjectMocks
    private ReportService reportService = new ReportServiceImpl();

    @Mock
    private VisitDataService visitDataService;

    @Mock
    private LookupService lookupService;

    @Mock
    private UnscheduledVisitDataService unscheduledVisitDataService;

    @Before
    public void setUp() {
        initMocks(this);
    }
}
