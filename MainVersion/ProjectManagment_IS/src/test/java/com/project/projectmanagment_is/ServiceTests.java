package com.project.projectmanagment_is;

import com.project.projectmanagment_is.model.Developer;
import com.project.projectmanagment_is.model.Project;
import com.project.projectmanagment_is.model.Specialization;
import com.project.projectmanagment_is.model.Task;
import com.project.projectmanagment_is.repository.DeveloperRepository;
import com.project.projectmanagment_is.repository.ProjectRepository;
import com.project.projectmanagment_is.repository.TaskRepository;
import com.project.projectmanagment_is.service.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

//@RunWith(MockitoJUnitRunner.class)
@ExtendWith(MockitoExtension.class)
public class ServiceTests {
    @Mock
    private DeveloperRepository developerRepository;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private TaskRepository taskRepository;
    @InjectMocks
    private ProjectService projectService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    private final Long VALID_ID_L = 1L;
    private final String VALID_NAME = "name";
    private final Specialization VALID_SPEC = Specialization.BACKEND;
    private final LocalDate VALID_DATE = LocalDate.parse("2000-10-10");
    private final int VALID_EST = 1;

    private final Developer validDev = new Developer(
            VALID_ID_L,
            VALID_NAME,
            "1",
            VALID_SPEC,
            ""
    );
    private final Project validProject = new Project(
            VALID_ID_L,
            VALID_ID_L,
            VALID_NAME,
            "",
            ""
    );
    private final Task validTask = new Task(
            VALID_ID_L,
            VALID_ID_L,
            VALID_ID_L,
            VALID_DATE,
            VALID_NAME,
            VALID_EST,
            VALID_SPEC,
            "",
            null,
            false,
            null,
            null,
            null
    );

    @Test
    public void postAssignment_ShouldReturnAssignmentList_WhenValidDataExists() {
        //setUp data for this test
        when(projectRepository.findById(VALID_ID_L)).thenReturn(Optional.of(new Project(
                VALID_ID_L,
                VALID_ID_L,
                VALID_NAME,
                "1,2,3",
                "1,2,3"
        )));
        when(taskRepository.findById(1L)).thenReturn(Optional.of(new Task(
                1L,
                VALID_ID_L,
                VALID_ID_L,
                LocalDate.parse("2000-10-10"),
                "task1",
                VALID_EST,
                VALID_SPEC,
                "",
                null,
                false,
                null,
                null,
                null
        )));
        when(taskRepository.findById(2L)).thenReturn(Optional.of(new Task(
                2L,
                VALID_ID_L,
                VALID_ID_L,
                LocalDate.parse("2000-10-10"),
                "task2",
                VALID_EST,
                VALID_SPEC,
                "",
                null,
                false,
                null,
                null,
                null
        )));
        when(taskRepository.findById(3L)).thenReturn(Optional.of(new Task(
                3L,
                VALID_ID_L,
                VALID_ID_L,
                LocalDate.parse("2000-10-10"),
                "task3",
                VALID_EST,
                Specialization.FRONTEND,
                "",
                null,
                false,
                null,
                null,
                null
        )));
        when(developerRepository.findById(1L)).thenReturn(Optional.of(validDev));
        when(developerRepository.findById(2L)).thenReturn(Optional.of(new Developer(
                2L,
                "dev2",
                "1",
                Specialization.FRONTEND,
                ""
        )));
        when(developerRepository.findById(3L)).thenReturn(Optional.of(new Developer(
                3L,
                "dev3",
                "1",
                Specialization.BACKEND,
                "1-1-1"
        )));
        //end of setup
        List<Pair<Long, Long>> returnedList = projectService.postAssigment(VALID_ID_L);

        Pair<Long, Long> assignment1 = new Pair<>(3L, 2L);
        Pair<Long, Long> assignment2 = new Pair<>(1L, 3L);
        Pair<Long, Long> assignment3 = new Pair<>(2L, 1L);
        assertAll(
                () -> assertEquals(returnedList.get(0).getTaskId(), assignment1.getTaskId()),
                () -> assertEquals(returnedList.get(0).getDevId(), assignment1.getDevId()),
                () -> assertEquals(returnedList.get(1).getTaskId(), assignment2.getTaskId()),
                () -> assertEquals(returnedList.get(1).getDevId(), assignment2.getDevId()),
                () -> assertEquals(returnedList.get(2).getTaskId(), assignment3.getTaskId()),
                () -> assertEquals(returnedList.get(2).getDevId(), assignment3.getDevId())
        );
    }

