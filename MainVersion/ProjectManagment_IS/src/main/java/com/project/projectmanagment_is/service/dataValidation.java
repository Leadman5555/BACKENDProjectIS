package com.project.projectmanagment_is.service;

import com.project.projectmanagment_is.PairOuter;
import com.project.projectmanagment_is.ProjectManagmentIsApplication;
import com.project.projectmanagment_is.model.*;
import com.project.projectmanagment_is.repository.DeveloperRepository;
import com.project.projectmanagment_is.repository.ProjectRepository;
import com.project.projectmanagment_is.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class dataValidation {
    @Autowired
    private DeveloperRepository developerRepository;
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private TaskRepository taskRepository;
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * This bool method checks the validity of the user-input date format. The accepted format is
     * YYYY-MM-DD.
     *
     * @param date String that is supposed to be a valid date when parsed.
     * @Results: (1) String is a valid date, returns true.
     * <p></p>(2) String is not a valid date, returns false.
     */
    public boolean checkDate(String date) {
        //Checks if the String can be formatted into a date
        LocalDate newDate;
        try {
            newDate = LocalDate.parse(date, dateTimeFormatter);
        } catch (DateTimeException e) {
            return false;
        }
        //Checks for the exception where the day number is bigger than the maximum day number for a month by
        //one, for example 2023-11-31, where the maximum value is 30.
        String tmp = new StringBuilder().append(date.charAt(8)).append(date.charAt(9)).toString();
        return String.valueOf(newDate.getDayOfMonth()).equals(tmp);
    }

    /**
     * This bool method checks if the specified project contains a specified task in its task list.
     *
     * @param projectId Id of the project that is supposed to contain the specified task
     * @param taskId    Id of that task
     * @Results: (1) Project contains the task, returns true.
     * <p></p>(2) Project doesn't exist or the task isn't linked to it, returns false.
     */
    public boolean checkProjectTaskExists(Long projectId, Long taskId) {
        Project project = projectRepository.findById(projectId).orElseThrow();
        List<Long> taskList = project.getTaskIdList_Long();
        return taskList.contains(taskId);
    }

    /**
     * This validation method is checks if the data for updating the task data is valid. Apart from that
     * it returns the user decision if the task was declared as completed (1) or not (2).
     *
     * @param taskDtoStatus The data transfer object for updating the data of the task
     * @param projectId     Id of the project that the task is linked to
     * @param taskId        Id of the task
     * @Results: (1) Data is valid, task was declared as finished, returns 1.
     * <p></p>(2) Data is valid, task was declared as unfinished (in-progress), returns 2.
     * <p></p>(3) Data invalid, returns 0.
     */
    public short checkTaskStatusData(TaskDtoStatus taskDtoStatus, Long projectId, Long taskId) {
        //Checks the overall validity fo the input
        Project project = projectRepository.findById(projectId).orElseThrow();
        List<Long> devList = project.getDevIdList_Long();
        if (devList.contains(taskDtoStatus.getAssignedDevId())) {
            Developer tmpDev = developerRepository.findById(taskDtoStatus.getAssignedDevId()).orElseThrow();
            if (!tmpDev.getSpecialization().equals(taskRepository.findById(taskId).orElseThrow().getSpecialization()))
                return 0;
        } else
            return 0;
        //Checks if the task was declared as completed or not yet.
        boolean choice = false;
        //Possible options for user input that can be interpreted as declaring the task state
        String[] optionsTrue = {"true", "True", "TRUE", "1", "Done", "done", "DONE", "completed", "COMPLETED", "Completed", "Finished", "finished", "FINISHED"};
        String[] optionsFalse = {"false", "False", "FALSE", "0", "Not done", "not done", "NOT DONE", "uncompleted", "UNCOMPLETED", "Uncompleted", "Unfinished", "unfinished", "UNFINISHED"};
        for (int i = 0; i < optionsFalse.length; i++) {
            if (optionsTrue[i].equals(taskDtoStatus.getTaskState())) {
                choice = true;
                break;
            }
            if (optionsFalse[i].equals(taskDtoStatus.getTaskState())) {
                return 2;
            }
        }
        //If none of the possible options match, the input is deemed invalid
        if (!choice)
            return 0;
        //If task was declared as done, check the validity of the rest of the data
        if (taskDtoStatus.getDateDone() == null)
            return 0;
        if (!checkDate(taskDtoStatus.getDateDone()))
            return 0;
        //Checks if date of creation of the task precedes the date of completion.
        Task task = taskRepository.findById(taskId).orElseThrow();
        if (!LocalDate.parse(taskDtoStatus.getDateDone()).isAfter(task.getDateCreated()) && !LocalDate.parse(taskDtoStatus.getDateDone()).isEqual(task.getDateCreated()))
            return 0;
        if (!taskDtoStatus.getAssignedDevId().equals(taskDtoStatus.getDoneDevId())) {
            return 0;
        }

        return 1;
    }

    /**
     * This bool method checks the validity of the project data.
     *
     * @param projectDto Data transfer object used to save user-input data for a new project
     * @Results: (1) Data is valid, returns true.
     * <p></p>(2) Data is not valid, returns false.
     */
    public boolean checkProjectData(ProjectDto projectDto) {
        if (!checkNameIsBlank(projectDto.getProjectName()))
            return false;
        //Checks for duplicate developer Ids in the project developer team
        List<Long> devIds = projectDto.getDevIdList_Long();
        if (!devIds.isEmpty()) {
            Set<Long> set = new HashSet<>(devIds);
            if (set.size() < devIds.size())
                return false;
            //And if each of them exists
            for (Long devId : devIds) {
                if (!checkDevExists(devId))
                    return false;
            }
        }
        return true;
    }

    /**
     * This bool method checks if the project of given Id exists in the database.
     *
     * @param projectId Id of the project that is supposed to exist
     * @Results: (1) Project exists, returns true.
     * <p></p>(2) Project does not exist, returns false.
     */
    public boolean checkProjectExists(Long projectId) {
        return projectRepository.existsById(projectId);
    }

    /**
     * This bool method checks if the given specialization is valid
     *
     * @param specialization String with the user-input specialization (always UPPER-case when this method is used)
     * @Results: (1) Specialization valid, returns true.
     * <p></p>(2) Specialization not valid, returns false.
     */
    public boolean checkSpecialization(String specialization) {
        return (specialization.equals("BACKEND") || specialization.equals("FRONTEND") || specialization.equals("DEVOPS") || specialization.equals("UX_UI"));
    }

    /**
     * This bool method checks if the developer of given Id exists in the database.
     *
     * @param devId Id of the developer that is supposed to exist
     * @Results: (1) Developer exists, returns true.
     * <p></p>(2) Developer does not exist, returns false.
     */
    public boolean checkDevExists(Long devId) {
        return developerRepository.existsById(devId);
    }

    /**
     * This bool method checks if the data of the task is valid.
     *
     * @param projectId Id of the project that the task is being linked to
     * @param taskDto   Data transfer object with the user-input data of the new task
     * @Results: (1) Data valid, returns true.
     * <p></p>(2) Data invalid, returns false.
     */
    public boolean checkTaskData(Long projectId, TaskDto taskDto) {
        //Checks the overall validity of the data
        if (!checkSpecialization(taskDto.getSpecialization().toUpperCase()))
            return false;
        if (!checkNameIsBlank(taskDto.getTaskName()))
            return false;
        if (!checkDevExists(taskDto.getCreatorDevId()))
            return false;
        if (!checkDate(taskDto.getDateCreated()))
            return false;
        //Checks if the given estimation of the task is correct
        if (!ProjectManagmentIsApplication.estimationValues.contains(taskDto.getEstimation()))
            return false;
        //If a developer was assigned to the task at its creation, checks if their specialization matches
        //the task's specialization and if they belong to the project's developer team
        if (taskDto.getAssignedDevId() != null) {
            Project project = projectRepository.findById(projectId).orElseThrow();
            if (!project.getDevIdList_Long().contains(taskDto.getAssignedDevId())) {
                Developer tmpDev = developerRepository.findById(taskDto.getAssignedDevId()).orElseThrow();
                return tmpDev.getSpecialization().toString().equals(taskDto.getSpecialization());
            } else
                return false;
        }
        return true;
    }

    /**
     * This bool method checks if the String (name) is not empty or filled with ' ' only.
     *
     * @param name User-input String that is supposed to be a name for an object
     * @Results: (1) Name valid, returns true.
     * <p></p>(2) Name invalid, returns false.
     */
    public boolean checkNameIsBlank(String name) {
        return name != null && !name.isBlank();
    }

    /**
     * This bool method checks if the task of given Id exists in the database.
     *
     * @param taskId Id of the task that is supposed to exist
     * @Results: (1) Task exists, returns true.
     * <p></p>(2) Task does not exist, returns false.
     */
    public boolean checkTaskExists(Long taskId) {
        return taskRepository.existsById(taskId);
    }

    /**
     * This bool method checks if the assignment algorithm has been run on the specified project,
     * if it exists, and, if user chooses not to accept all assignment for that project,
     * it checks if the particular assignment that user wants to accept exists.
     *
     * @param projectId    Id of the project that the algorithm was run on
     * @param all          Boolean that decides if user wants to accept all or just one assignment
     * @param assignmentId Id of the assignment user wants to accept if the 'all' boolean is false
     * @Results: (1) Assignment algorithm has been run on that project, user wants all assignments, returns true.
     * <p></p>(2) Assignment algorithm has been run on that project, the assignment user wants exists, return true.
     * <p></p>(3) Assignment algorithm hasn't been run on that project during this run-time
     * or the particular assignment user wants does not exist, returns false.
     */
    public boolean checkProjectHasAssignment(Long projectId, boolean all, int assignmentId) {
        for (PairOuter pair : ProjectManagmentIsApplication.assignmentList) {
            if (pair.getL().equals(projectId)) {
                if (all)
                    return true;
                else {
                    //Checks if the Id of the assignment user wants to accept is valid.
                    String pairs = (String) pair.getR();
                    //Counts the number of assignment pair for that project and checks if it includes the
                    //assignment pair user wants. It also checks if the assignmentId >=1.
                    return (pairs.length() - pairs.replaceAll(";", "").length() >= assignmentId) && assignmentId >= 1;
                }
            }
        }
        return false;
    }
}
