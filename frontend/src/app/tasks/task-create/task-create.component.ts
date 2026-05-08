import { Component } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { TaskService } from '../task.service';
import { TaskPriority } from '../task.model';

@Component({
  selector: 'app-task-create',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './task-create.component.html',
})
export class TaskCreateComponent {
  form: FormGroup;
  errorMessage = '';
  loading = false;
  readonly priorities: TaskPriority[] = ['BAIXA', 'MEDIA', 'ALTA'];

  constructor(
    private fb: FormBuilder,
    private taskService: TaskService,
    private router: Router,
  ) {
    this.form = this.fb.group({
      title: ['', [Validators.required, Validators.maxLength(255)]],
      description: [''],
      priority: ['MEDIA'],
      dueDate: [''],
    });
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading = true;
    this.errorMessage = '';
    const v = this.form.value;
    this.taskService
      .create({
        title: v.title,
        description: v.description || null,
        priority: v.priority,
        dueDate: v.dueDate || null,
      })
      .subscribe({
        next: task => this.router.navigate(['/tasks', task.id]),
        error: err => {
          this.loading = false;
          this.errorMessage =
            err.error?.errors?.[0]?.message ?? 'Erro ao criar tarefa. Tente novamente.';
        },
      });
  }
}
