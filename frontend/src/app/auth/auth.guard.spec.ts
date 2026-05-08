import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, Router, RouterStateSnapshot, UrlTree } from '@angular/router';
import { authGuard } from './auth.guard';
import { AuthService } from './auth.service';

describe('authGuard', () => {
  let mockAuthService: { isAuthenticated: jest.Mock };
  let mockRouter: { createUrlTree: jest.Mock };

  beforeEach(() => {
    mockAuthService = { isAuthenticated: jest.fn() };
    mockRouter = { createUrlTree: jest.fn().mockReturnValue({} as UrlTree) };

    TestBed.configureTestingModule({
      providers: [
        { provide: AuthService, useValue: mockAuthService },
        { provide: Router, useValue: mockRouter },
      ],
    });
  });

  const runGuard = () =>
    TestBed.runInInjectionContext(() =>
      authGuard({} as ActivatedRouteSnapshot, {} as RouterStateSnapshot)
    );

  it('should return true when user is authenticated', () => {
    mockAuthService.isAuthenticated.mockReturnValue(true);
    expect(runGuard()).toBe(true);
  });

  it('should redirect to /login when user is not authenticated', () => {
    mockAuthService.isAuthenticated.mockReturnValue(false);
    runGuard();
    expect(mockRouter.createUrlTree).toHaveBeenCalledWith(['/login']);
  });

  it('should return UrlTree (not true) when unauthenticated', () => {
    mockAuthService.isAuthenticated.mockReturnValue(false);
    const result = runGuard();
    expect(result).not.toBe(true);
  });
});
