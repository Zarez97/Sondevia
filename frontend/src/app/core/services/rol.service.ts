import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';
import { PrivilegioResponseDTO } from './privilegio.service';

export interface RolResponse {
  idRol: number;
  nombreRol: string;
  descripcionRol: string;
  privilegios: PrivilegioResponseDTO[];
}

export interface RolRequest {
  nombreRol: string;
  descripcionRol: string;
}

@Injectable({ providedIn: 'root' })
export class RolService {
  private readonly API = 'http://localhost:8080/roles';

  constructor(private http: HttpClient, private auth: AuthService) {}

  private headers(): HttpHeaders {
    return new HttpHeaders({ Authorization: `Bearer ${this.auth.getToken()}` });
  }

  listar(): Observable<RolResponse[]> {
    return this.http.get<RolResponse[]>(this.API, { headers: this.headers() });
  }

  crear(dto: RolRequest): Observable<RolResponse> {
    return this.http.post<RolResponse>(this.API, dto, { headers: this.headers() });
  }

  actualizar(id: number, dto: RolRequest): Observable<RolResponse> {
    return this.http.put<RolResponse>(`${this.API}/${id}`, dto, { headers: this.headers() });
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API}/${id}`, { headers: this.headers() });
  }

  asignarPrivilegios(rolId: number, privilegioIds: number[]): Observable<RolResponse> {
    return this.http.put<RolResponse>(`${this.API}/${rolId}/privilegios`, privilegioIds, { headers: this.headers() });
  }

  asignarRolAUsuario(rolId: number, usuarioId: number): Observable<RolResponse> {
    return this.http.post<RolResponse>(`${this.API}/${rolId}/usuarios/${usuarioId}`, {}, { headers: this.headers() });
  }

  quitarRolAUsuario(rolId: number, usuarioId: number): Observable<void> {
    return this.http.delete<void>(`${this.API}/${rolId}/usuarios/${usuarioId}`, { headers: this.headers() });
  }
}
