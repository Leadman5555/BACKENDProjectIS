package com.project.projectmanagment_is.controller;

import com.project.projectmanagment_is.Pair;
import com.project.projectmanagment_is.model.*;
import com.project.projectmanagment_is.service.ProjectService;
import com.project.projectmanagment_is.service.dataValidation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/project")
public class ProjectController {
    private final ProjectService projectService;
    private final dataValidation dataValidation;
    private static final Long EMPTY_ID = null;
    private static final String EMPTY_IDS = "";

    //------------------------------------ POST REQUESTS ---------------------------------------------------

    /**
     * Creates new Developer record in the database. This POST request is essential for the rest of the
     * program and should always be invoked first. Data given checked by dataValidation service.
     *
     * @RequestBody: DeveloperDto
     * @Results: (1) Valid data, new entry in database created, Http status 201 (CREATED),
     * returns full new Developer body.
     * <p></p>(2) Invalid data or part of it, Http status 400 (BAD_REQUEST),
     * returns error message with its source specified.
     * @URL: POST http://localhost:8080/project/user/add
     */
    @PostMapping("/user/add")
    public ResponseEntity<Object> createDeveloper(@RequestBody DeveloperDto developerDto) {
        //Checks the validity of data
        if (!dataValidation.checkSpecialization(developerDto.getSpecialization().toUpperCase())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Wrong specialization.");
        } else if (!dataValidation.checkNameIsBlank(developerDto.getName())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Name cannot be blank");
        } else {
            //Data is valid, creates new record in the database
            Developer developer = projectService.createDeveloper(new Developer(
                    EMPTY_ID,
                    developerDto.getName(),
                    EMPTY_IDS,
                    Enum.valueOf(Specialization.class, developerDto.getSpecialization().toUpperCase()),
                    EMPTY_IDS
            ));
            return ResponseEntity.status(HttpStatus.CREATED).body(developer);
        }
    }

    /**
     * Creates new Project record in the database. This POST request requires an existing Developer.
     * Data given checked by dataValidation service.
     *
     * @RequestBody: ProjectDto
     * @Results: (1) Valid data, new entry in database created, Http status 201 (CREATED), returns full new Project body,
     * the developer specified as the creator of the project has the project Id saved in his database record.
     * <p></p>(2) Invalid data or part of it, Http status 400 (BAD_REQUEST),
     * returns error message with its source specified.
     * @URL: POST http://localhost:8080/project
     */
    @PostMapping("")
    public ResponseEntity<Object> createProject(@RequestBody ProjectDto projectDto) {
        //Adds the creator Developer Id to the project Dev team.
        projectDto.setDevIdList(projectDto.getCreatorDevId() + "," + projectDto.getDevIdList());
        //Checks the validity of data given
        if (dataValidation.checkProjectData(projectDto)) {
            //Creates new entry in the database and linking the project to the creator Developer by the project Id
            Project project = projectService.createProject(new Project(
                    EMPTY_ID,
                    projectDto.getCreatorDevId(),
                    projectDto.getProjectName(),
                    projectDto.getDevIdList(),
                    ""
            ), true);
            return ResponseEntity.status(HttpStatus.CREATED).body(project);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Developers listed do not exist and/or are duplicate. Name cannot be blank");
        }
    }

    /**
     * Creates new Task record in the database. This POST request requires an existing Project and an existing
     * Developer. Created tasks are always unfinished when added to the project.
     * Data given checked by dataValidation service.
     *
     * @param projectId Id of the project that the task is being linked to
     * @RequestBody: TaskDto
     * @Results: (1) Valid data, new entry in database created, Http status 201 (CREATED), returns full new Task body,
     * adds the task Id to the project it is being linked to, updating the project record in the database.
     * If a developer has not been assigned to the task, the field value remains empty.<p></p>
     * (2) Invalid data or part of it, Http status 400 (BAD_REQUEST), returns error message with its source
     * specified.
     * -
     * @URL: POST http://localhost:8080/project/{projectId}/task
     */
    @PostMapping("/{projectId}/task")
    public ResponseEntity<Object> createTask(@PathVariable Long projectId, @RequestBody TaskDto taskDto) {
        if (!dataValidation.checkProjectExists(projectId)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No such project exists");
        }
        if (!dataValidation.checkTaskData(projectId, taskDto)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Wrong input data. Check: name, specialization, creator developer Id, date format (YYYY-MM-DD), estimation value or if the assigned developer is added to the project's dev team.");
        }
        Task task = projectService.createTask(new Task(
                EMPTY_ID,
                projectId,
                taskDto.getCreatorDevId(),
                LocalDate.parse(taskDto.getDateCreated()),
                taskDto.getTaskName(),
                taskDto.getEstimation(),
                Enum.valueOf(Specialization.class, taskDto.getSpecialization()),
                taskDto.getTaskComment(),
                taskDto.getAssignedDevId(),
                false,
                null,
                null,
                null
        ));
        return ResponseEntity.status(HttpStatus.CREATED).body(task);
    }

