package com.academy.models.task;

import com.academy.models.shared.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name="task")

// a task tem o enumerado task_type para saber qual o tipo de task que vai executar
// tem o status para saber se ja foi ou nao realizada a operação
// tem o schedule_at para definirmos com que frequência queremos que a task seja executada

@ToString(callSuper = true)
public class Task extends BaseEntity {

    @Column(name = "schedule_at", nullable = false)
    private LocalDateTime schedule_at;

    //pending, failed, done...
    @Column(name = "status")
    private String status;

    @Enumerated(EnumType.STRING)
    @Column(name = "task_type", nullable = false)
    private TaskTypeEnum taskTypeEnum;


}
