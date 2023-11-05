package com.project.projectmanagment_is;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.projectmanagment_is.model.*;
import com.project.projectmanagment_is.service.ProjectService;
import com.project.projectmanagment_is.service.dataValidation;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(com.project.projectmanagment_is.controller.ProjectController.class)
public class ControllerTests {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ProjectService projectService;
    @MockBean
    private dataValidation dataValidationMock;
    private WebApplicationContext webApplicationContext;
    private final String VALID_ID = "1";
    private final String INVALID_ID = "-1";
    private final String VALID_DATE = "2000-10-10";
    private final Long VALID_ID_L = Long.parseLong(VALID_ID);

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
    }

    public static String asJsonString(final Object o) {
        try {
            return new ObjectMapper().writeValueAsString(o);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void createDeveloper_ShouldCreateDeveloperInDB_WhenValidDataInput() throws Exception {
        when(dataValidationMock.checkNameIsBlank(anyString())).thenReturn(true);
        when(dataValidationMock.checkSpecialization(anyString())).thenReturn(true);
        mockMvc.perform(MockMvcRequestBuilders.post("/project/user/add")
                        .content(asJsonString(new DeveloperDto("name", "DEVOPS")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated());
    }

    @Test
    public void createDeveloper_ShouldReturnBadRequest_WhenInvalidSpecialization() throws Exception {
        when(dataValidationMock.checkNameIsBlank(anyString())).thenReturn(false);
        when(dataValidationMock.checkSpecialization(anyString())).thenReturn(false);
        mockMvc.perform(MockMvcRequestBuilders.post("/project/user/add")
                        .content(asJsonString(new DeveloperDto("", "badSpec")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    public void createDeveloper_ShouldReturnBadRequest_WhenInvalidName() throws Exception {
        when(dataValidationMock.checkNameIsBlank(anyString())).thenReturn(false);
        when(dataValidationMock.checkSpecialization(anyString())).thenReturn(true);
        mockMvc.perform(MockMvcRequestBuilders.post("/project/user/add")
                        .content(asJsonString(new DeveloperDto("", "badSpec")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    public void createProject_ShouldCreateProjectInDB_WhenValidDataInput() throws Exception {
        ProjectDto projectDto = new ProjectDto(
                VALID_ID_L,
                "project1",
                ""
        );
        when(dataValidationMock.checkProjectData(any(ProjectDto.class))).thenReturn(true);
        mockMvc.perform(MockMvcRequestBuilders.post("/project")
                        .content(asJsonString(projectDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
    }

    @Test
    public void createProject_ShouldReturnBadRequest_WhenInvalidDataInput() throws Exception {
        ProjectDto projectDto = new ProjectDto(
                Long.parseLong(INVALID_ID),
                "project1",
                ""
        );
        when(dataValidationMock.checkProjectData(any(ProjectDto.class))).thenReturn(false);
        mockMvc.perform(MockMvcRequestBuilders.post("/project")
                        .content(asJsonString(projectDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void getProject_ShouldReturnProjectById_WhenValidIdGiven() throws Exception {
        when(dataValidationMock.checkProjectExists(any())).thenReturn(true);
        mockMvc.perform(MockMvcRequestBuilders.get("/project/{projectId}", VALID_ID))
                .andExpect(status().isOk());
    }

    @Test
    public void getProject_ShouldReturnBadRequest_WhenInvalidIdGiven() throws Exception {
        when(dataValidationMock.checkProjectExists(any())).thenReturn(false);
        mockMvc.perform(MockMvcRequestBuilders.get("/project/{projectId}", INVALID_ID))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void getProjects_ShouldReturnAllProjects() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/project/all"))
                .andExpect(status().isOk());
    }

    @Test
    public void getDevProjects_ShouldReturnAllDevProjects_WhenValidDevIdGiven() throws Exception {
        when(dataValidationMock.checkDevExists(any())).thenReturn(true);
        mockMvc.perform(MockMvcRequestBuilders.get("/project/user/{devId}", VALID_ID))
                .andExpect(status().isOk());
    }

    @Test
    public void getDevProjects_ShouldReturnBadRequest_WhenInvalidDevIdGiven() throws Exception {
        when(dataValidationMock.checkDevExists(any())).thenReturn(false);
        mockMvc.perform(MockMvcRequestBuilders.get("/project/user/{devId}", INVALID_ID))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void createTask_ShouldCreateTaskInDB_WhenValidDataInput() throws Exception {
        when(dataValidationMock.checkProjectExists(any())).thenReturn(true);
        when(dataValidationMock.checkTaskData(any(), any())).thenReturn(true);
        TaskDto taskDto = new TaskDto(
                VALID_ID_L,
                VALID_DATE,
                "name",
                1,
                "BACKEND",
                "",
                null
        );
        mockMvc.perform(MockMvcRequestBuilders.post("/project/{projectId}/task", VALID_ID)
                        .content(asJsonString(taskDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
    }

    @Test
    public void createTask_ShouldReturnBadRequest_WhenInvalidDataInput() throws Exception {
        when(dataValidationMock.checkProjectExists(any())).thenReturn(false);
        when(dataValidationMock.checkTaskData(any(), any())).thenReturn(true);
        TaskDto taskDto = new TaskDto(
                Long.parseLong(INVALID_ID),
                VALID_DATE,
                "name",
                1,
                "BACKEND",
                "",
                null
        );
        mockMvc.perform(MockMvcRequestBuilders.post("/project/{projectId}/task", VALID_ID)
                        .content(asJsonString(taskDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void updateTask_ShouldReturnBadRequest_WhenInvalidDataAndChoice0() throws Exception {
        when(dataValidationMock.checkProjectExists(any())).thenReturn(true);
        when(dataValidationMock.checkProjectTaskExists(any(), any())).thenReturn(true);
        when(dataValidationMock.checkTaskStatusData(any(), any(), any())).thenReturn((short) 0);
        TaskDtoStatus taskDtoStatus = new TaskDtoStatus(
                "",
                VALID_ID_L,
                "true",
                VALID_DATE,
                Long.parseLong(INVALID_ID)
        );
        mockMvc.perform(MockMvcRequestBuilders.put("/project/{projectId}/task/{taskId}", VALID_ID, VALID_ID)
                        .content(asJsonString(taskDtoStatus))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

    }

    @Test
    public void updateTask_ShouldUpdateTaskDataInDB_WhenValidDataAndChoice1() throws Exception {
        when(dataValidationMock.checkProjectExists(any())).thenReturn(true);
        when(dataValidationMock.checkProjectTaskExists(any(), any())).thenReturn(true);
        when(dataValidationMock.checkTaskStatusData(any(), any(), any())).thenReturn((short) 1);
        when(projectService.getTask(any())).thenReturn(new Task(
                VALID_ID_L,
                VALID_ID_L,
                VALID_ID_L,
                LocalDate.parse(VALID_DATE),
                "name",
                1,
                Specialization.BACKEND,
                "",
                null,
                false,
                null,
                null,
                null
        ));
        when(projectService.getDeveloper(any())).thenReturn(new Developer(
                VALID_ID_L,
                "name",
                "",
                Specialization.BACKEND,
                ""
        ));
        TaskDtoStatus taskDtoStatus = new TaskDtoStatus(
                "",
                VALID_ID_L,
                "true",
                VALID_DATE,
                VALID_ID_L
        );
        mockMvc.perform(MockMvcRequestBuilders.put("/project/{projectId}/task/{taskId}", VALID_ID, VALID_ID)
                        .content(asJsonString(taskDtoStatus))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

    }

    @Test
    public void updateTask_ShouldUpdateTaskDataInDB_WhenValidDataAndChoice2() throws Exception {
        when(dataValidationMock.checkProjectExists(any())).thenReturn(true);
        when(dataValidationMock.checkProjectTaskExists(any(), any())).thenReturn(true);
        when(dataValidationMock.checkTaskStatusData(any(), any(), any())).thenReturn((short) 2);
        when(projectService.getTask(any())).thenReturn(new Task(
                VALID_ID_L,
                VALID_ID_L,
                VALID_ID_L,
                LocalDate.parse(VALID_DATE),
                "name",
                1,
                Specialization.BACKEND,
                "",
                null,
                false,
                null,
                null,
                null
        ));
        TaskDtoStatus taskDtoStatus = new TaskDtoStatus(
                "",
                VALID_ID_L,
                "false",
                null,
                null
        );
        mockMvc.perform(MockMvcRequestBuilders.put("/project/{projectId}/task/{taskId}", VALID_ID, VALID_ID)
                        .content(asJsonString(taskDtoStatus))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

    }

    @Test
    public void getProjectTask_ShouldReturnTaskData_WhenValidIdsGiven() throws Exception {
        when(dataValidationMock.checkProjectExists(any())).thenReturn(true);
        when(dataValidationMock.checkProjectTaskExists(any(), any())).thenReturn(true);
        mockMvc.perform(MockMvcRequestBuilders.get("/project/{projectId}/task/{taskId}/get", VALID_ID, VALID_ID))
                .andExpect(status().isOk());
    }

    @Test
    public void getProjectTask_ShouldReturnBadRequest_WhenInvalidIdsGiven() throws Exception {
        when(dataValidationMock.checkProjectExists(any())).thenReturn(false);
        when(dataValidationMock.checkProjectTaskExists(any(), any())).thenReturn(false);
        mockMvc.perform(MockMvcRequestBuilders.get("/project/{projectId}/task/{taskId}/get", INVALID_ID, INVALID_ID))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void getTask_ShouldReturnTaskData_WhenValidIdGiven() throws Exception {
        when(dataValidationMock.checkTaskExists(any())).thenReturn(true);
        mockMvc.perform(MockMvcRequestBuilders.get("/project/task/{taskId}/get", VALID_ID))
                .andExpect(status().isOk());
    }

    @Test
    public void getTask_ShouldReturnBadRequest_WhenInvalidIdGiven() throws Exception {
        when(dataValidationMock.checkTaskExists(any())).thenReturn(false);
        mockMvc.perform(MockMvcRequestBuilders.get("/project/task/{taskId}/get", INVALID_ID))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void getDev_ShouldReturnBadRequest_WhenInvalidIdGiven() throws Exception {
        when(dataValidationMock.checkDevExists(any())).thenReturn(false);
        mockMvc.perform(MockMvcRequestBuilders.get("/project/user/{devId}/get", INVALID_ID))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void getTask_ShouldReturnDevData_WhenValidIdGiven() throws Exception {
        when(dataValidationMock.checkDevExists(any())).thenReturn(true);
        mockMvc.perform(MockMvcRequestBuilders.get("/project/user/{devId}/get", VALID_ID))
                .andExpect(status().isOk());
    }

    @Test
    public void postAssignment_ShouldReturnBadRequest_WhenNoTasksAssigned() throws Exception {
        when(dataValidationMock.checkProjectExists(any())).thenReturn(true);
        mockMvc.perform(MockMvcRequestBuilders.post("/project/{projectId}/assignment", VALID_ID))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void postAssignment_ShouldReturnAssignment_WhenValidIdGiven() throws Exception {
        when(dataValidationMock.checkProjectExists(any())).thenReturn(true);
        when(projectService.postAssigment(any())).thenReturn(new ArrayList<>(Collections.singleton(new Pair<>(VALID_ID_L, VALID_ID_L))));
        mockMvc.perform(MockMvcRequestBuilders.post("/project/{projectId}/assignment", VALID_ID))
                .andExpect(status().isOk());
    }

    @Test
    public void postAssignment_ShouldReturnBadRequest_WhenInvalidIdGiven() throws Exception {
        when(dataValidationMock.checkProjectExists(any())).thenReturn(false);
        mockMvc.perform(MockMvcRequestBuilders.post("/project/{projectId}/assignment", INVALID_ID))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void acceptAllAssignments_ShouldAcceptAllAssignmentsOfAProject_WhenConditionsMet() throws Exception {
        when(dataValidationMock.checkProjectHasAssignment(VALID_ID_L, true, 0)).thenReturn(true);
        when(projectService.acceptAllAssignments(any())).thenReturn(true);
        mockMvc.perform(MockMvcRequestBuilders.put("/project/{projectId}/assignment/all", VALID_ID))
                .andExpect(status().isOk());
    }

    @Test
    public void acceptAllAssignments_ShouldReturnBadRequest_WhenNoAssignmentDone() throws Exception {
        when(dataValidationMock.checkProjectHasAssignment(VALID_ID_L, true, 0)).thenReturn(false);
        mockMvc.perform(MockMvcRequestBuilders.put("/project/{projectId}/assignment/all", VALID_ID))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void acceptAllAssignments_ShouldReturnInternalError_WhenUnableToAssign() throws Exception {
        when(dataValidationMock.checkProjectHasAssignment(VALID_ID_L, true, 0)).thenReturn(true);
        when(projectService.acceptAllAssignments(any())).thenReturn(false);
        mockMvc.perform(MockMvcRequestBuilders.put("/project/{projectId}/assignment/all", VALID_ID))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void acceptOneAssignment_ShouldAcceptThatAssignmentForThatProject_WhenConditionsMet() throws Exception {
        when(dataValidationMock.checkProjectHasAssignment(VALID_ID_L, false, Integer.parseInt(VALID_ID))).thenReturn(true);
        when(projectService.acceptOneAssignment(any(), anyInt())).thenReturn(true);
        mockMvc.perform(MockMvcRequestBuilders.put("/project/{projectId}/assignment/{assignmentId}", VALID_ID, VALID_ID))
                .andExpect(status().isOk());
    }

    @Test
    public void acceptOneAssignment_ShouldReturnBadRequest_WhenNoAssignmentDone() throws Exception {
        when(dataValidationMock.checkProjectHasAssignment(VALID_ID_L, false, Integer.parseInt(INVALID_ID))).thenReturn(false);
        mockMvc.perform(MockMvcRequestBuilders.put("/project/{projectId}/assignment/{assignmentId}", VALID_ID, INVALID_ID))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void acceptOneAssignment_ShouldReturnInternalError_WhenUnableToAssign() throws Exception {
        when(dataValidationMock.checkProjectHasAssignment(VALID_ID_L, false, Integer.parseInt(VALID_ID))).thenReturn(true);
        when(projectService.acceptOneAssignment(any(), anyInt())).thenReturn(false);
        mockMvc.perform(MockMvcRequestBuilders.put("/project/{projectId}/assignment/{assignmentId}", VALID_ID, VALID_ID))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void getMostTasksDev_ShouldReturnDevWithMostCompleted() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/project/user/most"))
                .andExpect(status().isOk());
    }
}
