import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';
import { environment } from '../../../environments/environment';

export interface PrivilegioResponseDTO {
  idPrivilegio: number;
  nombrePrivilegio: string;
  descripcionPrivilegio: string;
  urlPrivilegio: string;
}

export interface PrivilegioRequest {
  nombrePrivilegio: string;
  descripcionPrivilegio: string;
  urlPrivilegio: string;
}

@Injectable({ providedIn: 'root' })
export class PrivilegioService {
  private readonly API = `${environment.apiUrl}/privilegios`;

  constructor(private http: HttpClient, private auth: AuthService) {}

  private headers(): HttpHeaders {
    return new HttpHeaders({ Authorization: `Bearer ${this.auth.getToken()}` });
  }

  listar(): Observable<PrivilegioResponseDTO[]> {
    return this.http.get<PrivilegioResponseDTO[]>(this.API, { headers: this.headers() });
  }

  crear(dto: PrivilegioRequest): Observable<PrivilegioResponseDTO> {
    return this.http.post<PrivilegioResponseDTO>(this.API, dto, { headers: this.headers() });
  }

  actualizar(id: number, dto: PrivilegioRequest): Observable<PrivilegioResponseDTO> {
    return this.http.put<PrivilegioResponseDTO>(`${this.API}/${id}`, dto, { headers: this.headers() });
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API}/${id}`, { headers: this.headers() });
  }
}
