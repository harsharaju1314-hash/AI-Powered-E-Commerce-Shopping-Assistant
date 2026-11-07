import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { ApiService } from './api.service';
import { User, AuthResponse, UserLoginRequest, UserRegisterRequest } from '../models/user.model';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  private isAuthenticatedSubject = new BehaviorSubject<boolean>(false);
  public isAuthenticated$ = this.isAuthenticatedSubject.asObservable();

  constructor(private api: ApiService) {
    this.checkStoredUser();
  }

  private checkStoredUser() {
    const token = localStorage.getItem('omnistore_token');
    const userStr = localStorage.getItem('omnistore_user');
    if (token && userStr) {
      try {
        const user = JSON.parse(userStr);
        this.currentUserSubject.next(user);
        this.isAuthenticatedSubject.next(true);
      } catch (e) {
        this.logout();
      }
    }
  }

  public get currentUserValue(): User | null {
    return this.currentUserSubject.value;
  }

  login(credentials: UserLoginRequest): Observable<AuthResponse> {
    return this.api.post<AuthResponse>('/auth/login', credentials).pipe(
      tap(res => {
        localStorage.setItem('omnistore_token', res.access_token);
        localStorage.setItem('omnistore_user', JSON.stringify(res.user));
        this.currentUserSubject.next(res.user);
        this.isAuthenticatedSubject.next(true);
      })
    );
  }

  register(data: UserRegisterRequest): Observable<AuthResponse> {
    return this.api.post<AuthResponse>('/auth/register', data).pipe(
      tap(res => {
        localStorage.setItem('omnistore_token', res.access_token);
        localStorage.setItem('omnistore_user', JSON.stringify(res.user));
        this.currentUserSubject.next(res.user);
        this.isAuthenticatedSubject.next(true);
      })
    );
  }

  logout() {
    localStorage.removeItem('omnistore_token');
    localStorage.removeItem('omnistore_user');
    this.currentUserSubject.next(null);
    this.isAuthenticatedSubject.next(false);
  }

  loadProfile(): Observable<User> {
    return this.api.get<User>('/auth/me').pipe(
      tap(user => {
        localStorage.setItem('omnistore_user', JSON.stringify(user));
        this.currentUserSubject.next(user);
      })
    );
  }
}
