package com.project.projectmanagment_is;

import com.project.projectmanagment_is.controller.ProjectController;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import com.project.projectmanagment_is.repository.DeveloperRepository;
import com.project.projectmanagment_is.repository.TaskRepository;
import com.project.projectmanagment_is.service.ProjectService;

import java.util.*;

@SpringBootApplication
@ComponentScan(basePackageClasses = ProjectController.class)
@ComponentScan(basePackageClasses = ProjectService.class)
@ComponentScan(basePackageClasses = DeveloperRepository.class)
@ComponentScan(basePackageClasses = TaskRepository.class)
public class ProjectManagmentIsApplication {

    //Pre-computed values from the Fibonacci sequence
    public static Set<Integer> estimationValues = new HashSet<>(Arrays.asList(1, 2, 3, 5, 8, 13, 21, 34, 55, 89, 144, 233, 377, 610, 987, 1597, 2584, 4181, 6765, 10946, 17711, 28657, 46368, 75025, 121393, 196418, 317811, 514229, 832040, 1346269, 2178309, 3524578, 5702887, 9227465, 14930352, 24157817, 39088169, 63245986, 102334155, 165580141, 267914296, 433494437, 701408733, 1134903170, 1836311903));
    //Run-time-only list for the assignments suggested by the assignment algorithm.
    //Pair: <ProjectIdWeRunAlgorithmOn, AssignmentPairsAsStringSplitByCommaAndSemiColon>
    //Example: [<1 , "1,1;2,1;3,3;">, <2 , "4,5;5,1">   ]
    public static List<PairOuter<Long, String>> assignmentList = new ArrayList<>();

    public static void main(String[] args) {
        SpringApplication.run(ProjectManagmentIsApplication.class, args);
    }

}