    /**
     * This POST request runs the assignment algorithm on the specified project.
     * Data given checked by dataValidation service.
     *
     * @param projectId Id of the project that the algorithm is being run on
     * @Results: (1) The project has unassigned tasks, the algorithm succeeds, saves the suggested assignments to a
     * runtime-only list, Http status 200 (OK) and returns the suggested assignment pairs for that project.
     * <p></p>(2) Project of specified Id doesn't exist or has no unassigned tasks,
     * Http status 400 (BAD_REQUEST), returns error message with its source specified.
     * @URL: POST http://localhost:8080/project/{projectId}/assignment
     */
    @PostMapping("/{projectId}/assignment")
    public ResponseEntity<Object> postAssignment(@PathVariable Long projectId) {
        //checks if the project exists and if any tasks were assigned by algorithm
        if (!dataValidation.checkProjectExists(projectId))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No such project exists.");
        List<Pair<Long, Long>> assigment = projectService.postAssigment(projectId);
        if (assigment == null || assigment.isEmpty())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No tasks to assign.");
        //returns the suggested assignment list
        return ResponseEntity.status(HttpStatus.OK).body(assigment);
    }

    //------------------------------------ GET REQUESTS ---------------------------------------------------

    /**
     * This GET request returns the full data of project of specified Id form the database.
     * Data given checked by dataValidation service.
     *
     * @param projectId Id of the project that should be returned
     * @Results: (1) Project exists, Http status 200 (OK) and returns the full data of that project.
     * <p></p>(2) Project of specified Id doesn't exist, Http status 400 (BAD_REQUEST),
     * returned error message with its source specified.
     * @URL: GET http://localhost:8080/project/{projectId}
     */
    @GetMapping("/{projectId}")
    public ResponseEntity<Object> getProject(@PathVariable Long projectId) {
        if (dataValidation.checkProjectExists(projectId))
            return ResponseEntity.status(HttpStatus.OK).body(projectService.getProject(projectId));
        else
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No such project exists");
    }

    /**
     * This GET request returns the full data of all projects form the database.
     * Data given checked by dataValidation service.
     *
     * @Results: (1) All existing projects have their full data returned, Http status 200 (OK).
     * If none exist, the returned list is empty.
     * @URL: GET http://localhost:8080/project/all
     */
    @GetMapping("/all")
    public ResponseEntity<List<Project>> getProjects() {
        return ResponseEntity.status(HttpStatus.OK).body(projectService.getProjects());
    }

