import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { TaskService } from '../task.service';
import { Task, TaskStatus } from '../task.model';

@Component({
  selector: 'app-task-update-status',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './task-update-status.component.html',
})
export class TaskUpdateStatusComponent implements OnInit {
  @Input() task!: Task;
  @Output() statusUpdated = new EventEmitter<Task>();

  form: FormGroup;
  readonly statuses: TaskStatus[] = ['CRIADA', 'ANDAMENTO', 'CONCLUIDA'];
  errorMessage = '';
  loading = false;

  constructor(private fb: FormBuilder, private taskService: TaskService) {
    this.form = this.fb.group({ status: ['', Validators.required] });
  }

  ngOnInit(): void {
    this.form.patchValue({ status: this.task.status });
  }

  save(): void {
    if (this.form.invalid) return;
    this.loading = true;
    this.errorMessage = '';
    this.taskService.updateStatus(this.task.id, this.form.value).subscribe({
      next: updated => {
        this.loading = false;
        this.statusUpdated.emit(updated);
      },
      error: err => {
        this.loading = false;
        this.errorMessage = err.error?.message ?? 'Erro ao atualizar status. Tente novamente.';
      },
    });
  }
}
