import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Encuesta {
  idEncuesta: number;
  tituloEncuesta: string;
  objetivoEncuesta: string;
  instruccionesEncuesta: string;
  grupoMeta: string;
  estadoEncuesta: number;
  estadoNombre: string;
  fechaCreacion: string;
  fechaCierre: string;
  nombreUsuario: string;
  totalPreguntas: number;
  tokenPublico: string | null;
}

export interface EncuestaRequest {
  tituloEncuesta: string;
  objetivoEncuesta: string;
  instruccionesEncuesta: string;
  grupoMeta: string;
  fechaCierre: string;
}

@Injectable({ providedIn: 'root' })
export class EncuestaService {
  private api = 'http://localhost:8080/encuestas';

  constructor(private http: HttpClient) {}

  listar(): Observable<Encuesta[]> {
    return this.http.get<Encuesta[]>(this.api);
  }

  buscar(id: number): Observable<Encuesta> {
    return this.http.get<Encuesta>(`${this.api}/${id}`);
  }

  crear(data: EncuestaRequest): Observable<Encuesta> {
    return this.http.post<Encuesta>(this.api, data);
  }

  actualizar(id: number, data: EncuestaRequest): Observable<Encuesta> {
    return this.http.put<Encuesta>(`${this.api}/${id}`, data);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.api}/${id}`);
  }

  publicar(id: number): Observable<Encuesta> {
    return this.http.post<Encuesta>(`${this.api}/${id}/publicar`, {});
  }
}
