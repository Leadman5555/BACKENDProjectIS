package com.project.projectmanagment_is.service;

import com.project.projectmanagment_is.Pair;
import com.project.projectmanagment_is.PairOuter;
import com.project.projectmanagment_is.ProjectManagmentIsApplication;
import com.project.projectmanagment_is.model.Developer;
import com.project.projectmanagment_is.model.Project;
import com.project.projectmanagment_is.model.Specialization;
import com.project.projectmanagment_is.model.Task;
import com.project.projectmanagment_is.repository.DeveloperRepository;
import com.project.projectmanagment_is.repository.ProjectRepository;
import com.project.projectmanagment_is.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ProjectService {
    @Autowired
    private DeveloperRepository developerRepository;
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private TaskRepository taskRepository;

    /**
     * This method saves the Developer object in the database and returns its body.
     *
     * @param developer Developer object that is to be saved in the database
     * @Results: (1) Saves the developer in the database as a record, returns its body.
     */
    public Developer createDeveloper(Developer developer) {
        return developerRepository.save(developer);
    }

    /**
     * This method saves a new project to the database as well as, if addToList is true,
     * updates the database record of the developer who created the project, linking them to it.
     *
     * @param project   Project object that is to be saved in that database
     * @param addToList Boolean. This method can be used both to create new project and to
     *                  update it. If the boolean value is true, it means that we create the project, if it's false
     *                  we only update its data.
     * @Results: (1) Saves the project in the database as a record, updated the record of the developer
     * who created it, returns the project body.
     * (2) Updates the record of the project in the database, returns its body.
     */
    public Project createProject(Project project, boolean addToList) {
        projectRepository.save(project);
        //If a new project is created, its Id is added to the project Id list of the developer who was the
        //creator of the project.
        if (addToList) {
            Developer developer = developerRepository.findById(project.getCreatorDevId()).orElseThrow();
            //This line fixes a bug with MySQL database when saving an empty String ("") to it.
            //In short, MySQL writes it as <null> which is sometimes wrongly interpreted as "<null>" String
            //instead of an empty or null String, especially when appending extra content to that String.
            if (Objects.equals(developer.getProjectIds(), null))
                developer.setProjectIds(null);

            developer.addProjectToList(project.getProjectId());
            createDeveloper(new Developer(
                    developer.getDevId(),
                    developer.getName(),
                    developer.getProjectIds(),
                    developer.getSpecialization(),
                    developer.getDoneTaskIds()
            ));
        }
        return project;
    }

    /**
     * This method returns the body of an existing project in the database.
     *
     * @param projectId Id of the project that is supposed to exist
     * @Results: (1) Project exists, returns its body.
     * <p></p> (2) Project does not exist, throws exception, methods that called this method  display error message.
     */
    public Project getProject(Long projectId) {
        return projectRepository.findById(projectId).orElseThrow();
    }

    /**
     * This method returns the body of an existing task in the database.
     *
     * @param taskId Id of the task that is supposed to exist
     * @Results: (1) Task exists, returns its body.
     * <p></p> (2) Task does not exist, throws exception, methods that called this method  display error message.
     */
    public Task getTask(Long taskId) {
        return taskRepository.findById(taskId).orElseThrow();
    }

    /**
     * This method returns the list of all existing projects in the database.
     *
     * @Results: (1) Returns a list with all the existing projects, the list can be empty if none exist.
     */
    public List<Project> getProjects() {
        return projectRepository.findAll();
    }

    /**
     * This method returns a list of all projects created by a developer of specified Id. This method is always
     * called after checking if the specified developer exists in the database.
     *
     * @param devId Id of the developer whose projects user wants to get
     * @Results: (1) Developer exists, returns list with all their projects (can be empty).
     */
    public List<Project> getDevProjects(Long devId) {
        List<Project> allProjects = getProjects();
        List<Project> tmpList = new ArrayList<>();
        for (Project project : allProjects) {
            if (project.getCreatorDevId().equals(devId))
                tmpList.add(project);
        }
        return tmpList;
    }

    /**
     * This method saves the Task object in the database, updates the record of the project the task
     * is being linked to and returns the task's body.
     *
     * @param task Task object that is to be saved in the database
     * @Results: (1) Saves the task in the database as a record, adds its Id to the project in the database,
     * returns the task's body.
     */
    public Task createTask(Task task) {
        taskRepository.save(task);
        //Updates the project data, adding the task Id to the project's taskList.
        Project project = projectRepository.findById(task.getProjectId()).orElseThrow();
        project.addTaskToList(task.getTaskId());
        createProject(new Project(
                project.getProjectId(),
                project.getCreatorDevId(),
                project.getProjectName(),
                project.getDevIdList(),
                project.getTaskIdList()
        ), false);
        return task;
    }

    /**
     * This method updates the Task object in the database and returns its body.
     *
     * @param task Task object that is to be updated in the database
     * @Results: (1) Updates the task in the database, returns its body.
     */
    public Task updateTask(Task task) {
        return taskRepository.save(task);
    }

    /**
     * This method returns the body of an existing developer in the database.
     *
     * @param devId Id of the task that is supposed to exist
     * @Results: (1) Developer exists, returns its body.
     * <p></p> (2) Developer does not exist, throws exception, methods that called this method display error message.
     */
    public Developer getDeveloper(Long devId) {
        return developerRepository.findById(devId).orElseThrow();
    }

    /**
     * This method is the assignment algorithm. Upon being called, it creates suggested assignment pairs
     * for the specified by Id project. The main criteria of the algorithm are: <p>
     * On the team > Specialization match > Equality > History overall > Time optimization by history <p>
     * It means that for the current task, in order of importance, the developer who is assigned: <p>
     * - Is on that project's developer team. <p>
     * - Has matching specialization. <p>
     * - Is assigned the smallest number of tasks from those eligible (also counts the tasks outside the algorithm). <p>
     * - Has previously done tasks with the same estimation. <p>
     * - Did those tasks in the shortest time (the least days)
     *
     * @param projectId Id of the project that the algorithm is being run on
     * @Results: (1) Saves, or updates, the suggested assignment in the run-time only list not implementing it,
     * returns the list of assignments for the project (list may be empty).
     */
    public List<Pair<Long, Long>> postAssigment(Long projectId) {
        final int MAX_INT = 100000000;
        List<Pair<Long, Long>> returnedAssignmentList = new ArrayList<>();

        Project project = getProject(projectId);
        //Ids of all tasks currently link to the project
        List<Long> taskIdList = project.getTaskIdList_Long();
        //List of all unfinished tasks
        List<Task> unfinishedTaskList = new ArrayList<>();
        //List of developers on the team who are already assigned some tasks in that project
        List<Long> devsWithAlreadyAssignedTasks = new ArrayList<>();
        //Sorts the tasks linked to the project, saving those unassigned and saving the Ids of
        //developers with already assigned tasks (many duplicate Ids -> same developer with many tasks already assigned
        for (Long taskId : taskIdList) {
            if (getTask(taskId).getAssignedDevId() == null)
                unfinishedTaskList.add(getTask(taskId));
            else if (!getTask(taskId).isTaskState()) //Checks if the task is ongoing and not already completed
                devsWithAlreadyAssignedTasks.add(getTask(taskId).getAssignedDevId());
        }

        //If there are no unassigned tasks left, stops the algorithm and returns a null list
        if (unfinishedTaskList.isEmpty())
            return null;
        //List that stores number of assigned tasks for each developer to keep the equality requirement
        //Pair is: <Developer object, number of already assigned task (0 is the default)>
        List<PairOuter<Developer, Integer>> numberOfAssignedTasks = new ArrayList<>();
        for (Long devId : project.getDevIdList_Long()) {
            numberOfAssignedTasks.add(new PairOuter<>(getDeveloper(devId), Collections.frequency(devsWithAlreadyAssignedTasks, devId)));
        }
        //Cleaning no longer used objects
        devsWithAlreadyAssignedTasks = null;
        taskIdList = null;
        project = null;

        Specialization[] specializations = {Specialization.FRONTEND, Specialization.BACKEND, Specialization.DEVOPS, Specialization.UX_UI};
        for (Specialization specialization : specializations) {
            //Filters the list to choose only those with matching specialization for the current loop iteration
            List<PairOuter<Developer, Integer>> currentSpecializationDevs = new ArrayList<>(numberOfAssignedTasks.stream()
                    .filter(developerIntegerPair ->
                            developerIntegerPair.getL().getSpecialization().equals(specialization))
                    .toList());
            //If there are no developers for the current specialization, the next part is skipped
            if (currentSpecializationDevs.isEmpty())
                continue;
            //Finds the min number of assigned tasks for the current specialization from the <dev,number> pair list
            int minAssignedTaskNumber = MAX_INT;
            for (PairOuter<Developer, Integer> currentSpecializationDev : currentSpecializationDevs) {
                minAssignedTaskNumber = Math.min(minAssignedTaskNumber, currentSpecializationDev.getR());
            }
            //Chooses the tasks of matching specialization from the list of all unfinished tasks
            List<Task> currentSpecializationTasks = new ArrayList<>();
            for (Task task : unfinishedTaskList) {
                if (task.getSpecialization().equals(specialization))
                    currentSpecializationTasks.add(task);
            }
            //Iterates for each unfinished task with matching specialization
            for (Task currentSpecializationTask : currentSpecializationTasks) {
                //This is a temporary list containing one or more developers with minimal number of tasks assigned to them.
                //At least one developer will be added to it - the sole one with the minimal number.
                List<Developer> availableDevList = new ArrayList<>();
                for (PairOuter<Developer, Integer> currentSpecializationDev : currentSpecializationDevs) {
                    if (currentSpecializationDev.getR() == minAssignedTaskNumber)
                        availableDevList.add(currentSpecializationDev.getL());
                }
                //We create the default assignment for the first developer on the top of the list.
                Pair<Long, Long> defaultAssignment = new Pair<>(currentSpecializationTask.getTaskId(), availableDevList.getFirst().getDevId());
                //If there is only one developer available for choice, the next part is skipped
                if (availableDevList.size() == 1) minAssignedTaskNumber++;
                else {
                    //Creates the key used to search for the records of completing tasks with matching
                    //estimation in the past for each developer still eligible for choice.
                    String key = "-" + currentSpecializationTask.getEstimation() + "-";
                    //The 'shift' int variable is for accessing value from database records depending on the key used
                    int minTimeDone = MAX_INT, shift = key.length();
                    Long minDevId = null;
                    //Looks for the developer with minimal time in database records
                    for (Developer developer : availableDevList) {
                        String pastTasks = developer.getDoneTaskIds();
                        //If their record does not contain the key, the following part is skipped
                        if (!pastTasks.contains(key))
                            continue;
                        //Splits the String into separate task completion entries, stored in list for convenience
                        List<String> completedData = List.of(pastTasks.split(","));
                        for (String s : completedData) {
                            int keyIndex = s.indexOf(key);
                            //If the entry does not contain the key (indexOf returns -1), it is skipped
                            if (keyIndex > 0) {
                                //Time taken to complete the task is taken from the entry
                                int value = Integer.parseInt(s.substring(keyIndex + shift));
                                if (minTimeDone > value) {
                                    minTimeDone = value;
                                    minDevId = developer.getDevId();
                                }
                            }

                        }
                    }
                    //If there was a developer found, the default assignment pair is updated
                    if (minTimeDone != MAX_INT) {
                        defaultAssignment = new Pair<>(currentSpecializationTask.getTaskId(), minDevId);
                    }
                }
                //Assignment pair is added to the return
                returnedAssignmentList.add(defaultAssignment);
                //Increases the number of assigned task of the developer who just got assigned to the task
                Long tmp = defaultAssignment.getDevId();
                PairOuter<Developer, Integer> updateValue = currentSpecializationDevs.stream()
                        .filter(developerIntegerPair -> developerIntegerPair.getL().getDevId().equals(tmp))
                        .findAny().orElseThrow(); //Finds the pair
                currentSpecializationDevs.remove(updateValue); //Removes it
                Integer old = updateValue.getR();
                updateValue.setR(old + 1); //Updates it
                currentSpecializationDevs.add(updateValue); //Adds the updated one
            }
        }
        //Prepares the return data to be stored in the run-time only list for suggested
        //assignments for multiple projects
        StringBuilder stringBuilder = new StringBuilder();
        for (Pair<Long, Long> pair : returnedAssignmentList) {
            stringBuilder.append(pair.getTaskId()).append(",").append(pair.getDevId()).append(";");
        }
        //If the algorithm is run on the same project for another time, it replaces the previous suggestion
        ProjectManagmentIsApplication.assignmentList.removeIf(pair -> (pair.getL().equals(projectId)));
        ProjectManagmentIsApplication.assignmentList.add(new PairOuter<>(projectId, stringBuilder.toString()));

        return returnedAssignmentList;
    }

    /**
     * This is a private, auxiliary method to accept the passed assignment pair for the specified project.
     * It's main reason for existence is to avoid duplicate code and to check if the assignment was successful.
     *
     * @param s         Assignment pair passed as a String from the calling method
     * @param projectId Id of the project that the task from assignment pair is linked to
     * @Results: (1) Updates the task in the database, returns true.
     * <p></p> (2) Something went wrong (look at ProjectController.acceptAllAssignments), returns false.
     */
    private boolean assignmentSequence(String s, Long projectId) {
        int i = 0;
        while (s.charAt(i) != ',')
            i++;
        Long taskId = Long.parseLong(s.substring(0, i));
        Long devId = Long.parseLong(s.substring(i + 1));
        Task oldTask = getTask(taskId);
        //False in case of modification
        if (oldTask == null)
            return false;
        //Accepts the assignment pair
        updateTask(new Task(
                taskId,
                projectId,
                oldTask.getCreatorDevId(),
                oldTask.getDateCreated(),
                oldTask.getTaskName(),
                oldTask.getEstimation(),
                oldTask.getSpecialization(),
                oldTask.getTaskComment(),
                devId,
                false,
                null,
                null,
                null
        ));
        return true;
    }

    /**
     * This method updates accepts all the assignment for the specified project.
     *
     * @param projectId Id of the project the assignment pairs are accepted for
     * @Results: (1) All assignment pairs accepted, returns true.
     * <p></p> (2) The assignmentSequence method returned false, something went wrong
     * for one of the assignment pair or for all of them, returns false.
     */
    public boolean acceptAllAssignments(Long projectId) {
        //Gets all the assignment pairs for the project of the specified Id from the run-time only list
        PairOuter<Long, String> projectAssignments = ProjectManagmentIsApplication.assignmentList.stream()
                .filter(longStringPairOuter -> longStringPairOuter.getL().equals(projectId))
                .findAny().orElseThrow();
        //Splits the String into proper assignment pairs.
        String[] allAssignmentsString = projectAssignments.getR().split(";");
        for (String s : allAssignmentsString) {
            //Accepts the current assignment pair 
            if (!assignmentSequence(s, projectId)) return false;
        }
        return true;
    }

    /**
     * This method updates accepts one of the assignments for the specified project.
     *
     * @param projectId    Id of the project the assignment pair is accepted for
     * @param assignmentId Id of the assignment that should be accepted
     * @Results: (1) The assignment pair is accepted, returns true.
     * <p></p> (2) The assignmentSequence method returned false, something went wrong
     * for the assignment pair, returns false.
     */
    public boolean acceptOneAssignment(Long projectId, int assignmentId) {
        //Gets all the assignment pairs for the project of the specified Id from the run-time only list
        PairOuter<Long, String> projectAssignments = ProjectManagmentIsApplication.assignmentList.stream()
                .filter(longStringPairOuter -> longStringPairOuter.getL().equals(projectId))
                .findAny().orElseThrow();
        //Splits the String into proper assignment pairs and moves them to a list in order to access the 
        //specified pair later.
        List<String> allAssignmentsString = Arrays.stream(projectAssignments.getR().split(";")).toList();
        //If the user-input assignment Id is invalid even after the data validation due to modifications during run-time
        //it returns false after catching the exception.
        try {
            String s = allAssignmentsString.get(assignmentId - 1);
            //Accepts the chosen assignment pair
            return assignmentSequence(s, projectId);
        } catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }

    /**
     * This method returns the developer with most completed tasks.
     *
     * @Results: (1) At least one developer exists, returns its body.
     * <p></p> (2) No developers exist, returns null.
     */
    public Developer getMostTasksDev() {
        List<Developer> allDevList = developerRepository.findAll();
        Developer bestDev = null;
        int maxTasks = -1;

        for (Developer developer : allDevList) {
            //Counts the number of completed tasks for the current developer
            int howManyCompleted = developer.getDoneTaskIds().length()
                    - developer.getDoneTaskIds().replaceAll(";", "").length();
            if (howManyCompleted > maxTasks) {
                maxTasks = howManyCompleted;
                bestDev = developer;
            }
        }

        return bestDev;
    }
}
