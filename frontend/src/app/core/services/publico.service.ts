import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Pregunta } from './pregunta.service';
import { environment } from '../../../environments/environment';

export interface EncuestaPublica {
  idEncuesta: number;
  tituloEncuesta: string;
  objetivoEncuesta: string;
  instruccionesEncuesta: string;
  grupoMeta: string;
  fechaCierre: string;
  totalPreguntas: number;
}

export interface DetalleEnvio {
  idPregunta: number;
  texto: string;
  idOpcion: number | null;
  idOpciones: number[];
  valor: number | null;
  ranking: number[];
  otrosTexto: string;
}

export interface RespuestaConfirmacion {
  numeroRegistro: number;
  fechaRespuesta: string;
}

export interface EstadoRespuesta {
  yaRespondido: boolean;
  tieneBorrador: boolean;
}

export interface MiEncuesta {
  idEncuesta: number;
  tituloEncuesta: string;
  objetivoEncuesta: string;
  tokenPublico: string;
  estadoRespuesta: number; // 1=BORRADOR, 2=ENVIADA
  estadoNombre: string;
  fecha: string;
  numeroRegistro: number | null;
}

export interface EncuestaDisponible {
  idEncuesta: number;
  tituloEncuesta: string;
  objetivoEncuesta: string;
  grupoMeta: string;
  fechaCierre: string;
  totalPreguntas: number;
  tokenPublico: string;
  estadoRespuesta: number | null; // null=sin responder, 1=borrador, 2=enviada
}

@Injectable({ providedIn: 'root' })
export class PublicoService {
  private api = `${environment.apiUrl}/publico`;
  private responderApi = `${environment.apiUrl}/responder`;
  private misEncuestasApi = `${environment.apiUrl}/mis-encuestas`;
  private disponiblesApi = `${environment.apiUrl}/encuestas-disponibles`;

  constructor(private http: HttpClient) {}

  // Públicos (bienvenida + preguntas)
  cargarEncuesta(token: string): Observable<EncuestaPublica> {
    return this.http.get<EncuestaPublica>(`${this.api}/encuestas/${token}`);
  }

  cargarPreguntas(token: string): Observable<Pregunta[]> {
    return this.http.get<Pregunta[]>(`${this.api}/encuestas/${token}/preguntas`);
  }

  // Autenticados (el JWT lo agrega el interceptor)
  estado(token: string): Observable<EstadoRespuesta> {
    return this.http.get<EstadoRespuesta>(`${this.responderApi}/${token}/estado`);
  }

  enviar(token: string, respuestas: DetalleEnvio[]): Observable<RespuestaConfirmacion> {
    return this.http.post<RespuestaConfirmacion>(`${this.responderApi}/${token}`, { respuestas });
  }

  obtenerBorrador(token: string): Observable<DetalleEnvio[]> {
    return this.http.get<DetalleEnvio[]>(`${this.responderApi}/${token}/borrador`);
  }

  guardarBorrador(token: string, respuestas: DetalleEnvio[]): Observable<{ mensaje: string }> {
    return this.http.post<{ mensaje: string }>(`${this.responderApi}/${token}/borrador`, { respuestas });
  }

  // Etapa 18 - Panel del encuestado
  misEncuestas(): Observable<MiEncuesta[]> {
    return this.http.get<MiEncuesta[]>(this.misEncuestasApi);
  }

  // Catálogo de encuestas disponibles para responder
  disponibles(): Observable<EncuestaDisponible[]> {
    return this.http.get<EncuestaDisponible[]>(this.disponiblesApi);
  }
}
