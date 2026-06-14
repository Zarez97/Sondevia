import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

export interface LoginRequest {
  email: string;
  contrasenia: string;
}

export interface RegisterRequest {
  nombre: string;
  email: string;
  contrasenia: string;
  fechaNacimiento: string;
}

export interface LoginResponse {
  token: string;
  nombreUser: string;
  emailUser: string;
  roles: string[];
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly API = 'http://localhost:8080/auth';
  private readonly TOKEN_KEY = 'auth_token';
  private readonly USER_KEY = 'auth_user';

  constructor(private http: HttpClient) {}

  login(credentials: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.API}/login`, credentials).pipe(
      tap(response => this.guardarSesion(response))
    );
  }

  registrar(data: RegisterRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.API}/registro`, data).pipe(
      tap(response => this.guardarSesion(response))
    );
  }

  private guardarSesion(response: LoginResponse): void {
    localStorage.setItem(this.TOKEN_KEY, response.token);
    localStorage.setItem(this.USER_KEY, JSON.stringify({
      nombre: response.nombreUser,
      email: response.emailUser,
      roles: response.roles ?? []
    }));
  }

  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
  }

  isLoggedIn(): boolean {
    return !!localStorage.getItem(this.TOKEN_KEY);
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  getUser(): { nombre: string; email: string; roles: string[] } | null {
    const user = localStorage.getItem(this.USER_KEY);
    return user ? JSON.parse(user) : null;
  }

  getRoles(): string[] {
    return this.getUser()?.roles ?? [];
  }

  tieneRol(rol: string): boolean {
    return this.getRoles().includes(rol);
  }
}
