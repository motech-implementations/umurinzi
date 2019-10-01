package org.motechproject.umurinzi.osgi;

import static org.junit.Assert.assertEquals;
import static org.motechproject.umurinzi.utils.SubjectUtil.createSubject;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.umurinzi.domain.Subject;
import org.motechproject.umurinzi.domain.Visit;
import org.motechproject.umurinzi.domain.enums.Language;
import org.motechproject.umurinzi.domain.enums.VisitType;
import org.motechproject.umurinzi.repository.SubjectDataService;
import org.motechproject.umurinzi.repository.VisitDataService;
import org.motechproject.umurinzi.service.LookupService;
import org.motechproject.umurinzi.utils.VisitUtil;
import org.motechproject.umurinzi.web.domain.Records;
import org.motechproject.mds.query.QueryParams;
import org.motechproject.testing.osgi.BasePaxIT;
import org.motechproject.testing.osgi.container.MotechNativeTestContainerFactory;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@ExamFactory(MotechNativeTestContainerFactory.class)
public class LookupServiceIT extends BasePaxIT {

    @Inject
    private SubjectDataService subjectDataService;

    @Inject
    private VisitDataService visitDataService;

    @Inject
    private LookupService lookupService;

    private ArrayList<Visit> testVisits = new ArrayList<Visit>();

    @Before
    public void cleanBefore() {
        cleanDatabase();
        resetTestFields();
    }

    @Before
    public void cleanAfter() {
        cleanDatabase();
    }

    @Test
    public void shouldGetVisitEntitiesFromLookup() {
        addTestVisitsToDB();
        String []fields = {
                "{\"type\":\"PRIME_VACCINATION_DAY\"}",
                "{\"subject.subjectId\":\"1000000\"}",
                "{\"subject.subjectId\":\"1000000162\"}",
                "{\"subject.name\":\"Michal\"}",
                "{\"dateProjected\":{\"min\":\"2014-10-16\",\"max\":\"2014-10-23\"}}",
                "{\"date\":{\"min\":\"2014-10-16\",\"max\":\"2014-10-21\"}}"
        };
        String []lookups = {
                "Find By Type",
                "Find By Participant Id",
                "Find By exact Participant Id",
                "Find By Participant Name",
                "Find By Visit Planned Date Range",
                "Find By Visit Actual Date Range"
        };
        int []expectedResults = {2, 4, 2, 2, 3, 3};

        QueryParams queryParams = new QueryParams(1, null);
        for (int i = 0; i < lookups.length; i++) {
            Records<Visit> records = lookupService.getEntities(Visit.class, lookups[i], fields[i], queryParams);
            List<Visit> visitList = records.getRows();
            assertEquals(expectedResults[i], visitList.size());
        }
    }

    @Test
    public void shouldGetSubjectEntitiesFromLookup() {
        addTestVisitsToDB();
        String []fields = {
                "{\"primerVaccinationDate\":\"2014-10-16\"}",
                "{\"boosterVaccinationDate\":{\"min\":\"2014-10-15\",\"max\":\"2014-10-18\"}}",
                "{\"boosterVaccinationDate\":\"2014-10-16\"}"
        };
        String []lookups = {
                "Find By Primer Vaccination Date",
                "Find By Booster Vaccination Date Range",
                "Find By Booster Vaccination Date",
        };
        int []expectedResults = {1, 1, 1};

        QueryParams queryParams = new QueryParams(1, null);
        for (int i = 0; i < lookups.length; i++) {
            Records<Subject> records = lookupService.getEntities(Subject.class, lookups[i], fields[i], queryParams);
            List<Subject> subjectList = records.getRows();
            assertEquals(expectedResults[i], subjectList.size());
        }
    }

    private void resetTestFields() {
        Subject firstSubject = createSubject("1000000161", "Michal", "729402018364", Language.English);

        Subject secondSubject = createSubject("1000000162", "Rafal", "44443333222", Language.Susu);

        firstSubject.setPrimerVaccinationDate(new LocalDate(2014, 10, 16));
        firstSubject.setBoosterVaccinationDate(new LocalDate(2014, 10, 16));

        secondSubject.setPrimerVaccinationDate(new LocalDate(2014, 10, 17));
        secondSubject.setBoosterVaccinationDate(new LocalDate(2014, 10, 17));

        testVisits.add(VisitUtil.createVisit(firstSubject, VisitType.PRIME_VACCINATION_DAY,
                new LocalDate(2014, 10, 17), new LocalDate(2014, 10, 21), "owner"));

        testVisits.add(VisitUtil.createVisit(secondSubject, VisitType.PRIME_VACCINATION_DAY,
                new LocalDate(2014, 10, 19), new LocalDate(2014, 10, 21), "owner"));

        testVisits.add(VisitUtil.createVisit(secondSubject, VisitType.D1_VISIT,
                new LocalDate(2014, 10, 21), new LocalDate(2014, 10, 23), "owner"));

        testVisits.add(VisitUtil.createVisit(firstSubject, VisitType.BOOST_VACCINATION_DAY,
                new LocalDate(2014, 10, 22), new LocalDate(2014, 10, 24), "owner"));
    }

    private void addTestVisitsToDB() {
        assertEquals(0, subjectDataService.retrieveAll().size());
        assertEquals(0, visitDataService.retrieveAll().size());

        for (Visit visit : testVisits) {
            visitDataService.create(visit);
        }

        assertEquals(2, subjectDataService.retrieveAll().size());
        assertEquals(4, visitDataService.retrieveAll().size());
    }

    private void cleanDatabase() {
        visitDataService.deleteAll();
        subjectDataService.deleteAll();
    }
}