    /**
     * This GET request returns the full data of all projects created by the Developer of specified Id form the database.
     * Data given checked by dataValidation service.
     *
     * @param devId Id of the developer whose projects' data should be displayed
     * @Results: (1) Developer exists, Http status 200 (OK),
     * returns the full data of all projects created by that developer.
     * <p></p>(2) Developer of specified Id doesn't exist, Http status 400 (BAD_REQUEST),
     * returns error message with its source specified.
     * @URL: GET http://localhost:8080/project/user/{devId}
     */
    @GetMapping("/user/{devId}")
    public ResponseEntity<List<Project>> getDevProjects(@PathVariable Long devId) {
        if (dataValidation.checkDevExists(devId))
            return ResponseEntity.status(HttpStatus.OK).body(projectService.getDevProjects(devId));
        else
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);

    }


    /**
     * This GET request returns the full data of the task of specified Id belonging to the project of specified Id form the database.
     * Data given checked by dataValidation service.
     *
     * @param projectId Id of the project that has the task linked to it
     * @param taskId    Id of the task whose full data should be returned
     * @Results: (1) Project exists, task is linked to it, Http status 200 (OK), returns the full data of that task.
     * <p></p>(2) Invalid data given, Http status 400 (BAD_REQUEST),
     * returns error message with its source specified.
     * @URL: GET http://localhost:8080/project/{projectId}/task/{taskId}/get
     */
    @GetMapping("/{projectId}/task/{taskId}/get")
    public ResponseEntity<Object> getProjectTask(@PathVariable Long projectId, @PathVariable Long taskId) {
        if (!dataValidation.checkProjectExists(projectId))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No such project exists.");
        if (!dataValidation.checkProjectTaskExists(projectId, taskId))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No such task is added to the project.");
        return ResponseEntity.status(HttpStatus.OK).body(projectService.getTask(taskId));
    }

    /**
     * This GET request returns the full data of the task of specified Id form the database without
     * the need of specifying the project it should be linked to. Data given checked by dataValidation service.
     *
     * @param taskId Id of the task whose full data should be returned
     * @Results: (1) Task exists, Http status 200 (OK), returns the full data of that task.
     * <p></p>(2) Task doesn't exist, Http status 400 (BAD_REQUEST),
     * returns error message with its source specified.
     * @URL: GET http://localhost:8080/project/task/{taskId}/get
     */
    @GetMapping("/task/{taskId}/get")
    public ResponseEntity<Object> getTask(@PathVariable Long taskId) {
        if (!dataValidation.checkTaskExists(taskId))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No such task exists");
        return ResponseEntity.status(HttpStatus.OK).body(projectService.getTask(taskId));
    }

    /**
     * This GET request returns the full data of the developer of specified Id form the database.
     *
     * @param devId Id of the developer whose full data should be returned
     * @Results: (1) Developer exists, Http status 200 (OK), returns the full data of that developer.
     * <p></p>(2) Developer doesn't exist, Http status 400 (BAD_REQUEST),
     * returns error message with its source specified.
     * @URL: GET http://localhost:8080/project/user/{devId}/get
     */
    @GetMapping("/user/{devId}/get")
    public ResponseEntity<Object> getDev(@PathVariable Long devId){
        if(!dataValidation.checkDevExists(devId))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No such developer exists");
        return ResponseEntity.status(HttpStatus.OK).body(projectService.getDeveloper(devId));
    }
    /**
     * This GET request returns the full data of a developer with most tasks completed from the database.
     *
     * @Results: (1) Http status 200 (OK), returns the full data of that developer
     * @URL: GET http://localhost:8080/project/user/most
     */
    @GetMapping("/user/most")
    public ResponseEntity<Developer> getMostTasksDev(){
        return ResponseEntity.status(HttpStatus.OK).body(projectService.getMostTasksDev());
    }

    //------------------------------------ PUT REQUESTS ---------------------------------------------------

    /**
     * This PUT request updates the data of the task of specified Id from the project of specified Id.
     * If the state of the task is changed (task is completed)
     * method saves the most important parameters of the task to the database record of the developer who completed it,
     * updating their record. Data given checked by dataValidation service.
     *
     * @param projectId Id of the project that has the task linked to it
     * @param taskId    Id of the task whose data should be updated
     * @RequestBody: TaskDtoStatus (if task is not finished, the latter part of the body can be omitted at input)
     * @Results: (1) The project exists, has the task linked to it and the data given is valid, Http status 200 (OK),
     * updates that task's data in the database,
     * returns the full data of the newly updated task. If task was declared as done,
     * updates the record of the developer who finished it in the database.
     * <p></p>(2) All or part of the data is invalid, Http status 400 (BAD_REQUEST),
     * returns error message with its source specified.
     * @URL: PUT http://localhost:8080/project/{projectId}/task/{taskId}
     */
    @PutMapping("/{projectId}/task/{taskId}")
    public ResponseEntity<Object> updateTask(@PathVariable Long projectId, @PathVariable Long taskId, @RequestBody TaskDtoStatus taskDtoStatus) {
        //Checks the validity of data given.
        if (!dataValidation.checkProjectExists(projectId))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No such project exists");
        if (!dataValidation.checkProjectTaskExists(projectId, taskId))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No such task exists in the specified project");
        short choice = dataValidation.checkTaskStatusData(taskDtoStatus, projectId, taskId);
        if (choice == 0)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid task data. Check assigned developer Id and if it matches the end Id, if task status if correct and if date of completion comes after or is equal to the creation date");
        //Input was deemed valid. The "choice" variable now represents if the task was declared as
        //finished (1) or not (2) after the update.
        //Default values are for an unfinished task.
        Task oldTask = projectService.getTask(taskId);
        LocalDate doneDate = null;
        boolean taskState = false;
        Long doneDevId = null;
        Long timeDone = null;
        String newComment = taskDtoStatus.getTaskComment();
        if (newComment.isEmpty() || newComment.equals(oldTask.getTaskComment()))
            newComment = oldTask.getTaskComment();
        //The default values are modified if the task was declared as finished.
        if (choice == 1) {
            taskState = true;
            doneDate = LocalDate.parse(taskDtoStatus.getDateDone());
            doneDevId = taskDtoStatus.getDoneDevId();
            timeDone = ChronoUnit.DAYS.between(oldTask.getDateCreated(), doneDate);
            //Updates the record of the developer who finished the task in the database.
            Developer developer = projectService.getDeveloper(doneDevId);
            developer.addTaskToList(taskId, oldTask.getEstimation(), timeDone);
            projectService.createDeveloper(new Developer(
                    doneDevId,
                    developer.getName(),
                    developer.getProjectIds(),
                    developer.getSpecialization(),
                    developer.getDoneTaskIds()
            ));
        }
        //Updates the task record in the database
        Task newTask = projectService.updateTask(new Task(
                oldTask.getTaskId(),
                projectId,
                oldTask.getCreatorDevId(),
                oldTask.getDateCreated(),
                oldTask.getTaskName(),
                oldTask.getEstimation(),
                oldTask.getSpecialization(),
                newComment,
                taskDtoStatus.getAssignedDevId(),
                taskState,
                doneDate,
                timeDone,
                doneDevId
        ));
        return ResponseEntity.status(HttpStatus.OK).body(newTask);
    }

    /**
     * This PUT request accepts all previously generated assignment suggestions for the project of specified Id,
     * updating the records for the tasks in the database. Data given checked by dataValidation service.
     *
     * @param projectId Id of the project for which the generated assignment suggestions should be accepted
     * @Results: (1) The project exists and the assignment algorithm has been run on it, Http status 200 (OK),
     * updates that tasks' data in the database.
     * <p></p>(2) The project doesn't exist or the assignment algorithm hasn't been previously run on it
     * during the current run-time, Http status 400 (BAD_REQUEST), returns error message with its source specified.
     * <p></p>(3) After having run the assignment algorithm, the certain records in the database have been modified,
     * making the assignment suggestions no longer valid, Http status 500 (INTERNAL_SERVER_ERROR),
     * returns error message.
     * @URL: PUT http://localhost:8080/project/{projectId}/assignment/all
     */
    @PutMapping("/{projectId}/assignment/all")
    public ResponseEntity<Object> acceptAllAssignments(@PathVariable Long projectId) {
        if (!dataValidation.checkProjectHasAssignment(projectId, true, 0))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Run the assignment algorithm on the project first.");
        if (projectService.acceptAllAssignments(projectId))
            return ResponseEntity.status(HttpStatus.OK).build();
        //If an unknown error with the database records occurred during accepting the assignment
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong.");
    }

    /**
     * This PUT request accepts one of the previously generated assignment suggestions for the project of specified Id,
     * updating the records for the task in the database. Data given checked by dataValidation service.
     *
     * @param projectId    Id of the project for which the generated assignment suggestions should be accepted
     * @param assignmentId Id of the suggested assignment for the specified project
     * @Results: (1) The project exists and the assignment algorithm has been run on it, Http status 200 (OK),
     * updates that task's data in the database.
     * <p></p>(2) The project doesn't exist, the assignment algorithm hasn't been previously run on it
     * during the current run-time or the specified assignment does not exist, Http status 400 (BAD_REQUEST),
     * returns error message with its source specified.
     * <p></p>(3) After having run the assignment algorithm, the certain records in the database have been modified,
     * making the assignment suggestions no longer valid, Http status 500 (INTERNAL_SERVER_ERROR), returns error message.
     * @URL: PUT http://localhost:8080/project/{projectId}/assignment/{assignmentId}
     */
    @PutMapping("/{projectId}/assignment/{assignmentId}")
    public ResponseEntity<Object> acceptOneAssignment(@PathVariable Long projectId, @PathVariable int assignmentId) {
        if (!dataValidation.checkProjectHasAssignment(projectId, false, assignmentId))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Run the assignment algorithm on the project first and/or checks if assignment of such Id exists.");
        if (projectService.acceptOneAssignment(projectId, assignmentId))
            return ResponseEntity.status(HttpStatus.OK).build();
        //If an unknown error with the database records occurred during accepting the assignment
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong.");
    }
}