    @Test
    public void acceptAllAssignments_ShouldReturnTrue_WhenCarriedOutSuccessfully() {
        ProjectManagmentIsApplication.assignmentList.clear();
        ProjectManagmentIsApplication.assignmentList.add(new PairOuter<>(1L, "3,1;1,3;2,1;"));
        ProjectManagmentIsApplication.assignmentList.add(new PairOuter<>(2L, "3,1;1,3;2,1;"));
        ProjectManagmentIsApplication.assignmentList.add(new PairOuter<>(3L, "3,1;1,3;2,1;"));

        when(taskRepository.findById(any())).thenReturn(Optional.of(validTask));
        boolean success = projectService.acceptAllAssignments(2L);
        assertTrue(success);
    }

    @Test
    public void acceptOneAssignment_ShouldReturnNotReturnTrue_WhenInvalidData() {
        ProjectManagmentIsApplication.assignmentList.clear();
        ProjectManagmentIsApplication.assignmentList.add(new PairOuter<>(1L, "3,1;1,3;2,1;"));
        ProjectManagmentIsApplication.assignmentList.add(new PairOuter<>(2L, "3,1;1,3;2,1;"));
        ProjectManagmentIsApplication.assignmentList.add(new PairOuter<>(3L, "3,1;1,3;2,1;"));

        Exception exception1 = assertThrows(NoSuchElementException.class, () ->
                projectService.acceptOneAssignment(-1L, 1)
        );
        assertAll(
                () -> assertEquals("No value present", exception1.getMessage()),
                () -> assertFalse(projectService.acceptOneAssignment(VALID_ID_L, 5))
        );
    }

    @Test
    public void createDeveloper_ShouldReturnDev_WhenSaveDev() {
        when(projectService.createDeveloper(any(Developer.class))).thenReturn(new Developer(
                VALID_ID_L,
                VALID_NAME,
                "",
                VALID_SPEC,
                ""
        ));
        Developer created = projectService.createDeveloper(new Developer(
                VALID_ID_L,
                VALID_NAME,
                "",
                VALID_SPEC,
                ""
        ));
        assertEquals(VALID_NAME, created.getName());
    }

    @Test
    public void createProject_ShouldReturnProject_WhenSaveProject() {
        when(developerRepository.findById(VALID_ID_L)).thenReturn(Optional.of(validDev));
        Project project = projectService.createProject(validProject, true);
        assertEquals(project.getProjectName(), VALID_NAME);
    }

    @Test
    public void getProject_ShouldReturnProject() {
        when(projectRepository.findById(VALID_ID_L)).thenReturn(Optional.of(validProject));
        Project project = projectService.getProject(VALID_ID_L);
        assertEquals(VALID_NAME, project.getProjectName());
    }

    @Test
    public void getDevProjects_ShouldReturnDevProjects_WhenPresent() {
        when(projectRepository.findAll()).thenReturn(new ArrayList<>(Collections.singleton(validProject)));
        List<Project> expectedList = new ArrayList<>();
        expectedList.add(validProject);
        List<Project> returnList = projectService.getDevProjects(VALID_ID_L);
        assertEquals(expectedList, returnList);
    }

    @Test
    public void createTask_ShouldReturnTask_WhenSaveTask() {
        when(projectRepository.findById(VALID_ID_L)).thenReturn(Optional.of(validProject));
        Task created = projectService.createTask(validTask);
        assertEquals(validTask, created);
    }


    @Test
    public void acceptOneAssignment_ShouldReturnTrue_WhenCarriedOutSuccessfully() {
        ProjectManagmentIsApplication.assignmentList.clear();
        ProjectManagmentIsApplication.assignmentList.add(new PairOuter<>(1L, "3,1;1,3;2,1;"));
        ProjectManagmentIsApplication.assignmentList.add(new PairOuter<>(2L, "3,1;1,3;2,1;"));
        ProjectManagmentIsApplication.assignmentList.add(new PairOuter<>(3L, "3,1;1,3;2,1;"));
        when(taskRepository.findById(any())).thenReturn(Optional.of(validTask));

        assertTrue(projectService.acceptOneAssignment(VALID_ID_L, 1));
    }

    @Test
    public void getMostTasksDev_ShouldReturnDevWithMostCompleted_WhenSuccessful() {
        List<Developer> returnedList = new ArrayList<>();
        returnedList.add(validDev);
        Developer expectedDev = new Developer(
                2L,
                "dev2",
                "3",
                VALID_SPEC,
                "2-5-1;3-1-2;"
        );
        returnedList.add(expectedDev);
        returnedList.add(new Developer(
                3L,
                "dev3",
                "",
                VALID_SPEC,
                "1-1-1;"
        ));
        when(developerRepository.findAll()).thenReturn(returnedList);
        assertEquals(expectedDev, projectService.getMostTasksDev());
    }
}
