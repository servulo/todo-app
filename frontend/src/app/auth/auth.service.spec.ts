import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AuthService } from './auth.service';
import { environment } from '../../../environments/environment';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AuthService],
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
    localStorage.clear();
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('should store JWT on successful login', () => {
    service.login({ username: 'user', password: 'pass' }).subscribe();
    const req = httpMock.expectOne(`${environment.authApiUrl}/login`);
    expect(req.request.method).toBe('POST');
    req.flush({ token: 'eyJ.test.token' });
    expect(localStorage.getItem('auth_token')).toBe('eyJ.test.token');
  });

  it('should POST to authApiUrl with provided credentials', () => {
    service.login({ username: 'testuser', password: 'testpass' }).subscribe();
    const req = httpMock.expectOne(`${environment.authApiUrl}/login`);
    expect(req.request.body).toEqual({ username: 'testuser', password: 'testpass' });
    req.flush({ token: 'token' });
  });

  it('should surface 401 error without storing token', done => {
    service.login({ username: 'user', password: 'wrong' }).subscribe({
      error: err => {
        expect(err.status).toBe(401);
        expect(localStorage.getItem('auth_token')).toBeNull();
        done();
      },
    });
    const req = httpMock.expectOne(`${environment.authApiUrl}/login`);
    req.flush({ message: 'Invalid credentials' }, { status: 401, statusText: 'Unauthorized' });
  });

  it('isAuthenticated returns false when no token', () => {
    expect(service.isAuthenticated()).toBe(false);
  });

  it('isAuthenticated returns true when token exists', () => {
    localStorage.setItem('auth_token', 'some.jwt.token');
    expect(service.isAuthenticated()).toBe(true);
  });

  it('getToken returns null when no token stored', () => {
    expect(service.getToken()).toBeNull();
  });

  it('getToken returns stored token', () => {
    localStorage.setItem('auth_token', 'stored.token');
    expect(service.getToken()).toBe('stored.token');
  });

  it('logout removes token from localStorage', () => {
    localStorage.setItem('auth_token', 'token-to-remove');
    service.logout();
    expect(localStorage.getItem('auth_token')).toBeNull();
  });
});
