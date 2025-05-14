package com.academy.models.task;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name="task")

// a task tem o enumerado task_type para saber qual o tipo de task que vai executar
// tem o status para saber se ja foi ou nao realizada a operação
// tem o schedule_at para definirmos com que frequência queremos que a task seja executada

public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "task_id")
    private long id;


    @Column(name = "schedule_at", nullable = false)
    private LocalDateTime schedule_at;

    //pending, failed, done...
    @Column(name = "status")
    private String status;

    @Column(name = "created_at")
    private LocalTime createdAt;

    @Column(name = "update_at")
    private LocalTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "task_type", nullable = false)
    private TaskTypeEnum taskTypeEnum;


}
