# BACKEND PROJECT

## Kilka słów o mnie

### Powtórka z formularza i kierunek:
Imię i Nazwisko: Ignacy Smoliński

E-mail: ignacy.smolinski@gmail.com

Wydział: W4N - Wydział Informatyki i Telekomunikacji

Rok: 1, 1 semestr

Kierunek: Informatyka Stosowana

### A teraz trochę więcej:
Przed rozpoczęciem studiów miałem styczność jedynie z C++, to w nim pisałem maturę z informatyki, oraz z programowaniem algorytmicznym. Pierwszy program w Java z użyciem klas napisałem niecały tydzień przed rozpoczęciem studiów, a o API, czy zasadach REST dowiedziałem się dopiero po przeczytaniu zadania rekrutacyjnego około 13 października.

Mocno zaangażowałem się w zadanie rekrutacyjne, stopniowo poznawając Spring Boot, requesty w HTML, współpracę z zewnętrzną bazą danych (MySQL), a pod sam koniec pisanie testów jednostkowych i pierwszej w życiu dokumentacji. Można więc śmiało powiedzieć, że początkowo nie wiedziałem na ten temtat nic, a jednak po niecałym miesiącu prawie codziennej pracy nad projektem, czuje się w komfortowo zarówno w architekurze API, jak i w samej Javie.

## O projekcie

### Spełnione wymagania

Projekt spełnia wszystkie wymagania z Waszej listy, łącznie z "Nice to have" - testami jednostkowymi oraz dokumentacją. Całość jest napisana w Javie z użyciem Spring Boot, a zewnętrzną bazą danych jest MySQL. Requesty testowałem przy użyciu Postman'a, oprócz testów jednostkowych. 

### Baza danych

Projekt korzysta z MySQl, a dokładniej z jednej bazy danych posiadającej trzy tabele: Developer, Project i Task. Ten kawałek kodu umożliwia stworzenie odpowiednich tabel z pasującymi nazwami pól, wymaganymi do odpowiedniego działania programu:
```SQL
CREATE DATABASE restapi;
USE restapi;
CREATE TABLE Developer (
    devId int NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name varchar(20) NOT NULL,
    projectIds varchar(500),
    specialization varchar(10),
    doneTasksIds varchar(500)
);
CREATE TABLE Task (
    taskId INT AUTO_INCREMENT PRIMARY KEY NOT NULL,
    projectId INT,
    creatorDevId INT,
    dateCreated DATE,
    taskName VARCHAR(30),
    estimation INT,
    specialization VARCHAR(10),
    taskComment varchar(1000),
    assignedDevId INT,
    taskState BOOLEAN,
    dateDone DATE,
    timeDone INT,
    doneDevId INT
);
CREATE TABLE Project (
    projectId INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    creatorDevId INT,
    projectName VARCHAR(30),
    devIdList VARCHAR(500),
    taskIdList VARCHAR(500)
);
```
Reszta ustawień dotyczączych połączenia bazy danych znajduje się w pliku
    
    
    
    
    
