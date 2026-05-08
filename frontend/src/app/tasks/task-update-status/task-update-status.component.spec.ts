import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TaskUpdateStatusComponent } from './task-update-status.component';
import { TaskService } from '../task.service';
import { of, throwError } from 'rxjs';
import { Task } from '../task.model';
import { HttpErrorResponse } from '@angular/common/http';

const mockTask: Task = {
  id: 'task-id-1',
  title: 'Test Task',
  description: null,
  priority: 'MEDIA',
  status: 'CRIADA',
  dueDate: null,
  createdAt: '2026-05-06T14:30:00',
  completedAt: null,
};

describe('TaskUpdateStatusComponent', () => {
  let fixture: ComponentFixture<TaskUpdateStatusComponent>;
  let component: TaskUpdateStatusComponent;
  let mockTaskService: { updateStatus: jest.Mock };

  beforeEach(async () => {
    mockTaskService = { updateStatus: jest.fn() };

    await TestBed.configureTestingModule({
      imports: [TaskUpdateStatusComponent],
      providers: [{ provide: TaskService, useValue: mockTaskService }],
    }).compileComponents();

    fixture = TestBed.createComponent(TaskUpdateStatusComponent);
    component = fixture.componentInstance;
    component.task = mockTask;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialise form with current task status', () => {
    expect(component.form.get('status')?.value).toBe('CRIADA');
  });

  it('should call updateStatus and emit statusUpdated on success', () => {
    const updated: Task = { ...mockTask, status: 'ANDAMENTO' };
    mockTaskService.updateStatus.mockReturnValue(of(updated));
    const emitted: Task[] = [];
    component.statusUpdated.subscribe((t: Task) => emitted.push(t));

    component.form.patchValue({ status: 'ANDAMENTO' });
    component.save();

    expect(mockTaskService.updateStatus).toHaveBeenCalledWith('task-id-1', { status: 'ANDAMENTO' });
    expect(emitted[0].status).toBe('ANDAMENTO');
  });

  it('should display error message when update fails', () => {
    mockTaskService.updateStatus.mockReturnValue(
      throwError(() => new HttpErrorResponse({ status: 404 }))
    );
    component.form.patchValue({ status: 'CONCLUIDA' });
    component.save();
    expect(component.errorMessage).not.toBe('');
  });

  it('should reset loading flag on error', () => {
    mockTaskService.updateStatus.mockReturnValue(
      throwError(() => new HttpErrorResponse({ status: 500 }))
    );
    component.form.patchValue({ status: 'CONCLUIDA' });
    component.save();
    expect(component.loading).toBe(false);
  });
});
