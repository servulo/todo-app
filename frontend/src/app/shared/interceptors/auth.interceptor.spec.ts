import { TestBed } from '@angular/core/testing';
import { HttpRequest, HttpHandlerFn, HttpErrorResponse } from '@angular/common/http';
import { authInterceptor } from './auth.interceptor';
import { AuthService } from '../../auth/auth.service';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';

describe('authInterceptor', () => {
  let mockAuthService: { getToken: jest.Mock; logout: jest.Mock };
  let mockRouter: { navigate: jest.Mock };

  beforeEach(() => {
    mockAuthService = { getToken: jest.fn(), logout: jest.fn() };
    mockRouter = { navigate: jest.fn() };

    TestBed.configureTestingModule({
      providers: [
        { provide: AuthService, useValue: mockAuthService },
        { provide: Router, useValue: mockRouter },
      ],
    });
  });

  const runInterceptor = (req: HttpRequest<unknown>, handler: HttpHandlerFn) =>
    TestBed.runInInjectionContext(() => authInterceptor(req, handler));

  it('should attach Bearer token header when JWT is present', done => {
    mockAuthService.getToken.mockReturnValue('test.jwt.token');
    const req = new HttpRequest('GET', '/api/tasks');
    const handler: HttpHandlerFn = r => {
      expect(r.headers.get('Authorization')).toBe('Bearer test.jwt.token');
      return of(null as never);
    };
    runInterceptor(req, handler).subscribe({ complete: done });
  });

  it('should NOT attach Authorization header when no JWT in localStorage', done => {
    mockAuthService.getToken.mockReturnValue(null);
    const req = new HttpRequest('GET', '/api/tasks');
    const handler: HttpHandlerFn = r => {
      expect(r.headers.get('Authorization')).toBeNull();
      return of(null as never);
    };
    runInterceptor(req, handler).subscribe({ complete: done });
  });

  it('should logout and redirect to /login on 401 response from todo-app', done => {
    mockAuthService.getToken.mockReturnValue('expired.token');
    const req = new HttpRequest('GET', '/api/tasks');
    const error = new HttpErrorResponse({ status: 401, url: '/api/tasks' });
    const handler: HttpHandlerFn = () => throwError(() => error);

    runInterceptor(req, handler).subscribe({
      error: () => {
        expect(mockAuthService.logout).toHaveBeenCalled();
        expect(mockRouter.navigate).toHaveBeenCalledWith(['/login']);
        done();
      },
    });
  });

  it('should re-throw non-401 errors without logout', done => {
    mockAuthService.getToken.mockReturnValue('valid.token');
    const req = new HttpRequest('GET', '/api/tasks');
    const error = new HttpErrorResponse({ status: 500 });
    const handler: HttpHandlerFn = () => throwError(() => error);

    runInterceptor(req, handler).subscribe({
      error: err => {
        expect(err.status).toBe(500);
        expect(mockAuthService.logout).not.toHaveBeenCalled();
        done();
      },
    });
  });
});
