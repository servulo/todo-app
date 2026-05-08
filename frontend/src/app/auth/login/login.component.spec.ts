import { ComponentFixture, TestBed } from '@angular/core/testing';
import { LoginComponent } from './login.component';
import { AuthService } from '../auth.service';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';

describe('LoginComponent', () => {
  let fixture: ComponentFixture<LoginComponent>;
  let component: LoginComponent;
  let mockAuthService: { login: jest.Mock; isAuthenticated: jest.Mock };
  let mockRouter: { navigate: jest.Mock };

  beforeEach(async () => {
    mockAuthService = {
      login: jest.fn(),
      isAuthenticated: jest.fn().mockReturnValue(false),
    };
    mockRouter = { navigate: jest.fn() };

    await TestBed.configureTestingModule({
      imports: [LoginComponent],
      providers: [
        { provide: AuthService, useValue: mockAuthService },
        { provide: Router, useValue: mockRouter },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should redirect to /tasks on init when already authenticated', async () => {
    mockAuthService.isAuthenticated.mockReturnValue(true);
    fixture.destroy();
    await TestBed.configureTestingModule({
      imports: [LoginComponent],
      providers: [
        { provide: AuthService, useValue: mockAuthService },
        { provide: Router, useValue: mockRouter },
      ],
    }).compileComponents();
    const f = TestBed.createComponent(LoginComponent);
    f.detectChanges();
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/tasks']);
  });

  it('should NOT call auth-app when username is blank', () => {
    component.form.patchValue({ username: '', password: 'pass' });
    component.submit();
    expect(mockAuthService.login).not.toHaveBeenCalled();
  });

  it('should NOT call auth-app when password is blank', () => {
    component.form.patchValue({ username: 'user', password: '' });
    component.submit();
    expect(mockAuthService.login).not.toHaveBeenCalled();
  });

  it('should mark fields as touched when submitting empty form', () => {
    component.submit();
    expect(component.form.get('username')?.touched).toBe(true);
    expect(component.form.get('password')?.touched).toBe(true);
  });

  it('should call login and navigate to /tasks on success', () => {
    mockAuthService.login.mockReturnValue(of({ token: 'jwt.token' }));
    component.form.patchValue({ username: 'user', password: 'pass' });
    component.submit();
    expect(mockAuthService.login).toHaveBeenCalledWith({ username: 'user', password: 'pass' });
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/tasks']);
  });

  it('should show "Credenciais inválidas" on 401 and not redirect', () => {
    const error = new HttpErrorResponse({ status: 401 });
    mockAuthService.login.mockReturnValue(throwError(() => error));
    component.form.patchValue({ username: 'user', password: 'wrong' });
    component.submit();
    expect(component.errorMessage).toBe('Credenciais inválidas');
    expect(mockRouter.navigate).not.toHaveBeenCalled();
  });

  it('should show service unavailable message on network error', () => {
    const error = new HttpErrorResponse({ status: 0 });
    mockAuthService.login.mockReturnValue(throwError(() => error));
    component.form.patchValue({ username: 'user', password: 'pass' });
    component.submit();
    expect(component.errorMessage).toBe('Serviço indisponível, tente novamente');
  });

  it('should reset loading flag on error', () => {
    mockAuthService.login.mockReturnValue(throwError(() => new HttpErrorResponse({ status: 401 })));
    component.form.patchValue({ username: 'user', password: 'pass' });
    component.submit();
    expect(component.loading).toBe(false);
  });
});
