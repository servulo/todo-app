import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TaskCreateComponent } from './task-create.component';
import { TaskService } from '../task.service';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { Task } from '../task.model';
import { HttpErrorResponse } from '@angular/common/http';

const mockCreatedTask: Task = {
  id: 'task-id-1',
  title: 'My Task',
  description: null,
  priority: 'MEDIA',
  status: 'CRIADA',
  dueDate: null,
  createdAt: '2026-05-06T14:30:00',
  completedAt: null,
};

describe('TaskCreateComponent', () => {
  let fixture: ComponentFixture<TaskCreateComponent>;
  let component: TaskCreateComponent;
  let mockTaskService: { create: jest.Mock };
  let mockRouter: { navigate: jest.Mock };

  beforeEach(async () => {
    mockTaskService = { create: jest.fn() };
    mockRouter = { navigate: jest.fn() };

    await TestBed.configureTestingModule({
      imports: [TaskCreateComponent],
      providers: [
        { provide: TaskService, useValue: mockTaskService },
        { provide: Router, useValue: mockRouter },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(TaskCreateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should NOT submit when title is blank', () => {
    component.form.patchValue({ title: '' });
    component.submit();
    expect(mockTaskService.create).not.toHaveBeenCalled();
  });

  it('should mark title as touched when submitting blank form', () => {
    component.submit();
    expect(component.form.get('title')?.touched).toBe(true);
  });

  it('should call TaskService.create and navigate to task detail on success', () => {
    mockTaskService.create.mockReturnValue(of(mockCreatedTask));
    component.form.patchValue({ title: 'My Task', priority: 'MEDIA' });
    component.submit();
    expect(mockTaskService.create).toHaveBeenCalled();
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/tasks', 'task-id-1']);
  });

  it('should display error message on API error', () => {
    const error = new HttpErrorResponse({
      status: 400,
      error: { errors: [{ field: 'title', message: 'O título é obrigatório' }] },
    });
    mockTaskService.create.mockReturnValue(throwError(() => error));
    component.form.patchValue({ title: 'Bad' });
    component.submit();
    expect(component.errorMessage).not.toBe('');
  });

  it('should reset loading flag on error', () => {
    mockTaskService.create.mockReturnValue(throwError(() => new HttpErrorResponse({ status: 500 })));
    component.form.patchValue({ title: 'Task' });
    component.submit();
    expect(component.loading).toBe(false);
  });
});
