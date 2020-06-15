package de.tum.in.www1.artemis;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;

import de.tum.in.www1.artemis.domain.Course;
import de.tum.in.www1.artemis.domain.exam.Exam;
import de.tum.in.www1.artemis.domain.exam.ExerciseGroup;
import de.tum.in.www1.artemis.service.ExamAccessService;
import de.tum.in.www1.artemis.util.DatabaseUtilService;
import de.tum.in.www1.artemis.util.ModelFactory;
import de.tum.in.www1.artemis.util.RequestUtilService;

public class ExerciseGroupIntegrationTest extends AbstractSpringIntegrationBambooBitbucketJiraTest {

    @Autowired
    DatabaseUtilService database;

    @Autowired
    RequestUtilService request;

    @SpyBean
    ExamAccessService examAccessService;

    private Course course1;

    private Exam exam1;

    private Exam exam2;

    private ExerciseGroup exerciseGroup1;

    @BeforeEach
    public void initTestCase() {
        database.addUsers(1, 1, 1);
        course1 = database.addEmptyCourse();
        exam1 = database.addExam(course1);
        exam2 = database.addExam(course1);
        exerciseGroup1 = database.addExerciseGroup(exam1, true);
        database.addExerciseGroup(exam2, true);
    }

    @AfterEach
    public void resetDatabase() {
        database.resetDatabase();
    }

    @Test
    @WithMockUser(username = "student1", roles = "USER")
    public void testAll_asStudent() throws Exception {
        this.testAllPreAuthorize();
    }

    @Test
    @WithMockUser(username = "tutor1", roles = "TA")
    public void testAll_asTutor() throws Exception {
        this.testAllPreAuthorize();
    }

    private void testAllPreAuthorize() throws Exception {
        ExerciseGroup exerciseGroup = ModelFactory.generateExerciseGroup(true, exam1);
        request.post("/api/courses/" + course1.getId() + "/exams/" + exam1.getId() + "/exerciseGroups", exerciseGroup, HttpStatus.FORBIDDEN);
        request.put("/api/courses/" + course1.getId() + "/exams/" + exam1.getId() + "/exerciseGroups", exerciseGroup, HttpStatus.FORBIDDEN);
        request.get("/api/courses/" + course1.getId() + "/exams/" + exam1.getId() + "/exerciseGroups/" + exerciseGroup1.getId(), HttpStatus.FORBIDDEN, ExerciseGroup.class);
        request.getList("/api/courses/" + course1.getId() + "/exams/" + exam1.getId() + "/exerciseGroups", HttpStatus.FORBIDDEN, ExerciseGroup.class);
        request.delete("/api/courses/" + course1.getId() + "/exams/" + exam1.getId() + "/exerciseGroups/" + exerciseGroup1.getId(), HttpStatus.FORBIDDEN);
    }

    @Test
    @WithMockUser(username = "instructor1", roles = "INSTRUCTOR")
    public void testCreateExerciseGroup_asInstructor() throws Exception {
        ExerciseGroup exerciseGroup = ModelFactory.generateExerciseGroup(true, exam1);
        exerciseGroup.setId(55L);
        request.post("/api/courses/" + course1.getId() + "/exams/" + exam1.getId() + "/exerciseGroups", exerciseGroup, HttpStatus.BAD_REQUEST);
        exerciseGroup = ModelFactory.generateExerciseGroup(true, exam1);
        exerciseGroup.setExam(null);
        request.post("/api/courses/" + course1.getId() + "/exams/" + exam1.getId() + "/exerciseGroups", exerciseGroup, HttpStatus.CONFLICT);
        exerciseGroup = ModelFactory.generateExerciseGroup(true, exam2);
        request.post("/api/courses/" + course1.getId() + "/exams/" + exam2.getId() + "/exerciseGroups", exerciseGroup, HttpStatus.CREATED);
        verify(examAccessService, times(1)).checkCourseAndExamAccess(course1.getId(), exam2.getId());
    }

    @Test
    @WithMockUser(username = "instructor1", roles = "INSTRUCTOR")
    public void testUpdateExerciseGroup_asInstructor() throws Exception {
        ExerciseGroup exerciseGroup = ModelFactory.generateExerciseGroup(true, exam1);
        exerciseGroup.setExam(null);
        request.put("/api/courses/" + course1.getId() + "/exams/" + exam1.getId() + "/exerciseGroups", exerciseGroup, HttpStatus.CONFLICT);
        request.put("/api/courses/" + course1.getId() + "/exams/" + exam1.getId() + "/exerciseGroups", exerciseGroup1, HttpStatus.OK);
        verify(examAccessService, times(1)).checkCourseAndExamAndExerciseGroupAccess(course1.getId(), exam1.getId(), exerciseGroup1.getId());
    }

    @Test
    @WithMockUser(username = "instructor1", roles = "INSTRUCTOR")
    public void testGetExerciseGroup_asInstructor() throws Exception {
        request.get("/api/courses/" + course1.getId() + "/exams/" + exam1.getId() + "/exerciseGroups/" + exerciseGroup1.getId(), HttpStatus.OK, ExerciseGroup.class);
        verify(examAccessService, times(1)).checkCourseAndExamAndExerciseGroupAccess(course1.getId(), exam1.getId(), exerciseGroup1.getId());
    }

    @Test
    @WithMockUser(username = "instructor1", roles = "INSTRUCTOR")
    public void testGetExerciseGroupsForExam_asInstructor() throws Exception {
        request.getList("/api/courses/" + course1.getId() + "/exams/" + exam1.getId() + "/exerciseGroups", HttpStatus.OK, ExerciseGroup.class);
        verify(examAccessService, times(1)).checkCourseAndExamAccess(course1.getId(), exam1.getId());
    }

    @Test
    @WithMockUser(username = "instructor1", roles = "INSTRUCTOR")
    public void testDeleteExerciseGroup_asInstructor() throws Exception {
        request.delete("/api/courses/" + course1.getId() + "/exams/" + exam1.getId() + "/exerciseGroups/" + exerciseGroup1.getId(), HttpStatus.OK);
        verify(examAccessService, times(1)).checkCourseAndExamAndExerciseGroupAccess(course1.getId(), exam1.getId(), exerciseGroup1.getId());
    }
}