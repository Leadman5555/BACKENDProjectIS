package com.project.projectmanagment_is;

import com.project.projectmanagment_is.model.*;
import com.project.projectmanagment_is.repository.DeveloperRepository;
import com.project.projectmanagment_is.repository.ProjectRepository;
import com.project.projectmanagment_is.repository.TaskRepository;
import com.project.projectmanagment_is.service.dataValidation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ValidationTests {

    @Autowired
    private dataValidation dataValidation;
    @MockBean
    private DeveloperRepository developerRepository;
    @MockBean
    private ProjectRepository projectRepository;
    @MockBean
    private TaskRepository taskRepository;

    private final String VALID_ID = "1";
    private final String INVALID_ID = "-1";
    private final Long VALID_ID_L = Long.parseLong(VALID_ID);
    private final Long INVALID_ID_L = Long.parseLong(INVALID_ID);
    private final String VALID_SPEC_S = "BACKEND";
    private final String VALID_DATE = "2000-10-10";
    private final int VALID_EST = 1;
    private final String VALID_NAME = "name";

    @Before
    public void setUp() {
        //valid project returned when asked existing one
        Project validProject = new Project(
                VALID_ID_L,
                VALID_ID_L,
                VALID_NAME,
                VALID_ID,
                VALID_ID);

        when(taskRepository.findById(VALID_ID_L)).thenReturn(Optional.of(new Task(
                VALID_ID_L,
                VALID_ID_L,
                VALID_ID_L,
                LocalDate.parse(VALID_DATE),
                VALID_NAME,
                VALID_EST,
                Enum.valueOf(Specialization.class, VALID_SPEC_S),
                "",
                null,
                false,
                null,
                null,
                null
        )));
        when(projectRepository.findById(VALID_ID_L)).thenReturn(Optional.of(validProject));
        //when non-existing project is called for
        when(projectRepository.findById(INVALID_ID_L)).thenReturn(Optional.empty());
        //when an existing model is checked for existence
        when(projectRepository.existsById(VALID_ID_L)).thenReturn(true);
        when(developerRepository.existsById(VALID_ID_L)).thenReturn(true);
        when(taskRepository.existsById(VALID_ID_L)).thenReturn(true);
        //when a non-existing model is checked
        when(projectRepository.existsById(INVALID_ID_L)).thenReturn(false);
        when(developerRepository.existsById(INVALID_ID_L)).thenReturn(false);
        when(taskRepository.existsById(INVALID_ID_L)).thenReturn(false);
    }

    @Test
    public void checkSpecialization_ShouldReturnTrue_WhenSpecializationValid() {
        assertAll(
                () -> assertTrue(dataValidation.checkSpecialization("BACKEND")),
                () -> assertTrue(dataValidation.checkSpecialization("FRONTEND")),
                () -> assertTrue(dataValidation.checkSpecialization("DEVOPS")),
                () -> assertTrue(dataValidation.checkSpecialization("UX_UI"))
        );
    }

    @Test
    public void checkDate_ShouldReturnTrue_WhenValidDateFormat() {
        boolean result = dataValidation.checkDate(VALID_DATE);
        assertTrue(result);
    }

    @Test
    public void checkNameIsBland_ShouldReturnFalse_WhenNameInvalid() {
        assertAll(
                () -> assertFalse(dataValidation.checkNameIsBlank("")),
                () -> assertFalse(dataValidation.checkNameIsBlank("        ")),
                () -> assertFalse(dataValidation.checkNameIsBlank(null))
        );
    }

    @Test
    public void checkProjectHasAssignment_ShouldReturnTrue_WhenAll() {
        ProjectManagmentIsApplication.assignmentList.clear();
        ProjectManagmentIsApplication.assignmentList.add(new PairOuter<>(VALID_ID_L, "1,1"));
        assertTrue(dataValidation.checkProjectHasAssignment(VALID_ID_L, true, 0));
    }

    @Test
    public void checkProjectHasAssignment_ShouldReturnTrue_WhenIdExists() {
        ProjectManagmentIsApplication.assignmentList.clear();
        ProjectManagmentIsApplication.assignmentList.add(new PairOuter<>(VALID_ID_L, "1,1;"));
        assertTrue(dataValidation.checkProjectHasAssignment(VALID_ID_L, false, VALID_ID_L.intValue()));
    }

    @Test
    public void checkProjectHasAssignment_ShouldReturnFalse_WhenIdDoesNotExist() {
        ProjectManagmentIsApplication.assignmentList.clear();
        ProjectManagmentIsApplication.assignmentList.add(new PairOuter<>(VALID_ID_L, "1,1;"));
        assertAll(
                () -> assertFalse(dataValidation.checkProjectHasAssignment(VALID_ID_L, false, 2)),
                () -> assertFalse(dataValidation.checkProjectHasAssignment(VALID_ID_L, false, INVALID_ID_L.intValue())),
                () -> assertFalse(dataValidation.checkProjectHasAssignment(INVALID_ID_L, false, 1)),
                () -> assertFalse(dataValidation.checkProjectHasAssignment(INVALID_ID_L, true, 0))
        );
    }

    @Test
    public void checkDate_ShouldReturnFalse_WhenInvalidDateFormat() {
        String invalidDate = "10-10-2000";
        boolean result = dataValidation.checkDate(invalidDate);
        assertFalse(result);
    }

    @Test
    public void checkProjectTaskExists_ShouldReturnTrue_WhenTaskIsLinkedToProject() {
        boolean result = dataValidation.checkProjectTaskExists(VALID_ID_L, VALID_ID_L);
        assertTrue(result);
    }

    @Test
    public void checkProjectTaskExists_ShouldReturnFalse_WhenNoSuchProjectExists() {
        Exception exception = assertThrows(NoSuchElementException.class, () ->
                dataValidation.checkProjectTaskExists(INVALID_ID_L, VALID_ID_L)
        );
        assertEquals("No value present", exception.getMessage());
    }

    @Test
    public void checkProjectExists_ShouldReturnTrue_WhenModelExists() {
        assertTrue(dataValidation.checkProjectExists(VALID_ID_L));
    }

    @Test
    public void checkDevExists_ShouldReturnTrue_WhenModelExists() {
        assertTrue(dataValidation.checkDevExists(VALID_ID_L));
    }

    @Test
    public void checkTaskExists_ShouldReturnTrue_WhenModelExists() {
        assertTrue(dataValidation.checkTaskExists(VALID_ID_L));
    }

    @Test
    public void checkProjectExists_ShouldReturnFalse_WhenModelDoesNotExist() {
        assertFalse(dataValidation.checkProjectExists(INVALID_ID_L));
    }

    @Test
    public void checkDevExists_ShouldReturnFalse_WhenModelDoesNotExist() {
        assertFalse(dataValidation.checkDevExists(INVALID_ID_L));
    }

    @Test
    public void checkTaskExists_ShouldReturnFalse_WhenModelDoesNotExist() {
        assertFalse(dataValidation.checkTaskExists(INVALID_ID_L));
    }

    @Test
    public void checkProjectData_ShouldReturnTrue_WhenValidDataGiven() {
        assertTrue(dataValidation.checkProjectData(new ProjectDto(
                VALID_ID_L,
                VALID_NAME,
                VALID_ID
        )));
    }

    @Test
    public void checkProjectData_ShouldReturnFalse_WhenInvalidDataGiven() {
        assertAll(
                () -> assertFalse(dataValidation.checkProjectData(new ProjectDto(
                        VALID_ID_L,
                        "",
                        VALID_ID
                ))),
                () -> assertFalse(dataValidation.checkProjectData(new ProjectDto(
                        VALID_ID_L,
                        VALID_NAME,
                        INVALID_ID
                )))
        );
    }

    @Test
    public void checkTaskData_ShouldReturnTrue_WhenValidDataGiven() {
        assertTrue(dataValidation.checkTaskData(VALID_ID_L, new TaskDto(
                VALID_ID_L,
                VALID_DATE,
                VALID_NAME,
                VALID_EST,
                VALID_SPEC_S,
                "",
                null
        )));
    }

    @Test
    public void checkTaskData_ShouldReturnFalse_WhenInvalidDataGiven() {
        assertAll(
                () -> assertFalse(dataValidation.checkTaskData(VALID_ID_L, new TaskDto(
                        INVALID_ID_L,
                        VALID_DATE,
                        VALID_NAME,
                        VALID_EST,
                        VALID_SPEC_S,
                        "",
                        null
                ))),
                () -> assertFalse(dataValidation.checkTaskData(VALID_ID_L, new TaskDto(
                        VALID_ID_L,
                        "2000-50-10",
                        VALID_NAME,
                        VALID_EST,
                        VALID_SPEC_S,
                        "",
                        null
                ))),
                () -> assertFalse(dataValidation.checkTaskData(VALID_ID_L, new TaskDto(
                        VALID_ID_L,
                        VALID_DATE,
                        "",
                        VALID_EST,
                        VALID_SPEC_S,
                        "",
                        null
                ))),
                () -> assertFalse(dataValidation.checkTaskData(VALID_ID_L, new TaskDto(
                        VALID_ID_L,
                        VALID_DATE,
                        VALID_NAME,
                        6,
                        VALID_SPEC_S,
                        "",
                        null
                ))),
                () -> assertFalse(dataValidation.checkTaskData(VALID_ID_L, new TaskDto(
                        VALID_ID_L,
                        VALID_DATE,
                        "",
                        VALID_EST,
                        "invalid",
                        "",
                        null
                ))),
                () -> assertFalse(dataValidation.checkTaskData(VALID_ID_L, new TaskDto(
                        VALID_ID_L,
                        VALID_DATE,
                        "",
                        VALID_EST,
                        VALID_SPEC_S,
                        "",
                        INVALID_ID_L
                )))
        );
    }

    @Test
    public void CheckTaskStatusData_ShouldReturn0_WhenInvalidData() {
        when(developerRepository.findById(VALID_ID_L)).thenReturn(Optional.of(new Developer(
                VALID_ID_L,
                VALID_NAME,
                VALID_ID,
                Specialization.BACKEND,
                VALID_ID
        )));

        assertAll(
                () -> assertEquals(0, dataValidation.checkTaskStatusData(new TaskDtoStatus(
                        "",
                        INVALID_ID_L,
                        "true",
                        VALID_DATE,
                        VALID_ID_L
                ), VALID_ID_L, VALID_ID_L)),
                () -> assertEquals(0, dataValidation.checkTaskStatusData(new TaskDtoStatus(
                        "",
                        VALID_ID_L,
                        "invalidInput",
                        VALID_DATE,
                        VALID_ID_L
                ), VALID_ID_L, VALID_ID_L)),
                () -> assertEquals(0, dataValidation.checkTaskStatusData(new TaskDtoStatus(
                        "",
                        VALID_ID_L,
                        "true",
                        "2000-50-234",
                        VALID_ID_L
                ), VALID_ID_L, VALID_ID_L)),
                () -> assertEquals(0, dataValidation.checkTaskStatusData(new TaskDtoStatus(
                        "",
                        VALID_ID_L,
                        "true",
                        VALID_DATE,
                        INVALID_ID_L
                ), VALID_ID_L, VALID_ID_L))
        );
    }

    @Test
    public void CheckTaskStatusData_ShouldReturn1_WhenFullValidData() {
        when(developerRepository.findById(VALID_ID_L)).thenReturn(Optional.of(new Developer(
                VALID_ID_L,
                VALID_NAME,
                VALID_ID,
                Specialization.BACKEND,
                VALID_ID
        )));

        assertEquals(1, dataValidation.checkTaskStatusData(new TaskDtoStatus(
                "",
                VALID_ID_L,
                "true",
                VALID_DATE,
                VALID_ID_L
        ), VALID_ID_L, VALID_ID_L));
    }

    @Test
    public void CheckTaskStatusData_ShouldReturn2_WhenPartValidData() {
        when(developerRepository.findById(VALID_ID_L)).thenReturn(Optional.of(new Developer(
                VALID_ID_L,
                VALID_NAME,
                VALID_ID,
                Specialization.BACKEND,
                VALID_ID
        )));

        assertEquals(2, dataValidation.checkTaskStatusData(new TaskDtoStatus(
                "",
                VALID_ID_L,
                "false",
                null,
                null
        ), VALID_ID_L, VALID_ID_L));
    }
}
