import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { TaskService } from '../task.service';
import { Task } from '../task.model';
import { TaskUpdateStatusComponent } from '../task-update-status/task-update-status.component';

@Component({
  selector: 'app-task-detail',
  standalone: true,
  imports: [TaskUpdateStatusComponent],
  templateUrl: './task-detail.component.html',
})
export class TaskDetailComponent implements OnInit {
  task: Task | null = null;
  errorMessage = '';
  loading = false;

  constructor(
    private route: ActivatedRoute,
    private taskService: TaskService,
    private router: Router,
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id')!;
    this.taskService.getById(id).subscribe({
      next: task => (this.task = task),
      error: () => (this.errorMessage = 'Tarefa não encontrada'),
    });
  }

  onStatusUpdated(task: Task): void {
    this.task = task;
  }

  deleteTask(): void {
    if (!this.task) return;
    this.loading = true;
    this.errorMessage = '';
    this.taskService.delete(this.task.id).subscribe({
      next: () => this.router.navigate(['/']),
      error: () => {
        this.loading = false;
        this.errorMessage = 'Erro ao excluir tarefa. Tente novamente.';
      },
    });
  }
}
