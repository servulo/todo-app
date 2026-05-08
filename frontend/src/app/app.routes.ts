import { Routes } from '@angular/router';
import { authGuard } from './auth/auth.guard';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () =>
      import('./auth/login/login.component').then(m => m.LoginComponent),
  },
  {
    path: 'tasks',
    canActivate: [authGuard],
    children: [
      {
        path: 'new',
        loadComponent: () =>
          import('./tasks/task-create/task-create.component').then(m => m.TaskCreateComponent),
      },
      {
        path: ':id',
        loadComponent: () =>
          import('./tasks/task-detail/task-detail.component').then(m => m.TaskDetailComponent),
      },
    ],
  },
  { path: '', redirectTo: 'tasks', pathMatch: 'full' },
  { path: '**', redirectTo: 'tasks' },
];
