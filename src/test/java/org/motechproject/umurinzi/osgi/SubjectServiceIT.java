package org.motechproject.umurinzi.osgi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;
import javax.inject.Inject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.testing.osgi.BasePaxIT;
import org.motechproject.testing.osgi.container.MotechNativeTestContainerFactory;
import org.motechproject.umurinzi.domain.Subject;
import org.motechproject.umurinzi.repository.SubjectDataService;
import org.motechproject.umurinzi.repository.VisitDataService;
import org.motechproject.umurinzi.service.SubjectService;
import org.motechproject.umurinzi.utils.SubjectUtil;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@ExamFactory(MotechNativeTestContainerFactory.class)
public class SubjectServiceIT extends BasePaxIT {

    @Inject
    private SubjectService subjectService;

    @Inject
    private SubjectDataService subjectDataService;

    @Inject
    private VisitDataService visitDataService;

    private Subject firstSubject;

    private Subject secondSubject;

    @Before
    public void cleanBefore() {
        visitDataService.deleteAll();
        subjectDataService.deleteAll();
        resetSubjects();
    }

    @After
    public void cleanAfter() {
        visitDataService.deleteAll();
        subjectDataService.deleteAll();
    }

    private void resetSubjects() {
        firstSubject = SubjectUtil.createSubject("1000000161", "Michal", null);

        secondSubject = SubjectUtil.createSubject("1000000162", "Rafal", null);
    }

    @Test
    public void shouldFindSubjectBySubjectId() {
        assertEquals(0, subjectDataService.retrieveAll().size());
        assertEquals(0, visitDataService.retrieveAll().size());

        subjectService.create(firstSubject);
        subjectService.create(secondSubject);

        List<Subject> subjects = subjectDataService.retrieveAll();
        assertEquals(2, subjects.size());

        subjects = subjectDataService.retrieveAll();
        assertEquals(2, subjects.size());

        Subject subject = subjectService.findSubjectBySubjectId("1000000161");
        assertNotNull(subject);
        assertEquals("1000000161", subject.getSubjectId());
    }

    @Test
    public void shouldFindSubjectById() {
        assertEquals(0, subjectDataService.retrieveAll().size());
        assertEquals(0, visitDataService.retrieveAll().size());

        subjectService.create(firstSubject);
        subjectService.create(secondSubject);

        Subject subject = subjectService.findSubjectBySubjectId(firstSubject.getSubjectId());

        assertEquals("Michal", subject.getName());
    }
}
