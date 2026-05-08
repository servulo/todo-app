import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TaskDetailComponent } from './task-detail.component';
import { TaskService } from '../task.service';
import { ActivatedRoute, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { Task } from '../task.model';
import { HttpErrorResponse } from '@angular/common/http';

const mockTask: Task = {
  id: 'task-id-1',
  title: 'Detail Task',
  description: 'Some description',
  priority: 'ALTA',
  status: 'ANDAMENTO',
  dueDate: '2026-06-01',
  createdAt: '2026-05-06T14:30:00',
  completedAt: null,
};

const completedTask: Task = {
  ...mockTask,
  status: 'CONCLUIDA',
  completedAt: '2026-05-07T10:00:00',
};

describe('TaskDetailComponent', () => {
  let fixture: ComponentFixture<TaskDetailComponent>;
  let component: TaskDetailComponent;
  let mockTaskService: { getById: jest.Mock; delete: jest.Mock };
  let mockRouter: { navigate: jest.Mock };

  const createComponent = async (task: Task | null = mockTask, error = false) => {
    mockTaskService.getById = error
      ? jest.fn().mockReturnValue(throwError(() => new HttpErrorResponse({ status: 404 })))
      : jest.fn().mockReturnValue(of(task!));

    await TestBed.configureTestingModule({
      imports: [TaskDetailComponent],
      providers: [
        { provide: TaskService, useValue: mockTaskService },
        { provide: Router, useValue: mockRouter },
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: { get: () => 'task-id-1' } } },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(TaskDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  };

  beforeEach(() => {
    mockTaskService = { getById: jest.fn(), delete: jest.fn() };
    mockRouter = { navigate: jest.fn() };
    TestBed.resetTestingModule();
  });

  it('should load and display all task fields', async () => {
    await createComponent();
    expect(component.task).toEqual(mockTask);
    expect(component.task?.title).toBe('Detail Task');
    expect(component.task?.description).toBe('Some description');
    expect(component.task?.priority).toBe('ALTA');
    expect(component.task?.status).toBe('ANDAMENTO');
    expect(component.task?.dueDate).toBe('2026-06-01');
    expect(component.task?.createdAt).toBeDefined();
  });

  it('should display completedAt for a CONCLUIDA task', async () => {
    await createComponent(completedTask);
    expect(component.task?.completedAt).toBe('2026-05-07T10:00:00');
    expect(component.task?.status).toBe('CONCLUIDA');
  });

  it('should set errorMessage on 404 response', async () => {
    await createComponent(null, true);
    expect(component.errorMessage).not.toBe('');
    expect(component.task).toBeNull();
  });

  it('should delete task and navigate away on success', async () => {
    await createComponent();
    mockTaskService.delete.mockReturnValue(of(void 0));
    component.deleteTask();
    expect(mockTaskService.delete).toHaveBeenCalledWith('task-id-1');
    expect(mockRouter.navigate).toHaveBeenCalled();
  });

  it('should set errorMessage and reset loading on delete error', async () => {
    await createComponent();
    mockTaskService.delete.mockReturnValue(throwError(() => new HttpErrorResponse({ status: 500 })));
    component.deleteTask();
    expect(component.errorMessage).not.toBe('');
    expect(component.loading).toBe(false);
  });

  it('should update task when onStatusUpdated is called', async () => {
    await createComponent();
    const updated: Task = { ...mockTask, status: 'CONCLUIDA', completedAt: '2026-05-07T10:00:00' };
    component.onStatusUpdated(updated);
    expect(component.task?.status).toBe('CONCLUIDA');
  });
});
