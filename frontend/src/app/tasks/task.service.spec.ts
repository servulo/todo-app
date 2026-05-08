import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TaskService } from './task.service';
import { environment } from '../../../environments/environment';
import { Task } from './task.model';

const mockTask: Task = {
  id: '550e8400-e29b-41d4-a716-446655440000',
  title: 'Test Task',
  description: null,
  priority: 'MEDIA',
  status: 'CRIADA',
  dueDate: null,
  createdAt: '2026-05-06T14:30:00',
  completedAt: null,
};

describe('TaskService', () => {
  let service: TaskService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [TaskService],
    });
    service = TestBed.inject(TaskService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('create() should POST to /api/tasks and return task with status CRIADA', () => {
    service.create({ title: 'Test Task' }).subscribe(task => {
      expect(task.status).toBe('CRIADA');
      expect(task.id).toBeDefined();
    });
    const req = httpMock.expectOne(`${environment.apiUrl}/tasks`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ title: 'Test Task' });
    req.flush(mockTask);
  });

  it('updateFields() should PUT to /api/tasks/{id}', () => {
    const update = { title: 'Updated', priority: 'ALTA' as const };
    service.updateFields(mockTask.id, update).subscribe(task => {
      expect(task.title).toBe('Updated');
    });
    const req = httpMock.expectOne(`${environment.apiUrl}/tasks/${mockTask.id}`);
    expect(req.request.method).toBe('PUT');
    req.flush({ ...mockTask, title: 'Updated' });
  });

  it('updateStatus() should PATCH to /api/tasks/{id}/status', () => {
    service.updateStatus(mockTask.id, { status: 'CONCLUIDA' }).subscribe(task => {
      expect(task.status).toBe('CONCLUIDA');
      expect(task.completedAt).not.toBeNull();
    });
    const req = httpMock.expectOne(`${environment.apiUrl}/tasks/${mockTask.id}/status`);
    expect(req.request.method).toBe('PATCH');
    expect(req.request.body).toEqual({ status: 'CONCLUIDA' });
    req.flush({ ...mockTask, status: 'CONCLUIDA', completedAt: '2026-05-06T16:00:00' });
  });

  it('delete() should DELETE /api/tasks/{id}', () => {
    let completed = false;
    service.delete(mockTask.id).subscribe({ complete: () => (completed = true) });
    const req = httpMock.expectOne(`${environment.apiUrl}/tasks/${mockTask.id}`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
    expect(completed).toBe(true);
  });

  it('getById() should GET /api/tasks/{id} and return task', () => {
    service.getById(mockTask.id).subscribe(task => {
      expect(task).toEqual(mockTask);
    });
    const req = httpMock.expectOne(`${environment.apiUrl}/tasks/${mockTask.id}`);
    expect(req.request.method).toBe('GET');
    req.flush(mockTask);
  });
});
