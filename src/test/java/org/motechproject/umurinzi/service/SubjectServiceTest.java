package org.motechproject.umurinzi.service;

import static org.mockito.MockitoAnnotations.initMocks;

import org.junit.Before;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.motechproject.umurinzi.repository.SubjectDataService;
import org.motechproject.umurinzi.repository.VisitDataService;
import org.motechproject.umurinzi.service.impl.SubjectServiceImpl;

public class SubjectServiceTest {

    @InjectMocks
    private SubjectService subjectService = new SubjectServiceImpl();

    @Mock
    private SubjectDataService subjectDataService;

    @Mock
    private VisitDataService visitDataService;

    @Before
    public void setUp() {
        initMocks(this);
    }
}
